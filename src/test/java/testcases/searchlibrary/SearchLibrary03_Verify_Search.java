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

public class SearchLibrary03_Verify_Search extends BaseTest {

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
        searchPage.clickTabAll();  // dam bao o tab All
        sleep(800);
    }

    @Test(description = "TC_SL_14: Nhap query -> ket qua hien thi")
    public void TC_14_query_shows_results() {
        String term = findExistingSearchTerm();
        if (term == null) throw new SkipException("Khong tim duoc term co ket qua");

        searchPage.typeQuery(term);
        sleep(1500);
        Assert.assertTrue(searchPage.hasResults(),
                "Query '" + term + "' phai co ket qua");
        ExtentReportManager.getTest().log(Status.PASS,
                "Query '" + term + "' -> " + searchPage.getResultCount() + " results");
    }

    @Test(description = "TC_SL_15: Xoa query -> ket qua bien mat")
    public void TC_15_clear_removes_results() {
        String term = findExistingSearchTerm();
        if (term == null) throw new SkipException("Khong tim duoc term");

        searchPage.typeQuery(term);
        sleep(1500);
        Assert.assertTrue(searchPage.hasResults());

        searchPage.clearQuery();
        sleep(1500);
        Assert.assertFalse(searchPage.hasResults(),
                "Sau clear, ket qua phai bien mat");
        ExtentReportManager.getTest().log(Status.PASS, "Clear removes results OK");
    }

    @Test(description = "TC_SL_16: Fuzzy search - tim gan dung (substring)")
    public void TC_16_fuzzy_search() {
        // Lay 1 result title, dung substring de search fuzzy
        String fullTitle = findExistingSearchTerm();
        if (fullTitle == null || fullTitle.length() < 4)
            throw new SkipException("Khong co title du dai cho fuzzy test");

        // Lay 4 ky tu dau lam substring
        String substring = fullTitle.substring(0, Math.min(4, fullTitle.length()));
        searchPage.clearQuery();
        sleep(800);
        searchPage.typeQuery(substring);
        sleep(1500);

        Assert.assertTrue(searchPage.hasResults(),
                "Fuzzy search '" + substring + "' phai co ket qua");
        // Verify ket qua chua substring
        boolean matchfound = searchPage.hasResultContaining(substring);
        Assert.assertTrue(matchfound,
                "Ket qua phai chua substring '" + substring + "'");
        ExtentReportManager.getTest().log(Status.PASS,
                "Fuzzy '" + substring + "' -> " + searchPage.getResultCount() + " results");
    }

    @Test(description = "TC_SL_17: Exact search - tim chinh xac")
    public void TC_17_exact_search() {
        String fullTitle = findExistingSearchTerm();
        if (fullTitle == null) throw new SkipException("Khong co title");

        searchPage.clearQuery();
        sleep(800);
        searchPage.typeQuery(fullTitle);
        sleep(1500);

        Assert.assertTrue(searchPage.hasResults(),
                "Exact search '" + fullTitle + "' phai co ket qua");
        Assert.assertTrue(searchPage.hasResultContaining(fullTitle),
                "Phai co ket qua khop chinh xac: " + fullTitle);
        ExtentReportManager.getTest().log(Status.PASS,
                "Exact '" + fullTitle + "' found");
    }

    @Test(description = "TC_SL_19: Case insensitive search")
    public void TC_19_case_insensitive() {
        String fullTitle = findExistingSearchTerm();
        if (fullTitle == null) throw new SkipException("Khong co title");

        String substring = fullTitle.substring(0, Math.min(4, fullTitle.length()));
        String lower = substring.toLowerCase();
        String upper = substring.toUpperCase();

        // Search lowercase
        searchPage.clearQuery();
        sleep(800);
        searchPage.typeQuery(lower);
        sleep(1500);
        int lowerCount = searchPage.getResultCount();

        // Search uppercase
        searchPage.clearQuery();
        sleep(800);
        searchPage.typeQuery(upper);
        sleep(1500);
        int upperCount = searchPage.getResultCount();

        Assert.assertEquals(lowerCount, upperCount,
                "Case insensitive: lower (" + lowerCount + ") = upper (" + upperCount + ")");
        ExtentReportManager.getTest().log(Status.PASS,
                "Case insensitive OK: " + lowerCount + " = " + upperCount);
    }

    @Test(description = "TC_SL_20: No match -> 'Nothing found!'")
    public void TC_20_no_match() {
        searchPage.typeQuery("ZZZQQQXXX999VVV");
        sleep(1500);

        Assert.assertTrue(searchPage.isNoResultsDisplayed(),
                "Query khong khop phai hien 'Nothing found!'");
        Assert.assertFalse(searchPage.hasResults(),
                "Khong duoc co result rows");
        ExtentReportManager.getTest().log(Status.PASS,
                "No match -> 'Nothing found!' OK");
    }

    @Test(description = "TC_SL_23: Search bang so")
    public void TC_23_search_number() {
        // Nhieu test data co so (TEST_RENAMED_1767, etc.)
        searchPage.typeQuery("17");
        sleep(1500);
        // Co the co hoac khong ket qua - chi log
        ExtentReportManager.getTest().log(Status.PASS,
                "Search '17': " + searchPage.getResultCount() + " results, " +
                        "noResults=" + searchPage.isNoResultsDisplayed());
    }

    // ============== HELPER ==============

    /**
     * Tim 1 search term co ket qua bang cach thu cac ky tu pho bien.
     * Tra ve title cua result dau tien.
     */
    private String findExistingSearchTerm() {
        String[] commonChars = {"a", "e", "o", "s", "t", "i", "n", "TEST", "SON"};
        for (String c : commonChars) {
            searchPage.clearQuery();
            sleep(600);
            searchPage.typeQuery(c);
            sleep(1500);
            if (searchPage.hasResults()) {
                List<String> titles = searchPage.getResultTitles();
                if (!titles.isEmpty()) {
                    String title = titles.get(0);
                    searchPage.clearQuery();
                    sleep(600);
                    return title;
                }
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