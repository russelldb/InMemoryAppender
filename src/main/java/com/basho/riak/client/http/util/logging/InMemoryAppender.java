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

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * An in memory log4j appender that collects log statements in a circular
 * buffer.
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

    private static final Logger delegate = Logger.getLogger("basho.WireSink");

    private int capacity = 1000;
    private LinkedBlockingQueue<LoggingEvent> buffer = new LinkedBlockingQueue<LoggingEvent>(capacity);

    public synchronized void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.Appender#close()
     */
    @Override public void close() {
        buffer.clear();
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
        synchronized (this) {
            if (buffer.size() == capacity) {
                // pop one off first
                buffer.poll();
            }
        }
        buffer.offer(loggingEvent);
    }

    /**
     * call to flush the in memory buffer to the delegate appender.
     */
    public void dump() {
        // copy buffer contents and dump
        Collection<LoggingEvent> sink = new LinkedList<LoggingEvent>();
        buffer.drainTo(sink);

        for (LoggingEvent e : sink) {
            delegate.callAppenders(e);
        }
    }

}
