package cores;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.model.Media;
import logConfig.Log4j2Manager;
import org.openqa.selenium.InvalidSelectorException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import pages.*;
import reportConfig.ExtentManager;

import java.lang.reflect.Method;
import java.util.Random;

public class BaseTest {

    //Test instances
    protected WebsiteDriver webDriver;
    protected HomePage homepage;
    protected ProductsPage productPage;
    protected ProductDetailsPage productDetailsPage;
    protected CartPage cartPage;
    protected StoresLocationPage storesLocationPage;
    protected FAQPage faqPage;
    protected PaymentPage paymentPage;

    //Log instances
    protected static Log4j2Manager log4j2Manager;
    protected static ExtentManager extentManager;

    //Thread instances
    protected static final ThreadLocal<WebsiteDriver> webdriverThread = new ThreadLocal<>();

    //Driver method ***********************************************************
    public WebsiteDriver getWebDriver(Browser browser) {
        webDriver = DriverFactory.initWebsiteDriver(browser);
        webdriverThread.set(webDriver);
        return webdriverThread.get();
    }

    //Setup method ***********************************************************
    @BeforeSuite
    public void setupReport() {
        extentManager = ExtentManager.getInstance();
    }

    @AfterSuite(alwaysRun = true)
    void afterSuite() {
        logInfo("- Clean background process (driver)");
        cleanDriverProcess();
        webdriverThread.remove();
    }

    protected void navigateToHomePage() {
        sleepInSecond(1);
        if (webDriver.getPageTitle().startsWith("Hasaki.vn")) webDriver.click("div.logo_site");
        else webDriver.waitToBeClickable("a[aria-label='Homepage']").click();
    }

    /**
     * Default, User is navigated back in Homepage before log out.
     */
    protected void logout() {
        if (webDriver.isUnDisplayed("#btn-login")) {
            try {
                webDriver.moveToElement("div.item_header.item_login.user_login");
            } catch (InvalidSelectorException e) {
                webDriver.moveToElement("//a[text()='Tài khoản ']");
            }

            webDriver.findElement("//a[contains(text(),'Thoát')]").click();
        }
    }

    protected void quitBrowser() {
        if (webDriver != null) webDriver.quit();
    }

    protected void cleanDriverProcess() {
        webDriver.killDriverProcess();
    }

    protected void switchToMainWebsite() {
        webDriver.switchWindowByTitle("Hasaki.vn | Mỹ Phẩm & Clinic");
    }

    //Logging methods ***********************************************************
    protected void createLog(String suiteName) {
        log4j2Manager = Log4j2Manager.getLogger(suiteName);
        extentManager.createExtentTestSuite(suiteName);
    }

    protected void createLog(Class<?> clazz) {
        log4j2Manager = Log4j2Manager.getLogger(clazz);
        extentManager.createExtentTestSuite(clazz.getName());
    }

    protected void createTestCase(Method method) {
        var className = this.getClass().getName();
        Test testAnnotation = method.getAnnotation(Test.class);

        // Check Test annotation if it is existed
        if (testAnnotation != null) extentManager.createExtentTestCase(className, getTestName(method));
    }

    protected String getTestName(Method method){
        return method.getName().replace("_", " ");
    }

    /**
     * Write log at Suite level
     *
     * @param description
     */
    protected void logInfo(String description) {
        var className = this.getClass().getName();

        extentManager.getExtentTestSuiteMap().get(className).info(MarkupHelper.createLabel(description, ExtentColor.GREY));
        log4j2Manager.getInfoLogger(className).info(description);
    }

    /**
     * Write log at Test Case level
     * @param method
     * @param description
     */
    protected void logInfo(Method method, String description) {
        var className = this.getClass().getName();
        var testCase = extentManager.getExtentTestCaseMap().get(getTestName(method));

        testCase.info(MarkupHelper.createLabel(description, ExtentColor.GREY));
        log4j2Manager.getInfoLogger(className).info(description);
    }

    /**
     * Write log at Suite level
     *
     * @param description
     */
    protected void logInfo(String description, boolean enableCapture) {
        var className = this.getClass().getName();

        if (enableCapture)
            extentManager.getExtentTestSuiteMap().get(className).log(Status.INFO, MarkupHelper.createLabel(description, ExtentColor.TEAL), attachScreenshot());
        log4j2Manager.getInfoLogger(className).info(description);
    }

    /**
     * Write log at Test Case level
     *
     * @param testCaseName
     * @param description
     */
    protected void logInfo(String testCaseName, String description, boolean enableCapture) {
        var className = this.getClass().getName();
        var testCase = extentManager.getExtentTestCaseMap().get(testCaseName);

        if (enableCapture)
            testCase.log(Status.INFO, MarkupHelper.createLabel(description, ExtentColor.TEAL), attachScreenshot());
        log4j2Manager.getInfoLogger(className).info(description);
    }

    /**
     * Write log at Suite level
     *
     * @param description
     */
    protected void logInfo(String description, ExtentColor logColor) {
        var className = this.getClass().getName();

        extentManager.getExtentTestSuiteMap().get(className).info(MarkupHelper.createLabel(description, logColor));
        log4j2Manager.getInfoLogger(className).info(description);
    }

    /**
     * Write log at Test Case level
     *
     * @param method
     * @param description
     */
    protected void logInfo(Method method, String description, ExtentColor logColor) {
        var className = this.getClass().getName();
        var testCase = extentManager.getExtentTestCaseMap().get(getTestName(method));

        testCase.info(MarkupHelper.createLabel(description, logColor));
        log4j2Manager.getInfoLogger(className).info(description);
    }

    protected Media attachScreenshot() {
        var className = this.getClass().getName();
        var mediaList = extentManager.getExtentTestSuiteMap().get(className).getModel().getMedia();

        extentManager.getExtentTestSuiteMap().get(className).addScreenCaptureFromBase64String(webdriverThread.get().takeScreenshotBASE64());

        return mediaList.get(extentManager.getExtentTestSuiteMap().get(className).getModel().getMedia().size() - 1);
    }

    protected Media attachScreenshot(String testCaseName) {
        var className = this.getClass().getName();
        var testCase = extentManager.getExtentTestCaseMap().get(testCaseName);
        var mediaList = testCase.getModel().getMedia();

        testCase.addScreenCaptureFromBase64String(webdriverThread.get().takeScreenshotBASE64());

        return mediaList.get(testCase.getModel().getMedia().size() - 1);
    }

    //Util methods ***********************************************************
    protected static void sleepInSecond(long time) {
        try {
            Thread.sleep(time * 1000);
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
            CustomAssert.assertTrue(condition);
            log4j2Manager.getAssertionPassLogger(className).info("{} ====> PASS", message);
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("Assert True is FAILED: {}", e.getMessage());
            throw e;
        }

    }

    protected void assertTrue(boolean condition) {
        var className = this.getClass().getName();

        try {
            CustomAssert.assertTrue(condition);
            log4j2Manager.getAssertionPassLogger(className).info("Assert True is PASS");
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("Assert True is FAILED: {}", e.getMessage());
            throw e;
        }

    }

    protected void assertFalse(boolean condition, String message) {
        var className = this.getClass().getName();

        try {
            CustomAssert.assertFalse(condition);
            log4j2Manager.getAssertionPassLogger(className).info("{} ====> PASS", message);
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("Assert False is FAILED: {}", e.getMessage());
            throw e;
        }
    }

    protected void assertFalse(boolean condition) {
        var className = this.getClass().getName();

        try {
            CustomAssert.assertFalse(condition);
            log4j2Manager.getAssertionPassLogger(className).info("Assert False is PASS");
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("Assert False is FAILED: {}", e.getMessage());
            throw e;
        }
    }

    protected void assertEquals(Object actual, Object expected, String message) {
        var className = this.getClass().getName();

        try {
            CustomAssert.assertEquals(actual, expected);
            log4j2Manager.getAssertionPassLogger(className).info("{}: [Actual: {}] and [Expected: {}] ====> PASS", message, actual, expected);
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("[Actual: {}] [but Expected: {}]", actual, expected);
            throw e;
        }

    }

    protected void assertEquals(Object actual, Object expected) {
        var className = this.getClass().getName();

        try {
            CustomAssert.assertEquals(actual, expected);
            log4j2Manager.getAssertionPassLogger(className).info("[Actual: {}] and [Expected: {}] ====> PASS", actual, expected);
        } catch (Throwable e) {
            log4j2Manager.getAssertionFailLogger(className).error("[Actual: {}] but [Expected: {}]", actual, expected);
            throw e;
        }

    }

    //Simple verify
    protected boolean verifyTrue(boolean condition) {
        return new CustomAssert(GlobalVariables.HASAKI_KEYWORD).verifyTrue(condition);
    }

    protected boolean verifyTrue(boolean condition, String message) {
        return new CustomAssert(GlobalVariables.HASAKI_KEYWORD).verifyTrue(condition, message);
    }

    protected boolean verifyFalse(boolean condition) {
        return new CustomAssert(GlobalVariables.HASAKI_KEYWORD).verifyFalse(condition);
    }

    protected boolean verifyFalse(boolean condition, String message) {
        return new CustomAssert(GlobalVariables.HASAKI_KEYWORD).verifyFalse(condition, message);
    }

    protected void verifyEquals(Object actual, Object expected, String message) {
        new CustomAssert(GlobalVariables.HASAKI_KEYWORD).verifyEquals(actual, expected, message);
    }

    protected void verifyEquals(Object actual, Object expected) {
        new CustomAssert(GlobalVariables.HASAKI_KEYWORD).verifyEquals(actual, expected);
    }


}
