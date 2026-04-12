package cores;

import org.testng.Assert;

public class CustomAssert extends Assert {
    private String keyword;

    private String defaultTrue = "[" + GlobalVariables.CHECK_ICON + " True ]";
    private String defaultFalse = "[" + GlobalVariables.CHECK_ICON + " False]";
    private String defaultEqual = "[" + GlobalVariables.CHECK_ICON + " Equal]";
    private String pass = "PASS";
    private String fail = "FAIL";
    private String failFormat = "%c%s%12s%s%10s%S%5s" + "%s%s%7s" + "%s " + "%s " + "%s%s%s at .(%s:%d) %n";
    private String passFormat = "%c%s%12s%s%10s%S%5s" + "%s%S%7s";

    public CustomAssert(String keyword) {
        this.keyword = keyword;
    }

    /**
     * @param e To get StackTraceElements
     * @return The target StacktraceElement
     */
    private StackTraceElement getTargetStackElement(Throwable e) {
        StackTraceElement targetStack = null;
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.toString().contains(keyword)) {
                targetStack = element;
                break;
            }
        }
        return targetStack;
    }

    private String getClassName(Throwable e) {
        return getTargetStackElement(e).getClassName();
    }

    private String getMethodName(Throwable e) {
        return getTargetStackElement(e).getMethodName();
    }

    private int getLineNumber(Throwable e) {
        return getTargetStackElement(e).getLineNumber();
    }

    public boolean verifyTrue(boolean condition) {
        boolean result = true;

        try {
            assertTrue(condition);
            System.out.printf(passFormat + "%n", GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultTrue, GlobalVariables.ANSIReset, GlobalVariables.ansiGreen, pass, GlobalVariables.ANSIReset);
        } catch (Throwable e) {
            result = false;

            System.out.printf(failFormat
                    , GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultTrue, GlobalVariables.ANSIReset, GlobalVariables.ANSIRed, fail, GlobalVariables.ANSIReset, getClassName(e), getMethodName(e), GlobalVariables.WORDOrange, e.getMessage(), GlobalVariables.WORDReset, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
        return result;
    }

    public boolean verifyTrue(boolean condition, String message) {
        boolean result = true;
        try {
            assertTrue(condition);
            System.out.printf(passFormat + " | " + GlobalVariables.ANSIYellow + message + GlobalVariables.ANSIReset + "%n", GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultTrue, GlobalVariables.ANSIReset, GlobalVariables.ansiGreen, pass, GlobalVariables.ANSIReset);
        } catch (Throwable e) {
            result = false;
            System.out.printf(failFormat
                    , GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultTrue, GlobalVariables.ANSIReset, GlobalVariables.ANSIRed, fail, GlobalVariables.ANSIReset, getClassName(e), getMethodName(e), GlobalVariables.WORDOrange, e.getMessage(), GlobalVariables.WORDReset, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
        return result;
    }

    public boolean verifyFalse(boolean condition) {
        boolean result = false;
        try {
            assertFalse(condition);
            System.out.printf(passFormat + "%n", GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultFalse, GlobalVariables.ANSIReset, GlobalVariables.ansiGreen, pass, GlobalVariables.ANSIReset);
        } catch (Throwable e) {
            result = true;
            System.out.printf(failFormat
                    , GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultFalse, GlobalVariables.ANSIReset, GlobalVariables.ANSIRed, fail, GlobalVariables.ANSIReset, getClassName(e), getMethodName(e), GlobalVariables.WORDOrange, e.getMessage(), GlobalVariables.WORDReset, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
        return result;
    }

    public boolean verifyFalse(boolean condition, String message) {
        boolean result = false;
        try {
            assertFalse(condition);
            System.out.printf(passFormat + " | " + GlobalVariables.ANSIYellow + message + GlobalVariables.ANSIReset + "%n", GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultFalse, GlobalVariables.ANSIReset, GlobalVariables.ansiGreen, pass, GlobalVariables.ANSIReset);
        } catch (Throwable e) {
            result = true;
            System.out.printf(failFormat
                    , GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultFalse, GlobalVariables.ANSIReset, GlobalVariables.ANSIRed, fail, GlobalVariables.ANSIReset, getClassName(e), getMethodName(e), GlobalVariables.WORDOrange, e.getMessage(), GlobalVariables.WORDReset, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
        return result;
    }

    public void verifyEquals(Object actual, Object expected) {

        try {
            assertEquals(actual, expected);
            System.out.printf(passFormat + "%n", GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultEqual, GlobalVariables.ANSIReset, GlobalVariables.ansiGreen, pass, GlobalVariables.ANSIReset);
        } catch (AssertionError e) {
            System.out.printf(failFormat,
                    GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultEqual, GlobalVariables.ANSIReset, GlobalVariables.ANSIRed, fail, GlobalVariables.ANSIReset, getClassName(e), getMethodName(e), GlobalVariables.WORDOrange, e.getMessage(), GlobalVariables.WORDReset, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
    }

    public void verifyEquals(Object actual, Object expected, String message) {

        try {
            assertEquals(actual, expected);
            System.out.printf(passFormat + " | " + GlobalVariables.ANSIYellow + message + GlobalVariables.ANSIReset + "%n", GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultEqual, GlobalVariables.ANSIReset, GlobalVariables.ansiGreen, pass, GlobalVariables.ANSIReset);
        } catch (AssertionError e) {
            System.out.printf(failFormat,
                    GlobalVariables.CLOCK_ICON, GlobalVariables.ANSIBoldCyan, GlobalVariables.FORMAT_TIME, GlobalVariables.ANSIReset, GlobalVariables.ANSIBoldCyan, defaultEqual, GlobalVariables.ANSIReset, GlobalVariables.ANSIRed, fail, GlobalVariables.ANSIReset, getClassName(e), getMethodName(e), GlobalVariables.WORDOrange, e.getMessage(), GlobalVariables.WORDReset, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
    }

}
