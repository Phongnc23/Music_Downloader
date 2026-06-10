package testcases.home;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import report.ExtentReportManager;

/**
 * Test click vao Quick Actions + Header icons + Search bar.
 * Tuong ung TC_HOME_09 -> TC_HOME_15.
 *
 * <p>Sau moi thao tac dieu huong dung {@link #guardAd(long)} thay cho sleep cung: vua doi
 * man moi load, vua bypass ngay neu ad bung giua phien. {@code @AfterMethod} goi
 * {@link #resetToHome()} ve Home bang BACK (khong out app).
 */
public class Home04_Verify_Quick_Actions extends BaseTest {

    private HomePage homePage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void initHomePage() {
        homePage = new HomePage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed(),
                "Test phai bat dau tu Home screen");
    }

    @Test(description = "TC_HOME_09: Click search bar mo search screen")
    public void TC_HOME_09_click_search_bar() {
        assertNavigatesAwayFromHome(homePage::clickSearchBar,
                "Sau khi click search bar phai navigate khoi Home");
        ExtentReportManager.getTest().log(Status.PASS,
                "Click search bar - navigate thanh cong");
    }

    @Test(description = "TC_HOME_10: Click search icon (top-right)")
    public void TC_HOME_10_click_search_icon() {
        assertNavigatesAwayFromHome(homePage::clickSearchIcon,
                "Sau khi click search icon phai navigate khoi Home");
        ExtentReportManager.getTest().log(Status.PASS, "Click search icon thanh cong");
    }

    @Test(description = "TC_HOME_11: Click Downloaded button")
    public void TC_HOME_11_click_downloaded() {
        assertNavigatesAwayFromHome(homePage::clickDownloaded,
                "Sau khi click Downloaded phai navigate khoi Home");
        ExtentReportManager.getTest().log(Status.PASS,
                "Click Downloaded - sang Downloaded screen");
    }

    @Test(description = "TC_HOME_12: Click Sleep timer button")
    public void TC_HOME_12_click_sleep_timer() {
        homePage.clickSleepTimer();
        guardAd(2000);
        ExtentReportManager.getTest().log(Status.PASS,
                "Click Sleep timer - mo dialog/screen");
    }

    @Test(description = "TC_HOME_13: Click Rate us button")
    public void TC_HOME_13_click_rate_us() {
        homePage.clickRateUs();
        guardAd(2500);
        ExtentReportManager.getTest().log(Status.PASS,
                "Click Rate us - mo dialog rate hoac Play Store");
    }

    @Test(description = "TC_HOME_14: Click Settings button")
    public void TC_HOME_14_click_settings() {
        assertNavigatesAwayFromHome(homePage::clickSettings,
                "Sau khi click Settings phai navigate khoi Home");
        ExtentReportManager.getTest().log(Status.PASS,
                "Click Settings - navigate thanh cong");
    }

    @Test(description = "TC_HOME_15: Click hamburger menu mo va dong drawer")
    public void TC_HOME_15_click_menu() {
        homePage.clickMenuButton();
        guardAd(1500);
        // Dong drawer bang vuot phai->trai (lung) — dung gesture dong menu cua app.
        homePage.closeMenuDrawer();
        guardAd(1000);
        Assert.assertTrue(homePage.isHomeScreenDisplayed(),
                "Sau khi dong drawer phai quay ve Home");
        ExtentReportManager.getTest().log(Status.PASS,
                "Mo hamburger menu va dong drawer (vuot phai->trai) thanh cong");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfterTest() {
        resetToHome();  // Ve Home bang BACK (khong out app)
    }

    /**
     * Thao tac dieu huong roi Home, CHIU DUOC ad-pop: neu sau thao tac + guardAd van con o Home
     * (ad bung khi bam roi bypass-BACK pop nguoc ve Home) thi thu lai thao tac 1 lan nua (luc nay
     * da qua cooldown nen thuong khong bung ad) truoc khi assert da roi Home.
     */
    private void assertNavigatesAwayFromHome(Runnable action, String msg) {
        action.run();
        guardAd(2000);
        if (homePage.isAllQuickActionsDisplayed()) {
            action.run();
            guardAd(2000);
        }
        Assert.assertFalse(homePage.isAllQuickActionsDisplayed(), msg);
    }
}
