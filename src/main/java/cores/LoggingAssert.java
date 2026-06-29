package cores;

import org.testng.Assert;

public class LoggingAssert extends Assert {
    private String keyword;

    private String defaultTrue = "[" + Constants.CHECK_ICON + " True ]";
    private String defaultFalse = "[" + Constants.CHECK_ICON + " False]";
    private String defaultEqual = "[" + Constants.CHECK_ICON + " Equal]";

    public LoggingAssert(String keyword) {
        this.keyword = keyword;
    }

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

    private void printPass(String label) {
        System.out.printf("%c %s%s%s %s%S%s %s%s%s%n",
                Constants.CLOCK_ICON,
                Constants.ANSI_BOLD_CYAN, Constants.formatTime(), Constants.ANSI_RESET,
                Constants.ANSI_BOLD_CYAN, label, Constants.ANSI_RESET,
                Constants.ANSI_GREEN, "PASS", Constants.ANSI_RESET);
    }

    private void printPass(String label, String message) {
        System.out.printf("%c %s%s%s %s%S%s %s%s%s | %s%s%s%n",
                Constants.CLOCK_ICON,
                Constants.ANSI_BOLD_CYAN, Constants.formatTime(), Constants.ANSI_RESET,
                Constants.ANSI_BOLD_CYAN, label, Constants.ANSI_RESET,
                Constants.ANSI_GREEN, "PASS", Constants.ANSI_RESET,
                Constants.ANSI_YELLOW, message, Constants.ANSI_RESET);
    }

    private void printFail(String label, Throwable e) {
        StackTraceElement stack = getTargetStackElement(e);
        System.out.printf("%c %s%s%s %s%S%s %s%s%s %s.%s %s%s%s at .(%s:%d)%n",
                Constants.CLOCK_ICON,
                Constants.ANSI_BOLD_CYAN, Constants.formatTime(), Constants.ANSI_RESET,
                Constants.ANSI_BOLD_CYAN, label, Constants.ANSI_RESET,
                Constants.ANSI_RED, "FAIL", Constants.ANSI_RESET,
                getClassName(e), getMethodName(e),
                Constants.WORD_ORANGE, e.getMessage(), Constants.WORD_RESET,
                stack.getFileName(), getLineNumber(e));
    }

    public boolean verifyTrue(boolean condition) {
        try {
            assertTrue(condition);
            printPass(defaultTrue);
            return true;
        } catch (Throwable e) {
            printFail(defaultTrue, e);
            return false;
        }
    }

    public boolean verifyTrue(boolean condition, String message) {
        try {
            assertTrue(condition);
            printPass(defaultTrue, message);
            return true;
        } catch (Throwable e) {
            printFail(defaultTrue, e);
            return false;
        }
    }

    public boolean verifyFalse(boolean condition) {
        try {
            assertFalse(condition);
            printPass(defaultFalse);
            return true;
        } catch (Throwable e) {
            printFail(defaultFalse, e);
            return false;
        }
    }

    public boolean verifyFalse(boolean condition, String message) {
        try {
            assertFalse(condition);
            printPass(defaultFalse, message);
            return true;
        } catch (Throwable e) {
            printFail(defaultFalse, e);
            return false;
        }
    }

    public void verifyEquals(Object actual, Object expected) {
        try {
            assertEquals(actual, expected);
            printPass(defaultEqual);
        } catch (AssertionError e) {
            printFail(defaultEqual, e);
        }
    }

    public void verifyEquals(Object actual, Object expected, String message) {
        try {
            assertEquals(actual, expected);
            printPass(defaultEqual, message);
        } catch (AssertionError e) {
            printFail(defaultEqual, e);
        }
    }
}
