package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object cho Settings screen.
 *
 * Items:
 *  - Download Folder (path display)
 *  - Languages (Device default)
 *  - Rate us
 *  - Privacy policy
 *  - Share app
 *  - Version 9999
 */
public class SettingsPage {

    private static final Logger logger = LogManager.getLogger(SettingsPage.class);

    private final AppiumDriver driver;

    // Back button co content-desc="Back"
    private final By BACK_BUTTON = AppiumBy.accessibilityId("Back");

    private final By SETTINGS_TITLE = AppiumBy.accessibilityId("Settings");

    // Download Folder co content-desc dang "Download Folder\n/storage/emulated/0/Music/Music Download"
    private final By DOWNLOAD_FOLDER_ITEM = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"Download Folder\")");

    // Languages co content-desc "Languages\nDevice"
    private final By LANGUAGES_ITEM = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"Languages\")");

    private final By RATE_US_ITEM = AppiumBy.accessibilityId("Rate us");
    private final By PRIVACY_POLICY_ITEM = AppiumBy.accessibilityId("Privacy policy");
    private final By SHARE_APP_ITEM = AppiumBy.accessibilityId("Share app");
    private final By VERSION_ITEM = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"Version\")");

    public SettingsPage(AppiumDriver driver) {
        this.driver = driver;
    }

    private boolean isDisplayed(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public boolean isOnSettingsScreen() {
        // Settings co Download Folder + Languages
        return isDisplayed(DOWNLOAD_FOLDER_ITEM) && isDisplayed(LANGUAGES_ITEM);
    }

    public boolean isDownloadFolderDisplayed() {
        return isDisplayed(DOWNLOAD_FOLDER_ITEM);
    }

    public boolean isLanguagesDisplayed() {
        return isDisplayed(LANGUAGES_ITEM);
    }

    public boolean isAllSettingsItemsDisplayed() {
        return isDownloadFolderDisplayed()
                && isLanguagesDisplayed()
                && isDisplayed(RATE_US_ITEM)
                && isDisplayed(PRIVACY_POLICY_ITEM)
                && isDisplayed(SHARE_APP_ITEM)
                && isDisplayed(VERSION_ITEM);
    }

    public String getDownloadFolderPath() {
        try {
            List<WebElement> els = driver.findElements(DOWNLOAD_FOLDER_ITEM);
            if (els.isEmpty()) return null;
            String desc = els.get(0).getAttribute("content-desc");
            // Format: "Download Folder\n/storage/..."
            if (desc != null && desc.contains("\n")) {
                return desc.split("\n", 2)[1];
            }
            return desc;
        } catch (Exception e) {
            return null;
        }
    }

    public String getLanguagesValue() {
        try {
            List<WebElement> els = driver.findElements(LANGUAGES_ITEM);
            if (els.isEmpty()) return null;
            String desc = els.get(0).getAttribute("content-desc");
            if (desc != null && desc.contains("\n")) {
                return desc.split("\n", 2)[1];
            }
            return desc;
        } catch (Exception e) {
            return null;
        }
    }

    public void clickBack() {
        logger.info("Click Settings Back button");
        try {
            driver.findElement(BACK_BUTTON).click();
        } catch (Exception e) {
            ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        }
        sleep(1500);
    }
}