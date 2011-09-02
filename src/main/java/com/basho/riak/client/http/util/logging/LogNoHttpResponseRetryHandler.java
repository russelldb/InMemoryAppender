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
 * your log4j correctly. See the log4j.properties example with this project
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

    public LogNoHttpResponseRetryHandler() {
        this(INMEM_APPENDER_NAME);
    }

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
