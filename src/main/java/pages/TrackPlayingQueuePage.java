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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackPlayingQueuePage {

    private static final Logger logger = LogManager.getLogger(TrackPlayingQueuePage.class);
    private final AppiumDriver driver;

    private final By BACK_BUTTON = AppiumBy.accessibilityId("Back");
    private final By TITLE = AppiumBy.accessibilityId("Playing Queue");
    // descriptionContains("tracks") robust hon regex; getCountLabel se chon dung label co "(x/N)".
    private final By COUNT_LABEL = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"tracks\")");
    // Track items in queue
    private final By QUEUE_ITEMS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.view.View\").clickable(true).descriptionContains(\" • \")");

    public TrackPlayingQueuePage(AppiumDriver driver) {
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

    public boolean isOnPlayingQueueScreen() {
        return isDisplayed(TITLE) && isDisplayed(BACK_BUTTON);
    }

    public boolean isTitleDisplayed() { return isDisplayed(TITLE); }
    public boolean isBackButtonDisplayed() { return isDisplayed(BACK_BUTTON); }

    /** Label "N tracks(x/N)". Ep refresh tree + chon element co dang ngoac "(x/N)". */
    public String getCountLabel() {
        try { driver.getPageSource(); } catch (Exception ignored) {}
        try {
            List<WebElement> els = driver.findElements(COUNT_LABEL);
            for (WebElement el : els) {
                String d = el.getAttribute("content-desc");
                if (d != null && d.contains("(") && d.contains("/")) return d;
            }
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    /** Tong so bai trong queue = so dau "N tracks" (tang 1 moi lan add to queue). */
    public int getTotalQueueCount() {
        String label = getCountLabel();
        if (label == null) return -1;
        Matcher m = Pattern.compile("(\\d+)\\s*tracks?").matcher(label);
        if (m.find()) return Integer.parseInt(m.group(1));
        // fallback: so sau "/" trong (x/N)
        Matcher m2 = Pattern.compile("\\(\\d+/(\\d+)\\)").matcher(label);
        if (m2.find()) return Integer.parseInt(m2.group(1));
        return -1;
    }

    public int getCurrentPosition() {
        String label = getCountLabel();
        if (label == null) return -1;
        Matcher m = Pattern.compile("\\((\\d+)/\\d+\\)").matcher(label);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    public List<String> getQueueTrackTitles() {
        List<String> titles = new ArrayList<>();
        try {
            for (WebElement el : driver.findElements(QUEUE_ITEMS)) {
                String desc = el.getAttribute("content-desc");
                if (desc != null && desc.contains("\n")) {
                    titles.add(desc.split("\n")[0]);
                }
            }
        } catch (Exception ignored) {}
        return titles;
    }

    public int getDisplayedQueueItemsCount() {
        return getQueueTrackTitles().size();
    }

    /**
     * Click queue item theo index.
     */
    public void clickQueueItemByIndex(int index) {
        List<WebElement> items = driver.findElements(QUEUE_ITEMS);
        if (index >= items.size()) throw new RuntimeException("Index out of range");
        WebElement track = items.get(index);
        Rectangle r = track.getRect();
        tap(r.getX() + 600, r.getY() + r.getHeight() / 2);
        sleep(850);
    }

    /**
     * Click trash icon (top-right, bounds [1531,337][1682,488]) - clear queue.
     * CHU Y: action nay co confirm dialog -> destructive.
     */
    public void clickTrashIcon() {
        logger.warn("Click trash - clear queue");
        tap(1606, 412);
        sleep(850);
    }

    /**
     * Top-right header buttons:
     *  - Shuffle: [1419,64][1569,182]
     *  - Repeat: [1569,64][1720,182]
     */
    public void clickHeaderShuffleToggle() {
        logger.info("Header shuffle toggle");
        tap(1494, 123);
        sleep(700);
    }

    public void clickHeaderRepeatToggle() {
        logger.info("Header repeat toggle");
        tap(1644, 123);
        sleep(700);
    }

    public void clickBack() {
        try { driver.findElement(BACK_BUTTON).click(); }
        catch (Exception e) { ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK)); }
        sleep(700);
    }

    // ============================================
    // BO SUNG: EDIT SHEET TUNG ITEM + REMOVE FROM QUEUE
    // ============================================

    private final By REMOVE_FROM_QUEUE = AppiumBy.accessibilityId("Remove from queue");
    private final By SHEET_PLAY = AppiumBy.accessibilityId("Play");

    /** Edit sheet cua 1 queue item co mo khong (co "Remove from queue"). */
    public boolean isQueueItemSheetOpen() {
        return isDisplayed(REMOVE_FROM_QUEUE) && isDisplayed(SHEET_PLAY);
    }

    public boolean isRemoveFromQueueDisplayed() {
        return isDisplayed(REMOVE_FROM_QUEUE);
    }

    /** Tap icon edit (3 cham phai) cua queue item theo index -> mo edit sheet. */
    public void clickQueueItemEditByIndex(int index) {
        List<WebElement> items = driver.findElements(QUEUE_ITEMS);
        if (index >= items.size()) throw new RuntimeException("Index out of range");
        Rectangle r = items.get(index).getRect();
        // Icon edit nam goc phai cua item (~x=1666 tren man 1720).
        tap(1666, r.getY() + r.getHeight() / 2);
        sleep(700);
    }

    public void clickRemoveFromQueue() {
        logger.info("Click Remove from queue");
        try { driver.findElement(REMOVE_FROM_QUEUE).click(); } catch (Exception ignored) {}
        sleep(850);
    }

    // ============================================
    // BO SUNG: CLEAR QUEUE (TRASH + CONFIRM)
    // ============================================

    /** Dialog xac nhan clear queue (sau khi bam trash) — co nut confirm. */
    public boolean isClearQueueConfirmOpen() {
        // App thuong hien confirm "CLEAR"/"OK"/"YES"/"DELETE" hoac mot dialog.
        for (String t : new String[]{"CLEAR", "Clear", "OK", "YES", "Yes", "DELETE", "Delete", "CONFIRM"}) {
            if (isDisplayed(AppiumBy.accessibilityId(t))) return true;
        }
        return false;
    }

    /** Tim + bam nut confirm cua clear-queue dialog (neu co). Tra true neu da bam. */
    public boolean confirmClearQueue() {
        for (String t : new String[]{"CLEAR", "Clear", "OK", "YES", "Yes", "DELETE", "Delete", "CONFIRM"}) {
            List<WebElement> e = driver.findElements(AppiumBy.accessibilityId(t));
            if (!e.isEmpty()) { e.get(0).click(); sleep(850); return true; }
        }
        return false;
    }
}