package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class LocaleManager {

    private static final ThreadLocal<String> currentLocale = ThreadLocal.withInitial(() ->
            System.getProperty("test.locale", "vi"));

    private static final Map<String, Properties> cache = new ConcurrentHashMap<>();

    private LocaleManager() {}

    public static void setLocale(String locale) {
        currentLocale.set(locale);
    }

    public static String getLocale() {
        return currentLocale.get();
    }

    public static String get(String key) {
        return get(key, currentLocale.get());
    }

    public static String get(String key, String locale) {
        Properties props = cache.computeIfAbsent(locale, LocaleManager::loadProperties);
        String value = props.getProperty(key);
        if (value == null) {
            System.out.println("[LocaleManager] Key not found: '" + key + "' in locale '" + locale + "'");
            return key;
        }
        return value;
    }

    private static Properties loadProperties(String locale) {
        Properties props = new Properties();
        String path = "locales/" + locale + ".properties";
        try (InputStream is = LocaleManager.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                System.out.println("[LocaleManager] Locale file not found: " + path);
                return props;
            }
            props.load(is);
        } catch (IOException e) {
            System.out.println("[LocaleManager] Failed to load locale: " + path + " — " + e.getMessage());
        }
        return props;
    }
}
