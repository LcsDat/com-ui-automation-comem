package pages;

import cores.BrowserDriver;

public class ProductsPage extends HomePageComponents {

    public ProductsPage(BrowserDriver driver) {
        super(driver);
    }

    private static final String PRODUCT_NAME = "//div[text()=\"%s\"]//ancestor::div[@class='ProductGridItem__itemOuter']";
    private static final String PRODUCT_NAME_2nd = "//h2[text()=\"%s\"]";

    public void chooseProduct(String productName) {
        clickFirstMatching(new String[]{PRODUCT_NAME, PRODUCT_NAME_2nd}, productName);
    }

    public void chooseProductOnFirefox(String productName) {
        driver.doubleClick(ProductsPage.PRODUCT_NAME, productName);
    }

}
