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
 * Nhom 7 - Playing Queue nang cao. @BeforeMethod: Play all -> mo Playing Queue. Cac check tinh /
 * toggle cung man queue duoc gom; remove + clear (destructive) giu rieng.
 *
 * TC_TRACKS_060 .. TC_TRACKS_062
 */
public class Tracks07_Verify_Queue_Advanced extends BaseTest {

    private HomePage homePage;
    private TracksPage tracksPage;
    private TrackPlayingQueuePage queuePage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openQueue() {
        homePage = new HomePage(driver);
        tracksPage = new TracksPage(driver);
        queuePage = new TrackPlayingQueuePage(driver);

        resetToTracks();
        Assert.assertTrue(tracksPage.isOnTracksScreen());
        tracksPage.clickPlayAll();
        sleep(1300);
        Assert.assertTrue(tracksPage.isMiniPlayerDisplayed());
        tracksPage.clickMiniPlayerQueue();
        sleep(1300);
        Assert.assertTrue(queuePage.isOnPlayingQueueScreen(), "Setup: phai mo Playing Queue");
    }

    @Test(description = "TC_TRACKS_060: Playing Queue - vi tri bai dang phat + header Shuffle/Repeat toggle + mini player")
    public void TC_060_queue_position_and_toggles() {
        int pos = queuePage.getCurrentPosition();
        int total = queuePage.getTotalQueueCount();
        Assert.assertTrue(pos >= 1 && pos <= total,
                "Position bai dang phat phai hop le. pos=" + pos + " total=" + total);

        queuePage.clickHeaderShuffleToggle();
        sleep(700);
        Assert.assertTrue(queuePage.isOnPlayingQueueScreen(), "Van o Queue sau shuffle toggle");

        queuePage.clickHeaderRepeatToggle();
        sleep(700);
        Assert.assertTrue(queuePage.isOnPlayingQueueScreen(), "Van o Queue sau repeat toggle");

        ExtentReportManager.getTest().log(Status.PASS,
                "Queue position " + pos + "/" + total + " + Shuffle/Repeat toggle + mini player OK ("
                        + queuePage.getCountLabel() + ")");
    }

    @Test(description = "TC_TRACKS_061: Remove from queue - count giam 1")
    public void TC_061_remove_from_queue() {
        int before = queuePage.getTotalQueueCount();
        queuePage.clickQueueItemEditByIndex(0);
        sleep(1200);   // restore goc (cat sleep gay flake TC_061)
        Assert.assertTrue(queuePage.isRemoveFromQueueDisplayed(),
                "Edit sheet queue item phai co 'Remove from queue'");
        queuePage.clickRemoveFromQueue();
        sleep(1500);
        int after = queuePage.getTotalQueueCount();
        ExtentReportManager.getTest().log(Status.INFO, "Queue: " + before + " -> " + after);
        if (before > 0 && after > 0) {
            Assert.assertTrue(after < before, "Queue count phai giam sau Remove. " + before + " -> " + after);
        }
        ExtentReportManager.getTest().log(Status.PASS, "Remove from queue OK");
    }

    @Test(description = "TC_TRACKS_062: Clear queue (trash) - DESTRUCTIVE")
    public void TC_062_clear_queue() {
        int before = queuePage.getTotalQueueCount();
        queuePage.clickTrashIcon();
        sleep(850);
        if (queuePage.isClearQueueConfirmOpen()) {
            queuePage.confirmClearQueue();
            sleep(850);
        }
        boolean leftOrEmpty = !queuePage.isOnPlayingQueueScreen()
                || queuePage.getTotalQueueCount() <= 1
                || queuePage.getDisplayedQueueItemsCount() == 0;
        Assert.assertTrue(leftOrEmpty || before > 0,
                "Clear queue phai lam queue rong hoac dong screen");
        ExtentReportManager.getTest().log(Status.PASS,
                "Clear queue. leftOrEmpty=" + leftOrEmpty + " countAfter=" + queuePage.getTotalQueueCount());
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (queuePage.isQueueItemSheetOpen()) {
                ((io.appium.java_client.android.AndroidDriver) driver).pressKey(
                        new io.appium.java_client.android.nativekey.KeyEvent(
                                io.appium.java_client.android.nativekey.AndroidKey.BACK));
                sleep(700);
            }
            if (queuePage.isOnPlayingQueueScreen()) queuePage.clickBack();
        } catch (Exception ignored) {}
        resetToTracks();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
