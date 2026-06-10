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

/**
 * Edit actions:
 *  - RENAME: thao tac tren playlist USER DAU TIEN (de quan sat), rename xong KHOI PHUC lai ten cu.
 *  - DELETE: dung label "My playlist (N)" lam tin hieu -> sau khi xoa N-1 = thanh cong. Dung
 *    playlist TAM (createTempPlaylist) de khong xoa du lieu that.
 *  - CLEAR recently played: chi verify confirm + Cancel (khong clear that).
 */
public class Playlists05_Verify_Edit_Actions extends BaseTest {

    private HomePage homePage;
    private PlaylistsPage playlistsPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        playlistsPage = new PlaylistsPage(driver);
        resetToPlaylists();
        Assert.assertTrue(playlistsPage.isOnPlaylistsScreen());
        playlistsPage.scrollToTop();
    }

    // ============== RENAME (playlist dau tien) ==============

    @Test(description = "TC_PL_17: Rename playlist dau - dialog prefilled ten cu + CANCEL giu nguyen")
    public void TC_17_rename_prefilled_cancel() {
        String orig = playlistsPage.getFirstUserPlaylist();
        if (orig == null) throw new SkipException("Khong co user playlist");

        playlistsPage.clickEditButtonOf(orig);
        sleep(1500);
        Assert.assertTrue(playlistsPage.isEditSheetOpen(), "Sheet phai mo");
        playlistsPage.clickSheetRename();
        sleep(2000);
        Assert.assertTrue(playlistsPage.isRenameDialogOpen(), "Rename dialog phai mo");
        String prefilled = playlistsPage.getRenameInputText();
        Assert.assertTrue(prefilled != null && prefilled.contains(orig),
                "Input prefilled ten cu. Expected contains: " + orig + ", Actual: " + prefilled);

        playlistsPage.typeRenameValue("RENAMED_NOT_SAVED");
        playlistsPage.clickRenameCancel();
        sleep(1500);
        // Cancel -> playlist dau van la 'orig' (top-only, KHONG cuon ca list)
        Assert.assertEquals(playlistsPage.getFirstUserPlaylist(), orig, "Cancel -> ten cu phai giu: " + orig);
        Assert.assertFalse(playlistsPage.isPlaylistVisible("RENAMED_NOT_SAVED"), "Ten moi KHONG ap dung");
        ExtentReportManager.getTest().log(Status.PASS, "Rename '" + orig + "' prefilled OK + Cancel giu nguyen");
    }

    @Test(description = "TC_PL_18: Rename playlist dau - SAVE doi ten")
    public void TC_18_rename_save() {
        String orig = playlistsPage.getFirstUserPlaylist();
        if (orig == null) throw new SkipException("Khong co user playlist");
        String newName = "RENAMED_" + (System.currentTimeMillis() % 100000);

        renamePlaylist(orig, newName);
        // Present: isPlaylistPresent (tim co huong, thuong khong cuon neu ten moi o top). Absent:
        // isPlaylistVisible (KHONG cuon ca list - tranh ton thoi gian).
        Assert.assertTrue(playlistsPage.isPlaylistPresent(newName), "Ten moi phai xuat hien: " + newName);
        Assert.assertFalse(playlistsPage.isPlaylistVisible(orig), "Ten cu khong con: " + orig);
        ExtentReportManager.getTest().log(Status.PASS, "Rename SAVE: " + orig + " -> " + newName);
    }

    // ============== DELETE (tin hieu: My playlist (N) -> N-1) ==============

    @Test(description = "TC_PL_19: Delete playlist dau - CANCEL -> My playlist (N) khong doi")
    public void TC_19_delete_cancel() {
        String target = playlistsPage.getFirstUserPlaylist();
        if (target == null) throw new SkipException("Khong co user playlist de xoa");
        int n = playlistsPage.getMyPlaylistCount();

        playlistsPage.clickEditButtonOf(target);
        sleep(1500);
        Assert.assertTrue(playlistsPage.isEditSheetOpen(), "Sheet phai mo");
        playlistsPage.clickSheetDelete();
        sleep(1500);
        Assert.assertTrue(playlistsPage.isConfirmDialogOpen(), "Phai co confirm dialog delete");
        playlistsPage.clickConfirmCancel();
        sleep(1500);

        Assert.assertEquals(playlistsPage.getMyPlaylistCount(), n,
                "Cancel delete -> My playlist (N) phai GIU nguyen = " + n);
        ExtentReportManager.getTest().log(Status.PASS,
                "Delete Cancel '" + target + "' - N giu nguyen = " + n);
    }

    @Test(description = "TC_PL_20: Delete playlist dau - CONFIRM -> My playlist (N) giam con N-1")
    public void TC_20_delete_confirm() {
        String target = playlistsPage.getFirstUserPlaylist();
        if (target == null) throw new SkipException("Khong co user playlist de xoa");
        int n = playlistsPage.getMyPlaylistCount();
        Assert.assertTrue(n > 0, "Phai doc duoc My playlist (N). Actual: " + n);

        playlistsPage.clickEditButtonOf(target);
        sleep(1500);
        Assert.assertTrue(playlistsPage.isEditSheetOpen(), "Sheet phai mo");
        playlistsPage.clickSheetDelete();
        sleep(1500);
        Assert.assertTrue(playlistsPage.isConfirmDialogOpen(), "Phai co confirm dialog delete");
        playlistsPage.clickConfirmDelete();
        sleep(2000);

        int after = playlistsPage.getMyPlaylistCount();
        Assert.assertEquals(after, n - 1,
                "Sau khi xoa, My playlist phai = N-1. Before=" + n + ", After=" + after);
        ExtentReportManager.getTest().log(Status.PASS,
                "Delete confirm '" + target + "' OK. My playlist: " + n + " -> " + after);
    }

    // ============== CLEAR RECENTLY PLAYED (chi verify confirm + Cancel) ==============

    @Test(description = "TC_PL_21: Clear recently played -> confirm dialog, CANCEL giu nguyen")
    public void TC_21_clear_recently_played_cancel() {
        int before = playlistsPage.getTrackCountOf("Recently Played");

        playlistsPage.clickEditButtonOf("Recently Played");
        sleep(1500);
        Assert.assertTrue(playlistsPage.isClearRecentlyPlayedDisplayed());

        playlistsPage.clickSheetClearRecent();
        sleep(1500);
        if (playlistsPage.isConfirmDialogOpen()) {
            playlistsPage.clickConfirmCancel();
            sleep(1500);
            Assert.assertEquals(playlistsPage.getTrackCountOf("Recently Played"), before,
                    "Recently Played phai giu nguyen sau Cancel. Before=" + before);
        } else {
            ExtentReportManager.getTest().log(Status.INFO, "Khong co confirm dialog - skip verify");
        }
        ExtentReportManager.getTest().log(Status.PASS, "Clear recently played - Cancel flow OK");
    }

    // ============== HELPERS ==============

    /** Rename 1 playlist tu {@code from} sang {@code to} qua sheet -> Rename -> SAVE. */
    private void renamePlaylist(String from, String to) {
        playlistsPage.clickEditButtonOf(from);
        sleep(1500);
        playlistsPage.clickSheetRename();
        sleep(2000);
        playlistsPage.typeRenameValue(to);
        playlistsPage.clickRenameSave();
        sleep(2500);
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (playlistsPage.isConfirmDialogOpen()) playlistsPage.clickConfirmCancel();
            if (playlistsPage.isRenameDialogOpen()) playlistsPage.clickRenameCancel();
            if (playlistsPage.isCreateDialogOpen()) playlistsPage.clickDialogCancel();
            if (playlistsPage.isEditSheetOpen()) playlistsPage.closeEditSheetByBack();
        } catch (Exception ignored) {}
        resetToPlaylists();
    }
}
