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

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

/**
 * An {@link HttpMethodRetryHandler} that delegates directly to the
 * {@link DefaultHttpMethodRetryHandler}, after first checking the
 * {@link IOException} type and flushing the {@link InMemoryAppender} if
 * required.
 * 
 * <p>
 * Works in concert with {@link InMemoryAppender}. To use you must configure
 * your log4j correctly. See the log4j.properties example with this project.
 * Basically, the "httpclient.wire" logger must have an {@link InMemoryAppender}
 * configured for it. And the name of the appender must be made available to
 * this class, either via the constructor or by using
 * {@link InMemoryAppender#DEFAULT_NAME}
 * </p>
 * 
 * @author russell
 * 
 */
public class LogNoHttpResponseRetryHandler implements HttpMethodRetryHandler {

    private static final Logger logger = Logger.getLogger("httpclient.wire");
    private static final String INMEM_APPENDER_NAME = InMemoryAppender.DEFAULT_NAME;
    private final DefaultHttpMethodRetryHandler delegate = new DefaultHttpMethodRetryHandler();

    private final InMemoryAppender inMemoryAppender;

    /**
     * Create a handler which calls dump on an appender with the name
     * {@link InMemoryAppender#DEFAULT_NAME}
     */
    public LogNoHttpResponseRetryHandler() {
        this(INMEM_APPENDER_NAME);
    }

    /**
     * Create a hndler which called dump on an appender with the name
     * <code>inMemAppenderName</code>
     * 
     * @param inMemAppenderName
     *            the name of the "httpclient.wire" appender to call
     *            <code>dump</code> on when a {@link NoHttpResponseException} is
     *            received.
     */
    public LogNoHttpResponseRetryHandler(String inMemAppenderName) {
        Appender a = logger.getAppender(inMemAppenderName);
        if (a == null || !(a instanceof InMemoryAppender)) {
            throw new IllegalStateException("No " + INMEM_APPENDER_NAME + " appender found");
        }

        inMemoryAppender = (InMemoryAppender) a;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.commons.httpclient.HttpMethodRetryHandler#retryMethod(org.
     * apache.commons.httpclient.HttpMethod, java.io.IOException, int)
     */
    @Override public boolean retryMethod(HttpMethod method, IOException exception, int executionCount) {
        if (exception instanceof NoHttpResponseException) {
            inMemoryAppender.dump();
        }

        return delegate.retryMethod(method, exception, executionCount);
    }

}
