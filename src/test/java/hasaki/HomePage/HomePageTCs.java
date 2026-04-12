package hasaki.HomePage;

import cores.BaseTest;
import cores.Browser;
import cores.DriverFactory;
import cores.PageFactory;
import org.testng.annotations.*;

public class HomePageTCs extends BaseTest {


    @Parameters({"chrome", "url"})
    @BeforeClass
    void beforeClass(Browser browser, String url) {
        webDriver = DriverFactory.initWebsiteDriver(browser);
        homepage = PageFactory.generateHomePage(webDriver);
        productPage = PageFactory.generateProductsPage(webDriver);
        productDetailsPage = PageFactory.generateProductDetailsPage(webDriver);
        cartPage = PageFactory.generateCartPage(webDriver);
        storesLocationPage = PageFactory.generateStoresLocationPage(webDriver);

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

    @AfterTest
    void afterTest() {
        cleanDriverProcess();
    }

    @Test(groups = {"Negative Test Cases"})
    void tc01() {
        homepage.chooseProductFromSearchDropdown("Cerave");
        productDetailsPage.addProductToCart();

        verifyTrue(webDriver.waitToBeVisible("div[role='dialog']").isDisplayed());
        verifyEquals(
                webDriver.findElement("input[name='username']").getDomAttribute("placeholder"),
                "Nhập email hoặc số điện thoại");
        verifyEquals(
                webDriver.findElement("input[name='password']").getDomAttribute("placeholder"),
                "Nhập password");

        cartPage.closeLoginDialog();
        cartPage.shipExpress2h();

        verifyTrue(webDriver.findElement("div[role='dialog']").isDisplayed());
        verifyEquals(
                webDriver.findElement("input[name='username']").getDomAttribute("placeholder"),
                "Nhập email hoặc số điện thoại");
        verifyEquals(
                webDriver.findElement("input[name='password']").getDomAttribute("placeholder"),
                "Nhập password");

        cartPage.closeLoginDialog();
    }

    @Test(groups = "Negative Test Cases")
    void tc02() {
        homepage.chooseProductType("Chăm Sóc Da Mặt", "Tẩy Trang Mặt");
        productPage.chooseProduct("Combo 2 Nước Tẩy Trang Bí Đao Cocoon Làm Sạch & Giảm Dầu 500ml");
        productDetailsPage.addProductToCart();

        verifyTrue(webDriver.waitToBeVisible("div[role='dialog']").isDisplayed());
        verifyEquals(
                webDriver.findElement("input[name='username']").getDomAttribute("placeholder"),
                "Nhập email hoặc số điện thoại");
        verifyEquals(
                webDriver.findElement("input[name='password']").getDomAttribute("placeholder"),
                "Nhập password");

        cartPage.closeLoginDialog();
        cartPage.shipExpress2h();

        verifyTrue(webDriver.findElement("div[role='dialog']").isDisplayed());
        verifyEquals(
                webDriver.findElement("input[name='username']").getDomAttribute("placeholder"),
                "Nhập email hoặc số điện thoại");
        verifyEquals(
                webDriver.findElement("input[name='password']").getDomAttribute("placeholder"),
                "Nhập password");

        cartPage.closeLoginDialog();
    }

    @Test
    void tc03() {
        homepage.navigateToStoresLocationPage();
        homepage.switchWindowByTitle("Hệ Thống Cửa Hàng");

        verifyEquals(storesLocationPage.getBreadcrumbText(), "Hệ Thống Cửa Hàng Hasaki Trên Toàn Quốc | Hasaki.vn");
        verifyTrue(storesLocationPage.isStoresDisplayed());
        verifyEquals(storesLocationPage.getTotalStores(), "Có 254 cửa hàng Hasaki trên toàn quốc");
    }
}
