package testcases.menu;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.MenuPage;
import report.ExtentReportManager;

/**
 * Test hien thi cua Drawer Menu.
 * TC_MENU_01 -> TC_MENU_04.
 */
public class Menu01_Verify_Drawer_Display extends BaseTest {

    private HomePage homePage;
    private MenuPage menuPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openDrawer() {
        homePage = new HomePage(driver);
        menuPage = new MenuPage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed());

        homePage.clickMenuButton();
        sleep(1300);

        Assert.assertTrue(menuPage.isDrawerOpen(),
                "Drawer phai mo sau khi click hamburger");
    }

    @Test(description = "TC_MENU_01: Drawer mo va hien thi day du")
    public void TC_MENU_01_drawer_opens() {
        Assert.assertTrue(menuPage.isDrawerOpen(),
                "Drawer phai hien thi");
        ExtentReportManager.getTest().log(Status.PASS, "Drawer mo thanh cong");
    }

    @Test(description = "TC_MENU_02: Header hien thi (App name + Tagline)")
    public void TC_MENU_02_header_displayed() {
        Assert.assertTrue(menuPage.isAppNameDisplayed(),
                "App name 'Music Downloader' phai hien thi");
        Assert.assertTrue(menuPage.isAppTaglineDisplayed(),
                "Tagline 'Enjoy Listening' phai hien thi");
        ExtentReportManager.getTest().log(Status.PASS,
                "Header hien thi day du");
    }

    @Test(description = "TC_MENU_03: 9 menu items hien thi day du")
    public void TC_MENU_03_all_items_displayed() {
        Assert.assertTrue(menuPage.isAllMenuItemsDisplayed(),
                "9 menu items phai hien thi: Equalizer, Downloaded, Sleep timer, " +
                        "Privacy policy, Rate us, Share app, Settings, Version, Exit app");
        ExtentReportManager.getTest().log(Status.PASS,
                "9 menu items hien thi day du");
    }

    @Test(description = "TC_MENU_04: Version hien thi voi so 9999")
    public void TC_MENU_04_version_displayed() {
        String versionText = menuPage.getVersionText();
        Assert.assertNotNull(versionText, "Version text khong duoc null");
        Assert.assertTrue(versionText.contains("9999"),
                "Version phai chua so 9999. Actual: " + versionText);
        ExtentReportManager.getTest().log(Status.PASS,
                "Version: " + versionText);
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        // Drawer dang mo sau test -> dong bang VUOT (KHONG de resetToHome BACK, vi BACK o
        // drawer chi bung exit dialog -> resetToHome loop ~2.7 phut roi fail).
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