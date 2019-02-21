package org.apache.commons.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.slf4j.Logger;

import java.io.Serializable;

final class LogAdapter {

    private static final String LOG4J_SPI = "org.apache.logging.log4j.spi.ExtendedLogger";

    private static final String LOG4J_SLF4J_PROVIDER = "org.apache.logging.slf4j.SLF4JProvider";

    private static final String SLF4J_SPI = "org.slf4j.spi.LocationAwareLogger";

    private static final String SLF4J_API = "org.slf4j.Logger";

    private enum LogApi {
        LOG4J,SLF4J_LAL,SLF4J,JUL
    }

    private static class Log4jLog implements Log, Serializable {

        private static final String FQCN = Log4jLog.class.getName();

        private static final LoggerContext loggerContext =
                LogManager.getContext(Log4jLog.class.getClassLoader(),false);

        private final ExtendedLogger logger;

        Log4jLog(String name) {
            this.logger = loggerContext.getLogger(name);
        }

        @Override
        public boolean isFatalEnabled() {
            return this.logger.isEnabled(Level.FATAL);
        }

        @Override
        public boolean isErrorEnabled() {
            return this.logger.isEnabled(Level.ERROR);
        }

        @Override
        public boolean isWarnEnabled() {
            return this.logger.isEnabled(Level.WARN);
        }

        @Override
        public boolean isInfoEnabled() {
            return this.logger.isEnabled(Level.INFO);
        }

        @Override
        public boolean isDebugEnabled() {
            return this.logger.isEnabled(Level.DEBUG);
        }

        @Override
        public boolean isTraceEnabled() {
            return this.logger.isEnabled(Level.TRACE);
        }

        @Override
        public void fatal(Object message) {
            log(Level.FATAL,message,null);
        }

        @Override
        public void fatal(Object message, Throwable t) {
            log(Level.FATAL,message,t);
        }

        @Override
        public void error(Object message) {
            log(Level.ERROR,message,null);
        }

        @Override
        public void error(Object message, Throwable t) {
            log(Level.ERROR,message,t);
        }

        @Override
        public void warn(Object message) {
            log(Level.WARN,message,null);
        }

        @Override
        public void warn(Object message, Throwable t) {
            log(Level.WARN,message,t);
        }

        @Override
        public void info(Object message) {
            log(Level.INFO,message,null);
        }

        @Override
        public void info(Object message, Throwable t) {
            log(Level.INFO,message,t);
        }

        @Override
        public void debug(Object message) {
            log(Level.DEBUG,message,null);
        }

        @Override
        public void debug(Object message, Throwable t) {
            log(Level.DEBUG,message,t);
        }

        @Override
        public void trance(Object message) {
            log(Level.TRACE,message,null);
        }

        @Override
        public void trance(Object message, Throwable t) {
            log(Level.TRACE,message,t);
        }

        private void log(Level level,Object message, Throwable t) {
            if (message instanceof String) {
                if (t != null) {
                    this.logger.logIfEnabled(FQCN,level,null,(String)message,t);
                } else {
                    this.logger.logIfEnabled(FQCN,level,null,(String)message);
                }
            } else {
                this.logger.logIfEnabled(FQCN,level,null,message,t);
            }
        }
    }

    private static class Slf4jLog<T extends Logger> implements Log,Serializable {

        protected final String name;

        protected transient T logger;

        Slf4jLog(T logger) {
            this.name = logger.getName();
            this.logger = logger;
        }

        @Override
        public boolean isFatalEnabled() {
            return isErrorEnabled();
        }

        @Override
        public boolean isErrorEnabled() {
            return this.logger.isErrorEnabled();
        }

        @Override
        public boolean isWarnEnabled() {
            return this.logger.isWarnEnabled();
        }

        @Override
        public boolean isInfoEnabled() {
            return this.logger.isInfoEnabled();
        }

        @Override
        public boolean isDebugEnabled() {
            return this.logger.isDebugEnabled();
        }

        @Override
        public boolean isTraceEnabled() {
            return this.logger.isTraceEnabled();
        }

        @Override
        public void fatal(Object message) {
            error(message);
        }

        @Override
        public void fatal(Object message, Throwable t) {
            error(message,t);
        }

        @Override
        public void error(Object message) {
            if (message instanceof String || this.logger.isErrorEnabled()) {
                this.logger.error(String.valueOf(message));
            }
        }

        @Override
        public void error(Object message, Throwable t) {
            if (message instanceof String || this.logger.isErrorEnabled()) {
                this.logger.error(String.valueOf(message),t);
            }
        }

        @Override
        public void warn(Object message) {
            if (message instanceof String || this.logger.isWarnEnabled()) {
                this.logger.warn(String.valueOf(message));
            }
        }

        @Override
        public void warn(Object message, Throwable t) {
            if (message instanceof String || this.logger.isWarnEnabled()) {
                this.logger.warn(String.valueOf(message),t);
            }
        }

        @Override
        public void info(Object message) {
            if (message instanceof String || this.logger.isInfoEnabled()) {
                this.logger.info(String.valueOf(message));
            }
        }

        @Override
        public void info(Object message, Throwable t) {
            if (message instanceof String || this.logger.isInfoEnabled()) {
                this.logger.info(String.valueOf(message),t);
            }
        }

        @Override
        public void debug(Object message) {
            if (message instanceof String || this.logger.isDebugEnabled()) {
                this.logger.debug(String.valueOf(message));
            }
        }

        @Override
        public void debug(Object message, Throwable t) {
            if (message instanceof String || this.logger.isDebugEnabled()) {
                this.logger.debug(String.valueOf(message),t);
            }
        }

        @Override
        public void trance(Object message) {
            if (message instanceof String || this.logger.isTraceEnabled()) {
                this.logger.trace(String.valueOf(message));
            }
        }

        @Override
        public void trance(Object message, Throwable t) {
            if (message instanceof String || this.logger.isTraceEnabled()) {
                this.logger.trace(String.valueOf(message),t);
            }
        }

        protected Object readResolve() {
            return null;//TODO
        }
    }

}
