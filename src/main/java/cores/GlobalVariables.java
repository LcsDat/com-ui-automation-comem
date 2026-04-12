package cores;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GlobalVariables {
    public static final String PROJECTPATH = System.getProperty("user.dir");
    public static final String JAVA_VERSION = System.getProperty("java.version");
    public static final long LONG_TIMEOUT = 25;
    public static final long SHORT_TIMEOUT = 5;

    public static final char CHECK_ICON = (char) 10004;
    public static final char CLOCK_ICON = (char) 9200;

    public static final String ANSIReset = "\u001B[0m";
    public static final String ANSIBoldCyan = "\u001B[36;1m";
    public static final String ansiGreen = "\u001B[32m";
    public static final String ANSIRed = "\u001B[31m";
    public static final String ANSIYellow = "\u001B[33m";
    public static final String WORDOrange = "\033[38:5:208m";
    public static final String WORDReset = "\033[m";

    public static final String HASAKI_KEYWORD = "hasaki";

    public static final String FORMAT_TIME = new SimpleDateFormat("hh:mm:ss a", Locale.US).format(new Date());
}
