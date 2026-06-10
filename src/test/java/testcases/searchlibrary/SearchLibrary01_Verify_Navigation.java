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

public class SearchLibrary01_Verify_Navigation extends BaseTest {

    private HomePage homePage;
    private SearchLibraryPage searchPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        searchPage = new SearchLibraryPage(driver);
        resetToHome();   // app co the cold-start/khoi phuc vao man search -> dam bao ve Home truoc
        Assert.assertTrue(homePage.isHomeScreenDisplayed(), "Phai bat dau tu Home");
    }

    @Test(description = "TC_SL_01: Tap search icon goc phai -> mo Search screen")
    public void TC_01_open_search() {
        searchPage.openFromHomeSearchIcon();
        Assert.assertTrue(searchPage.isOnSearchScreen(),
                "Phai mo Search In Library screen");
        ExtentReportManager.getTest().log(Status.PASS, "Search screen mo OK");
    }

    @Test(description = "TC_SL_02: Search screen co EditText + 5 tabs")
    public void TC_02_search_ui() {
        searchPage.openFromHomeSearchIcon();
        Assert.assertTrue(searchPage.isSearchInputDisplayed(), "Phai co EditText");
        Assert.assertTrue(searchPage.areAllTabsDisplayed(),
                "Phai co 5 tabs: All/Tracks/Albums/Artists/Playlists");
        ExtentReportManager.getTest().log(Status.PASS, "EditText + 5 tabs OK");
    }

    @Test(description = "TC_SL_04: Tap Back -> ve Home")
    public void TC_04_back_to_home() {
        searchPage.openFromHomeSearchIcon();
        Assert.assertTrue(searchPage.isOnSearchScreen());

        searchPage.clickBack();
        sleep(1500);
        Assert.assertTrue(homePage.isHomeScreenDisplayed(),
                "Back phai ve Home");
        ExtentReportManager.getTest().log(Status.PASS, "Back to Home OK");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() { resetToHome(); }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}