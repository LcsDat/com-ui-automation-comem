package pages;

import components.HeaderComponent;
import cores.BasePage;
import cores.BrowserDriver;

public class HomePage extends BasePage<HomePage> {

    public final HeaderComponent header;

    public HomePage(BrowserDriver driver) {
        super(driver);
        this.header = new HeaderComponent(driver);
    }


}
