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

/**
 * Search In Library screen.
 *
 * Mo bang cach tap search icon goc phai tai Home [1531,64][1682,184]
 * (KHAC voi "Search music online..." bar - do la search download online).
 *
 * Cau truc:
 *  - Back button [0,94][108,202] (no desc)
 *  - EditText [184,100][1474,197]
 *  - Clear X icon [1474,88][1605,208] (no desc)
 *  - 5 tabs: All, Tracks, Albums, Artists, Playlists
 *  - Results below tabs (y > 410), filtered theo tab active
 *
 * Result format:
 *  - Track: "title\n<artist> • duration"
 *  - Album/Artist/Playlist: "name\nN tracks"
 * No match: "Nothing found!" + "No results found in the library."
 */
public class SearchLibraryPage {

    private static final Logger logger = LogManager.getLogger(SearchLibraryPage.class);
    private final AppiumDriver driver;

    // Home search icon (top-right)
    private static final int HOME_SEARCH_ICON_X = 1606;
    private static final int HOME_SEARCH_ICON_Y = 124;
    // Back button center
    private static final int BACK_X = 54;
    private static final int BACK_Y = 148;
    // Clear X center
    private static final int CLEAR_X = 1539;
    private static final int CLEAR_Y = 148;

    private final By SEARCH_INPUT = AppiumBy.className("android.widget.EditText");
    private final By TAB_ALL = AppiumBy.accessibilityId("All");
    private final By TAB_TRACKS = AppiumBy.accessibilityId("Tracks");
    private final By TAB_ALBUMS = AppiumBy.accessibilityId("Albums");
    private final By TAB_ARTISTS = AppiumBy.accessibilityId("Artists");
    private final By TAB_PLAYLISTS = AppiumBy.accessibilityId("Playlists");
    private final By NOTHING_FOUND = AppiumBy.accessibilityId("Nothing found!");
    private final By NO_RESULTS_MSG = AppiumBy.accessibilityId("No results found in the library.");

    // Result rows = clickable elements; loc newline + y>410 TRONG JAVA (UA2 descriptionContains
    // KHONG match duoc ky tu newline that trong content-desc).
    private final By CLICKABLE = AppiumBy.androidUIAutomator(
            "new UiSelector().clickable(true)");

    public SearchLibraryPage(AppiumDriver driver) {
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

    /**
     * Result rows: clickable + content-desc co "\n", nam duoi tabs (y > 410).
     */
    private List<WebElement> getResultRows() {
        List<WebElement> rows = new ArrayList<>();
        try {
            for (WebElement el : driver.findElements(CLICKABLE)) {
                try {
                    Rectangle r = el.getRect();
                    if (r.getY() <= 410) continue;   // bo tab bar / header
                    String desc = el.getAttribute("content-desc");
                    if (desc != null && desc.contains("\n")) rows.add(el);  // newline THAT (Java)
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return rows;
    }

    // ============== NAVIGATION ==============

    /**
     * Tap search icon goc phai tai Home -> mo Search In Library.
     */
    public void openFromHomeSearchIcon() {
        logger.info("Tap search icon goc phai (Home)");
        tap(HOME_SEARCH_ICON_X, HOME_SEARCH_ICON_Y);
        sleep(2000);
    }

    public boolean isOnSearchScreen() {
        return isDisplayed(SEARCH_INPUT) && isDisplayed(TAB_ALL);
    }

    public void clickBack() {
        logger.info("Tap Back");
        tap(BACK_X, BACK_Y);
        sleep(1500);
    }

    // ============== SEARCH INPUT ==============

    public boolean isSearchInputDisplayed() { return isDisplayed(SEARCH_INPUT); }

    public void typeQuery(String query) {
        logger.info("Nhap query: " + query);
        try {
            WebElement input = driver.findElement(SEARCH_INPUT);
            input.click();
            sleep(300);
            input.clear();
            sleep(300);
            input.sendKeys(query);
            sleep(1500);  // cho ket qua load
        } catch (Exception e) { logger.warn("Loi nhap query: " + e.getMessage()); }
    }

    public String getQueryText() {
        try { return driver.findElement(SEARCH_INPUT).getText(); }
        catch (Exception e) { return ""; }
    }

    public void clearQuery() {
        logger.info("Clear query (X button)");
        tap(CLEAR_X, CLEAR_Y);
        sleep(1200);
    }

    public void clearQueryViaInput() {
        try {
            driver.findElement(SEARCH_INPUT).clear();
            sleep(1200);
        } catch (Exception e) { logger.warn("Clear input loi: " + e.getMessage()); }
    }

    /**
     * An ban phim bang BACK 1 lan (BACK lan 1 chi tat keyboard, KHONG thoat search) -> mini player
     * / ket qua duoi khong con bi che. (Theo hanh vi thuc te cua app.)
     */
    public void hideKeyboard() {
        try { ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK)); }
        catch (Exception ignored) {}
        sleep(800);
    }

    // ============== TABS ==============

    /** 4 tab dau (All/Tracks/Albums/Artists) - luon render tren man rong. */
    public boolean areCoreTabsDisplayed() {
        return isDisplayed(TAB_ALL) && isDisplayed(TAB_TRACKS)
                && isDisplayed(TAB_ALBUMS) && isDisplayed(TAB_ARTISTS);
    }

    /** Ca 5 tab. Playlists nam ngoai HorizontalScrollView -> cuon ngang de lo no ra neu can. */
    public boolean areAllTabsDisplayed() {
        return areCoreTabsDisplayed() && (isDisplayed(TAB_PLAYLISTS) || ensureTabVisible("Playlists"));
    }

    public boolean isTabDisplayed(String tab) {
        return isDisplayed(AppiumBy.accessibilityId(tab)) || ensureTabVisible(tab);
    }

    /** Cuon ngang tab bar de tab {@code tab} vao vung nhin (vd Playlists o cuoi). */
    public boolean ensureTabVisible(String tab) {
        if (isDisplayed(AppiumBy.accessibilityId(tab))) return true;
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).setAsHorizontalList()"
                            + ".scrollIntoView(new UiSelector().description(\"" + tab + "\"))"));
            sleep(500);
        } catch (Exception ignored) {}
        return isDisplayed(AppiumBy.accessibilityId(tab));
    }

    // Man khong du rong cho ca 5 tab -> phai cuon tab bar (ngang) de lo tab can bam.
    public void clickTabAll() { clickTab("All", TAB_ALL); }
    public void clickTabTracks() { clickTab("Tracks", TAB_TRACKS); }
    public void clickTabAlbums() { clickTab("Albums", TAB_ALBUMS); }
    public void clickTabArtists() { clickTab("Artists", TAB_ARTISTS); }
    public void clickTabPlaylists() { clickTab("Playlists", TAB_PLAYLISTS); }

    private void clickTab(String name, By locator) {
        logger.info("Tab: " + name);
        ensureTabVisible(name);   // cuon tab bar lo tab (vd Playlists o cuoi phai keo sang trai)
        click(locator);
        sleep(1500);
    }

    // ============== RESULTS ==============

    public boolean isNoResultsDisplayed() {
        return isDisplayed(NOTHING_FOUND) || isDisplayed(NO_RESULTS_MSG);
    }

    public boolean hasResults() {
        return !getResultRows().isEmpty() && !isNoResultsDisplayed();
    }

    public int getResultCount() {
        return getResultRows().size();
    }

    public List<String> getResultTitles() {
        List<String> titles = new ArrayList<>();
        for (WebElement el : getResultRows()) {
            try {
                String desc = el.getAttribute("content-desc");
                if (desc != null && desc.contains("\n")) titles.add(desc.split("\n")[0]);
            } catch (Exception ignored) {}
        }
        return titles;
    }

    public List<String> getResultFullDescs() {
        List<String> descs = new ArrayList<>();
        for (WebElement el : getResultRows()) {
            try {
                String desc = el.getAttribute("content-desc");
                if (desc != null) descs.add(desc);
            } catch (Exception ignored) {}
        }
        return descs;
    }

    /**
     * Kiem tra co ket qua chua title chua substring (case-insensitive).
     */
    public boolean hasResultContaining(String substring) {
        String lower = substring.toLowerCase();
        for (String t : getResultTitles()) {
            if (t.toLowerCase().contains(lower)) return true;
        }
        return false;
    }

    public void clickResultByIndex(int index) {
        List<WebElement> rows = getResultRows();
        if (index >= rows.size()) throw new RuntimeException("Result index out of range: " + index);
        WebElement row = rows.get(index);
        Rectangle r = row.getRect();
        tap(r.getX() + 600, r.getY() + r.getHeight() / 2);
        sleep(2500);
    }

    /**
     * Lay title cua track result dau tien (co " • " trong desc).
     */
    public String getFirstTrackResultTitle() {
        for (WebElement el : getResultRows()) {
            try {
                String desc = el.getAttribute("content-desc");
                if (desc != null && desc.contains(" • ")) return desc.split("\n")[0];
            } catch (Exception ignored) {}
        }
        return null;
    }

    // ============== STATUS ==============

    public boolean isShareIntentResolverOpen() {
        try {
            String pkg = ((AndroidDriver) driver).getCurrentPackage();
            return pkg != null && pkg.contains("intentresolver");
        } catch (Exception e) { return false; }
    }

    /** Mini player (content-desc dang "N%, ...") -> dang phat. */
    public boolean isMiniPlayerDisplayed() {
        return isDisplayed(AppiumBy.androidUIAutomator(
                "new UiSelector().descriptionContains(\"%,\")"));
    }

    /** Click result theo tab dang active, lay result dau co " • " (track) hoac " tracks" (album/playlist). */
    public String clickFirstResultGetTitle() {
        hideKeyboard();   // BACK 1 lan: tat ban phim (van o search) -> tap result sach, lo mini player
        List<WebElement> rows = getResultRows();
        if (rows.isEmpty()) return null;
        WebElement row = rows.get(0);
        String title = null;
        try {
            String desc = row.getAttribute("content-desc");
            if (desc != null && desc.contains("\n")) title = desc.split("\n")[0];
        } catch (Exception ignored) {}
        Rectangle r = row.getRect();
        tap(r.getX() + 600, r.getY() + r.getHeight() / 2);
        sleep(2500);
        return title;
    }
}