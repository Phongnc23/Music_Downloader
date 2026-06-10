package testcases.artists;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ArtistsPage;
import pages.HomePage;
import pages.TracksPage;
import report.ExtentReportManager;

/**
 * Test cac action khong-Share trong artist edit sheet:
 * Play, Add to playing queue, Add to playlist.
 * (Share tach rieng vao Artists05 vi co logic 10-song limit.)
 */
public class Artists04_Verify_Edit_Actions extends BaseTest {

    private HomePage homePage;
    private ArtistsPage artistsPage;
    private TracksPage tracksPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        artistsPage = new ArtistsPage(driver);
        tracksPage = new TracksPage(driver);
        resetToArtists();
        Assert.assertTrue(artistsPage.isOnArtistsScreen());
    }

    @Test(description = "TC_ARTISTS_26: Play artist -> bai dau phat, sheet dong")
    public void TC_26_play_artist() {
        artistsPage.clickEditButtonByIndex(0);
        Assert.assertTrue(artistsPage.isEditSheetOpen());

        artistsPage.clickSheetPlay();
        sleep(3000);

        Assert.assertFalse(artistsPage.isEditSheetOpen(),
                "Sheet phai dong sau Play");
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(),
                "Mini player phai hien (bai dang phat)");
        ExtentReportManager.getTest().log(Status.PASS,
                "Play artist OK. Mini: " + tracksPage.getMiniPlayerContent());
    }

    @Test(description = "TC_ARTISTS_27: Add to playing queue artist - tat ca songs vao queue")
    public void TC_27_add_artist_to_queue() {
        // Bat dau phat 1 bai de co queue
        homePage.clickBottomNavTracks();
        sleep(2000);
        tracksPage.clickPlayAll();
        sleep(3000);

        // Quay lai Artists
        artistsPage.openArtistsFromBottomNav();
        sleep(2000);

        artistsPage.clickEditButtonByIndex(0);
        int songsToAdd = artistsPage.getSheetArtistSongCount();
        Assert.assertTrue(songsToAdd > 0);

        artistsPage.clickSheetAddToQueue();
        sleep(3000);

        Assert.assertFalse(artistsPage.isEditSheetOpen(),
                "Sheet phai dong sau Add to queue");
        ExtentReportManager.getTest().log(Status.PASS,
                "Added " + songsToAdd + " songs to queue");
    }

    @Test(description = "TC_ARTISTS_28: Add to playlist - mo dialog")
    public void TC_28_add_to_playlist_dialog() {
        artistsPage.clickEditButtonByIndex(0);

        artistsPage.clickSheetAddToPlaylist();
        sleep(2500);

        Assert.assertTrue(tracksPage.isAddToPlaylistDialogOpen(),
                "Add to playlist dialog phai mo");
        Assert.assertTrue(tracksPage.isMyFavoritePlaylistDisplayed());
        ExtentReportManager.getTest().log(Status.PASS,
                "Add to playlist dialog mo. Favorite: " + tracksPage.getMyFavoriteText());

        // Cleanup: dismiss dialog
        tracksPage.closeEditSheetByScrim();
    }

    @Test(description = "TC_ARTISTS_29: Add artist to My Favorite - tat ca songs vao playlist")
    public void TC_29_add_to_my_favorite() {
        artistsPage.clickEditButtonByIndex(0);
        int songCount = artistsPage.getSheetArtistSongCount();

        artistsPage.clickSheetAddToPlaylist();
        sleep(2500);
        Assert.assertTrue(tracksPage.isAddToPlaylistDialogOpen());

        tracksPage.clickMyFavorite();
        sleep(3000);

        Assert.assertFalse(tracksPage.isAddToPlaylistDialogOpen(),
                "Dialog phai dong sau khi chon playlist");
        ExtentReportManager.getTest().log(Status.PASS,
                "Added " + songCount + " songs to My Favorite");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (tracksPage.isAddToPlaylistDialogOpen()) tracksPage.closeEditSheetByScrim();
            if (artistsPage.isEditSheetOpen()) artistsPage.closeEditSheetByBack();
        } catch (Exception ignored) {}
        resetToArtists();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}