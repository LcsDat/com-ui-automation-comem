import cores.Browser;
import cores.DriverFactory;
import cores.WebsiteDriver;
import org.openqa.selenium.Keys;

public class Demo1 {

    public static void main(String[] args) {

        String searchLocator = "//textarea[@title='Tìm kiếm']";

        System.out.println(System.getProperty("os.name"));
        WebsiteDriver driver = DriverFactory.initWebsiteDriver(Browser.CHROME);
        driver.navigate("http://www.google.com");
        driver.setText(searchLocator, "hello world");
        driver.sendKeys(searchLocator, Keys.chord(Keys.COMMAND, Keys.LEFT));

    }
    public void hel(){}
}
