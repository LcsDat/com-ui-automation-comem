package cores;

import org.testng.Assert;

public class LoggingAssert extends Assert {
    private String keyword;

    private String defaultTrue = "[" + Constants.CHECK_ICON + " True ]";
    private String defaultFalse = "[" + Constants.CHECK_ICON + " False]";
    private String defaultEqual = "[" + Constants.CHECK_ICON + " Equal]";
    private String pass = "PASS";
    private String fail = "FAIL";
    private String failFormat = "%c%s%12s%s%10s%S%5s" + "%s%s%7s" + "%s " + "%s " + "%s%s%s at .(%s:%d) %n";
    private String passFormat = "%c%s%12s%s%10s%S%5s" + "%s%S%7s";

    public LoggingAssert(String keyword) {
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
            System.out.printf(passFormat + "%n", Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultTrue, Constants.ANSI_RESET, Constants.ANSI_GREEN, pass, Constants.ANSI_RESET);
        } catch (Throwable e) {
            result = false;

            System.out.printf(failFormat
                    , Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultTrue, Constants.ANSI_RESET, Constants.ANSI_RED, fail, Constants.ANSI_RESET, getClassName(e), getMethodName(e), Constants.WORD_ORANGE, e.getMessage(), Constants.WORD_RESET, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
        return result;
    }

    public boolean verifyTrue(boolean condition, String message) {
        boolean result = true;
        try {
            assertTrue(condition);
            System.out.printf(passFormat + " | " + Constants.ANSI_YELLOW + message + Constants.ANSI_RESET + "%n", Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultTrue, Constants.ANSI_RESET, Constants.ANSI_GREEN, pass, Constants.ANSI_RESET);
        } catch (Throwable e) {
            result = false;
            System.out.printf(failFormat
                    , Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultTrue, Constants.ANSI_RESET, Constants.ANSI_RED, fail, Constants.ANSI_RESET, getClassName(e), getMethodName(e), Constants.WORD_ORANGE, e.getMessage(), Constants.WORD_RESET, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
        return result;
    }

    public boolean verifyFalse(boolean condition) {
        boolean result = false;
        try {
            assertFalse(condition);
            System.out.printf(passFormat + "%n", Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultFalse, Constants.ANSI_RESET, Constants.ANSI_GREEN, pass, Constants.ANSI_RESET);
        } catch (Throwable e) {
            result = true;
            System.out.printf(failFormat
                    , Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultFalse, Constants.ANSI_RESET, Constants.ANSI_RED, fail, Constants.ANSI_RESET, getClassName(e), getMethodName(e), Constants.WORD_ORANGE, e.getMessage(), Constants.WORD_RESET, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
        return result;
    }

    public boolean verifyFalse(boolean condition, String message) {
        boolean result = false;
        try {
            assertFalse(condition);
            System.out.printf(passFormat + " | " + Constants.ANSI_YELLOW + message + Constants.ANSI_RESET + "%n", Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultFalse, Constants.ANSI_RESET, Constants.ANSI_GREEN, pass, Constants.ANSI_RESET);
        } catch (Throwable e) {
            result = true;
            System.out.printf(failFormat
                    , Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultFalse, Constants.ANSI_RESET, Constants.ANSI_RED, fail, Constants.ANSI_RESET, getClassName(e), getMethodName(e), Constants.WORD_ORANGE, e.getMessage(), Constants.WORD_RESET, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
        return result;
    }

    public void verifyEquals(Object actual, Object expected) {

        try {
            assertEquals(actual, expected);
            System.out.printf(passFormat + "%n", Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultEqual, Constants.ANSI_RESET, Constants.ANSI_GREEN, pass, Constants.ANSI_RESET);
        } catch (AssertionError e) {
            System.out.printf(failFormat,
                    Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultEqual, Constants.ANSI_RESET, Constants.ANSI_RED, fail, Constants.ANSI_RESET, getClassName(e), getMethodName(e), Constants.WORD_ORANGE, e.getMessage(), Constants.WORD_RESET, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
    }

    public void verifyEquals(Object actual, Object expected, String message) {

        try {
            assertEquals(actual, expected);
            System.out.printf(passFormat + " | " + Constants.ANSI_YELLOW + message + Constants.ANSI_RESET + "%n", Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultEqual, Constants.ANSI_RESET, Constants.ANSI_GREEN, pass, Constants.ANSI_RESET);
        } catch (AssertionError e) {
            System.out.printf(failFormat,
                    Constants.CLOCK_ICON, Constants.ANSI_BOLD_CYAN, Constants.FORMAT_TIME, Constants.ANSI_RESET, Constants.ANSI_BOLD_CYAN, defaultEqual, Constants.ANSI_RESET, Constants.ANSI_RED, fail, Constants.ANSI_RESET, getClassName(e), getMethodName(e), Constants.WORD_ORANGE, e.getMessage(), Constants.WORD_RESET, getTargetStackElement(e).getFileName(), getLineNumber(e));
        }
    }

}
