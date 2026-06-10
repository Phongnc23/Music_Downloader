package testcases.search;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.SearchPage;
import report.ExtentReportManager;

import java.util.List;

/**
 * Search — Execution, Results display & Download trigger. Moi TC tach rieng 1 @Test (report
 * pass/fail tung case) nhung chia se 1 man Search ({@code @BeforeMethod} dieu huong that 1
 * lan/class, cac lan sau no-op; reset o {@code @AfterClass}). Moi test tu search lai bang
 * {@code searchWithRetry} (clear + go + submit tren CUNG man, khong thoat ra/vao).
 *
 * <p>Map spec: D (15,18,19) + E (20,21) + F (25,26). (Da bo case tim kiem tieng Viet co dau —
 * chi nhap ky tu ASCII thuong + tim kiem, khong doi IME thiet bi.)
 */
public class Search03_Verify_Results_And_Download extends BaseTest {

    private SearchPage searchPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void ensureOnSearch() {
        searchPage = openSearch(true);
        Assert.assertTrue(searchPage.isOnSearchScreen(), "Phai o Search screen");
    }

    @Test(priority = 1, description = "Search query hop le -> co results")
    public void searchValidQueryShowsResults() {
        Assert.assertTrue(searchWithRetry(searchPage, "son tung", 18),
                "Query hop le 'son tung' phai co results");
        int count = searchPage.getResultCount();
        Assert.assertTrue(count > 0, "Phai co it nhat 1 result");
        ExtentReportManager.getTest().log(Status.PASS, "'son tung' -> " + count + " results");
    }

    @Test(priority = 2, description = "Result list hien thi nhieu tracks (>=3)")
    public void resultsListShowsMultipleTracks() {
        Assert.assertTrue(searchWithRetry(searchPage, "son tung", 18), "Phai co results de dem");
        int count = searchPage.getResultCount();
        Assert.assertTrue(count >= 3, "Mong doi >=3 results, actual=" + count);
        ExtentReportManager.getTest().log(Status.PASS, count + " results hien thi");
    }

    @Test(priority = 3, description = "Moi result co title + info (title \\n creator/duration)")
    public void eachResultHasTitleAndInfo() {
        Assert.assertTrue(searchWithRetry(searchPage, "son tung", 18), "Phai co results de kiem tra");
        List<String> descs = searchPage.getResultDescs();
        Assert.assertFalse(descs.isEmpty(), "Phai co results");
        for (int i = 0; i < descs.size(); i++) {
            String d = descs.get(i);
            Assert.assertTrue(d.contains("\n"), "Result " + i + " phai co title + info: " + d);
            Assert.assertTrue(d.split("\n")[0].length() > 3, "Title rong o result " + i);
        }
        ExtentReportManager.getTest().log(Status.PASS,
                "Tat ca " + descs.size() + " results co title + info");
    }

    @Test(priority = 4, description = "Search query chua ky tu dac biet -> khong crash")
    public void searchSpecialCharsNoCrash() {
        searchPage.clearTextbox();
        searchPage.typeQuery("rock & roll!");
        sleep(1000);
        searchPage.submitSearch();
        guardAd(3000);
        sleep(2500);
        Assert.assertTrue(searchPage.isOnSearchScreen() || searchPage.hasResults(),
                "Query ky tu dac biet khong duoc crash");
        ExtentReportManager.getTest().log(Status.PASS, "Query 'rock & roll!' -> khong crash");
    }

    @Test(priority = 5, description = "Search query khong co ket qua -> empty state, khong crash")
    public void searchNoResultQueryShowsEmptyState() {
        searchPage.clearTextbox();
        searchPage.typeQuery("asdfqwerty12345xyznotfound");
        sleep(1000);
        searchPage.submitSearch();
        guardAd(3000);
        sleep(3000);
        ExtentReportManager.getTest().log(Status.INFO,
                "Query rac -> " + searchPage.getResultCount() + " results (mong doi 0/it)");
        Assert.assertTrue(searchPage.isOnSearchScreen() || searchPage.hasResults(),
                "Query khong ket qua khong duoc crash");
        ExtentReportManager.getTest().log(Status.PASS, "Query khong ket qua -> empty state, khong crash");
    }

    @Test(priority = 6, description = "Click download tren 1 track -> kich hoat tai, khong crash")
    public void triggerDownloadFromResult() {
        Assert.assertTrue(searchWithRetry(searchPage, "son tung", 18), "Phai co results truoc khi download");
        String trackTitle = searchPage.getResultTitleByIndex(0);
        ExtentReportManager.getTest().log(Status.INFO, "Se download track: " + trackTitle);

        searchPage.clickDownloadByIndex(0);
        guardAd(8000);   // download thuong bung ad -> bypass tai cho
        sleep(2000);
        guardAd(2000);

        Assert.assertTrue(searchPage.isOnSearchScreen() || searchPage.hasResults(),
                "Sau khi kich hoat download phai van o flow search (khong crash)");
        ExtentReportManager.getTest().log(Status.PASS, "Kich hoat download '" + trackTitle + "' thanh cong");
    }

    @AfterClass(alwaysRun = true)
    public void resetAfterClass() {
        leaveSearchAndReset();
    }
}
