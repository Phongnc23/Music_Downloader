package testcases.home;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import constants.AppConstants;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import report.ExtentReportManager;

import java.time.Duration;
import java.util.List;

/**
 * Test verify app launch + bypass quang cao thanh cong.
 * Chua test noi dung app, chi check ad da duoc tat.
 */
public class Home01_Verify_App_Launched extends BaseTest {

    @Test(description = "TC_01: Verify app launch va bypass quang cao thanh cong")
    public void TC_01_app_launch_and_bypass_ad() {
        ExtentReportManager.getTest().log(Status.INFO, "Bat dau test bypass quang cao");

        // Verify app dang foreground voi dung package
        String currentPackage = ((AndroidDriver) driver).getCurrentPackage();
        logger.info("Current package: " + currentPackage);
        ExtentReportManager.getTest().log(Status.INFO, "Package: " + currentPackage);

        Assert.assertEquals(currentPackage, AppConstants.APP_PACKAGE,
                "App phai la Music Downloader (khong stuck o ad/browser/Play Store)");

        // Verify khong con o AdActivity
        String currentActivity = ((AndroidDriver) driver).currentActivity();
        logger.info("Current activity: " + currentActivity);
        ExtentReportManager.getTest().log(Status.INFO, "Activity: " + currentActivity);

        boolean isStuckInAd = currentActivity != null && (
                currentActivity.contains("AdActivity")
                        || currentActivity.contains("com.google.android.gms.ads")
                        || currentActivity.contains("com.facebook.ads")
                        || currentActivity.contains("com.unity3d")
                        || currentActivity.contains("ATPortraitActivity")
                        || currentActivity.contains("ATLandscapeActivity")
        );

        Assert.assertFalse(isStuckInAd,
                "Khong duoc stuck o AdActivity. Current: " + currentActivity);

        // Verify HOME THAT SU da load (khong chi "khong o man ad"). App Flutter dung
        // 1 activity cho ca splash lan home, nen check activity la chua du — phai thay
        // duoc element dac trung cua home. Cho toi 15s vi home co the dang render.
        // Cac content-desc on dinh cua home: Downloaded / Search music online... /
        // Sleep timer / Settings / Tracks / Albums / Playlists.
        By[] homeMarkers = {
                AppiumBy.accessibilityId("Downloaded"),
                AppiumBy.accessibilityId("Search music online..."),
                AppiumBy.accessibilityId("Sleep timer"),
                AppiumBy.accessibilityId("Settings"),
        };
        boolean homeLoaded;
        try {
            homeLoaded = new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
                for (By marker : homeMarkers) {
                    List<?> found = d.findElements(marker);
                    if (!found.isEmpty()) return true;
                }
                return false;
            });
        } catch (Exception e) {
            homeLoaded = false;
        }
        logger.info("Home loaded: " + homeLoaded);
        ExtentReportManager.getTest().log(Status.INFO, "Home loaded: " + homeLoaded);

        Assert.assertTrue(homeLoaded,
                "Home chua load that su (khong thay element dac trung cua home - co the dang ket o splash)");

        ExtentReportManager.getTest().log(Status.PASS,
                "Bypass quang cao thanh cong - app vao HOME that su");
    }
}