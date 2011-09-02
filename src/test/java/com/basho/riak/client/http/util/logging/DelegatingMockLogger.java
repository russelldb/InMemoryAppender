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

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

public final class DelegatingMockLogger extends Logger {

    private final Logger mockDelegate;

    /**
     * @param name
     */
    protected DelegatingMockLogger(String name, Logger mockDelegate) {
        super(name);
        this.mockDelegate = mockDelegate;
    }

    /**
     * @param newAppender
     * @see org.apache.log4j.Category#addAppender(org.apache.log4j.Appender)
     */
    public void addAppender(Appender newAppender) {
        mockDelegate.addAppender(newAppender);
    }

    /**
     * @param assertion
     * @param msg
     * @see org.apache.log4j.Category#assertLog(boolean, java.lang.String)
     */
    public void assertLog(boolean assertion, String msg) {
        mockDelegate.assertLog(assertion, msg);
    }

    /**
     * @return
     * @see org.apache.log4j.Logger#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return mockDelegate.isTraceEnabled();
    }

    /**
     * @param event
     * @see org.apache.log4j.Category#callAppenders(org.apache.log4j.spi.LoggingEvent)
     */
    public void callAppenders(LoggingEvent event) {
        mockDelegate.callAppenders(event);
    }

    /**
     * @param message
     * @see org.apache.log4j.Category#debug(java.lang.Object)
     */
    public void debug(Object message) {
        mockDelegate.debug(message);
    }

    /**
     * @param message
     * @param t
     * @see org.apache.log4j.Category#debug(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void debug(Object message, Throwable t) {
        mockDelegate.debug(message, t);
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return mockDelegate.equals(obj);
    }

    /**
     * @param message
     * @see org.apache.log4j.Category#error(java.lang.Object)
     */
    public void error(Object message) {
        mockDelegate.error(message);
    }

    /**
     * @param message
     * @param t
     * @see org.apache.log4j.Category#error(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void error(Object message, Throwable t) {
        mockDelegate.error(message, t);
    }

    /**
     * @param message
     * @see org.apache.log4j.Category#fatal(java.lang.Object)
     */
    public void fatal(Object message) {
        mockDelegate.fatal(message);
    }

    /**
     * @param message
     * @param t
     * @see org.apache.log4j.Category#fatal(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void fatal(Object message, Throwable t) {
        mockDelegate.fatal(message, t);
    }

    /**
     * @return
     * @see org.apache.log4j.Category#getAdditivity()
     */
    public boolean getAdditivity() {
        return mockDelegate.getAdditivity();
    }

    /**
     * @return
     * @see org.apache.log4j.Category#getAllAppenders()
     */
    @SuppressWarnings("rawtypes") public Enumeration getAllAppenders() {
        return mockDelegate.getAllAppenders();
    }

    /**
     * @param name
     * @return
     * @see org.apache.log4j.Category#getAppender(java.lang.String)
     */
    public Appender getAppender(String name) {
        return mockDelegate.getAppender(name);
    }

    /**
     * @return
     * @see org.apache.log4j.Category#getEffectiveLevel()
     */
    public Level getEffectiveLevel() {
        return mockDelegate.getEffectiveLevel();
    }

    /**
     * @return
     * @deprecated
     * @see org.apache.log4j.Category#getChainedPriority()
     */
    public Priority getChainedPriority() {
        return mockDelegate.getChainedPriority();
    }

    /**
     * @return
     * @deprecated
     * @see org.apache.log4j.Category#getHierarchy()
     */
    public LoggerRepository getHierarchy() {
        return mockDelegate.getHierarchy();
    }

    /**
     * @return
     * @see org.apache.log4j.Category#getLoggerRepository()
     */
    public LoggerRepository getLoggerRepository() {
        return mockDelegate.getLoggerRepository();
    }

    /**
     * @return
     * @see org.apache.log4j.Category#getResourceBundle()
     */
    public ResourceBundle getResourceBundle() {
        return mockDelegate.getResourceBundle();
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return mockDelegate.hashCode();
    }

    /**
     * @param message
     * @see org.apache.log4j.Category#info(java.lang.Object)
     */
    public void info(Object message) {
        mockDelegate.info(message);
    }

    /**
     * @param message
     * @param t
     * @see org.apache.log4j.Category#info(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void info(Object message, Throwable t) {
        mockDelegate.info(message, t);
    }

    /**
     * @param appender
     * @return
     * @see org.apache.log4j.Category#isAttached(org.apache.log4j.Appender)
     */
    public boolean isAttached(Appender appender) {
        return mockDelegate.isAttached(appender);
    }

    /**
     * @return
     * @see org.apache.log4j.Category#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return mockDelegate.isDebugEnabled();
    }

    /**
     * @param level
     * @return
     * @see org.apache.log4j.Category#isEnabledFor(org.apache.log4j.Priority)
     */
    public boolean isEnabledFor(Priority level) {
        return mockDelegate.isEnabledFor(level);
    }

    /**
     * @return
     * @see org.apache.log4j.Category#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return mockDelegate.isInfoEnabled();
    }

    /**
     * @param priority
     * @param key
     * @param t
     * @see org.apache.log4j.Category#l7dlog(org.apache.log4j.Priority,
     *      java.lang.String, java.lang.Throwable)
     */
    public void l7dlog(Priority priority, String key, Throwable t) {
        mockDelegate.l7dlog(priority, key, t);
    }

    /**
     * @param priority
     * @param key
     * @param params
     * @param t
     * @see org.apache.log4j.Category#l7dlog(org.apache.log4j.Priority,
     *      java.lang.String, java.lang.Object[], java.lang.Throwable)
     */
    public void l7dlog(Priority priority, String key, Object[] params, Throwable t) {
        mockDelegate.l7dlog(priority, key, params, t);
    }

    /**
     * @param priority
     * @param message
     * @param t
     * @see org.apache.log4j.Category#log(org.apache.log4j.Priority,
     *      java.lang.Object, java.lang.Throwable)
     */
    public void log(Priority priority, Object message, Throwable t) {
        mockDelegate.log(priority, message, t);
    }

    /**
     * @param priority
     * @param message
     * @see org.apache.log4j.Category#log(org.apache.log4j.Priority,
     *      java.lang.Object)
     */
    public void log(Priority priority, Object message) {
        mockDelegate.log(priority, message);
    }

    /**
     * @param callerFQCN
     * @param level
     * @param message
     * @param t
     * @see org.apache.log4j.Category#log(java.lang.String,
     *      org.apache.log4j.Priority, java.lang.Object, java.lang.Throwable)
     */
    public void log(String callerFQCN, Priority level, Object message, Throwable t) {
        mockDelegate.log(callerFQCN, level, message, t);
    }

    /**
     * 
     * @see org.apache.log4j.Category#removeAllAppenders()
     */
    public void removeAllAppenders() {
        mockDelegate.removeAllAppenders();
    }

    /**
     * @param appender
     * @see org.apache.log4j.Category#removeAppender(org.apache.log4j.Appender)
     */
    public void removeAppender(Appender appender) {
        mockDelegate.removeAppender(appender);
    }

    /**
     * @param name
     * @see org.apache.log4j.Category#removeAppender(java.lang.String)
     */
    public void removeAppender(String name) {
        mockDelegate.removeAppender(name);
    }

    /**
     * @param additive
     * @see org.apache.log4j.Category#setAdditivity(boolean)
     */
    public void setAdditivity(boolean additive) {
        mockDelegate.setAdditivity(additive);
    }

    /**
     * @param level
     * @see org.apache.log4j.Category#setLevel(org.apache.log4j.Level)
     */
    public void setLevel(Level level) {
        mockDelegate.setLevel(level);
    }

    /**
     * @param priority
     * @deprecated
     * @see org.apache.log4j.Category#setPriority(org.apache.log4j.Priority)
     */
    public void setPriority(Priority priority) {
        mockDelegate.setPriority(priority);
    }

    /**
     * @param bundle
     * @see org.apache.log4j.Category#setResourceBundle(java.util.ResourceBundle)
     */
    public void setResourceBundle(ResourceBundle bundle) {
        mockDelegate.setResourceBundle(bundle);
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return mockDelegate.toString();
    }

    /**
     * @param message
     * @see org.apache.log4j.Logger#trace(java.lang.Object)
     */
    public void trace(Object message) {
        mockDelegate.trace(message);
    }

    /**
     * @param message
     * @param t
     * @see org.apache.log4j.Logger#trace(java.lang.Object, java.lang.Throwable)
     */
    public void trace(Object message, Throwable t) {
        mockDelegate.trace(message, t);
    }

    /**
     * @param message
     * @see org.apache.log4j.Category#warn(java.lang.Object)
     */
    public void warn(Object message) {
        mockDelegate.warn(message);
    }

    /**
     * @param message
     * @param t
     * @see org.apache.log4j.Category#warn(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void warn(Object message, Throwable t) {
        mockDelegate.warn(message, t);
    }
}