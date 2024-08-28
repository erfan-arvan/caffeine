/*
 * Copyright 2018 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache;

import static java.util.Objects.requireNonNull;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

/**
 * This class provides a skeletal implementation of the {@link AsyncLoadingCache} interface to
 * minimize the effort required to implement a {@link LocalCache}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
interface LocalAsyncCache<K, V> extends AsyncCache<K, V> {

    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Logger logger = Logger.getLogger(LocalAsyncCache.class.getName());

    /**
     * Returns the backing {@link LocalCache} data store.
     */
    LocalCache<K, CompletableFuture<V>> cache();

    /**
     * Returns the policy supported by this implementation and its configuration.
     */
    Policy<K, V> policy();

    default @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CompletableFuture<V> getIfPresent(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull LocalAsyncCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object key) {
        return cache().getIfPresent(key, /* recordStats */
        true);
    }

    default @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CompletableFuture<V> get(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull LocalAsyncCache<K, V> this, K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Function<? super K, ? extends V> mappingFunction) {
        requireNonNull(mappingFunction);
        return get(key, (k1, executor) -> CompletableFuture.supplyAsync(() -> mappingFunction.apply(key), executor));
    }

    default CompletableFuture<V> get(K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull BiFunction<? super K, Executor, CompletableFuture<V>> mappingFunction) {
        return get(key, mappingFunction, /* recordStats */
        true);
    }

    default CompletableFuture<V> get(K key, BiFunction<? super K, Executor, CompletableFuture<V>> mappingFunction, boolean recordStats) {
        long startTime = cache().statsTicker().read();
        CompletableFuture<V>[] result = new CompletableFuture[1];
        CompletableFuture<V> future = cache().computeIfAbsent(key, k -> {
            result[0] = mappingFunction.apply(key, cache().executor());
            return requireNonNull(result[0]);
        }, recordStats, /* recordLoad */
        false);
        if (result[0] != null) {
            AtomicBoolean completed = new AtomicBoolean();
            result[0].whenComplete((value, error) -> {
                if (!completed.compareAndSet(false, true)) {
                    // Ignore multiple invocations due to ForkJoinPool retrying on delays
                    return;
                }
                long loadTime = cache().statsTicker().read() - startTime;
                if (value == null) {
                    if (error != null) {
                        logger.log(Level.WARNING, "Exception thrown during asynchronous load", error);
                    }
                    cache().statsCounter().recordLoadFailure(loadTime);
                    cache().remove(key, result[0]);
                } else {
                    // update the weight and expiration timestamps
                    cache().replace(key, result[0], result[0]);
                    cache().statsCounter().recordLoadSuccess(loadTime);
                }
            });
        }
        return future;
    }

    default void put(K key, CompletableFuture<V> valueFuture) {
        if (valueFuture.isCompletedExceptionally() || (valueFuture.isDone() && (valueFuture.join() == null))) {
            cache().statsCounter().recordLoadFailure(0L);
            cache().remove(key);
            return;
        }
        AtomicBoolean completed = new AtomicBoolean();
        long startTime = cache().statsTicker().read();
        cache().put(key, valueFuture);
        valueFuture.whenComplete((value, error) -> {
            if (!completed.compareAndSet(false, true)) {
                // Ignore multiple invocations due to ForkJoinPool retrying on delays
                return;
            }
            long loadTime = cache().statsTicker().read() - startTime;
            if (value == null) {
                if (error != null) {
                    logger.log(Level.WARNING, "Exception thrown during asynchronous load", error);
                }
                cache().remove(key, valueFuture);
                cache().statsCounter().recordLoadFailure(loadTime);
            } else {
                // update the weight and expiration timestamps
                cache().replace(key, valueFuture, valueFuture);
                cache().statsCounter().recordLoadSuccess(loadTime);
            }
        });
    }

    /* ---------------- Synchronous views -------------- */
    final class CacheView<K, V> extends AbstractCacheView<K, V> {

        private static final long serialVersionUID = 1L;

        final LocalAsyncCache<K, V> asyncCache;

        CacheView(LocalAsyncCache<K, V> asyncCache) {
            this.asyncCache = requireNonNull(asyncCache);
        }

        LocalAsyncCache<K, V> asyncCache() {
            return asyncCache;
        }
    }

    abstract class AbstractCacheView<K, V> implements Cache<K, V>, Serializable {

        transient AsMapView<K, V> asMapView;

        abstract LocalAsyncCache<K, V> asyncCache();

        public V getIfPresent(Object key) {
            CompletableFuture<V> future = asyncCache().cache().getIfPresent(key, /* recordStats */
            true);
            return Async.getIfReady(future);
        }

        public Map<K, V> getAllPresent(Iterable<?> keys) {
            Set<Object> uniqueKeys = new LinkedHashSet<>();
            for (Object key : keys) {
                uniqueKeys.add(key);
            }
            int misses = 0;
            Map<Object, Object> result = new LinkedHashMap<>();
            for (Object key : uniqueKeys) {
                CompletableFuture<V> future = asyncCache().cache().get(key);
                Object value = Async.getIfReady(future);
                if (value == null) {
                    misses++;
                } else {
                    result.put(key, value);
                }
            }
            asyncCache().cache().statsCounter().recordMisses(misses);
            asyncCache().cache().statsCounter().recordHits(result.size());
            Map<K, V> castedResult = (Map<K, V>) result;
            return Collections.unmodifiableMap(castedResult);
        }

        public V get(K key, Function<? super K, ? extends V> mappingFunction) {
            requireNonNull(mappingFunction);
            CompletableFuture<V> future = asyncCache().get(key, (k, executor) -> CompletableFuture.supplyAsync(() -> mappingFunction.apply(key), executor));
            try {
                return future.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                }
                throw new CompletionException(e.getCause());
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        }

        public void put(K key, V value) {
            requireNonNull(value);
            asyncCache().cache().put(key, CompletableFuture.completedFuture(value));
        }

        public void putAll(Map<? extends K, ? extends V> map) {
            map.forEach(this::put);
        }

        public void invalidate(Object key) {
            asyncCache().cache().remove(key);
        }

        public void invalidateAll(Iterable<?> keys) {
            asyncCache().cache().invalidateAll(keys);
        }

        public void invalidateAll() {
            asyncCache().cache().clear();
        }

        public long estimatedSize() {
            return asyncCache().cache().size();
        }

        public CacheStats stats() {
            return asyncCache().cache().statsCounter().snapshot();
        }

        public void cleanUp() {
            asyncCache().cache().cleanUp();
        }

        public Policy<K, V> policy() {
            return asyncCache().policy();
        }

        public ConcurrentMap<K, V> asMap() {
            return (asMapView == null) ? (asMapView = new AsMapView<>(asyncCache().cache())) : asMapView;
        }
    }

    final class AsMapView<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

        final LocalCache<K, CompletableFuture<V>> delegate;

        Collection<V> values;

        Set<Entry<K, V>> entries;

        AsMapView(LocalCache<K, CompletableFuture<V>> delegate) {
            this.delegate = delegate;
        }

        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        public int size() {
            return delegate.size();
        }

        public void clear() {
            delegate.clear();
        }

        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        public boolean containsValue(Object value) {
            requireNonNull(value);
            for (CompletableFuture<V> valueFuture : delegate.values()) {
                if (value.equals(Async.getIfReady(valueFuture))) {
                    return true;
                }
            }
            return false;
        }

        public V get(Object key) {
            return Async.getIfReady(delegate.get(key));
        }

        public V putIfAbsent(K key, V value) {
            requireNonNull(value);
            CompletableFuture<V> valueFuture = delegate.putIfAbsent(key, CompletableFuture.completedFuture(value));
            return Async.getWhenSuccessful(valueFuture);
        }

        public V put(K key, V value) {
            requireNonNull(value);
            CompletableFuture<V> oldValueFuture = delegate.put(key, CompletableFuture.completedFuture(value));
            return Async.getWhenSuccessful(oldValueFuture);
        }

        public V remove(Object key) {
            CompletableFuture<V> oldValueFuture = delegate.remove(key);
            return Async.getWhenSuccessful(oldValueFuture);
        }

        public boolean remove(Object key, Object value) {
            requireNonNull(key);
            if (value == null) {
                return false;
            }
            K castedKey = (K) key;
            boolean[] removed = { false };
            boolean[] done = { false };
            for (; ; ) {
                CompletableFuture<V> future = delegate.get(key);
                V oldValue = Async.getWhenSuccessful(future);
                if ((future != null) && !value.equals(oldValue)) {
                    // Optimistically check if the current value is equal, but don't skip if it may be loading
                    return false;
                }
                delegate.compute(castedKey, (k, oldValueFuture) -> {
                    if (future != oldValueFuture) {
                        return oldValueFuture;
                    }
                    done[0] = true;
                    removed[0] = value.equals(oldValue);
                    return removed[0] ? null : oldValueFuture;
                }, /* recordStats */
                false, /* recordLoad */
                false);
                if (done[0]) {
                    return removed[0];
                }
            }
        }

        public V replace(K key, V value) {
            requireNonNull(value);
            CompletableFuture<V> oldValueFuture = delegate.replace(key, CompletableFuture.completedFuture(value));
            return Async.getWhenSuccessful(oldValueFuture);
        }

        public boolean replace(K key, V oldValue, V newValue) {
            requireNonNull(oldValue);
            requireNonNull(newValue);
            CompletableFuture<V> oldValueFuture = delegate.get(key);
            if ((oldValueFuture != null) && !oldValue.equals(Async.getWhenSuccessful(oldValueFuture))) {
                // Optimistically check if the current value is equal, but don't skip if it may be loading
                return false;
            }
            K castedKey = key;
            boolean[] replaced = { false };
            delegate.compute(castedKey, (k, value) -> {
                replaced[0] = oldValue.equals(Async.getWhenSuccessful(value));
                return replaced[0] ? CompletableFuture.completedFuture(newValue) : value;
            }, /* recordStats */
            false, /* recordLoad */
            false);
            return replaced[0];
        }

        public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
            requireNonNull(mappingFunction);
            CompletableFuture<V> valueFuture = delegate.computeIfAbsent(key, k -> {
                V newValue = mappingFunction.apply(key);
                return (newValue == null) ? null : CompletableFuture.completedFuture(newValue);
            });
            return Async.getWhenSuccessful(valueFuture);
        }

        public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            requireNonNull(remappingFunction);
            boolean[] computed = { false };
            for (; ; ) {
                CompletableFuture<V> future = delegate.get(key);
                V oldValue = Async.getWhenSuccessful(future);
                if (oldValue == null) {
                    return null;
                }
                CompletableFuture<V> valueFuture = delegate.computeIfPresent(key, (k, oldValueFuture) -> {
                    if (future != oldValueFuture) {
                        return oldValueFuture;
                    }
                    computed[0] = true;
                    V newValue = remappingFunction.apply(key, oldValue);
                    return (newValue == null) ? null : CompletableFuture.completedFuture(newValue);
                });
                if (computed[0] || (valueFuture == null)) {
                    return Async.getWhenSuccessful(valueFuture);
                }
            }
        }

        public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            requireNonNull(remappingFunction);
            boolean[] computed = { false };
            for (; ; ) {
                CompletableFuture<V> future = delegate.get(key);
                V oldValue = Async.getWhenSuccessful(future);
                CompletableFuture<V> valueFuture = delegate.compute(key, (k, oldValueFuture) -> {
                    if (future != oldValueFuture) {
                        return oldValueFuture;
                    }
                    computed[0] = true;
                    long startTime = delegate.statsTicker().read();
                    V newValue = remappingFunction.apply(key, oldValue);
                    long loadTime = delegate.statsTicker().read() - startTime;
                    if (newValue == null) {
                        delegate.statsCounter().recordLoadFailure(loadTime);
                        return null;
                    }
                    delegate.statsCounter().recordLoadSuccess(loadTime);
                    return CompletableFuture.completedFuture(newValue);
                }, /* recordMiss */
                false, /* recordLoad */
                false);
                if (computed[0]) {
                    return Async.getWhenSuccessful(valueFuture);
                }
            }
        }

        public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            requireNonNull(value);
            requireNonNull(remappingFunction);
            CompletableFuture<V> newValueFuture = CompletableFuture.completedFuture(value);
            boolean[] merged = { false };
            for (; ; ) {
                CompletableFuture<V> future = delegate.get(key);
                V oldValue = Async.getWhenSuccessful(future);
                CompletableFuture<V> mergedValueFuture = delegate.merge(key, newValueFuture, (oldValueFuture, valueFuture) -> {
                    if (future != oldValueFuture) {
                        return oldValueFuture;
                    }
                    merged[0] = true;
                    if (oldValue == null) {
                        return valueFuture;
                    }
                    V mergedValue = remappingFunction.apply(oldValue, value);
                    if (mergedValue == null) {
                        return null;
                    } else if (mergedValue == oldValue) {
                        return oldValueFuture;
                    } else if (mergedValue == value) {
                        return valueFuture;
                    }
                    return CompletableFuture.completedFuture(mergedValue);
                });
                if (merged[0] || (mergedValueFuture == newValueFuture)) {
                    return Async.getWhenSuccessful(mergedValueFuture);
                }
            }
        }

        public Set<K> keySet() {
            return delegate.keySet();
        }

        public Collection<V> values() {
            return (values == null) ? (values = new Values()) : values;
        }

        public Set<Entry<K, V>> entrySet() {
            return (entries == null) ? (entries = new EntrySet()) : entries;
        }

        private final class Values extends AbstractCollection<V> {

            public boolean isEmpty() {
                return AsMapView.this.isEmpty();
            }

            public int size() {
                return AsMapView.this.size();
            }

            public boolean contains(Object o) {
                return AsMapView.this.containsValue(o);
            }

            public void clear() {
                AsMapView.this.clear();
            }

            public Iterator<V> iterator() {
                return new Iterator<V>() {

                    Iterator<Entry<K, V>> iterator = entrySet().iterator();

                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    public V next() {
                        return iterator.next().getValue();
                    }

                    public void remove() {
                        iterator.remove();
                    }
                };
            }
        }

        private final class EntrySet extends AbstractSet<Entry<K, V>> {

            public boolean isEmpty() {
                return AsMapView.this.isEmpty();
            }

            public int size() {
                return AsMapView.this.size();
            }

            public boolean contains(Object o) {
                if (!(o instanceof Entry<?, ?>)) {
                    return false;
                }
                Entry<?, ?> entry = (Entry<?, ?>) o;
                V value = AsMapView.this.get(entry.getKey());
                return (value != null) && value.equals(entry.getValue());
            }

            public boolean remove(Object obj) {
                if (!(obj instanceof Entry<?, ?>)) {
                    return false;
                }
                Entry<?, ?> entry = (Entry<?, ?>) obj;
                return AsMapView.this.remove(entry.getKey(), entry.getValue());
            }

            public void clear() {
                AsMapView.this.clear();
            }

            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {

                    Iterator<Entry<K, CompletableFuture<V>>> iterator = delegate.entrySet().iterator();

                    Entry<K, V> cursor;

                    K removalKey;

                    public boolean hasNext() {
                        while ((cursor == null) && iterator.hasNext()) {
                            Entry<K, CompletableFuture<V>> entry = iterator.next();
                            V value = Async.getIfReady(entry.getValue());
                            if (value != null) {
                                cursor = new WriteThroughEntry<>(AsMapView.this, entry.getKey(), value);
                            }
                        }
                        return (cursor != null);
                    }

                    public Entry<K, V> next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        K key = cursor.getKey();
                        Entry<K, V> entry = cursor;
                        removalKey = key;
                        cursor = null;
                        return entry;
                    }

                    public void remove() {
                        Caffeine.requireState(removalKey != null);
                        delegate.remove(removalKey);
                        removalKey = null;
                    }
                };
            }
        }
    }
}