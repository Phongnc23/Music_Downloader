package testcases.playlists;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.PlaylistsPage;
import report.ExtentReportManager;

import java.util.List;

/**
 * 2 loai edit sheet:
 *  - User playlist: 6 actions (co Rename + Delete)
 *  - Local playlist (Recently Played / My Favorite): khong Rename/Delete; Recently Played co
 *    "Clear recently played".
 * Gom cac check cung 1 sheet vao 1 lan mo sheet. Bo sung: "Add to playlist" picker mo.
 */
public class Playlists04_Verify_Edit_Sheets extends BaseTest {

    private HomePage homePage;
    private PlaylistsPage playlistsPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        playlistsPage = new PlaylistsPage(driver);
        resetToPlaylists();
        Assert.assertTrue(playlistsPage.isOnPlaylistsScreen());
    }

    @Test(description = "TC_PL_12: User playlist sheet = 6 actions (co Rename + Delete) (gom)")
    public void TC_12_user_playlist_sheet() {
        String userPlaylist = findUserPlaylist();
        if (userPlaylist == null) throw new SkipException("Khong co user playlist");

        playlistsPage.clickEditButtonOf(userPlaylist);
        sleep(1500);
        Assert.assertTrue(playlistsPage.isEditSheetOpen(), "Sheet phai mo");
        Assert.assertTrue(playlistsPage.isUserPlaylistSheet(), "User playlist co Rename + Delete");
        Assert.assertTrue(playlistsPage.hasRenameOption(), "Co Rename");
        Assert.assertTrue(playlistsPage.hasDeleteOption(), "Co Delete");
        ExtentReportManager.getTest().log(Status.PASS,
                "User playlist '" + userPlaylist + "': 6 actions (Rename+Delete) OK");
    }

    @Test(description = "TC_PL_13: Tap Scrim -> dong sheet")
    public void TC_13_close_scrim() {
        String userPlaylist = findUserPlaylist();
        if (userPlaylist == null) throw new SkipException("Khong co user playlist");
        playlistsPage.clickEditButtonOf(userPlaylist);
        sleep(1500);
        Assert.assertTrue(playlistsPage.isEditSheetOpen());

        playlistsPage.closeEditSheetByScrim();
        Assert.assertFalse(playlistsPage.isEditSheetOpen(), "Scrim phai dong sheet");
        ExtentReportManager.getTest().log(Status.PASS, "Scrim close OK");
    }

    @Test(description = "TC_PL_14: Recently Played sheet = co Clear, KHONG Rename/Delete (gom)")
    public void TC_14_recently_played_sheet() {
        playlistsPage.clickEditButtonOf("Recently Played");
        sleep(1500);
        Assert.assertTrue(playlistsPage.isEditSheetOpen(), "Sheet phai mo");
        Assert.assertTrue(playlistsPage.isClearRecentlyPlayedDisplayed(), "Co 'Clear recently played'");
        Assert.assertFalse(playlistsPage.hasRenameOption(), "KHONG co Rename");
        Assert.assertFalse(playlistsPage.hasDeleteOption(), "KHONG co Delete");
        ExtentReportManager.getTest().log(Status.PASS,
                "Recently Played sheet: Clear + khong Rename/Delete OK");
    }

    @Test(description = "TC_PL_15: My Favorite la local sheet (khong Rename/Delete)")
    public void TC_15_my_favorite_local() {
        playlistsPage.clickEditButtonOf("My Favorite");
        sleep(1500);
        Assert.assertTrue(playlistsPage.isEditSheetOpen());
        Assert.assertTrue(playlistsPage.isLocalPlaylistSheet(), "My Favorite = local (khong Rename/Delete)");
        ExtentReportManager.getTest().log(Status.PASS, "My Favorite local sheet OK");
    }

    @Test(description = "TC_PL_16: Sheet 'Add to playlist' -> mo picker (Create new + danh sach)")
    public void TC_16_add_to_playlist_picker() {
        String userPlaylist = findUserPlaylist();
        if (userPlaylist == null) throw new SkipException("Khong co user playlist");
        playlistsPage.clickEditButtonOf(userPlaylist);
        sleep(1500);
        Assert.assertTrue(playlistsPage.isEditSheetOpen());

        playlistsPage.clickSheetAddToPlaylist();
        sleep(1500);
        Assert.assertTrue(playlistsPage.isAddToPlaylistPickerOpen(),
                "Picker 'Add to playlist' phai mo (Create new playlist + danh sach)");
        ExtentReportManager.getTest().log(Status.PASS, "Add to playlist picker mo OK");
        playlistsPage.closeEditSheetByBack();
    }

    // ============== HELPER ==============

    private String findUserPlaylist() {
        List<String> names = playlistsPage.getAllPlaylistNames();
        for (String n : names) {
            if (!n.contains("My Favorite") && !n.contains("Recently Played")) return n;
        }
        return null;
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try { if (playlistsPage.isEditSheetOpen()) playlistsPage.closeEditSheetByBack(); }
        catch (Exception ignored) {}
        resetToPlaylists();
    }
}
