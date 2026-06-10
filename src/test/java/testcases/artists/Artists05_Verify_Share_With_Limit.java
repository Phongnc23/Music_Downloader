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
import report.ExtentReportManager;

/**
 * Test SPECIAL: Share track logic voi gioi han 10 songs.
 *
 * Rule:
 *  - Folder/artist <= 10 songs: Share -> mo Android share intent resolver
 *  - Folder/artist > 10 songs: Share -> sheet dong + toast/notification
 *
 * Sample data:
 *  - <unknown> artist: 382 songs -> CANNOT
 *  - Music Download: 31 songs -> CANNOT
 *  - VoiceChanger: 341 songs -> CANNOT
 *  - BrowserDownloader: 5 songs -> CAN
 *  - RecoveredAudios: 4 songs -> CAN
 *  - Notifications: 1 song -> CAN
 */
public class Artists05_Verify_Share_With_Limit extends BaseTest {

    private HomePage homePage;
    private ArtistsPage artistsPage;
    private ArtistDetailPage detailPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        artistsPage = new ArtistsPage(driver);
        detailPage = new ArtistDetailPage(driver);
        resetToArtists();
        Assert.assertTrue(artistsPage.isOnArtistsScreen());
    }

    // ============== VƯỢT LIMIT - SHEET DONG + TOAST ==============

    @Test(description = "TC_ARTISTS_30: Share <unknown> artist (382 songs) > 10 -> FAIL")
    public void TC_30_share_artist_over_limit() {
        artistsPage.clickEditButtonByIndex(0);
        int songCount = artistsPage.getSheetArtistSongCount();
        Assert.assertTrue(songCount > ArtistsPage.SHARE_LIMIT,
                "Artist[0] phai co > 10 songs de test. Actual: " + songCount);

        artistsPage.clickSheetShare();
        sleep(3000);

        // Expected: sheet dong, KHONG mo share resolver
        Assert.assertFalse(artistsPage.isShareIntentResolverOpen(),
                "Share resolver KHONG duoc mo voi " + songCount + " songs (> 10)");
        Assert.assertTrue(artistsPage.isSheetClosedAndStillInApp(),
                "Sheet phai dong va van o app");

        ExtentReportManager.getTest().log(Status.PASS,
                "Share " + songCount + " songs: sheet dong + toast (correct)");
    }

    @Test(description = "TC_ARTISTS_34: Share Music Download folder (31 songs) > 10 -> FAIL")
    public void TC_34_share_music_download_folder() {
        artistsPage.clickArtistByIndex(0);
        Assert.assertTrue(detailPage.isOnArtistDetailScreen());

        // Vao folder Music Download
        if (!detailPage.isFolderDisplayed("Music Download")) {
            throw new SkipException("Folder Music Download khong co");
        }
        detailPage.clickFolder("Music Download");
        sleep(2500);

        // Mo edit cua folder (3-dot top right)
        detailPage.clickArtistMenu();
        sleep(2500);
        Assert.assertTrue(detailPage.isEditSheetOpen());

        detailPage.clickSheetShare();
        sleep(3000);

        Assert.assertFalse(detailPage.isShareIntentResolverOpen(),
                "Music Download (31 songs > 10) KHONG duoc mo share");
        Assert.assertTrue(detailPage.isStillInApp(),
                "Phai van o app");
        ExtentReportManager.getTest().log(Status.PASS,
                "Music Download share blocked correctly");
    }

    @Test(description = "TC_ARTISTS_35: Share VoiceChanger folder (341 songs) > 10 -> FAIL")
    public void TC_35_share_voice_changer_folder() {
        artistsPage.clickArtistByIndex(0);
        if (!detailPage.isFolderDisplayed("VoiceChanger")) {
            throw new SkipException("Folder VoiceChanger khong co");
        }
        detailPage.clickFolder("VoiceChanger");
        sleep(2500);

        detailPage.clickArtistMenu();
        sleep(2500);
        Assert.assertTrue(detailPage.isEditSheetOpen());

        detailPage.clickSheetShare();
        sleep(3000);

        Assert.assertFalse(detailPage.isShareIntentResolverOpen(),
                "VoiceChanger (341 songs > 10) KHONG duoc mo share");
        ExtentReportManager.getTest().log(Status.PASS,
                "VoiceChanger share blocked correctly");
    }

    // ============== TRONG LIMIT - SHARE INTENT MO ==============

    @Test(description = "TC_ARTISTS_31: Share Notifications folder (1 song) <= 10 -> SUCCESS")
    public void TC_31_share_notifications_folder() {
        artistsPage.clickArtistByIndex(0);
        // Notifications o cuoi Albums (cuon ngang) -> reveal truoc khi click.
        if (!detailPage.ensureFolderVisible("Notifications")) {
            throw new SkipException("Folder Notifications khong co (sau khi cuon)");
        }
        detailPage.clickFolder("Notifications");
        sleep(2500);

        int songCount = detailPage.getArtistSongCount();
        Assert.assertTrue(songCount <= ArtistsPage.SHARE_LIMIT,
                "Notifications phai co <= 10 songs. Actual: " + songCount);

        detailPage.clickArtistMenu();
        sleep(2500);
        Assert.assertTrue(detailPage.isEditSheetOpen());

        detailPage.clickSheetShare();
        sleep(4000);

        Assert.assertTrue(detailPage.isShareIntentResolverOpen(),
                "Notifications (" + songCount + " songs) phai mo share resolver");
        ExtentReportManager.getTest().log(Status.PASS,
                "Share Notifications (" + songCount + " songs) mo resolver OK");

        // Cleanup: dismiss share
        detailPage.dismissShareIntent();
    }

    @Test(description = "TC_ARTISTS_32: Share BrowserDownloader folder (5 songs) <= 10 -> SUCCESS")
    public void TC_32_share_browser_folder() {
        artistsPage.clickArtistByIndex(0);
        if (!detailPage.isFolderDisplayed("BrowserDownloader")) {
            throw new SkipException("Folder BrowserDownloader khong co");
        }
        detailPage.clickFolder("BrowserDownloader");
        sleep(2500);

        int songCount = detailPage.getArtistSongCount();
        Assert.assertTrue(songCount <= ArtistsPage.SHARE_LIMIT);

        detailPage.clickArtistMenu();
        sleep(2500);
        detailPage.clickSheetShare();
        sleep(4000);

        Assert.assertTrue(detailPage.isShareIntentResolverOpen(),
                "BrowserDownloader (" + songCount + " songs) phai mo share");
        ExtentReportManager.getTest().log(Status.PASS,
                "BrowserDownloader share OK with " + songCount + " songs");

        detailPage.dismissShareIntent();
    }

    @Test(description = "TC_ARTISTS_33: Share RecoveredAudios folder (4 songs) <= 10 -> SUCCESS")
    public void TC_33_share_recovered_folder() {
        artistsPage.clickArtistByIndex(0);
        if (!detailPage.isFolderDisplayed("RecoveredAudios")) {
            throw new SkipException("Folder RecoveredAudios khong co");
        }
        detailPage.clickFolder("RecoveredAudios");
        sleep(2500);

        int songCount = detailPage.getArtistSongCount();
        detailPage.clickArtistMenu();
        sleep(2500);
        detailPage.clickSheetShare();
        sleep(4000);

        Assert.assertTrue(detailPage.isShareIntentResolverOpen(),
                "RecoveredAudios (" + songCount + " songs) phai mo share");
        ExtentReportManager.getTest().log(Status.PASS,
                "RecoveredAudios share OK with " + songCount + " songs");

        detailPage.dismissShareIntent();
    }

    @Test(description = "TC_ARTISTS_36: Share 1 track tu tracks list - LUON OK")
    public void TC_36_share_single_track() {
        artistsPage.clickArtistByIndex(0);
        Assert.assertTrue(detailPage.isOnArtistDetailScreen());

        int trackCount = detailPage.getDisplayedTracksCount();
        if (trackCount == 0) {
            throw new SkipException("Khong co tracks");
        }

        // Click edit cua track[0]
        detailPage.clickTrackEditByIndex(0);
        sleep(2500);

        // Tu day la track-edit sheet (7 actions, dung TracksPage)
        // Click Share track
        try {
            driver.findElement(io.appium.java_client.AppiumBy.accessibilityId("Share track")).click();
            sleep(4000);
        } catch (Exception e) { /* fall through */ }

        Assert.assertTrue(detailPage.isShareIntentResolverOpen(),
                "Share 1 track phai mo share resolver");
        ExtentReportManager.getTest().log(Status.PASS,
                "Single track share OK");
        detailPage.dismissShareIntent();
    }

    @Test(description = "TC_ARTISTS_37: Cancel share intent -> quay ve app")
    public void TC_37_cancel_share_intent() {
        // Dung BrowserDownloader (5 songs, hien san, share OK) - chi can 1 share resolver de cancel.
        artistsPage.clickArtistByIndex(0);
        detailPage.clickFolder("BrowserDownloader");
        sleep(2500);

        detailPage.clickArtistMenu();
        sleep(2500);
        detailPage.clickSheetShare();
        sleep(4000);
        Assert.assertTrue(detailPage.isShareIntentResolverOpen(),
                "Phai mo share resolver (BrowserDownloader 5 songs)");

        // Press BACK to cancel
        detailPage.dismissShareIntent();
        sleep(2000);

        Assert.assertTrue(detailPage.isStillInApp(),
                "Sau khi cancel share, phai ve app");
        ExtentReportManager.getTest().log(Status.PASS, "Cancel share OK");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (detailPage.isShareIntentResolverOpen()) detailPage.dismissShareIntent();
            if (artistsPage.isEditSheetOpen()) artistsPage.closeEditSheetByBack();
            if (detailPage.isEditSheetOpen()) detailPage.closeEditSheetByBack();
        } catch (Exception ignored) {}
        resetToArtists();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // Import inline cho SkipException
    private static class SkipException extends RuntimeException {
        SkipException(String msg) { super(msg); }
    }
}