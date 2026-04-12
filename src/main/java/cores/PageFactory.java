package cores;

import pages.*;

public class PageFactory {

    public static HomePage generateHomePage(WebsiteDriver driver) {
        return new HomePage(driver);
    }

    public static CartPage generateCartPage(WebsiteDriver driver) {
        return new CartPage(driver);
    }

    public static ProductsPage generateProductsPage(WebsiteDriver driver) {
        return new ProductsPage(driver);
    }

    public static ProductDetailsPage generateProductDetailsPage(WebsiteDriver  driver){
        return new ProductDetailsPage(driver);
    }

    public static StoresLocationPage generateStoresLocationPage(WebsiteDriver  driver){
        return new StoresLocationPage(driver);
    }
    public static FAQPage generateFAQPage(WebsiteDriver  driver){
        return new FAQPage(driver);
    }

    public static PaymentPage generatePaymentPage(WebsiteDriver driver) {
        return new PaymentPage(driver);
    }
}
