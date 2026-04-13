package cores;

import org.openqa.selenium.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BrowserDriver {

    private WebDriver driver;
    private ActionsHelper actionsHelper;
    private WaitHelper waitHelper;
    private JavascriptExecutor jsExecutor;

    private Duration defaultTimeout = Duration.ofSeconds(Constants.LONG_TIMEOUT);

    public BrowserDriver(Browser browser) {
        this.driver = DriverFactory.createWebDriver(browser);
        this.actionsHelper = new ActionsHelper(this, defaultTimeout);
        this.waitHelper = new WaitHelper(this, defaultTimeout);
        this.jsExecutor = (JavascriptExecutor) driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public Duration getDefaultTimeout() {
        return defaultTimeout;
    }

    public WebDriver openNewTab() {
        return driver.switchTo().newWindow(WindowType.TAB);
    }

    public String getWindowHandle() {
        return driver.getWindowHandle();
    }

    public void closeTab() {
        driver.close();
    }

    public void refreshPage() {
        driver.navigate().refresh();
    }

    /**
     * <b> ONLY</b>  use for Xpath
     *
     * @param locator Xpath locator
     * @param varargs variables can be flexible edited in the locator
     * @return Default WebElement
     * @throws NoSuchElementException Timeout to find the elemtn
     */
    public WebElement findNativeElement(String locator, String... varargs) {
        return driver.findElement(By.xpath(String.format(locator, varargs)));
    }

    /**
     * <b> ONLY</b>  use for Xpath
     *
     * @param locator Xpath locator
     * @return List of WebElement
     */
    private List<WebElement> findNativeWebElements(String locator) {
        return driver.findElements(By.xpath(locator));
    }

    /**
     * <b> ONLY</b>  use for Xpath
     *
     * @param locator Xpath locator
     * @return List of WebElement
     */
    private List<WebElement> findNativeWebElements(String locator, String... varargs) {
        return driver.findElements(By.xpath(String.format(locator, varargs)));
    }

    /**
     * This method is auto-detect the appropriate locator strategy to find web element
     *
     * @param locator To interact with the web element
     * @return Web element
     * @throws InvalidSelectorException The locator is invalid
     */
    public WebElement findNativeElement(String locator) {
        WebElement element = null;

        List<By> list = Arrays.asList(
                By.xpath(locator),
                By.cssSelector(locator),
                By.className(locator.replace(" ", "")),
                By.id(locator),
                By.name(locator));

        //Idenify Xpath locator
        if (locator.startsWith("/") || locator.startsWith("(")) {
            element = driver.findElement(By.xpath(locator));
        }

        //Identify css selector
        else if (locator.startsWith("#") || locator.startsWith(".")) element = driver.findElement(list.get(1));
        else {
            for (int i = 1; i < list.size() - 1; i++) {
                try {
                    //Set short timeout to find appropriate strategy
                    setImplicitWait(Duration.ofMillis(800));
                    element = driver.findElement(list.get(i));
                    if (element != null) {
//                        System.out.println("Found element using: " + list.get(i));
                        break;
                    }
                } catch (NoSuchElementException e) {

                } finally {
                    //Re-set default timeout
                    setImplicitWait(defaultTimeout);
                }
            }

            if (element == null) {
                throw new InvalidSelectorException("No strategy can be used with: " + locator);
            }
        }

        return element;
    }

    public String takeScreenshotBase64() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public void setImplicitWait(Duration duration) {
        driver.manage().timeouts().implicitlyWait(duration);
    }

    public void switchWindowByTitle(String titleContains) {

        driver.getWindowHandles().stream()
                .anyMatch(handle -> {
                    driver.switchTo().window(handle);
                    return driver.getTitle().contains(titleContains);
                });
    }

    public void switchWindowById(String windowID) {
        driver.switchTo().window(windowID);
    }

    private String getBrowserDriverName() {
        String driverName = driver.toString().toLowerCase();
        String browserDriverName = null;
        if (driverName.contains("chrome")) browserDriverName = "chromedriver";
        else if (driverName.contains("firefox")) browserDriverName = "geckodriver";
        else if (driverName.contains("edge")) browserDriverName = "msedgedriver";

        return browserDriverName;
    }

    public void killDriverProcess() {
        String cmdChrome = null;
        String cmdFirefox = null;
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            String browserDriverName = getBrowserDriverName();

            if (osName.contains("window")) {
                cmdChrome = "taskkill /F /IM chromedriver.exe /T";
                cmdFirefox = "taskkill /F /IM geckodriver.exe /T";
            } else {
                cmdChrome = "pkill -f chromedriver";
                cmdFirefox = "pkill -f geckodriver";
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                Process process1 = Runtime.getRuntime().exec(cmdChrome);
                process1.waitFor();
                Process process2 = Runtime.getRuntime().exec(cmdFirefox);
                process2.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isNotDisplayed(String locator) {
        boolean flag = false;
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            findElement(locator);
        } catch (NoSuchElementException e) {
            flag = true;
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        return flag;
    }

    public boolean isDisplayed(String locator) {
        return findElement(locator).isDisplayed();
    }

    public boolean isDisplayed(String locator, String... varargs) {
        return findElement(locator, varargs).isDisplayed();
    }

    public UIElement findElement(String locator) {
        return new UIElement(this, locator);
    }

    public UIElement findElement(String locator, String... varargs) {
        return new UIElement(this, locator, varargs);
    }


    /**
     * Find all elements using same locator
     *
     * @param locator The locator <b>MUST</b> be Xpath syntax
     * @return A list of elements
     */
    public List<UIElement> findElements(String locator) {
        List<WebElement> oriEles;
        List<UIElement> newEles = new ArrayList<>();

        oriEles = findNativeWebElements(locator);

        int i = 1;
        for (WebElement element : oriEles) {
            newEles.add(new UIElement(this, "(" + locator + ")" + "[" + i + "]"));
            i++;
        }

        return newEles;
    }

    /**
     * Find all elements using same locator
     *
     * @param locator The locator <b>MUST</b> be Xpath syntax
     * @return A list of elements
     */
    public List<UIElement> findElements(String locator, String... varargs) {
        List<WebElement> oriEles;
        List<UIElement> newEles = new ArrayList<>();

        if (locator.startsWith("/") || locator.startsWith("("))
            oriEles = findNativeWebElements(locator, varargs);
        else throw new InvalidSelectorException("Invalid Xpath locator.");

        int i = 1;
        for (WebElement element : oriEles) {
            newEles.add(new UIElement(this, "(" + locator + ")" + "[" + i + "]", varargs));
            i++;
        }

        return newEles;
    }

    public void waitForPageLoad() {
//        return jsExecutor.executeScript("return document.readyState").equals("complete");
        waitHelper.getWait().until(d -> jsExecutor.executeScript("return document.readyState").equals("complete"));
    }

    public String getText(String locator) {
        return findElement(locator).getText();
    }

    public String getDomAttribute(String locator, String attributeValue) {
        return findElement(locator).getDomAttribute(attributeValue);
    }

    public String getDomAttribute(String locator, String attributeValue, String... varargs) {
        return findElement(locator, varargs).getDomAttribute(attributeValue);
    }

    public String getText(String locator, String... varargs) {
        return findElement(locator, varargs).getText();
    }

    public void moveToElement(String locator) {
        actionsHelper.moveToElement(locator);
    }

    public void moveToElement(String locator, String... varargs) {
        actionsHelper.moveToElement(locator, varargs);
    }

    public void click(String locator) {
        findElement(locator).click();
    }

    public void click(String locator, String... varargs) {
        findElement(locator, varargs).click();
    }

    public void setText(String locator, String value) {
        findElement(locator).setText(value);
    }

    public void setText(String locator, String value, String... varargs) {
        findElement(locator, varargs).setText(value);
    }

    public void clear(String locator) {
        findElement(locator).clear();
    }

    public void clear(String locator, String... varargs) {
        findElement(locator, varargs).clear();
    }

    public Boolean waitUntilInvisible(String locator, String... varargs) {
        return waitHelper.waitUntilInvisible(locator, varargs);
    }

    public Boolean waitUntilInvisible(String locator) {
        return waitHelper.waitUntilInvisible(locator);
    }

    public UIElement waitUntilClickable(String locator) {
        return waitHelper.waitUntilClickable(locator);
    }

    public UIElement waitUntilClickable(String locator, String... varargs) {
        return waitHelper.waitUntilClickable(locator, varargs);
    }

    public UIElement waitUntilVisible(String locator) {
        return waitHelper.waitUntilVisible(locator);
    }

    public UIElement waitUntilVisible(String locator, String... varargs) {
        return waitHelper.waitUntilVisible(locator, varargs);
    }

    public void navigate(String applicationURL) {
        driver.get(applicationURL);
    }

    public void quit() {
        if (driver != null) {
            driver.manage().deleteAllCookies();
            driver.quit();
        }
    }

    public void actionClick(String locator) {
        actionsHelper.click(locator);
    }

    public void actionClick(String locator, String... varargs) {
        actionsHelper.click(locator, varargs);
    }

    public void doubleClick(String locator, String... varargs) {
        actionsHelper.doubleClick(locator, varargs);
    }

    public void doubleClick(String locator) {
        actionsHelper.doubleClick(locator);
    }

    public void sendKeys(String locator, Keys keys) {
        findNativeElement(locator).sendKeys(keys);
    }

    public void sendKeys(String locator, String... chord) {
        findNativeElement(locator).sendKeys(chord);
    }

    public void sendKeys(String locator, Keys... keys) {
        findNativeElement(locator).sendKeys(keys);
    }

    public void sendKeys(String locator, Keys keys, String... varargs) {
        findNativeElement(locator, varargs).sendKeys(keys);
    }

    public boolean isEnabled(String locator) {
        return findElement(locator).isEnabled();
    }

}
