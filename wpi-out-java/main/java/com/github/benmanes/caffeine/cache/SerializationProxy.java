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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Serializes the configuration of the cache, reconsitituting it as a {@link Cache},
 * {@link LoadingCache}, or {@link AsyncLoadingCache} using {@link Caffeine} upon
 * deserialization. The data held by the cache is not retained.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
final class SerializationProxy<K, V> implements Serializable {

    private static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long serialVersionUID = 1;

    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull Ticker ticker;

     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean async;

     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean weakKeys;

     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean weakValues;

     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean softValues;

    @org.checkerframework.checker.initialization.qual.FBCBottom @org.checkerframework.checker.nullness.qual.Nullable Expiry<?, ?> expiry;

    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull Weigher<?, ?> weigher;

    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull CacheWriter<?, ?> writer;

     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isRecordingStats;

     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long expiresAfterWriteNanos;

     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long expiresAfterAccessNanos;

     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long refreshAfterWriteNanos;

    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull AsyncCacheLoader<?, ?> loader;

    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable RemovalListener<?, ?> removalListener;

     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long maximumSize = Caffeine.UNSET_INT;

     @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long maximumWeight = Caffeine.UNSET_INT;

    @org.checkerframework.dataflow.qual.Impure
    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Caffeine<Object, Object> recreateCaffeine() {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        if (ticker != null) {
            builder.ticker(ticker);
        }
        if (isRecordingStats) {
            builder.recordStats();
        }
        if (maximumSize != Caffeine.UNSET_INT) {
            builder.maximumSize(maximumSize);
        }
        if (maximumWeight != Caffeine.UNSET_INT) {
            builder.maximumWeight(maximumWeight);
            builder.weigher((Weigher<Object, Object>) weigher);
        }
        if (expiry != null) {
            builder.expireAfter(expiry);
        }
        if (expiresAfterWriteNanos > 0) {
            builder.expireAfterWrite(expiresAfterWriteNanos, TimeUnit.NANOSECONDS);
        }
        if (expiresAfterAccessNanos > 0) {
            builder.expireAfterAccess(expiresAfterAccessNanos, TimeUnit.NANOSECONDS);
        }
        if (refreshAfterWriteNanos > 0) {
            builder.refreshAfterWrite(refreshAfterWriteNanos, TimeUnit.NANOSECONDS);
        }
        if (weakKeys) {
            builder.weakKeys();
        }
        if (weakValues) {
            builder.weakValues();
        }
        if (softValues) {
            builder.softValues();
        }
        if (removalListener != null) {
            builder.removalListener((RemovalListener<Object, Object>) removalListener);
        }
        if (writer != CacheWriter.disabledWriter()) {
            builder.writer((CacheWriter<Object, Object>) writer);
        }
        return builder;
    }

    @org.checkerframework.dataflow.qual.Impure
    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object readResolve() {
        Caffeine<Object, Object> builder = recreateCaffeine();
        if (async) {
            AsyncCacheLoader<K, V> cacheLoader = (AsyncCacheLoader<K, V>) loader;
            return builder.buildAsync(cacheLoader);
        } else if (loader != null) {
            CacheLoader<K, V> cacheLoader = (CacheLoader<K, V>) loader;
            return builder.build(cacheLoader);
        }
        return builder.build();
    }
}
