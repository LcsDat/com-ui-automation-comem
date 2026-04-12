package pages;

import cores.WebsiteDriver;

public class ProductsPage extends HomeProductCommons {

    public ProductsPage(WebsiteDriver driver) {
        super(driver);
    }

    private static final String PRODUCT_NAME = "//div[text()=\"%s\"]//ancestor::div[@class='ProductGridItem__itemOuter']";
    private static final String PRODUCT_NAME_2nd = "//h2[text()=\"%s\"]";

    public void chooseProduct(String productName) {
        tryClickLocators(new String[]{PRODUCT_NAME, PRODUCT_NAME_2nd}, productName);
    }

    public void chooseProductOnFirefox(String productName) {
        driver.doubleClickByActions(ProductsPage.PRODUCT_NAME, productName);
    }

}
