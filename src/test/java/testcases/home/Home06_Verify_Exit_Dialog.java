package testcases.home;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import constants.AppConstants;
import io.appium.java_client.android.AndroidDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import report.ExtentReportManager;

/**
 * Test luong EXIT app: nhan BACK o Home se bung dialog xac nhan thoat
 * ("Are you sure you want to exit?") voi 2 nut Exit / Cancel (kem 1 native ad).
 * Tuong ung TC_HOME_26 -> TC_HOME_28.
 *
 * @AfterMethod goi resetToHome() — neu app da thoat (sau khi bam Exit) thi
 * resetToHome se cold restart de test tiep theo van co app o Home.
 */
public class Home06_Verify_Exit_Dialog extends BaseTest {

    private HomePage homePage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void initHomePage() {
        homePage = new HomePage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed(),
                "Test phai bat dau tu Home screen");
    }

    @Test(description = "TC_HOME_26: Nhan BACK o Home -> bung dialog xac nhan thoat")
    public void TC_HOME_26_back_shows_exit_dialog() {
        homePage.pressBack();
        sleep(1500);
        Assert.assertTrue(homePage.isExitDialogDisplayed(),
                "Nhan BACK o Home phai bung dialog 'Are you sure you want to exit?'");
        ExtentReportManager.getTest().log(Status.PASS,
                "BACK o Home -> exit dialog hien thi (Exit/Cancel)");
    }

    @Test(description = "TC_HOME_27: Bam Cancel tren exit dialog -> o lai Home")
    public void TC_HOME_27_cancel_stays_in_app() {
        homePage.pressBack();
        sleep(1500);
        Assert.assertTrue(homePage.isExitDialogDisplayed(),
                "Exit dialog phai hien truoc khi bam Cancel");

        homePage.clickExitCancel();
        sleep(1500);

        Assert.assertFalse(homePage.isExitDialogDisplayed(),
                "Sau khi bam Cancel, exit dialog phai dong");
        Assert.assertTrue(homePage.isHomeScreenDisplayed(),
                "Bam Cancel phai o lai Home, khong thoat app");
        ExtentReportManager.getTest().log(Status.PASS,
                "Bam Cancel -> dialog dong, van o Home");
    }

    @Test(description = "TC_HOME_28: Bam Exit tren exit dialog -> thoat app")
    public void TC_HOME_28_exit_closes_app() {
        homePage.pressBack();
        sleep(1500);
        Assert.assertTrue(homePage.isExitDialogDisplayed(),
                "Exit dialog phai hien truoc khi bam Exit");

        homePage.clickExitConfirm();
        sleep(2500);

        // Sau khi bam Exit, app khong con o foreground (package doi sang launcher/khac).
        String pkg = ((AndroidDriver) driver).getCurrentPackage();
        Assert.assertNotEquals(pkg, AppConstants.APP_PACKAGE,
                "Bam Exit phai thoat app, nhung van o package: " + pkg);
        ExtentReportManager.getTest().log(Status.PASS,
                "Bam Exit -> app thoat, foreground package = " + pkg);
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfterTest() {
        // TC_26 de lai exit dialog dang mo -> resetToHome bam Cancel ve Home.
        // TC_28 app da thoat -> resetToHome dua MainActivity tro lai (startActivity).
        resetToHome();
    }
}
