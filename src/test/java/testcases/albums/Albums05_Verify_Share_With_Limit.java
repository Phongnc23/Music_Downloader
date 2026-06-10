package testcases.albums;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AlbumDetailPage;
import pages.AlbumsPage;
import pages.HomePage;
import report.ExtentReportManager;

/**
 * Album Share track logic - gioi han 10 songs.
 *  - Album <= 10 tracks: Share -> mo Android share intent resolver
 *  - Album > 10 tracks: Share -> sheet dong + toast (van o app)
 * Sample: Music Download 34 / VoiceChanger 333 -> CANNOT; BrowserDownloader 5 / RecoveredAudios 4
 *         / Notifications 1 -> CAN.
 *
 * Share tu ALBUM DETAIL (3-dot top-right co dinh) thay vi 3-dot tren card list (vi tri khong
 * on dinh voi album hang duoi). clickAlbumByName co scrollIntoView nen mo duoc ca album hang duoi.
 */
public class Albums05_Verify_Share_With_Limit extends BaseTest {

    private HomePage homePage;
    private AlbumsPage albumsPage;
    private AlbumDetailPage detailPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        albumsPage = new AlbumsPage(driver);
        detailPage = new AlbumDetailPage(driver);
        resetToAlbums();
        Assert.assertTrue(albumsPage.isOnAlbumsScreen());
    }

    /** Mo album detail theo ten -> 3-dot album -> Share track. Tra ve so songs cua album.
     *  clickAlbumByName tu scrollIntoView nen mo duoc album da bi cuon off (vd sau khi cuon
     *  xuong xem VoiceChanger). KHONG pre-check findAlbumIndexByName (se skip nham khi off-screen). */
    private int shareAlbumFromDetail(String name) {
        albumsPage.clickAlbumByName(name);
        Assert.assertTrue(detailPage.isOnAlbumDetailScreen(), "Detail '" + name + "' phai mo");
        int songs = detailPage.getAlbumSongCount();
        detailPage.clickAlbumMenu();
        sleep(1500);
        Assert.assertTrue(detailPage.isEditSheetOpen(), "Edit sheet '" + name + "' phai mo");
        detailPage.clickSheetShare();
        sleep(2500);
        return songs;
    }

    // ========== OVER LIMIT (> 10) - SHEET DONG, KHONG MO RESOLVER ==========

    @Test(description = "TC_ALBUMS_45_38: Share Music Download (34) > 10 -> FAIL (sheet dong, van o app)")
    public void TC_45_share_over_limit_music_download() {
        int songs = shareAlbumFromDetail("Music Download");
        Assert.assertTrue(songs > AlbumsPage.SHARE_LIMIT, "Music Download phai > 10. Actual: " + songs);
        Assert.assertFalse(detailPage.isShareIntentResolverOpen(),
                "Music Download (" + songs + " > 10) KHONG duoc mo share");
        Assert.assertTrue(detailPage.isStillInApp(), "Phai van o app");
        ExtentReportManager.getTest().log(Status.PASS, "Music Download (" + songs + "): share blocked OK");
    }

    @Test(description = "TC_ALBUMS_46: Share VoiceChanger (333) > 10 -> FAIL")
    public void TC_46_share_over_limit_voice_changer() {
        int songs = shareAlbumFromDetail("VoiceChanger");
        Assert.assertFalse(detailPage.isShareIntentResolverOpen(),
                "VoiceChanger (" + songs + " > 10) KHONG duoc mo share");
        Assert.assertTrue(detailPage.isStillInApp());
        ExtentReportManager.getTest().log(Status.PASS, "VoiceChanger (" + songs + "): share blocked OK");
    }

    // ========== UNDER LIMIT (<= 10) - MO SHARE RESOLVER ==========

    @Test(description = "TC_ALBUMS_47: Share Notifications (1) <= 10 -> SUCCESS (resolver mo)")
    public void TC_47_share_notifications() {
        int songs = shareAlbumFromDetail("Notifications");
        Assert.assertTrue(songs <= AlbumsPage.SHARE_LIMIT, "Notifications phai <= 10. Actual: " + songs);
        Assert.assertTrue(detailPage.isShareIntentResolverOpen(),
                "Notifications (" + songs + " <= 10) phai mo share resolver");
        ExtentReportManager.getTest().log(Status.PASS, "Notifications (" + songs + "): resolver OK");
        detailPage.dismissShareIntent();
    }

    @Test(description = "TC_ALBUMS_48: Share RecoveredAudios (4) <= 10 -> SUCCESS")
    public void TC_48_share_recovered() {
        int songs = shareAlbumFromDetail("RecoveredAudios");
        Assert.assertTrue(detailPage.isShareIntentResolverOpen(),
                "RecoveredAudios (" + songs + ") phai mo share");
        ExtentReportManager.getTest().log(Status.PASS, "RecoveredAudios (" + songs + "): share OK");
        detailPage.dismissShareIntent();
    }

    @Test(description = "TC_ALBUMS_49: Share BrowserDownloader (5) <= 10 -> SUCCESS")
    public void TC_49_share_browser() {
        int songs = shareAlbumFromDetail("BrowserDownloader");
        Assert.assertTrue(detailPage.isShareIntentResolverOpen(),
                "BrowserDownloader (" + songs + ") phai mo share");
        ExtentReportManager.getTest().log(Status.PASS, "BrowserDownloader (" + songs + "): share OK");
        detailPage.dismissShareIntent();
    }

    // ========== SINGLE TRACK + CANCEL ==========

    @Test(description = "TC_ALBUMS_50: Share single track trong album -> LUON OK")
    public void TC_50_share_single_track() {
        albumsPage.clickAlbumByIndex(0);
        Assert.assertTrue(detailPage.isOnAlbumDetailScreen());
        if (detailPage.getDisplayedTracksCount() == 0)
            throw new SkipException("Album khong co tracks");
        detailPage.clickTrackEditByIndex(0);
        sleep(1500);
        try {
            driver.findElement(io.appium.java_client.AppiumBy.accessibilityId("Share track")).click();
            sleep(2500);
        } catch (Exception ignored) {}
        Assert.assertTrue(detailPage.isShareIntentResolverOpen(), "Single track share phai mo resolver");
        ExtentReportManager.getTest().log(Status.PASS, "Single track share OK");
        detailPage.dismissShareIntent();
    }

    @Test(description = "TC_ALBUMS_51: Cancel share intent -> ve app")
    public void TC_51_cancel_share() {
        int songs = shareAlbumFromDetail("Notifications");
        Assert.assertTrue(detailPage.isShareIntentResolverOpen(),
                "Notifications (" + songs + ") phai mo resolver de cancel");
        detailPage.dismissShareIntent();
        sleep(1500);
        Assert.assertTrue(detailPage.isStillInApp(), "Sau cancel share phai ve app");
        ExtentReportManager.getTest().log(Status.PASS, "Cancel share OK");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (detailPage.isShareIntentResolverOpen()) detailPage.dismissShareIntent();
            if (detailPage.isEditSheetOpen()) detailPage.closeEditSheetByBack();
            if (detailPage.isOnAlbumDetailScreen()) detailPage.clickBack();
        } catch (Exception ignored) {}
        resetToAlbums();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
