package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackPlayNowPage {

    private static final Logger logger = LogManager.getLogger(TrackPlayNowPage.class);
    private final AppiumDriver driver;

    private final By PLAYING_NOW_CONTAINER = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionStartsWith(\"Playing now\")");
    private final By SEEK_BAR = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.SeekBar\")");

    public TrackPlayNowPage(AppiumDriver driver) {
        this.driver = driver;
    }

    private boolean isDisplayed(By l) {
        try { return !driver.findElements(l).isEmpty(); } catch (Exception e) { return false; }
    }

    private void tap(int x, int y) {
        Map<String, Object> args = new HashMap<>();
        args.put("x", x);
        args.put("y", y);
        driver.executeScript("mobile: clickGesture", args);
        sleep(500);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ============================================
    // SCREEN STATE
    // ============================================

    public boolean isOnPlayNowScreen() {
        return isDisplayed(PLAYING_NOW_CONTAINER) && isDisplayed(SEEK_BAR);
    }

    public boolean isSeekBarDisplayed() {
        return isDisplayed(SEEK_BAR);
    }

    /**
     * Lay content-desc: "Playing now\n<artist>\n4:09\n4:38"
     */
    public String getPlayingNowFullInfo() {
        try { return driver.findElement(PLAYING_NOW_CONTAINER).getAttribute("content-desc"); }
        catch (Exception e) { return null; }
    }

    /**
     * Lay SeekBar progress: "X%"
     */
    public String getSeekBarProgress() {
        try { return driver.findElement(SEEK_BAR).getAttribute("content-desc"); }
        catch (Exception e) { return null; }
    }

    public int getSeekBarProgressPercent() {
        String p = getSeekBarProgress();
        if (p == null) return -1;
        try { return Integer.parseInt(p.replace("%", "").trim()); }
        catch (Exception e) { return -1; }
    }

    /**
     * Lay time current va total tu content-desc "Playing now\n...\n4:09\n4:38".
     */
    public String getCurrentTime() {
        String full = getPlayingNowFullInfo();
        if (full == null) return null;
        String[] parts = full.split("\n");
        return parts.length >= 3 ? parts[2] : null;
    }

    public String getTotalDuration() {
        String full = getPlayingNowFullInfo();
        if (full == null) return null;
        String[] parts = full.split("\n");
        return parts.length >= 4 ? parts[3] : null;
    }

    // ============================================
    // CONTROL BUTTONS (bottom row)
    // ============================================

    /**
     * 5 control buttons o y~1953-2275:
     *  - Shuffle: [57,2029][227,2199]
     *  - Previous: [330,1981][596,2247]
     *  - Play/Pause: [699,1953][1021,2275] (lon nhat)
     *  - Next: [1124,1981][1390,2247]
     *  - Repeat: [1493,2029][1663,2199]
     */
    public void clickShuffleControl() { logger.info("Shuffle control"); tap(142, 2114); sleep(700); }
    public void clickPreviousControl() { logger.info("Previous"); tap(463, 2114); sleep(850); }
    public void clickPlayPauseControl() { logger.info("Play/Pause"); tap(860, 2114); sleep(700); }
    public void clickNextControl() { logger.info("Next"); tap(1257, 2114); sleep(850); }
    public void clickRepeatControl() { logger.info("Repeat"); tap(1578, 2114); sleep(700); }

    // ============================================
    // ACTION ICONS (middle row, y~1583)
    // ============================================

    /**
     * 5 action icons o y~1583-1733:
     *  - Heart: [57,1583][208,1733]
     *  - Add to playlist: [421,1583][572,1733]
     *  - Equalizer: [785,1583][935,1733]
     *  - Sleep timer: [1148,1583][1299,1733]
     *  - Queue: [1512,1583][1663,1733]
     */
    public void clickHeartIcon() { logger.info("Heart"); tap(132, 1658); sleep(700); }
    public void clickAddToPlaylistIcon() { logger.info("Add to playlist"); tap(496, 1658); sleep(850); }
    public void clickEqualizerIcon() { logger.info("Equalizer"); tap(860, 1658); sleep(1100); }
    public void clickSleepTimerIcon() { logger.info("Sleep timer"); tap(1223, 1658); sleep(850); }
    public void clickQueueIcon() { logger.info("Queue"); tap(1587, 1658); sleep(1100); }

    // ============================================
    // SEEK BAR INTERACTION
    // ============================================

    /**
     * Drag SeekBar tu vi tri hien tai sang percentage moi.
     */
    public void dragSeekBarTo(int targetPercent) {
        try {
            WebElement seek = driver.findElement(SEEK_BAR);
            Rectangle r = seek.getRect();
            int startX = r.getX() + r.getWidth() / 2;
            int targetX = r.getX() + (int) ((double) targetPercent / 100 * r.getWidth());
            int y = r.getY() + r.getHeight() / 2;

            Map<String, Object> args = new HashMap<>();
            args.put("startX", startX);
            args.put("startY", y);
            args.put("endX", targetX);
            args.put("endY", y);
            args.put("speed", 1500);
            driver.executeScript("mobile: dragGesture", args);
            sleep(700);
        } catch (Exception e) { logger.warn("Drag SeekBar loi: " + e.getMessage()); }
    }

    // ============================================
    // NAVIGATION
    // ============================================

    /**
     * Click down arrow (top-left) hoac press BACK de dong Play Now.
     */
    public void close() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(850);
    }

    public void clickThreeDotMenu() {
        logger.info("3-dot menu (top-right)");
        tap(1666, 139);
        sleep(850);
    }

    /** Tap (khong drag) len SeekBar tai percentage cho truoc -> nhay toi vi tri. */
    public void tapSeekBarAt(int percent) {
        try {
            WebElement seek = driver.findElement(SEEK_BAR);
            Rectangle r = seek.getRect();
            int x = r.getX() + (int) ((double) percent / 100 * r.getWidth());
            int y = r.getY() + r.getHeight() / 2;
            tap(x, y);
            sleep(700);
        } catch (Exception e) { logger.warn("tapSeekBar loi: " + e.getMessage()); }
    }
}