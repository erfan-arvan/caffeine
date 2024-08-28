/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a skeletal implementation of the {@link LoadingCache} interface to minimize
 * the effort required to implement a {@link LocalCache}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
interface LocalLoadingCache<C extends LocalCache<K, V>, K, V> extends LocalManualCache<C, K, V>, LoadingCache<K, V> {

    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Logger logger = Logger.getLogger(LocalLoadingCache.class.getName());

    /**
     * Returns the {@link CacheLoader} used by this cache.
     */
    @org.checkerframework.dataflow.qual.Pure
    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CacheLoader<? super K, V> cacheLoader();

    /**
     * Returns the {@link CacheLoader} as a mapping function.
     */
    @org.checkerframework.dataflow.qual.Pure
    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Function<K, V> mappingFunction();

    /**
     * Returns whether the cache loader supports bulk loading.
     */
    @org.checkerframework.dataflow.qual.Pure
     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean hasBulkLoader();

    /**
     * Returns whether the supplied cache loader has bulk load functionality.
     */
    @org.checkerframework.dataflow.qual.SideEffectFree
    default  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean hasLoadAll(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CacheLoader<? super K, V> loader) {
        try {
            Method classLoadAll = loader.getClass().getMethod("loadAll", Iterable.class);
            Method defaultLoadAll = CacheLoader.class.getMethod("loadAll", Iterable.class);
            return !classLoadAll.equals(defaultLoadAll);
        } catch (NoSuchMethodException | SecurityException e) {
            logger.log(Level.WARNING, "Cannot determine if CacheLoader can bulk load", e);
            return false;
        }
    }

    @org.checkerframework.dataflow.qual.Impure
    default V get(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull LocalLoadingCache<C, K, V> this, K key) {
        return cache().computeIfAbsent(key, mappingFunction());
    }

    @org.checkerframework.dataflow.qual.Impure
    default @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Map<K, V> getAll(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull LocalLoadingCache<C, K, V> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterable<? extends K> keys) {
        return hasBulkLoader() ? loadInBulk(keys) : loadSequentially(keys);
    }

    /**
     * Sequentially loads each missing entry.
     */
    @org.checkerframework.dataflow.qual.Impure
    default @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Map<K, V> loadSequentially(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterable<? extends K> keys) {
        Set<K> uniqueKeys = new HashSet<>();
        for (K key : keys) {
            uniqueKeys.add(key);
        }
        int count = 0;
        Map<K, V> result = new HashMap<>(uniqueKeys.size());
        try {
            for (K key : uniqueKeys) {
                count++;
                V value = get(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
        } catch (Throwable t) {
            cache().statsCounter().recordMisses(uniqueKeys.size() - count);
            throw t;
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Batch loads the missing entries.
     */
    @org.checkerframework.dataflow.qual.Impure
    default @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Map<K, V> loadInBulk(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterable<? extends K> keys) {
        Map<K, V> found = cache().getAllPresent(keys);
        Set<K> keysToLoad = new HashSet<>();
        for (K key : keys) {
            if (!found.containsKey(key)) {
                keysToLoad.add(key);
            }
        }
        if (keysToLoad.isEmpty()) {
            return found;
        }
        Map<K, V> result = new HashMap<>(found);
        bulkLoad(keysToLoad, result);
        return Collections.unmodifiableMap(result);
    }

    /**
     * Performs a non-blocking bulk load of the missing keys. Any missing entry that materializes
     * during the load are replaced when the loaded entries are inserted into the cache.
     */
    @org.checkerframework.dataflow.qual.Impure
    default void bulkLoad(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Set<K> keysToLoad, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Map<K, V> result) {
        boolean success = false;
        long startTime = cache().statsTicker().read();
        try {
            Map<K, V> loaded = (Map<K, V>) cacheLoader().loadAll(keysToLoad);
            loaded.forEach((key, value) -> {
                cache().put(key, value, /* notifyWriter */
                false);
                if (keysToLoad.contains(key)) {
                    result.put(key, value);
                }
            });
            success = !loaded.isEmpty();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CompletionException(e);
        } finally {
            long loadTime = cache().statsTicker().read() - startTime;
            if (success) {
                cache().statsCounter().recordLoadSuccess(loadTime);
            } else {
                cache().statsCounter().recordLoadFailure(loadTime);
            }
        }
    }

    @org.checkerframework.dataflow.qual.Impure
    default void refresh(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull LocalLoadingCache<C, K, V> this, K key) {
        requireNonNull(key);
        long[] writeTime = new long[1];
        long startTime = cache().statsTicker().read();
        V oldValue = cache().getIfPresentQuietly(key, writeTime);
        CompletableFuture<V> refreshFuture = (oldValue == null) ? cacheLoader().asyncLoad(key, cache().executor()) : cacheLoader().asyncReload(key, oldValue, cache().executor());
        refreshFuture.whenComplete((newValue, error) -> {
            long loadTime = cache().statsTicker().read() - startTime;
            if (error != null) {
                logger.log(Level.WARNING, "Exception thrown during refresh", error);
                cache().statsCounter().recordLoadFailure(loadTime);
                return;
            }
            boolean[] discard = new boolean[1];
            cache().compute(key, (k, currentValue) -> {
                if (currentValue == null) {
                    return newValue;
                } else if (currentValue == oldValue) {
                    long expectedWriteTime = writeTime[0];
                    if (cache().hasWriteTime()) {
                        cache().getIfPresentQuietly(key, writeTime);
                    }
                    if (writeTime[0] == expectedWriteTime) {
                        return newValue;
                    }
                }
                discard[0] = true;
                return currentValue;
            }, /* recordMiss */
            false, /* recordLoad */
            false);
            if (discard[0] && cache().hasRemovalListener()) {
                cache().notifyRemoval(key, newValue, RemovalCause.REPLACED);
            }
            if (newValue == null) {
                cache().statsCounter().recordLoadFailure(loadTime);
            } else {
                cache().statsCounter().recordLoadSuccess(loadTime);
            }
        });
    }
}
