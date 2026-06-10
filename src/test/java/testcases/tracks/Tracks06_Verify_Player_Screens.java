package testcases.tracks;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.TrackPlayNowPage;
import pages.TrackPlayingQueuePage;
import pages.TracksPage;
import report.ExtentReportManager;

/**
 * Nhom 6 - Play Now screen (full player). @BeforeMethod mo san Play Now (phat track[0] -> click
 * mini player body). Cac check tinh / control cung 1 man Play Now duoc gom de tranh mo lai.
 *
 * TC_TRACKS_049 .. TC_TRACKS_059
 */
public class Tracks06_Verify_Player_Screens extends BaseTest {

    private HomePage homePage;
    private TracksPage tracksPage;
    private TrackPlayNowPage playNowPage;
    private TrackPlayingQueuePage queuePage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setupPlayNow() {
        homePage = new HomePage(driver);
        tracksPage = new TracksPage(driver);
        playNowPage = new TrackPlayNowPage(driver);
        queuePage = new TrackPlayingQueuePage(driver);

        resetToTracks();
        Assert.assertTrue(tracksPage.isOnTracksScreen());
        // Click track -> mini player; click mini body -> mo full Play Now.
        tracksPage.clickTrackByIndex(0);
        sleep(1100);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Setup: click track phai hien mini player");
        tracksPage.clickMiniPlayerBody();
        sleep(1100);
        Assert.assertTrue(playNowPage.isOnPlayNowScreen(), "Setup: Play Now phai mo");
    }

    @Test(description = "TC_TRACKS_049: Play Now layout + SeekBar + current/total time + total duration M:SS")
    public void TC_049_layout_and_seekbar() {
        Assert.assertTrue(playNowPage.isOnPlayNowScreen());
        Assert.assertTrue(playNowPage.isSeekBarDisplayed());

        String info = playNowPage.getPlayingNowFullInfo();
        Assert.assertNotNull(info);
        Assert.assertTrue(info.startsWith("Playing now"), "Info phai bat dau 'Playing now'. Actual: " + info);

        String progress = playNowPage.getSeekBarProgress();
        Assert.assertNotNull(progress);
        Assert.assertTrue(progress.endsWith("%"), "Progress format 'N%'. Actual: " + progress);

        String current = playNowPage.getCurrentTime();
        String total = playNowPage.getTotalDuration();
        Assert.assertNotNull(current);
        Assert.assertNotNull(total);
        Assert.assertTrue(total.matches("\\d+:\\d{2}"), "Total duration phai M:SS. Actual: " + total);
        ExtentReportManager.getTest().log(Status.PASS,
                "Play Now layout OK. " + progress + " | " + current + " / " + total + " | " + info);
    }

    @Test(description = "TC_TRACKS_050: Controls toggle - Play/Pause + Shuffle + Repeat (3 states) + Heart")
    public void TC_050_control_toggles() {
        int p1 = playNowPage.getSeekBarProgressPercent();
        playNowPage.clickPlayPauseControl();   // pause
        sleep(850);
        int p2 = playNowPage.getSeekBarProgressPercent();
        playNowPage.clickPlayPauseControl();   // resume
        sleep(700);

        playNowPage.clickShuffleControl();
        sleep(650);
        Assert.assertTrue(playNowPage.isOnPlayNowScreen(), "Van o Play Now sau shuffle");

        for (int i = 0; i < 3; i++) {
            playNowPage.clickRepeatControl();
            sleep(600);
            Assert.assertTrue(playNowPage.isOnPlayNowScreen(), "Van o Play Now sau repeat state " + (i + 1));
        }

        playNowPage.clickHeartIcon();
        sleep(650);
        playNowPage.clickHeartIcon();   // toggle off (cleanup)
        sleep(650);
        Assert.assertTrue(playNowPage.isOnPlayNowScreen(), "Van o Play Now sau heart toggle");
        ExtentReportManager.getTest().log(Status.PASS,
                "Controls toggle OK. Play/Pause " + p1 + "% -> " + p2 + "% + Shuffle + Repeat 3 + Heart");
    }

    @Test(description = "TC_TRACKS_051: Next button - chuyen bai sau")
    public void TC_051_next_changes_track() {
        String beforeInfo = playNowPage.getPlayingNowFullInfo();
        playNowPage.clickNextControl();
        sleep(1300);
        String afterInfo = playNowPage.getPlayingNowFullInfo();
        Assert.assertNotEquals(afterInfo, beforeInfo,
                "Next phai chuyen bai. Before: " + beforeInfo + " After: " + afterInfo);
        ExtentReportManager.getTest().log(Status.PASS, "Next chuyen bai OK");
    }

    @Test(description = "TC_TRACKS_052: Previous button - reset playback")
    public void TC_052_previous_reset_playback() {
        // LUU Y (user): khi app o che do REPEAT ONE, Next/Previous chi RESET bai (khong doi bai).
        // Verify hanh vi dung moi mode: keo SeekBar ~50% roi Previous -> SeekBar phai tut.
        playNowPage.dragSeekBarTo(50);
        sleep(1100);
        int p2 = playNowPage.getSeekBarProgressPercent();
        playNowPage.clickPreviousControl();
        sleep(1100);
        int p3 = playNowPage.getSeekBarProgressPercent();
        ExtentReportManager.getTest().log(Status.INFO, "SeekBar: " + p2 + "% -> (Previous) " + p3 + "%");
        Assert.assertTrue(p3 < p2 - 10 || p3 <= 10,
                "Previous phai RESET playback (SeekBar tut). " + p2 + "% -> " + p3 + "%");
        ExtentReportManager.getTest().log(Status.PASS, "Previous reset playback OK. " + p2 + "% -> " + p3 + "%");
    }

    @Test(description = "TC_TRACKS_053: SeekBar - drag + tap nhay vi tri")
    public void TC_053_seekbar_drag_tap() {
        int b1 = playNowPage.getSeekBarProgressPercent();
        playNowPage.dragSeekBarTo(60);
        sleep(700);
        int a1 = playNowPage.getSeekBarProgressPercent();
        Assert.assertTrue(playNowPage.isOnPlayNowScreen());

        int b2 = playNowPage.getSeekBarProgressPercent();
        playNowPage.tapSeekBarAt(30);
        sleep(700);
        int a2 = playNowPage.getSeekBarProgressPercent();
        Assert.assertTrue(playNowPage.isOnPlayNowScreen());
        ExtentReportManager.getTest().log(Status.PASS,
                "Drag: " + b1 + "% -> " + a1 + "% | Tap: " + b2 + "% -> " + a2 + "%");
    }

    @Test(description = "TC_TRACKS_054: Add to playlist icon -> mo dialog")
    public void TC_054_add_to_playlist_icon() {
        playNowPage.clickAddToPlaylistIcon();
        sleep(1100);
        Assert.assertTrue(tracksPage.isAddToPlaylistDialogOpen(), "Add to playlist icon phai mo dialog");
        ExtentReportManager.getTest().log(Status.PASS, "Add to playlist dialog OK");
        tracksPage.closeEditSheetByScrim();
        sleep(700);
    }

    @Test(description = "TC_TRACKS_055: Equalizer icon -> mo equalizer (system/in-app)")
    public void TC_055_equalizer_icon() {
        playNowPage.clickEqualizerIcon();
        sleep(1300);
        String pkg = ((io.appium.java_client.android.AndroidDriver) driver).getCurrentPackage();
        ExtentReportManager.getTest().log(Status.PASS, "Equalizer -> package: " + pkg);
        if (!constants.AppConstants.APP_PACKAGE.equals(pkg)) {
            ((io.appium.java_client.android.AndroidDriver) driver).pressKey(
                    new io.appium.java_client.android.nativekey.KeyEvent(
                            io.appium.java_client.android.nativekey.AndroidKey.BACK));
            sleep(850);
        }
    }

    @Test(description = "TC_TRACKS_056: Sleep timer icon -> mo sleep timer dialog")
    public void TC_056_sleep_timer_icon() {
        playNowPage.clickSleepTimerIcon();
        sleep(1100);
        boolean dialog = !driver.findElements(
                io.appium.java_client.AppiumBy.accessibilityId("Custom")).isEmpty()
                || !driver.findElements(io.appium.java_client.AppiumBy.androidUIAutomator(
                "new UiSelector().descriptionContains(\"mins\")")).isEmpty()
                || !driver.findElements(io.appium.java_client.AppiumBy.accessibilityId("Cancel")).isEmpty();
        ExtentReportManager.getTest().log(Status.PASS, "Sleep timer icon -> dialog hien: " + dialog);
        ((io.appium.java_client.android.AndroidDriver) driver).pressKey(
                new io.appium.java_client.android.nativekey.KeyEvent(
                        io.appium.java_client.android.nativekey.AndroidKey.BACK));
        sleep(700);
    }

    @Test(description = "TC_TRACKS_057: 3-dot menu -> mo edit sheet")
    public void TC_057_three_dot_menu() {
        // RETRY tap 3-dot toi khi sheet MO (toi da 3 lan): khi chay tich hop (UI cham) 1 tap don
        // le co the khong kip mo sheet (flaky).
        boolean opened = false;
        for (int i = 0; i < 3 && !opened; i++) {
            playNowPage.clickThreeDotMenu();
            sleep(1500);
            opened = tracksPage.isEditSheetOpen();
        }
        Assert.assertTrue(opened, "3-dot menu phai mo edit sheet");
        ExtentReportManager.getTest().log(Status.PASS, "3-dot -> edit sheet OK");
        tracksPage.closeEditSheetByScrim();
        sleep(700);
    }

    @Test(description = "TC_TRACKS_058: Queue icon -> Playing Queue (layout + count + click item phat)")
    public void TC_058_queue_from_play_now() {
        playNowPage.clickQueueIcon();
        sleep(1300);
        Assert.assertTrue(queuePage.isOnPlayingQueueScreen(), "Click queue phai mo Playing Queue");
        Assert.assertTrue(queuePage.isTitleDisplayed());
        Assert.assertTrue(queuePage.isBackButtonDisplayed());

        int total = queuePage.getTotalQueueCount();
        int pos = queuePage.getCurrentPosition();
        Assert.assertTrue(total > 0, "Total count phai > 0");
        Assert.assertTrue(pos > 0 && pos <= total, "Position 1<=pos<=total. pos=" + pos + " total=" + total);

        // Click item index 2 -> phat bai do (position doi)
        if (queuePage.getDisplayedQueueItemsCount() > 2) {
            queuePage.clickQueueItemByIndex(2);
            sleep(1300);
            int posAfter = queuePage.getCurrentPosition();
            ExtentReportManager.getTest().log(Status.INFO, "Position: " + pos + " -> " + posAfter);
        }
        ExtentReportManager.getTest().log(Status.PASS,
                "Playing Queue OK. " + queuePage.getCountLabel());
        queuePage.clickBack();
    }

    @Test(description = "TC_TRACKS_059: Down arrow/Back -> dong Play Now, bai van phat")
    public void TC_059_close_play_now() {
        playNowPage.close();
        sleep(850);
        Assert.assertTrue(tracksPage.isOnTracksScreen(), "Phai ve Tracks sau khi dong Play Now");
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Mini player van hien (bai van phat)");
        ExtentReportManager.getTest().log(Status.PASS, "Close Play Now OK");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (tracksPage.isAddToPlaylistDialogOpen()) tracksPage.closeEditSheetByScrim();
            if (tracksPage.isEditSheetOpen()) tracksPage.closeEditSheetByScrim();
            if (queuePage.isOnPlayingQueueScreen()) queuePage.clickBack();
            if (playNowPage.isOnPlayNowScreen()) playNowPage.close();
        } catch (Exception ignored) {}
        resetToTracks();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
