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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.pattern.LogEvent;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author russell
 * 
 */
public class InMemoryAppenderTest {

    private static final int CAPACITY = 10;
    // needed to vary the logger name
    // Log4j won't destroy or remove a logger
    private final String randomName = UUID.randomUUID().toString();

    private InMemoryAppender inMemoryAppender;
    private LoggerFactory lf;
    @Mock private Logger mockLogger;
    @Captor private ArgumentCaptor<LoggingEvent> logEventCaptor;

    /**
     * Set up a logging hierarchy programmatically that includes a logger that
     * uses the {@link InMemoryAppender} and a {@link DelegatingMockLogger} that
     * the {@link InMemoryAppender} delegates to.
     */
    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
        lf = new LoggerFactory() {
            @Override public Logger makeNewLoggerInstance(String str) {
                return new DelegatingMockLogger("any", mockLogger);
            }
        };

        inMemoryAppender = new InMemoryAppender();
        inMemoryAppender.setCapacity(CAPACITY);
        inMemoryAppender.setDelegateName("mock.logger" + randomName);

        Logger rootLogger = Logger.getRootLogger();

        rootLogger.setLevel(Level.FATAL);
        rootLogger.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN)));

        Logger testLogger = rootLogger.getLoggerRepository().getLogger("inmem");
        testLogger.setLevel(Level.DEBUG);
        testLogger.addAppender(inMemoryAppender);
        testLogger.setAdditivity(false);

        rootLogger.getLoggerRepository().getLogger("mock.logger" + randomName, lf).setAdditivity(false);
    }

    /**
     * Test method for
     * {@link com.basho.riak.client.http.util.logging.InMemoryAppender#append(org.apache.log4j.spi.LoggingEvent)}
     * .
     */
    @Test public void logsEventsToDelegateOnDump() {
        final String logMessage = "testing testing one, two, three";
        Logger testLogger = Logger.getLogger("inmem");
        assertNotNull(testLogger);

        testLogger.debug(logMessage);

        inMemoryAppender.dump();

        verify(mockLogger, times(1)).callAppenders(logEventCaptor.capture());

        assertTrue("Expected log message to be present", getLogMessage(logEventCaptor.getValue()).contains(logMessage));

        reset(mockLogger);

        inMemoryAppender.dump();

        verify(mockLogger, never()).callAppenders(any(LoggingEvent.class));
    }

    /**
     * Tests the no more than the <code>capacity</code> {@link LogEvent}s are in
     * the buffer at once, and that when new messages, over capacity are added,
     * old messages are lost.
     */
    @Test public void onlyHoldsTheLastNLoggedEventsInBuffer() {
        Logger testLogger = Logger.getLogger("inmem");
        assertNotNull(testLogger);

        for (int i = 0; i < CAPACITY * 2; i++) {
            testLogger.debug("message " + i);
        }

        inMemoryAppender.dump();

        verify(mockLogger, times(CAPACITY)).callAppenders(logEventCaptor.capture());

        int cnt = 10;
        for (LoggingEvent le : logEventCaptor.getAllValues()) {
            String expectedMessage = getLogMessage(le);
            assertTrue("Expected log message to be present " + expectedMessage,
                       expectedMessage.contains("message " + cnt));
            cnt++;
        }
    }

    /**
     * Tests the no more than the <code>capacity</code> {@link LogEvent}s are in
     * the buffer at once, and that when new messages, over capacity are added,
     * old messages are lost.
     */
    @Test public void capacityCannotBeSetSmallerThanCurrentSize() {
        Logger testLogger = Logger.getLogger("inmem");
        assertNotNull(testLogger);

        for (int i = 0; i < CAPACITY; i++) {
            testLogger.debug("message " + i);
        }

        try {
            inMemoryAppender.setCapacity(4);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // NO-OP
        }
    }

    /**
     * @param value
     * @return
     */
    private String getLogMessage(LoggingEvent value) {
        return value.getMessage() == null ? "" : value.getMessage().toString();
    }
}
