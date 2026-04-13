package cores;

import org.openqa.selenium.NoSuchElementException;

import java.time.Duration;

public class BasePage<T extends BasePage<T>> {
    protected BrowserDriver driver;



    public BasePage(BrowserDriver driver) {
        this.driver = driver;

    }

    private static final String LOGIN_DIALOG_CLOSE_BUTTON = "button[aria-label='Close notify form']";

    public void clickFirstMatching(String[] locators, String productName){
        for (String locator : locators){
            try {
                driver.setImplicitWait(Duration.ofSeconds(Constants.SHORT_TIMEOUT));
                driver.click(locator, productName);
            } catch (NoSuchElementException e){
                System.out.println("Use locator " + locator + " for product " + productName + " is fail.");
            } finally {
                driver.setImplicitWait(Duration.ofSeconds(Constants.LONG_TIMEOUT));
            }
        }

    }

    public Boolean waitUntilInvisible(String locator) {
        return driver.waitUntilInvisible(locator);
    }

    public Boolean waitUntilInvisible(String locator, String... varargs) {
        return driver.waitUntilInvisible(locator, varargs);
    }

    public UIElement waitUntilClickable(String locator) {
        return driver.waitUntilClickable(locator);
    }

    public UIElement waitUntilClickable(String locator, String... varargs) {
        return driver.waitUntilClickable(locator, varargs);
    }

    public UIElement waitUntilVisible(String locator) {
        return driver.waitUntilVisible(locator);
    }

    public UIElement waitUntilVisible(String locator, String... varargs) {
        return driver.waitUntilVisible(locator, varargs);
    }

    public void closeLoginDialog() {
        driver.click(LOGIN_DIALOG_CLOSE_BUTTON);
    }

    public void switchWindowByTitle(String titleContains) {
        driver.switchWindowByTitle(titleContains);
    }

    public void refreshPage(){
        driver.refreshPage();
    }

    public String getPageTitle() {
        return driver.getPageTitle();
    }

    protected static void pause(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isDisplayed(String locator) {
        return driver.isDisplayed(locator);
    }

    public boolean isDisplayed(String locator, String... varargs) {
        return driver.isDisplayed(locator, varargs);
    }

    public void waitForPageLoad() {
        driver.waitForPageLoad();
    }
}
