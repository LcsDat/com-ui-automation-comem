package pages;

import cores.BasePage;
import cores.BrowserDriver;

public class HomePage extends BasePage<HomePage> {

    private static final String NAV_LINK = "//a[text()='%s']";
    private static final String ACCOUNT_LINK = "a[href='/account']";
    private static final String CART_LINK = "a[href='/cart']";
    private static final String STORE_LOCATOR_LINK = "a[href='https://comem.vn/dia-chi-cua-hang']";
    private static final String LOGO = "a[title='Cỏ Mềm HomeLab']";

    public HomePage(BrowserDriver driver) {
        super(driver);
    }

    public boolean isLogoDisplayed() {
        return driver.isDisplayed(LOGO);
    }

    public boolean isNavLinkDisplayed(String linkText) {
        return driver.isDisplayed(String.format(NAV_LINK, linkText));
    }

    public void clickNavLink(String linkText) {
        driver.click(String.format(NAV_LINK, linkText));
    }

    public void clickStoreLocator() {
        driver.waitUntilClickable(STORE_LOCATOR_LINK).click();
    }

    public String getPageTitle() {
        return driver.getPageTitle();
    }

    public String getCurrentUrl() {
        return driver.getDriver().getCurrentUrl();
    }
}
