package components;

import cores.BaseComponent;
import cores.BrowserDriver;
import io.qameta.allure.Step;

public class DropdownComponent extends BaseComponent<DropdownComponent> {

    private static final String OPTION_BY_TEXT = "%s//li[normalize-space()='%s'] | %s//option[normalize-space()='%s']";
    private static final String SELECTED_LABEL = "%s//*[contains(@class,'selected')] | %s//option[@selected]";
    private static final String ALL_OPTIONS    = "%s//li[@role='option'] | %s//option";

    public DropdownComponent(BrowserDriver driver) {
        super(driver);
    }

    @Step("Select option '{text}'")
    public DropdownComponent selectByText(String locator, String text) {
        click(locator);
        click(String.format(OPTION_BY_TEXT, locator, text, locator, text));
        return self();
    }

    @Step("Get selected option text")
    public String getSelectedText(String locator) {
        return getText(String.format(SELECTED_LABEL, locator, locator));
    }

    @Step("Check if option '{text}' is present")
    public boolean isOptionPresent(String locator, String text) {
        return isDisplayed(String.format(OPTION_BY_TEXT, locator, text, locator, text));
    }

    @Step("Get total number of options")
    public int getOptionCount(String locator) {
        return getElements(String.format(ALL_OPTIONS, locator, locator)).size();
    }
}
