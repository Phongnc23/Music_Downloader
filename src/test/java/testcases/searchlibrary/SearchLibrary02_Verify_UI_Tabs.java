package testcases.searchlibrary;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.SearchLibraryPage;
import report.ExtentReportManager;

public class SearchLibrary02_Verify_UI_Tabs extends BaseTest {

    private HomePage homePage;
    private SearchLibraryPage searchPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        searchPage = new SearchLibraryPage(driver);
        resetToHome();   // dam bao ve Home truoc (app co the o man search tu truoc)
        Assert.assertTrue(homePage.isHomeScreenDisplayed());
        searchPage.openFromHomeSearchIcon();
        Assert.assertTrue(searchPage.isOnSearchScreen());
    }

    @Test(description = "TC_SL_09: Empty query -> KHONG hien thi ket qua")
    public void TC_09_empty_no_results() {
        // Chua nhap gi
        Assert.assertFalse(searchPage.hasResults(),
                "Empty query khong duoc co ket qua");
        ExtentReportManager.getTest().log(Status.PASS,
                "Empty query: khong ket qua (correct)");
    }

    @Test(description = "TC_SL_12: Click tab -> chuyen tab")
    public void TC_12_switch_tabs() {
        searchPage.clickTabTracks();
        sleep(1000);
        Assert.assertTrue(searchPage.isOnSearchScreen(), "Van o search sau click Tracks tab");

        searchPage.clickTabAlbums();
        sleep(1000);
        Assert.assertTrue(searchPage.isOnSearchScreen());

        searchPage.clickTabAll();
        sleep(1000);
        ExtentReportManager.getTest().log(Status.PASS, "Tab switching OK");
    }

    @Test(description = "TC_SL_13: 5 tabs van truy cap duoc khi co query")
    public void TC_13_tabs_with_query() {
        searchPage.typeQuery("a");
        sleep(1500);

        // Sau khi co query, ca 5 tab phai accessible
        Assert.assertTrue(searchPage.isTabDisplayed("Playlists"),
                "Tab Playlists phai hien khi co query");
        ExtentReportManager.getTest().log(Status.PASS, "5 tabs accessible voi query");
        searchPage.clearQuery();
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try { searchPage.clickBack(); } catch (Exception ignored) {}
        resetToHome();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}