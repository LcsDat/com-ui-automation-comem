package comem.authentication;

import cores.BaseTest;
import cores.Browser;
import cores.PageProvider;
import io.qameta.allure.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Feature("Authentication")
public class LoginTests extends BaseTest {

    @BeforeClass
    @Parameters("url")
    private void beforeClass(String url) {
        initLogger(LoginTests.class);
        webDriver = initDriver(Browser.CHROME);
        homepage = PageProvider.createHomePage(webDriver);
        webDriver.navigate(url);
    }

    @Test
    @Story("Positive login")
    @Description("User logs in with valid credentials and verifies the account is accessible")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyPositiveCaseLogIn() {
        Allure.step("Open the login form", () ->
                homepage.header.clickOnAccountIcon()
        );

        Allure.step("Fill in credentials and submit", () ->
                homepage.header
                        .inputUsername("datle.testing01@gmail.com")
                        .inputPassword("#Onimusha00")
                        .clickOnLoginButton()
                        .hoverPersonIcon()
                        .clickOnPersonalInformationLink()

        );

//        homepage.header;
        Allure.step("Verify email is displayed", () ->
                assertEquals(homepage.header.getValue(homepage.header.EMAIL_FIELD), "datle.testing01@gmail.com")
        );

        Allure.step("Verify full name is displayed", () ->
                assertEquals(homepage.header.getValue(homepage.header.NAME_FIELD), "Lê Châu Sỷ Đạt")
        );
    }

    @Test
    @Story("Negative login")
    @Description("User attempts to log in with invalid credentials and verifies the error message")
    @Severity(SeverityLevel.NORMAL)
    public void verifyNegativeCaseLogIn() {
        // TODO: implement negative login steps
    }
}