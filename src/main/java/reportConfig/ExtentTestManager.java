package reportConfig;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.model.Media;

import java.util.HashMap;
import java.util.Map;

public class ExtentTestManager {

    private static ExtentTestManager extentTestManager;
    private static final Map<String, ExtentTest> extentTestMap = new HashMap<>();

    public String getTestClass() {
        return testClass;
    }

    private String testClass;

    public static ExtentTestManager init() {
        extentTestManager = new ExtentTestManager();
        return extentTestManager;

    }

    public ExtentTest getExtentTest() {
        return extentTestMap.get(testClass);
    }

    public ExtentTest getExtentTest(String testClass) {
        return extentTestMap.get(testClass);
    }

    public Map<String, ExtentTest> getMap() {
        return extentTestMap;
    }

//    public synchronized ExtentTest startTest(String testName, String desc) {
//        var extentTest = ExtentManager.init().createTest(testName, desc);
//        testClass = testName;
//        extentTestMap.put(testClass, extentTest);
//        System.out.println("map value: " + extentTestMap);
//        return extentTest;
//    }


}
