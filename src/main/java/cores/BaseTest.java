package cores;

import infrastructure.logs.Log4j2Manager;
import org.openqa.selenium.InvalidSelectorException;
import org.testng.annotations.AfterSuite;
import pages.*;

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

    //Thread instances
    public static final ThreadLocal<WebsiteDriver> webdriverThread = new ThreadLocal<>();

    //Driver method ***********************************************************
    public WebsiteDriver getWebDriver(Browser browser) {
        webDriver = DriverFactory.initWebsiteDriver(browser);
        webdriverThread.set(webDriver);
        return webdriverThread.get();
    }

    @AfterSuite(alwaysRun = true)
    void afterSuite() {
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
    protected void createLog(Class<?> clazz) {
        log4j2Manager = Log4j2Manager.getLogger(clazz);
    }

    protected void logInfo(String description) {
        var className = this.getClass().getName();
        log4j2Manager.getInfoLogger(className).info(description);
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
        return new CustomAssert(GlobalVariables.COMEM_KEYWORD).verifyTrue(condition);
    }

    protected boolean verifyTrue(boolean condition, String message) {
        return new CustomAssert(GlobalVariables.COMEM_KEYWORD).verifyTrue(condition, message);
    }

    protected boolean verifyFalse(boolean condition) {
        return new CustomAssert(GlobalVariables.COMEM_KEYWORD).verifyFalse(condition);
    }

    protected boolean verifyFalse(boolean condition, String message) {
        return new CustomAssert(GlobalVariables.COMEM_KEYWORD).verifyFalse(condition, message);
    }

    protected void verifyEquals(Object actual, Object expected, String message) {
        new CustomAssert(GlobalVariables.COMEM_KEYWORD).verifyEquals(actual, expected, message);
    }

    protected void verifyEquals(Object actual, Object expected) {
        new CustomAssert(GlobalVariables.COMEM_KEYWORD).verifyEquals(actual, expected);
    }
}