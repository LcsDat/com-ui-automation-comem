package cores;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public class JsHelper {

    private final BrowserDriver driver;
    private final JavascriptExecutor js;

    public JsHelper(BrowserDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver.getDriver();
    }

    // ── Scroll ───────────────────────────────────────────────────────────────

    public void scrollToElement(String locator) {
        WebElement element = driver.findNativeElement(locator);
        js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", element);
    }

    public void scrollToElement(String locator, String... varargs) {
        WebElement element = driver.findNativeElement(locator, varargs);
        js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", element);
    }

    public void scrollToTop() {
        js.executeScript("window.scrollTo(0, 0);");
    }

    public void scrollToBottom() {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    public void scrollBy(int x, int y) {
        js.executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
    }

    // ── Click / Interact (bypass overlay) ────────────────────────────────────

    public void jsClick(String locator) {
        WebElement element = driver.findNativeElement(locator);
        js.executeScript("arguments[0].click();", element);
    }

    public void jsClick(String locator, String... varargs) {
        WebElement element = driver.findNativeElement(locator, varargs);
        js.executeScript("arguments[0].click();", element);
    }

    public void jsSetText(String locator, String value) {
        WebElement element = driver.findNativeElement(locator);
        js.executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                element, value);
    }

    public void jsSetText(String locator, String value, String... varargs) {
        WebElement element = driver.findNativeElement(locator, varargs);
        js.executeScript("arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                element, value);
    }

    // ── Attribute ────────────────────────────────────────────────────────────

    public void setAttribute(String locator, String attr, String value) {
        WebElement element = driver.findNativeElement(locator);
        js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", element, attr, value);
    }

    public String getAttribute(String locator, String attr) {
        WebElement element = driver.findNativeElement(locator);
        return (String) js.executeScript("return arguments[0].getAttribute(arguments[1]);", element, attr);
    }

    public void removeAttribute(String locator, String attr) {
        WebElement element = driver.findNativeElement(locator);
        js.executeScript("arguments[0].removeAttribute(arguments[1]);", element, attr);
    }

    // ── Visibility ───────────────────────────────────────────────────────────

    public void showElement(String locator) {
        WebElement element = driver.findNativeElement(locator);
        js.executeScript("arguments[0].style.display='';", element);
    }

    public void hideElement(String locator) {
        WebElement element = driver.findNativeElement(locator);
        js.executeScript("arguments[0].style.display='none';", element);
    }

    // ── Highlight ────────────────────────────────────────────────────────────

    public void highlightElement(String locator) {
        WebElement element = driver.findNativeElement(locator);
        doHighlight(element);
    }

    public void highlightElement(String locator, String... varargs) {
        WebElement element = driver.findNativeElement(locator, varargs);
        doHighlight(element);
    }

    private void doHighlight(WebElement element) {
        String originalStyle = element.getAttribute("style");
        js.executeScript(
                "arguments[0].style.border='2px solid red';"
                + "arguments[0].style.backgroundColor='rgba(255,0,0,0.1)';",
                element);
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {}
        js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, originalStyle != null ? originalStyle : "");
    }

    // ── Text ─────────────────────────────────────────────────────────────────

    public String getInnerText(String locator) {
        WebElement element = driver.findNativeElement(locator);
        return (String) js.executeScript("return arguments[0].innerText;", element);
    }

    public String getInnerHtml(String locator) {
        WebElement element = driver.findNativeElement(locator);
        return (String) js.executeScript("return arguments[0].innerHTML;", element);
    }

    // ── Raw execution ────────────────────────────────────────────────────────

    public Object executeScript(String script, Object... args) {
        return js.executeScript(script, args);
    }
}
