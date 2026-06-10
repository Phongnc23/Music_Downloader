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

/**
 * Playlist Detail screen (click vao playlist).
 *
 * Cau truc:
 *  - Header: Back + "Show menu" (3-dot, content-desc="Show menu")
 *  - Hero: "<name>\nN tracks"
 *  - Play all + Shuffle
 *  - "N tracks" label
 *  - Track list
 *  - Mini player
 */
public class PlaylistDetailPage {

    private static final Logger logger = LogManager.getLogger(PlaylistDetailPage.class);
    private final AppiumDriver driver;

    private final By BACK_BUTTON = AppiumBy.accessibilityId("Back");
    private final By SHOW_MENU = AppiumBy.accessibilityId("Show menu");
    // Hero "<name>\n<N> tracks" - giu rang buoc co \n (phan biet voi label "N tracks" doc lap),
    // nhung tolerant non-breaking space giua N va "tracks" (dung "." thay vi space).
    private final By HERO = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionMatches(\".+\\n.*tracks.*\")");
    private final By PLAY_ALL = AppiumBy.accessibilityId("Play all");
    private final By SHUFFLE = AppiumBy.accessibilityId("Shuffle");
    private final By TRACK_COUNT_LABEL = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionMatches(\"\\\\d+ tracks?\").className(\"android.view.View\")");
    private final By TRACK_ITEMS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.view.View\").clickable(true).descriptionContains(\" • \")");
    // Mini player: content-desc dang "0%, <unknown>" -> dau hieu dang phat
    private final By MINI_PLAYER = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"%,\")");

    public PlaylistDetailPage(AppiumDriver driver) {
        this.driver = driver;
    }

    private boolean isDisplayed(By l) {
        try { return !driver.findElements(l).isEmpty(); } catch (Exception e) { return false; }
    }

    private void click(By l) {
        try { driver.findElement(l).click(); sleep(800); }
        catch (Exception e) { logger.warn(e.getMessage()); }
    }

    private void tap(int x, int y) {
        Map<String, Object> args = new HashMap<>();
        args.put("x", x); args.put("y", y);
        driver.executeScript("mobile: clickGesture", args);
        sleep(500);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private List<WebElement> getValidTracks() {
        List<WebElement> valid = new ArrayList<>();
        for (WebElement el : driver.findElements(TRACK_ITEMS)) {
            try {
                String d = el.getAttribute("content-desc");
                if (d != null && d.contains("\n")) valid.add(el);
            } catch (Exception ignored) {}
        }
        return valid;
    }

    // ============== SCREEN STATE ==============

    public boolean isOnPlaylistDetailScreen() {
        return isDisplayed(BACK_BUTTON) && isDisplayed(PLAY_ALL) && isDisplayed(SHUFFLE);
    }

    public boolean isHeaderDisplayed() {
        return isDisplayed(BACK_BUTTON) && isDisplayed(SHOW_MENU);
    }

    public boolean isShowMenuDisplayed() { return isDisplayed(SHOW_MENU); }

    public String getHeroInfo() {
        try {
            List<WebElement> els = driver.findElements(HERO);
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    public int getTrackCount() {
        String info = getHeroInfo();
        if (info == null) return -1;
        Matcher m = Pattern.compile("(\\d+)\\D{0,5}tracks?").matcher(info);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    public boolean isPlayAllAndShuffleDisplayed() {
        return isDisplayed(PLAY_ALL) && isDisplayed(SHUFFLE);
    }

    public boolean isMiniPlayerDisplayed() { return isDisplayed(MINI_PLAYER); }

    public int getDisplayedTracksCount() { return getValidTracks().size(); }

    public List<String> getTrackTitles() {
        List<String> titles = new ArrayList<>();
        for (WebElement el : getValidTracks()) {
            try { titles.add(el.getAttribute("content-desc").split("\n")[0]); }
            catch (Exception ignored) {}
        }
        return titles;
    }

    // ============== ACTIONS ==============

    public void clickPlayAll() { logger.info("Playlist Play all"); click(PLAY_ALL); sleep(3000); }
    public void clickShuffle() { logger.info("Playlist Shuffle"); click(SHUFFLE); sleep(3000); }
    public void clickBack() { click(BACK_BUTTON); sleep(1500); }

    /**
     * Click "Show menu" (3-dot top-right [1612,70][1720,178]) -> edit sheet.
     */
    public void clickShowMenu() {
        logger.info("Click Show menu");
        click(SHOW_MENU);
        sleep(2500);
    }

    public void clickTrackByIndex(int index) {
        List<WebElement> tracks = getValidTracks();
        if (index >= tracks.size()) throw new RuntimeException("Index out of range");
        WebElement t = tracks.get(index);
        Rectangle r = t.getRect();
        tap(r.getX() + 600, r.getY() + r.getHeight() / 2);
        sleep(2500);
    }

    public void clickTrackEditByIndex(int index) {
        List<WebElement> tracks = getValidTracks();
        if (index >= tracks.size()) throw new RuntimeException("Index out of range");
        WebElement t = tracks.get(index);
        Rectangle r = t.getRect();
        tap(r.getX() + r.getWidth() - 54, r.getY() + r.getHeight() / 2);
        sleep(2000);
    }
}