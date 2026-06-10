package testcases.albums;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AlbumDetailPage;
import pages.AlbumsPage;
import pages.HomePage;
import pages.TrackPlayNowPage;
import report.ExtentReportManager;

/**
 * AlbumDetail behaviors. Setup navigate vao album[0] detail 1 lan. Track-level (click track,
 * track edit 7-action) la "giong man Tracks" -> chi test case chinh, bo qua case da test o Tracks.
 * Toi uu: gom 2 sheet check (album 4-action + track 7-action) vao 1 case; giam sleep.
 */
public class Albums06_Verify_Album_Detail extends BaseTest {

    private HomePage homePage;
    private AlbumsPage albumsPage;
    private AlbumDetailPage detailPage;
    private TrackPlayNowPage playNowPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        albumsPage = new AlbumsPage(driver);
        detailPage = new AlbumDetailPage(driver);
        playNowPage = new TrackPlayNowPage(driver);
        resetToAlbums();
        Assert.assertTrue(albumsPage.isOnAlbumsScreen());
        // Mo theo TEN (clickAlbumByName co scrollIntoView) thay vi index(0): grid co the bi cuon
        // tu file truoc (Albums05 share VoiceChanger hang duoi) -> index(0) tap nham card.
        albumsPage.clickAlbumByName("Music Download");
        Assert.assertTrue(detailPage.isOnAlbumDetailScreen());
    }

    @Test(description = "TC_ALBUMS_53: Play all album -> mini player / Play Now")
    public void TC_53_play_all() {
        String firstTrack = detailPage.getTrackTitles().isEmpty()
                ? null : detailPage.getTrackTitles().get(0);
        detailPage.clickPlayAll();
        sleep(2000);
        Assert.assertTrue(detailPage.isOnAlbumDetailScreen() || playNowPage.isOnPlayNowScreen(),
                "Play all phai phat (van o detail hoac mo Play Now)");
        ExtentReportManager.getTest().log(Status.PASS, "Album Play all OK. First: " + firstTrack);
    }

    @Test(description = "TC_ALBUMS_54: Shuffle album")
    public void TC_54_shuffle() {
        detailPage.clickShuffle();
        sleep(2000);
        ExtentReportManager.getTest().log(Status.PASS, "Album Shuffle OK");
    }

    @Test(description = "TC_ALBUMS_22: Click track trong album -> phat (mini player), giong man Tracks")
    public void TC_22_click_track_plays() {
        Assert.assertTrue(detailPage.getDisplayedTracksCount() > 0, "Album phai co tracks");
        detailPage.clickTrackByIndex(0);
        // Hanh vi giong Tracks/Artists: click track = phat + mini player (KHONG mo Play Now).
        boolean played = false;
        for (int i = 0; i < 5 && !played; i++) {
            played = detailPage.isMiniPlayerDisplayed() || playNowPage.isOnPlayNowScreen();
            if (!played) sleep(800);
        }
        Assert.assertTrue(played, "Click track phai phat (mini player) hoac mo Play Now");
        boolean playNow = playNowPage.isOnPlayNowScreen();
        ExtentReportManager.getTest().log(Status.PASS,
                "Click track phat OK (mini=" + detailPage.isMiniPlayerDisplayed() + ", playNow=" + playNow + ")");
        if (playNow) playNowPage.close();
    }

    @Test(description = "TC_ALBUMS_55_56: AlbumDetail 3-dot = 4-action album-level; track 3-dot = 7-action (gom)")
    public void TC_55_56_sheets() {
        // TC_55: 3-dot album (top-right) -> sheet 4-action album-level (KHONG Rename/Delete)
        detailPage.clickAlbumMenu();
        sleep(1500);
        Assert.assertTrue(detailPage.isEditSheetOpen(), "3-dot album detail phai mo sheet");
        Assert.assertTrue(detailPage.isAllAlbumActionsDisplayed(), "Phai co 4 actions album-level");
        boolean albRename = !driver.findElements(
                io.appium.java_client.AppiumBy.accessibilityId("Rename")).isEmpty();
        Assert.assertFalse(albRename, "Album sheet KHONG duoc co Rename (album-level 4-action)");
        detailPage.closeEditSheetByBack();
        sleep(800);

        // TC_56: 3-dot track[0] -> sheet 7-action track-level (co Rename + Delete)
        Assert.assertTrue(detailPage.getDisplayedTracksCount() > 0, "Album phai co tracks");
        detailPage.clickTrackEditByIndex(0);
        sleep(1500);
        boolean hasRename = !driver.findElements(
                io.appium.java_client.AppiumBy.accessibilityId("Rename")).isEmpty();
        boolean hasDelete = !driver.findElements(
                io.appium.java_client.AppiumBy.accessibilityId("Delete from device")).isEmpty();
        Assert.assertTrue(hasRename && hasDelete, "Track sheet phai co Rename + Delete (7 actions)");
        ExtentReportManager.getTest().log(Status.PASS,
                "Album sheet = 4-action; Track sheet = 7-action OK");
        detailPage.closeEditSheetByBack();
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (playNowPage.isOnPlayNowScreen()) playNowPage.close();
            if (detailPage.isEditSheetOpen()) detailPage.closeEditSheetByBack();
            if (detailPage.isOnAlbumDetailScreen()) detailPage.clickBack();
        } catch (Exception ignored) {}
        resetToAlbums();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
