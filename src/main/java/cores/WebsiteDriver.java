package cores;

import org.openqa.selenium.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WebsiteDriver {

    private WebDriver driver;
    private WebsiteActions actions;
    private ExplicitWait webDriverWait;
    private JavascriptExecutor jsExecutor;

    private Duration defaultTimeout = Duration.ofSeconds(GlobalVariables.LONG_TIMEOUT);

    public WebsiteDriver(Browser browser) {
        this.driver = DriverFactory.initWebdriver(browser);
        this.actions = new WebsiteActions(this, defaultTimeout);
        this.webDriverWait = new ExplicitWait(this, defaultTimeout);
        this.jsExecutor = (JavascriptExecutor) driver;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public Duration getDefaultTimeout() {
        return defaultTimeout;
    }

    public WebDriver openNewTab(){
        return driver.switchTo().newWindow(WindowType.TAB);
    }

    public String getWindowHandle(){
        return driver.getWindowHandle();
    }

    public void closeTab(){
        driver.close();
    }
    public void refreshPage(){
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
    public WebElement findDefaultWebElement(String locator, String... varargs) {
        return driver.findElement(By.xpath(String.format(locator, varargs)));
    }

    /**
     * <b> ONLY</b>  use for Xpath
     *
     * @param locator Xpath locator
     * @return List of WebElement
     */
    public List<WebElement> findDefaultWebElements(String locator) {
        return driver.findElements(By.xpath(locator));
    }

    /**
     * <b> ONLY</b>  use for Xpath
     *
     * @param locator Xpath locator
     * @return List of WebElement
     */
    public List<WebElement> findDefaultWebElements(String locator, String... varargs) {
        return driver.findElements(By.xpath(String.format(locator, varargs)));
    }

    /**
     * This method is auto-detect the appropriate locator strategy to find web element
     *
     * @param locator To interact with the web element
     * @return Web element
     * @throws InvalidSelectorException The locator is invalid
     */
    public WebElement findDefaultWebElement(String locator) {
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

    public  String takeScreenshotBASE64(){
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

    public void switchWindowByID(String windowID) {
        driver.switchTo().window(windowID);
    }

    protected String getBrowserDriverName() {
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

    public boolean isUnDisplayed(String locator) {
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

    public WebsiteElement findElement(String locator) {
        return new WebsiteElement(this, locator);
    }

    public WebsiteElement findElement(String locator, String... varargs) {
        return new WebsiteElement(this, locator, varargs);
    }


    /**
     * Find all elements using same locator
     *
     * @param locator The locator <b>MUST</b> be Xpath syntax
     * @return A list of elements
     */
    public List<WebsiteElement> findElements(String locator) {
        List<WebElement> oriEles;
        List<WebsiteElement> newEles = new ArrayList<>();

        oriEles = findDefaultWebElements(locator);

        int i = 1;
        for (WebElement element : oriEles) {
            newEles.add(new WebsiteElement(this, "(" + locator + ")" + "[" + i + "]"));
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
    public List<WebsiteElement> findElements(String locator, String... varargs) {
        List<WebElement> oriEles;
        List<WebsiteElement> newEles = new ArrayList<>();

        if (locator.startsWith("/") || locator.startsWith("("))
            oriEles = findDefaultWebElements(locator, varargs);
        else throw new InvalidSelectorException("Invalid Xpath locator.");

        int i = 1;
        for (WebElement element : oriEles) {
            newEles.add(new WebsiteElement(this, "(" + locator + ")" + "[" + i + "]", varargs));
            i++;
        }

        return newEles;
    }

    public void waitForPageLoad() {
//        return jsExecutor.executeScript("return document.readyState").equals("complete");
        webDriverWait.getWait().until(d -> jsExecutor.executeScript("return document.readyState").equals("complete"));
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
        actions.moveToElement(locator);
    }

    public void moveToElement(String locator, String... varargs) {
        actions.moveToElement(locator, varargs);
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

    public void clear(String locator){
        findElement(locator).clear();
    }

    public void clear(String locator, String... varargs){
        findElement(locator, varargs).clear();
    }

    public Boolean waitToBeInvisible(String locator, String... varargs) {
        return webDriverWait.waitToBeInvisibleBy(locator, varargs);
    }

    public WebsiteElement waitToBeClickable(String locator) {
        return webDriverWait.waitToBeClickable(locator);
    }

    public WebsiteElement waitToBeClickable(String locator, String... varargs) {
        return webDriverWait.waitToBeClickable(locator, varargs);
    }

    public WebsiteElement waitToBeVisible(String locator) {
        return webDriverWait.waitToBeVisible(locator);
    }

    public WebsiteElement waitToBeVisible(String locator, String... varargs) {
        return webDriverWait.waitToBeVisible(locator, varargs);
    }

//    public WebElement waitToBeVisibleByXpath(String locator) {
//        return webDriverWait.until(ExpectedConditions.visibilityOf(findByXpath(locator)));
//    }
//
//    public WebElement waitToBeVisibleByCss(String locator) {
//        return webDriverWait.until(ExpectedConditions.visibilityOf(findByCss(locator)));
//    }
//
//    public WebElement waitToBeVisibleByClass(String locator) {
//        return webDriverWait.until(ExpectedConditions.visibilityOf(findByClass(locator)));
//    }
//
//    public WebElement waitToBeVisibleByID(String locator) {
//        return webDriverWait.until(ExpectedConditions.visibilityOf(findByID(locator)));
//    }
//
//    public WebElement waitToBeVisibleByName(String locator) {
//        return webDriverWait.until(ExpectedConditions.visibilityOf(findByName(locator)));
//    }
//
//    public Boolean waitToBeInvisibleByXpath(String locator) {
//        return webDriverWait.until(ExpectedConditions.invisibilityOf(findByXpath(locator)));
//    }
//
//    public Boolean waitToBeInvisibleByCss(String locator) {
//        return webDriverWait.until(ExpectedConditions.invisibilityOf(findByCss(locator)));
//    }
//
//    public Boolean waitToBeInvisibleByClass(String locator) {
//        return webDriverWait.until(ExpectedConditions.invisibilityOf(findByClass(locator)));
//    }
//
//    public Boolean waitToBeInvisibleByID(String locator) {
//        return webDriverWait.until(ExpectedConditions.invisibilityOf(findByID(locator)));
//    }
//
//    public Boolean waitToBeInvisibleByName(String locator) {
//        return webDriverWait.until(ExpectedConditions.invisibilityOf(findByName(locator)));
//    }
//
//    public WebElement waitToBeClickableByXpath(String locator) {
//        return webDriverWait.until(ExpectedConditions.elementToBeClickable(findByXpath(locator)));
//    }
//
//    public WebElement waitToBeClickableByCss(String locator) {
//        return webDriverWait.until(ExpectedConditions.elementToBeClickable(findByCss(locator)));
//    }
//
//    public WebElement waitToBeClickableByClass(String locator) {
//        return webDriverWait.until(ExpectedConditions.elementToBeClickable(findByClass(locator)));
//    }
//
//    public WebElement waitToBeClickableByID(String locator) {
//        return webDriverWait.until(ExpectedConditions.elementToBeClickable(findByID(locator)));
//    }
//
//    public WebElement waitToBeClickableByName(String locator) {
//        return webDriverWait.until(ExpectedConditions.elementToBeClickable(findByName(locator)));
//    }

    public void navigate(String applicationURL) {
        driver.get(applicationURL);
    }

    public void quit() {
        if (driver != null) {
            driver.manage().deleteAllCookies();
            driver.quit();
        }
    }

    public void clickByActions(String locator) {
        actions.click(locator);
    }

    public void clickByActions(String locator, String... varargs) {
        actions.click(locator, varargs);
    }

    public void doubleClickByActions(String locator, String... varargs) {
        actions.doubleClick(locator, varargs);
    }

    public void doubleClickByActions(String locator) {
        actions.doubleClick(locator);
    }

    public void sendKeys(String locator, Keys keys) {
        findDefaultWebElement(locator).sendKeys(keys);
    }

    public void sendKeys(String locator, String... chord) {
        findDefaultWebElement(locator).sendKeys(chord);
    }

    public void sendKeys(String locator, Keys... keys) {
        findDefaultWebElement(locator).sendKeys(keys);
    }

    public void sendKeys(String locator, Keys keys, String... varargs) {
        findDefaultWebElement(locator, varargs).sendKeys(keys);
    }

    public boolean isEnabled(String locator){
        return findElement(locator).isEnabled();
    }

//    public void clickByCss(String locator) {
//        actions.click(findByCss(locator)).perform();
//    }
//
//    public void clickByID(String locator) {
//        actions.click(findByID(locator)).perform();
//    }
//
//    public void clickByClass(String locator) {
//        actions.click(findByClass(locator)).perform();
//    }
//
//    public void clickByName(String locator) {
//        actions.click(findByName(locator)).perform();
//    }

//    public void moveToElementByXpath(String locator) {
//        actions.moveToElement(findByXpath(locator)).perform();
//    }
//
//    public void moveToElementByXpath(String locator, String... varargs) {
//        actions.moveToElement(findByXpath(locator, varargs)).perform();
//    }
//
//    public void moveToElementByCss(String locator) {
//        actions.moveToElement(findByCss(locator)).perform();
//    }
//
//    public void moveToElementByClass(String locator) {
//        actions.moveToElement(findByClass(locator)).perform();
//    }
//
//    public void moveToElementByID(String locator) {
//        actions.moveToElement(findByID(locator)).perform();
//    }
//
//    public void moveToElementByName(String locator) {
//        actions.moveToElement(findByName(locator)).perform();
//    }

//    public WebElement findByXpath(String locator) {
//        return driver.findElement(By.xpath(locator));
//    }
//
//    public WebElement findByXpath(String locator, String... varargs) {
//        return driver.findElement(By.xpath(String.format(locator, varargs)));
//    }
//
//    public WebElement findByCss(String locator) {
//        return driver.findElement(By.cssSelector(locator));
//    }
//
//    public WebElement findByID(String locator) {
//        return driver.findElement(By.id(locator));
//    }
//
//    public WebElement findByClass(String locator) {
//        return driver.findElement(By.className(locator));
//    }
//
//    public WebElement findByName(String locator) {
//        return driver.findElement(By.name(locator));
//    }
//
//    public List<WebElement> findAllByXpath(String locator) {
//        return driver.findElements(By.xpath(locator));
//    }
//
//    public List<WebElement> findAllByCss(String locator) {
//        return driver.findElements(By.cssSelector(locator));
//    }
//
//    public List<WebElement> findAllByID(String locator) {
//        return driver.findElements(By.id(locator));
//    }
//
//    public List<WebElement> findAllByName(String locator) {
//        return driver.findElements(By.name(locator));
//    }
//
//    public List<WebElement> findAllByClass(String locator) {
//        return driver.findElements(By.className(locator));
//    }
}
