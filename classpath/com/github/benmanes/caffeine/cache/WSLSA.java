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
 *   <li>ExpireAccess
 *   <li>WeakKeys (inherited)
 *   <li>StrongValues (inherited)
 *   <li>Listening (inherited)
 *   <li>Stats (inherited)
 * </ul>
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings({"unchecked", "MissingOverride", "NullAway"})
class WSLSA<K, V> extends WSLS<K, V> {
  final Ticker ticker;

  final AccessOrderDeque<Node<K, V>> accessOrderEdenDeque;

  final Expiry<K, V> expiry;

  final TimerWheel<K, V> timerWheel;

  volatile long expiresAfterAccessNanos;

  final MpscGrowableArrayQueue<Runnable> writeBuffer;

  WSLSA(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
    super(builder, cacheLoader, async);
    this.ticker = builder.getTicker();
    this.accessOrderEdenDeque = builder.evicts() || builder.expiresAfterAccess()
        ? new AccessOrderDeque<Node<K, V>>()
        : null;
    this.expiry = builder.getExpiry(isAsync);
    this.timerWheel = builder.expiresVariable() ? new TimerWheel<K, V>(this) : null;
    this.expiresAfterAccessNanos = builder.getExpiresAfterAccessNanos();
    this.writeBuffer = new MpscGrowableArrayQueue<>(WRITE_BUFFER_MIN, WRITE_BUFFER_MAX);
  }

  public final Ticker expirationTicker() {
    return ticker;
  }

  protected final AccessOrderDeque<Node<K, V>> accessOrderEdenDeque() {
    return accessOrderEdenDeque;
  }

  protected final boolean expiresVariable() {
    return (timerWheel != null);
  }

  protected final Expiry<K, V> expiry() {
    return expiry;
  }

  protected final TimerWheel<K, V> timerWheel() {
    return timerWheel;
  }

  protected final boolean expiresAfterAccess() {
    return (timerWheel == null);
  }

  protected final long expiresAfterAccessNanos() {
    return expiresAfterAccessNanos;
  }

  protected final void setExpiresAfterAccessNanos(long expiresAfterAccessNanos) {
    this.expiresAfterAccessNanos = expiresAfterAccessNanos;
  }

  protected final MpscGrowableArrayQueue<Runnable> writeBuffer() {
    return writeBuffer;
  }

  protected final boolean buffersWrites() {
    return true;
  }
}
