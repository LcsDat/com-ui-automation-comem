package pages;

import components.HeaderComponent;
import cores.BasePage;
import cores.WebsiteDriver;

public class HomePage extends BasePage<HomePage> {

    public final HeaderComponent header;

    public HomePage(WebsiteDriver driver) {
        super(driver);
        this.header = new HeaderComponent(driver);
    }


}
