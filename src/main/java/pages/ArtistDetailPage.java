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
 * Artist Detail screen (click vao artist card).
 *
 * Cau truc:
 *  - Header: Back + 3-dot menu (artist edit)
 *  - Artist hero: "<name>\nN songs"
 *  - Play all + Shuffle
 *  - Section "Albums": cac folder cards (Music Download, BrowserDownloader, etc.)
 *  - Section "Tracks": list tracks
 *  - Sort button trong Tracks section
 *  - Mini player
 *
 * Note: Cung dung cho ArtistFolderPage vi layout giong nhau.
 */
public class ArtistDetailPage {

    private static final Logger logger = LogManager.getLogger(ArtistDetailPage.class);
    private final AppiumDriver driver;

    // ===== HEADER =====
    private final By BACK_BUTTON = AppiumBy.accessibilityId("Back");
    // 3-dot menu o goc tren phai [1569,64][1720,182]
    // Khong co content-desc, dung bounds-based

    // ===== ARTIST HERO =====
    // Hero co dang "<name>\nN songs". descriptionContains("songs") on dinh hon descriptionMatches
    // (regex co \n + non-breaking space hay khong match tren UA2).
    private final By ARTIST_HERO = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"songs\")");

    // ===== PLAY ALL / SHUFFLE =====
    private final By PLAY_ALL = AppiumBy.accessibilityId("Play all");
    private final By SHUFFLE = AppiumBy.accessibilityId("Shuffle");

    // ===== ALBUMS SECTION =====
    private final By ALBUMS_LABEL = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Albums\").className(\"android.view.View\")");
    // Folder cards: ImageView clickable, content-desc = ten folder
    private final By FOLDER_MUSIC_DOWNLOAD = AppiumBy.accessibilityId("Music Download");
    private final By FOLDER_BROWSER = AppiumBy.accessibilityId("BrowserDownloader");
    private final By FOLDER_RECOVERED = AppiumBy.accessibilityId("RecoveredAudios");
    private final By FOLDER_VOICE_CHANGER = AppiumBy.accessibilityId("VoiceChanger");
    private final By FOLDER_NOTIFICATIONS = AppiumBy.accessibilityId("Notifications");

    // ===== TRACKS SECTION =====
    private final By TRACKS_LABEL = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Tracks\").className(\"android.view.View\")");
    private final By TRACK_SORT_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.Button\").clickable(true).instance(0)");
    // Track items - View clickable co " • " trong content-desc (artist • duration)
    private final By TRACK_ITEMS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.view.View\").clickable(true).descriptionContains(\" • \")");

    // ===== EDIT SHEET (4 actions - giong ArtistsPage) =====
    private final By SHEET_PLAY = AppiumBy.accessibilityId("Play");
    private final By SHEET_ADD_QUEUE = AppiumBy.accessibilityId("Add to playing queue");
    private final By SHEET_ADD_PLAYLIST = AppiumBy.accessibilityId("Add to playlist");
    private final By SHEET_SHARE = AppiumBy.accessibilityId("Share track");
    // Track-level edit sheet (7 actions) co Rename + Delete - PHAN BIET voi artist sheet (4 actions)
    private final By TRACK_SHEET_RENAME = AppiumBy.accessibilityId("Rename");
    private final By TRACK_SHEET_DELETE = AppiumBy.accessibilityId("Delete from device");

    public ArtistDetailPage(AppiumDriver driver) {
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

    // ============== SCREEN STATE ==============

    public boolean isOnArtistDetailScreen() {
        return isDisplayed(BACK_BUTTON) && isDisplayed(PLAY_ALL)
                && isDisplayed(SHUFFLE) && isDisplayed(TRACKS_LABEL);
    }

    public boolean isHeaderDisplayed() { return isDisplayed(BACK_BUTTON); }

    public String getArtistHeroInfo() {
        try {
            List<WebElement> els = driver.findElements(ARTIST_HERO);
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    public int getArtistSongCount() {
        String info = getArtistHeroInfo();
        if (info == null) return -1;
        Matcher m = Pattern.compile("(\\d+)\\D{0,5}songs?").matcher(info);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    public boolean isPlayAllAndShuffleDisplayed() {
        return isDisplayed(PLAY_ALL) && isDisplayed(SHUFFLE);
    }

    public boolean isAlbumsSectionDisplayed() { return isDisplayed(ALBUMS_LABEL); }

    public boolean isTracksSectionDisplayed() { return isDisplayed(TRACKS_LABEL); }

    // ============== FOLDERS ==============

    public boolean isFolderDisplayed(String name) {
        switch (name) {
            case "Music Download": return isDisplayed(FOLDER_MUSIC_DOWNLOAD);
            case "BrowserDownloader": return isDisplayed(FOLDER_BROWSER);
            case "RecoveredAudios": return isDisplayed(FOLDER_RECOVERED);
            case "VoiceChanger": return isDisplayed(FOLDER_VOICE_CHANGER);
            case "Notifications": return isDisplayed(FOLDER_NOTIFICATIONS);
        }
        return false;
    }

    /** Cuon ngang section Albums sang trai de lo folder bi an (vd Notifications o cuoi). */
    public void scrollAlbumsLeft() {
        try {
            Map<String, Object> a = new HashMap<>();
            a.put("left", 100); a.put("top", 880); a.put("width", 1500); a.put("height", 520);
            a.put("direction", "left"); a.put("percent", 0.85);
            driver.executeScript("mobile: swipeGesture", a);
        } catch (Exception ignored) {}
        sleep(1200);
    }

    /** Cuon Albums toi khi folder hien (toi 4 lan). */
    public boolean ensureFolderVisible(String name) {
        for (int i = 0; i < 4; i++) {
            if (isFolderDisplayed(name)) return true;
            scrollAlbumsLeft();
        }
        return isFolderDisplayed(name);
    }

    public void clickFolder(String name) {
        logger.info("Click folder: " + name);
        switch (name) {
            case "Music Download": click(FOLDER_MUSIC_DOWNLOAD); break;
            case "BrowserDownloader": click(FOLDER_BROWSER); break;
            case "RecoveredAudios": click(FOLDER_RECOVERED); break;
            case "VoiceChanger": click(FOLDER_VOICE_CHANGER); break;
            case "Notifications": click(FOLDER_NOTIFICATIONS); break;
            default: throw new RuntimeException("Unknown folder: " + name);
        }
        sleep(2500);
    }

    // ============== ACTIONS ==============

    public void clickPlayAll() {
        logger.info("Play all (artist)");
        click(PLAY_ALL);
        sleep(3000);
    }

    public void clickShuffle() {
        logger.info("Shuffle (artist)");
        click(SHUFFLE);
        sleep(3000);
    }

    public void clickBack() {
        click(BACK_BUTTON);
        sleep(1500);
    }

    /**
     * Click 3-dot menu o goc tren phai [1569,64][1720,182] -> mo artist edit sheet.
     */
    public void clickArtistMenu() {
        logger.info("Click 3-dot menu artist");
        tap(1644, 123);
        sleep(2500);
    }

    // ============== TRACKS LIST ==============

    public List<String> getTrackTitles() {
        List<String> titles = new ArrayList<>();
        try {
            for (WebElement el : driver.findElements(TRACK_ITEMS)) {
                String desc = el.getAttribute("content-desc");
                if (desc != null && desc.contains("\n")) {
                    titles.add(desc.split("\n")[0]);
                }
            }
        } catch (Exception ignored) {}
        return titles;
    }

    public int getDisplayedTracksCount() {
        try { return driver.findElements(TRACK_ITEMS).size(); }
        catch (Exception e) { return 0; }
    }

    /** content-desc cua track item (dang "TITLE\n<artist> • M:SS"). */
    public String getTrackDesc(int index) {
        List<WebElement> tracks = driver.findElements(TRACK_ITEMS);
        if (index >= tracks.size()) return null;
        try { return tracks.get(index).getAttribute("content-desc"); }
        catch (Exception e) { return null; }
    }

    public boolean isTrackSortButtonDisplayed() { return isDisplayed(TRACK_SORT_BUTTON); }

    /** Mini player (content-desc dang "44%, <artist>"). descriptionContains("%,") on dinh hon. */
    public boolean isMiniPlayerDisplayed() {
        return isDisplayed(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"%,\")"));
    }

    /** Track-level edit sheet (7 actions) co Rename + Delete - khac artist sheet (chi 4 actions). */
    public boolean isTrackEditSheetOpen() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(TRACK_SHEET_RENAME)
                && isDisplayed(TRACK_SHEET_DELETE);
    }

    public void closeTrackEditSheetByBack() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(1500);
    }

    public void clickTrackByIndex(int index) {
        List<WebElement> tracks = driver.findElements(TRACK_ITEMS);
        if (index >= tracks.size()) throw new RuntimeException("Index out of range");
        WebElement t = tracks.get(index);
        Rectangle r = t.getRect();
        tap(r.getX() + 600, r.getY() + r.getHeight() / 2);
        sleep(2500);
    }

    public void clickTrackEditByIndex(int index) {
        // RETRY tap 3-dot toi khi sheet MO (toi da 3 lan): chay tich hop (UI cham) 1 tap don le
        // co the khong kip mo sheet / tree chua ready (flaky).
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                List<WebElement> tracks = driver.findElements(TRACK_ITEMS);
                if (index >= tracks.size()) {
                    try { driver.getPageSource(); } catch (Exception ignored) {}
                    sleep(500);
                    continue;
                }
                WebElement t = tracks.get(index);
                Rectangle r = t.getRect();
                tap(r.getX() + r.getWidth() - 54, r.getY() + r.getHeight() / 2);
                sleep(1800);
                if (isTrackEditSheetOpen()) return;   // sheet mo -> xong
            } catch (Exception e) {
                logger.warn("clickTrackEditByIndex loi (lan " + attempt + "): " + e.getMessage());
            }
            sleep(500);
        }
    }

    // ============== EDIT SHEET (4 actions) ==============

    public boolean isEditSheetOpen() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_SHARE);
    }

    public boolean isAllArtistActionsDisplayed() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_ADD_QUEUE)
                && isDisplayed(SHEET_ADD_PLAYLIST) && isDisplayed(SHEET_SHARE);
    }

    /** Info artist tren header cua edit sheet (khi sheet mo) - dang "name\nN songs". */
    public String getSheetArtistInfo() {
        try {
            List<WebElement> els = driver.findElements(ARTIST_HERO);
            // Khi sheet mo co the co ca hero (sau scrim) + sheet header -> lay cai cuoi (sheet).
            if (!els.isEmpty()) return els.get(els.size() - 1).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    public void clickSheetPlay() { click(SHEET_PLAY); sleep(3000); }
    public void clickSheetAddToQueue() { click(SHEET_ADD_QUEUE); sleep(3000); }
    public void clickSheetAddToPlaylist() { click(SHEET_ADD_PLAYLIST); sleep(2500); }
    public void clickSheetShare() { click(SHEET_SHARE); sleep(3500); }

    public void closeEditSheetByBack() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(1500);
    }

    public void closeEditSheetByScrim() {
        tap(860, 200);
        sleep(1500);
    }

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

    /**
     * Dismiss share intent resolver (press BACK).
     */
    public void dismissShareIntent() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(2000);
    }
}