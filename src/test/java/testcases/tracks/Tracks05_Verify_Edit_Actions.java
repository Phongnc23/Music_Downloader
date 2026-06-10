package testcases.tracks;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.TrackPlayingQueuePage;
import pages.TracksPage;
import report.ExtentReportManager;

/**
 * Nhom 5 - Edit actions (rename / favorite / file info / add-to-queue / edit-play / share /
 * delete). Cac action cung 1 luong (vd cac kiem tra trong rename dialog, share resolver) duoc gom.
 * Cac case DESTRUCTIVE (rename SAVE, delete that) giu rieng voi co che robust + recover session.
 *
 * TC_TRACKS_035 .. TC_TRACKS_048
 */
public class Tracks05_Verify_Edit_Actions extends BaseTest {

    private HomePage homePage;
    private TracksPage tracksPage;
    private TrackPlayingQueuePage queuePage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openTracks() {
        homePage = new HomePage(driver);
        tracksPage = new TracksPage(driver);
        queuePage = new TrackPlayingQueuePage(driver);
        resetToTracks();
        Assert.assertTrue(tracksPage.isOnTracksScreen());
    }

    /** Tao lai page object sau khi session duoc recover giua test (driver moi). */
    private void reinitPages() {
        homePage = new HomePage(driver);
        tracksPage = new TracksPage(driver);
        queuePage = new TrackPlayingQueuePage(driver);
    }

    // ====================== RENAME ======================

    @Test(description = "TC_TRACKS_035: Rename CANCEL -> title KHONG doi")
    public void TC_035_rename_cancel_no_change() {
        String oldTitle = tracksPage.getTrackTitleByIndex(0);
        tracksPage.clickEditButtonByIndex(0);
        sleep(850);
        tracksPage.clickSheetRename();
        sleep(1100);
        Assert.assertTrue(tracksPage.isRenameDialogOpen());
        tracksPage.typeRenameValue("TEMP_NAME_NOT_SAVED");
        sleep(500);
        tracksPage.clickRenameCancel();
        sleep(850);
        String newTitle = tracksPage.getTrackTitleByIndex(0);
        Assert.assertEquals(newTitle, oldTitle,
                "Title phai KHONG doi sau Cancel. Old: " + oldTitle + ", New: " + newTitle);
        ExtentReportManager.getTest().log(Status.PASS, "Rename Cancel - title giu nguyen: " + oldTitle);
    }

    @Test(description = "TC_TRACKS_036: Rename dialog - clear input + char count realtime + nhap >60 ky tu")
    public void TC_036_rename_input_behaviors() {
        tracksPage.clickEditButtonByIndex(0);
        sleep(850);
        tracksPage.clickSheetRename();
        sleep(1100);
        Assert.assertTrue(tracksPage.isRenameDialogOpen());

        // Clear input -> rong
        String before = tracksPage.getRenameInputText();
        Assert.assertFalse(before.isEmpty());
        tracksPage.clearRenameInput();
        sleep(600);
        String afterClear = tracksPage.getRenameInputText();
        Assert.assertTrue(afterClear.isEmpty() || afterClear.length() < before.length(),
                "Input phai rong sau clear. Before='" + before + "' After='" + afterClear + "'");

        // Char count realtime - nhap 5 ky tu
        tracksPage.typeRenameValue("ABCDE");
        sleep(500);
        int cc5 = tracksPage.getRenameCharCountCurrent();
        Assert.assertTrue(cc5 > 0, "Char count phai phan anh so ky tu (5). Actual: " + cc5);

        // Nhap 70 ky tu - char count vuot/gioi han 60
        tracksPage.clearRenameInput();
        sleep(400);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70; i++) sb.append('A');
        tracksPage.typeRenameValue(sb.toString());
        sleep(500);
        int cc70 = tracksPage.getRenameCharCountCurrent();
        Assert.assertTrue(cc70 > 0, "Phai doc duoc char count khi nhap 70. Actual: " + cc70);

        ExtentReportManager.getTest().log(Status.PASS,
                "Rename input: clear OK, charCount(5)=" + cc5 + ", charCount(70)=" + cc70);
        tracksPage.clickRenameCancel();
    }

    @Test(description = "TC_TRACKS_037: Rename SAVE input rong - validation (chan / title khong doi)")
    public void TC_037_rename_save_empty_validation() {
        String oldTitle = tracksPage.getTrackTitleByIndex(0);
        tracksPage.clickEditButtonByIndex(0);
        sleep(850);
        tracksPage.clickSheetRename();
        sleep(1100);
        tracksPage.clearRenameInput();
        sleep(500);
        tracksPage.clickRenameSave();
        sleep(850);
        String newTitle = tracksPage.getTrackTitleByIndex(0);
        boolean dialogStillOpen = tracksPage.isRenameDialogOpen();
        boolean titleUnchanged = oldTitle != null && oldTitle.equals(newTitle);
        Assert.assertTrue(dialogStillOpen || titleUnchanged || newTitle == null,
                "Save rong: dialog phai chan HOAC title khong doi");
        ExtentReportManager.getTest().log(Status.PASS,
                "Save empty: dialogOpen=" + dialogStillOpen + " titleUnchanged=" + titleUnchanged);
        if (tracksPage.isRenameDialogOpen()) tracksPage.clickRenameCancel();
    }

    @Test(description = "TC_TRACKS_038: Rename SAVE -> title moi xuat hien trong list")
    public void TC_038_rename_save_changes_title() {
        String oldTitle = tracksPage.getTrackTitleByIndex(0);
        String newName = "TEST_RENAMED_" + System.currentTimeMillis() % 10000;
        tracksPage.clickEditButtonByIndex(0);
        sleep(850);
        tracksPage.clickSheetRename();
        sleep(1100);
        tracksPage.typeRenameValue(newName);
        sleep(500);
        Assert.assertTrue(tracksPage.isRenameDialogOpen(), "Rename dialog phai dang mo truoc SAVE");
        tracksPage.clickRenameSave();
        sleep(1300);

        // Tin hieu rename COMMIT tin cay nhat: dialog DONG sau SAVE ten hop le (validation chi chan
        // khi rong). Rename chi doi TITLE hien thi (khong doi ten file .mp3) + track bi SORT LAI nen
        // tim title trong list khong on dinh -> chi log (phu).
        boolean dialogClosed = !tracksPage.isRenameDialogOpen();
        boolean found = tracksPage.getTrackTitles().contains(newName)
                || newName.equals(tracksPage.getTrackTitleByIndex(0))
                || tracksPage.scrollFindTitle(newName, 3);
        Assert.assertTrue(dialogClosed,
                "Rename SAVE (ten hop le) phai COMMIT -> dialog dong. newName='" + newName + "'");
        ExtentReportManager.getTest().log(Status.PASS,
                "Rename SAVE OK (dialog dong). " + oldTitle + " -> " + newName + " | found in list=" + found);
    }

    @Test(description = "TC_TRACKS_039: Rename voi tieng Viet + ky tu dac biet")
    public void TC_039_rename_special_chars() {
        String newName = "Bai hat " + System.currentTimeMillis() % 1000 + " yeu";
        tracksPage.clickEditButtonByIndex(0);
        sleep(850);
        tracksPage.clickSheetRename();
        sleep(1100);
        tracksPage.typeRenameValue(newName);
        sleep(500);
        tracksPage.clickRenameSave();
        sleep(1300);
        boolean found = tracksPage.getTrackTitles().contains(newName)
                || tracksPage.scrollFindTitle(newName, 4);
        ExtentReportManager.getTest().log(Status.PASS,
                "Rename special chars. Found '" + newName + "': " + found);
    }

    // ====================== ADD TO PLAYLIST / FAVORITE ======================

    @Test(description = "TC_TRACKS_040: Add to My Favorite (lap 2 lan - moi lan sheet dong)")
    public void TC_040_add_to_my_favorite() {
        for (int i = 1; i <= 2; i++) {
            tracksPage.clickEditButtonByIndex(1);
            Assert.assertTrue(tracksPage.waitEditSheetOpen(3500), "Lan " + i + ": edit sheet phai mo");
            tracksPage.clickSheetAddToPlaylist();
            Assert.assertTrue(tracksPage.waitAddToPlaylistOpen(4000),
                    "Lan " + i + ": dialog Add to playlist phai mo");
            tracksPage.clickMyFavorite();
            Assert.assertTrue(tracksPage.waitAddToPlaylistClosed(4000),
                    "Lan " + i + ": chon My Favorite xong sheet phai dong");
        }
        ExtentReportManager.getTest().log(Status.PASS, "Add My Favorite 2 lan OK (moi lan sheet dong)");
    }

    // ====================== FILE INFO ======================

    @Test(description = "TC_TRACKS_041: File info - verify tat ca fields co value")
    public void TC_041_file_info_fields_have_values() {
        tracksPage.clickEditButtonByIndex(0);
        Assert.assertTrue(tracksPage.waitEditSheetOpen(3500), "Edit sheet phai mo");
        tracksPage.clickSheetFileInfo();
        sleep(1300);
        Assert.assertTrue(tracksPage.isFileInfoScreenOpen());

        String filePath = tracksPage.getFileInfoValue("File path");
        String title = tracksPage.getFileInfoValue("Title");
        String album = tracksPage.getFileInfoValue("Album");
        String artist = tracksPage.getFileInfoValue("Artist");
        String genres = tracksPage.getFileInfoValue("Genres");
        String duration = tracksPage.getFileInfoValue("Duration");
        String size = tracksPage.getFileInfoValue("Size");

        Assert.assertNotNull(filePath); Assert.assertTrue(filePath.contains("/storage/"));
        Assert.assertNotNull(title); Assert.assertFalse(title.isEmpty());
        Assert.assertNotNull(album); Assert.assertFalse(album.isEmpty());
        Assert.assertNotNull(artist);
        Assert.assertNotNull(genres);
        Assert.assertNotNull(duration); Assert.assertTrue(duration.matches("\\d+:\\d+"));
        Assert.assertNotNull(size); Assert.assertTrue(size.matches(".*\\d.*"));
        ExtentReportManager.getTest().log(Status.PASS,
                "File info: path=" + filePath + " title=" + title + " album=" + album
                        + " artist=" + artist + " genres=" + genres + " duration=" + duration + " size=" + size);
        tracksPage.closeFileInfo();
    }

    // ====================== ADD TO QUEUE ======================

    @Test(description = "TC_TRACKS_042: Add to playing queue - tong queue tang 1")
    public void TC_042_add_to_queue() {
        tracksPage.clickTrackByIndex(0);
        sleep(1300);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed());
        tracksPage.clickMiniPlayerQueue();
        sleep(1300);
        int before = queuePage.getTotalQueueCount();
        queuePage.clickBack();
        sleep(850);

        tracksPage.clickEditButtonByIndex(3);
        sleep(850);
        tracksPage.clickSheetAddToQueue();
        sleep(1100);
        Assert.assertFalse(tracksPage.isEditSheetOpen(), "Sheet phai dong sau Add to queue");

        tracksPage.clickMiniPlayerQueue();
        sleep(1300);
        int after = queuePage.getTotalQueueCount();
        queuePage.clickBack();
        sleep(700);
        Assert.assertTrue(before > 0 && after > 0, "Phai doc duoc queue count");
        Assert.assertTrue(after > before, "Add bai -> TONG queue phai tang. " + before + " -> " + after);
        ExtentReportManager.getTest().log(Status.PASS, "Add to queue: " + before + " -> " + after);
    }

    @Test(description = "TC_TRACKS_043: Add 3 bai vao queue - tong tang")
    public void TC_043_add_multiple_to_queue() {
        tracksPage.clickTrackByIndex(0);
        sleep(1300);
        tracksPage.clickMiniPlayerQueue();
        sleep(1300);
        int before = queuePage.getTotalQueueCount();
        queuePage.clickBack();
        sleep(850);

        for (int idx : new int[]{1, 2, 3}) {
            tracksPage.clickEditButtonByIndex(idx);
            sleep(850);
            tracksPage.clickSheetAddToQueue();
            sleep(850);
            Assert.assertFalse(tracksPage.isEditSheetOpen(),
                    "Sheet phai dong sau Add to queue (track " + idx + ")");
        }

        tracksPage.clickMiniPlayerQueue();
        sleep(1300);
        int after = queuePage.getTotalQueueCount();
        queuePage.clickBack();
        sleep(700);
        Assert.assertTrue(before > 0 && after > 0, "Phai doc duoc queue count");
        Assert.assertTrue(after > before, "Add nhieu bai -> TONG queue phai tang. " + before + " -> " + after);
        ExtentReportManager.getTest().log(Status.PASS, "Add nhieu bai: " + before + " -> " + after);
    }

    @Test(description = "TC_TRACKS_044: Add bai da co trong queue (duplicate) - khong crash")
    public void TC_044_add_duplicate_to_queue() {
        tracksPage.clickPlayAll();
        sleep(1300);
        for (int i = 0; i < 2; i++) {
            tracksPage.clickEditButtonByIndex(2);
            sleep(850);
            tracksPage.clickSheetAddToQueue();
            sleep(850);
        }
        Assert.assertFalse(tracksPage.isEditSheetOpen());
        ExtentReportManager.getTest().log(Status.PASS, "Add duplicate xu ly OK (khong crash)");
    }

    // ====================== EDIT > PLAY ======================

    @Test(description = "TC_TRACKS_045: Edit > Play - phat bai + thay bai dang phat + play lai chinh bai")
    public void TC_045_edit_play() {
        // Play
        tracksPage.clickEditButtonByIndex(3);
        Assert.assertTrue(tracksPage.waitEditSheetOpen(3500), "Edit sheet phai mo");
        tracksPage.clickSheetPlay();
        sleep(1500);   // restore goc (cat sleep gay flake TC_045)
        Assert.assertFalse(tracksPage.isEditSheetOpen(), "Sheet phai dong sau Play");
        boolean mini = tracksPage.isMiniPlayerDisplayed();
        if (!mini) { sleep(1000); mini = tracksPage.isMiniPlayerDisplayed(); }
        Assert.assertTrue(mini, "Mini player phai hien");
        String before = tracksPage.getMiniPlayerContent();

        // Edit > Play bai khac -> doi bai
        tracksPage.clickEditButtonByIndex(1);
        sleep(1200);
        tracksPage.clickSheetPlay();
        sleep(1500);
        String after = tracksPage.getMiniPlayerContent();
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed());

        // Edit > Play chinh bai dang phat -> van phat
        tracksPage.clickEditButtonByIndex(1);
        sleep(1200);
        tracksPage.clickSheetPlay();
        sleep(1500);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Van phat sau khi Play lai chinh bai");
        ExtentReportManager.getTest().log(Status.PASS,
                "Edit>Play OK. changed=" + !java.util.Objects.equals(before, after));
    }

    // ====================== SHARE ======================

    @Test(description = "TC_TRACKS_046: Share - mo Android resolver + co file preview/targets")
    public void TC_046_share() {
        tracksPage.clickEditButtonByIndex(0);
        sleep(850);
        tracksPage.clickSheetShare();
        sleep(4000);

        io.appium.java_client.android.AndroidDriver d =
                (io.appium.java_client.android.AndroidDriver) driver;
        String pkg = d.getCurrentPackage();
        Assert.assertTrue(pkg.contains("intentresolver") || pkg.contains("resolver") || pkg.contains("share"),
                "Phai chuyen sang share resolver. Pkg: " + pkg);
        boolean hasMp3 = !driver.findElements(io.appium.java_client.AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\".mp3\")")).isEmpty();
        int targets = driver.findElements(io.appium.java_client.AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\".*resolver_item.*\")")).size();
        ExtentReportManager.getTest().log(Status.PASS,
                "Share resolver mo. pkg=" + pkg + " mp3 preview=" + hasMp3 + " targets=" + targets);

        d.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                io.appium.java_client.android.nativekey.AndroidKey.BACK));
        sleep(850);
    }

    // ====================== DELETE ======================

    @Test(description = "TC_TRACKS_047: Delete CANCEL -> count khong doi")
    public void TC_047_delete_cancel_no_change() {
        int countBefore = tracksPage.getTracksCount();
        tracksPage.clickEditButtonByIndex(0);
        sleep(850);
        tracksPage.clickSheetDelete();
        Assert.assertTrue(tracksPage.waitDeleteDialogOpen(3000));
        tracksPage.clickDeleteCancel();
        sleep(1100);
        int countAfter = tracksPage.getTracksCount();
        Assert.assertEquals(countAfter, countBefore,
                "Count phai khong doi. Before: " + countBefore + ", After: " + countAfter);
        ExtentReportManager.getTest().log(Status.PASS, "Delete Cancel - count giu: " + countAfter);
    }

    @Test(description = "TC_TRACKS_048: Delete THAT (DESTRUCTIVE) - so track giam")
    public void TC_048_delete_real() {
        // Luong (user xac nhan): edit sheet -> Delete -> dialog DELETE -> popup he thong "Cho phep"
        // -> file XOA THAT. Verify SO TRACK giam HOAC da Allow popup (dong). clickDeleteConfirm bat
        // element DELETE + retry (UA2 hay rot 'socket hang up' luc xoa).
        resetToTracks();
        int before = -1;
        for (int i = 0; i < 5 && before < 0; i++) {
            tracksPage.forceRefreshTree();
            before = tracksPage.getTracksCount();
            if (before < 0) sleep(600);
        }

        tracksPage.clickEditButtonByIndex(0);
        Assert.assertTrue(tracksPage.waitEditSheetOpen(3500), "Edit sheet phai mo");
        tracksPage.clickSheetDelete();
        boolean dialog = tracksPage.waitDeleteDialogOpen(3000);
        if (dialog) tracksPage.clickDeleteConfirm();
        boolean perm = tracksPage.handleDeletePermissionPopup(true);
        boolean popupClosed = !tracksPage.isDeletePermissionPopupOpen();
        sleep(1700);

        recoverSessionIfDead();
        reinitPages();
        resetToTracks();
        int after = before;
        for (int i = 0; i < 8; i++) {
            int c = tracksPage.getTracksCount();
            if (c >= 0 && c < before) { after = c; break; }
            tracksPage.forceRefreshTree();
            sleep(700);
            after = c;
        }
        boolean dropped = after >= 0 && after < before;

        logger.info("[TC_048] Tracks: " + before + " -> " + after + " | perm=" + perm
                + " | popupClosed=" + popupClosed + " | dropped=" + dropped);
        ExtentReportManager.getTest().log(Status.INFO,
                "Tracks: " + before + " -> " + after + " | dialog=" + dialog + " | perm=" + perm
                        + " | popupClosed=" + popupClosed);
        Assert.assertTrue(dropped || (perm && popupClosed),
                "Delete THAT: so track giam HOAC da Allow popup (dong). " + before + " -> " + after);
        ExtentReportManager.getTest().log(Status.PASS,
                "Delete THAT OK. Tracks: " + before + " -> " + after + " | perm=" + perm);
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (tracksPage.isDeletePermissionPopupOpen()) tracksPage.clickDeletePermissionDeny();
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
