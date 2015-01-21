/*
 * Copyright 2014 brutusin.org
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

/**
 * Instances of this class are executed in parallel via 
 * {@link FifoTaskExecutor#execute(FifoTask)}, under the following contracts:
 * <br><br>
 * <b>FifoTask life-cycle. Contract for a task:</b>
 * <ol>
 * <li>{@link #runParallel()} is invoked first.</li>
 * <li>Depending on {@code runParallel()} outcome; either {@link #runSequential(Object)}
 * or {@link #onError(Throwable)} is invoked afterwards.</li>
 * </ol>
 * <br>
 * <b>FifoTaskExecutor contract for a group of executing tasks:</b>
 * <ul>
 * <li>{@link #runParallel()} are invoked in parallel.</li>
 * <li>{@link #runSequential(Object)} are invoked sequentially, following the
 * submission order.</li>
 * </ul>
 * 
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 * @param <E> generic parameter
 */
public interface FifoTask<E> {
    
    /**
     * Method to be run in parallel.
     * @return The outcome to be passed to {@code runSequential(E e)} 
     */
    public E runParallel();
    
    /**
     * Method to be run sequentially.
     * @param e returned by {@code runParallel()}
     */
    public void runSequential(E e);
    
    /**
     * Method to be run sequentially, in case of an error occurred in 
     * {@code runParallel()} execution.
     * @param th launched by {@code runParallel()}
     */
    public void onError(Throwable th);
}
