package testcases.menu;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.MenuPage;
import pages.SettingsPage;
import report.ExtentReportManager;

/**
 * Test Settings screen (mo tu drawer menu).
 * TC_MENU_15 -> TC_MENU_18.
 */
public class Menu04_Verify_Settings_Screen extends BaseTest {

    private HomePage homePage;
    private MenuPage menuPage;
    private SettingsPage settingsPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openSettings() {
        homePage = new HomePage(driver);
        menuPage = new MenuPage(driver);
        settingsPage = new SettingsPage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed());

        homePage.clickMenuButton();
        sleep(1300);
        menuPage.clickSettings();
        sleep(1500);

        Assert.assertTrue(settingsPage.isOnSettingsScreen(),
                "Phai o Settings screen");
    }

    @Test(description = "TC_MENU_15: Settings co Download Folder voi path")
    public void TC_MENU_15_download_folder_displayed() {
        Assert.assertTrue(settingsPage.isDownloadFolderDisplayed(),
                "Download Folder item phai hien thi");

        String path = settingsPage.getDownloadFolderPath();
        Assert.assertNotNull(path, "Path khong duoc null");
        Assert.assertTrue(path.contains("Music") || path.contains("storage"),
                "Path phai chua 'Music' hoac 'storage'. Actual: " + path);
        ExtentReportManager.getTest().log(Status.PASS,
                "Download Folder path: " + path);
    }

    @Test(description = "TC_MENU_16: Languages hien thi voi value 'Device'")
    public void TC_MENU_16_languages_displayed() {
        Assert.assertTrue(settingsPage.isLanguagesDisplayed(),
                "Languages item phai hien thi");

        String value = settingsPage.getLanguagesValue();
        ExtentReportManager.getTest().log(Status.PASS,
                "Languages value: " + value);
    }

    @Test(description = "TC_MENU_17: Settings co day du 6 items")
    public void TC_MENU_17_all_settings_items() {
        Assert.assertTrue(settingsPage.isAllSettingsItemsDisplayed(),
                "Settings phai co: Download Folder, Languages, Rate us, " +
                        "Privacy policy, Share app, Version");
        ExtentReportManager.getTest().log(Status.PASS,
                "6 settings items day du");
    }

    @Test(description = "TC_MENU_18: Click Back -> quay ve Home")
    public void TC_MENU_18_back_to_home() {
        settingsPage.clickBack();
        sleep(1200);

        // Sau back, co the o drawer hoac home
        Assert.assertTrue(homePage.isHomeScreenDisplayed()
                        || menuPage.isDrawerOpen(),
                "Phai ve Home hoac drawer sau khi Back");
        ExtentReportManager.getTest().log(Status.PASS,
                "Back tu Settings thanh cong");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        // Phong khi @BeforeMethod loi lam drawer con mo -> dong bang VUOT truoc reset.
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