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

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.brutusin.commons.Bean;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public class FifoRunnerTest {

    private static final long SEED = System.currentTimeMillis();

    private long execute(int maxThreads, int numExecutions) throws InterruptedException {

        long start = System.currentTimeMillis();
        final FifoTaskExecutor<Integer> fifoRunner = new FifoTaskExecutor(maxThreads);
        final Bean<AssertionError> assertionWrapper = new Bean();
        final Bean<Integer> intWrapper = new Bean();
        intWrapper.setValue(-1);
        final Random random = new Random(SEED);

        try {
            for (int i = 0; i < numExecutions; i++) {
                final Integer index = i;
                fifoRunner.execute(new FifoTask<Integer>() {
                    @Override
                    public Integer runParallel() {
                        double d = random.nextDouble();
                        try {
                            Thread.sleep((long) (d * 100));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        if (d < 0.5) {
                            throw new RuntimeException(String.valueOf(index));
                        }
                        return index;
                    }

                    @Override
                    public void runSequential(Integer i) {
                        System.out.println("runSequential(): " + i);
                        if (i != intWrapper.getValue() + 1) {
                            assertionWrapper.setValue(new AssertionError(i));
                        }
                        intWrapper.setValue(i);
                    }

                    public void onError(Throwable th) {
                        System.out.println("onError(): " + th.getMessage());
                        intWrapper.setValue(intWrapper.getValue() + 1);
                    }
                });
            }
        } finally {
            fifoRunner.shutdown();
            fifoRunner.awaitTermination(1, TimeUnit.DAYS);
        }
        if (assertionWrapper.getValue() != null) {
            throw assertionWrapper.getValue();
        }
        assertTrue(numExecutions == intWrapper.getValue() + 1);

        return System.currentTimeMillis() - start;
    }

    /**
     * Test of run method, of class FifoRunner.
     */
    @Test
    public void testExecute() throws Exception {
        long time1 = execute(1, 100);
        if (Runtime.getRuntime().availableProcessors() > 1) {
            long time2 = execute(2, 100);
            assertTrue(time1 > time2);
            if (Runtime.getRuntime().availableProcessors() > 2) {
                long time3 = execute(3, 100);
                assertTrue(time2 > time3);
                if (Runtime.getRuntime().availableProcessors() > 3) {
                    long time4 = execute(4, 100);
                    assertTrue(time3 > time4);
                }
            }
        }
    }
}
