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
package com.github.benmanes.caffeine.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import sun.misc.Unsafe;

/**
 * Static access to {@link Unsafe} and convenient utility methods for performing low-level, unsafe
 * operations.
 * <p>
 * <b>Warning:</b> This class is scheduled for removal in version <tt>3.0.0</tt>.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
public final class UnsafeAccess {

    static final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull String ANDROID = "THE_ONE";

    static final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull String OPEN_JDK = "theUnsafe";

    /**
     * The Unsafe instance.
     */
    public static final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull Unsafe UNSAFE;

    static {
        try {
            UNSAFE = load(OPEN_JDK, ANDROID);
        } catch (Exception e) {
            throw new Error("Failed to load sun.misc.Unsafe", e);
        }
    }

    /**
     * Returns the location of a given static field.
     *
     * @param clazz the class containing the field
     * @param fieldName the name of the field
     * @return the address offset of the field
     */
    @org.checkerframework.dataflow.qual.Impure
    public static  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long objectFieldOffset(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Class<?> clazz, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull String fieldName) {
        try {
            return UNSAFE.objectFieldOffset(clazz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException | SecurityException e) {
            throw new Error(e);
        }
    }

    @org.checkerframework.dataflow.qual.Impure
    static @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Unsafe load(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull String openJdk, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull String android) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Field field;
        try {
            // try OpenJDK field name
            field = Unsafe.class.getDeclaredField(openJdk);
        } catch (NoSuchFieldException e) {
            try {
                // try Android field name
                field = Unsafe.class.getDeclaredField(android);
            } catch (NoSuchFieldException e2) {
                // try to create a new instance
                Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
                unsafeConstructor.setAccessible(true);
                return unsafeConstructor.newInstance();
            }
        }
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    @org.checkerframework.dataflow.qual.SideEffectFree
    private UnsafeAccess() {
    }
}
