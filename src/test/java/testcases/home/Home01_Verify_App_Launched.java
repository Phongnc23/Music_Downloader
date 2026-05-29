package testcases.home;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import constants.AppConstants;
import io.appium.java_client.android.AndroidDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import report.ExtentReportManager;

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

        ExtentReportManager.getTest().log(Status.PASS,
                "Bypass quang cao thanh cong - app o MainActivity");
    }
}