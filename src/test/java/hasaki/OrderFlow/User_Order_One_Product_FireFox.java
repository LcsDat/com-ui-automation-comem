package hasaki.OrderFlow;

import com.aventstack.extentreports.markuputils.ExtentColor;
import cores.BaseTest;
import cores.Browser;
import cores.PageFactory;
import org.testng.annotations.*;

public class User_Order_One_Product_FireFox extends BaseTest {

    @Parameters({"browser", "url", "username", "password"})
    @BeforeClass
    void beforeClass(Browser browser, String url, String username, String password) {
        System.out.println("bien log4j2manager: " + log4j2Manager);
        createLog(User_Order_One_Product_FireFox.class);

        logInfo("Browser: " + browser, ExtentColor.LIME);
        webDriver = getWebDriver(browser);

        logInfo("------ Setup steps include ------");
        logInfo("- Initialize relevant pages");
        homepage = PageFactory.generateHomePage(webDriver);
        productPage = PageFactory.generateProductsPage(webDriver);
        productDetailsPage = PageFactory.generateProductDetailsPage(webDriver);
        cartPage = PageFactory.generateCartPage(webDriver);
        paymentPage = PageFactory.generatePaymentPage(webDriver);

        logInfo("- Navigate to " + url);
        webDriver.navigate(url);

        logInfo("- Close popup");
        homepage.cancelPopup();

        logInfo("- Reject cookie");
        homepage.cancelCookie();

        logInfo("- Login with Username: " + username + " Password: " + password);
        homepage.login(username, password);

        logInfo("- Remove products in Cart if they exist");
        homepage.removeProductFromCart();
    }

    @AfterMethod
    void afterMethod() {
        logInfo("Switch back to main tab after each test case");
        switchToMainWebsite();

        logInfo("Navigate back to Home page after each test case");
        navigateToHomePage();
    }

    @AfterClass
    void afterClass() {
        logInfo("------ Tear down steps include ------");
        logInfo("- Log out");
        logout();

        logInfo("- Close the browser");
        quitBrowser();
    }

    @Test()
    void tc01() {
//        Choose product
        logInfo("Choose 'Skin Care' in Category Menu, then choose Cleansing product type");
        homepage.chooseProductType("Chăm Sóc Da Mặt", "Tẩy Trang Mặt");

        webDriver.waitToBeClickable("div.top-bar-wrap");
        sleepInSecond(2);

        logInfo("Choose a specific product");
        productPage.chooseProductOnFirefox("Combo 2 Nước Tẩy Trang Bí Đao Cocoon Làm Sạch & Giảm Dầu 500ml");

        sleepInSecond(2);

        logInfo("Increase product quantity to 2");
        productDetailsPage.setProductQty(2);

        sleepInSecond(1);

        assertEquals(webDriver.getDomAttribute("input[name='qty']", "value"), "2");

        logInfo("Click add product to Cart");
        productDetailsPage.addProductToCart();

        logInfo("Wait for warning message visible: 'Maximum quantity is 1'");
        productDetailsPage.waitToBeVisible("//div[text()='Sản phẩm chỉ được mua tối đa là 1']");

        assertTrue(webDriver.isDisplayed("//div[text()='Sản phẩm chỉ được mua tối đa là 1']"));

        logInfo("Wait for warning message invisible: 'Maximum quantity is 1'");
        productDetailsPage.waitToBeInvisible("//div[text()='Sản phẩm chỉ được mua tối đa là 1']");

        logInfo("Decrease product quantity by 1");
        productDetailsPage.decreaseProductQty();

        logInfo("Click add product to cart");
        productDetailsPage.addProductToCart();

        logInfo("Wait for success message visible: 'Successfully add product to the cart'");
        productDetailsPage.waitToBeVisible("//div[text()='Sản Phẩm đã được thêm vào giỏ hàng thành công']");

        assertTrue(productDetailsPage.isDisplayed("//div[text()='Sản Phẩm đã được thêm vào giỏ hàng thành công']"));

        logInfo("Wait for success message invisible: 'Successfully add product to the cart'");
        productDetailsPage.waitToBeInvisible("//div[text()='Sản Phẩm đã được thêm vào giỏ hàng thành công']");

        sleepInSecond(1);

        String productQuantity = webDriver.getText("//span[text()='Cart Icon']/following-sibling::span");
        String productName = webDriver.getText("//h1");
        String productPrice = webDriver.getText("span.text-orange.text-lg.font-bold").replaceAll("[^0-9]", "");

        assertEquals(productQuantity, "1");

        logInfo("Click to view Cart info");
        productDetailsPage.clickToCart();

        webDriver.waitForPageLoad();

        sleepInSecond(3);

        assertEquals(webDriver.getText("//a[text()='Combo 2 Nước Tẩy Trang Bí Đao Cocoon Làm Sạch & Giảm Dầu 500ml']"), productName);

        Integer calculatedPrice = (Integer.parseInt(productQuantity) * Integer.parseInt(productPrice));

        sleepInSecond(2);

        String totalPriceeAt = webDriver.getText("//tbody//tr[1]/td[4]/div").replaceAll("[^0-9]", "");
        Integer totalPrice = Integer.parseInt(totalPriceeAt);

        assertEquals(calculatedPrice, totalPrice);

        logInfo("Click proceed to Cart");
        cartPage.clickProceedToCart();

        sleepInSecond(2);

        assertTrue(webDriver.getPageTitle().contains("Thanh toán"));

        //Temp verification of user delivery address
        String[] userInfos = webDriver.getText("//h2[text()='Địa chỉ nhận hàng']/following-sibling::div/child::div")
                .replace("\n", "#")
                .split("#");
        String addressType = userInfos[0];
        String userNameAndPhone = userInfos[1];
        String userAddress = userInfos[2];

        assertEquals(addressType, "Nhà riêng");
        assertEquals(userNameAndPhone, "Vũ Trường Thiên Phương - 0796280280");
        assertEquals(userAddress, "687/5 Lạc Long Quân, Phường 10, Quận Tân Bình, Hồ Chí Minh");

        logInfo("Click to Edit the payment method");
        paymentPage.chooseEdit("Hình thức thanh toán", "Thay đổi");

        //Choose by name
        logInfo("Change payment method to VNPAY");
        paymentPage.choosePaymentMethod("Thanh toán trực tuyến VNPAY");

        logInfo("Click to continue cart process");
        paymentPage.clickContinue("Hình thức thanh toán");

        logInfo("Wait for success message invisible: 'Successfully update payment method'");
        paymentPage.waitForMessageInvisible("Cập nhật hình thức thanh toán thành công");

        logInfo("Click to edit coupons");
        paymentPage.chooseEdit("Phiếu mua hàng", "Chọn phiếu mua hàng");

        assertTrue(paymentPage.isDisplayed("//h2[text()='Bạn có phiếu mua hàng']"));

        logInfo("Close Coupon popup");
        paymentPage.closePopup();

        sleepInSecond(1);

        logInfo("Click to edit vouchers");
        paymentPage.chooseEdit("Mã giảm giá", "Nhập mã giảm giá");

        assertTrue(paymentPage.isDisplayed("//h2[text()='Bạn có mã giảm giá']"));

        logInfo("Close Voucher popup");
        paymentPage.closePopup();

        sleepInSecond(1);

        paymentPage.changeProduct();

        sleepInSecond(2);

        assertTrue(paymentPage.getPageTitle().contains("Giỏ hàng"));
    }
}
