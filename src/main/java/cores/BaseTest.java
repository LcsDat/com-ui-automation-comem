package cores;

import infrastructure.logs.Log4j2Manager;
import io.qameta.allure.Allure;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import pages.*;
import utilities.CommonUtils;
import utilities.ScreenRecorder;

import java.lang.reflect.Method;
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

    //Recording instance
    protected ScreenRecorder recorder = new ScreenRecorder();

    //Thread instances
    public static final ThreadLocal<BrowserDriver> driverThread = new ThreadLocal<>();

    //Driver method ***********************************************************
    public BrowserDriver initDriver(Browser browser) {
        webDriver = new BrowserDriver(browser);
        driverThread.set(webDriver);
        return driverThread.get();
    }

    protected void startRecording(Method method) {
        recorder.start(method.getName());
    }

    protected void stopRecording() {
        recorder.stop();
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
        CommonUtils.pause(seconds);
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
    private void doAssert(Runnable assertion, String type, String message) {
        var className = this.getClass().getName();
        try {
            assertion.run();
            log4j2Manager.getAssertionPassLogger(className)
                    .info("[{}] {} — PASS", type, message);
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className)
                    .error("[{}] {} — FAILED: {}", type, message, e.getMessage());
            throw e;
        }
    }

    protected void assertTrue(boolean condition, String message) {
        doAssert(() -> LoggingAssert.assertTrue(condition), "assertTrue", message);
    }

    protected void assertFalse(boolean condition, String message) {
        doAssert(() -> LoggingAssert.assertFalse(condition), "assertFalse", message);
    }

    protected void assertEquals(Object actual, Object expected, String message) {
        doAssert(() -> LoggingAssert.assertEquals(actual, expected),
                "assertEquals",
                message + " | Actual: \"" + actual + "\" — Expected: \"" + expected + "\"");
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