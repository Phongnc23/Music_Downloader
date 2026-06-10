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
 * Test luong Exit app tu drawer menu.
 * TC_MENU_19, TC_MENU_20.
 *
 * KHONG test click Exit button -> dong app.
 */
public class Menu05_Verify_Exit_Flow extends BaseTest {

    private HomePage homePage;
    private MenuPage menuPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openDrawerAndClickExit() {
        homePage = new HomePage(driver);
        menuPage = new MenuPage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed());

        homePage.clickMenuButton();
        sleep(1300);
        menuPage.clickExitApp();
        sleep(1200);

        // Robust: thinh thoang tap "Exit app" ban khi drawer mo chua on dinh -> dialog khong
        // bung (drawer van mo). Neu chua co exit dialog thi thu lai: con o drawer -> tap Exit app
        // lai; lo ve Home -> mo drawer roi tap lai.
        for (int attempt = 0; attempt < 2 && !menuPage.isExitDialogOpen(); attempt++) {
            logger.info("[Setup] Exit dialog chua mo -> thu lai (attempt " + (attempt + 1) + ")");
            if (!menuPage.isDrawerOpen()) {
                homePage.clickMenuButton();
                sleep(1300);
            }
            menuPage.clickExitApp();
            sleep(1200);
        }
    }

    @Test(description = "TC_MENU_19: Click Exit app -> hien Exit confirmation dialog")
    public void TC_MENU_19_exit_dialog_opens() {
        Assert.assertTrue(menuPage.isExitDialogOpen(),
                "Exit confirmation dialog phai mo voi Exit + Cancel");
        ExtentReportManager.getTest().log(Status.PASS,
                "Exit dialog hien thi 'Are you sure you want to exit?'");
    }

    @Test(description = "TC_MENU_20: Click Cancel trong Exit dialog -> dialog dong, app van mo")
    public void TC_MENU_20_cancel_closes_dialog() {
        Assert.assertTrue(menuPage.isExitDialogOpen());

        menuPage.clickExitCancel();
        sleep(1200);

        Assert.assertFalse(menuPage.isExitDialogOpen(),
                "Dialog phai dong sau khi click Cancel");
        // Verify van trong app
        String currentPkg;
        try {
            currentPkg = ((io.appium.java_client.android.AndroidDriver) driver).getCurrentPackage();
        } catch (Exception e) {
            currentPkg = "";
        }
        Assert.assertEquals(currentPkg, constants.AppConstants.APP_PACKAGE,
                "Van phai o trong app sau Cancel");
        ExtentReportManager.getTest().log(Status.PASS,
                "Cancel dong dialog, app van mo");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        // Exit dialog con mo -> Cancel; sau Cancel co the lo ve drawer -> dong drawer bang
        // VUOT (BACK o drawer chi bung lai exit dialog -> resetToHome loop lau roi fail).
        try {
            if (menuPage.isExitDialogOpen()) {
                menuPage.clickExitCancel();
                sleep(800);
            }
            if (menuPage.isDrawerOpen()) {
                menuPage.closeDrawer();
                sleep(800);
            }
        } catch (Exception e) {
            logger.warn("[Cleanup] dong exit dialog/drawer loi: " + e.getMessage());
        }
        resetToHome();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}