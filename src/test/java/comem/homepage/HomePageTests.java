package comem.homepage;

import cores.BaseTest;
import cores.Browser;
import cores.PageProvider;
import io.qameta.allure.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.ITestResult;
import pages.AboutPage;
import pages.HomePage;
import pages.StoreLocatorPage;
import utilities.CommonUtils;

import java.lang.reflect.Method;

@Feature("Homepage")
public class HomePageTests extends BaseTest {

    private HomePage homePage;

    @BeforeClass
    @Parameters("url")
    private void beforeClass(String url) {
        initLogger(HomePageTests.class);
        webDriver = initDriver(Browser.CHROME);

        webDriver.setHighlight(true);
        recorder.setEnabled(true);

        homePage = PageProvider.create(HomePage.class, webDriver);
        webDriver.navigate(url);
    }

    @BeforeMethod
    private void beforeMethod(Method method) {
        startRecording(method);
    }

    @AfterMethod
    private void afterMethod(ITestResult result) {
        stopRecording();
    }

    @AfterClass
    private void afterClass() {
        webDriver.quit();
    }

    @Test
    @Story("Homepage loads correctly")
    @Description("Verify homepage loads with logo and navigation links displayed")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyHomepageLoads() {
        logInfo("Verify logo is displayed", () ->
                assertTrue(homePage.isLogoDisplayed(), "Logo is displayed on homepage")
        );

        logInfo("Verify SALE nav link is displayed", () ->
                assertTrue(homePage.isNavLinkDisplayed("SALE"), "SALE nav link is displayed")
        );

        logInfo("Verify SẢN PHẨM nav link is displayed", () ->
                assertTrue(homePage.isNavLinkDisplayed("SẢN PHẨM"), "SẢN PHẨM nav link is displayed")
        );

        logInfo("Verify VỀ CỎ MỀM nav link is displayed", () ->
                assertTrue(homePage.isNavLinkDisplayed("VỀ CỎ MỀM"), "VỀ CỎ MỀM nav link is displayed")
        );

        logInfo("Verify page title contains Cỏ Mềm", () ->
                assertTrue(homePage.getPageTitle().contains("Cỏ Mềm"), "Page title contains 'Cỏ Mềm'")
        );
    }

    @Test
    @Story("Navigate to About page")
    @Description("Click VỀ CỎ MỀM link and verify About page loads correctly")
    @Severity(SeverityLevel.NORMAL)
    public void verifyNavigateToAboutPage() {
        logInfo("Click VỀ CỎ MỀM nav link", () -> {
            homePage.clickNavLink("VỀ CỎ MỀM");
            CommonUtils.pause(2);
        });

        AboutPage aboutPage = PageProvider.create(AboutPage.class, webDriver);

        logInfo("Verify URL contains /ve-co-mem", () ->
                assertTrue(aboutPage.getCurrentUrl().contains("/ve-co-mem"), "URL contains /ve-co-mem")
        );

        logInfo("Verify page heading is displayed", () ->
                assertTrue(aboutPage.isPageHeadingDisplayed(), "About page heading is displayed")
        );

        logInfo("Navigate back to homepage", () -> {
            webDriver.navigate("https://comem.vn/");
            CommonUtils.pause(2);
        });
    }

    @Test
    @Story("Navigate to Store Locator")
    @Description("Click store locator link and verify store information is displayed")
    @Severity(SeverityLevel.NORMAL)
    public void verifyNavigateToStoreLocator() {
//        logInfo("Scroll down to store locator link", () ->
//                webDriver.js().scrollToBottom()
//        );

        CommonUtils.pause(1);

        logInfo("Click store locator link", () -> {
            homePage.clickStoreLocator();
            CommonUtils.pause(2);
        });

        StoreLocatorPage storeLocatorPage = PageProvider.create(StoreLocatorPage.class, webDriver);

        logInfo("Verify URL contains /dia-chi-cua-hang", () ->
                assertTrue(storeLocatorPage.getCurrentUrl().contains("/dia-chi-cua-hang"),
                        "URL contains /dia-chi-cua-hang")
        );

        logInfo("Verify hotline is displayed", () ->
                assertTrue(storeLocatorPage.isHotlineDisplayed(), "Hotline contact is displayed")
        );

        logInfo("Navigate back to homepage", () -> {
            webDriver.navigate("https://comem.vn/");
            CommonUtils.pause(2);
        });
    }
}
