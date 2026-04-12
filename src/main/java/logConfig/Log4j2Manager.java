package logConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.concurrent.ConcurrentHashMap;

public class Log4j2Manager {
    private static Log4j2Manager log4jManager;
    private Logger infoLogger;
    private Logger assertionPassLogger;
    private Logger assertionFailLogger;

    private static final ConcurrentHashMap<String, Logger> infoMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Logger> assertPassMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Logger> assertFailMap = new ConcurrentHashMap<>();

    private void waitForLoggerStarted() {
        // Force Log4j2 to initialize completely
        LoggerContext context = (LoggerContext) LogManager.getContext(false);

        // Wait for initialization to complete
        if (!context.isStarted()) {
            // Force re-initialization
            context.reconfigure();

            // Wait for configuration to be applied
            int attempts = 0;
            while (!context.isStarted() && attempts < 10) {
                try {
                    Thread.sleep(100);
                    attempts++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private Log4j2Manager(Class<?> clazz) {
        infoLogger = LogManager.getLogger(clazz);
        assertionPassLogger = LogManager.getLogger("assertionsPass." + clazz.getSimpleName());
        assertionFailLogger = LogManager.getLogger("assertionsFail." + clazz.getSimpleName());

        waitForLoggerStarted();

        infoMap.put(clazz.getName(), infoLogger);
        assertPassMap.put(clazz.getName(), assertionPassLogger);
        assertFailMap.put(clazz.getName(), assertionFailLogger);
    }

    private Log4j2Manager(Object object) {
        infoLogger = LogManager.getLogger(object);
        assertionPassLogger = LogManager.getLogger("assertionsPass." + object.getClass().getSimpleName());
        assertionFailLogger = LogManager.getLogger("assertionsFail." + object.getClass().getSimpleName());

        waitForLoggerStarted();

        infoMap.put(object.getClass().getName(), infoLogger);
        assertPassMap.put(object.getClass().getName(), assertionPassLogger);
        assertFailMap.put(object.getClass().getName(), assertionFailLogger);
    }

    public Logger getAssertionFailLogger() {
        return assertionFailLogger;
    }

    public Logger getAssertionFailLogger(String className) {
        return assertFailMap.get(className);
    }

    public Logger getInfoLogger() {
        return infoLogger;
    }

    public Logger getInfoLogger(String className) {
        return infoMap.get(className);
    }

    public Logger getAssertionPassLogger() {
        return assertionPassLogger;
    }

    public Logger getAssertionPassLogger(String className) {
        return assertPassMap.get(className);
    }


    public static Log4j2Manager getLogger(Class<?> clazz) {
            log4jManager = new Log4j2Manager(clazz);
        return log4jManager;
    }


    public static Log4j2Manager getLogger(Object object) {
            log4jManager = new Log4j2Manager(object);
        return log4jManager;
    }

    public void logInfo(String message, Object... params) {
        getInfoLogger().info(message, params);
    }

    public void logAssertionPass(String message, Object... params) {
        getAssertionPassLogger().info(message);
    }

    public void logAssertionFail(String message, Object... params) {
        getAssertionFailLogger().error(message);
    }


}
