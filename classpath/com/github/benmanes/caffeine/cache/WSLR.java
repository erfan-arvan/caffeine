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

import java.lang.Runnable;
import java.lang.SuppressWarnings;

/**
 * <em>WARNING: GENERATED CODE</em>
 *
 * A cache that provides the following features:
 * <ul>
 *   <li>RefreshWrite
 *   <li>WeakKeys (inherited)
 *   <li>StrongValues (inherited)
 *   <li>Listening (inherited)
 * </ul>
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings({"unchecked", "MissingOverride", "NullAway"})
final class WSLR<K, V> extends WSL<K, V> {
  final Ticker ticker;

  volatile long refreshAfterWriteNanos;

  final MpscGrowableArrayQueue<Runnable> writeBuffer;

  WSLR(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
    super(builder, cacheLoader, async);
    this.ticker = builder.getTicker();
    this.refreshAfterWriteNanos = builder.getRefreshAfterWriteNanos();
    this.writeBuffer = new MpscGrowableArrayQueue<>(WRITE_BUFFER_MIN, WRITE_BUFFER_MAX);
  }

  public Ticker expirationTicker() {
    return ticker;
  }

  protected boolean refreshAfterWrite() {
    return true;
  }

  protected long refreshAfterWriteNanos() {
    return refreshAfterWriteNanos;
  }

  protected void setRefreshAfterWriteNanos(long refreshAfterWriteNanos) {
    this.refreshAfterWriteNanos = refreshAfterWriteNanos;
  }

  protected MpscGrowableArrayQueue<Runnable> writeBuffer() {
    return writeBuffer;
  }

  protected boolean buffersWrites() {
    return true;
  }
}
