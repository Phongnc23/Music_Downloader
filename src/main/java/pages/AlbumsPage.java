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
 * Albums grid screen + album-level Sort dialog + album-level edit sheet (4 actions).
 *
 * Album edit sheet giong Artist: Play/Add to queue/Add to playlist/Share track.
 * Share track co limit 10 songs: album > 10 tracks -> sheet dong + toast.
 *
 * Album-level Sort (sortByAlbums) khac in-album track sort:
 *  - Album grid sort: chu yeu Title (asc/desc toggle)
 *  - In-album track sort: 7 options (xem AlbumDetailPage)
 */
public class AlbumsPage {

    private static final Logger logger = LogManager.getLogger(AlbumsPage.class);
    public static final int SHARE_LIMIT = 10;

    private final AppiumDriver driver;

    // ===== HEADER =====
    private final By TITLE_ALBUMS = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Albums\").className(\"android.view.View\")");
    // descriptionContains("albums") (lowercase) on dinh hon descriptionMatches: count "N albums"
    // dung non-breaking space ( ) nen \\s+ KHONG match. Title/nav la "Albums" (hoa) -> khong clash.
    private final By ALBUMS_COUNT = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"albums\")");
    private final By SORT_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.Button\").clickable(true).instance(0)");

    // ===== ALBUM CARDS (grid) =====
    // Card = View clickable, content-desc dang "name\nN tracks". descriptionContains("tracks")
    // (non-breaking space giua N va tracks) - tren man Albums chi card co "tracks".
    private final By ALBUM_CARDS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.view.View\").clickable(true).descriptionContains(\"tracks\")");

    // ===== ALBUM-LEVEL SORT DIALOG =====
    private final By SORT_DIALOG_TITLE = AppiumBy.accessibilityId("Sort by");
    private final By SORT_OPT_TITLE = AppiumBy.accessibilityId("Title");

    // ===== EDIT BOTTOM SHEET (4 actions) =====
    // descriptionContains("songs") - header "name\nN songs" (non-breaking space). Card list dung
    // "tracks" nen khong clash.
    private final By SHEET_HEADER = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"songs\")");
    private final By SHEET_PLAY = AppiumBy.accessibilityId("Play");
    private final By SHEET_ADD_QUEUE = AppiumBy.accessibilityId("Add to playing queue");
    private final By SHEET_ADD_PLAYLIST = AppiumBy.accessibilityId("Add to playlist");
    private final By SHEET_SHARE = AppiumBy.accessibilityId("Share track");

    // ===== BOTTOM NAV =====
    private final By NAV_ALBUMS = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Albums\").className(\"android.widget.ImageView\")");

    public AlbumsPage(AppiumDriver driver) {
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

    public void openAlbumsFromBottomNav() {
        logger.info("Click Albums tab");
        click(NAV_ALBUMS);
        sleep(2500);
    }

    public boolean isOnAlbumsScreen() {
        // Count label doc qua UA2 hay flaky (non-breaking space) -> khong bat buoc, fallback co card.
        return isDisplayed(TITLE_ALBUMS)
                && (isDisplayed(ALBUMS_COUNT) || !getAlbumCards().isEmpty());
    }

    // ============== HEADER ==============

    public boolean isTitleDisplayed() { return isDisplayed(TITLE_ALBUMS); }
    public boolean isCountDisplayed() { return isDisplayed(ALBUMS_COUNT); }
    public boolean isSortButtonDisplayed() { return isDisplayed(SORT_BUTTON); }

    public int getAlbumsCount() {
        // Robust: iterate tat ca element khop "albums", lay cai dau co so; refresh+retry 3x.
        // findElement don le hay tra -1 (element dau khong co so / tree chua san / stale).
        for (int i = 0; i < 3; i++) {
            try {
                for (WebElement el : driver.findElements(ALBUMS_COUNT)) {
                    String desc = el.getAttribute("content-desc");
                    if (desc == null) continue;
                    Matcher m = Pattern.compile("(\\d+)").matcher(desc);
                    if (m.find()) return Integer.parseInt(m.group(1));
                }
            } catch (Exception ignored) {}
            try { driver.getPageSource(); } catch (Exception ignored) {}
            sleep(400);
        }
        return -1;
    }

    // ============== ALBUM CARDS ==============

    public List<WebElement> getAlbumCards() {
        try { return driver.findElements(ALBUM_CARDS); }
        catch (Exception e) { return new ArrayList<>(); }
    }

    public int getDisplayedAlbumsCount() { return getAlbumCards().size(); }

    public List<String> getAlbumNames() {
        // RETRY: tree co the cu/rong sau gesture (cuoi run dai) -> doc duoc list rong/thieu lam
        // check thu tu sort sai. Thu lai (refresh) toi khi co ten.
        List<String> names = new ArrayList<>();
        for (int attempt = 0; attempt < 3; attempt++) {
            names = new ArrayList<>();
            for (WebElement c : getAlbumCards()) {
                try {
                    String desc = c.getAttribute("content-desc");
                    if (desc != null && desc.contains("\n")) names.add(desc.split("\n")[0]);
                } catch (Exception ignored) {}
            }
            if (!names.isEmpty()) break;
            try { driver.getPageSource(); } catch (Exception ignored) {}
            sleep(600);
        }
        return names;
    }

    public String getAlbumDesc(int index) {
        List<WebElement> cards = getAlbumCards();
        if (index >= cards.size()) return null;
        try { return cards.get(index).getAttribute("content-desc"); }
        catch (Exception e) { return null; }
    }

    public String getAlbumName(int index) {
        String desc = getAlbumDesc(index);
        if (desc == null || !desc.contains("\n")) return desc;
        return desc.split("\n")[0];
    }

    public int getAlbumTrackCount(int index) {
        String desc = getAlbumDesc(index);
        if (desc == null) return -1;
        Matcher m = Pattern.compile("(\\d+)\\D{0,5}tracks?").matcher(desc);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    /**
     * Tim index cua album theo ten.
     */
    public int findAlbumIndexByName(String name) {
        List<String> names = getAlbumNames();
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).contains(name)) return i;
        }
        return -1;
    }

    public void clickAlbumByIndex(int index) {
        List<WebElement> cards = getAlbumCards();
        if (index >= cards.size()) throw new RuntimeException("Index out of range: " + index);
        WebElement card = cards.get(index);
        Rectangle r = card.getRect();
        // Tap vung tren card, tranh edit button goc duoi phai
        tap(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 3);
        sleep(2500);
    }

    public void clickAlbumByName(String name) {
        // scrollIntoView de lo album hang duoi (vd VoiceChanger), roi tap vung tren card.
        scrollAlbumIntoView(name);
        WebElement card = findCardByName(name);
        if (card == null) throw new RuntimeException("Khong tim thay album: " + name);
        Rectangle r = card.getRect();
        tap(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 3);
        sleep(2500);
    }

    /**
     * Click edit (3-dot) goc duoi phai cua album card.
     */
    public void clickEditButtonByIndex(int index) {
        List<WebElement> cards = getAlbumCards();
        if (index >= cards.size()) throw new RuntimeException("Index out of range");
        WebElement card = cards.get(index);
        Rectangle r = card.getRect();
        // 3-dot Button o goc duoi phai card. Vd card [0,497][860,1558], button center (806,1262)
        // ~72% chieu cao card (KHONG phai bottom-110 -> tap truot xuong duoi button).
        int tapX = r.getX() + r.getWidth() - 54;
        int tapY = r.getY() + (int) (r.getHeight() * 0.72);
        tap(tapX, tapY);
        sleep(2000);
    }

    /**
     * Click 3-dot cua album theo TEN - robust cho album hang duoi (3-dot bi mini player che):
     * scrollIntoView truoc, neu 3-dot van o vung mini player (y>=1850) thi swipe len roi tap lai.
     */
    public void clickEditButtonByName(String name) {
        scrollAlbumIntoView(name);
        WebElement card = findCardByName(name);
        if (card == null) throw new RuntimeException("Khong tim thay album: " + name);
        Rectangle r = card.getRect();
        int tapY = r.getY() + (int) (r.getHeight() * 0.72);
        if (tapY >= 1850) {                 // 3-dot bi mini player ([1979..]) che -> nang card len
            swipeGridUp();
            WebElement again = findCardByName(name);
            if (again != null) r = again.getRect();
            tapY = r.getY() + (int) (r.getHeight() * 0.72);
        }
        tap(r.getX() + r.getWidth() - 54, tapY);
        sleep(2000);
    }

    private WebElement findCardByName(String name) {
        for (WebElement c : getAlbumCards()) {
            try {
                String d = c.getAttribute("content-desc");
                if (d != null && d.contains(name)) return c;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void scrollAlbumIntoView(String name) {
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true).instance(0))"
                            + ".scrollIntoView(new UiSelector().descriptionContains(\"" + name + "\"))"));
            sleep(800);
        } catch (Exception e) { logger.warn("scrollAlbumIntoView: " + e.getMessage()); }
    }

    private void swipeGridUp() {
        try {
            Map<String, Object> a = new HashMap<>();
            a.put("left", 100); a.put("top", 500); a.put("width", 1500); a.put("height", 1300);
            a.put("direction", "up"); a.put("percent", 0.6);
            driver.executeScript("mobile: swipeGesture", a);
        } catch (Exception ignored) {}
        sleep(1000);
    }

    // ============== ALBUM-LEVEL SORT ==============

    public void openSortDialog() {
        logger.info("Mo album-level Sort dialog");
        // KHONG dung Button.instance(0): tren Albums grid, nut 3-dot cua card cung la Button va
        // dung TRUOC nut sort trong cay -> instance(0) bam nham 3-dot (mo edit sheet). Nut sort o
        // header (y nho nhat) -> chon Button tren cung.
        tapTopmostButton();
        sleep(1500);
    }

    /** Tap clickable Button co Y nho nhat (= nut sort o header; card 3-dot deu o y > 1200). */
    private void tapTopmostButton() {
        WebElement top = null;
        int minY = Integer.MAX_VALUE;
        try {
            for (WebElement b : driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.Button\").clickable(true)"))) {
                try {
                    Rectangle r = b.getRect();
                    if (r.getY() < minY) { minY = r.getY(); top = b; }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        if (top != null) {
            Rectangle r = top.getRect();
            tap(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
        }
    }

    public boolean isSortDialogOpen() {
        return isDisplayed(SORT_DIALOG_TITLE) && isDisplayed(SORT_OPT_TITLE);
    }

    public boolean isSortOptionDisplayed(String option) {
        return isDisplayed(AppiumBy.accessibilityId(option));
    }

    public void selectSortByTitle() {
        logger.info("Album sort: Title");
        click(SORT_OPT_TITLE);
        sleep(2000);
    }

    public void closeSortDialogByScrim() {
        tap(860, 300);
        sleep(1500);
    }

    public void closeSortDialogByBack() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(1200);
    }

    // ============== SORT VERIFICATION HELPERS ==============

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

    public boolean isOrderDifferent(List<String> a, List<String> b) {
        if (a.size() != b.size()) return true;
        int diff = 0;
        for (int i = 0; i < a.size(); i++) if (!a.get(i).equals(b.get(i))) diff++;
        return diff > 0;
    }

    // ============== EDIT SHEET (4 actions) ==============

    public boolean isEditSheetOpen() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_SHARE)
                && isDisplayed(SHEET_ADD_QUEUE) && isDisplayed(SHEET_ADD_PLAYLIST);
    }

    public boolean isAllAlbumActionsDisplayed() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_ADD_QUEUE)
                && isDisplayed(SHEET_ADD_PLAYLIST) && isDisplayed(SHEET_SHARE);
    }

    public String getSheetAlbumInfo() {
        try {
            List<WebElement> els = driver.findElements(SHEET_HEADER);
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    public int getSheetAlbumSongCount() {
        String info = getSheetAlbumInfo();
        if (info == null) return -1;
        Matcher m = Pattern.compile("(\\d+)\\D{0,5}songs?").matcher(info);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    public void clickSheetPlay() { logger.info("Sheet: Play"); click(SHEET_PLAY); sleep(3000); }
    public void clickSheetAddToQueue() { logger.info("Sheet: Add to queue"); click(SHEET_ADD_QUEUE); sleep(3000); }
    public void clickSheetAddToPlaylist() { logger.info("Sheet: Add to playlist"); click(SHEET_ADD_PLAYLIST); sleep(2500); }
    public void clickSheetShare() { logger.info("Sheet: Share"); click(SHEET_SHARE); sleep(3500); }

    public void closeEditSheetByBack() {
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(1500);
    }

    public void closeEditSheetByScrim() {
        tap(860, 200);
        sleep(1500);
    }

    // ============== SHARE LIMIT STATUS ==============

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