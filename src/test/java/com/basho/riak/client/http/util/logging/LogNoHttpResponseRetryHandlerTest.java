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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author russell
 * 
 */
public class LogNoHttpResponseRetryHandlerTest {

    private static final String MOCK_APPENDER_NAME = "mockAppender";

    @Mock private InMemoryAppender mockAppender;

    private Logger testLogger;

    /**
     * @throws java.lang.Exception
     */
    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // Set up logger hierarchy
        Logger rootLogger = Logger.getRootLogger();

        rootLogger.setLevel(Level.FATAL);
        rootLogger.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN)));

        // there *must* be an httpclient.wire logger present
        testLogger = rootLogger.getLoggerRepository().getLogger("httpclient.wire");

        testLogger.setLevel(Level.DEBUG);
        InMemoryAppender appender = new InMemoryAppender();
        appender.setName(InMemoryAppender.DEFAULT_NAME);
        InMemoryAppender appender2 = new InMemoryAppender();
        appender2.setName("1235");
        testLogger.addAppender(appender);
        testLogger.addAppender(appender2);

        // add the mock appender
        testLogger.addAppender(new MockAppederWrapper(mockAppender));
    }

    @After public void removerAppenders() {
        testLogger.removeAllAppenders();
    }

    /**
     * Test method for
     * {@link com.basho.riak.client.http.util.logging.LogNoHttpResponseRetryHandler#LogNoHttpResponseRetryHandler()}
     * .
     */
    @Test public void constructor_blueSky() {
        new LogNoHttpResponseRetryHandler();
        new LogNoHttpResponseRetryHandler("1235");
    }

    /**
     * Test method for
     * {@link com.basho.riak.client.http.util.logging.LogNoHttpResponseRetryHandler#LogNoHttpResponseRetryHandler()}
     * .
     */
    @Test public void constructor_noAppender() {
        // due to horrible log4j we can't start again with no
        // logger hierarchy in this test
        // instead test that an appender must be present
        // using a non-existent appender.
        try {
            new LogNoHttpResponseRetryHandler("6789");
            fail("expected illegal state exception");
        } catch (IllegalStateException e) {
            // no-op
        }
    }

    /**
     * Test method for
     * {@link com.basho.riak.client.http.util.logging.LogNoHttpResponseRetryHandler#retryMethod(org.apache.commons.httpclient.HttpMethod, java.io.IOException, int)}
     * .
     */
    @Test public void retry() {
        final HttpMethod method = new GetMethod();
        final IOException noHttpResponseException = new NoHttpResponseException();
        final IOException otherException = new ConnectTimeoutException();
        final int executionCount = 0;

        // when retry is called with a NoHtpResponseException
        // dump should be called on the appender
        LogNoHttpResponseRetryHandler handler = new LogNoHttpResponseRetryHandler(MOCK_APPENDER_NAME);
        boolean expected = new DefaultHttpMethodRetryHandler().retryMethod(method, noHttpResponseException,
                                                                           executionCount);
        boolean actual = handler.retryMethod(method, noHttpResponseException, executionCount);

        verify(mockAppender, times(1)).dump();

        assertEquals(expected, actual);

        expected = new DefaultHttpMethodRetryHandler().retryMethod(method, otherException, executionCount);
        actual = handler.retryMethod(method, otherException, executionCount);

        // dump must not have been called again!
        verify(mockAppender, times(1)).dump();

        // but the behaviour of the handler should still match the default
        assertEquals(expected, actual);
    }

    /**
     * Appenders *must* have names and you can't mock a final method
     * (AppenderSkeleton.getName() is final), so wrap the mock and set the name
     * on super.
     * 
     */
    private static final class MockAppederWrapper extends InMemoryAppender {
        private final InMemoryAppender delegate;

        public MockAppederWrapper(InMemoryAppender mockDelegate) {
            super();
            super.setName(MOCK_APPENDER_NAME);
            this.delegate = mockDelegate;
        }

        /**
         * 
         * @see com.basho.riak.client.http.util.logging.InMemoryAppender#dump()
         */
        public void dump() {
            delegate.dump();
        }
    }
}
