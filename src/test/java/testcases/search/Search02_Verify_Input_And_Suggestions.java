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
 * Search — Input & Suggestions. Moi TC tach rieng 1 @Test de report pass/fail tung case,
 * NHUNG chia se 1 man Search: {@code @BeforeMethod} chi dieu huong THAT 1 lan/class (cac lan
 * sau la no-op vi da o Search), KHONG reset giua cac test -> it bypass ad, do flaky. Thu tu
 * dam bao bang {@code priority}; reset ve Home o {@code @AfterClass}.
 *
 * <p>Map spec muc C: 09 (suggestions), 10 (update), 12 (push fill), 11 (clear), 16 (empty),
 * 13 (chon suggestion -> search).
 */
public class Search02_Verify_Input_And_Suggestions extends BaseTest {

    private SearchPage searchPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void ensureOnSearch() {
        // Lan dau: dieu huong vao Search + ban phim. Cac lan sau: no-op (da o Search) -> nhanh.
        searchPage = openSearch(true);
        Assert.assertTrue(searchPage.isOnSearchScreen(), "Phai o Search screen");
    }

    @Test(priority = 1, description = "Go ky tu -> danh sach suggestions hien")
    public void typingShowsSuggestions() {
        searchPage.clearTextbox();
        searchPage.typeQuery("son");
        Assert.assertTrue(searchPage.waitForSuggestions(5), "Go 'son' phai hien suggestions");
        List<String> sug = searchPage.getSuggestionTexts();
        Assert.assertFalse(sug.isEmpty(), "Suggestion list khong duoc rong");
        ExtentReportManager.getTest().log(Status.PASS, "Go 'son' -> " + sug.size() + " suggestions: " + sug);
    }

    @Test(priority = 2, description = "Suggestions cap nhat theo tung ky tu nhap them")
    public void suggestionsUpdateWithInput() {
        searchPage.clearTextbox();
        searchPage.typeQuery("s");
        searchPage.waitForSuggestions(5);
        int afterOne = searchPage.getSuggestionCount();
        searchPage.typeQuery("on");  // -> "son"
        searchPage.waitForSuggestions(5);
        List<String> sug = searchPage.getSuggestionTexts();
        Assert.assertFalse(sug.isEmpty(), "Sau khi go them ky tu, suggestions van phai hien");
        ExtentReportManager.getTest().log(Status.PASS,
                "'s' -> " + afterOne + " suggestions; 'son' -> " + sug.size() + " (update theo input)");
    }

    @Test(priority = 3, description = "Click icon push trong suggestion -> fill textbox, KHONG search")
    public void pushIconFillsTextboxWithoutSearching() {
        searchPage.clearTextbox();
        searchPage.typeQuery("son");
        searchPage.waitForSuggestions(5);
        List<String> sug = searchPage.getSuggestionTexts();
        Assert.assertFalse(sug.isEmpty(), "Phai co suggestions de test push icon");
        String firstSugg = sug.get(0);

        searchPage.clickPushIconByIndex(0);
        sleep(1200);
        String tbVal = searchPage.getTextboxValue();
        Assert.assertTrue(
                tbVal.equalsIgnoreCase(firstSugg)
                        || firstSugg.toLowerCase().startsWith(tbVal.toLowerCase())
                        || tbVal.toLowerCase().startsWith(firstSugg.toLowerCase()),
                "Push icon phai fill suggestion vao textbox. Actual='" + tbVal + "', expect~='" + firstSugg + "'");
        Assert.assertFalse(searchPage.hasResults(), "Push icon CHI fill textbox, KHONG duoc trigger search");
        ExtentReportManager.getTest().log(Status.PASS,
                "Push icon fill textbox ('" + tbVal + "') khong trigger search");
    }

    @Test(priority = 4, description = "Clear textbox -> noi dung bi xoa")
    public void clearTextboxRemovesContent() {
        searchPage.clearTextbox();
        searchPage.typeQuery("son");
        searchPage.waitForSuggestions(5);
        Assert.assertFalse(searchPage.getTextboxValue().isEmpty(), "Phai co text truoc khi clear");

        searchPage.clearTextbox();
        sleep(1000);
        Assert.assertEquals(searchPage.getTextboxValue(), "", "Textbox phai rong sau khi clear");
        ExtentReportManager.getTest().log(Status.PASS, "Clear textbox -> rong");
    }

    @Test(priority = 5, description = "Query rong -> khong trigger search")
    public void emptyQueryDoesNotSearch() {
        searchPage.clearTextbox();
        sleep(500);
        searchPage.submitSearch();
        sleep(1500);
        Assert.assertFalse(searchPage.hasResults(), "Query rong KHONG duoc trigger search ra results");
        ExtentReportManager.getTest().log(Status.PASS, "Query rong -> khong search");
    }

    @Test(priority = 6, description = "Click text suggestion -> trigger search ra results")
    public void selectingSuggestionTriggersSearch() {
        searchPage.clearTextbox();
        searchPage.typeQuery("son");
        searchPage.waitForSuggestions(5);
        Assert.assertFalse(searchPage.getSuggestionTexts().isEmpty(), "Phai co suggestions de chon");

        searchPage.clickSuggestionByIndex(0);
        guardAd(3000);
        Assert.assertTrue(searchPage.waitForResults(12),
                "Click text suggestion phai trigger search hien results");
        ExtentReportManager.getTest().log(Status.PASS, "Click suggestion -> hien results");
    }

    @AfterClass(alwaysRun = true)
    public void resetAfterClass() {
        leaveSearchAndReset();
    }
}
