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
package com.github.benmanes.caffeine;

import static java.util.Objects.requireNonNull;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.github.benmanes.caffeine.SingleConsumerQueue.Node;
import com.github.benmanes.caffeine.base.UnsafeAccess;

/**
 * A lock-free unbounded queue based on linked nodes that supports concurrent producers and is
 * restricted to a single consumer. This queue orders elements FIFO (first-in-first-out). The
 * <em>head</em> of the queue is that element that has been on the queue the longest time. The
 * <em>tail</em> of the queue is that element that has been on the queue the shortest time. New
 * elements are inserted at the tail of the queue, and the queue retrieval operations obtain
 * elements at the head of the queue. Like most other concurrent collection implementations, this
 * class does not permit the use of {@code null} elements.
 * <p>
 * A {@code SingleConsumerQueue} is an appropriate choice when many producer threads will share
 * access to a common collection and a single consumer thread drains it. This collection is useful
 * in scenarios such as implementing flat combining, actors, or lock amortization.
 * <p>
 * This implementation employs combination to transfer elements between threads that are producing
 * concurrently. This approach avoids contention on the queue by combining colliding operations
 * that have identical semantics. When a pair of producers collide, the task of performing the
 * combined set of operations is delegated to one of the threads and the other thread optionally
 * waits for its operation to be completed. This decision of whether to wait for completion is
 * determined by constructing either a <em>linearizable</em> or <em>optimistic</em> queue.
 * <p>
 * Iterators are <i>weakly consistent</i>, returning elements reflecting the state of the queue at
 * some point at or since the creation of the iterator. They do <em>not</em> throw {@link
 * java.util.ConcurrentModificationException}, and may proceed concurrently with other operations.
 * Elements contained in the queue since the creation of the iterator will be returned exactly once.
 * <p>
 * Beware that it is the responsibility of the caller to ensure that a consumer has exclusive read
 * access to the queue. This implementation does <em>not</em> include fail-fast behavior to guard
 * against incorrect consumer usage.
 * <p>
 * Beware that, unlike in most collections, the {@code size} method is <em>NOT</em> a
 * constant-time operation. Because of the asynchronous nature of these queues, determining the
 * current number of elements requires a traversal of the elements, and so may report inaccurate
 * results if this collection is modified during traversal.
 * <p>
 * <b>Warning:</b> This class is scheduled for removal in version <tt>3.0.0</tt>.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <E> the type of elements held in this collection
 */
@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
public final class SingleConsumerQueue<E> extends SCQHeader.HeadAndTailRef<E> implements Queue<E>, Serializable {

    /*
   * The queue is represented as a singly-linked list with an atomic head and tail reference. It is
   * based on the non-intrusive multi-producer / single-consumer node queue described by
   * Dmitriy Vyukov [1].
   *
   * The backoff strategy of combining operations with identical semantics is based on inverting
   * the elimination technique [2]. Elimination allows pairs of operations with reverse semantics,
   * like pushes and pops on a stack, to complete without any central coordination and therefore
   * substantially aids scalability. The approach of applying elimination and reversing its
   * semantics was explored in [3, 4]. Unlike other approaches, this implementation does not use
   * opcodes or a background thread.
   *
   * This implementation borrows optimizations from {@link java.util.concurrent.Exchanger} for
   * choosing an arena location and awaiting a match [5].
   *
   * [1] Non-intrusive MPSC node-based queue
   * http://www.1024cores.net/home/lock-free-algorithms/queues/non-intrusive-mpsc-node-based-queue
   * [2] A Scalable Lock-free Stack Algorithm
   * http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.156.8728
   * [3] Using Elimination to Implement Scalable and Lock-Free FIFO Queues
   * http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.108.6422
   * [4] A Dynamic Elimination-Combining Stack Algorithm
   * http://www.cs.bgu.ac.il/~hendlerd/papers/DECS.pdf
   * [5] A Scalable Elimination-based Exchange Channel
   * http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.59.7396
   */
    /**
     * The number of CPUs
     */
    static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int NCPU = Runtime.getRuntime().availableProcessors();

    /**
     * The number of slots in the elimination array.
     */
    static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int ARENA_LENGTH = ceilingPowerOfTwo((NCPU + 1) / 2);

    /**
     * The mask value for indexing into the arena.
     */
    static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int ARENA_MASK = ARENA_LENGTH - 1;

    /**
     * The factory for creating an optimistic node.
     */
    static final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Function<?, ?> OPTIMISIC = Node<Object>::new;

    /**
     * The number of times to spin (doing nothing except polling a memory location) before giving up
     * while waiting to eliminate an operation. Should be zero on uniprocessors. On multiprocessors,
     * this value should be large enough so that two threads exchanging items as fast as possible
     * block only when one of them is stalled (due to GC or preemption), but not much longer, to avoid
     * wasting CPU resources. Seen differently, this value is a little over half the number of cycles
     * of an average context switch time on most systems. The value here is approximately the average
     * of those across a range of tested systems.
     */
    static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int SPINS = (NCPU == 1) ? 0 : 2000;

    /**
     * The offset to the thread-specific probe field.
     */
    static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long PROBE = UnsafeAccess.objectFieldOffset(Thread.class, "threadLocalRandomProbe");

    @org.checkerframework.dataflow.qual.Pure
    static  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int ceilingPowerOfTwo( @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int x) {
        // From Hacker's Delight, Chapter 3, Harry S. Warren Jr.
        return 1 << -Integer.numberOfLeadingZeros(x - 1);
    }

    final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull AtomicReference<Node<E>> @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull [] arena;

    final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Function<E, Node<E>> factory;

    @org.checkerframework.dataflow.qual.Impure
    private SingleConsumerQueue(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Function<E, Node<E>> factory) {
        arena = new AtomicReference[ARENA_LENGTH];
        for (int i = 0; i < ARENA_LENGTH; i++) {
            arena[i] = new AtomicReference<>();
        }
        Node<E> node = new Node<>(null);
        this.factory = factory;
        lazySetTail(node);
        head = node;
    }

    /**
     * Creates a queue with an optimistic backoff strategy. A thread completes its operation
     * without waiting after it successfully hands off the additional element(s) to another producing
     * thread for batch insertion. This optimistic behavior may result in additions not appearing in
     * FIFO order due to the backoff strategy trying to compensate for queue contention.
     *
     * @param <E> the type of elements held in this collection
     * @return a new queue where producers complete their operation immediately if combined with
     *         another producing thread's
     */
    @org.checkerframework.dataflow.qual.Pure
    public static <E> @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> optimistic() {
        Function<E, Node<E>> factory = (Function<E, Node<E>>) OPTIMISIC;
        return new SingleConsumerQueue<>(factory);
    }

    /**
     * Creates a queue with a linearizable backoff strategy. A thread waits for a completion
     * signal if it successfully hands off the additional element(s) to another producing
     * thread for batch insertion.
     *
     * @param <E> the type of elements held in this collection
     * @return a new queue where producers wait for a completion signal after combining its addition
     *         with another producing thread's
     */
    @org.checkerframework.dataflow.qual.Pure
    public static <E> @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> linearizable() {
        return new SingleConsumerQueue<>(LinearizableNode<E>::new);
    }

    @org.checkerframework.dataflow.qual.Pure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isEmpty(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> this) {
        return (head == tail);
    }

    @org.checkerframework.dataflow.qual.Impure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int size(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> this) {
        Node<E> cursor = head;
        Node<E> t = tail;
        int size = 0;
        while ((cursor != t) && (size != Integer.MAX_VALUE)) {
            Node<E> next = cursor.getNextRelaxed();
            if (next == null) {
                while ((next = cursor.next) == null) {
                }
            }
            cursor = next;
            size++;
        }
        return size;
    }

    @org.checkerframework.dataflow.qual.Impure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean contains(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object o) {
        if (o == null) {
            return false;
        }
        for (Iterator<E> it = iterator(); it.hasNext(); ) {
            if (o.equals(it.next())) {
                return true;
            }
        }
        return false;
    }

    @org.checkerframework.dataflow.qual.Impure
    public E peek(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> this) {
        Node<E> h = head;
        Node<E> t = tail;
        if (h == t) {
            return null;
        }
        Node<E> next = h.getNextRelaxed();
        if (next == null) {
            while ((next = h.next) == null) {
            }
        }
        return next.value;
    }

    @org.checkerframework.dataflow.qual.Impure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean offer(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> this, E e) {
        requireNonNull(e);
        Node<E> node = factory.apply(e);
        append(node, node);
        return true;
    }

    @org.checkerframework.dataflow.qual.Impure
    public E poll(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> this) {
        Node<E> h = head;
        Node<E> next = h.getNextRelaxed();
        if (next == null) {
            if (h == tail) {
                return null;
            } else {
                while ((next = h.next) == null) {
                }
            }
        }
        E e = next.value;
        next.value = null;
        head = next;
        if (factory == OPTIMISIC) {
            // prevent nepotism
            h.next = null;
        }
        return e;
    }

    @org.checkerframework.dataflow.qual.Impure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean add(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> this, E e) {
        return offer(e);
    }

    @org.checkerframework.dataflow.qual.Impure
    public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean addAll(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> this, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Collection<? extends E> c) {
        requireNonNull(c);
        Node<E> first = null;
        Node<E> last = null;
        for (E e : c) {
            requireNonNull(e);
            if (first == null) {
                first = factory.apply(e);
                last = first;
            } else {
                Node<E> newLast = new Node<>(e);
                last.lazySetNext(newLast);
                last = newLast;
            }
        }
        if (first == null) {
            return false;
        }
        append(first, last);
        return true;
    }

    /**
     * Adds the linked list of nodes to the queue.
     */
    @org.checkerframework.dataflow.qual.Impure
    void append(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Node<E> first, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Node<E> last) {
        for (; ; ) {
            Node<E> t = tail;
            if (casTail(t, last)) {
                t.lazySetNext(first);
                if (factory == OPTIMISIC) {
                    return;
                }
                for (; ; ) {
                    first.complete();
                    if (first == last) {
                        return;
                    }
                    Node<E> next = first.getNextRelaxed();
                    if (next.value == null) {
                        // reduce nepotism
                        first.next = null;
                    }
                    first = next;
                }
            }
            Node<E> node = transferOrCombine(first, last);
            if (node == null) {
                first.await();
                return;
            } else if (node != first) {
                last = node;
            }
        }
    }

    /**
     * Attempts to receive a linked list from a waiting producer or transfer the specified linked list
     * to an arriving producer.
     *
     * @param first the first node in the linked list to try to transfer
     * @param last the last node in the linked list to try to transfer
     * @return either {@code null} if the element was transferred, the first node if neither a
     *         transfer nor receive were successful, or the received last element from a producer
     */
    @org.checkerframework.dataflow.qual.Impure
    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Node<E> transferOrCombine(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Node<E> first, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Node<E> last) {
        int index = index();
        AtomicReference<Node<E>> slot = arena[index];
        for (; ; ) {
            Node<E> found = slot.get();
            if (found == null) {
                if (slot.compareAndSet(null, first)) {
                    for (int spin = 0; spin < SPINS; spin++) {
                        if (slot.get() != first) {
                            return null;
                        }
                    }
                    return slot.compareAndSet(first, null) ? first : null;
                }
            } else if (slot.compareAndSet(found, null)) {
                last.lazySetNext(found);
                last = findLast(found);
                for (int i = 1; i < ARENA_LENGTH; i++) {
                    slot = arena[(i + index) & ARENA_MASK];
                    found = slot.get();
                    if ((found != null) && slot.compareAndSet(found, null)) {
                        last.lazySetNext(found);
                        last = findLast(found);
                    }
                }
                return last;
            }
        }
    }

    /**
     * Returns the arena index for the current thread.
     */
    @org.checkerframework.dataflow.qual.Impure
    static  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull int index() {
        int probe = UnsafeAccess.UNSAFE.getInt(Thread.currentThread(), PROBE);
        if (probe == 0) {
            // force initialization
            ThreadLocalRandom.current();
            probe = UnsafeAccess.UNSAFE.getInt(Thread.currentThread(), PROBE);
        }
        return (probe & ARENA_MASK);
    }

    /**
     * Returns the last node in the linked list.
     */
    @org.checkerframework.dataflow.qual.Impure
    static <E> @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Node<E> findLast(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Node<E> node) {
        Node<E> next;
        while ((next = node.getNextRelaxed()) != null) {
            node = next;
        }
        return node;
    }

    @org.checkerframework.dataflow.qual.Impure
    public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Iterator<E> iterator(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> this) {
        return new Iterator<E>() {

            @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.MonotonicNonNull Node<E> prev;

            @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Node<E> t = tail;

            @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Node<E> cursor = head;

             @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean failOnRemoval = true;

            @org.checkerframework.dataflow.qual.Pure
            public  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean hasNext() {
                return (cursor != t);
            }

            @org.checkerframework.dataflow.qual.Impure
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                advance();
                failOnRemoval = false;
                return cursor.value;
            }

            @org.checkerframework.dataflow.qual.Impure
            private void advance() {
                if ((prev == null) || !failOnRemoval) {
                    prev = cursor;
                }
                cursor = awaitNext();
            }

            @org.checkerframework.dataflow.qual.Impure
            public void remove() {
                if (failOnRemoval) {
                    throw new IllegalStateException();
                }
                failOnRemoval = true;
                cursor.value = null;
                if (t == cursor) {
                    prev.lazySetNext(null);
                    if (casTail(t, prev)) {
                        return;
                    }
                }
                prev.lazySetNext(awaitNext());
            }

            @org.checkerframework.dataflow.qual.Impure
            @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Node<E> awaitNext() {
                if (cursor.getNextRelaxed() == null) {
                    while (cursor.next == null) {
                    }
                }
                return cursor.getNextRelaxed();
            }
        };
    }

    /* ---------------- Serialization Support -------------- */
    static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long serialVersionUID = 1;

    @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    /**
     * A proxy that is serialized instead of the queue.
     */
    static final class SerializationProxy<E> implements Serializable {

        final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean linearizable;

        final @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull List<E> elements;

        SerializationProxy(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull SingleConsumerQueue<E> queue) {
            linearizable = (queue.factory.apply(null) instanceof LinearizableNode<?>);
            elements = new ArrayList<>(queue);
        }

        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Object readResolve() {
            SingleConsumerQueue<E> queue = linearizable ? linearizable() : optimistic();
            queue.addAll(elements);
            return queue;
        }

        static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long serialVersionUID = 1;
    }

    static class Node<E> {

        static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long NEXT_OFFSET = UnsafeAccess.objectFieldOffset(Node.class, "next");

        @org.checkerframework.checker.initialization.qual.FBCBottom @org.checkerframework.checker.nullness.qual.Nullable E value;

        volatile @org.checkerframework.checker.initialization.qual.FBCBottom @org.checkerframework.checker.nullness.qual.Nullable Node<E> next;

        Node(E value) {
            this.value = value;
        }

        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Node<E> getNextRelaxed() {
            return (Node<E>) UnsafeAccess.UNSAFE.getObject(this, NEXT_OFFSET);
        }

        void lazySetNext(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Node<E> newNext) {
            UnsafeAccess.UNSAFE.putOrderedObject(this, NEXT_OFFSET, newNext);
        }

        /**
         * A no-op notification that the element was added to the queue.
         */
        void complete() {
        }

        /**
         * A no-op wait until the operation has completed.
         */
        void await() {
        }

        /**
         * Always returns that the operation completed.
         */
         @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isDone() {
            return true;
        }

        public @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull String toString(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Node<E> this) {
            return getClass().getSimpleName() + "[" + value + "]";
        }
    }

    static final class LinearizableNode<E> extends Node<E> {

        volatile  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean done;

        LinearizableNode(E value) {
            super(value);
        }

        /**
         * A notification that the element was added to the queue.
         */
        void complete(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull LinearizableNode<E> this) {
            done = true;
        }

        /**
         * A busy wait until the operation has completed.
         */
        void await(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull LinearizableNode<E> this) {
            while (!done) {
            }
        }

        /**
         * Returns whether the operation completed.
         */
         @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean isDone(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull LinearizableNode<E> this) {
            return done;
        }
    }
}

/**
 * The namespace for field padding through inheritance.
 */
@org.checkerframework.framework.qual.AnnotatedFor("org.checkerframework.checker.nullness.NullnessChecker")
final class SCQHeader {

    abstract static class PadHead<E> extends AbstractQueue<E> {

        long p00, p01, p02, p03, p04, p05, p06, p07;

        long p10, p11, p12, p13, p14, p15, p16;
    }

    /**
     * Enforces a memory layout to avoid false sharing by padding the head node.
     */
    abstract static class HeadRef<E> extends PadHead<E> {

        @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Node<E> head;
    }

    abstract static class PadHeadAndTail<E> extends HeadRef<E> {

        long p20, p21, p22, p23, p24, p25, p26, p27;

        long p30, p31, p32, p33, p34, p35, p36;
    }

    /**
     * Enforces a memory layout to avoid false sharing by padding the tail node.
     */
    abstract static class HeadAndTailRef<E> extends PadHeadAndTail<E> {

        static final  @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull long TAIL_OFFSET = UnsafeAccess.objectFieldOffset(HeadAndTailRef.class, "tail");

        volatile Node<E> tail;

        void lazySetTail(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Node<E> next) {
            UnsafeAccess.UNSAFE.putOrderedObject(this, TAIL_OFFSET, next);
        }

         @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull boolean casTail(@org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.Nullable Node<E> expect, @org.checkerframework.checker.initialization.qual.Initialized @org.checkerframework.checker.nullness.qual.NonNull Node<E> update) {
            return UnsafeAccess.UNSAFE.compareAndSwapObject(this, TAIL_OFFSET, expect, update);
        }
    }
}
