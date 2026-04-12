package hasaki.OrderFlow;

import com.aventstack.extentreports.markuputils.ExtentColor;
import cores.BaseTest;
import cores.Browser;
import cores.PageFactory;
import org.openqa.selenium.InvalidSelectorException;
import org.testng.Assert;
import org.testng.annotations.*;
import utilities.ExcelData;
import utilities.TestData;

import java.lang.reflect.Method;

public class User_Order_One_Product extends BaseTest {

    @BeforeTest
    void beforeTest() {
        createLog(User_Order_One_Product.class);
    }

    @Parameters({"browser", "url", "username", "password"})
    @BeforeMethod
    void beforeMethod(Browser browser, String url, String username, String password, Method method) {

        createTestCase(method);

        logInfo(method, "Browser: " + browser, ExtentColor.LIME);
        webDriver = getWebDriver(browser);
        logInfo(method, "------ Setup steps include ------");
        logInfo(method, "- Initialize relevant pages");
        homepage = PageFactory.generateHomePage(webDriver);
        productPage = PageFactory.generateProductsPage(webDriver);
        productDetailsPage = PageFactory.generateProductDetailsPage(webDriver);
        cartPage = PageFactory.generateCartPage(webDriver);
        paymentPage = PageFactory.generatePaymentPage(webDriver);

        logInfo(method, "- Navigate to " + url);
        webDriver.navigate(url);

//        logInfo(method, "- Close popup");
//        homepage.cancelPopup();

        logInfo(method, "- Reject cookie");
        homepage.cancelCookie();

        logInfo(method, "- Login with Username: " + username + " Password: " + password);
        homepage.login(username, password);

        logInfo(method, "- Remove products in Cart if they exist");
        homepage.removeProductFromCart();
    }

    @AfterMethod
    void afterMethod(Method method) {

        logInfo(method, "Switch back to main tab after each test case");
        switchToMainWebsite();


        logInfo(method, "Navigate back to Home page after each test case");
        navigateToHomePage();
        sleepInSecond(1);

        logInfo(method, "Remove products in Cart if they exist");
        homepage.removeProductFromCart();

        logInfo(method, "Navigate back to Home page after each test case");
        navigateToHomePage();


        logInfo(method, "------ Tear down steps include ------");
        logInfo(method, "- Log out");
        logout();

        logInfo(method, "- Close the browser");
        quitBrowser();
    }

//    @AfterClass
//    void afterClass(Method method) {
//        logInfo(method,"------ Tear down steps include ------");
//        logInfo(method,"- Log out");
//        logout();
//
//        logInfo(method,"- Close the browser");
//        quitBrowser();
//    }


    @Test(dataProvider = "excel", dataProviderClass = TestData.class)
    @ExcelData(sheet = "OrderProductInChrome-tc01")
    void TC01_Order_A_Product_Successfully(String categoryName, String productType, String productName, String expectedQuantity, Method method) {

        logInfo(method, "Choose %s in Category Menu, then choose %s product type".formatted(categoryName, productType));
        homepage.chooseProductType(categoryName, productType);

        logInfo(method, "Choose a specific product: %s".formatted(productName));
        productPage.chooseProduct(productName);

        sleepInSecond(1);

        logInfo(method, "Increase product quantity to %s".formatted(expectedQuantity));
        productDetailsPage.setProductQty(Integer.parseInt(expectedQuantity));

        sleepInSecond(5);

        assertEquals(webDriver.getDomAttribute("input[name='qty']", "value"), expectedQuantity);

        logInfo(method, "Click add product to Cart");
        productDetailsPage.addProductToCart();

        assertTrue(productDetailsPage.isDisplayed("//div[text()='Sản Phẩm đã được thêm vào giỏ hàng thành công']"));

        logInfo(method, "Wait for success message invisible: 'Successfully add product to the cart'");
        productDetailsPage.waitToBeInvisible("//div[text()='Sản Phẩm đã được thêm vào giỏ hàng thành công']");

        sleepInSecond(1);

        String productQuantity = webDriver.getText("//span[text()='Cart Icon']/following-sibling::span");
        String productPrice = "";
        try {
            productPrice = webDriver.getText("span.text-orange.text-lg.font-bold").replaceAll("[^0-9]", "");
        } catch (InvalidSelectorException e) {
            productPrice = webDriver.getText("span.text-orange.text-base").replaceAll("[^0-9]", "");
        }

        logInfo(method, "Click to view Cart info");
        productDetailsPage.clickToCart();

        sleepInSecond(2);

        assertEquals(webDriver.getText("//a[text()=\"%s\"]".formatted(productName)), productName);

        Integer calculatedPrice = (Integer.parseInt(productQuantity) * Integer.parseInt(productPrice));

        String totalPriceeAt = webDriver.getText("//tbody//tr[1]/td[4]/div").replaceAll("[^0-9]", "");
        Integer totalPrice = Integer.parseInt(totalPriceeAt);

        assertEquals(calculatedPrice, totalPrice);

        logInfo(method, "Click proceed to Cart");
        cartPage.clickProceedToCart();

        sleepInSecond(2);

        assertTrue(webDriver.getPageTitle().contains("Thanh toán"));

        //Temp verification of user delivery address
        String[] userInfos = webDriver.getText("//h2[text()='Địa chỉ nhận hàng']/following-sibling::div/child::div").replace("\n", "#").split("#");
        String addressType = userInfos[0];
        String userNameAndPhone = userInfos[1];
        String userAddress = userInfos[2];

        assertEquals(addressType, "Nhà riêng");

        //Begin: show full mobile phone
        //16/12/2025: mask the mobile phone
        assertEquals(userNameAndPhone, "Le Dat - *******246");
        assertEquals(userAddress, "687/5 Lạc Long Quân, Phường 10, Quận Tân Bình, Hồ Chí Minh");

        logInfo(method, "Click to edit Delivery address");
        paymentPage.chooseEdit("Địa chỉ nhận hàng", "Thay đổi");

        logInfo(method, "Click to add a new address");
        paymentPage.clickAddNewAddress();

        logInfo(method, "Click Continue to create a new address");
        paymentPage.clickContinue("Thêm địa chỉ mới");

        assertEquals(paymentPage.getCommonValidationMessageInput("Số điện thoại"), "Vui lòng điền số điện thoại");
        assertEquals(webDriver.getText("//input[@placeholder='Họ và tên']/following-sibling::p"), "Vui lòng điền Họ và Tên");
        assertEquals(paymentPage.getCommonValidationMessageDropdown("Chọn Tỉnh/ TP, Quận/ Huyện"), "Vui lòng chọn Tỉnh/ TP, Quận/ Huyện");
        assertEquals(paymentPage.getCommonValidationMessageDropdown("Chọn Phường/ Xã"), "Vui lòng chọn Phường/ Xã");

        //Before: Vui lòng điền địa chỉ
        //Now: Địa chỉ phải từ 5 ký tự
        assertEquals(webDriver.getText("//input[@placeholder='Số nhà + Tên đường']/following-sibling::p"), "Địa chỉ phải từ 5 ký tự");

        String phoneNo = "0345864246";
        String maskedPhoneNo = "*******" + phoneNo.substring(7);
        String userName = "Dat Le Mot" + randomAlphabetic(4);
        String cityName = "Quận Tân Bình";
        String wardName = "Phường 10";
        String streetNumberName = "687 Lạc Long Quân";

        logInfo(method, "Input phone number for new address");
        paymentPage.setTextToNewAddressFields("Số điện thoại", phoneNo);

        logInfo(method, "Input Name contact for new address");
        paymentPage.setTextToNewAddressFields("Họ và tên", userName);

        logInfo(method, "Input City for new address");
        paymentPage.chooseCity(cityName);

        logInfo(method, "Input Ward for new address");
        paymentPage.chooseWard(wardName);

        logInfo(method, "Click to Street field");
        paymentPage.clickStreetField();

        //Continue button is disable if user doesn't input street number
        assertFalse(webDriver.isEnabled("//span[text()='Sửa vị trí trên bản đồ']/parent::div//following-sibling::div//button[text()='Tiếp tục']"));

        logInfo(method, "Input street for new address, which does not meet minimum length of characters");
        paymentPage.setTextStreetField("687");

        sleepInSecond(2);

        assertEquals(webDriver.getText("(//input[@placeholder='Nhập vị trí của bạn']/following-sibling::span)[1]"), "Địa chỉ phải trên 5 ký tự");

        logInfo(method, "Input street for new address, which is a valid length");
        paymentPage.setTextStreetField(streetNumberName);

        //Continue input is enable
        assertTrue(webDriver.isEnabled("//span[text()='Sửa vị trí trên bản đồ']/parent::div//following-sibling::div//button[text()='Tiếp tục']"));

        String newStreetNo = paymentPage.getStreetNumberInputValue("value");

        logInfo(method, "Click to create a new street address");
        paymentPage.clickContinueStreetNumberButton();

        logInfo(method, "Click to create a new address");
        paymentPage.clickContinue("Thêm địa chỉ mới");

        logInfo(method, "Wait for success message invisible: 'Successfully update a new delivery address'");
        paymentPage.waitForMessageInvisible("Cập nhật địa chỉ thành công");

        String[] newUserInfosArr = webDriver.getText("//p[contains(string(),'Dat Le Mot')]/ancestor::label").replaceAll("\n", "#").split("#");

        assertEquals(newUserInfosArr[0], userName + " - " + maskedPhoneNo);
        assertEquals(newUserInfosArr[3], newStreetNo + ", " + wardName + ", " + cityName + ", " + "Hồ Chí Minh");

        logInfo(method, "Click to delete an address");
        paymentPage.deleteAddress(newUserInfosArr[0]);

        logInfo(method, "Wait for success message invisible: 'Successfully delete the address'");
        paymentPage.waitForMessageInvisible("Thông tin địa chỉ nhận hàng đã được xóa.");

        logInfo(method, "Click to continue cart process");
        paymentPage.clickContinue("Địa chỉ nhận hàng");

        sleepInSecond(1);

//        logInfo("Wait for success message invisible: 'Successfully update delivery address'");
//        paymentPage.waitForMessageInvisible("Cập nhật địa chỉ thành công");

        logInfo(method, "Click to Edit the payment method");
        paymentPage.chooseEdit("Hình thức thanh toán", "Thay đổi");

        //Choose by name
        logInfo(method, "Change payment method to VNPAY");
        paymentPage.choosePaymentMethod("Thanh toán trực tuyến VNPAY");

        logInfo(method, "Click to continue cart process");
        paymentPage.clickContinue("Hình thức thanh toán");

//        logInfo("Wait for success message invisible: 'Successfully update payment method'");
//        paymentPage.waitForMessageInvisible("Cập nhật hình thức thanh toán thành công");

        logInfo(method, "Click to edit coupons");
        paymentPage.chooseEdit("Phiếu mua hàng", "Chọn phiếu mua hàng");

        assertTrue(paymentPage.isDisplayed("//h2[text()='Bạn có phiếu mua hàng']"));

        logInfo(method, "Close Coupon popup");
        paymentPage.closePopup();

        logInfo(method, "Click to edit vouchers");
        paymentPage.chooseEdit("Mã giảm giá", "Nhập mã giảm giá");

        sleepInSecond(1);

        Assert.assertTrue(paymentPage.isDisplayed("//h2[text()='Bạn có mã giảm giá']"));
        assertTrue(paymentPage.isDisplayed("//h2[text()='Bạn có mã giảm giá']"));

        logInfo(method, "Close Voucher popup");
        paymentPage.closePopup();

        logInfo(method, "Click to change the desired product");
        paymentPage.changeProduct();

        sleepInSecond(2);

        assertTrue(paymentPage.getPageTitle().contains("Giỏ hàng"));
    }
}
