package components;

import cores.BaseComponent;
import cores.BrowserDriver;
import io.qameta.allure.Step;

public class HeaderComponent extends BaseComponent<HeaderComponent> {

    public HeaderComponent(BrowserDriver driver) {
        super(driver);
    }

    //Login components
    private static final String ACCOUNT_BUTTON = "a[aria-label='Tài khoản']";
    private static final String USERNAME_INPUT = "input[type='email']";
    private static final String PASSWORD_INPUT = "input[type='password']";
    private static final String SIGN_IN_BUTTON = "button[type='submit']";
    private static final String PERSONAL_INFORMATION_LINK = "//a[text()='Thông tin cá nhân']";
    private static final String PERSON_ICON_AFTER_LOGIN = "div.icon-member-active";

    //Personal Information page
    public static final String EMAIL_FIELD = "input[name='email'][class='form__control form__control--full']";
    public static final String NAME_FIELD = "input[name='fullname'][class='form__control form__control--full']";

    @Step("Click on Account icon")
    public HeaderComponent clickOnAccountIcon() {
        click(ACCOUNT_BUTTON);
        return self();
    }

    @Step("Input username: {value}")
    public HeaderComponent inputUsername(String value) {
        setText(USERNAME_INPUT, value);
        return self();
    }

    @Step("Input password")
    public HeaderComponent inputPassword(String value) {
        setText(PASSWORD_INPUT, value);
        return self();
    }

    @Step("Click Login button")
    public HeaderComponent clickOnLoginButton() {
        click(SIGN_IN_BUTTON);
        return self();
    }

    @Step("Hover Person Icon")
    public HeaderComponent hoverPersonIcon() {
        hover(PERSON_ICON_AFTER_LOGIN);
        return self();
    }

    @Step("Click Personal Information Link")
    public HeaderComponent clickOnPersonalInformationLink() {
        click(PERSONAL_INFORMATION_LINK);
        return self();
    }

    @Step("Get {0} value")
    public String getValue(String fieldName) {
        return getDomAttribute(fieldName, "value");
    }

}
