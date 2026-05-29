package driver;

import io.appium.java_client.AppiumDriver;

/**
 * Thread-safe driver storage dung ThreadLocal.
 */
public class DriverManager {

    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();

    public static AppiumDriver getDriver() {
        return DRIVER.get();
    }

    public static void setDriver(AppiumDriver driver) {
        DRIVER.set(driver);
    }

    public static void removeDriver() {
        DRIVER.remove();
    }

    private DriverManager() {}
}