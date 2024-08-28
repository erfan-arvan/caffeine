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
package com.github.benmanes.caffeine.cache.stats;

import static java.util.Objects.requireNonNull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link StatsCounter} implementation that suppresses and logs any exception thrown by the
 * delegate <tt>statsCounter</tt>.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
final class GuardedStatsCounter implements StatsCounter {

    static final Logger logger = Logger.getLogger(GuardedStatsCounter.class.getName());

    final StatsCounter delegate;

    GuardedStatsCounter(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull StatsCounter delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public void recordHits(int count) {
        try {
            delegate.recordHits(count);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception thrown by stats counter", t);
        }
    }

    public void recordMisses(int count) {
        try {
            delegate.recordMisses(count);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception thrown by stats counter", t);
        }
    }

    public void recordLoadSuccess(long loadTime) {
        try {
            delegate.recordLoadSuccess(loadTime);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception thrown by stats counter", t);
        }
    }

    public void recordLoadFailure(long loadTime) {
        try {
            delegate.recordLoadFailure(loadTime);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception thrown by stats counter", t);
        }
    }

    public void recordEviction() {
        try {
            delegate.recordEviction();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception thrown by stats counter", t);
        }
    }

    public void recordEviction(int weight) {
        try {
            delegate.recordEviction(weight);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception thrown by stats counter", t);
        }
    }

    public CacheStats snapshot() {
        try {
            return delegate.snapshot();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception thrown by stats counter", t);
            return CacheStats.empty();
        }
    }

    public String toString() {
        return delegate.toString();
    }
}
