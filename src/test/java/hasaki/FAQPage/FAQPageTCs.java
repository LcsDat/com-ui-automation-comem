package hasaki.FAQPage;

import cores.BaseTest;
import cores.Browser;
import cores.DriverFactory;
import cores.PageFactory;
import org.testng.annotations.*;

public class FAQPageTCs extends BaseTest {
    @Parameters({"chrome", "url"})
    @BeforeClass
    void beforeClass(Browser browser, String url) {
        webDriver = DriverFactory.initWebsiteDriver(browser);
        homepage = PageFactory.generateHomePage(webDriver);
        productPage = PageFactory.generateProductsPage(webDriver);
        productDetailsPage = PageFactory.generateProductDetailsPage(webDriver);
        cartPage = PageFactory.generateCartPage(webDriver);
        storesLocationPage = PageFactory.generateStoresLocationPage(webDriver);
        faqPage = PageFactory.generateFAQPage(webDriver);

        webDriver.navigate(url);

        homepage.cancelPopup();
        homepage.cancelCookie();
        homepage.removeProductFromCart();
    }

    @AfterMethod
    void afterMethod() {
        switchToMainWebsite();
        navigateToHomePage();
    }

    @AfterClass
    void afterClass() {
        logout();
        quitBrowser();
    }

    @AfterTest(alwaysRun = true)
    void afterTest() {
        cleanDriverProcess();
    }

    @Test
    void tc01() {
        homepage.navigateFAQPage();
        homepage.switchWindowByTitle("Hỗ trợ khách hàng");

        verifyEquals(faqPage.getSloganHeader(), "Xin chào! Chúng tôi có thể giúp gì cho bạn?");

    }

    @Test
    void tc02() {
        homepage.switchWindowByTitle("Hỗ trợ khách hàng");
        faqPage.navigateStoresLocationPage();

        verifyEquals(storesLocationPage.getBreadcrumbText(), "Hệ Thống Cửa Hàng Hasaki Trên Toàn Quốc | Hasaki.vn");
        verifyTrue(storesLocationPage.isStoresDisplayed());
        verifyEquals(storesLocationPage.getTotalStores(), "Có 254 cửa hàng Hasaki trên toàn quốc");

        faqPage.clickStore("1");

        verifyEquals(faqPage.getFullAddress("1").trim(),"Địa chỉ: 71 Hoàng Hoa Thám, Phường 13, Quận Tân Bình, Hồ Chí Minh");
        verifyEquals(faqPage.getClockQuantity("1"),2);
    }
}
