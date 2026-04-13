package cores;

import org.openqa.selenium.interactions.Actions;

import java.time.Duration;

public class ActionsHelper {
    private Actions actions;
    private BrowserDriver driver;

    public ActionsHelper(BrowserDriver driver, Duration timeout) {
        this.driver = driver;
        this.actions = new Actions(driver.getDriver(), timeout);
    }

    private UIElement resolveElement(String locator){
        return new UIElement(driver, locator);
    }

    private UIElement resolveElement(String locator, String... varargs){
        return new UIElement(driver, locator, varargs);
    }

    public void moveToElement(String locator){
        actions.moveToElement(resolveElement(locator).getElement()).perform();
    }

    public void moveToElement(String locator, String... varargs){
        actions.moveToElement(resolveElement(locator, varargs).getElement()).perform();
    }

    public void click(String locator){
        actions.click(resolveElement(locator).getElement()).perform();
    }

    public void click(String locator, String... varargs){
        actions.click(resolveElement(locator,varargs).getElement()).perform();
    }

    public void doubleClick(String locator){
        actions.doubleClick(resolveElement(locator).getElement()).perform();
    }

    public void doubleClick(String locator, String... varargs){
        actions.doubleClick(resolveElement(locator, varargs).getElement()).perform();
    }
}
