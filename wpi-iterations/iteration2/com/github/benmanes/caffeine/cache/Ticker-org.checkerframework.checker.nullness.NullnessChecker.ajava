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

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A time source that returns a time value representing the number of nanoseconds elapsed since some
 * fixed but arbitrary point in time.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
public interface Ticker {

    /**
     * Returns the number of nanoseconds elapsed since this ticker's fixed point of reference.
     *
     * @return the number of nanoseconds elapsed since this ticker's fixed point of reference
     */
    @org.checkerframework.dataflow.qual.Pure
    long read();

    /**
     * Returns a ticker that reads the current time using {@link System#nanoTime}.
     *
     * @return a ticker that reads the current time using {@link System#nanoTime}
     */
    @org.checkerframework.dataflow.qual.Pure
    static @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Ticker systemTicker() {
        return SystemTicker.INSTANCE;
    }

    /**
     * Returns a ticker that always returns {@code 0}.
     *
     * @return a ticker that always returns {@code 0}
     */
    @org.checkerframework.dataflow.qual.Pure
    static @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Ticker disabledTicker() {
        return DisabledTicker.INSTANCE;
    }
}

@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
enum SystemTicker implements Ticker {

    INSTANCE;

    public long read() {
        return System.nanoTime();
    }
}

@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
enum DisabledTicker implements Ticker {

    INSTANCE;

    public long read() {
        return 0L;
    }
}
