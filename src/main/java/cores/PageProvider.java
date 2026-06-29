package cores;

import java.lang.reflect.InvocationTargetException;

public class PageProvider {

    public static <T extends BasePage<?>> T create(Class<T> pageClass, BrowserDriver driver) {
        try {
            return pageClass.getDeclaredConstructor(BrowserDriver.class).newInstance(driver);
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to create page: " + pageClass.getSimpleName(), e);
        }
    }
}
