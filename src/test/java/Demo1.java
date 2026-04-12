import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import cores.Browser;
import cores.DriverFactory;
import cores.GlobalVariables;
import cores.WebsiteDriver;
import org.openqa.selenium.Keys;

import java.util.Arrays;

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
