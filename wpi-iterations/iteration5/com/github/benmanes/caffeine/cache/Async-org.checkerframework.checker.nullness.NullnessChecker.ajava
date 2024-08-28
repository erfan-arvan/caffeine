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
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Static utility methods and classes pertaining to asynchronous operations.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
final class Async {

    // 150 years
    static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long MAXIMUM_EXPIRY = (Long.MAX_VALUE >> 1);

    @org.checkerframework.dataflow.qual.SideEffectFree
    private Async() {
    }

    /**
     * Returns if the future has successfully completed.
     */
    @org.checkerframework.dataflow.qual.Impure
    static  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isReady(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable CompletableFuture<?> future) {
        return (future != null) && future.isDone() && !future.isCompletedExceptionally() && (future.join() != null);
    }

    /**
     * Returns the current value or null if either not done or failed.
     */
    @org.checkerframework.dataflow.qual.Impure
    static <V> V getIfReady(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable CompletableFuture<V> future) {
        return isReady(future) ? future.join() : null;
    }

    /**
     * Returns the value when completed successfully or null if failed.
     */
    @org.checkerframework.dataflow.qual.Impure
    static <V> V getWhenSuccessful(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable CompletableFuture<V> future) {
        try {
            return (future == null) ? null : future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }

    /**
     * A removal listener that asynchronously forwards the value stored in a {@link CompletableFuture}
     * if successful to the user-supplied removal listener.
     */
    static final class AsyncRemovalListener<K, V> implements RemovalListener<K, CompletableFuture<V>>, Serializable {

        private static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long serialVersionUID = 1L;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull RemovalListener<K, V> delegate;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Executor executor;

        @org.checkerframework.dataflow.qual.Impure
        AsyncRemovalListener(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull RemovalListener<K, V> delegate, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Executor executor) {
            this.delegate = requireNonNull(delegate);
            this.executor = requireNonNull(executor);
        }

        @org.checkerframework.dataflow.qual.Impure
        public void onRemoval(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull AsyncRemovalListener<K, V> this, K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CompletableFuture<V> future, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull RemovalCause cause) {
            future.thenAcceptAsync(value -> delegate.onRemoval(key, value, cause), executor);
        }

        @org.checkerframework.dataflow.qual.Pure
        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object writeReplace() {
            return delegate;
        }
    }

    /**
     * A weigher for asynchronous computations. When the value is being loaded this weigher returns
     * {@code 0} to indicate that the entry should not be evicted due to a size constraint. If the
     * value is computed successfully the entry must be reinserted so that the weight is updated and
     * the expiration timeouts reflect the value once present. This can be done safely using
     * {@link Map#replace(Object, Object, Object)}.
     */
    static final class AsyncWeigher<K, V> implements Weigher<K, CompletableFuture<V>>, Serializable {

        private static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long serialVersionUID = 1L;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Weigher<K, V> delegate;

        @org.checkerframework.dataflow.qual.Impure
        AsyncWeigher(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Weigher<K, V> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int weigh(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull AsyncWeigher<K, V> this, K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CompletableFuture<V> future) {
            return isReady(future) ? delegate.weigh(key, future.join()) : 0;
        }

        @org.checkerframework.dataflow.qual.Pure
        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object writeReplace() {
            return delegate;
        }
    }

    /**
     * An expiry for asynchronous computations. When the value is being loaded this expiry returns
     * {@code Long.MAX_VALUE} to indicate that the entry should not be evicted due to an expiry
     * constraint. If the value is computed successfully the entry must be reinserted so that the
     * expiration is updated and the expiration timeouts reflect the value once present. The value
     * maximum range is reserved to coordinate the asynchronous life cycle.
     */
    static final class AsyncExpiry<K, V> implements Expiry<K, CompletableFuture<V>>, Serializable {

        private static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long serialVersionUID = 1L;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Expiry<K, V> delegate;

        @org.checkerframework.dataflow.qual.Impure
        AsyncExpiry(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Expiry<K, V> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long expireAfterCreate(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull AsyncExpiry<K, V> this, K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CompletableFuture<V> future,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long currentTime) {
            if (isReady(future)) {
                long duration = delegate.expireAfterCreate(key, future.join(), currentTime);
                return Math.min(duration, MAXIMUM_EXPIRY);
            }
            return Long.MAX_VALUE;
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long expireAfterUpdate(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull AsyncExpiry<K, V> this, K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CompletableFuture<V> future,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long currentTime,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long currentDuration) {
            if (isReady(future)) {
                long duration = (currentDuration > MAXIMUM_EXPIRY) ? delegate.expireAfterCreate(key, future.join(), currentTime) : delegate.expireAfterUpdate(key, future.join(), currentTime, currentDuration);
                return Math.min(duration, MAXIMUM_EXPIRY);
            }
            return Long.MAX_VALUE;
        }

        @org.checkerframework.dataflow.qual.Impure
        public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long expireAfterRead(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull AsyncExpiry<K, V> this, K key, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull CompletableFuture<V> future,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long currentTime,  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long currentDuration) {
            if (isReady(future)) {
                long duration = delegate.expireAfterRead(key, future.join(), currentTime, currentDuration);
                return Math.min(duration, MAXIMUM_EXPIRY);
            }
            return Long.MAX_VALUE;
        }

        @org.checkerframework.dataflow.qual.Pure
        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object writeReplace() {
            return delegate;
        }
    }
}
