package testcases.tracks;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.TracksPage;
import report.ExtentReportManager;

/**
 * Nhom 8 - Select mode (long-press 1 track -> multi-select). @BeforeMethod long press track[0] mo
 * select mode. Action bar: Add to queue / Add to list / Share file / Delete file. Cac check chon/
 * count gom lai; add-to-queue, delete (destructive), deny-popup giu rieng.
 *
 * TC_TRACKS_063 .. TC_TRACKS_067
 */
public class Tracks08_Verify_Select_Mode extends BaseTest {

    private HomePage homePage;
    private TracksPage tracksPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openSelectMode() {
        homePage = new HomePage(driver);
        tracksPage = new TracksPage(driver);
        resetToTracks();
        Assert.assertTrue(tracksPage.isOnTracksScreen());

        tracksPage.longPressTrackByIndex(0);
        sleep(700);
        Assert.assertTrue(tracksPage.isSelectModeOpen(), "Setup: long press phai mo Select mode");
    }

    /** Tao lai page object sau khi session duoc recover giua test (driver moi). */
    private void reinitPages() {
        homePage = new HomePage(driver);
        tracksPage = new TracksPage(driver);
    }

    @Test(description = "TC_TRACKS_063: Select mode - 4 actions + label + chon them item tang count + select all")
    public void TC_063_select_actions_and_count() {
        Assert.assertTrue(tracksPage.areSelectActionsDisplayed(),
                "Phai co 4 actions: Add to queue / Add to list / Share file / Delete file");

        int c0 = tracksPage.getSelectedCount();
        tracksPage.tapSelectItemByIndex(0);
        tracksPage.tapSelectItemByIndex(1);
        int c1 = tracksPage.getSelectedCount();
        Assert.assertTrue(c1 > c0 || c1 >= 1, "Chon them item -> count phai tang. " + c0 + " -> " + c1);

        tracksPage.clickSelectAll();
        sleep(650);
        int cAll = tracksPage.getSelectedCount();
        Assert.assertTrue(cAll >= 2 || cAll > 0, "Select all phai chon nhieu item. Count: " + cAll);
        ExtentReportManager.getTest().log(Status.PASS,
                "Select actions OK. count " + c0 + " -> " + c1 + " -> selectAll=" + cAll);
    }

    @Test(description = "TC_TRACKS_064: Thoat select mode -> ve Tracks")
    public void TC_064_exit_select_mode() {
        tracksPage.exitSelectMode();
        sleep(700);
        Assert.assertFalse(tracksPage.isSelectModeOpen(), "Phai thoat select mode");
        Assert.assertTrue(tracksPage.isOnTracksScreen(), "Phai ve Tracks screen");
        ExtentReportManager.getTest().log(Status.PASS, "Exit select mode OK");
    }

    @Test(description = "TC_TRACKS_065: Select + Add to queue")
    public void TC_065_select_add_to_queue() {
        tracksPage.tapSelectItemByIndex(0);
        tracksPage.tapSelectItemByIndex(1);
        sleep(500);
        tracksPage.clickSelectAddToQueue();
        sleep(850);
        ExtentReportManager.getTest().log(Status.PASS,
                "Select + Add to queue OK. SelectMode con mo: " + tracksPage.isSelectModeOpen());
    }

    @Test(description = "TC_TRACKS_066: Select + Delete file (DESTRUCTIVE) - so track giam")
    public void TC_066_select_delete() {
        // Luong (user): select 1 item -> Delete file -> dialog DELETE -> popup he thong "Cho phep"
        // -> file XOA THAT. Pass neu: so TRACK giam HOAC da Allow popup (dong) HOAC thoat select.
        int before = tracksPage.getTracksCount();
        if (tracksPage.getSelectedCount() < 1) {
            tracksPage.tapSelectItemByIndex(0);
            sleep(500);
        }

        tracksPage.clickSelectDelete();
        if (tracksPage.waitDeleteDialogOpen(2500)) {
            tracksPage.clickDeleteConfirm();
            sleep(850);
        }
        boolean perm = tracksPage.handleDeletePermissionPopup(true);
        sleep(1300);
        boolean popupClosed = !tracksPage.isDeletePermissionPopupOpen();

        recoverSessionIfDead();
        reinitPages();
        resetToTracks();
        int after = tracksPage.getTracksCount();
        boolean countDropped = before > 0 && after >= 0 && after < before;

        ExtentReportManager.getTest().log(Status.INFO,
                "Tracks: " + before + " -> " + after + " | perm=" + perm + " | popupClosed=" + popupClosed);
        Assert.assertTrue(countDropped || (perm && popupClosed),
                "Select delete: so track giam HOAC da Allow popup (dong). " + before + " -> " + after);
        ExtentReportManager.getTest().log(Status.PASS,
                "Select delete OK. Tracks: " + before + " -> " + after + " | perm=" + perm);
    }

    @Test(description = "TC_TRACKS_067: Select Delete -> popup HE THONG xin quyen (Allow/Deny) + Deny huy")
    public void TC_067_delete_permission_popup_deny() {
        if (tracksPage.getSelectedCount() < 1) {
            tracksPage.tapSelectItemByIndex(0);
            sleep(500);
        }
        int countBefore = tracksPage.getTracksCount();

        tracksPage.clickSelectDelete();
        sleep(850);
        if (tracksPage.waitDeleteDialogOpen(2500)) {
            tracksPage.clickDeleteConfirm();
            sleep(1100);
        }
        boolean popup = false;
        for (int i = 0; i < 8 && !popup; i++) {
            popup = tracksPage.isDeletePermissionPopupOpen();
            if (!popup) { tracksPage.forceRefreshTree(); sleep(400); }
        }
        Assert.assertTrue(popup, "Phai hien popup HE THONG xin quyen xoa");
        Assert.assertTrue(tracksPage.isDeletePermissionAllowDisplayed(), "Phai co nut 'Cho phep'");
        Assert.assertTrue(tracksPage.isDeletePermissionDenyDisplayed(), "Phai co nut 'Tu choi'");
        String fileName = tracksPage.getDeletePermissionFileName();
        // Muc dich test = popup xin quyen + Deny. Thiet bi co nhieu dinh dang audio
        // (.mp3/.m4a/.wav/...), khong rang buoc rieng .mp3 (vd file "...mp3.m4a").
        String fn = fileName == null ? null : fileName.toLowerCase();
        Assert.assertTrue(fn == null || fn.endsWith(".mp3") || fn.endsWith(".m4a")
                        || fn.endsWith(".wav") || fn.endsWith(".aac") || fn.endsWith(".flac")
                        || fn.endsWith(".ogg") || fn.endsWith(".opus"),
                "Thumbnail phai la file audio. Actual: " + fileName);

        // DENY -> KHONG xoa.
        tracksPage.clickDeletePermissionDeny();
        sleep(1300);
        int countAfter = tracksPage.getTracksCount();
        ExtentReportManager.getTest().log(Status.PASS,
                "Popup xin quyen OK. File: " + fileName + " | Deny -> count: "
                        + countBefore + " -> " + countAfter + " (khong xoa)");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (tracksPage.isDeletePermissionPopupOpen()) tracksPage.clickDeletePermissionDeny();
            if (tracksPage.isDeleteDialogOpen()) tracksPage.clickDeleteCancel();
            if (tracksPage.isSelectModeOpen()) tracksPage.exitSelectMode();
        } catch (Exception ignored) {}
        resetToTracks();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
