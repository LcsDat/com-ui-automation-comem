package hasaki.accountManagement;

import com.aventstack.extentreports.markuputils.ExtentColor;
import cores.BaseTest;
import cores.Browser;
import cores.PageFactory;
import hasaki.OrderFlow.User_Order_One_Product;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class Profile_Information_Update extends BaseTest {

    @BeforeTest
    void beforeTest() {
        createLog(Profile_Information_Update.class);
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

    @Test
    void EditProfileUnderUserAvatar() {
        //Hover in Account
        //Select My Account
        //Select Edit Your Account, under the avatar
        //Verify Account Information Page (decide verify points later)
        //Edit Account (change Male to Female)
        //Click on Update
        //Verify message 'Update account information successfully'
    }

    @Test
    void EditProfileInAccountManagement() {
        //Hover in Account
        //Select My Account
        //Click on Edit button
        //Verify Account Information Page (decide verify points later)
        //Edit Account (change Male to Female)
        //Click on Update
        //Verify message 'Update account information successfully'
    }

    @Test
    void EditProfileInAccountInformation() {
        //Hover in Account
        //Select My Account
        //Click on Account Information menu
        //Verify Account Information Page (decide verify points later)
        //Edit Account (change Male to Female)
        //Click on Update
        //Verify message 'Update account information successfully'
    }
}
