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

import java.lang.Class;
import java.lang.IllegalStateException;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.Throwable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * <em>WARNING: GENERATED CODE</em>
 *
 * A factory for caches optimized for a particular configuration.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
final class LocalCacheFactory {
  public static final String MAXIMUM = "maximum";

  public static final String EDEN_MAXIMUM = "edenMaximum";

  public static final String MAIN_PROTECTED_MAXIMUM = "mainProtectedMaximum";

  public static final String WEIGHTED_SIZE = "weightedSize";

  public static final String EDEN_WEIGHTED_SIZE = "edenWeightedSize";

  public static final String MAIN_PROTECTED_WEIGHTED_SIZE = "mainProtectedWeightedSize";

  public static final String KEY = "key";

  public static final String VALUE = "value";

  public static final String ACCESS_TIME = "accessTime";

  public static final String WRITE_TIME = "writeTime";

  public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  public static final MethodType FACTORY = MethodType.methodType(void.class, Caffeine.class, CacheLoader.class, boolean.class);

  private LocalCacheFactory() {
  }

  /**
   * Returns a cache optimized for this configuration.
   */
  static <K, V> BoundedLocalCache<K, V> newBoundedLocalCache(Caffeine<K, V> builder,
      CacheLoader<? super K, V> cacheLoader, boolean async) {
    StringBuilder sb = new StringBuilder("com.github.benmanes.caffeine.cache.");
    if (builder.isStrongKeys()) {
      sb.append('S');
    } else {
      sb.append('W');
    }
    if (builder.isStrongValues()) {
      sb.append('S');
    } else {
      sb.append('I');
    }
    if (builder.removalListener != null) {
      sb.append('L');
    }
    if (builder.isRecordingStats()) {
      sb.append('S');
    }
    if (builder.evicts()) {
      sb.append('M');
      if (builder.isWeighted()) {
        sb.append('W');
      } else {
        sb.append('S');
      }
    }
    if (builder.expiresAfterAccess() || builder.expiresVariable()) {
      sb.append('A');
    }
    if (builder.expiresAfterWrite()) {
      sb.append('W');
    }
    if (builder.refreshes()) {
      sb.append('R');
    }
    try {
      Class<?> clazz = LocalCacheFactory.class.getClassLoader().loadClass(sb.toString());
      MethodHandle handle = LOOKUP.findConstructor(clazz, FACTORY);
      return (BoundedLocalCache<K, V>) handle.invoke(builder, cacheLoader, async);
    } catch (Throwable t) {
      throw new IllegalStateException(sb.toString(), t);
    }
  }
}
