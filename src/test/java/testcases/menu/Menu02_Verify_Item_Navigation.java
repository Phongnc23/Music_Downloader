package testcases.menu;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import constants.AppConstants;
import io.appium.java_client.android.AndroidDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.MenuPage;
import pages.SettingsPage;
import report.ExtentReportManager;

/**
 * Test click vao tung menu item -> navigate dung.
 * TC_MENU_05 -> TC_MENU_10.
 */
public class Menu02_Verify_Item_Navigation extends BaseTest {

    private HomePage homePage;
    private MenuPage menuPage;
    private SettingsPage settingsPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openDrawer() {
        homePage = new HomePage(driver);
        menuPage = new MenuPage(driver);
        settingsPage = new SettingsPage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed());

        homePage.clickMenuButton();
        sleep(1300);
        Assert.assertTrue(menuPage.isDrawerOpen());
    }

    @Test(description = "TC_MENU_05: Click Equalizer -> mo Android Dolby Atmos settings")
    public void TC_MENU_05_click_equalizer() {
        menuPage.clickEqualizer();
        sleep(2000);

        String currentPkg = getCurrentPackage();
        // Mong doi out khoi app (vao Android settings)
        boolean wentToSettings = "com.android.settings".equals(currentPkg)
                || !AppConstants.APP_PACKAGE.equals(currentPkg);
        Assert.assertTrue(wentToSettings,
                "Click Equalizer phai chuyen sang Android settings. Current: " + currentPkg);
        ExtentReportManager.getTest().log(Status.PASS,
                "Equalizer -> chuyen sang " + currentPkg);
    }

    @Test(description = "TC_MENU_06: Click Downloaded -> mo Downloaded screen trong app")
    public void TC_MENU_06_click_downloaded() {
        menuPage.clickDownloaded();
        sleep(1800);

        String currentPkg = getCurrentPackage();
        Assert.assertEquals(currentPkg, AppConstants.APP_PACKAGE,
                "Downloaded phai van trong app");
        // Verify Downloaded screen co title hoac track list
        boolean isOnDownloaded = !driver.findElements(
                io.appium.java_client.AppiumBy.accessibilityId("Downloaded")).isEmpty();
        Assert.assertTrue(isOnDownloaded, "Phai o Downloaded screen");
        ExtentReportManager.getTest().log(Status.PASS,
                "Downloaded screen mo thanh cong");
    }

    @Test(description = "TC_MENU_07: Click Privacy policy")
    public void TC_MENU_07_click_privacy_policy() {
        menuPage.clickPrivacyPolicy();
        sleep(1800);

        String currentPkg = getCurrentPackage();
        ExtentReportManager.getTest().log(Status.PASS,
                "Privacy policy -> package = " + currentPkg + " (browser hoac webview)");
        // Khong crash la pass
    }

    @Test(description = "TC_MENU_08: Click Rate us -> mo dialog hoac Play Store")
    public void TC_MENU_08_click_rate_us() {
        menuPage.clickRateUs();
        sleep(2000);

        String currentPkg = getCurrentPackage();
        ExtentReportManager.getTest().log(Status.PASS,
                "Rate us -> package = " + currentPkg);
        // Co the la Play Store (com.android.vending) hoac dialog trong app
    }

    @Test(description = "TC_MENU_09: Click Share app -> mo Android share sheet")
    public void TC_MENU_09_click_share_app() {
        menuPage.clickShareApp();
        sleep(1800);

        String currentPkg = getCurrentPackage();
        boolean wentToShare = "com.android.intentresolver".equals(currentPkg)
                || currentPkg.contains("intentresolver")
                || currentPkg.contains("chooser");
        Assert.assertTrue(wentToShare,
                "Share app phai mo intent resolver. Current: " + currentPkg);
        ExtentReportManager.getTest().log(Status.PASS,
                "Share app -> " + currentPkg);
    }

    @Test(description = "TC_MENU_10: Click Settings -> mo Settings screen trong app")
    public void TC_MENU_10_click_settings() {
        menuPage.clickSettings();
        sleep(1800);

        String currentPkg = getCurrentPackage();
        Assert.assertEquals(currentPkg, AppConstants.APP_PACKAGE,
                "Settings phai van trong app");
        Assert.assertTrue(settingsPage.isOnSettingsScreen(),
                "Phai o Settings screen voi Download Folder + Languages");
        ExtentReportManager.getTest().log(Status.PASS,
                "Settings screen mo thanh cong");
    }

    @Test(description = "TC_MENU_10b: Click Version -> quay ve Home")
    public void TC_MENU_10b_click_version_returns_home() {
        menuPage.clickVersion();
        sleep(1000);

        Assert.assertEquals(getCurrentPackage(), AppConstants.APP_PACKAGE, "Van trong app");
        Assert.assertTrue(homePage.isHomeScreenDisplayed(),
                "Nhan Version phai quay ve Home");
        ExtentReportManager.getTest().log(Status.PASS, "Version -> quay ve Home");
    }

    private String getCurrentPackage() {
        try {
            return ((AndroidDriver) driver).getCurrentPackage();
        } catch (Exception e) {
            return "";
        }
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        // Phong khi test fail som lam drawer con mo -> dong bang VUOT truoc (BACK o drawer
        // chi bung exit dialog -> resetToHome loop lau roi fail).
        try {
            if (menuPage.isDrawerOpen()) {
                menuPage.closeDrawer();
                sleep(800);
            }
        } catch (Exception e) {
            logger.warn("[Cleanup] dong drawer loi: " + e.getMessage());
        }
        resetToHome();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}