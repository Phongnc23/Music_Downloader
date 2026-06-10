package driver;

import constants.AppConstants;
import constants.TimeOutConstants;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Khoi tao Android driver voi UiAutomator2 options.
 * Cau hinh dac biet cho Oppo Pad Neo (ColorOS).
 */
public class AndroidDriverManager {

    private static final Logger logger = LogManager.getLogger(AndroidDriverManager.class);

    public static AndroidDriver createDriver() {
        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName(AppConstants.PLATFORM_NAME)
                .setPlatformVersion(AppConstants.PLATFORM_VERSION)
                .setDeviceName(AppConstants.DEVICE_NAME)
                .setAutomationName(AppConstants.AUTOMATION_NAME)
                .setAppPackage(AppConstants.APP_PACKAGE)
                .setAppActivity(AppConstants.APP_ACTIVITY)
                .setNoReset(true)
                .setAutoGrantPermissions(true)
                .setNewCommandTimeout(Duration.ofSeconds(TimeOutConstants.NEW_COMMAND_TIMEOUT));

        // KHONG dung unicodeKeyboard/resetKeyboard: chung doi IME thiet bi sang ban phim an
        // cua Appium, neu session crash/kill giua chung se KHONG tra lai duoc -> sau test go
        // tay khong hien ban phim. Test chi nhap ky tu ASCII (vd "son tung") nen sendKeys
        // mac dinh cua UiAutomator2 da du, khong can doi IME.

        // Capabilities cho Oppo/ColorOS
        options.setCapability("appium:ignoreHiddenApiPolicyError", true);
        options.setCapability("appium:disableWindowAnimation", true);

        // ====== CRITICAL: FIX cho video ad lam UI khong bao gio idle ======
        options.setCapability("appium:waitForIdleTimeout", 100);
        options.setCapability("appium:waitForSelectorTimeout", 5000);

        // Khong wait animation he thong
        options.setCapability("appium:settings[waitForIdleTimeout]", 100);
        options.setCapability("appium:settings[actionAcknowledgmentTimeout]", 100);
        options.setCapability("appium:settings[keyInjectionDelay]", 0);

        // Disable accessibility service suppression (UIAutomator2 bug workaround)
        options.setCapability("appium:disableSuppressAccessibilityService", true);

        // Khong require WebView neu khong dung
        options.setCapability("appium:ensureWebviewsHavePages", false);

        if (AppConstants.UDID != null && !AppConstants.UDID.isEmpty()) {
            options.setUdid(AppConstants.UDID);
        }

        try {
            logger.info("Khoi tao Android driver: " + AppConstants.APPIUM_SERVER_URL);
            AndroidDriver driver = new AndroidDriver(
                    new URL(AppConstants.APPIUM_SERVER_URL), options);
            driver.manage().timeouts().implicitlyWait(
                    Duration.ofSeconds(TimeOutConstants.IMPLICIT_WAIT));
            logger.info("Driver khoi tao thanh cong");
            return driver;
        } catch (MalformedURLException e) {
            logger.error("URL Appium server khong hop le: " + e.getMessage());
            throw new RuntimeException("Khong khoi tao duoc driver", e);
        }
    }
    private AndroidDriverManager() {}
}