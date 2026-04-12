import com.aventstack.extentreports.ExtentTest;
import org.apache.logging.log4j.Logger;
import reportConfig.ExtentTestManager;

public class DemoParent {
    protected  static ExtentTest extentTest;
    protected static Logger logger;

//    protected synchronized static void startTestLog(String testClass, String desc){
//        extentTest = ExtentTestManager.startTest(testClass + " Test Suite",
//                desc);
//    }
}
