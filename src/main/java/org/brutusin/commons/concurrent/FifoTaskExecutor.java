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

import java.util.HashMap;
import java.util.TreeMap;
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
 * A maximum of {@code maxThreads} (see {@link #FifoTaskExecutor(int)}) tasks
 * can be run concurrently. If more task are submitted,
 * {@link #execute(FifoTask)} blocks until a thread becomes available.
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 * @see FifoTask
 */
public class FifoTaskExecutor<E> {

    private final int maxThreads;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final HashMap<Integer, FifoTask<E>> taskMap = new HashMap();
    private final TreeMap<Integer, Result<E>> resultMap = new TreeMap();

    private volatile int lastResult=-1;
    private volatile int idCounter;
    private volatile int activeCounter;

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

    private void processResults() {
        synchronized (resultMap) {
            while (!resultMap.isEmpty() && resultMap.firstKey() == lastResult + 1) {
                Integer id = resultMap.firstKey();
                Result<E> result = resultMap.remove(id);
                FifoTask<E> task = taskMap.remove(id);
                if (result.th != null) {
                    task.onError(result.th);
                } else {
                    task.runSequential(result.e);
                }
                lastResult = id;
            }
        }
    }

    /**
     * Executes the submitted task. If the maximum number of pooled threads is
     * in use, this method blocks until one of a them is available.
     *
     * @param task
     * @throws InterruptedException
     */
    public void execute(final FifoTask<E> task) throws InterruptedException {
        final int id;
        synchronized (this) {
            id = idCounter++;
            taskMap.put(id, task);
            while (activeCounter >= maxThreads) {
                wait();
            }
            activeCounter++;
        }

        this.threadPoolExecutor.execute(new Runnable() {
            public void run() {
                try {
                    try {
                        final E outcome = task.runParallel();
                        synchronized (resultMap) {
                            resultMap.put(id, new Result(outcome));
                        }
                    } catch (Throwable th) {
                        synchronized (resultMap) {
                            resultMap.put(id, new Result(null, th));
                        }
                    } finally {
                        processResults();
                        synchronized (FifoTaskExecutor.this) {
                            activeCounter--;
                            FifoTaskExecutor.this.notifyAll();
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FifoTaskExecutor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        });
    }

    private class Result<E> {

        private final E e;
        private final Throwable th;

        public Result(E e) {
            this.e = e;
            this.th = null;
        }

        public Result(E e, Throwable th) {
            this.e = e;
            this.th = th;
        }
    }
}
