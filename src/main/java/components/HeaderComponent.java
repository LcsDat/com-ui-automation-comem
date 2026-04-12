package components;

import cores.BaseComponent;
import cores.WebsiteDriver;
import io.qameta.allure.Step;

public class HeaderComponent extends BaseComponent<HeaderComponent> {

    public HeaderComponent(WebsiteDriver driver) {
        super(driver);
    }

    //Login components
    private static final String ACCOUNT_BUTTON = "a[aria-label='Tài khoản']";
    private static final String USERNAME_INPUT = "input[type='email']";
    private static final String PASSWORD_INPUT = "input[type='password']";
    private static final String SIGN_IN_BUTTON = "button[type='submit']";

    @Step("Click on Account icon")
    public HeaderComponent clickOnAccountIcon() {
        click(ACCOUNT_BUTTON);
        return self();
    }

    @Step("Input username: {value}")
    public HeaderComponent inputUsername(String value) {
        input(USERNAME_INPUT, value);
        return self();
    }

    @Step("Input password")
    public HeaderComponent inputPassword(String value) {
        input(PASSWORD_INPUT, value);
        return self();
    }

    @Step("Click Login button")
    public HeaderComponent clickOnLoginButton() {
        click(SIGN_IN_BUTTON);
        return self();
    }
}
