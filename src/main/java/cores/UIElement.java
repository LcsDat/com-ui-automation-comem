package cores;

import org.openqa.selenium.*;

import java.util.function.Supplier;

public class UIElement {

    private WebElement element;
    private final BrowserDriver customDriver;
    private final String locator;
    private final String[] varargs;
    private static final int MAX_RETRIES = 2;

    public UIElement(BrowserDriver browserDriver, String locator) {
        this.customDriver = browserDriver;
        this.locator = locator;
        this.varargs = null;
        this.element = customDriver.findNativeElement(locator);
    }

    public UIElement(BrowserDriver browserDriver, String locator, String... varargs) {
        this.customDriver = browserDriver;
        this.locator = locator;
        this.varargs = varargs;
        this.element = customDriver.findNativeElement(locator, varargs);
    }

    private void refind() {
        element = (varargs != null)
                ? customDriver.findNativeElement(locator, varargs)
                : customDriver.findNativeElement(locator);
    }

    private <T> T withRetry(Supplier<T> action) {
        for (int i = 0; i <= MAX_RETRIES; i++) {
            try {
                return action.get();
            } catch (StaleElementReferenceException e) {
                if (i == MAX_RETRIES) throw e;
                refind();
            }
        }
        throw new StaleElementReferenceException("Max retries exceeded for: " + locator);
    }

    private void withRetryVoid(Runnable action) {
        withRetry(() -> { action.run(); return null; });
    }

    public WebElement getElement() {
        return element;
    }

    public void click() {
        withRetryVoid(() -> element.click());
    }

    public void setText(String value) {
        withRetryVoid(() -> element.sendKeys(value));
    }

    public String getText() {
        return withRetry(() -> element.getText());
    }

    public String getDomAttribute(String attributeValue) {
        return withRetry(() -> element.getDomAttribute(attributeValue));
    }

    public boolean isDisplayed() {
        return withRetry(() -> element.isDisplayed());
    }

    public String getCssValue(String value) {
        return withRetry(() -> element.getCssValue(value));
    }

    public boolean isEnabled() {
        return withRetry(() -> element.isEnabled());
    }

    public void clear() {
        withRetryVoid(() -> element.clear());
    }
}
