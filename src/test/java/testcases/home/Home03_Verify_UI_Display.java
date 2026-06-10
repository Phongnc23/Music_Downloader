package testcases.home;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import report.ExtentReportManager;

/**
 * Test verify cac UI element hien thi tren Home screen.
 * Tuong ung TC_HOME_01 -> TC_HOME_08.
 *
 * KHONG can reset giua test vi chi display, khong co click navigate.
 */
public class Home03_Verify_UI_Display extends BaseTest {

    private HomePage homePage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void initHomePage() {
        homePage = new HomePage(driver);
    }

    @Test(description = "TC_HOME_01: Verify Home title hien thi")
    public void TC_HOME_01_home_title_displayed() {
        ExtentReportManager.getTest().log(Status.INFO, "Verify Home title");
        Assert.assertTrue(homePage.isHomeTitleDisplayed(),
                "Home title phai hien thi tren cung");
        ExtentReportManager.getTest().log(Status.PASS, "Home title hien thi");
    }

    @Test(description = "TC_HOME_02: Verify hamburger menu icon hien thi")
    public void TC_HOME_02_menu_button_displayed() {
        Assert.assertTrue(homePage.isMenuButtonDisplayed(),
                "Menu button phai hien thi o goc tren-trai");
        ExtentReportManager.getTest().log(Status.PASS, "Menu button hien thi");
    }

    @Test(description = "TC_HOME_03: Verify search icon hien thi")
    public void TC_HOME_03_search_icon_displayed() {
        Assert.assertTrue(homePage.isSearchIconDisplayed(),
                "Search icon phai hien thi o goc tren-phai");
        ExtentReportManager.getTest().log(Status.PASS, "Search icon hien thi");
    }

    @Test(description = "TC_HOME_04: Verify search bar hien thi voi placeholder")
    public void TC_HOME_04_search_bar_displayed() {
        Assert.assertTrue(homePage.isSearchBarDisplayed(),
                "Search bar 'Search music online...' phai hien thi");
        ExtentReportManager.getTest().log(Status.PASS,
                "Search bar hien thi voi placeholder dung");
    }

    @Test(description = "TC_HOME_05: Verify 4 Quick Action buttons hien thi")
    public void TC_HOME_05_quick_actions_displayed() {
        Assert.assertTrue(homePage.isDownloadedButtonDisplayed(),
                "Button 'Downloaded' phai hien thi");
        Assert.assertTrue(homePage.isSleepTimerButtonDisplayed(),
                "Button 'Sleep timer' phai hien thi");
        Assert.assertTrue(homePage.isRateUsButtonDisplayed(),
                "Button 'Rate us' phai hien thi");
        Assert.assertTrue(homePage.isSettingsButtonDisplayed(),
                "Button 'Settings' phai hien thi");
        ExtentReportManager.getTest().log(Status.PASS, "4 Quick Action buttons hien thi");
    }

    @Test(description = "TC_HOME_06: Verify Mini Player hien thi")
    public void TC_HOME_06_mini_player_displayed() {
        Assert.assertTrue(homePage.isMiniPlayerDisplayed(),
                "Mini player phai hien thi o duoi (tren bottom nav)");
        ExtentReportManager.getTest().log(Status.PASS, "Mini player hien thi");
    }

    @Test(description = "TC_HOME_07: Verify Bottom Navigation hien thi 5 tabs")
    public void TC_HOME_07_bottom_nav_displayed() {
        Assert.assertTrue(homePage.isBottomNavDisplayed(),
                "Bottom navigation phai hien thi du 5 tab");
        ExtentReportManager.getTest().log(Status.PASS, "Bottom nav 5 tabs hien thi");
    }

    @Test(description = "TC_HOME_08: Verify Home screen tong the")
    public void TC_HOME_08_home_screen_complete() {
        Assert.assertTrue(homePage.isHomeScreenDisplayed(),
                "Home screen phai hien thi day du");
        ExtentReportManager.getTest().log(Status.PASS,
                "Home screen hien thi day du");
    }
}