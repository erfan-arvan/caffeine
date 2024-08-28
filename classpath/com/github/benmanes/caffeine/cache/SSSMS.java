// Copyright 2024 Ben Manes. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.base.UnsafeAccess;
import java.lang.Runnable;
import java.lang.SuppressWarnings;

/**
 * <em>WARNING: GENERATED CODE</em>
 *
 * A cache that provides the following features:
 * <ul>
 *   <li>MaximumSize
 *   <li>StrongKeys (inherited)
 *   <li>StrongValues (inherited)
 *   <li>Stats (inherited)
 * </ul>
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings({"unchecked", "MissingOverride"})
class SSSMS<K, V> extends SSS<K, V> {
  protected static final long MAXIMUM_OFFSET = UnsafeAccess.objectFieldOffset(SSSMS.class, com.github.benmanes.caffeine.cache.LocalCacheFactory.MAXIMUM);

  protected static final long EDEN_MAXIMUM_OFFSET = UnsafeAccess.objectFieldOffset(SSSMS.class, com.github.benmanes.caffeine.cache.LocalCacheFactory.EDEN_MAXIMUM);

  protected static final long MAIN_PROTECTED_MAXIMUM_OFFSET = UnsafeAccess.objectFieldOffset(SSSMS.class, com.github.benmanes.caffeine.cache.LocalCacheFactory.MAIN_PROTECTED_MAXIMUM);

  protected static final long WEIGHTED_SIZE_OFFSET = UnsafeAccess.objectFieldOffset(SSSMS.class, com.github.benmanes.caffeine.cache.LocalCacheFactory.WEIGHTED_SIZE);

  protected static final long EDEN_WEIGHTED_SIZE_OFFSET = UnsafeAccess.objectFieldOffset(SSSMS.class, com.github.benmanes.caffeine.cache.LocalCacheFactory.EDEN_WEIGHTED_SIZE);

  protected static final long MAIN_PROTECTED_WEIGHTED_SIZE_OFFSET = UnsafeAccess.objectFieldOffset(SSSMS.class, com.github.benmanes.caffeine.cache.LocalCacheFactory.MAIN_PROTECTED_WEIGHTED_SIZE);

  volatile long maximum;

  volatile long edenMaximum;

  volatile long mainProtectedMaximum;

  volatile long weightedSize;

  volatile long edenWeightedSize;

  volatile long mainProtectedWeightedSize;

  final FrequencySketch<K> sketch;

  final AccessOrderDeque<Node<K, V>> accessOrderEdenDeque;

  final AccessOrderDeque<Node<K, V>> accessOrderProbationDeque;

  final AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque;

  final MpscGrowableArrayQueue<Runnable> writeBuffer;

  SSSMS(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
    super(builder, cacheLoader, async);
    this.sketch = new FrequencySketch<K>();
    if (builder.hasInitialCapacity()) {
      long capacity = Math.min(builder.getMaximum(), builder.getInitialCapacity());
      this.sketch.ensureCapacity(capacity);
    }
    this.accessOrderEdenDeque = builder.evicts() || builder.expiresAfterAccess()
        ? new AccessOrderDeque<Node<K, V>>()
        : null;
    this.accessOrderProbationDeque = new AccessOrderDeque<Node<K, V>>();
    this.accessOrderProtectedDeque = new AccessOrderDeque<Node<K, V>>();
    this.writeBuffer = new MpscGrowableArrayQueue<>(WRITE_BUFFER_MIN, WRITE_BUFFER_MAX);
  }

  protected final boolean evicts() {
    return true;
  }

  protected final long maximum() {
    return UnsafeAccess.UNSAFE.getLong(this, MAXIMUM_OFFSET);
  }

  protected final void lazySetMaximum(long maximum) {
    UnsafeAccess.UNSAFE.putLong(this, MAXIMUM_OFFSET, maximum);
  }

  protected final long edenMaximum() {
    return UnsafeAccess.UNSAFE.getLong(this, EDEN_MAXIMUM_OFFSET);
  }

  protected final void lazySetEdenMaximum(long edenMaximum) {
    UnsafeAccess.UNSAFE.putLong(this, EDEN_MAXIMUM_OFFSET, edenMaximum);
  }

  protected final long mainProtectedMaximum() {
    return UnsafeAccess.UNSAFE.getLong(this, MAIN_PROTECTED_MAXIMUM_OFFSET);
  }

  protected final void lazySetMainProtectedMaximum(long mainProtectedMaximum) {
    UnsafeAccess.UNSAFE.putLong(this, MAIN_PROTECTED_MAXIMUM_OFFSET, mainProtectedMaximum);
  }

  protected final long weightedSize() {
    return UnsafeAccess.UNSAFE.getLong(this, WEIGHTED_SIZE_OFFSET);
  }

  protected final void lazySetWeightedSize(long weightedSize) {
    UnsafeAccess.UNSAFE.putLong(this, WEIGHTED_SIZE_OFFSET, weightedSize);
  }

  protected final long edenWeightedSize() {
    return UnsafeAccess.UNSAFE.getLong(this, EDEN_WEIGHTED_SIZE_OFFSET);
  }

  protected final void lazySetEdenWeightedSize(long edenWeightedSize) {
    UnsafeAccess.UNSAFE.putLong(this, EDEN_WEIGHTED_SIZE_OFFSET, edenWeightedSize);
  }

  protected final long mainProtectedWeightedSize() {
    return UnsafeAccess.UNSAFE.getLong(this, MAIN_PROTECTED_WEIGHTED_SIZE_OFFSET);
  }

  protected final void lazySetMainProtectedWeightedSize(long mainProtectedWeightedSize) {
    UnsafeAccess.UNSAFE.putLong(this, MAIN_PROTECTED_WEIGHTED_SIZE_OFFSET, mainProtectedWeightedSize);
  }

  protected final FrequencySketch<K> frequencySketch() {
    return sketch;
  }

  protected boolean fastpath() {
    return true;
  }

  protected final AccessOrderDeque<Node<K, V>> accessOrderEdenDeque() {
    return accessOrderEdenDeque;
  }

  protected final AccessOrderDeque<Node<K, V>> accessOrderProbationDeque() {
    return accessOrderProbationDeque;
  }

  protected final AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque() {
    return accessOrderProtectedDeque;
  }

  protected final MpscGrowableArrayQueue<Runnable> writeBuffer() {
    return writeBuffer;
  }

  protected final boolean buffersWrites() {
    return true;
  }
}
