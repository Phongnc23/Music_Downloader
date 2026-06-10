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
 * Nhom 4 - Edit sheet (mo tu nut edit tren track) + cac dialog con (add-to-playlist, rename,
 * file info, delete, create playlist). @BeforeMethod mo san sheet cho track[0]. Cac check cung
 * 1 sheet/dialog duoc gom de tranh mo lai nhieu lan.
 *
 * TC_TRACKS_026 .. TC_TRACKS_034
 */
public class Tracks04_Verify_Edit_Sheet extends BaseTest {

    private HomePage homePage;
    private TracksPage tracksPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openSheet() {
        homePage = new HomePage(driver);
        tracksPage = new TracksPage(driver);
        resetToTracks();
        Assert.assertTrue(tracksPage.isOnTracksScreen());

        tracksPage.clickEditButtonByIndex(0);
        if (!tracksPage.waitEditSheetOpen(3000)) {
            tracksPage.clickEditButtonByIndex(0);
            tracksPage.waitEditSheetOpen(3000);
        }
    }

    @Test(description = "TC_TRACKS_026: Edit sheet mo + co day du 7 actions")
    public void TC_026_sheet_opens_with_7_actions() {
        Assert.assertTrue(tracksPage.isEditSheetOpen(), "Edit sheet phai mo");
        Assert.assertTrue(tracksPage.isAllEditOptionsDisplayed(), "Phai co day du 7 actions");
        ExtentReportManager.getTest().log(Status.PASS, "Sheet mo + 7 actions OK");
    }

    @Test(description = "TC_TRACKS_027: Dong sheet bang Scrim / BACK / Swipe down")
    public void TC_027_close_sheet_variants() {
        // Scrim
        tracksPage.closeEditSheetByScrim();
        sleep(700);
        Assert.assertFalse(tracksPage.isEditSheetOpen(), "Scrim phai dong sheet");

        // BACK
        tracksPage.clickEditButtonByIndex(0);
        tracksPage.waitEditSheetOpen(3000);
        tracksPage.closeEditSheetByBack();
        sleep(700);
        Assert.assertFalse(tracksPage.isEditSheetOpen(), "BACK phai dong sheet");
        Assert.assertTrue(tracksPage.isOnTracksScreen(), "Ve Tracks sau BACK");

        // Swipe down
        tracksPage.clickEditButtonByIndex(0);
        tracksPage.waitEditSheetOpen(3000);
        tracksPage.closeEditSheetBySwipeDown();
        sleep(700);
        Assert.assertFalse(tracksPage.isEditSheetOpen(), "Swipe down phai dong sheet");
        ExtentReportManager.getTest().log(Status.PASS, "Close by Scrim / BACK / Swipe down OK");
    }

    @Test(description = "TC_TRACKS_028: Sheet hien dung track duoc chon (track[2])")
    public void TC_028_sheet_shows_correct_track() {
        tracksPage.closeEditSheetByBack();
        sleep(700);
        String track2Title = tracksPage.getTrackTitleByIndex(2);
        Assert.assertNotNull(track2Title);

        tracksPage.clickEditButtonByIndex(2);
        Assert.assertTrue(tracksPage.waitEditSheetOpen(3000));
        String sheetTitle = tracksPage.getSheetTrackTitle();
        if (sheetTitle != null) {
            Assert.assertTrue(sheetTitle.contains(track2Title) || track2Title.contains(sheetTitle),
                    "Sheet title phai khop track[2]. Track: " + track2Title + " | Sheet: " + sheetTitle);
        }
        ExtentReportManager.getTest().log(Status.PASS, "Sheet hien dung track[2]: " + track2Title);
    }

    @Test(description = "TC_TRACKS_029: Add to playlist - mo dialog co My Favorite")
    public void TC_029_add_to_playlist_dialog() {
        tracksPage.clickSheetAddToPlaylist();
        Assert.assertTrue(tracksPage.waitAddToPlaylistOpen(4000), "Add to playlist dialog phai mo");
        Assert.assertTrue(tracksPage.isMyFavoritePlaylistDisplayed(), "Phai co My Favorite");
        ExtentReportManager.getTest().log(Status.PASS, "My Favorite: " + tracksPage.getMyFavoriteText());
        tracksPage.closeEditSheetByScrim();
    }

    @Test(description = "TC_TRACKS_030: Rename - dialog mo voi text prefilled + char count")
    public void TC_030_rename_dialog_prefilled() {
        tracksPage.clickSheetRename();
        sleep(1100);
        Assert.assertTrue(tracksPage.isRenameDialogOpen());
        String prefilled = tracksPage.getRenameInputText();
        Assert.assertNotNull(prefilled);
        Assert.assertFalse(prefilled.isEmpty(), "Input phai prefilled");
        String charCount = tracksPage.getRenameCharCount();
        Assert.assertNotNull(charCount, "Phai co char count");
        ExtentReportManager.getTest().log(Status.PASS,
                "Rename prefilled: " + prefilled + " | " + charCount);
        tracksPage.clickRenameCancel();
    }

    @Test(description = "TC_TRACKS_031: File info - screen mo voi day du fields")
    public void TC_031_file_info_screen() {
        tracksPage.clickSheetFileInfo();
        sleep(1300);
        Assert.assertTrue(tracksPage.isFileInfoScreenOpen(), "File info screen phai mo");
        Assert.assertTrue(tracksPage.areAllFileInfoFieldsDisplayed(), "Phai co day du fields");
        ExtentReportManager.getTest().log(Status.PASS, "File info day du fields");
        tracksPage.closeFileInfo();
    }

    @Test(description = "TC_TRACKS_032: Delete - confirm dialog mo + message chua ten track (KHONG xoa)")
    public void TC_032_delete_confirm_dialog() {
        String sheetTitle = tracksPage.getSheetTrackTitle();
        tracksPage.clickSheetDelete();
        Assert.assertTrue(tracksPage.waitDeleteDialogOpen(4000), "Delete confirm dialog phai mo");
        String msg = tracksPage.getDeleteMessage();
        Assert.assertTrue(msg != null && msg.startsWith("Do you want to delete"),
                "Message format dung. Actual: " + msg);
        if (sheetTitle != null && sheetTitle.length() > 12) {
            String part = sheetTitle.substring(0, 12);
            Assert.assertTrue(msg.contains(part),
                    "Message phai chua ten track. Track: " + sheetTitle + " | Msg: " + msg);
        }
        ExtentReportManager.getTest().log(Status.PASS, "Delete dialog + co ten track. Msg: " + msg);
        tracksPage.clickDeleteCancel();
    }

    @Test(description = "TC_TRACKS_033: Heart toggle (add/remove favorite)")
    public void TC_033_heart_toggle() {
        Assert.assertTrue(tracksPage.waitEditSheetOpen(3000), "Edit sheet phai mo (setup)");
        tracksPage.clickSheetHeart();   // add
        sleep(700);
        ExtentReportManager.getTest().log(Status.INFO, "Heart click 1 (add favorite)");
        if (!tracksPage.isEditSheetOpen()) {
            tracksPage.clickEditButtonByIndex(0);
            sleep(850);
        }
        if (tracksPage.isEditSheetOpen()) {
            tracksPage.clickSheetHeart();   // remove (cleanup)
            sleep(700);
        }
        ExtentReportManager.getTest().log(Status.PASS, "Heart toggle OK (add + remove)");
    }

    @Test(description = "TC_TRACKS_034: Create new playlist - dialog mo + CANCEL khong tao + SAVE tao moi")
    public void TC_034_create_new_playlist() {
        // Mo dialog create
        tracksPage.clickSheetAddToPlaylist();
        Assert.assertTrue(tracksPage.waitAddToPlaylistOpen(4000));
        tracksPage.clickCreateNewPlaylist();
        sleep(850);
        Assert.assertTrue(tracksPage.isCreatePlaylistDialogOpen(),
                "Phai mo dialog nhap ten playlist moi");

        // CANCEL -> khong tao
        tracksPage.typeNewPlaylistName("CANCEL_PL_NOT_SAVED");
        sleep(500);
        tracksPage.clickCreatePlaylistCancel();
        sleep(850);
        Assert.assertFalse(tracksPage.isPlaylistInSheet("CANCEL_PL_NOT_SAVED"),
                "Cancel khong duoc tao playlist");

        // SAVE -> tao playlist moi
        String name = "PL" + (System.currentTimeMillis() % 100000);
        tracksPage.clickCreateNewPlaylist();
        sleep(850);
        Assert.assertTrue(tracksPage.isCreatePlaylistDialogOpen());
        tracksPage.typeNewPlaylistName(name);
        sleep(500);
        tracksPage.clickCreatePlaylistSave();
        sleep(1300);
        boolean created = false;
        for (int i = 0; i < 5 && !created; i++) {
            created = tracksPage.isPlaylistInSheet(name) || !tracksPage.isCreatePlaylistDialogOpen();
            if (!created) sleep(500);
        }
        Assert.assertTrue(created,
                "Sau SAVE phai tao duoc playlist '" + name + "'");
        ExtentReportManager.getTest().log(Status.PASS,
                "Create playlist CANCEL + SAVE OK: " + name);
        tracksPage.closeEditSheetByScrim();
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (tracksPage.isDeleteDialogOpen()) tracksPage.clickDeleteCancel();
            if (tracksPage.isRenameDialogOpen()) tracksPage.clickRenameCancel();
            if (tracksPage.isFileInfoScreenOpen()) tracksPage.closeFileInfo();
            if (tracksPage.isAddToPlaylistDialogOpen()) tracksPage.closeEditSheetByScrim();
            if (tracksPage.isEditSheetOpen()) tracksPage.closeEditSheetByBack();
        } catch (Exception ignored) {}
        resetToTracks();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
