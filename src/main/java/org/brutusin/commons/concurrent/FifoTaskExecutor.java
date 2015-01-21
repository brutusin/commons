/*
 * Copyright 2015 brutusin.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brutusin.commons.concurrent;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes {@link FifoTask} tasks asynchronously keeping the output order,
 * using a pool of threads of size {@code maxThreads}.
 * <br><br>
 * A maximum of {@code maxThreads} (see {@link #FifoTaskExecutor(int)}) tasks can be
 * run concurrently. If more task are submitted, {@link #execute(FifoTask)}
 * blocks until a thread becomes available.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 * @see FifoTask
 */
public class FifoTaskExecutor<E> {

    private final int maxThreads;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final Queue<FifoTask> queue = new LinkedList();

    private int counter;

    /**
     * Same as {@code FifoTaskExecutor(0, null)}
     */
    public FifoTaskExecutor() {
        this(0, null);
    }

    /**
     * Same as {@code FifoTaskExecutor(maxThreads, null)}
     */
    public FifoTaskExecutor(int maxThreads) {
        this(maxThreads, null);
    }

    /**
     * If {@code maxThreads == 0}, {@code maxThreads} is set to the number of
     * available processors returned by
     * {@code Runtime.getRuntime().availableProcessors()}.
     *
     * @param maxThreads number of maximum allowed threads.
     * @param tf the factory to use when the executor creates a new thread. If
     * {@code null}, {@link Executors#defaultThreadFactory()} is used.
     * @throws IllegalArgumentException if {@code maxThreads < 0}
     */
    public FifoTaskExecutor(int maxThreads, ThreadFactory tf) {
        if (maxThreads < 0) {
            throw new IllegalArgumentException("maxThreads can not be negative");
        } else if (maxThreads == 0) {
            maxThreads = Runtime.getRuntime().availableProcessors();
        }
        if (tf == null) {
            tf = Executors.defaultThreadFactory();
        }
        this.maxThreads = maxThreads;
        this.threadPoolExecutor = new ThreadPoolExecutor(
                maxThreads,
                maxThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                tf);
    }

    /**
     * @return Number of maximum allowed threads
     */
    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * {@link ExecutorService#shutdown()}
     */
    public void shutdown() {
        threadPoolExecutor.shutdown();
    }

    /**
     * {@link ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)}
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return threadPoolExecutor.awaitTermination(timeout, unit);
    }

    /**
     * Executes the submitted task. If the maximum number of pooled threads is
     * in use, this method blocks until one of a them is available.
     *
     * @param task
     * @throws InterruptedException
     */
    public void execute(final FifoTask<E> task) throws InterruptedException {
        synchronized (this) {
            while (counter >= maxThreads) {
                wait();
            }
            counter++;
        }
        synchronized (queue) {
            queue.add(task);
        }
        this.threadPoolExecutor.execute(new Runnable() {
            public void run() {
                try {
                    try {
                        final E outcome = task.runParallel();
                        synchronized (queue) {
                            while (queue.peek() != task) {
                                queue.wait();
                            }
                            queue.poll();
                            task.runSequential(outcome);
                            queue.notifyAll();
                        }

                    } catch (Throwable th) {
                        synchronized (queue) {
                            while (queue.peek() != task) {
                                queue.wait();
                            }
                            queue.poll();
                            task.onError(th);
                            queue.notifyAll();
                        }
                    } finally {
                        // Unblock execute() callers
                        synchronized (FifoTaskExecutor.this) {
                            counter--;
                            FifoTaskExecutor.this.notifyAll();
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FifoTaskExecutor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });
    }
}
