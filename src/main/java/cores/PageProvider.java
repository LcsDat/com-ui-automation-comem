package cores;

import pages.*;

public class PageProvider {

    public static HomePage createHomePage(BrowserDriver driver) {
        return new HomePage(driver);
    }

    public static ProductsPage createProductsPage(BrowserDriver driver) {
        return new ProductsPage(driver);
    }

    public static ProductDetailsPage createProductDetailsPage(BrowserDriver driver) {
        return new ProductDetailsPage(driver);
    }

    public static StoresLocationPage createStoresLocationPage(BrowserDriver driver) {
        return new StoresLocationPage(driver);
    }

    public static FAQPage createFAQPage(BrowserDriver driver) {
        return new FAQPage(driver);
    }

    public static PaymentPage createPaymentPage(BrowserDriver driver) {
        return new PaymentPage(driver);
    }
}
