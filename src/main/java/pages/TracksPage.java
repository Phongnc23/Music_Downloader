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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TracksPage {

    private static final Logger logger = LogManager.getLogger(TracksPage.class);

    private final AppiumDriver driver;
    private final WebDriverWait wait;

    // ===== HEADER =====
    private final By MENU_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.ImageView\").clickable(true).instance(0)");
    private final By TITLE_TRACKS = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Tracks\").className(\"android.view.View\")");
    private final By SEARCH_ICON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.ImageView\").clickable(true).instance(1)");
    // \\D{0,5} thay vi \\s+ : count co the dung non-breaking space (\\u00A0) ma \\s khong khop.
    private final By TRACKS_COUNT = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionMatches(\"\\\\d+\\\\D{0,5}tracks?\")");
    private final By SORT_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.Button\").clickable(true).instance(0)");
    private final By PLAY_ALL = AppiumBy.accessibilityId("Play all");
    private final By SHUFFLE = AppiumBy.accessibilityId("Shuffle");

    // ===== TRACKS LIST =====
    private final By TRACK_ITEMS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.view.View\").longClickable(true)");

    // ===== MINI PLAYER =====
    private final By MINI_PLAYER = AppiumBy.androidUIAutomator(
            "new UiSelector().scrollable(true).clickable(true)");

    // ===== BOTTOM NAV =====
    private final By NAV_TRACKS = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Tracks\").className(\"android.widget.ImageView\")");

    // ===== SORT DIALOG =====
    private final By SORT_DIALOG_TITLE = AppiumBy.accessibilityId("Sort by");
    private final By SORT_TITLE = AppiumBy.accessibilityId("Title");
    private final By SORT_ARTIST = AppiumBy.accessibilityId("Artist");
    private final By SORT_ALBUM = AppiumBy.accessibilityId("Album");
    private final By SORT_FILE_NAME = AppiumBy.accessibilityId("File name");
    private final By SORT_DURATION = AppiumBy.accessibilityId("Duration");
    private final By SORT_DATE_ADDED = AppiumBy.accessibilityId("Date added");
    private final By SORT_DATE_MODIFIED = AppiumBy.accessibilityId("Date modified");

    // ===== EDIT BOTTOM SHEET =====
    private final By SHEET_PLAY = AppiumBy.accessibilityId("Play");
    private final By SHEET_ADD_QUEUE = AppiumBy.accessibilityId("Add to playing queue");
    private final By SHEET_ADD_PLAYLIST = AppiumBy.accessibilityId("Add to playlist");
    private final By SHEET_RENAME = AppiumBy.accessibilityId("Rename");
    private final By SHEET_FILE_INFO = AppiumBy.accessibilityId("File information");
    private final By SHEET_SHARE = AppiumBy.accessibilityId("Share track");
    private final By SHEET_DELETE = AppiumBy.accessibilityId("Delete from device");

    // ===== RENAME DIALOG =====
    private final By RENAME_TITLE = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Rename\").focusable(true)");
    private final By RENAME_INPUT = AppiumBy.className("android.widget.EditText");
    private final By RENAME_CANCEL = AppiumBy.accessibilityId("CANCEL");
    private final By RENAME_SAVE = AppiumBy.accessibilityId("SAVE");
    private final By CHAR_COUNT = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionMatches(\"\\\\d+/\\\\d+\")");

    // ===== DELETE DIALOG =====
    private final By DELETE_TITLE = AppiumBy.accessibilityId("Delete from device");
    private final By DELETE_MESSAGE = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionStartsWith(\"Do you want to delete\")");
    private final By DELETE_CANCEL = AppiumBy.accessibilityId("CANCEL");
    private final By DELETE_CONFIRM = AppiumBy.accessibilityId("DELETE");

    // ===== ADD TO PLAYLIST DIALOG =====
    private final By PLAYLIST_TITLE = AppiumBy.accessibilityId("Add to playlist");
    private final By CREATE_NEW_PLAYLIST = AppiumBy.accessibilityId("Create new playlist");
    private final By MY_FAVORITE = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionStartsWith(\"My Favorite\")");

    // ===== FILE INFO SCREEN =====
    private final By FILE_INFO_TITLE = AppiumBy.accessibilityId("Track information");
    private final By LABEL_FILE_PATH = AppiumBy.accessibilityId("File path");
    private final By LABEL_TITLE_FIELD = AppiumBy.accessibilityId("Title");
    private final By LABEL_ALBUM = AppiumBy.accessibilityId("Album");
    private final By LABEL_ARTIST = AppiumBy.accessibilityId("Artist");
    private final By LABEL_GENRES = AppiumBy.accessibilityId("Genres");
    private final By LABEL_DURATION = AppiumBy.accessibilityId("Duration");
    private final By LABEL_SIZE = AppiumBy.accessibilityId("Size");

    public TracksPage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // ============================================
    // PRIVATE HELPERS
    // ============================================

    private boolean isDisplayed(By locator) {
        try { return !driver.findElements(locator).isEmpty(); } catch (Exception e) { return false; }
    }

    private void click(By locator) {
        try { driver.findElement(locator).click(); sleep(500); }
        catch (Exception e) { logger.warn("Click error: " + e.getMessage()); }
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

    private List<WebElement> getValidTrackElements() {
        List<WebElement> valid = new ArrayList<>();
        for (WebElement el : driver.findElements(TRACK_ITEMS)) {
            try {
                String d = el.getAttribute("content-desc");
                if (d != null && d.contains("\n")) valid.add(el);
            } catch (Exception ignored) {}
        }
        return valid;
    }

    // ============================================
    // NAVIGATION
    // ============================================

    public void openTracksFromBottomNav() {
        logger.info("Click Tracks tab");
        click(NAV_TRACKS);
        sleep(700);
    }

    public boolean isOnTracksScreen() {
        return isDisplayed(TITLE_TRACKS) && isDisplayed(PLAY_ALL) && isDisplayed(SHUFFLE);
    }

    /**
     * Doc toast message (best-effort). Toast la transient (~2s) va UA2 bat khong on dinh ->
     * tra null neu khong bat duoc. Chi dung de LOG (khong hard-assert).
     */
    public String getToastMessage() {
        try {
            List<WebElement> t = driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.Toast\")"));
            if (!t.isEmpty()) {
                String txt = t.get(0).getText();
                if (txt != null && !txt.isEmpty()) return txt;
                return t.get(0).getAttribute("content-desc");
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ============================================
    // HEADER VERIFICATION
    // ============================================

    public boolean isHeaderDisplayed() {
        return isDisplayed(MENU_BUTTON) && isDisplayed(TITLE_TRACKS) && isDisplayed(SEARCH_ICON);
    }

    public boolean isTracksCountDisplayed() {
        return isDisplayed(TRACKS_COUNT);
    }

    /** Ep UA2 doc lai accessibility tree (sau gesture/delete tree hay cu). */
    public void forceRefreshTree() {
        try { driver.getPageSource(); } catch (Exception ignored) {}
    }

    /**
     * Tim title (chua namePart) trong list bang cach scroll xuong toi maxScrolls lan. Dung de verify
     * rename khi track doi ten bi sort ra khoi top-6 visible. Scroll VE DAU sau khi xong (tranh
     * lech vi tri cho test sau).
     */
    public boolean scrollFindTitle(String namePart, int maxScrolls) {
        boolean found = false;
        int scrolled = 0;
        for (int i = 0; i <= maxScrolls && !found; i++) {
            forceRefreshTree();
            for (String t : getTrackTitles()) {
                if (t != null && t.contains(namePart)) { found = true; break; }
            }
            if (!found && i < maxScrolls) { scrollListDown(); scrolled++; }
        }
        for (int i = 0; i < scrolled; i++) scrollListUp();   // ve dau
        return found;
    }

    public int getTracksCount() {
        try {
            String desc = driver.findElement(TRACKS_COUNT).getAttribute("content-desc");
            return Integer.parseInt(desc.replaceAll("\\D+", ""));
        } catch (Exception e) {
            // Tree co the cu sau gesture (vd vua thoat select mode) -> ep refresh + thu lai.
            try { driver.getPageSource(); } catch (Exception ignored) {}
            try {
                String desc = driver.findElement(TRACKS_COUNT).getAttribute("content-desc");
                return Integer.parseInt(desc.replaceAll("\\D+", ""));
            } catch (Exception e2) { return -1; }
        }
    }

    public boolean arePlayAllAndShuffleDisplayed() {
        return isDisplayed(PLAY_ALL) && isDisplayed(SHUFFLE);
    }

    public boolean isSortButtonDisplayed() {
        return isDisplayed(SORT_BUTTON);
    }

    public boolean isMiniPlayerDisplayed() {
        return isDisplayed(MINI_PLAYER);
    }

    public String getMiniPlayerContent() {
        try {
            List<WebElement> els = driver.findElements(MINI_PLAYER);
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    // ============================================
    // TRACK LIST OPERATIONS
    // ============================================

    public List<String> getTrackTitles() {
        List<String> titles = new ArrayList<>();
        for (WebElement el : getValidTrackElements()) {
            try {
                String desc = el.getAttribute("content-desc");
                titles.add(desc.split("\n")[0]);
            } catch (Exception ignored) {}
        }
        return titles;
    }

    /**
     * Parse duration tu content-desc "TITLE\n<artist> * M:SS".
     */
    public List<Integer> getTrackDurationsInSeconds() {
        List<Integer> durations = new ArrayList<>();
        for (WebElement el : getValidTrackElements()) {
            try {
                String desc = el.getAttribute("content-desc");
                if (!desc.contains(" • ")) continue;
                String durStr = desc.substring(desc.lastIndexOf(" • ") + 3).trim();
                String[] parts = durStr.split(":");
                if (parts.length == 2) {
                    durations.add(Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]));
                }
            } catch (Exception ignored) {}
        }
        return durations;
    }

    public int getDisplayedTracksCount() {
        int n = getValidTrackElements().size();
        // Tree co the cu sau gesture (scroll/dragGesture) -> 0 item gia. Ep refresh + thu lai.
        if (n == 0) {
            try { driver.getPageSource(); } catch (Exception ignored) {}
            sleep(400);
            n = getValidTrackElements().size();
        }
        return n;
    }

    public String getTrackTitleByIndex(int index) {
        List<String> titles = getTrackTitles();
        // Tree co the cu sau gesture (vd vua BACK dong sheet) -> list rong/thieu -> refresh + thu lai.
        if (titles.size() <= index) {
            try { driver.getPageSource(); } catch (Exception ignored) {}
            sleep(500);
            titles = getTrackTitles();
        }
        return index < titles.size() ? titles.get(index) : null;
    }

    public void clickTrackByIndex(int index) {
        List<WebElement> valid = getValidTrackElements();
        if (index >= valid.size()) throw new RuntimeException("Index out of range: " + index);
        WebElement track = valid.get(index);
        Rectangle r = track.getRect();
        int tapX = r.getX() + 800;  // Tranh edit button o x > 1612
        int tapY = r.getY() + r.getHeight() / 2;
        tap(tapX, tapY);
        sleep(850);
    }

    public void clickEditButtonByIndex(int index) {
        // RETRY tap 3-dot toi khi sheet MO (toi da 4 lan). Khi chay tich hop (sau ~2h, UI cham)
        // tree co the chua ready / tap truot -> refresh + tap lai thay vi throw ngay.
        RuntimeException last = null;
        for (int attempt = 1; attempt <= 4; attempt++) {
            try {
                List<WebElement> valid = getValidTrackElements();
                if (index >= valid.size()) {
                    last = new RuntimeException("Index out of range (tree chua ready): " + index);
                    forceRefreshTree();
                    sleep(500);
                    continue;
                }
                WebElement track = valid.get(index);
                Rectangle r = track.getRect();
                int tapX = r.getX() + r.getWidth() - 54;
                int tapY = r.getY() + r.getHeight() / 2;
                tap(tapX, tapY);
                if (waitEditSheetOpen(1800)) return;   // sheet mo -> xong
                forceRefreshTree();                    // tap truot / tree cu -> refresh roi thu lai
                sleep(400);
            } catch (Exception e) {
                last = new RuntimeException("clickEditButtonByIndex loi: " + e.getMessage());
                forceRefreshTree();
                sleep(400);
            }
        }
        if (last != null) logger.warn("clickEditButtonByIndex khong mo duoc sheet sau 4 lan: " + last.getMessage());
    }

    // ============================================
    // PLAY ALL / SHUFFLE
    // ============================================

    public void clickPlayAll() {
        logger.info("Click Play all");
        click(PLAY_ALL);
        sleep(1100);
    }

    public void clickShuffle() {
        logger.info("Click Shuffle");
        click(SHUFFLE);
        sleep(1100);
    }

    // ============================================
    // MINI PLAYER CONTROLS
    // ============================================

    public void clickMiniPlayerPlayPause() {
        logger.info("Click mini player play/pause");
        List<WebElement> buttons = driver.findElements(AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.ImageView\").clickable(true)"));
        for (WebElement el : buttons) {
            try {
                Rectangle r = el.getRect();
                if (r.getY() > 1900 && r.getX() > 1300 && r.getX() < 1500) {
                    tap(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
                    sleep(700);
                    return;
                }
            } catch (Exception ignored) {}
        }
        logger.warn("Khong tim duoc play/pause");
    }

    public void clickMiniPlayerQueue() {
        logger.info("Click mini player queue");
        // Nut queue = ImageView clickable PHAI NHAT trong dai mini player (tren bottom nav).
        // KHONG dung nguong x>1500 cung: app update co the doi vi tri (vd queue tu [1523..] ->
        // [1484..]) lam tap truot. Dai mini player: y_center ~1979..2136, bottom nav y>=2153.
        // Trong dai do co 3 ImageView clickable: ca thanh ("%,"), nut Play, nut Queue -> queue
        // la cai phai nhat (center-x lon nhat).
        List<WebElement> buttons = driver.findElements(AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.ImageView\").clickable(true)"));
        WebElement queueBtn = null;
        int maxCenterX = -1;
        for (WebElement el : buttons) {
            try {
                Rectangle r = el.getRect();
                int cy = r.getY() + r.getHeight() / 2;
                if (cy > 1950 && cy < 2150) {
                    int cx = r.getX() + r.getWidth() / 2;
                    if (cx > maxCenterX) { maxCenterX = cx; queueBtn = el; }
                }
            } catch (Exception ignored) {}
        }
        if (queueBtn != null) {
            Rectangle r = queueBtn.getRect();
            tap(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
            sleep(1100);
        } else {
            logger.warn("Khong tim thay nut queue tren mini player");
        }
    }

    public void clickMiniPlayerBody() {
        logger.info("Click mini player body -> Play Now");
        // Tap vung text track info (giua mini player, tranh icon ben phai)
        tap(500, 2055);
        sleep(1100);
    }

    // ============================================
    // SORT DIALOG
    // ============================================

    public void openSortDialog() {
        logger.info("Mo Sort by dialog");
        click(SORT_BUTTON);
        sleep(700);
    }

    public boolean isSortDialogOpen() {
        return isDisplayed(SORT_DIALOG_TITLE) && isDisplayed(SORT_TITLE);
    }

    public boolean isAllSortOptionsDisplayed() {
        return isDisplayed(SORT_TITLE) && isDisplayed(SORT_ARTIST) && isDisplayed(SORT_ALBUM)
                && isDisplayed(SORT_FILE_NAME) && isDisplayed(SORT_DURATION)
                && isDisplayed(SORT_DATE_ADDED) && isDisplayed(SORT_DATE_MODIFIED);
    }

    public void selectSortByTitle() { logger.info("Sort: Title"); click(SORT_TITLE); sleep(850); }
    public void selectSortByArtist() { logger.info("Sort: Artist"); click(SORT_ARTIST); sleep(850); }
    public void selectSortByAlbum() { logger.info("Sort: Album"); click(SORT_ALBUM); sleep(850); }
    public void selectSortByFileName() { logger.info("Sort: File name"); click(SORT_FILE_NAME); sleep(850); }
    public void selectSortByDuration() { logger.info("Sort: Duration"); click(SORT_DURATION); sleep(850); }
    public void selectSortByDateAdded() { logger.info("Sort: Date added"); click(SORT_DATE_ADDED); sleep(850); }
    public void selectSortByDateModified() { logger.info("Sort: Date modified"); click(SORT_DATE_MODIFIED); sleep(850); }

    public void closeSortDialogByX() {
        logger.info("Close sort by X");
        tap(1408, 1012);
        sleep(700);
    }

    public void closeSortDialogByScrim() {
        logger.info("Close sort by Scrim");
        tap(860, 400);
        sleep(700);
    }

    public void closeSortDialogByBack() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(650);
    }

    // ============================================
    // SORT VERIFICATION HELPERS
    // ============================================

    public boolean isOrderDifferent(List<String> before, List<String> after) {
        if (before.size() != after.size()) return true;
        int diff = 0;
        for (int i = 0; i < before.size(); i++) {
            if (!before.get(i).equals(after.get(i))) diff++;
        }
        return diff > 1;
    }

    public boolean isReversedOrder(List<String> a, List<String> b) {
        if (a.size() != b.size()) return false;
        int n = a.size();
        int matches = 0;
        for (int i = 0; i < n; i++) {
            if (a.get(i).equals(b.get(n - 1 - i))) matches++;
        }
        return matches >= n - 2;
    }

    public boolean isStringListAscending(List<String> list) {
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1).toLowerCase().compareTo(list.get(i).toLowerCase()) > 0) return false;
        }
        return true;
    }

    public boolean isStringListDescending(List<String> list) {
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1).toLowerCase().compareTo(list.get(i).toLowerCase()) < 0) return false;
        }
        return true;
    }

    public boolean isIntListAscending(List<Integer> list) {
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1) > list.get(i)) return false;
        }
        return true;
    }

    public boolean isIntListDescending(List<Integer> list) {
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1) < list.get(i)) return false;
        }
        return true;
    }

    // ============================================
    // EDIT BOTTOM SHEET
    // ============================================

    public boolean isEditSheetOpen() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_RENAME) && isDisplayed(SHEET_DELETE);
    }

    /** Poll cho edit sheet MO (sau click edit icon; tree co the cu -> ep refresh). */
    public boolean waitEditSheetOpen(int maxMs) {
        long deadline = System.currentTimeMillis() + maxMs;
        while (System.currentTimeMillis() < deadline) {
            try { driver.getPageSource(); } catch (Exception ignored) {}
            if (isEditSheetOpen()) return true;
            sleep(400);
        }
        return isEditSheetOpen();
    }

    public boolean isAllEditOptionsDisplayed() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_ADD_QUEUE)
                && isDisplayed(SHEET_ADD_PLAYLIST) && isDisplayed(SHEET_RENAME)
                && isDisplayed(SHEET_FILE_INFO) && isDisplayed(SHEET_SHARE)
                && isDisplayed(SHEET_DELETE);
    }

    public void clickSheetPlay() { logger.info("Sheet: Play"); click(SHEET_PLAY); sleep(1100); }
    public void clickSheetAddToQueue() { logger.info("Sheet: Add to queue"); click(SHEET_ADD_QUEUE); sleep(1100); }
    public void clickSheetAddToPlaylist() { logger.info("Sheet: Add to playlist"); click(SHEET_ADD_PLAYLIST); sleep(1100); }
    public void clickSheetRename() { logger.info("Sheet: Rename"); click(SHEET_RENAME); sleep(1100); }
    public void clickSheetFileInfo() { logger.info("Sheet: File info"); click(SHEET_FILE_INFO); sleep(1100); }
    public void clickSheetShare() { logger.info("Sheet: Share"); click(SHEET_SHARE); sleep(1700); }
    public void clickSheetDelete() { logger.info("Sheet: Delete"); click(SHEET_DELETE); sleep(850); }

    public void closeEditSheetByBack() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(700);
    }

    public void closeEditSheetByScrim() {
        tap(860, 400);
        sleep(700);
    }

    /**
     * Lay title cua track dang hien tren sheet (track ma user vua click edit).
     */
    public String getSheetTrackTitle() {
        try {
            List<WebElement> els = driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.view.View\").descriptionContains(\"-\")"));
            // Track title trong sheet o phia tren cac action
            for (WebElement el : els) {
                Rectangle r = el.getRect();
                if (r.getY() > 800 && r.getY() < 1100 && r.getX() > 400) {
                    return el.getAttribute("content-desc");
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ============================================
    // RENAME DIALOG
    // ============================================

    public boolean isRenameDialogOpen() {
        return isDisplayed(RENAME_INPUT) && isDisplayed(RENAME_CANCEL) && isDisplayed(RENAME_SAVE);
    }

    public String getRenameInputText() {
        try { return driver.findElement(RENAME_INPUT).getText(); }
        catch (Exception e) { return ""; }
    }

    public String getRenameCharCount() {
        // Ep refresh tree + tim element co content-desc dang "N/M" (vd "64/60"). Regex
        // descriptionMatches truc tiep doi khi khong match trong UA2 -> iterate + match Java.
        try { driver.getPageSource(); } catch (Exception ignored) {}
        try {
            for (WebElement el : driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().descriptionContains(\"/\")"))) {
                String d = el.getAttribute("content-desc");
                if (d != null && d.matches("\\d+/\\d+")) return d;
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void typeRenameValue(String text) {
        try {
            WebElement input = driver.findElement(RENAME_INPUT);
            input.clear();
            sleep(300);
            input.sendKeys(text);
            sleep(500);
        } catch (Exception e) { logger.warn("Loi nhap rename: " + e.getMessage()); }
    }

    public void clearRenameInput() {
        // Tap vao X icon o bounds [1424,911][1519,1006]
        tap(1471, 958);
        sleep(500);
    }

    public void clickRenameSave() {
        // KHONG hideKeyboard: UA2 hideKeyboard gui BACK -> HUY rename truoc khi SAVE. SAVE hien
        // tren ban phim -> click thang.
        click(RENAME_SAVE);
        sleep(1100);
    }

    public void clickRenameCancel() {
        click(RENAME_CANCEL);
        sleep(700);
    }

    // ============================================
    // DELETE DIALOG
    // ============================================

    public boolean isDeleteDialogOpen() {
        return isDisplayed(DELETE_TITLE) && isDisplayed(DELETE_CANCEL) && isDisplayed(DELETE_CONFIRM);
    }

    /** Poll cho delete confirm dialog MO (sau click Delete; tree co the cu -> ep refresh). */
    public boolean waitDeleteDialogOpen(int maxMs) {
        long deadline = System.currentTimeMillis() + maxMs;
        while (System.currentTimeMillis() < deadline) {
            try { driver.getPageSource(); } catch (Exception ignored) {}
            if (isDeleteDialogOpen()) return true;
            sleep(400);
        }
        return isDeleteDialogOpen();
    }

    public String getDeleteMessage() {
        try { return driver.findElement(DELETE_MESSAGE).getAttribute("content-desc"); }
        catch (Exception e) { return ""; }
    }

    public void clickDeleteCancel() { click(DELETE_CANCEL); sleep(700); }
    /** Bam DELETE xac nhan (element-based - toa do khong chuan khi ten file dai). Retry vi UA2
     *  hay rot 'socket hang up' luc xoa. Tra true neu da bam duoc. */
    public boolean clickDeleteConfirm() {
        logger.warn("Click DELETE - destructive!");
        for (int i = 0; i < 3; i++) {
            try {
                driver.findElement(DELETE_CONFIRM).click();
                sleep(1300);
                return true;
            } catch (Exception e) {
                logger.warn("clickDeleteConfirm thu " + (i + 1) + " loi: " + e.getMessage());
                try { driver.getPageSource(); } catch (Exception ignored) {}
                sleep(600);
            }
        }
        sleep(700);
        return false;
    }

    // ============================================
    // ADD TO PLAYLIST DIALOG
    // ============================================

    public boolean isAddToPlaylistDialogOpen() {
        return isDisplayed(PLAYLIST_TITLE) && isDisplayed(CREATE_NEW_PLAYLIST);
    }

    public boolean isMyFavoritePlaylistDisplayed() {
        return isDisplayed(MY_FAVORITE);
    }

    public String getMyFavoriteText() {
        try {
            List<WebElement> els = driver.findElements(MY_FAVORITE);
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    public void clickMyFavorite() { click(MY_FAVORITE); sleep(850); }
    public void clickCreateNewPlaylist() { click(CREATE_NEW_PLAYLIST); sleep(850); }

    /** Poll cho add-to-playlist dialog MO (sau click "Add to playlist"; tree hay cu -> refresh). */
    public boolean waitAddToPlaylistOpen(int maxMs) {
        long deadline = System.currentTimeMillis() + maxMs;
        while (System.currentTimeMillis() < deadline) {
            try { driver.getPageSource(); } catch (Exception ignored) {}
            if (isAddToPlaylistDialogOpen()) return true;
            sleep(400);
        }
        return isAddToPlaylistDialogOpen();
    }

    /** Poll cho add-to-playlist sheet DONG (sau khi chon 1 playlist -> toast + dong sheet). */
    public boolean waitAddToPlaylistClosed(int maxMs) {
        long deadline = System.currentTimeMillis() + maxMs;
        while (System.currentTimeMillis() < deadline) {
            try { driver.getPageSource(); } catch (Exception ignored) {}
            if (!isAddToPlaylistDialogOpen()) return true;
            sleep(400);
        }
        return !isAddToPlaylistDialogOpen();
    }

    /** So playlist trong sheet add-to-playlist (cac dong co "N tracks", tru "Create new playlist"). */
    public int getPlaylistCountInSheet() {
        int c = 0;
        try {
            for (WebElement el : driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().descriptionMatches(\"(?s).*\\\\d+ tracks?.*\")"))) {
                String d = el.getAttribute("content-desc");
                if (d != null && d.matches("(?s).*\\d+ tracks?.*")) c++;
            }
        } catch (Exception ignored) {}
        return c;
    }

    // ===== CREATE NEW PLAYLIST DIALOG (EditText hint "Playlist name", char "0/60", CANCEL/SAVE) =====
    private final By CREATE_PL_INPUT = AppiumBy.className("android.widget.EditText");
    private final By CREATE_PL_SAVE = AppiumBy.accessibilityId("SAVE");
    private final By CREATE_PL_CANCEL = AppiumBy.accessibilityId("CANCEL");

    public void typeNewPlaylistName(String name) {
        try {
            WebElement in = driver.findElement(CREATE_PL_INPUT);
            in.clear();
            sleep(300);
            in.sendKeys(name);
            sleep(600);
        } catch (Exception e) { logger.warn("typeNewPlaylistName loi: " + e.getMessage()); }
    }

    public void clickCreatePlaylistSave() {
        // KHONG hideKeyboard (UA2 hideKeyboard gui BACK -> nut SAVE bien mat/khong commit).
        // SAVE hien tren ban phim -> click thang.
        click(CREATE_PL_SAVE);
        sleep(1100);
    }

    public void clickCreatePlaylistCancel() {
        click(CREATE_PL_CANCEL);
        sleep(850);
    }

    /** Playlist co ten 'name' co xuat hien trong sheet add-to-playlist khong. */
    public boolean isPlaylistInSheet(String name) {
        return isDisplayed(AppiumBy.androidUIAutomator(
                "new UiSelector().descriptionStartsWith(\"" + name + "\")"));
    }

    // ============================================
    // FILE INFORMATION SCREEN
    // ============================================

    public boolean isFileInfoScreenOpen() {
        return isDisplayed(FILE_INFO_TITLE);
    }

    public boolean areAllFileInfoFieldsDisplayed() {
        return isDisplayed(LABEL_FILE_PATH) && isDisplayed(LABEL_TITLE_FIELD)
                && isDisplayed(LABEL_ALBUM) && isDisplayed(LABEL_ARTIST)
                && isDisplayed(LABEL_GENRES) && isDisplayed(LABEL_DURATION)
                && isDisplayed(LABEL_SIZE);
    }

    public String getFileInfoValue(String fieldLabel) {
        // Mỗi field có 1 View label + 1 View value bên cạnh
        // Tìm element value (View focusable, không clickable, x > 637)
        try {
            List<WebElement> els = driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.view.View\").focusable(true)"));
            boolean foundLabel = false;
            for (WebElement el : els) {
                String desc = el.getAttribute("content-desc");
                if (desc == null) continue;
                if (foundLabel) {
                    return desc;
                }
                if (desc.equals(fieldLabel)) foundLabel = true;
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void closeFileInfo() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(700);
    }

    // ============================================
    // BO SUNG: TITLE TRUNCATE / SORT INDICATOR
    // ============================================

    /**
     * Co title du DAI de UI phai truncate khong. LUU Y: content-desc tra ve title DAY DU
     * (khong co "..."), truncation chi la visual o TextView -> khong the bat "..." truc tiep.
     * Dung proxy: ton tai title dai (>30 ky tu) — chac chan bi truncate tren 1 dong.
     */
    public boolean hasTruncatedTitle() {
        for (WebElement el : getValidTrackElements()) {
            try {
                String t = el.getAttribute("content-desc").split("\n")[0];
                if (t.endsWith("…") || t.endsWith("...") || t.length() > 30) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Option dang duoc CHON trong Sort dialog — nhan biet qua icon indicator (mui ten len/xuong)
     * nam ngay ben trai option do. Map Y cua indicator -> option co Y gan nhat.
     * Tra null neu khong tim thay.
     */
    public String getSelectedSortOption() {
        try {
            Map<String, Integer> optY = new LinkedHashMap<>();
            for (String opt : new String[]{"Title", "Artist", "Album", "File name",
                    "Duration", "Date added", "Date modified"}) {
                List<WebElement> e = driver.findElements(AppiumBy.accessibilityId(opt));
                if (!e.isEmpty()) {
                    Rectangle r = e.get(0).getRect();
                    optY.put(opt, r.getY() + r.getHeight() / 2);
                }
            }
            for (WebElement el : driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.view.View\").clickable(true)"))) {
                Rectangle r = el.getRect();
                // Indicator: nho, nam ben trai (x<400), trong vung dialog (y>900).
                if (r.getWidth() < 200 && r.getX() < 400 && r.getY() > 900) {
                    int iy = r.getY() + r.getHeight() / 2;
                    String best = null;
                    int bestD = Integer.MAX_VALUE;
                    for (Map.Entry<String, Integer> en : optY.entrySet()) {
                        int d = Math.abs(en.getValue() - iy);
                        if (d < bestD) { bestD = d; best = en.getKey(); }
                    }
                    if (best != null && bestD < 130) return best;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public boolean isReversedExact(List<String> a, List<String> b) {
        if (a.size() != b.size() || a.isEmpty()) return false;
        int n = a.size();
        for (int i = 0; i < n; i++) {
            if (!a.get(i).equals(b.get(n - 1 - i))) return false;
        }
        return true;
    }

    // ============================================
    // BO SUNG: MINI PLAYER PROGRESS
    // ============================================

    private final By MINI_PROGRESS = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionMatches(\"\\\\d+%,.*\")");

    /** % tien do tu mini player ("N%, <artist>"). -1 neu khong doc duoc. */
    public int getMiniPlayerProgressPercent() {
        try {
            List<WebElement> els = driver.findElements(MINI_PROGRESS);
            if (!els.isEmpty()) {
                Matcher m = Pattern.compile("(\\d+)%").matcher(els.get(0).getAttribute("content-desc"));
                if (m.find()) return Integer.parseInt(m.group(1));
            }
        } catch (Exception ignored) {}
        return -1;
    }

    // ============================================
    // BO SUNG: TRACK LONG PRESS
    // ============================================

    /**
     * Long-press 1 track -> mo Select mode.
     * VAN DE: adb input swipe MO duoc select mode that su (mat thay) NHUNG UiAutomator2 KHONG
     * refresh accessibility tree (adb bom input bo qua UA2) -> findElements van thay tree cu
     * (man Tracks) -> isSelectModeOpen=false. Vi vay:
     *   1) Thu longClickGesture QUA UA2 (UA2 tu biet tree doi -> detect duoc).
     *   2) Neu chua mo -> adb input swipe (chac chan mo) roi EP UA2 doc lai tree (getPageSource)
     *      va poll isSelectModeOpen.
     */
    public void longPressTrackByIndex(int index) {
        // longClickGesture qua UA2 mo select mode VA UA2 tu biet (detect chac chan), nhung HOI
        // FLAKY -> RETRY toi 3 lan. Neu da o select mode roi thi khong long-press lai (tranh
        // long-press trong select mode gay tac dung phu).
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (isSelectModeOpen()) return;
            List<WebElement> valid = getValidTrackElements();
            if (index >= valid.size()) { sleep(700); continue; }
            Rectangle r = valid.get(index).getRect();
            int x = r.getX() + 800;
            int y = r.getY() + r.getHeight() / 2;
            logger.info("longPress attempt " + attempt + " at (" + x + "," + y + ")");
            try {
                Map<String, Object> a = new HashMap<>();
                a.put("x", x); a.put("y", y); a.put("duration", 2200);
                driver.executeScript("mobile: longClickGesture", a);
            } catch (Exception e) { logger.warn("longClickGesture loi: " + e.getMessage()); }
            if (waitSelectMode(2500)) {
                logger.info("Select mode mo (attempt " + attempt + ")");
                return;
            }
            logger.warn("Long-press attempt " + attempt + " chua mo select mode -> retry");
            sleep(600);
        }
        // Last resort: adb input swipe (chac chan mo) + ep refresh tree.
        List<WebElement> valid = getValidTrackElements();
        if (!valid.isEmpty() && index < valid.size()) {
            Rectangle r = valid.get(index).getRect();
            logger.warn("Long-press fallback adb input swipe");
            adbLongPress(r.getX() + 800, r.getY() + r.getHeight() / 2, 1500);
            waitSelectMode(3000);
        }
    }

    /** Poll isSelectModeOpen toi maxMs, moi vong EP UA2 doc lai tree bang getPageSource(). */
    private boolean waitSelectMode(int maxMs) {
        long deadline = System.currentTimeMillis() + maxMs;
        while (System.currentTimeMillis() < deadline) {
            try { driver.getPageSource(); } catch (Exception ignored) {}
            if (isSelectModeOpen()) return true;
            sleep(400);
        }
        return false;
    }

    /** Long-press qua ADB input swipe (cung toa do, giu duration ms) — touch event that. */
    private void adbLongPress(int x, int y, int durationMs) {
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "-s", constants.AppConstants.UDID,
                    "shell", "input", "swipe",
                    String.valueOf(x), String.valueOf(y),
                    String.valueOf(x), String.valueOf(y),
                    String.valueOf(durationMs));
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.getInputStream().readAllBytes();
            p.waitFor();
        } catch (Exception e) {
            logger.warn("adbLongPress loi: " + e.getMessage());
        }
    }

    // ============================================
    // BO SUNG: EDIT SHEET HEART + SWIPE-DOWN CLOSE
    // ============================================

    /** Heart o goc tren-phai header edit sheet (Button khong co content-desc) — tap toa do. */
    public void clickSheetHeart() {
        logger.info("Sheet: Heart (toggle favorite)");
        tap(1485, 990);
        sleep(700);
    }

    /** Dong sheet bang vuot xuong (swipe down) tren than sheet. */
    public void closeEditSheetBySwipeDown() {
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("startX", 860);
            args.put("startY", 1100);
            args.put("endX", 860);
            args.put("endY", 2200);
            args.put("speed", 2000);
            driver.executeScript("mobile: dragGesture", args);
        } catch (Exception e) {
            logger.warn("swipe down sheet loi: " + e.getMessage());
        }
        sleep(700);
    }

    // ============================================
    // BO SUNG: CREATE NEW PLAYLIST DIALOG
    // ============================================

    public boolean isCreatePlaylistDialogOpen() {
        // Dialog tao playlist moi co EditText nhap ten.
        return isDisplayed(AppiumBy.className("android.widget.EditText"));
    }

    // ============================================
    // BO SUNG: RENAME CHAR COUNT PARSE
    // ============================================

    /** So ky tu hien tai tu char count "N/60". -1 neu khong doc duoc. */
    public int getRenameCharCountCurrent() {
        String c = getRenameCharCount();
        if (c == null) return -1;
        Matcher m = Pattern.compile("(\\d+)/\\d+").matcher(c);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    // ============================================
    // BO SUNG: SCROLL LIST
    // ============================================

    // Scroll = VUOT NHANH (flick 120ms) qua adb input swipe. dragGesture cham/giu lau -> app
    // hieu nham long-press -> tu chuyen sang man SELECT. Flick nhanh tranh dieu nay (user xac nhan).
    public void scrollListDown() {
        adbFastSwipe(860, 1700, 860, 700, 120);
        sleep(700);
    }

    public void scrollListUp() {
        adbFastSwipe(860, 700, 860, 1900, 120);
        sleep(700);
    }

    /** Vuot nhanh (flick) qua adb input swipe - duration nho = flick, khong bi hieu long-press. */
    private void adbFastSwipe(int x1, int y1, int x2, int y2, int durationMs) {
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "-s", constants.AppConstants.UDID,
                    "shell", "input", "swipe",
                    String.valueOf(x1), String.valueOf(y1),
                    String.valueOf(x2), String.valueOf(y2),
                    String.valueOf(durationMs));
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.getInputStream().readAllBytes();
            p.waitFor();
        } catch (Exception e) {
            logger.warn("adbFastSwipe loi: " + e.getMessage());
        }
    }

    // ============================================
    // BO SUNG: SELECT MODE (long-press 1 track -> multi-select)
    // ============================================

    // descriptionContains("selected") thay vi regex \\d+ items? selected — robust hon, header
    // "N item selected" luon chua "selected". (Regex co the khong match trong UA2 voi 1 so case.)
    private final By SELECT_MODE_LABEL = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"selected\")");
    private final By SELECT_ADD_QUEUE = AppiumBy.accessibilityId("Add to queue");
    private final By SELECT_ADD_LIST = AppiumBy.accessibilityId("Add to list");
    private final By SELECT_SHARE = AppiumBy.accessibilityId("Share file");
    private final By SELECT_DELETE = AppiumBy.accessibilityId("Delete file");
    // Item trong select mode la ImageView clickable co content-desc "... • M:SS".
    private final By SELECT_ITEMS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.ImageView\").clickable(true).descriptionContains(\" • \")");

    public boolean isSelectModeOpen() {
        // "Add to queue" + "Delete file" CHI co o select mode (khac edit sheet "Add to playing
        // queue"/"Delete from device"). Dung 2 content-desc nay -> nhan biet chac chan.
        return isDisplayed(SELECT_ADD_QUEUE) && isDisplayed(SELECT_DELETE);
    }

    /** So item dang chon tu header "N item(s) selected". -1 neu khong doc duoc. */
    public int getSelectedCount() {
        // Ep UA2 doc lai tree (sau gesture select tree co the cu) roi moi tim label.
        try { driver.getPageSource(); } catch (Exception ignored) {}
        try {
            for (WebElement el : driver.findElements(SELECT_MODE_LABEL)) {
                String d = el.getAttribute("content-desc");
                if (d != null && d.contains("selected")) {
                    Matcher m = Pattern.compile("(\\d+)").matcher(d);
                    if (m.find()) return Integer.parseInt(m.group(1));
                }
            }
        } catch (Exception ignored) {}
        return -1;
    }

    public boolean areSelectActionsDisplayed() {
        return isDisplayed(SELECT_ADD_QUEUE) && isDisplayed(SELECT_ADD_LIST)
                && isDisplayed(SELECT_SHARE) && isDisplayed(SELECT_DELETE);
    }

    /** Tap 1 item trong select mode de toggle chon. */
    public void tapSelectItemByIndex(int index) {
        List<WebElement> items = driver.findElements(SELECT_ITEMS);
        if (index >= items.size()) {
            throw new RuntimeException("Select item index out of range: " + index + "/" + items.size());
        }
        Rectangle r = items.get(index).getRect();
        tap(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
        sleep(500);
    }

    /** Nut "select all" goc tren-phai (ImageView [1579,64][1710,190]). */
    public void clickSelectAll() {
        tap(1644, 127);
        sleep(650);
    }

    /** Thoat select mode (nut back/close goc tren-trai [38,73][146,181]). */
    public void exitSelectMode() {
        tap(92, 127);
        sleep(650);
    }

    // Action bar duoi (tap ICON o y~2180, on dinh hon label clickable=false):
    public void clickSelectAddToQueue() { logger.info("Select: Add to queue"); tap(250, 2180); sleep(850); }
    public void clickSelectAddToList()  { logger.info("Select: Add to list");  tap(657, 2180); sleep(850); }
    public void clickSelectShare()      { logger.info("Select: Share file");   tap(1063, 2180); sleep(1300); }
    public void clickSelectDelete()     { logger.info("Select: Delete file");  tap(1469, 2180); sleep(850); }

    // ============================================
    // BO SUNG: SYSTEM DELETE PERMISSION POPUP
    // (Android scoped-storage MediaProvider — hien khi xoa file that, ca edit-sheet
    //  Delete from device LAN select-mode Delete file. Title id/dialog_title,
    //  nut "Cho phep"=android:id/button1, "Tu choi"=android:id/button2.)
    // ============================================

    private final By DEL_PERM_TITLE = AppiumBy.id(
            "com.google.android.providers.media.module:id/dialog_title");
    private final By DEL_PERM_THUMB = AppiumBy.id(
            "com.google.android.providers.media.module:id/thumb_full");
    private final By DEL_PERM_ALLOW = AppiumBy.id("android:id/button1");  // Cho phep
    private final By DEL_PERM_DENY = AppiumBy.id("android:id/button2");   // Tu choi

    public boolean isDeletePermissionPopupOpen() {
        return isDisplayed(DEL_PERM_TITLE)
                || (isDisplayed(DEL_PERM_ALLOW) && isDisplayed(DEL_PERM_DENY));
    }

    /** Ten file (.mp3) dang duoc xoa, lay tu thumbnail content-desc. */
    public String getDeletePermissionFileName() {
        try {
            List<WebElement> els = driver.findElements(DEL_PERM_THUMB);
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    public boolean isDeletePermissionAllowDisplayed() { return isDisplayed(DEL_PERM_ALLOW); }
    public boolean isDeletePermissionDenyDisplayed() { return isDisplayed(DEL_PERM_DENY); }

    /** Bam "Cho phep" (Allow) -> hoan tat xoa file. */
    public void clickDeletePermissionAllow() {
        logger.info("Delete permission popup: CHO PHEP (Allow)");
        click(DEL_PERM_ALLOW);
        sleep(1300);
    }

    /** Bam "Tu choi" (Deny) -> huy xoa. */
    public void clickDeletePermissionDeny() {
        logger.info("Delete permission popup: TU CHOI (Deny)");
        click(DEL_PERM_DENY);
        sleep(1100);
    }

    /**
     * Sau khi bam DELETE/Delete file, neu HE THONG hien popup xin quyen xoa thi xu ly:
     * allow=true -> Cho phep (xoa that); false -> Tu choi (huy). Tra true neu da xu ly popup.
     */
    public boolean handleDeletePermissionPopup(boolean allow) {
        // Popup la HE THONG (package khac) -> UA2 tree hay CU, phai ep getPageSource() refresh
        // moi thay (neu khong se tra false -> khong bam Allow -> file KHONG bi xoa).
        for (int i = 0; i < 10; i++) {
            try { driver.getPageSource(); } catch (Exception ignored) {}
            if (isDeletePermissionPopupOpen()) {
                if (allow) clickDeletePermissionAllow(); else clickDeletePermissionDeny();
                return true;
            }
            sleep(500);
        }
        return false;
    }
}