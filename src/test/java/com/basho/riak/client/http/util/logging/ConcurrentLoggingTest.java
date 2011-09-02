/*
 * This file is provided to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.basho.riak.client.http.util.logging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * The {@link InMemoryAppender} and {@link LogNoHttpResponseRetryHandler} must
 * not lose nor duplicate log messages when called concurrently from multiple
 * threads.
 * 
 * @author russell
 * 
 */
public class ConcurrentLoggingTest {

    private static final String MESSAGE = "%d%d";
    private static final int CAPACITY = 100;
    // needed to vary the logger name
    // Log4j won't destroy or remove a logger
    private final String randomName = UUID.randomUUID().toString();

    private InMemoryAppender inMemoryAppender;
    private LoggerFactory lf;
    @Mock private Logger mockLogger;
    @Captor private ArgumentCaptor<LoggingEvent> logEventCaptor;

    /**
     * @throws java.lang.Exception
     */
    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        lf = new LoggerFactory() {
            @Override public Logger makeNewLoggerInstance(String str) {
                return new DelegatingMockLogger("any", mockLogger);
            }
        };

        inMemoryAppender = new InMemoryAppender();
        inMemoryAppender.setCapacity(CAPACITY);
        inMemoryAppender.setDelegateName("mock.logger" + randomName);
        inMemoryAppender.setName(InMemoryAppender.DEFAULT_NAME);

        Logger rootLogger = Logger.getRootLogger();

        rootLogger.setLevel(Level.FATAL);
        rootLogger.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN)));

        Logger httpclientWireLogger = rootLogger.getLoggerRepository().getLogger("httpclient.wire");
        httpclientWireLogger.setLevel(Level.DEBUG);
        httpclientWireLogger.addAppender(inMemoryAppender);
        httpclientWireLogger.setAdditivity(false);

        rootLogger.getLoggerRepository().getLogger("mock.logger" + randomName, lf).setAdditivity(false);

    }

    /**
     * @throws java.lang.Exception
     */
    @After public void tearDown() throws Exception {}

    /**
     * Test method for
     * {@link com.basho.riak.client.http.util.logging.LogNoHttpResponseRetryHandler#retryMethod(org.apache.commons.httpclient.HttpMethod, java.io.IOException, int)}
     * .
     * 
     * @throws InterruptedException
     */
    @Test public void retry_concurrentLogAndDump() throws InterruptedException {
        // create a bunch of threads
        // each must log 10 statements and call flush
        // ALL the statements must be present BUT ONCE in
        // the mock delegate appender (order does not matter)
        final int numThreads = 10;
        final LogNoHttpResponseRetryHandler handler = new LogNoHttpResponseRetryHandler();
        ExecutorService es = Executors.newFixedThreadPool(numThreads);
        List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(numThreads);

        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch dumpLatch = new CountDownLatch(10);

        for (int i = 0; i < numThreads; i++) {
            final int threadCounter = i;
            tasks.add(new Callable<Void>() {

                @Override public Void call() {
                    Logger logger = Logger.getLogger("httpclient.wire");
                    try {
                        startLatch.await();

                        for (int j = 0; j < 10; j++) {
                            logger.debug(String.format(MESSAGE, new Object[] { threadCounter, j }));
                        }

                        dumpLatch.countDown();
                        dumpLatch.await();

                        handler.retryMethod(new GetMethod(), new NoHttpResponseException(), 0);

                        return null;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        startLatch.countDown();
        es.invokeAll(tasks);

        verify(mockLogger, times(100)).callAppenders(logEventCaptor.capture());

        TreeSet<Integer> check = new TreeSet<Integer>();

        for (LoggingEvent le : logEventCaptor.getAllValues()) {
            // verify that each of Thread:Iter is present for 0-90-9
            int loc = Integer.parseInt(le.getMessage().toString());
            check.add(loc);
        }

        assertEquals(100, check.size());
        assertEquals(0, (int) check.first());
        assertEquals(99, (int) check.last());
    }
}
