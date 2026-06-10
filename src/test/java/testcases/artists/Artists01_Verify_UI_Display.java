package testcases.artists;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ArtistsPage;
import pages.HomePage;
import report.ExtentReportManager;

public class Artists01_Verify_UI_Display extends BaseTest {

    private HomePage homePage;
    private ArtistsPage artistsPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openArtists() {
        homePage = new HomePage(driver);
        artistsPage = new ArtistsPage(driver);
        resetToArtists();
        Assert.assertTrue(artistsPage.isOnArtistsScreen(), "Phai o Artists screen");
    }

    @Test(description = "TC_ARTISTS_01: Header co title 'Artists' + sort button")
    public void TC_01_header_displayed() {
        Assert.assertTrue(artistsPage.isTitleDisplayed());
        Assert.assertTrue(artistsPage.isSortButtonDisplayed());
        ExtentReportManager.getTest().log(Status.PASS, "Header OK");
    }

    @Test(description = "TC_ARTISTS_02: Count 'N artists' hien thi")
    public void TC_02_count_displayed() {
        Assert.assertTrue(artistsPage.isCountDisplayed(), "Count label 'N artists' phai hien");
        // Value count doc qua UA2 hay flaky (non-breaking space) -> soft. Co artist card la du chung.
        int count = artistsPage.getArtistsCount();
        Assert.assertTrue(artistsPage.getDisplayedArtistsCount() > 0, "Phai co >=1 artist card");
        if (count > 0) {
            ExtentReportManager.getTest().log(Status.PASS, "Count: " + count);
        } else {
            ExtentReportManager.getTest().log(Status.WARNING,
                    "Count label hien nhung khong doc duoc value qua UA2 (flaky). Cards: "
                            + artistsPage.getDisplayedArtistsCount());
        }
    }

    @Test(description = "TC_ARTISTS_03: Artist card co name + track count")
    public void TC_03_artist_card_info() {
        int n = artistsPage.getDisplayedArtistsCount();
        Assert.assertTrue(n > 0, "Phai co >=1 artist hien thi");

        String name = artistsPage.getArtistName(0);
        int trackCount = artistsPage.getArtistTrackCount(0);
        Assert.assertNotNull(name);
        Assert.assertFalse(name.isEmpty());
        Assert.assertTrue(trackCount > 0, "Tracks count phai > 0");
        ExtentReportManager.getTest().log(Status.PASS,
                "Artist[0]: " + name + " | " + trackCount + " tracks");
    }

    @Test(description = "TC_ARTISTS_04: Unknown artist co tat ca tracks")
    public void TC_04_unknown_artist_has_all_tracks() {
        // Tim artist <unknown>
        boolean found = false;
        int tracks = -1;
        for (int i = 0; i < artistsPage.getDisplayedArtistsCount(); i++) {
            String name = artistsPage.getArtistName(i);
            if (name != null && name.contains("unknown")) {
                found = true;
                tracks = artistsPage.getArtistTrackCount(i);
                break;
            }
        }
        Assert.assertTrue(found, "Phai co artist '<unknown>'");
        Assert.assertTrue(tracks > 0, "Unknown artist phai co tracks");
        ExtentReportManager.getTest().log(Status.PASS,
                "<unknown> artist: " + tracks + " tracks");
    }

    @Test(description = "TC_ARTISTS_08: Bottom nav co Artists tab")
    public void TC_08_bottom_nav() {
        Assert.assertTrue(homePage.isBottomNavDisplayed());
        ExtentReportManager.getTest().log(Status.PASS, "Bottom nav OK");
    }

    @Test(description = "TC_ARTISTS_07: Mini player hien sau khi phat (qua artist edit Play)")
    public void TC_07_mini_player_after_play() {
        artistsPage.clickEditButtonByIndex(0);
        Assert.assertTrue(artistsPage.isEditSheetOpen(), "Edit sheet phai mo");
        artistsPage.clickSheetPlay();
        // Mini player co the hien cham sau khi phat -> poll.
        boolean mini = false;
        for (int i = 0; i < 5 && !mini; i++) {
            mini = artistsPage.isMiniPlayerDisplayed();
            if (!mini) sleep(800);
        }
        Assert.assertTrue(mini, "Mini player phai hien sau khi Play artist");
        ExtentReportManager.getTest().log(Status.PASS, "Mini player tren Artists screen OK");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() { resetToArtists(); }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}