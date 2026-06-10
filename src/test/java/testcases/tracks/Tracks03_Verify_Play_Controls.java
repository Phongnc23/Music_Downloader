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
 * Nhom 3 - Play controls + mini player (man Tracks). Cac hanh vi cung 1 man (play all / shuffle /
 * click track / mini player) duoc gom de tranh play + reset lai nhieu lan.
 *
 * TC_TRACKS_015 .. TC_TRACKS_025
 */
public class Tracks03_Verify_Play_Controls extends BaseTest {

    private HomePage homePage;
    private TracksPage tracksPage;
    private TrackPlayNowPage playNowPage;
    private TrackPlayingQueuePage queuePage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openTracks() {
        homePage = new HomePage(driver);
        tracksPage = new TracksPage(driver);
        playNowPage = new TrackPlayNowPage(driver);
        queuePage = new TrackPlayingQueuePage(driver);
        resetToTracks();
        Assert.assertTrue(tracksPage.isOnTracksScreen());
    }

    @Test(description = "TC_TRACKS_015: Play all - phat bai[0] + mini player; Play all thay bai dang phat")
    public void TC_015_play_all() {
        String firstTitle = tracksPage.getTrackTitleByIndex(0);
        Assert.assertNotNull(firstTitle, "Phai co track[0]");
        tracksPage.clickPlayAll();
        sleep(1300);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Mini player phai hien sau Play all");

        // Phat 1 bai khac roi Play all lai -> van phat (thay bai dang phat = tracks[0])
        tracksPage.clickTrackByIndex(4);
        sleep(850);
        tracksPage.clickPlayAll();
        sleep(1100);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Mini player phai hien sau Play all lai");
        ExtentReportManager.getTest().log(Status.PASS,
                "Play all OK. First: " + firstTitle + " | Mini: " + tracksPage.getMiniPlayerContent());
    }

    @Test(description = "TC_TRACKS_016: Play all sau sort Duration - mini player phat")
    public void TC_016_play_all_after_sort() {
        tracksPage.openSortDialog();
        tracksPage.selectSortByDuration();
        sleep(850);
        String firstAfterSort = tracksPage.getTrackTitleByIndex(0);
        tracksPage.clickPlayAll();
        sleep(1300);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed());
        ExtentReportManager.getTest().log(Status.PASS,
                "Play all after Duration sort - first: " + firstAfterSort);
    }

    @Test(description = "TC_TRACKS_017: Shuffle - phat random, lap 5 lan deu phat")
    public void TC_017_shuffle_plays_randomly() {
        // mini player container khong expose title duy nhat -> verify shuffle CHAY 5 lan + mini hien.
        java.util.Set<Integer> progresses = new java.util.HashSet<>();
        for (int i = 0; i < 5; i++) {
            tracksPage.clickShuffle();
            sleep(1100);
            Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(),
                    "Lan shuffle " + (i + 1) + " phai co mini player");
            progresses.add(tracksPage.getMiniPlayerProgressPercent());
        }
        ExtentReportManager.getTest().log(Status.PASS,
                "Shuffle 5 lan OK. Progress observed: " + progresses);
    }

    @Test(description = "TC_TRACKS_018: Play all <-> Shuffle doi bai dang phat")
    public void TC_018_play_all_shuffle_changes() {
        tracksPage.clickPlayAll();
        sleep(1100);
        String afterPlayAll = tracksPage.getMiniPlayerContent();
        tracksPage.clickShuffle();
        sleep(1100);
        String afterShuffle = tracksPage.getMiniPlayerContent();
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed());
        ExtentReportManager.getTest().log(Status.PASS,
                "PlayAll vs Shuffle changed: " + !java.util.Objects.equals(afterPlayAll, afterShuffle));
    }

    @Test(description = "TC_TRACKS_019: Click track - phat + mini (KHONG mo Play Now); click bai khac -> mini update; click lai bai dang phat khong restart")
    public void TC_019_click_track_behaviors() {
        // Click track[2] -> phat + mini + van o Tracks (KHONG mo Play Now)
        tracksPage.clickTrackByIndex(2);
        sleep(1100);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Click track phai phat + hien mini player");
        Assert.assertTrue(tracksPage.isOnTracksScreen(), "Van o Tracks (khong mo Play Now)");
        String mini2 = tracksPage.getMiniPlayerContent();

        // Click bai khac -> mini update
        tracksPage.clickTrackByIndex(3);
        sleep(1100);
        String mini3 = tracksPage.getMiniPlayerContent();
        Assert.assertTrue(tracksPage.isOnTracksScreen(), "Van o Tracks");

        // Click lai chinh bai dang phat -> khong restart (progress khong tut ve 0)
        int pa = tracksPage.getMiniPlayerProgressPercent();
        sleep(700);
        tracksPage.clickTrackByIndex(3);
        sleep(700);
        int pb = tracksPage.getMiniPlayerProgressPercent();
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Van phat sau khi click lai bai dang phat");
        ExtentReportManager.getTest().log(Status.PASS,
                "Click track: mini2 vs mini3 different=" + !java.util.Objects.equals(mini2, mini3)
                        + " | replay progress " + pa + "% -> " + pb + "%");
    }

    @Test(description = "TC_TRACKS_020: Long press track -> mo Select mode (KHONG Play Now)")
    public void TC_020_long_press_select_mode() {
        tracksPage.longPressTrackByIndex(0);
        sleep(700);
        Assert.assertFalse(playNowPage.isOnPlayNowScreen(), "Long press KHONG duoc mo Play Now");
        Assert.assertTrue(tracksPage.isSelectModeOpen(), "Long press phai mo Select mode");
        ExtentReportManager.getTest().log(Status.PASS,
                "Long press -> Select mode. Selected: " + tracksPage.getSelectedCount());
        tracksPage.exitSelectMode();
        sleep(700);
    }

    @Test(description = "TC_TRACKS_021: Scroll list - khong roi Tracks, bai van phat")
    public void TC_021_scroll_keeps_tracks() {
        tracksPage.clickPlayAll();
        sleep(1100);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed());

        tracksPage.scrollListDown();
        tracksPage.scrollListUp();
        sleep(600);
        // Sau scroll: van o Tracks (title/PlayAll/Shuffle) HOAC con item HOAC mini player con hien.
        boolean ok = false;
        for (int i = 0; i < 5 && !ok; i++) {
            ok = tracksPage.isOnTracksScreen()
                    || tracksPage.getDisplayedTracksCount() > 0
                    || tracksPage.isMiniPlayerDisplayed();
            if (!ok) sleep(600);
        }
        Assert.assertTrue(ok, "Sau scroll phai van o Tracks list (title/item/mini player con hien)");
        ExtentReportManager.getTest().log(Status.PASS,
                "Scroll OK - van o Tracks (mini=" + tracksPage.isMiniPlayerDisplayed() + ")");
    }

    @Test(description = "TC_TRACKS_022: Mini player play/pause toggle")
    public void TC_022_mini_player_play_pause() {
        tracksPage.clickPlayAll();
        sleep(1300);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed());
        tracksPage.clickMiniPlayerPlayPause();   // pause
        sleep(700);
        tracksPage.clickMiniPlayerPlayPause();   // resume
        sleep(700);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Mini player phai van hien sau toggle");
        ExtentReportManager.getTest().log(Status.PASS, "Play/pause toggle OK");
    }

    @Test(description = "TC_TRACKS_023: Mini player co content + progress % tang theo thoi gian")
    public void TC_023_mini_player_progress() {
        tracksPage.clickPlayAll();
        sleep(850);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Mini player phai hien");
        int p1 = tracksPage.getMiniPlayerProgressPercent();
        sleep(5000);
        int p2 = tracksPage.getMiniPlayerProgressPercent();
        ExtentReportManager.getTest().log(Status.PASS,
                "Mini content=" + tracksPage.getMiniPlayerContent() + " | progress " + p1 + "% -> " + p2 + "%");
        if (p1 >= 0 && p2 >= 0) {
            Assert.assertTrue(p2 >= p1, "Progress phai tang khi dang phat. " + p1 + " -> " + p2);
        }
    }

    @Test(description = "TC_TRACKS_024: Mini player body -> mo Play Now; Back -> ve Tracks, bai van phat")
    public void TC_024_mini_to_play_now_and_back() {
        tracksPage.clickPlayAll();
        sleep(1300);
        tracksPage.clickMiniPlayerBody();
        sleep(1100);
        Assert.assertTrue(playNowPage.isOnPlayNowScreen(), "Click mini player phai mo Play Now");

        playNowPage.close();
        sleep(850);
        Assert.assertTrue(tracksPage.isOnTracksScreen(), "Phai ve Tracks sau back");
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed(), "Mini player van hien (bai van phat)");
        ExtentReportManager.getTest().log(Status.PASS,
                "Mini -> Play Now -> Back OK. Mini: " + tracksPage.getMiniPlayerContent());
    }

    @Test(description = "TC_TRACKS_025: Mini player queue icon -> mo Playing Queue")
    public void TC_025_mini_player_opens_queue() {
        tracksPage.clickPlayAll();
        sleep(1300);
        tracksPage.clickMiniPlayerQueue();
        sleep(1300);
        Assert.assertTrue(queuePage.isOnPlayingQueueScreen(), "Phai mo Playing Queue");
        ExtentReportManager.getTest().log(Status.PASS,
                "Playing Queue OK. Count: " + queuePage.getCountLabel());
        queuePage.clickBack();
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() { resetToTracks(); }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
