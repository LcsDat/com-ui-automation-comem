package hasaki.authentication;

import com.aventstack.extentreports.markuputils.ExtentColor;
import cores.BaseTest;
import cores.Browser;
import cores.DriverFactory;
import cores.PageFactory;
import org.testng.annotations.*;
import utilities.ExcelData;
import utilities.SheetNames;
import utilities.TestData;

import java.lang.reflect.Method;


public class Login extends BaseTest {


    @BeforeTest
    void beforeClass() {
        createLog(Login.class);
    }

    @Parameters({"browser", "url"})
    @BeforeMethod
    void beforeMethod(Browser browser, String url, Method method) {
        createTestCase(method);

        logInfo(method, "Open the first Browser: " + browser, ExtentColor.LIME);
        webDriver = getWebDriver(browser);

        logInfo(method, "------ Setup steps include ------");
        logInfo(method, "- Initialize relevant pages");
        homepage = PageFactory.generateHomePage(webDriver);

        logInfo(method, "- First browser: " + browser + " - Navigate to " + url);
        webDriver.navigate(url);

        logInfo(method, "- First browser: " + browser + " - Close popup");
        homepage.cancelPopup();

        logInfo(method, "- First browser: " + browser + " - Reject cookie");
        homepage.cancelCookie();
    }

    @AfterMethod
    void afterMethod(Method method) {
        logInfo(method, "------ Tear down steps include ------");
        logInfo(method, "Remove product from Cart if existed");
        homepage.removeProductFromCart();

        logInfo(method, "- Log out");
        logout();

        logInfo(method, "- Close the browser");
        quitBrowser();
    }

    @Test(dataProvider = "excel", dataProviderClass = TestData.class)
    @ExcelData(sheet = SheetNames.Login.TC01)
    void TC01_User_Login_With_Valid_Data(String username, String password, Method method) {

        logInfo(method, "Access application and login with valid credentials");
        homepage.login(username, password);

        logInfo(method, "Verify user is logged in successfully");
        assertEquals(homepage.getWelcomeText(), "Chào Dat", "A welcome label + user first name is displayed to the user");
    }

    @Test(dataProvider = "Login-tc02", dataProviderClass = TestData.class)
    @ExcelData(sheet = "Login-tc02")
    void TC02_User_Login_With_Incorrect_Data(String username, String password, Method method) {

        logInfo(method, "Access application and login with invalid credentials");
        homepage.login(username, password);

        assertEquals(homepage.getWarningMessage(), "Tên đăng nhập hoặc mật khẩu không khớp !", "Error message display when user incorrect data");
    }

    @Test()
    void TC03_User_Login_With_Blank_Username(Method method) {

        logInfo(method, "Access application and login with blank username");
        homepage.login("", "#Onimusha00");

        assertEquals(homepage.getWarningMessage(), "Vui lòng nhập tên đăng nhập", "Validation message display that username is required");
    }

    @Test()
    void TC04_User_Login_With_Blank_Password(Method method) {

        logInfo(method, "Access application and login with blank password");
        homepage.login("0345864246", "");

        assertEquals(homepage.getWarningMessage(), "Vui lòng nhập mật khẩu", "Validation message display that password is required");
    }

    @Test()
    void TC05_User_Login_With_All_Blank_Fields(Method method) {

        logInfo(method, "Access application and login with blank fields");
        homepage.login("", "");

        assertEquals(homepage.getWarningMessage(), "Vui lòng nhập tên đăng nhập", "Validation message display that user need to fill credentials");
    }

//    @Test()
//    void userLoginWithFacebook() {
//        homepage.loginByFacebook();
//
//        assertEquals(homepage.getWelcomeText(), "Chào Đạt");
//        sleepInSecond(2);
//    }

    @Test()
    void TC06_User_Login_With_Different_Accounts_In_A_Same_Browser(Method method) {

        //Get window handle of 1st tab
        var firstWindow = webDriver.getDriver().getWindowHandle();

        //Open 2nd tab, get window handle of 2nd tab
        logInfo(method, "Open a new tab then navigate to the application");
        webDriver.openNewTab().get("https://hasaki.vn/");

        var secondWindow = webDriver.getDriver().getWindowHandle();

        //Log in second account in 2nd tab
        logInfo(method, "Log in a new different account in the second tab");
        homepage.login("0345864246", "#Onimusha00");
        //Verify it's logged
        assertEquals(homepage.getWelcomeText(), "Chào Dat", "A welcome label + user first name is displayed to the user");

        //Switch back 1st, verify the 2nd account now is logged in tab 1
        logInfo(method, "Switch back to the first tab");
        webDriver.switchWindowByID(firstWindow);

        //Log in 1st account in 1st tab
        logInfo(method, "Log in another account in the first tab");
        homepage.login("0796280280", "27051993@Phuong");

        //Verify it's logged
        assertEquals(homepage.getWelcomeText(), "Chào Phương", "A welcome label + user first name is displayed to the user");

        //Switch back 2nd, refresh and verify the 1st account now is logged in tab 2
        logInfo(method, "Switch back to the second tab");
        webDriver.switchWindowByID(secondWindow);

        logInfo(method, "Refresh page");
        homepage.refreshPage();

        assertEquals(homepage.getWelcomeText(), "Chào Phương", "The account in the first tab, now is displayed in the second tab");

        logInfo(method, "Close the second tab");
        webDriver.closeTab();

        //Switch back to first window
        logInfo(method, "Switch back to the first tab");
        webDriver.switchWindowByID(firstWindow);
    }

    @Test()
    void TC07_User_Login_With_Same_Accounts_In_A_Same_Browser(Method method) {

        //Get window handle of 1st tab
        var firstWindow = webDriver.getDriver().getWindowHandle();

        //Open 2nd tab, get window handle of 2nd tab
        logInfo(method, "Open a new tab then navigate to the application");
        webDriver.openNewTab().get("https://hasaki.vn/");

        var secondWindow = webDriver.getDriver().getWindowHandle();

        //Log in account in 2nd tab
        logInfo(method, "Log in with valid account in the second tab");
        homepage.login("0345864246", "#Onimusha00");

        //Verify it's logged
        assertEquals(homepage.getWelcomeText(), "Chào Dat", "A welcome label + user first name is displayed to the user");

        //Switch back to first tab, log in again
        logInfo(method, "Switch back to the first tab");
        webDriver.getDriver().switchTo().window(firstWindow);

        logInfo(method, "log in with same account");
        homepage.login("0345864246", "#Onimusha00");

        //Verify that the account is still logged in 1st tab
        assertEquals(homepage.getWelcomeText(), "Chào Dat", "User is enable to log in 2 tabs with same account");

        //Add something to cart
        logInfo(method, "Add a product into cart");
        webDriver.click("(//div[@class='item_sp_hasaki width_common relative'])[1]");
        webDriver.waitToBeClickable("//div[text()='Giỏ hàng']/parent::button").click();

        assertTrue(webDriver.waitToBeVisible("//div[text()='Sản Phẩm đã được thêm vào giỏ hàng thành công']").isDisplayed());

        //Switch back to second tab and refresh
        logInfo(method, "Switch back to the second tab");
        webDriver.getDriver().switchTo().window(secondWindow);

        logInfo(method, "Get item quantity before refreshing page");
        int initialQty = Integer.parseInt(webDriver.getText("span.counter_number.counter"));

        logInfo(method, "Refresh the page");
        webDriver.refreshPage();

        //Verify the cart is updated
        assertEquals(webDriver.getText("span.counter_number.counter"), String.valueOf(initialQty + 1), "The item quantity is updated.");
    }

    @Test()
    void TC08_User_Login_With_Different_Accounts_In_Different_Browsers(Method method) {

        //Log in first account
        logInfo(method, "Log in 1st account in 1st browser");
        homepage.login("0345864246", "#Onimusha00");

        //Verify it's logged
        assertEquals(homepage.getWelcomeText(), "Chào Dat", "A welcome message is displayed to the user");

        //Open 2nd browser
        var secondDriver = DriverFactory.initWebsiteDriver(Browser.FIREFOX);
        logInfo(method, "Open 2nd browser");
        secondDriver.navigate("https://hasaki.vn/");

        //Log in second account
        var secondHomepage = PageFactory.generateHomePage(secondDriver);

        logInfo(method, "Close popup in 2nd browser");
        secondHomepage.cancelPopup();

        logInfo(method, "Reject cookie in 2nd browser");
        secondHomepage.cancelCookie();

        logInfo(method, "Login 2nd account in second browser");
        secondHomepage.login("0796280280", "27051993@Phuong");

        //Verify it's logged
        assertEquals(secondHomepage.getWelcomeText(), "Chào Phương", "A welcome message is displayed to the user");
    }

    @Test()
    void TC09_User_Login_With_Same_Accounts_In_Different_Browsers(Method method) {

        //Log in first account
        logInfo(method, "Log in account in 1st browser");
        homepage.login("0345864246", "#Onimusha00");

        //Verify it's logged
        assertEquals(homepage.getWelcomeText(), "Chào Dat", "A welcome message is displayed to the user");

        //Open 2nd browser
        var secondDriver = DriverFactory.initWebsiteDriver(Browser.HEADLESSFIREFOX);
        logInfo(method, "Open 2nd browser");
        secondDriver.navigate("https://hasaki.vn/");

        //Log in first account
        var secondHomepage = PageFactory.generateHomePage(secondDriver);

        logInfo(method, "Close popup in 2nd browser");
        secondHomepage.cancelPopup();

        logInfo(method, "Reject cookie in 2nd browser");
        secondHomepage.cancelCookie();

        logInfo(method, "Login same account in 2nd browser");
        secondHomepage.login("0345864246", "#Onimusha00");

        //Verify it's logged in 2nd browser
        assertEquals(secondHomepage.getWelcomeText(), "Chào Dat", "A welcome message is displayed to the user");
    }

}
