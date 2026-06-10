package testcases.playlists;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.PlaylistDetailPage;
import pages.PlaylistsPage;
import report.ExtentReportManager;

import java.util.List;

/**
 * Navigation + detail BEHAVIOR (khong chi hien thi). Gom cac check cung 1 man detail vao 1 lan
 * navigate. Bo sung: Play all/Shuffle thuc su PHAT, click track PHAT, empty playlist (0 tracks).
 */
public class Playlists02_Verify_Navigation extends BaseTest {

    private HomePage homePage;
    private PlaylistsPage playlistsPage;
    private PlaylistDetailPage detailPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        playlistsPage = new PlaylistsPage(driver);
        detailPage = new PlaylistDetailPage(driver);
        resetToPlaylists();
        Assert.assertTrue(playlistsPage.isOnPlaylistsScreen());
    }

    @Test(description = "TC_PL_03: My Favorite detail (header+hero+Play/Shuffle) + Back ve list (gom)")
    public void TC_03_my_favorite_detail() {
        playlistsPage.clickMyFavorite();
        Assert.assertTrue(detailPage.isOnPlaylistDetailScreen(), "Phai o playlist detail");
        Assert.assertTrue(detailPage.isHeaderDisplayed(), "Header Back + Show menu");
        Assert.assertTrue(detailPage.isShowMenuDisplayed(), "Show menu");
        Assert.assertTrue(detailPage.isPlayAllAndShuffleDisplayed(), "Play all + Shuffle");
        String hero = detailPage.getHeroInfo();
        Assert.assertNotNull(hero, "Hero phai co text");
        Assert.assertTrue(hero.contains("track"), "Hero co 'N tracks': " + hero);
        Assert.assertTrue(detailPage.getTrackCount() > 0, "My Favorite phai co tracks");

        detailPage.clickBack();
        Assert.assertTrue(playlistsPage.isOnPlaylistsScreen(), "Back phai ve Playlists");
        ExtentReportManager.getTest().log(Status.PASS, "My Favorite detail OK: " + hero);
    }

    @Test(description = "TC_PL_04: Recently Played detail mo + hero")
    public void TC_04_recently_played_detail() {
        playlistsPage.clickRecentlyPlayed();
        Assert.assertTrue(detailPage.isOnPlaylistDetailScreen());
        String hero = detailPage.getHeroInfo();
        Assert.assertTrue(hero != null && hero.contains("track"), "Hero: " + hero);
        Assert.assertTrue(detailPage.getTrackCount() > 0);
        ExtentReportManager.getTest().log(Status.PASS, "Recently Played detail: " + hero);
        detailPage.clickBack();
    }

    @Test(description = "TC_PL_05: Play all trong detail -> thuc su PHAT (mini player)")
    public void TC_05_play_all_plays() {
        playlistsPage.clickMyFavorite();
        Assert.assertTrue(detailPage.isOnPlaylistDetailScreen());
        detailPage.clickPlayAll();
        boolean played = waitPlaying();
        Assert.assertTrue(played, "Play all phai phat (mini player hien)");
        ExtentReportManager.getTest().log(Status.PASS, "Play all phat OK (mini player)");
        detailPage.clickBack();
    }

    @Test(description = "TC_PL_06: Click track trong detail -> phat (giong man Tracks)")
    public void TC_06_click_track_plays() {
        playlistsPage.clickMyFavorite();
        Assert.assertTrue(detailPage.isOnPlaylistDetailScreen());
        Assert.assertTrue(detailPage.getDisplayedTracksCount() > 0, "Phai co track de click");
        detailPage.clickTrackByIndex(0);
        Assert.assertTrue(waitPlaying(), "Click track phai phat (mini player)");
        ExtentReportManager.getTest().log(Status.PASS, "Click track phat OK");
        detailPage.clickBack();
    }

    @Test(description = "TC_PL_07: Empty playlist (0 tracks) -> detail mo, hero '0 tracks'")
    public void TC_07_empty_playlist_detail() {
        String empty = findPlaylistWithTracks(0);
        if (empty == null) throw new SkipException("Khong co playlist 0 tracks de test");
        playlistsPage.clickPlaylistByName(empty);
        Assert.assertTrue(detailPage.isOnPlaylistDetailScreen(), "Empty playlist detail phai mo");
        Assert.assertEquals(detailPage.getTrackCount(), 0, "Hero phai bao 0 tracks");
        Assert.assertEquals(detailPage.getDisplayedTracksCount(), 0, "Khong co track item");
        ExtentReportManager.getTest().log(Status.PASS,
                "Empty playlist '" + empty + "' detail: 0 tracks OK");
        detailPage.clickBack();
    }

    // ============== HELPERS ==============

    private boolean waitPlaying() {
        for (int i = 0; i < 5; i++) {
            if (detailPage.isMiniPlayerDisplayed()) return true;
            sleep(800);
        }
        return detailPage.isMiniPlayerDisplayed();
    }

    private String findPlaylistWithTracks(int count) {
        List<String> names = playlistsPage.getAllPlaylistNames();
        for (String n : names) {
            if (n.contains("My Favorite") || n.contains("Recently Played")) continue;
            if (playlistsPage.getTrackCountOf(n) == count) return n;
        }
        return null;
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try { if (detailPage.isOnPlaylistDetailScreen()) detailPage.clickBack(); }
        catch (Exception ignored) {}
        resetToPlaylists();
    }
}
