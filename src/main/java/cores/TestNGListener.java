package cores;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

public class TestNGListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String className = result.getMethod().getTestClass().getName();
        String failureMessage = result.getThrowable().getMessage();

        System.out.println("Test failed: " + className + "." + methodName + " - " + failureMessage);

        // Store the method name in Reporter for later retrieval
//        Reporter.setCurrentTestResult(result);
    }
}
