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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * An in memory log4j appender that collects log statements in a buffer.
 * <p>
 * The buffer is bounded to the <code>capacity</code> the appender is configured
 * with. When the capacity is reached, older {@link LoggingEvent}s are dropped
 * for new ones. The <code>capacity</code> most recent {@link LoggingEvent}s are
 * kept in the buffer and flushed to a delegate {@link Logger} when
 * <code>dump()</code> is called.
 * </p>
 * <p>
 * Created to allow apache hc "wire_trace" logging to be on always without
 * filling the logs with noise.
 * 
 * Works in concert with {@link LogNoHttpResponseRetryHandler}, when the retry
 * handler is called with a {@link NoHttpResponseException} it calls
 * <code>dump</code> on the InMemoryAppender.
 * </p>
 * 
 * <p>
 * Note: in order for this all to work you must configure your logging
 * correctly. See the example log4j.properties in this project.
 * </p>
 * 
 * 
 * @author russell
 * 
 */
public class InMemoryAppender extends AppenderSkeleton {

    public static final String DEFAULT_NAME = "InMem";

    private final Object bufferLock = new Object();
    private String delegateLoggerName = "basho.WireSink";
    private int capacity = 1000;
    private ArrayDeque<LoggingEvent> buffer = new ArrayDeque<LoggingEvent>();

    /**
     * The maximum number of {@link LoggingEvent}s to hold in the buffer. When
     * this size is reached older {@link LoggingEvent}s are dropped for new ones
     * (FIFO)
     * <p>
     * Usually called by the log4j framework: eg.
     * <code>log4j.appender.InMem.Capacity=1000</code>
     * </p>
     * <p>
     * Defaults to 1000 if not set
     * </p>
     * 
     * @param capacity
     *            the number of {@link LoggingEvent}s to hold in the buffer.
     */
    public void setCapacity(int capacity) {
        synchronized (bufferLock) {
            if (buffer.size() > capacity) {
                throw new IllegalArgumentException("Can't set capacity to less than current buffer size");
            } else {
                this.capacity = capacity;
            }
        }
    }

    /**
     * The name of the logger that this appender will delegate to when
     * <code>dump</code> is called.
     * <p>
     * Usually configured by the log4j framework: eg.
     * log4j.appender.InMem.DelegateLoggerName=myLogger
     * </p>
     * <p>
     * Defaults to "basho.WireSink", if not set.
     * </p>
     * 
     * @param delegateLoggerName
     *            the name of the logger to flush the buffer to when
     *            <code>dump</code> is called.
     */
    public synchronized void setDelegateName(String delegateLoggerName) {
        this.delegateLoggerName = delegateLoggerName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.Appender#close()
     */
    @Override public void close() {
        synchronized (bufferLock) {
            buffer.clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    @Override public boolean requiresLayout() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent
     * )
     */
    @Override protected void append(LoggingEvent loggingEvent) {
        synchronized (bufferLock) {
            if (buffer.size() == capacity) {
                buffer.poll();
            }
            buffer.offer(loggingEvent);
        }
    }

    /**
     * Flushes the buffer to the {@link Logger} named
     * <code>delegateLoggerName</code>
     */
    public void dump() {
        Logger delegate = Logger.getLogger(delegateLoggerName);
        // copy buffer contents and dump
        Collection<LoggingEvent> sink;

        synchronized (bufferLock) {
            sink = new LinkedList<LoggingEvent>(buffer);
            buffer.clear();
        }

        for (LoggingEvent e : sink) {
            delegate.callAppenders(e);
        }
    }

}
