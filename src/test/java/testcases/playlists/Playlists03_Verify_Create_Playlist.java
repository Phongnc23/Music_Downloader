package testcases.playlists;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.PlaylistsPage;
import report.ExtentReportManager;

/**
 * Create playlist dialog. Gom cac check trang thai dialog (mo + rong + char count + dismiss) vao
 * 1 case. Cac case co side-effect (Save/Cancel/empty validation) tach rieng. resetToPlaylists.
 */
public class Playlists03_Verify_Create_Playlist extends BaseTest {

    private HomePage homePage;
    private PlaylistsPage playlistsPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        playlistsPage = new PlaylistsPage(driver);
        resetToPlaylists();
        Assert.assertTrue(playlistsPage.isOnPlaylistsScreen());
    }

    @Test(description = "TC_PL_08: Dialog mo + rong '0/60' + char count update + Dismiss dong (gom)")
    public void TC_08_dialog_states() {
        playlistsPage.clickCreateNewPlaylist();
        Assert.assertTrue(playlistsPage.isCreateDialogOpen(), "Create dialog phai mo");
        Assert.assertTrue(playlistsPage.isDialogInputEmpty(), "Input rong ban dau");
        String c0 = playlistsPage.getDialogCharCount();
        Assert.assertTrue(c0 != null && c0.startsWith("0/"), "Char count '0/60'. Actual: " + c0);

        playlistsPage.typePlaylistName("Hello");
        String c5 = playlistsPage.getDialogCharCount();
        Assert.assertTrue(c5 != null && c5.startsWith("5/"), "Char count '5/60' sau 'Hello'. Actual: " + c5);

        playlistsPage.dismissDialog();
        Assert.assertFalse(playlistsPage.isCreateDialogOpen(), "Dismiss phai dong dialog");
        ExtentReportManager.getTest().log(Status.PASS,
                "Dialog: mo + " + c0 + " -> " + c5 + " + Dismiss dong OK");
    }

    @Test(description = "TC_PL_09: CANCEL -> khong tao, count khong doi")
    public void TC_09_cancel_no_create() {
        int before = playlistsPage.getMyPlaylistCount();
        playlistsPage.clickCreateNewPlaylist();
        playlistsPage.typePlaylistName("SHOULD_NOT_EXIST");
        playlistsPage.clickDialogCancel();
        sleep(1500);

        // Count khong doi da chung minh khong tao moi -> KHONG can isPlaylistPresent (cuon ca list).
        Assert.assertEquals(playlistsPage.getMyPlaylistCount(), before, "Count khong doi sau Cancel");
        ExtentReportManager.getTest().log(Status.PASS, "Cancel OK - count giu: " + before);
    }

    @Test(description = "TC_PL_10: SAVE -> tao playlist moi (+cleanup delete)")
    public void TC_10_save_creates_playlist() {
        int before = playlistsPage.getMyPlaylistCount();
        String newName = "TEST_PL_" + (System.currentTimeMillis() % 100000);

        playlistsPage.clickCreateNewPlaylist();
        playlistsPage.typePlaylistName(newName);
        playlistsPage.clickDialogSave();
        sleep(2500);

        Assert.assertFalse(playlistsPage.isCreateDialogOpen(), "Dialog phai dong sau SAVE");
        int after = playlistsPage.getMyPlaylistCount();
        // Count-first (re, khong cuon); chi fallback isPlaylistPresent neu count chua cap nhat.
        boolean created = after == before + 1 || playlistsPage.isPlaylistPresent(newName);
        Assert.assertTrue(created, "Playlist moi phai tao. Name: " + newName + ", count " + before + "->" + after);
        ExtentReportManager.getTest().log(Status.PASS, "Created '" + newName + "'. Count: " + before + "->" + after);

        cleanupDeletePlaylist(newName);
    }

    @Test(description = "TC_PL_11: SAVE voi ten rong -> validation (dialog van mo HOAC khong tao)")
    public void TC_11_save_empty_name() {
        int before = playlistsPage.getMyPlaylistCount();
        playlistsPage.clickCreateNewPlaylist();
        playlistsPage.clickDialogSave();
        sleep(1500);

        int after = playlistsPage.getMyPlaylistCount();
        boolean validated = playlistsPage.isCreateDialogOpen() || after == before;
        Assert.assertTrue(validated, "Ten rong: dialog van mo HOAC khong tao. " + before + "->" + after);
        ExtentReportManager.getTest().log(Status.PASS,
                "Empty name validation OK (dialog open=" + playlistsPage.isCreateDialogOpen() + ")");
        if (playlistsPage.isCreateDialogOpen()) playlistsPage.clickDialogCancel();
    }

    // ============== HELPER ==============

    private void cleanupDeletePlaylist(String name) {
        try {
            if (!playlistsPage.isPlaylistPresent(name)) return;
            playlistsPage.clickEditButtonOf(name);
            sleep(1500);
            if (playlistsPage.hasDeleteOption()) {
                playlistsPage.clickSheetDelete();
                sleep(1500);
                if (playlistsPage.isConfirmDialogOpen()) {
                    playlistsPage.clickConfirmDelete();
                    sleep(1500);
                }
            }
            logger.info("Cleanup deleted playlist: " + name);
        } catch (Exception e) {
            logger.warn("Cleanup delete loi: " + e.getMessage());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (playlistsPage.isCreateDialogOpen()) playlistsPage.clickDialogCancel();
            if (playlistsPage.isConfirmDialogOpen()) playlistsPage.clickConfirmCancel();
            if (playlistsPage.isEditSheetOpen()) playlistsPage.closeEditSheetByBack();
        } catch (Exception ignored) {}
        resetToPlaylists();
    }
}
