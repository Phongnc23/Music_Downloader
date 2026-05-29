package driver;

import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory pattern de tao driver theo platform.
 */
public class DriverFactory {

    private static final Logger logger = LogManager.getLogger(DriverFactory.class);

    public static AppiumDriver createDriver(String platform) {
        if (platform == null || platform.isEmpty()) {
            platform = "Android";
        }

        switch (platform.toLowerCase()) {
            case "android":
                logger.info("Tao Android driver");
                return AndroidDriverManager.createDriver();
            default:
                throw new IllegalArgumentException("Platform khong support: " + platform);
        }
    }

    public static AppiumDriver createDriver() {
        return createDriver("Android");
    }

    private DriverFactory() {}
}