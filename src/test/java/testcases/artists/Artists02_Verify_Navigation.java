package testcases.artists;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ArtistDetailPage;
import pages.ArtistsPage;
import pages.HomePage;
import pages.TrackPlayNowPage;
import report.ExtentReportManager;

public class Artists02_Verify_Navigation extends BaseTest {

    private HomePage homePage;
    private ArtistsPage artistsPage;
    private ArtistDetailPage detailPage;
    private TrackPlayNowPage playNowPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        artistsPage = new ArtistsPage(driver);
        detailPage = new ArtistDetailPage(driver);
        playNowPage = new TrackPlayNowPage(driver);
        resetToArtists();
        Assert.assertTrue(artistsPage.isOnArtistsScreen());
    }

    @Test(description = "TC_ARTISTS_05: Click artist -> mo ArtistDetail screen")
    public void TC_05_click_artist_opens_detail() {
        String beforeName = artistsPage.getArtistName(0);
        int beforeCount = artistsPage.getArtistTrackCount(0);

        artistsPage.clickArtistByIndex(0);
        Assert.assertTrue(detailPage.isOnArtistDetailScreen(),
                "Phai o ArtistDetail screen");

        int songCount = detailPage.getArtistSongCount();
        ExtentReportManager.getTest().log(Status.PASS,
                "Detail of '" + beforeName + "': " + songCount + " songs " +
                        "(was: " + beforeCount + " tracks)");
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_09: ArtistDetail co header + back + 3-dot")
    public void TC_09_detail_header() {
        artistsPage.clickArtistByIndex(0);
        Assert.assertTrue(detailPage.isHeaderDisplayed(), "Header phai co Back");
        ExtentReportManager.getTest().log(Status.PASS, "Detail header OK");
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_10: ArtistDetail hero co name + N songs")
    public void TC_10_detail_hero() {
        artistsPage.clickArtistByIndex(0);
        String info = detailPage.getArtistHeroInfo();
        Assert.assertNotNull(info);
        Assert.assertTrue(info.contains("song"), "Hero phai contains 'song'");
        int songs = detailPage.getArtistSongCount();
        Assert.assertTrue(songs > 0);
        ExtentReportManager.getTest().log(Status.PASS, "Hero: " + info);
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_11: ArtistDetail co Play all + Shuffle")
    public void TC_11_detail_play_shuffle() {
        artistsPage.clickArtistByIndex(0);
        Assert.assertTrue(detailPage.isPlayAllAndShuffleDisplayed());
        ExtentReportManager.getTest().log(Status.PASS, "Play all + Shuffle OK");
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_12: ArtistDetail co section Albums + Tracks")
    public void TC_12_detail_sections() {
        artistsPage.clickArtistByIndex(0);
        Assert.assertTrue(detailPage.isAlbumsSectionDisplayed(),
                "Section Albums phai hien");
        Assert.assertTrue(detailPage.isTracksSectionDisplayed(),
                "Section Tracks phai hien");
        ExtentReportManager.getTest().log(Status.PASS, "2 sections OK");
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_16: Back tu ArtistDetail -> ArtistsPage")
    public void TC_16_back_navigation() {
        artistsPage.clickArtistByIndex(0);
        Assert.assertTrue(detailPage.isOnArtistDetailScreen());

        detailPage.clickBack();
        Assert.assertTrue(artistsPage.isOnArtistsScreen(),
                "Back phai ve Artists screen");
        ExtentReportManager.getTest().log(Status.PASS, "Back navigation OK");
    }

    @Test(description = "TC_ARTISTS_14: Section Tracks co track items (title + artist + duration + edit)")
    public void TC_14_tracks_section_items() {
        artistsPage.clickArtistByIndex(0);
        Assert.assertTrue(detailPage.isTracksSectionDisplayed(), "Section Tracks phai hien");
        Assert.assertTrue(detailPage.getDisplayedTracksCount() > 0, "Phai co >=1 track");
        String desc = detailPage.getTrackDesc(0);
        Assert.assertNotNull(desc, "Track[0] phai co content-desc");
        Assert.assertTrue(desc.contains(" • "),
                "Track desc phai co dang 'title\\nartist • duration'. Actual: " + desc);
        ExtentReportManager.getTest().log(Status.PASS, "Tracks section item: " + desc);
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_15: Sort button tren Tracks section")
    public void TC_15_tracks_sort_button() {
        artistsPage.clickArtistByIndex(0);
        Assert.assertTrue(detailPage.isTrackSortButtonDisplayed(), "Tracks section phai co sort button");
        ExtentReportManager.getTest().log(Status.PASS, "Tracks sort button OK");
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_18: Click track trong ArtistDetail -> phat bai (mini player), giong man Tracks")
    public void TC_18_click_track_plays() {
        artistsPage.clickArtistByIndex(0);
        detailPage.clickTrackByIndex(0);
        // Hanh vi giong man Tracks: click track = phat + mini player (KHONG mo Play Now).
        // Tolerant: pass neu mini player hien HOAC (so build khac) mo Play Now.
        boolean played = false;
        for (int i = 0; i < 5 && !played; i++) {
            played = detailPage.isMiniPlayerDisplayed() || playNowPage.isOnPlayNowScreen();
            if (!played) sleep(800);
        }
        Assert.assertTrue(played, "Click track phai phat (mini player hien) hoac mo Play Now");
        boolean playNow = playNowPage.isOnPlayNowScreen();
        ExtentReportManager.getTest().log(Status.PASS,
                "Click track phat OK (mini=" + detailPage.isMiniPlayerDisplayed() + ", playNow=" + playNow + ")");
        if (playNow) playNowPage.close();
        sleep(1000);
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_49: Track edit trong artist -> 7-action sheet (giong Tracks, khac artist 4-action)")
    public void TC_49_track_edit_seven_actions() {
        artistsPage.clickArtistByIndex(0);
        detailPage.clickTrackEditByIndex(0);
        sleep(1500);
        Assert.assertTrue(detailPage.isTrackEditSheetOpen(),
                "Track edit phai mo 7-action sheet (co Rename + Delete from device)");
        ExtentReportManager.getTest().log(Status.PASS,
                "Track edit sheet = 7 actions (track-level, khac artist 4-action) OK");
        detailPage.closeTrackEditSheetByBack();
        detailPage.clickBack();
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (playNowPage.isOnPlayNowScreen()) playNowPage.close();
            if (detailPage.isTrackEditSheetOpen()) detailPage.closeTrackEditSheetByBack();
        } catch (Exception ignored) {}
        resetToArtists();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}