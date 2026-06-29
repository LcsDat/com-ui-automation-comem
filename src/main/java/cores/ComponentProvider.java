package cores;

import components.DatePickerComponent;
import components.DropdownComponent;
import components.HeaderComponent;
import components.TableComponent;

public class ComponentProvider {

    private final HeaderComponent     header;
    private final DropdownComponent   dropdown;
    private final TableComponent      table;
    private final DatePickerComponent datePicker;

    public ComponentProvider(BrowserDriver driver) {
        this.header     = new HeaderComponent(driver);
        this.dropdown   = new DropdownComponent(driver);
        this.table      = new TableComponent(driver);
        this.datePicker = new DatePickerComponent(driver);
    }

    public HeaderComponent header()         { return header; }
    public DropdownComponent dropdown()     { return dropdown; }
    public TableComponent table()           { return table; }
    public DatePickerComponent datePicker() { return datePicker; }
}