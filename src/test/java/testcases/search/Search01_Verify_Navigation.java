package testcases.search;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.SearchPage;
import report.ExtentReportManager;

/**
 * Search — Navigation & Keyboard. Cac case dieu huong (Home<->Search) tach rieng tung @Test
 * de report pass/fail tung case; moi test bat dau tu Home va reset ve Home sau khi xong
 * (navigation can chuyen man nen khong chia se man duoc).
 *
 * <p>Map spec A (01,03,04) + B (02,05,06) + I (43,45 ad bung khi mo/back).
 */
public class Search01_Verify_Navigation extends BaseTest {

    private HomePage homePage;
    private SearchPage searchPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void initPages() {
        homePage = new HomePage(driver);
        searchPage = new SearchPage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed(), "Test phai bat dau tu Home screen");
    }

    @Test(priority = 1, description = "Click search bar -> navigate sang Search screen")
    public void navigateToSearchViaSearchBar() {
        homePage.clickSearchBar();
        guardAd(3000);
        Assert.assertTrue(searchPage.waitForSearchScreen(5),
                "Click search bar phai sang Search screen");
        ExtentReportManager.getTest().log(Status.PASS, "Search bar -> Search screen");
    }

    @Test(priority = 2, description = "Vao Search -> ban phim hien de nhap "
            + "(no-ad tu hien; co-ad-bypass thi tap textbox lan nua)")
    public void keyboardShowsOnSearchScreen() {
        homePage.clickSearchBar();
        guardAd(3000);
        Assert.assertTrue(searchPage.waitForSearchScreen(5), "Phai o Search screen");
        Assert.assertTrue(searchPage.ensureKeyboardShown(),
                "Ban phim ao phai hien de nhap");
        ExtentReportManager.getTest().log(Status.PASS, "Ban phim ao hien thi");
    }

    @Test(priority = 3, description = "Click search icon (top-right) -> navigate sang Search screen")
    public void navigateToSearchViaSearchIcon() {
        homePage.clickSearchIcon();
        guardAd(3000);
        Assert.assertTrue(searchPage.waitForSearchScreen(5),
                "Click search icon phai sang Search screen");
        ExtentReportManager.getTest().log(Status.PASS, "Search icon -> Search screen");
    }

    @Test(priority = 4, description = "BACK tu Search -> an ban phim roi ve Home")
    public void backFromSearchReturnsHome() {
        homePage.clickSearchBar();
        guardAd(3000);
        Assert.assertTrue(searchPage.waitForSearchScreen(5), "Phai o Search truoc khi back");
        searchPage.ensureKeyboardShown();

        // clickBack tu lap 2 back (lan 1 an ban phim, lan 2 ve Home).
        searchPage.clickBack();
        guardAd(1500);
        Assert.assertTrue(homePage.isHomeScreenDisplayed(), "BACK tu Search phai ve Home");
        ExtentReportManager.getTest().log(Status.PASS, "BACK -> ve Home");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        leaveSearchAndReset();
    }
}
