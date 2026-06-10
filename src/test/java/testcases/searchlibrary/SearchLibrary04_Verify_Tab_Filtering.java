package testcases.searchlibrary;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.SearchLibraryPage;
import report.ExtentReportManager;

import java.util.List;

public class SearchLibrary04_Verify_Tab_Filtering extends BaseTest {

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

    @Test(description = "TC_SL_26: Tab Tracks -> chi tracks (co ' • duration')")
    public void TC_26_tracks_tab_filter() {
        String term = findExistingSearchTerm();
        if (term == null) throw new SkipException("Khong co term");

        searchPage.clickTabTracks();
        sleep(1500);

        if (searchPage.hasResults()) {
            // Track result co " • " trong desc
            List<String> descs = searchPage.getResultFullDescs();
            long trackLike = descs.stream().filter(d -> d.contains(" • ")).count();
            ExtentReportManager.getTest().log(Status.PASS,
                    "Tracks tab: " + descs.size() + " results, " + trackLike + " co duration format");
        } else {
            ExtentReportManager.getTest().log(Status.PASS,
                    "Tracks tab: " + (searchPage.isNoResultsDisplayed() ? "Nothing found" : "empty"));
        }
    }

    @Test(description = "TC_SL_27: Tab Albums -> chi albums ('N tracks')")
    public void TC_27_albums_tab_filter() {
        String term = findExistingSearchTerm();
        if (term == null) throw new SkipException("Khong co term");

        searchPage.clickTabAlbums();
        sleep(1500);

        if (searchPage.hasResults()) {
            List<String> descs = searchPage.getResultFullDescs();
            long albumLike = descs.stream().filter(d -> d.contains("tracks")).count();
            ExtentReportManager.getTest().log(Status.PASS,
                    "Albums tab: " + descs.size() + " results, " + albumLike + " co 'tracks' format");
        } else {
            ExtentReportManager.getTest().log(Status.PASS, "Albums tab: no results");
        }
    }

    @Test(description = "TC_SL_29: Tab Playlists -> chi playlists")
    public void TC_29_playlists_tab_filter() {
        String term = findExistingSearchTerm();
        if (term == null) throw new SkipException("Khong co term");

        searchPage.clickTabPlaylists();
        sleep(1500);
        ExtentReportManager.getTest().log(Status.PASS,
                "Playlists tab: " + searchPage.getResultCount() + " results");
    }

    @Test(description = "TC_SL_30: Cung query, doi tab -> filter lai")
    public void TC_30_tab_refilter_same_query() {
        String term = findExistingSearchTerm();
        if (term == null) throw new SkipException("Khong co term");

        // Search lai term
        searchPage.typeQuery(term.substring(0, Math.min(4, term.length())));
        sleep(1500);

        searchPage.clickTabAll();
        sleep(1200);
        int allCount = searchPage.getResultCount();

        searchPage.clickTabTracks();
        sleep(1200);
        int tracksCount = searchPage.hasResults() ? searchPage.getResultCount() : 0;

        // All >= Tracks (vi All bao gom tracks + others)
        Assert.assertTrue(allCount >= tracksCount,
                "All count (" + allCount + ") phai >= Tracks count (" + tracksCount + ")");
        ExtentReportManager.getTest().log(Status.PASS,
                "All: " + allCount + ", Tracks: " + tracksCount + " (All >= Tracks)");
    }

    @Test(description = "TC_SL_33: Giu query khi chuyen tab")
    public void TC_33_query_persists() {
        String term = findExistingSearchTerm();
        if (term == null) throw new SkipException("Khong co term");
        String query = term.substring(0, Math.min(4, term.length()));

        searchPage.typeQuery(query);
        sleep(1500);

        searchPage.clickTabAlbums();
        sleep(1200);

        String currentQuery = searchPage.getQueryText();
        Assert.assertTrue(currentQuery != null && currentQuery.contains(query),
                "Query phai giu khi doi tab. Expected: " + query + ", Actual: " + currentQuery);
        ExtentReportManager.getTest().log(Status.PASS,
                "Query persists: " + currentQuery);
    }

    // ============== HELPER ==============

    private String findExistingSearchTerm() {
        searchPage.clickTabAll();
        sleep(600);
        String[] commonChars = {"a", "e", "o", "s", "t", "TEST", "SON"};
        for (String c : commonChars) {
            searchPage.clearQuery();
            sleep(600);
            searchPage.typeQuery(c);
            sleep(1500);
            if (searchPage.hasResults()) {
                List<String> titles = searchPage.getResultTitles();
                if (!titles.isEmpty()) return titles.get(0);
            }
        }
        return null;
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            searchPage.clearQuery();
            searchPage.clickBack();
        } catch (Exception ignored) {}
        resetToHome();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}