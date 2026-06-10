package testcases.searchlibrary;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AlbumDetailPage;
import pages.HomePage;
import pages.PlaylistDetailPage;
import pages.SearchLibraryPage;
import report.ExtentReportManager;

/**
 * Click ket qua search-in-library -> dieu huong dung man:
 *  - Track result -> phat (mini player) / mo player.
 *  - Album result -> Album detail.
 *  - Playlist result -> Playlist detail.
 * Cac case phu thuoc data (tab co the rong) -> skip an toan neu khong co result.
 */
public class SearchLibrary05_Verify_Result_Navigation extends BaseTest {

    private HomePage homePage;
    private SearchLibraryPage searchPage;
    private AlbumDetailPage albumDetail;
    private PlaylistDetailPage playlistDetail;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        searchPage = new SearchLibraryPage(driver);
        albumDetail = new AlbumDetailPage(driver);
        playlistDetail = new PlaylistDetailPage(driver);
        resetToHome();   // dam bao ve Home truoc (app co the o man search tu truoc)
        Assert.assertTrue(homePage.isHomeScreenDisplayed());
        searchPage.openFromHomeSearchIcon();
        Assert.assertTrue(searchPage.isOnSearchScreen());
    }

    @Test(description = "TC_SL_34: Click track result -> phat (mini player) hoac roi search")
    public void TC_34_click_track_result_plays() {
        searchPage.clickTabTracks();
        sleep(800);
        searchPage.typeQuery("a");
        sleep(1500);
        if (!searchPage.hasResults()) throw new SkipException("Khong co track result cho 'a'");

        String title = searchPage.clickFirstResultGetTitle();
        searchPage.hideKeyboard();   // BACK 1 lan: tat ban phim (no che mini player) -> moi thay mini player
        boolean played = false;
        for (int i = 0; i < 5 && !played; i++) {
            played = searchPage.isMiniPlayerDisplayed() || !searchPage.isOnSearchScreen();
            if (!played) sleep(800);
        }
        Assert.assertTrue(played, "Click track result phai phat (mini player) hoac roi man search");
        ExtentReportManager.getTest().log(Status.PASS, "Track result '" + title + "' -> phat OK");
    }

    @Test(description = "TC_SL_35: Click album result -> Album detail")
    public void TC_35_click_album_result_opens_detail() {
        searchPage.typeQuery("a");
        sleep(1200);
        searchPage.clickTabAlbums();
        sleep(1500);
        if (!searchPage.hasResults()) throw new SkipException("Khong co album result");

        String title = searchPage.clickFirstResultGetTitle();
        boolean opened = albumDetail.isOnAlbumDetailScreen() || !searchPage.isOnSearchScreen();
        Assert.assertTrue(opened, "Click album result phai mo Album detail / roi man search");
        ExtentReportManager.getTest().log(Status.PASS, "Album result '" + title + "' -> detail OK");
    }

    @Test(description = "TC_SL_36: Click playlist result -> Playlist detail")
    public void TC_36_click_playlist_result_opens_detail() {
        // Query khop ten playlist co trong library (TMP_/PL/TEST_PL deu chua 'p'). 'a' khong khop.
        searchPage.typeQuery("p");
        sleep(1200);
        searchPage.clickTabPlaylists();   // tab Playlists o cuoi -> tu cuon tab bar lo no
        sleep(1500);
        if (!searchPage.hasResults()) throw new SkipException("Khong co playlist result cho 'p'");

        String title = searchPage.clickFirstResultGetTitle();
        boolean opened = playlistDetail.isOnPlaylistDetailScreen() || !searchPage.isOnSearchScreen();
        Assert.assertTrue(opened, "Click playlist result phai mo Playlist detail / roi man search");
        ExtentReportManager.getTest().log(Status.PASS, "Playlist result '" + title + "' -> detail OK");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        // Co the dang o detail/player sau khi click result -> ve Home roi reset.
        resetToHome();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
