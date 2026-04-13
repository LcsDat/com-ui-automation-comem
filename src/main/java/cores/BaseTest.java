package cores;

import infrastructure.logs.Log4j2Manager;
import io.qameta.allure.Allure;
import org.openqa.selenium.InvalidSelectorException;
import org.testng.annotations.AfterSuite;
import pages.*;

import java.util.Random;

public class BaseTest {

    //Test instances
    protected BrowserDriver webDriver;
    protected HomePage homepage;
    protected ProductsPage productPage;
    protected ProductDetailsPage productDetailsPage;
    protected CartPage cartPage;
    protected StoresLocationPage storesLocationPage;
    protected FAQPage faqPage;
    protected PaymentPage paymentPage;

    //Log instances
    protected static Log4j2Manager log4j2Manager;

    //Thread instances
    public static final ThreadLocal<BrowserDriver> driverThread = new ThreadLocal<>();

    //Driver method ***********************************************************
    public BrowserDriver initDriver(Browser browser) {
        webDriver = new BrowserDriver(browser);
        driverThread.set(webDriver);
        return driverThread.get();
    }

    @AfterSuite(alwaysRun = true)
    void afterSuite() {
        webDriver.killDriverProcess();
        driverThread.remove();
    }

    //Logging methods ***********************************************************
    protected void initLogger(Class<?> clazz) {
        log4j2Manager = Log4j2Manager.getLogger(clazz);
    }

    protected void logInfo(String description, Allure.ThrowableRunnableVoid runnable) {
        var className = this.getClass().getName();
        log4j2Manager.getInfoLogger(className).info(description);
        Allure.step(description, runnable);
    }

    //Util methods ***********************************************************
    protected static void pause(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected String randomAlphabetic(int targetLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'

        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    //Assertion methods ***********************************************************
    protected void assertTrue(boolean condition, String message) {
        var className = this.getClass().getName();

        try {
            LoggingAssert.assertTrue(condition);
            log4j2Manager.getAssertionPassLogger(className).info("{} ====> PASS", message);
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("Assert True is FAILED: {}", e.getMessage());
            throw e;
        }
    }

    protected void assertTrue(boolean condition) {
        var className = this.getClass().getName();

        try {
            LoggingAssert.assertTrue(condition);
            log4j2Manager.getAssertionPassLogger(className).info("Assert True is PASS");
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("Assert True is FAILED: {}", e.getMessage());
            throw e;
        }
    }

    protected void assertFalse(boolean condition, String message) {
        var className = this.getClass().getName();

        try {
            LoggingAssert.assertFalse(condition);
            log4j2Manager.getAssertionPassLogger(className).info("{} ====> PASS", message);
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("Assert False is FAILED: {}", e.getMessage());
            throw e;
        }
    }

    protected void assertFalse(boolean condition) {
        var className = this.getClass().getName();

        try {
            LoggingAssert.assertFalse(condition);
            log4j2Manager.getAssertionPassLogger(className).info("Assert False is PASS");
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("Assert False is FAILED: {}", e.getMessage());
            throw e;
        }
    }

    protected void assertEquals(Object actual, Object expected, String message) {
        var className = this.getClass().getName();

        try {
            LoggingAssert.assertEquals(actual, expected);
            log4j2Manager.getAssertionPassLogger(className).info("{}: [Actual: {}] and [Expected: {}] ====> PASS", message, actual, expected);
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("[Actual: {}] [but Expected: {}]", actual, expected);
            throw e;
        }
    }

    protected void assertEquals(Object actual, Object expected) {
        var className = this.getClass().getName();

        try {
            LoggingAssert.assertEquals(actual, expected);
            log4j2Manager.getAssertionPassLogger(className).info("[Actual: {}] and [Expected: {}] ====> PASS", actual, expected);
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("[Actual: {}] but [Expected: {}]", actual, expected);
            throw e;
        }
    }

    //Simple verify
    protected boolean verifyTrue(boolean condition) {
        return new LoggingAssert(Constants.COMEM_KEYWORD).verifyTrue(condition);
    }

    protected boolean verifyTrue(boolean condition, String message) {
        return new LoggingAssert(Constants.COMEM_KEYWORD).verifyTrue(condition, message);
    }

    protected boolean verifyFalse(boolean condition) {
        return new LoggingAssert(Constants.COMEM_KEYWORD).verifyFalse(condition);
    }

    protected boolean verifyFalse(boolean condition, String message) {
        return new LoggingAssert(Constants.COMEM_KEYWORD).verifyFalse(condition, message);
    }

    protected void verifyEquals(Object actual, Object expected, String message) {
        new LoggingAssert(Constants.COMEM_KEYWORD).verifyEquals(actual, expected, message);
    }

    protected void verifyEquals(Object actual, Object expected) {
        new LoggingAssert(Constants.COMEM_KEYWORD).verifyEquals(actual, expected);
    }
}