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
 * Playlists tab: Local playlists + User playlists + Create dialog + 2 loai edit sheet.
 *
 * 2 nhom playlist:
 *  - Local: My Favorite, Recently Played (he thong)
 *  - User: test, PL44740, ... (nguoi dung tao)
 *
 * 3 loai edit sheet:
 *  - User playlist: Play/Queue/Playlist/Rename/Share/Delete (6 actions)
 *  - Recently Played: Play/Queue/Playlist/Share/Clear recently played (5)
 *  - My Favorite: Play/Queue/Playlist/Share (+ co the Clear) - local
 *
 * Share track limit 10 songs: playlist > 10 tracks -> sheet dong + toast.
 */
public class PlaylistsPage {

    private static final Logger logger = LogManager.getLogger(PlaylistsPage.class);
    public static final int SHARE_LIMIT = 10;

    private final AppiumDriver driver;

    // ===== HEADER =====
    private final By TITLE_PLAYLISTS = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Playlists\").className(\"android.view.View\")");
    private final By LOCAL_LABEL = AppiumBy.accessibilityId("Local playlist");
    // "My playlist (N)" - dau cach co the la non-breaking space ( ) -> dung "." cho moi cach
    private final By PLAYLIST_WORD = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"playlist\")");
    private static final Pattern MY_PLAYLIST_PATTERN = Pattern.compile("\\((\\d+)\\)");

    // ===== LOCAL PLAYLISTS =====
    private final By MY_FAVORITE = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionStartsWith(\"My Favorite\")");
    private final By RECENTLY_PLAYED = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionStartsWith(\"Recently Played\")");

    // ===== CREATE NEW PLAYLIST =====
    private final By CREATE_NEW_PLAYLIST = AppiumBy.accessibilityId("Create new playlist");

    // ===== ALL PLAYLIST ITEMS (co "N tracks") - dung descriptionContains (nbsp-proof) =====
    private final By PLAYLIST_ITEMS = AppiumBy.androidUIAutomator(
            "new UiSelector().clickable(true).descriptionContains(\"tracks\")");

    // ===== CREATE / RENAME DIALOG =====
    private final By DIALOG_INPUT = AppiumBy.className("android.widget.EditText");
    // Char count "N/60": UA2 descriptionMatches khong on dinh -> dung descriptionContains("/60").
    private final By DIALOG_CHAR_COUNT = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"/60\")");
    private final By DIALOG_CANCEL = AppiumBy.accessibilityId("CANCEL");
    private final By DIALOG_SAVE = AppiumBy.accessibilityId("SAVE");
    private final By CREATE_DIALOG_TITLE = AppiumBy.accessibilityId("Create new playlist");
    private final By RENAME_DIALOG_TITLE = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Rename\")");

    // ===== EDIT SHEET (all variants) =====
    private final By SHEET_HEADER = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"tracks\")");
    private final By SHEET_PLAY = AppiumBy.accessibilityId("Play");
    private final By SHEET_ADD_QUEUE = AppiumBy.accessibilityId("Add to playing queue");
    private final By SHEET_ADD_PLAYLIST = AppiumBy.accessibilityId("Add to playlist");
    private final By SHEET_RENAME = AppiumBy.accessibilityId("Rename");
    private final By SHEET_SHARE = AppiumBy.accessibilityId("Share track");
    private final By SHEET_DELETE = AppiumBy.accessibilityId("Delete from device");
    private final By SHEET_CLEAR_RECENT = AppiumBy.accessibilityId("Clear recently played");

    // ===== DELETE / CLEAR CONFIRM DIALOG =====
    private final By CONFIRM_CANCEL = AppiumBy.accessibilityId("CANCEL");
    private final By CONFIRM_DELETE = AppiumBy.accessibilityId("DELETE");

    // ===== BOTTOM NAV =====
    private final By NAV_PLAYLISTS = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Playlists\").className(\"android.widget.ImageView\")");

    public PlaylistsPage(AppiumDriver driver) {
        this.driver = driver;
    }

    // ============== HELPERS ==============

    private boolean isDisplayed(By l) {
        try { return !driver.findElements(l).isEmpty(); } catch (Exception e) { return false; }
    }

    private void click(By l) {
        try { driver.findElement(l).click(); sleep(800); }
        catch (Exception e) { logger.warn("Click loi: " + e.getMessage()); }
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

    // ============== NAVIGATION ==============

    public void openPlaylistsFromBottomNav() {
        logger.info("Click Playlists tab");
        click(NAV_PLAYLISTS);
        sleep(2500);
    }

    public boolean isOnPlaylistsScreen() {
        // TOLERANT SCROLL: khi list cuon xuong, "Local playlist" off-screen -> KHONG duoc coi la
        // roi man Playlists (truoc day gay resetToPlaylists loop vo tan). Header "Playlists" co
        // dinh; ngoai ra con o man neu thay Local label HOAC co playlist item dang hien.
        return isDisplayed(TITLE_PLAYLISTS)
                && (isDisplayed(LOCAL_LABEL) || !driver.findElements(PLAYLIST_ITEMS).isEmpty());
    }

    /** Cuon list playlist ve dau. TOI UU: neu da thay "Local playlist" (item dau) thi dang o dau
     *  roi -> KHONG cuon (tranh thao tac thua). */
    public void scrollToTop() {
        if (isDisplayed(LOCAL_LABEL)) return;
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).scrollToBeginning(10)"));
            sleep(500);
        } catch (Exception ignored) {}
    }

    /** Tra ve row playlist theo ten neu DANG HIEN (khong cuon); null neu khong thay. */
    private WebElement findVisiblePlaylistRow(String name) {
        try {
            List<WebElement> els = driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().descriptionStartsWith(\"" + name + "\")"));
            return els.isEmpty() ? null : els.get(0);
        } catch (Exception e) { return null; }
    }

    // ============== UI VERIFICATION ==============

    public boolean isTitleDisplayed() { return isDisplayed(TITLE_PLAYLISTS); }
    public boolean isLocalLabelDisplayed() { return isDisplayed(LOCAL_LABEL); }
    public boolean isMyPlaylistLabelDisplayed() { return getMyPlaylistLabelDesc() != null; }
    public boolean isMyFavoriteDisplayed() { return isDisplayed(MY_FAVORITE); }
    public boolean isRecentlyPlayedDisplayed() { return isDisplayed(RECENTLY_PLAYED); }
    public boolean isCreateButtonDisplayed() { return isDisplayed(CREATE_NEW_PLAYLIST); }

    /**
     * Tim content-desc cua label "My playlist (N)": lay tat ca phan tu co "playlist" (Local
     * playlist / Create new playlist / My playlist (N)) roi chon cai co "(so)". Tranh UA2
     * descriptionMatches (khong on dinh voi text thuc te).
     */
    private String getMyPlaylistLabelDesc() {
        String d = findMyPlaylistLabelDesc();
        if (d == null) {
            // Label "My playlist (N)" o gan dau list; neu dang cuon xuong -> cuon len roi tim lai.
            scrollToTop();
            d = findMyPlaylistLabelDesc();
        }
        return d;
    }

    private String findMyPlaylistLabelDesc() {
        try {
            for (WebElement el : driver.findElements(PLAYLIST_WORD)) {
                String desc = el.getAttribute("content-desc");
                if (desc != null && MY_PLAYLIST_PATTERN.matcher(desc).find()) return desc;
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Lay so playlist user tu label "My playlist (N)".
     */
    public int getMyPlaylistCount() {
        String desc = getMyPlaylistLabelDesc();
        if (desc == null) return -1;
        Matcher m = MY_PLAYLIST_PATTERN.matcher(desc);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    public int getTrackCountOf(String playlistName) {
        // RETRY: doc element co the flaky (tra null/khong thay) trong run dai -> thu 3 lan.
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                WebElement el = driver.findElement(AppiumBy.androidUIAutomator(
                        "new UiSelector().descriptionStartsWith(\"" + playlistName + "\")"));
                String desc = el.getAttribute("content-desc");
                Matcher m = Pattern.compile("(\\d+)\\D{0,5}tracks?").matcher(desc);
                if (m.find()) return Integer.parseInt(m.group(1));
            } catch (Exception ignored) {}
            sleep(600);
        }
        return -1;
    }

    public List<String> getAllPlaylistNames() {
        List<String> names = new ArrayList<>();
        try {
            for (WebElement el : driver.findElements(PLAYLIST_ITEMS)) {
                String desc = el.getAttribute("content-desc");
                if (desc != null && desc.contains("\n")) names.add(desc.split("\n")[0]);
            }
        } catch (Exception ignored) {}
        return names;
    }

    public boolean isPlaylistPresent(String name) {
        if (getAllPlaylistNames().stream().anyMatch(n -> n.contains(name))) return true;
        // Co the playlist nam DUOI fold (list la ScrollView) -> cuon toi roi kiem tra lai.
        scrollToPlaylistName(name);
        return getAllPlaylistNames().stream().anyMatch(n -> n.contains(name));
    }

    /**
     * Chi kiem tra trong cac item DANG HIEN (KHONG cuon). Dung cho playlist o dau list / sau khi
     * vua thao tac (tranh cuon ca list khi kiem tra "vang mat" - rat ton thoi gian).
     */
    public boolean isPlaylistVisible(String name) {
        return getAllPlaylistNames().stream().anyMatch(n -> n.contains(name));
    }

    /**
     * Cuon list playlist toi item co content-desc chua {@code name}. Khong throw neu khong thay
     * (vd ten khong ton tai) — chi log. Dung truoc khi dinh vi playlist o duoi fold.
     */
    private void scrollToPlaylistName(String name) {
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true))"
                            + ".scrollIntoView(new UiSelector().descriptionContains(\"" + name + "\"))"));
            sleep(500);
        } catch (Exception e) {
            logger.info("scrollToPlaylistName khong thay '" + name + "' (co the chua ton tai)");
        }
    }

    // ============== NAVIGATION TO DETAIL ==============

    public void clickMyFavorite() { logger.info("Click My Favorite"); click(MY_FAVORITE); sleep(2500); }
    public void clickRecentlyPlayed() { logger.info("Click Recently Played"); click(RECENTLY_PLAYED); sleep(2500); }

    public void clickPlaylistByName(String name) {
        logger.info("Click playlist: " + name);
        if (findVisiblePlaylistRow(name) == null) scrollToPlaylistName(name);   // chi cuon neu chua hien
        click(AppiumBy.androidUIAutomator(
                "new UiSelector().descriptionStartsWith(\"" + name + "\")"));
        sleep(2500);
    }

    // ============== EDIT BUTTON ==============

    /**
     * Click edit (3-dot) cua playlist theo ten.
     * Edit button o goc phai cua row (x > 1612).
     */
    public void clickEditButtonOf(String playlistName) {
        // RETRY tap 3-dot toi khi sheet MO (toi da 3 lan). Khi chay tich hop (sau ~2h, nhieu
        // playback) UI render cham hon -> 1 tap don le co the khong kip mo sheet (flaky). Re-tap
        // trong cung 1 test on dinh hon retry ca test.
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                // Neu playlist DANG HIEN (vd playlist dau) -> tap luon, KHONG cuon. Chi cuon khi
                // chua hien (o duoi fold).
                WebElement el = findVisiblePlaylistRow(playlistName);
                if (el == null) {
                    scrollToPlaylistName(playlistName);
                    el = findVisiblePlaylistRow(playlistName);
                }
                if (el == null) { logger.warn("Khong tim thay row playlist: " + playlistName); return; }
                Rectangle r = el.getRect();
                tap(r.getX() + r.getWidth() - 54, r.getY() + r.getHeight() / 2);
                sleep(2000);
                if (isEditSheetOpen()) return;   // sheet da mo -> xong
                logger.info("clickEditButtonOf '" + playlistName + "' lan " + attempt
                        + ": sheet chua mo -> thu lai");
            } catch (Exception e) {
                logger.warn("clickEditButtonOf loi (lan " + attempt + "): " + e.getMessage());
            }
            sleep(600);
        }
    }

    // ============== CREATE PLAYLIST DIALOG ==============

    public void clickCreateNewPlaylist() {
        logger.info("Click Create new playlist");
        scrollToTop();   // nut Create o gan dau list -> dam bao no trong vung nhin (list co the dang cuon)
        click(CREATE_NEW_PLAYLIST);
        sleep(2000);
    }

    /** Ten playlist USER dau tien (bo qua My Favorite / Recently Played). null neu khong co. */
    public String getFirstUserPlaylist() {
        scrollToTop();
        for (String n : getAllPlaylistNames()) {
            if (!n.contains("My Favorite") && !n.contains("Recently Played")) return n;
        }
        return null;
    }

    public boolean isCreateDialogOpen() {
        return isDisplayed(CREATE_DIALOG_TITLE) && isDisplayed(DIALOG_INPUT)
                && isDisplayed(DIALOG_SAVE);
    }

    public boolean isDialogInputEmpty() {
        try {
            String t = driver.findElement(DIALOG_INPUT).getText();
            return t == null || t.isEmpty();
        } catch (Exception e) { return true; }
    }

    public String getDialogCharCount() {
        try {
            List<WebElement> els = driver.findElements(DIALOG_CHAR_COUNT);
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    public void typePlaylistName(String name) {
        try {
            WebElement input = driver.findElement(DIALOG_INPUT);
            input.click();          // QUAN TRONG: focus + bung keyboard truoc khi go (giong SearchPage)
            sleep(400);
            input.clear();
            sleep(300);
            input.sendKeys(name);
            sleep(700);
            // Verify text da vao chua (char count "L/.."); chua thi tap + go lai 1 lan.
            if (!isCharCountAtLeast(name.length())) {
                logger.warn("Text chua vao field -> tap + go lai: " + name);
                input.click();
                sleep(400);
                input.clear();
                sleep(300);
                input.sendKeys(name);
                sleep(700);
            }
        } catch (Exception e) { logger.warn("Loi nhap ten: " + e.getMessage()); }
    }

    /** Char count "N/60" co N >= expected khong (xac nhan text da nhap). */
    private boolean isCharCountAtLeast(int expected) {
        String c = getDialogCharCount();
        if (c == null) return false;
        Matcher m = Pattern.compile("(\\d+)\\s*/").matcher(c);
        if (m.find()) return Integer.parseInt(m.group(1)) >= expected;
        return false;
    }

    public String getDialogInputText() {
        try { return driver.findElement(DIALOG_INPUT).getText(); }
        catch (Exception e) { return ""; }
    }

    // KHONG goi hideKeyboard() o day: tren thiet bi nay hideKeyboard gui BACK -> DONG luon dialog
    // (mat nut SAVE/CANCEL). Nut SAVE/CANCEL (y~1270) nam TREN ban phim nen van bam truc tiep duoc.
    public void clickDialogSave() {
        click(DIALOG_SAVE);
        sleep(2500);
    }

    public void clickDialogCancel() {
        click(DIALOG_CANCEL);
        sleep(1500);
    }

    public void dismissDialog() {
        // Khi keyboard hien, dialog dich len -> tap(860,200) co the trung noi dung dialog (khong dong).
        // Tap scrim tren cung; neu con mo thi BACK (1-2 lan: lan 1 co the chi an keyboard).
        tap(860, 120);
        sleep(900);
        for (int i = 0; i < 2 && isCreateDialogOpen(); i++) {
            ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
            sleep(1000);
        }
        sleep(600);
    }

    // ============== EDIT SHEET ==============

    public boolean isEditSheetOpen() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_SHARE);
    }

    /**
     * User playlist sheet: co Rename + Delete.
     */
    public boolean isUserPlaylistSheet() {
        return isDisplayed(SHEET_RENAME) && isDisplayed(SHEET_DELETE);
    }

    /**
     * Local playlist sheet (Recently Played): co Clear recently played, khong co Rename/Delete.
     */
    public boolean isLocalPlaylistSheet() {
        return !isDisplayed(SHEET_RENAME) && !isDisplayed(SHEET_DELETE);
    }

    public boolean isClearRecentlyPlayedDisplayed() {
        return isDisplayed(SHEET_CLEAR_RECENT);
    }

    public boolean hasRenameOption() { return isDisplayed(SHEET_RENAME); }
    public boolean hasDeleteOption() { return isDisplayed(SHEET_DELETE); }

    /**
     * "Add to playlist" picker (mo tu sheet -> Add to playlist). Phan biet voi sheet 6-action:
     * picker co "Create new playlist" nhung KHONG con Play/Share track cua sheet goc.
     */
    public boolean isAddToPlaylistPickerOpen() {
        return isDisplayed(CREATE_NEW_PLAYLIST) && !isDisplayed(SHEET_PLAY) && !isDisplayed(SHEET_SHARE);
    }

    public String getSheetPlaylistInfo() {
        try {
            List<WebElement> els = driver.findElements(SHEET_HEADER);
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    public int getSheetTrackCount() {
        String info = getSheetPlaylistInfo();
        if (info == null) return -1;
        Matcher m = Pattern.compile("(\\d+)\\s+tracks?").matcher(info);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    public void clickSheetPlay() { click(SHEET_PLAY); sleep(3000); }
    public void clickSheetAddToQueue() { click(SHEET_ADD_QUEUE); sleep(3000); }
    public void clickSheetAddToPlaylist() { click(SHEET_ADD_PLAYLIST); sleep(2500); }
    public void clickSheetRename() { click(SHEET_RENAME); sleep(2500); }
    public void clickSheetShare() { click(SHEET_SHARE); sleep(3500); }
    public void clickSheetDelete() { click(SHEET_DELETE); sleep(2000); }
    public void clickSheetClearRecent() { click(SHEET_CLEAR_RECENT); sleep(2000); }

    public void closeEditSheetByBack() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(1500);
    }

    public void closeEditSheetByScrim() {
        tap(860, 200);
        sleep(1500);
    }

    // ============== RENAME DIALOG (reuse dialog input) ==============

    public boolean isRenameDialogOpen() {
        return isDisplayed(DIALOG_INPUT) && isDisplayed(DIALOG_SAVE) && isDisplayed(DIALOG_CANCEL);
    }

    public String getRenameInputText() { return getDialogInputText(); }
    public void typeRenameValue(String name) { typePlaylistName(name); }
    public void clickRenameSave() { clickDialogSave(); }
    public void clickRenameCancel() { clickDialogCancel(); }

    // ============== CONFIRM DIALOG (Delete / Clear) ==============

    public boolean isConfirmDialogOpen() {
        return isDisplayed(CONFIRM_CANCEL) && isDisplayed(CONFIRM_DELETE);
    }

    public void clickConfirmCancel() { click(CONFIRM_CANCEL); sleep(1500); }
    public void clickConfirmDelete() {
        logger.warn("Confirm DELETE/CLEAR - destructive!");
        click(CONFIRM_DELETE);
        sleep(2500);
    }

    // ============== SHARE STATUS ==============

    public boolean isShareIntentResolverOpen() {
        try {
            String pkg = ((AndroidDriver) driver).getCurrentPackage();
            return pkg != null && pkg.contains("intentresolver");
        } catch (Exception e) { return false; }
    }

    public boolean isSheetClosedAndStillInApp() {
        try {
            String pkg = ((AndroidDriver) driver).getCurrentPackage();
            return pkg != null && pkg.contains("musicdownloadapp") && !isEditSheetOpen();
        } catch (Exception e) { return false; }
    }

    public void dismissShareIntent() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(2000);
    }
}