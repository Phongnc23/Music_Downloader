package testcases.albums;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AlbumsPage;
import pages.HomePage;
import pages.TracksPage;
import report.ExtentReportManager;

/**
 * Album-level edit sheet (4 actions). Setup mo san sheet tu 3-dot album[0]. Toi uu: gom
 * open/4-actions/header (cung sheet dang mo) vao 1 case; giam sleep.
 */
public class Albums04_Verify_Edit_Sheet extends BaseTest {

    private HomePage homePage;
    private AlbumsPage albumsPage;
    private TracksPage tracksPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        albumsPage = new AlbumsPage(driver);
        tracksPage = new TracksPage(driver);
        resetToAlbums();
        Assert.assertTrue(albumsPage.isOnAlbumsScreen());
        albumsPage.clickEditButtonByIndex(0);
    }

    @Test(description = "TC_ALBUMS_33_34_35: Sheet mo + 4 actions + header (name + N songs) - gom 1 sheet")
    public void TC_33_35_sheet_display() {
        Assert.assertTrue(albumsPage.isEditSheetOpen(), "Sheet phai mo");
        Assert.assertTrue(albumsPage.isAllAlbumActionsDisplayed(),
                "Phai co 4 actions: Play/Queue/Playlist/Share");
        String info = albumsPage.getSheetAlbumInfo();
        Assert.assertNotNull(info, "Header phai co content-desc");
        Assert.assertTrue(info.contains("song"), "Header phai contains 'song'. Actual: " + info);
        Assert.assertTrue(albumsPage.getSheetAlbumSongCount() > 0, "N songs phai > 0");
        ExtentReportManager.getTest().log(Status.PASS, "Sheet 4 actions + header OK: " + info);
    }

    @Test(description = "TC_ALBUMS_36: Tap Scrim -> dong sheet")
    public void TC_36_close_scrim() {
        albumsPage.closeEditSheetByScrim();
        Assert.assertFalse(albumsPage.isEditSheetOpen());
        ExtentReportManager.getTest().log(Status.PASS, "Scrim close OK");
    }

    @Test(description = "TC_ALBUMS_37: Press BACK -> dong sheet, ve Albums")
    public void TC_37_close_back() {
        albumsPage.closeEditSheetByBack();
        Assert.assertFalse(albumsPage.isEditSheetOpen());
        Assert.assertTrue(albumsPage.isOnAlbumsScreen());
        ExtentReportManager.getTest().log(Status.PASS, "BACK close OK");
    }

    @Test(description = "TC_ALBUMS_40: Play album -> mini player")
    public void TC_40_play_album() {
        albumsPage.clickSheetPlay();
        sleep(1500);
        Assert.assertFalse(albumsPage.isEditSheetOpen(), "Sheet phai dong");
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Mini player phai hien");
        ExtentReportManager.getTest().log(Status.PASS,
                "Play album OK. Mini: " + tracksPage.getMiniPlayerContent());
    }

    @Test(description = "TC_ALBUMS_41: Add to playing queue -> sheet dong + van o app")
    public void TC_41_add_to_queue() {
        albumsPage.clickSheetAddToQueue();
        sleep(1200);
        Assert.assertFalse(albumsPage.isEditSheetOpen(), "Sheet phai dong sau Add to queue");
        Assert.assertTrue(albumsPage.isSheetClosedAndStillInApp(), "Phai van o app");
        ExtentReportManager.getTest().log(Status.PASS, "Add album to queue OK");
    }

    @Test(description = "TC_ALBUMS_42: Add to playlist -> mo dialog")
    public void TC_42_add_to_playlist() {
        albumsPage.clickSheetAddToPlaylist();
        sleep(1500);
        Assert.assertTrue(tracksPage.isAddToPlaylistDialogOpen(), "Add to playlist dialog phai mo");
        ExtentReportManager.getTest().log(Status.PASS, "Add to playlist dialog OK");
        tracksPage.closeEditSheetByScrim();
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (tracksPage.isAddToPlaylistDialogOpen()) tracksPage.closeEditSheetByScrim();
            if (albumsPage.isEditSheetOpen()) albumsPage.closeEditSheetByBack();
        } catch (Exception ignored) {}
        resetToAlbums();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
