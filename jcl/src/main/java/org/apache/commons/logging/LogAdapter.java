package org.apache.commons.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import java.io.Serializable;
import java.util.logging.LogRecord;

final class LogAdapter {

    private static final String LOG4J_SPI = "org.apache.logging.log4j.spi.ExtendedLogger";

    private static final String LOG4J_SLF4J_PROVIDER = "org.apache.logging.slf4j.SLF4JProvider";

    private static final String SLF4J_SPI = "org.slf4j.spi.LocationAwareLogger";

    private static final String SLF4J_API = "org.slf4j.Logger";

    private static final LogApi logApi;

    static {
        if (isPresent(LOG4J_SPI)) {
            if (isPresent(LOG4J_SLF4J_PROVIDER) && isPresent(SLF4J_SPI)) {
                // log4j-to-slf4j bridge -> we'll rather go with the SLF4J SPI;
                // however, we still prefer Log4j over the plain SLF4J API since
                // the latter does not have location awareness support.
                logApi = LogApi.SLF4J_LAL;
            }
            else {
                // Use Log4j 2.x directly, including location awareness support
                logApi = LogApi.LOG4J;
            }
        }
        else if (isPresent(SLF4J_SPI)) {
            // Full SLF4J SPI including location awareness support
            logApi = LogApi.SLF4J_LAL;
        }
        else if (isPresent(SLF4J_API)) {
            // Minimal SLF4J API without location awareness support
            logApi = LogApi.SLF4J;
        }
        else {
            // java.util.logging as default
            logApi = LogApi.JUL;
        }
    }

    private LogAdapter() {

    }

    static Log createLog(String name) {
        switch (logApi) {
            case LOG4J:
                return Log4jAdapter.createLog(name);
            case SLF4J_LAL:
                return Slf4jAdapter.createLocationAwareLog(name);
            case SLF4J:
                return Slf4jAdapter.createLog(name);
            default:
                // Defensively use lazy-initializing adapter class here as well since the
                // java.logging module is not present by default on JDK 9. We are requiring
                // its presence if neither Log4j nor SLF4J is available; however, in the
                // case of Log4j or SLF4J, we are trying to prevent early initialization
                // of the JavaUtilLog adapter - e.g. by a JVM in debug mode - when eagerly
                // trying to parse the bytecode for all the cases of this switch clause.
                return JavaUtilAdapter.createLog(name);
        }
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className, false, LogAdapter.class.getClassLoader());
            return true;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private enum LogApi {
        LOG4J,SLF4J_LAL,SLF4J,JUL
    }

    private static class Log4jAdapter {

        static Log createLog(String name) {
            return new Log4jLog(name);
        }

    }

    private static class Slf4jAdapter {

        static Log createLocationAwareLog(String name) {
            Logger logger = LoggerFactory.getLogger(name);
            return logger instanceof LocationAwareLogger ?
                    new Slf4jLocationAwareLog((LocationAwareLogger) logger) :
                    new Slf4jLog<>(logger);
        }

        static Log createLog(String name) {
            return new Slf4jLog<>(LoggerFactory.getLogger(name));
        }

    }

    private static class JavaUtilAdapter {

        static Log createLog(String name) {
            return new JavaUtilLog(name);
        }

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
        public void trace(Object message) {
            log(Level.TRACE,message,null);
        }

        @Override
        public void trace(Object message, Throwable t) {
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
        public void trace(Object message) {
            if (message instanceof String || this.logger.isTraceEnabled()) {
                this.logger.trace(String.valueOf(message));
            }
        }

        @Override
        public void trace(Object message, Throwable t) {
            if (message instanceof String || this.logger.isTraceEnabled()) {
                this.logger.trace(String.valueOf(message),t);
            }
        }

        protected Object readResolve() {
            return Slf4jAdapter.createLog(this.name);
        }
    }

    private static class Slf4jLocationAwareLog extends Slf4jLog<LocationAwareLogger> implements Serializable {

        private static final String FQCN = Slf4jLocationAwareLog.class.getName();

        Slf4jLocationAwareLog(LocationAwareLogger logger) {
            super(logger);
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
                this.logger.log(null,FQCN,LocationAwareLogger.ERROR_INT,String.valueOf(message)
                        ,null,null);
            }
        }

        @Override
        public void error(Object message, Throwable t) {
            if (message instanceof String || this.logger.isErrorEnabled()) {
                this.logger.log(null,FQCN,LocationAwareLogger.ERROR_INT,String.valueOf(message),
                        null,null);
            }
        }
        @Override
        public void warn(Object message) {
            if (message instanceof String || this.logger.isWarnEnabled()) {
                this.logger.log(null, FQCN, LocationAwareLogger.WARN_INT, String.valueOf(message), null, null);
            }
        }

        @Override
        public void warn(Object message, Throwable exception) {
            if (message instanceof String || this.logger.isWarnEnabled()) {
                this.logger.log(null, FQCN, LocationAwareLogger.WARN_INT, String.valueOf(message), null, exception);
            }
        }

        @Override
        public void info(Object message) {
            if (message instanceof String || this.logger.isInfoEnabled()) {
                this.logger.log(null, FQCN, LocationAwareLogger.INFO_INT, String.valueOf(message), null, null);
            }
        }

        @Override
        public void info(Object message, Throwable exception) {
            if (message instanceof String || this.logger.isInfoEnabled()) {
                this.logger.log(null, FQCN, LocationAwareLogger.INFO_INT, String.valueOf(message), null, exception);
            }
        }

        @Override
        public void debug(Object message) {
            if (message instanceof String || this.logger.isDebugEnabled()) {
                this.logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, String.valueOf(message), null, null);
            }
        }

        @Override
        public void debug(Object message, Throwable exception) {
            if (message instanceof String || this.logger.isDebugEnabled()) {
                this.logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, String.valueOf(message), null, exception);
            }
        }

        @Override
        public void trace(Object message) {
            if (message instanceof String || this.logger.isTraceEnabled()) {
                this.logger.log(null, FQCN, LocationAwareLogger.TRACE_INT, String.valueOf(message), null, null);
            }
        }

        @Override
        public void trace(Object message, Throwable exception) {
            if (message instanceof String || this.logger.isTraceEnabled()) {
                this.logger.log(null, FQCN, LocationAwareLogger.TRACE_INT, String.valueOf(message), null, exception);
            }
        }

        @Override
        protected Object readResolve() {
            return Slf4jAdapter.createLocationAwareLog(this.name);
        }
    }

    private static class JavaUtilLog implements Log,Serializable {

        private String name;

        private transient java.util.logging.Logger logger;

        JavaUtilLog(String name) {
            this.name = name;
            this.logger = java.util.logging.Logger.getLogger(name);
        }

        @Override
        public boolean isFatalEnabled() {
            return isErrorEnabled();
        }

        @Override
        public boolean isErrorEnabled() {
            return this.logger.isLoggable(java.util.logging.Level.SEVERE);
        }

        @Override
        public boolean isWarnEnabled() {
            return this.logger.isLoggable(java.util.logging.Level.WARNING);
        }

        @Override
        public boolean isInfoEnabled() {
            return this.logger.isLoggable(java.util.logging.Level.INFO);
        }

        @Override
        public boolean isDebugEnabled() {
            return this.logger.isLoggable(java.util.logging.Level.FINE);
        }

        @Override
        public boolean isTraceEnabled() {
            return this.logger.isLoggable(java.util.logging.Level.FINEST);
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
            log(java.util.logging.Level.SEVERE,message,null);
        }

        @Override
        public void error(Object message, Throwable t) {
            log(java.util.logging.Level.SEVERE,message,t);
        }

        @Override
        public void warn(Object message) {
            log(java.util.logging.Level.WARNING,message,null);
        }

        @Override
        public void warn(Object message, Throwable t) {
            log(java.util.logging.Level.WARNING,message,t);
        }

        @Override
        public void info(Object message) {
            log(java.util.logging.Level.INFO,message,null);
        }

        @Override
        public void info(Object message, Throwable t) {
            log(java.util.logging.Level.INFO,message,t);
        }

        @Override
        public void debug(Object message) {
            log(java.util.logging.Level.FINE,message,null);
        }

        @Override
        public void debug(Object message, Throwable t) {
            log(java.util.logging.Level.FINE,message,t);
        }

        @Override
        public void trace(Object message) {
            log(java.util.logging.Level.FINEST,message,null);
        }

        @Override
        public void trace(Object message, Throwable t) {
            log(java.util.logging.Level.FINEST,message,t);
        }

        private void log(java.util.logging.Level level,Object message,Throwable t) {
            if (this.logger.isLoggable(level)) {
                LogRecord rec;
                if (message instanceof LogRecord) {
                    rec = (LogRecord)message;
                } else {
                    rec = new LocationResolvingLogRecord(level, String.valueOf(message));
                    rec.setLoggerName(this.name);
                    rec.setResourceBundleName(logger.getResourceBundleName());
                    rec.setResourceBundle(logger.getResourceBundle());
                    rec.setThrown(t);
                }
                logger.log(rec);
            }
        }
    }

    private static class LocationResolvingLogRecord extends LogRecord {

        private static final String FQCN = JavaUtilLog.class.getName();

        private volatile boolean resolved;

        LocationResolvingLogRecord(java.util.logging.Level level,String msg) {
            super(level,msg);
        }

        @Override
        public String getSourceClassName() {
            if (!this.resolved) {
                resolve();
            }
            return super.getSourceClassName();
        }

        @Override
        public void setSourceClassName(String sourceClassName) {
            super.setSourceClassName(sourceClassName);
            this.resolved = true;
        }

        @Override
        public String getSourceMethodName() {
            if (!this.resolved) {
                resolve();
            }
            return super.getSourceMethodName();
        }

        @Override
        public void setSourceMethodName(String sourceMethodName) {
            super.setSourceMethodName(sourceMethodName);
            this.resolved = true;
        }

        private void resolve() {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            String sourceClassName = null;
            String sourceMethodName = null;
            boolean found = false;
            for (StackTraceElement element : stack) {
                String className = element.getClassName();
                if (FQCN.equals(className)) {
                    found = true;
                }
                else if (found) {
                    sourceClassName = className;
                    sourceMethodName = element.getMethodName();
                    break;
                }
            }
            setSourceClassName(sourceClassName);
            setSourceMethodName(sourceMethodName);
        }

        @SuppressWarnings("deprecation")
        protected Object writeReplace() {
            LogRecord serialized = new LogRecord(getLevel(), getMessage());
            serialized.setLoggerName(getLoggerName());
            serialized.setResourceBundle(getResourceBundle());
            serialized.setResourceBundleName(getResourceBundleName());
            serialized.setSourceClassName(getSourceClassName());
            serialized.setSourceMethodName(getSourceMethodName());
            serialized.setSequenceNumber(getSequenceNumber());
            serialized.setParameters(getParameters());
            serialized.setThreadID(getThreadID());
            serialized.setMillis(getMillis());
            serialized.setThrown(getThrown());
            return serialized;
        }
    }

}
