/*
 * Copyright 2014 Ben Manes. All Rights Reserved.
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
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;

/**
 * An in-memory cache that has no capabilities for bounding the map. This implementation provides
 * a lightweight wrapper on top of {@link ConcurrentHashMap}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
final class UnboundedLocalCache<K, V> implements LocalCache<K, V> {

    final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull RemovalListener<K, V> removalListener;

    final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ConcurrentHashMap<K, V> data;

    final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull StatsCounter statsCounter;

    final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isRecordingStats;

    final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull CacheWriter<K, V> writer;

    final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Executor executor;

    final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Ticker ticker;

    transient @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull Set<K> keySet;

    transient @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull Collection<V> values;

    transient @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull Set<Entry<K, V>> entrySet;

    @org.checkerframework.dataflow.qual.Impure
    UnboundedLocalCache(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Caffeine<? super K, ? super V> builder,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean async) {
        this.data = new ConcurrentHashMap<>(builder.getInitialCapacity());
        this.statsCounter = builder.getStatsCounterSupplier().get();
        this.removalListener = builder.getRemovalListener(async);
        this.isRecordingStats = builder.isRecordingStats();
        this.writer = builder.getCacheWriter();
        this.executor = builder.getExecutor();
        this.ticker = builder.getTicker();
    }

    @org.checkerframework.dataflow.qual.Pure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean hasWriteTime(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return false;
    }

    /* ---------------- Cache -------------- */
    @org.checkerframework.dataflow.qual.Pure
    public V getIfPresent(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Object key,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean recordStats) {
        V value = data.get(key);
        if (recordStats) {
            if (value == null) {
                statsCounter.recordMisses(1);
            } else {
                statsCounter.recordHits(1);
            }
        }
        return value;
    }

    @org.checkerframework.dataflow.qual.Pure
    public V getIfPresentQuietly(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object key,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long /* 1 */
    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull [] writeTime) {
        return data.get(key);
    }

    @org.checkerframework.dataflow.qual.Impure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long estimatedSize(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return data.mappingCount();
    }

    @org.checkerframework.dataflow.qual.Impure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Map<K, V> getAllPresent(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterable<?> keys) {
        Set<Object> uniqueKeys = new HashSet<>();
        for (Object key : keys) {
            uniqueKeys.add(key);
        }
        int misses = 0;
        Map<Object, Object> result = new HashMap<>(uniqueKeys.size());
        for (Object key : uniqueKeys) {
            Object value = data.get(key);
            if (value == null) {
                misses++;
            } else {
                result.put(key, value);
            }
        }
        statsCounter.recordMisses(misses);
        statsCounter.recordHits(result.size());
        Map<K, V> castedResult = (Map<K, V>) result;
        return Collections.unmodifiableMap(castedResult);
    }

    @org.checkerframework.dataflow.qual.SideEffectFree
    public void cleanUp(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
    }

    @org.checkerframework.dataflow.qual.Pure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull StatsCounter statsCounter(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return statsCounter;
    }

    @org.checkerframework.dataflow.qual.Pure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean hasRemovalListener(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return (removalListener != null);
    }

    @org.checkerframework.dataflow.qual.Pure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable RemovalListener<K, V> removalListener(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return removalListener;
    }

    @org.checkerframework.dataflow.qual.Impure
    public void notifyRemoval(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, K key, V value, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull RemovalCause cause) {
        requireNonNull(removalListener, "Notification should be guarded with a check");
        executor.execute(() -> removalListener.onRemoval(key, value, cause));
    }

    @org.checkerframework.dataflow.qual.Pure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isRecordingStats(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return isRecordingStats;
    }

    @org.checkerframework.dataflow.qual.Pure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Executor executor(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return executor;
    }

    @org.checkerframework.dataflow.qual.Pure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Ticker expirationTicker(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return Ticker.disabledTicker();
    }

    @org.checkerframework.dataflow.qual.Pure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Ticker statsTicker(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return ticker;
    }

    /* ---------------- JDK8+ Map extensions -------------- */
    @org.checkerframework.dataflow.qual.Impure
    public void forEach(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull BiConsumer<? super K, ? super V> action) {
        data.forEach(action);
    }

    @org.checkerframework.dataflow.qual.Impure
    public void replaceAll(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull BiFunction<? super K, ? super V, ? extends V> function) {
        requireNonNull(function);
        // ensures that the removal notification is processed after the removal has completed
        K[] notificationKey = (K[]) new Object[1];
        V[] notificationValue = (V[]) new Object[1];
        data.replaceAll((key, value) -> {
            if (notificationKey[0] != null) {
                notifyRemoval(notificationKey[0], notificationValue[0], RemovalCause.REPLACED);
                notificationValue[0] = null;
                notificationKey[0] = null;
            }
            V newValue = requireNonNull(function.apply(key, value));
            if (newValue != value) {
                writer.write(key, newValue);
            }
            if (hasRemovalListener() && (newValue != value)) {
                notificationKey[0] = key;
                notificationValue[0] = value;
            }
            return newValue;
        });
        if (notificationKey[0] != null) {
            notifyRemoval(notificationKey[0], notificationValue[0], RemovalCause.REPLACED);
        }
    }

    @org.checkerframework.dataflow.qual.Impure
    public V computeIfAbsent(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Function<? super K, ? extends V> mappingFunction,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean recordStats,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean recordLoad) {
        requireNonNull(mappingFunction);
        // optimistic fast path due to computeIfAbsent always locking
        V value = data.get(key);
        if (value != null) {
            if (recordStats) {
                statsCounter.recordHits(1);
            }
            return value;
        }
        boolean[] missed = new boolean[1];
        value = data.computeIfAbsent(key, k -> {
            // Do not communicate to CacheWriter on a load
            missed[0] = true;
            return recordStats ? statsAware(mappingFunction, recordLoad).apply(key) : mappingFunction.apply(key);
        });
        if (!missed[0] && recordStats) {
            statsCounter.recordHits(1);
        }
        return value;
    }

    @org.checkerframework.dataflow.qual.Impure
    public V computeIfPresent(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        requireNonNull(remappingFunction);
        // optimistic fast path due to computeIfAbsent always locking
        if (!data.containsKey(key)) {
            return null;
        }
        // ensures that the removal notification is processed after the removal has completed
        V[] oldValue = (V[]) new Object[1];
        RemovalCause[] cause = new RemovalCause[1];
        V nv = data.computeIfPresent(key, (K k, V value) -> {
            BiFunction<? super K, ? super V, ? extends V> function = statsAware(remappingFunction, /* recordMiss */
            false, /* recordLoad */
            true);
            V newValue = function.apply(k, value);
            cause[0] = (newValue == null) ? RemovalCause.EXPLICIT : RemovalCause.REPLACED;
            if (hasRemovalListener() && (newValue != value)) {
                oldValue[0] = value;
            }
            return newValue;
        });
        if (oldValue[0] != null) {
            notifyRemoval(key, oldValue[0], cause[0]);
        }
        return nv;
    }

    @org.checkerframework.dataflow.qual.Impure
    public V compute(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean recordMiss,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean recordLoad) {
        requireNonNull(remappingFunction);
        return remap(key, statsAware(remappingFunction, recordMiss, recordLoad));
    }

    @org.checkerframework.dataflow.qual.Impure
    public V merge(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, K key, V value, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        requireNonNull(remappingFunction);
        requireNonNull(value);
        return remap(key, (k, oldValue) -> (oldValue == null) ? value : statsAware(remappingFunction).apply(oldValue, value));
    }

    /**
     * A {@link Map#compute(Object, BiFunction)} that does not directly record any cache statistics.
     *
     * @param key key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     */
    @org.checkerframework.dataflow.qual.Impure
    V remap(K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        // ensures that the removal notification is processed after the removal has completed
        V[] oldValue = (V[]) new Object[1];
        RemovalCause[] cause = new RemovalCause[1];
        V nv = data.compute(key, (K k, V value) -> {
            V newValue = remappingFunction.apply(k, value);
            if ((value == null) && (newValue == null)) {
                return null;
            }
            cause[0] = (newValue == null) ? RemovalCause.EXPLICIT : RemovalCause.REPLACED;
            if (hasRemovalListener() && (value != null) && (newValue != value)) {
                oldValue[0] = value;
            }
            return newValue;
        });
        if (oldValue[0] != null) {
            notifyRemoval(key, oldValue[0], cause[0]);
        }
        return nv;
    }

    /* ---------------- Concurrent Map -------------- */
    @org.checkerframework.dataflow.qual.Pure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isEmpty(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return data.isEmpty();
    }

    @org.checkerframework.dataflow.qual.Pure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int size(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return data.size();
    }

    @org.checkerframework.dataflow.qual.Impure
    public void clear(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        if (!hasRemovalListener() && (writer == CacheWriter.disabledWriter())) {
            data.clear();
            return;
        }
        for (K key : data.keySet()) {
            remove(key);
        }
    }

    @org.checkerframework.dataflow.qual.Pure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean containsKey(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object key) {
        return data.containsKey(key);
    }

    @org.checkerframework.dataflow.qual.Pure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean containsValue(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object value) {
        return data.containsValue(value);
    }

    @org.checkerframework.dataflow.qual.Pure
    public V get(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Object key) {
        return getIfPresent(key, /* recordStats */
        false);
    }

    @org.checkerframework.dataflow.qual.Impure
    public V put(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, K key, V value) {
        return put(key, value, /* notifyWriter */
        true);
    }

    @org.checkerframework.dataflow.qual.Impure
    public V put(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, K key, V value,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean notifyWriter) {
        requireNonNull(value);
        // ensures that the removal notification is processed after the removal has completed
        V[] oldValue = (V[]) new Object[1];
        if ((writer == CacheWriter.disabledWriter()) || !notifyWriter) {
            oldValue[0] = data.put(key, value);
        } else {
            data.compute(key, (k, v) -> {
                if (value != v) {
                    writer.write(key, value);
                }
                oldValue[0] = v;
                return value;
            });
        }
        if (hasRemovalListener() && (oldValue[0] != null) && (oldValue[0] != value)) {
            notifyRemoval(key, oldValue[0], RemovalCause.REPLACED);
        }
        return oldValue[0];
    }

    @org.checkerframework.dataflow.qual.Impure
    public V putIfAbsent(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, K key, V value) {
        requireNonNull(value);
        boolean[] wasAbsent = new boolean[1];
        V val = data.computeIfAbsent(key, k -> {
            writer.write(key, value);
            wasAbsent[0] = true;
            return value;
        });
        return wasAbsent[0] ? null : val;
    }

    @org.checkerframework.dataflow.qual.Impure
    public void putAll(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Map<? extends K, ? extends V> map) {
        if (!hasRemovalListener() && (writer == CacheWriter.disabledWriter())) {
            data.putAll(map);
            return;
        }
        map.forEach(this::put);
    }

    @org.checkerframework.dataflow.qual.Impure
    public V remove(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Object key) {
        K castKey = (K) key;
        V[] oldValue = (V[]) new Object[1];
        if (writer == CacheWriter.disabledWriter()) {
            oldValue[0] = data.remove(key);
        } else {
            data.computeIfPresent(castKey, (k, v) -> {
                writer.delete(castKey, v, RemovalCause.EXPLICIT);
                oldValue[0] = v;
                return null;
            });
        }
        if (hasRemovalListener() && (oldValue[0] != null)) {
            notifyRemoval(castKey, oldValue[0], RemovalCause.EXPLICIT);
        }
        return oldValue[0];
    }

    @org.checkerframework.dataflow.qual.Impure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean remove(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Object key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Object value) {
        if (value == null) {
            requireNonNull(key);
            return false;
        }
        K castKey = (K) key;
        V[] oldValue = (V[]) new Object[1];
        data.computeIfPresent(castKey, (k, v) -> {
            if (v.equals(value)) {
                writer.delete(castKey, v, RemovalCause.EXPLICIT);
                oldValue[0] = v;
                return null;
            }
            return v;
        });
        boolean removed = (oldValue[0] != null);
        if (hasRemovalListener() && removed) {
            notifyRemoval(castKey, oldValue[0], RemovalCause.EXPLICIT);
        }
        return removed;
    }

    @org.checkerframework.dataflow.qual.Impure
    public V replace(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, K key, V value) {
        requireNonNull(value);
        V[] oldValue = (V[]) new Object[1];
        data.computeIfPresent(key, (k, v) -> {
            if (value != v) {
                writer.write(key, value);
            }
            oldValue[0] = v;
            return value;
        });
        if (hasRemovalListener() && (oldValue[0] != null) && (oldValue[0] != value)) {
            notifyRemoval(key, value, RemovalCause.REPLACED);
        }
        return oldValue[0];
    }

    @org.checkerframework.dataflow.qual.Impure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean replace(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, K key, V oldValue, V newValue) {
        requireNonNull(oldValue);
        requireNonNull(newValue);
        V[] prev = (V[]) new Object[1];
        data.computeIfPresent(key, (k, v) -> {
            if (v.equals(oldValue)) {
                if (newValue != v) {
                    writer.write(key, newValue);
                }
                prev[0] = v;
                return newValue;
            }
            return v;
        });
        boolean replaced = (prev[0] != null);
        if (hasRemovalListener() && replaced && (prev[0] != newValue)) {
            notifyRemoval(key, prev[0], RemovalCause.REPLACED);
        }
        return replaced;
    }

    @org.checkerframework.dataflow.qual.Pure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean equals(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Object o) {
        return data.equals(o);
    }

    @org.checkerframework.dataflow.qual.Pure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int hashCode(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return data.hashCode();
    }

    @org.checkerframework.dataflow.qual.Pure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull String toString(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        return data.toString();
    }

    @org.checkerframework.dataflow.qual.Impure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Set<K> keySet(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        final Set<K> ks = keySet;
        return (ks == null) ? (keySet = new KeySetView<>(this)) : ks;
    }

    @org.checkerframework.dataflow.qual.Impure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Collection<V> values(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        final Collection<V> vs = values;
        return (vs == null) ? (values = new ValuesView<>(this)) : vs;
    }

    @org.checkerframework.dataflow.qual.Impure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Set<Entry<K, V>> entrySet(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> this) {
        final Set<Entry<K, V>> es = entrySet;
        return (es == null) ? (entrySet = new EntrySetView<>(this)) : es;
    }

    /**
     * An adapter to safely externalize the keys.
     */
    static final class KeySetView<K> extends AbstractSet<K> {

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, ?> cache;

        @org.checkerframework.dataflow.qual.Impure
        KeySetView(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, ?> cache) {
            this.cache = requireNonNull(cache);
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isEmpty(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull KeySetView<K> this) {
            return cache.isEmpty();
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int size(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull KeySetView<K> this) {
            return cache.size();
        }

        @org.checkerframework.dataflow.qual.Impure
        public void clear(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull KeySetView<K> this) {
            cache.clear();
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean contains(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull KeySetView<K> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object o) {
            return cache.containsKey(o);
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean remove(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull KeySetView<K> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object obj) {
            return (cache.remove(obj) != null);
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterator<K> iterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull KeySetView<K> this) {
            return new KeyIterator<>(cache);
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Spliterator<K> spliterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull KeySetView<K> this) {
            return cache.data.keySet().spliterator();
        }
    }

    /**
     * An adapter to safely externalize the key iterator.
     */
    static final class KeyIterator<K> implements Iterator<K> {

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, ?> cache;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterator<K> iterator;

        @org.checkerframework.checker.initialization.qual.FBCBottom @org.checkerframework.checker.nullness.qual.Nullable K current;

        @org.checkerframework.dataflow.qual.Impure
        KeyIterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, ?> cache) {
            this.cache = requireNonNull(cache);
            this.iterator = cache.data.keySet().iterator();
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean hasNext(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull KeyIterator<K> this) {
            return iterator.hasNext();
        }

        @org.checkerframework.dataflow.qual.Impure
        public K next(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull KeyIterator<K> this) {
            current = iterator.next();
            return current;
        }

        @org.checkerframework.dataflow.qual.Impure
        public void remove(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull KeyIterator<K> this) {
            Caffeine.requireState(current != null);
            cache.remove(current);
            current = null;
        }
    }

    /**
     * An adapter to safely externalize the values.
     */
    static final class ValuesView<K, V> extends AbstractCollection<V> {

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache;

        @org.checkerframework.dataflow.qual.Impure
        ValuesView(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache) {
            this.cache = requireNonNull(cache);
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isEmpty(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ValuesView<K, V> this) {
            return cache.isEmpty();
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int size(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ValuesView<K, V> this) {
            return cache.size();
        }

        @org.checkerframework.dataflow.qual.Impure
        public void clear(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ValuesView<K, V> this) {
            cache.clear();
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean contains(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ValuesView<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object o) {
            return cache.containsValue(o);
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean removeIf(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ValuesView<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Predicate<? super V> filter) {
            requireNonNull(filter);
            boolean removed = false;
            for (Entry<K, V> entry : cache.data.entrySet()) {
                if (filter.test(entry.getValue())) {
                    removed |= cache.remove(entry.getKey(), entry.getValue());
                }
            }
            return removed;
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterator<V> iterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ValuesView<K, V> this) {
            return new ValuesIterator<>(cache);
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Spliterator<V> spliterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ValuesView<K, V> this) {
            return cache.data.values().spliterator();
        }
    }

    /**
     * An adapter to safely externalize the value iterator.
     */
    static final class ValuesIterator<K, V> implements Iterator<V> {

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterator<Entry<K, V>> iterator;

        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Entry<K, V> entry;

        @org.checkerframework.dataflow.qual.Impure
        ValuesIterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache) {
            this.cache = requireNonNull(cache);
            this.iterator = cache.data.entrySet().iterator();
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean hasNext(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ValuesIterator<K, V> this) {
            return iterator.hasNext();
        }

        @org.checkerframework.dataflow.qual.Impure
        public V next(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ValuesIterator<K, V> this) {
            entry = iterator.next();
            return entry.getValue();
        }

        @org.checkerframework.dataflow.qual.Impure
        public void remove(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull ValuesIterator<K, V> this) {
            Caffeine.requireState(entry != null);
            cache.remove(entry.getKey());
            entry = null;
        }
    }

    /**
     * An adapter to safely externalize the entries.
     */
    static final class EntrySetView<K, V> extends AbstractSet<Entry<K, V>> {

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache;

        @org.checkerframework.dataflow.qual.Impure
        EntrySetView(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache) {
            this.cache = requireNonNull(cache);
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isEmpty(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySetView<K, V> this) {
            return cache.isEmpty();
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int size(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySetView<K, V> this) {
            return cache.size();
        }

        @org.checkerframework.dataflow.qual.Impure
        public void clear(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySetView<K, V> this) {
            cache.clear();
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean contains(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySetView<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object o) {
            if (!(o instanceof Entry<?, ?>)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) o;
            V value = cache.get(entry.getKey());
            return (value != null) && value.equals(entry.getValue());
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean remove(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySetView<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object obj) {
            if (!(obj instanceof Entry<?, ?>)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) obj;
            return cache.remove(entry.getKey(), entry.getValue());
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean removeIf(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySetView<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Predicate<? super Entry<K, V>> filter) {
            requireNonNull(filter);
            boolean removed = false;
            for (Entry<K, V> entry : cache.data.entrySet()) {
                if (filter.test(entry)) {
                    removed |= cache.remove(entry.getKey(), entry.getValue());
                }
            }
            return removed;
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterator<Entry<K, V>> iterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySetView<K, V> this) {
            return new EntryIterator<>(cache);
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Spliterator<Entry<K, V>> spliterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySetView<K, V> this) {
            return (cache.writer == CacheWriter.disabledWriter()) ? cache.data.entrySet().spliterator() : new EntrySpliterator<>(cache);
        }
    }

    /**
     * An adapter to safely externalize the entry iterator.
     */
    static final class EntryIterator<K, V> implements Iterator<Entry<K, V>> {

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterator<Entry<K, V>> iterator;

        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Entry<K, V> entry;

        @org.checkerframework.dataflow.qual.Impure
        EntryIterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache) {
            this.cache = requireNonNull(cache);
            this.iterator = cache.data.entrySet().iterator();
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean hasNext(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntryIterator<K, V> this) {
            return iterator.hasNext();
        }

        @org.checkerframework.dataflow.qual.Impure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Entry<K, V> next(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntryIterator<K, V> this) {
            entry = iterator.next();
            return new WriteThroughEntry<>(cache, entry.getKey(), entry.getValue());
        }

        @org.checkerframework.dataflow.qual.Impure
        public void remove(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntryIterator<K, V> this) {
            Caffeine.requireState(entry != null);
            cache.remove(entry.getKey());
            entry = null;
        }
    }

    /**
     * An adapter to safely externalize the entry spliterator.
     */
    static final class EntrySpliterator<K, V> implements Spliterator<Entry<K, V>> {

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Spliterator<Entry<K, V>> spliterator;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache;

        @org.checkerframework.dataflow.qual.Impure
        EntrySpliterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache) {
            this(cache, cache.data.entrySet().spliterator());
        }

        @org.checkerframework.dataflow.qual.Impure
        EntrySpliterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Spliterator<Entry<K, V>> spliterator) {
            this.spliterator = requireNonNull(spliterator);
            this.cache = requireNonNull(cache);
        }

        @org.checkerframework.dataflow.qual.Impure
        public void forEachRemaining(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySpliterator<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Consumer<? super Entry<K, V>> action) {
            requireNonNull(action);
            spliterator.forEachRemaining(entry -> {
                Entry<K, V> e = new WriteThroughEntry<>(cache, entry.getKey(), entry.getValue());
                action.accept(e);
            });
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean tryAdvance(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySpliterator<K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Consumer<? super Entry<K, V>> action) {
            requireNonNull(action);
            return spliterator.tryAdvance(entry -> {
                Entry<K, V> e = new WriteThroughEntry<>(cache, entry.getKey(), entry.getValue());
                action.accept(e);
            });
        }

        @org.checkerframework.dataflow.qual.Impure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable EntrySpliterator<K, V> trySplit(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySpliterator<K, V> this) {
            Spliterator<Entry<K, V>> split = spliterator.trySplit();
            return (split == null) ? null : new EntrySpliterator<>(cache, split);
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long estimateSize(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySpliterator<K, V> this) {
            return spliterator.estimateSize();
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int characteristics(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull EntrySpliterator<K, V> this) {
            return spliterator.characteristics();
        }
    }

    /* ---------------- Manual Cache -------------- */
    static class UnboundedLocalManualCache<K, V> implements LocalManualCache<UnboundedLocalCache<K, V>, K, V>, Serializable {

        private static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long serialVersionUID = 1;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache;

        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull Policy<K, V> policy;

        @org.checkerframework.dataflow.qual.SideEffectFree
        UnboundedLocalManualCache(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Caffeine<K, V> builder) {
            cache = new UnboundedLocalCache<>(builder, /* async */
            false);
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalCache<K, V> cache(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalManualCache<K, V> this) {
            return cache;
        }

        @org.checkerframework.dataflow.qual.Impure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Policy<K, V> policy(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalManualCache<K, V> this) {
            return (policy == null) ? (policy = new UnboundedPolicy<>(cache.isRecordingStats)) : policy;
        }

        @org.checkerframework.dataflow.qual.SideEffectFree
        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        @org.checkerframework.dataflow.qual.Impure
        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object writeReplace() {
            SerializationProxy<K, V> proxy = new SerializationProxy<>();
            proxy.isRecordingStats = cache.isRecordingStats;
            proxy.removalListener = cache.removalListener;
            proxy.ticker = cache.ticker;
            proxy.writer = cache.writer;
            return proxy;
        }
    }

    /**
     * An eviction policy that supports no boundings.
     */
    static final class UnboundedPolicy<K, V> implements Policy<K, V> {

        private final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isRecordingStats;

        @org.checkerframework.dataflow.qual.SideEffectFree
        UnboundedPolicy( @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isRecordingStats) {
            this.isRecordingStats = isRecordingStats;
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isRecordingStats(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedPolicy<K, V> this) {
            return isRecordingStats;
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Optional<Eviction<K, V>> eviction(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedPolicy<K, V> this) {
            return Optional.empty();
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Optional<Expiration<K, V>> expireAfterAccess(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedPolicy<K, V> this) {
            return Optional.empty();
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Optional<Expiration<K, V>> expireAfterWrite(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedPolicy<K, V> this) {
            return Optional.empty();
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Optional<Expiration<K, V>> refreshAfterWrite(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedPolicy<K, V> this) {
            return Optional.empty();
        }
    }

    /* ---------------- Loading Cache -------------- */
    static final class UnboundedLocalLoadingCache<K, V> extends UnboundedLocalManualCache<K, V> implements LocalLoadingCache<UnboundedLocalCache<K, V>, K, V> {

        private static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long serialVersionUID = 1;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CacheLoader<? super K, V> loader;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Function<K, V> mappingFunction;

        final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean hasBulkLoader;

        @org.checkerframework.dataflow.qual.Impure
        UnboundedLocalLoadingCache(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Caffeine<K, V> builder, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CacheLoader<? super K, V> loader) {
            super(builder);
            this.loader = loader;
            this.hasBulkLoader = hasLoadAll(loader);
            this.mappingFunction = key -> {
                try {
                    return loader.load(key);
                } catch (RuntimeException e) {
                    throw e;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CompletionException(e);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            };
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CacheLoader<? super K, V> cacheLoader(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalLoadingCache<K, V> this) {
            return loader;
        }

        @org.checkerframework.dataflow.qual.Pure
        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Function<K, V> mappingFunction(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalLoadingCache<K, V> this) {
            return mappingFunction;
        }

        @org.checkerframework.dataflow.qual.Pure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean hasBulkLoader(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalLoadingCache<K, V> this) {
            return hasBulkLoader;
        }

        @org.checkerframework.dataflow.qual.Impure
        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object writeReplace(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalLoadingCache<K, V> this) {
            SerializationProxy<K, V> proxy = (SerializationProxy<K, V>) super.writeReplace();
            proxy.loader = loader;
            return proxy;
        }

        @org.checkerframework.dataflow.qual.SideEffectFree
        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }
    }

    /* ---------------- Async Loading Cache -------------- */
    static final class UnboundedLocalAsyncLoadingCache<K, V> extends LocalAsyncLoadingCache<UnboundedLocalCache<K, CompletableFuture<V>>, K, V> implements Serializable {

        private static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long serialVersionUID = 1;

        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull Policy<K, V> policy;

        @org.checkerframework.dataflow.qual.SideEffectFree
        UnboundedLocalAsyncLoadingCache(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Caffeine<K, V> builder, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull AsyncCacheLoader<? super K, V> loader) {
            super(new UnboundedLocalCache<>((Caffeine<K, CompletableFuture<V>>) builder, /* async */
            true), loader);
        }

        @org.checkerframework.dataflow.qual.Impure
        protected @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Policy<K, V> policy(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull UnboundedLocalAsyncLoadingCache<K, V> this) {
            return (policy == null) ? (policy = new UnboundedPolicy<>(cache.isRecordingStats)) : policy;
        }

        @org.checkerframework.dataflow.qual.SideEffectFree
        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }

        @org.checkerframework.dataflow.qual.Impure
        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object writeReplace() {
            SerializationProxy<K, V> proxy = new SerializationProxy<>();
            proxy.isRecordingStats = cache.isRecordingStats;
            proxy.removalListener = cache.removalListener;
            proxy.ticker = cache.ticker;
            proxy.writer = cache.writer;
            proxy.loader = loader;
            proxy.async = true;
            return proxy;
        }
    }
}
