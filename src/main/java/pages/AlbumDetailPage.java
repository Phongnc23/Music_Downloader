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
 * Album Detail screen (click vao album card).
 *
 * Cau truc (giong folder screen cua Artist):
 *  - Header: Back + 3-dot menu (album-level edit)
 *  - Hero: "<album>\nN songs"
 *  - Play all + Shuffle
 *  - Section "Tracks" + sort button
 *  - Track list (chi tracks cua album)
 *  - Mini player
 *
 * In-album track sort: 7 options (Title/Artist/Album/File name/Duration/Date added/Date modified).
 */
public class AlbumDetailPage {

    private static final Logger logger = LogManager.getLogger(AlbumDetailPage.class);
    private final AppiumDriver driver;

    // ===== HEADER =====
    private final By BACK_BUTTON = AppiumBy.accessibilityId("Back");
    // 3-dot menu top-right [bounds ~ 1531-1720, 0-184], dung bounds-based tap

    // ===== HERO =====
    // descriptionContains("songs") - hero "name\nN songs" dung non-breaking space nen
    // descriptionMatches(\\s+) KHONG match (giong ArtistDetailPage).
    private final By ALBUM_HERO = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"songs\")");

    // ===== PLAY ALL / SHUFFLE =====
    private final By PLAY_ALL = AppiumBy.accessibilityId("Play all");
    private final By SHUFFLE = AppiumBy.accessibilityId("Shuffle");

    // ===== TRACKS SECTION =====
    private final By TRACKS_LABEL = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Tracks\").className(\"android.view.View\")");
    private final By TRACK_SORT_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.Button\").clickable(true).instance(0)");
    private final By TRACK_ITEMS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.view.View\").clickable(true).descriptionContains(\" • \")");

    // ===== IN-ALBUM SORT DIALOG (7 options) =====
    private final By SORT_DIALOG_TITLE = AppiumBy.accessibilityId("Sort by");
    private final By SORT_TITLE = AppiumBy.accessibilityId("Title");
    private final By SORT_ARTIST = AppiumBy.accessibilityId("Artist");
    private final By SORT_ALBUM = AppiumBy.accessibilityId("Album");
    private final By SORT_FILE_NAME = AppiumBy.accessibilityId("File name");
    private final By SORT_DURATION = AppiumBy.accessibilityId("Duration");
    private final By SORT_DATE_ADDED = AppiumBy.accessibilityId("Date added");
    private final By SORT_DATE_MODIFIED = AppiumBy.accessibilityId("Date modified");

    // ===== EDIT SHEET (4 actions album-level) =====
    private final By SHEET_PLAY = AppiumBy.accessibilityId("Play");
    private final By SHEET_ADD_QUEUE = AppiumBy.accessibilityId("Add to playing queue");
    private final By SHEET_ADD_PLAYLIST = AppiumBy.accessibilityId("Add to playlist");
    private final By SHEET_SHARE = AppiumBy.accessibilityId("Share track");

    public AlbumDetailPage(AppiumDriver driver) {
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

    public boolean isOnAlbumDetailScreen() {
        return isDisplayed(BACK_BUTTON) && isDisplayed(PLAY_ALL)
                && isDisplayed(SHUFFLE) && isDisplayed(TRACKS_LABEL);
    }

    public boolean isHeaderDisplayed() { return isDisplayed(BACK_BUTTON); }

    public String getAlbumHeroInfo() {
        try {
            List<WebElement> els = driver.findElements(ALBUM_HERO);
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    public int getAlbumSongCount() {
        String info = getAlbumHeroInfo();
        if (info == null) return -1;
        Matcher m = Pattern.compile("(\\d+)\\D{0,5}songs?").matcher(info);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    public boolean isPlayAllAndShuffleDisplayed() {
        return isDisplayed(PLAY_ALL) && isDisplayed(SHUFFLE);
    }

    public boolean isTracksSectionDisplayed() { return isDisplayed(TRACKS_LABEL); }

    /** Mini player (content-desc "N%, <artist>"). descriptionContains("%,") on dinh. */
    public boolean isMiniPlayerDisplayed() {
        return isDisplayed(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"%,\")"));
    }

    // ============== ACTIONS ==============

    public void clickPlayAll() { logger.info("Album Play all"); click(PLAY_ALL); sleep(3000); }
    public void clickShuffle() { logger.info("Album Shuffle"); click(SHUFFLE); sleep(3000); }
    public void clickBack() { click(BACK_BUTTON); sleep(1500); }

    /**
     * Click 3-dot menu album top-right [1531,64][1682,184] -> album edit sheet.
     */
    public void clickAlbumMenu() {
        logger.info("Click 3-dot album menu");
        tap(1606, 124);
        sleep(2500);
    }

    // ============== TRACKS ==============

    public List<String> getTrackTitles() {
        List<String> titles = new ArrayList<>();
        for (WebElement el : getValidTracks()) {
            try { titles.add(el.getAttribute("content-desc").split("\n")[0]); }
            catch (Exception ignored) {}
        }
        return titles;
    }

    public List<Integer> getTrackDurationsInSeconds() {
        List<Integer> durations = new ArrayList<>();
        for (WebElement el : getValidTracks()) {
            try {
                String desc = el.getAttribute("content-desc");
                if (!desc.contains(" • ")) continue;
                String durStr = desc.substring(desc.lastIndexOf(" • ") + 3).trim();
                String[] parts = durStr.split(":");
                if (parts.length == 2) durations.add(Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]));
            } catch (Exception ignored) {}
        }
        return durations;
    }

    public int getDisplayedTracksCount() { return getValidTracks().size(); }

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

    // ============== IN-ALBUM SORT (7 options) ==============

    public void openTrackSortDialog() {
        logger.info("Mo in-album track sort dialog");
        // KHONG dung Button.instance(0): track row 3-dot cung la Button. Nut sort o section
        // "Tracks" (y ~788) NAM TREN cac track 3-dot (y > 1160) -> chon Button tren cung.
        tapTopmostButton();
        sleep(1500);
    }

    /**
     * Tap nut sort section Tracks = Button trong vung header section (y trong [400,1050]):
     * - Bo qua 3-dot menu album o top-right (y < 400, cung la Button).
     * - Bo qua track row 3-dot (y > 1050).
     * Nut sort co dinh o ~y788 (duoi Play all/Shuffle, tren track rows).
     */
    private void tapTopmostButton() {
        WebElement target = null;
        int minY = Integer.MAX_VALUE;
        try {
            for (WebElement b : driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.Button\").clickable(true)"))) {
                try {
                    Rectangle r = b.getRect();
                    if (r.getY() > 400 && r.getY() < 1050 && r.getY() < minY) {
                        minY = r.getY(); target = b;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        if (target != null) {
            Rectangle r = target.getRect();
            tap(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
        }
    }

    public boolean isSortDialogOpen() {
        return isDisplayed(SORT_DIALOG_TITLE) && isDisplayed(SORT_TITLE);
    }

    public boolean isAllSortOptionsDisplayed() {
        return isDisplayed(SORT_TITLE) && isDisplayed(SORT_ARTIST) && isDisplayed(SORT_ALBUM)
                && isDisplayed(SORT_FILE_NAME) && isDisplayed(SORT_DURATION)
                && isDisplayed(SORT_DATE_ADDED) && isDisplayed(SORT_DATE_MODIFIED);
    }

    public void selectSortByTitle() { click(SORT_TITLE); sleep(2000); }
    public void selectSortByArtist() { click(SORT_ARTIST); sleep(2000); }
    public void selectSortByDuration() { click(SORT_DURATION); sleep(2000); }
    public void selectSortByDateAdded() { click(SORT_DATE_ADDED); sleep(2000); }
    public void selectSortByDateModified() { click(SORT_DATE_MODIFIED); sleep(2000); }

    public void closeSortDialogByScrim() { tap(860, 300); sleep(1500); }

    // Sort verification
    public boolean isStringListAscending(List<String> l) {
        for (int i = 1; i < l.size(); i++)
            if (l.get(i-1).toLowerCase().compareTo(l.get(i).toLowerCase()) > 0) return false;
        return true;
    }
    public boolean isStringListDescending(List<String> l) {
        for (int i = 1; i < l.size(); i++)
            if (l.get(i-1).toLowerCase().compareTo(l.get(i).toLowerCase()) < 0) return false;
        return true;
    }
    public boolean isIntListAscending(List<Integer> l) {
        for (int i = 1; i < l.size(); i++) if (l.get(i-1) > l.get(i)) return false;
        return true;
    }
    public boolean isIntListDescending(List<Integer> l) {
        for (int i = 1; i < l.size(); i++) if (l.get(i-1) < l.get(i)) return false;
        return true;
    }
    public boolean isOrderDifferent(List<String> a, List<String> b) {
        if (a.size() != b.size()) return true;
        int diff = 0;
        for (int i = 0; i < a.size(); i++) if (!a.get(i).equals(b.get(i))) diff++;
        return diff > 1;
    }

    // ============== EDIT SHEET (4 actions) ==============

    public boolean isEditSheetOpen() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_SHARE);
    }

    public boolean isAllAlbumActionsDisplayed() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_ADD_QUEUE)
                && isDisplayed(SHEET_ADD_PLAYLIST) && isDisplayed(SHEET_SHARE);
    }

    public void clickSheetPlay() { click(SHEET_PLAY); sleep(3000); }
    public void clickSheetAddToQueue() { click(SHEET_ADD_QUEUE); sleep(3000); }
    public void clickSheetAddToPlaylist() { click(SHEET_ADD_PLAYLIST); sleep(2500); }
    public void clickSheetShare() { click(SHEET_SHARE); sleep(3500); }

    public void closeEditSheetByBack() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(1500);
    }

    public void closeEditSheetByScrim() { tap(860, 200); sleep(1500); }

    // ============== SHARE STATUS ==============

    public boolean isShareIntentResolverOpen() {
        try {
            String pkg = ((AndroidDriver) driver).getCurrentPackage();
            return pkg != null && pkg.contains("intentresolver");
        } catch (Exception e) { return false; }
    }

    public boolean isStillInApp() {
        try {
            String pkg = ((AndroidDriver) driver).getCurrentPackage();
            return pkg != null && pkg.contains("musicdownloadapp");
        } catch (Exception e) { return false; }
    }

    public void dismissShareIntent() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(2000);
    }
}