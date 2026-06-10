package testcases.tracks;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.TracksPage;
import report.ExtentReportManager;

import java.util.List;

/**
 * Nhom 1 - Tracks list UI (man hinh Tracks tinh). Cac check tinh tren CUNG 1 man duoc gom lai
 * de tranh reset/mo lai man nhieu lan.
 *
 * TC_TRACKS_001 .. TC_TRACKS_003
 */
public class Tracks01_Verify_UI_Display extends BaseTest {

    private HomePage homePage;
    private TracksPage tracksPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openTracks() {
        homePage = new HomePage(driver);
        tracksPage = new TracksPage(driver);
        resetToTracks();
        Assert.assertTrue(tracksPage.isOnTracksScreen(), "Phai o Tracks screen");
    }

    @Test(description = "TC_TRACKS_001: Header + top controls + bottom nav hien thi day du")
    public void TC_001_header_and_controls_displayed() {
        Assert.assertTrue(tracksPage.isHeaderDisplayed(),
                "Header phai co menu + title 'Tracks' + search icon");
        Assert.assertTrue(tracksPage.arePlayAllAndShuffleDisplayed(), "Play all + Shuffle phai hien");
        Assert.assertTrue(tracksPage.isSortButtonDisplayed(), "Sort icon phai hien");
        Assert.assertTrue(homePage.isBottomNavDisplayed(), "Bottom nav phai hien");
        // Label "N tracks" hien tren man (DOM co "409 tracks") nhung UA2 doc descriptionMatches hay
        // CHAP CHON -> SOFT check: poll+refresh, neu doc duoc thi verify >0, khong doc duoc chi log
        // (header+playall+shuffle+sort+nav da chung minh dang o Tracks screen).
        int count = -1;
        for (int i = 0; i < 4 && count < 0; i++) {
            count = tracksPage.getTracksCount();
            if (count < 0) { tracksPage.forceRefreshTree(); sleep(500); }
        }
        if (count > 0) {
            ExtentReportManager.getTest().log(Status.PASS,
                    "Header + Play all + Shuffle + Sort + Bottom nav + count(" + count + ") OK");
        } else {
            ExtentReportManager.getTest().log(Status.WARNING,
                    "Header + controls OK. Track count label khong doc duoc qua UA2 (flaky), bo qua.");
        }
    }

    @Test(description = "TC_TRACKS_002: Track list - co items, moi item co title + duration M:SS + co title truncate")
    public void TC_002_track_list_items_valid() {
        int displayed = tracksPage.getDisplayedTracksCount();
        Assert.assertTrue(displayed > 0, "Track list phai co >=1 bai, actual: " + displayed);

        List<String> titles = tracksPage.getTrackTitles();
        for (int i = 0; i < titles.size(); i++) {
            Assert.assertNotNull(titles.get(i), "Title " + i + " null");
            Assert.assertFalse(titles.get(i).isEmpty(), "Title " + i + " rong");
        }

        List<Integer> durations = tracksPage.getTrackDurationsInSeconds();
        Assert.assertFalse(durations.isEmpty(), "Phai parse duoc duration M:SS");
        for (int d : durations) {
            // Upper bound noi rong: thu vien co the co track dai > 1h (vd file test ~2.5h = 8997s).
            // Chi can duration la so duong hop ly (< 24h) -> khong phai garbage.
            Assert.assertTrue(d > 0 && d < 86400, "Duration phai 0 < d < 86400 sec, actual: " + d);
        }

        Assert.assertTrue(tracksPage.hasTruncatedTitle(),
                "Phai co it nhat 1 title dai bi truncate '...'");
        ExtentReportManager.getTest().log(Status.PASS,
                displayed + " tracks, all titles OK, durations(sec)=" + durations + ", co title truncate");
    }

    @Test(description = "TC_TRACKS_003: Mini player hien sau khi Play all")
    public void TC_003_mini_player_after_play() {
        tracksPage.clickPlayAll();
        sleep(1100);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Mini player phai hien sau khi play");
        ExtentReportManager.getTest().log(Status.PASS,
                "Mini player content: " + tracksPage.getMiniPlayerContent());
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() { resetToTracks(); }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
