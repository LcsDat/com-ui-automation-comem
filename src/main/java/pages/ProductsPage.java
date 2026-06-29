package pages;

import cores.BrowserDriver;
import cores.Constants;
import org.openqa.selenium.NoSuchElementException;

import java.time.Duration;

public class ProductsPage extends HomePageComponents {

    public ProductsPage(BrowserDriver driver) {
        super(driver);
    }

    private static final String PRODUCT_NAME = "//div[text()=\"%s\"]//ancestor::div[@class='ProductGridItem__itemOuter']";
    private static final String PRODUCT_NAME_2nd = "//h2[text()=\"%s\"]";

    public void chooseProduct(String productName) {
        for (String locator : new String[]{PRODUCT_NAME, PRODUCT_NAME_2nd}) {
            try {
                driver.setImplicitWait(Duration.ofSeconds(Constants.SHORT_TIMEOUT));
                driver.click(locator, productName);
                return;
            } catch (NoSuchElementException e) {
                // try next locator
            } finally {
                driver.setImplicitWait(Duration.ofSeconds(Constants.LONG_TIMEOUT));
            }
        }
    }

    public void chooseProductOnFirefox(String productName) {
        driver.doubleClick(ProductsPage.PRODUCT_NAME, productName);
    }

}
