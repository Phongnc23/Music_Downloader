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
 * Artists list screen + Artist-level edit bottom sheet.
 *
 * Artist edit sheet KHAC voi Track edit sheet:
 *  - Chi co 4 actions: Play / Add to queue / Add to playlist / Share track
 *  - KHONG co Rename/Delete/FileInfo
 *  - Share track co limit: artist > 10 songs -> sheet dong + toast
 */
public class ArtistsPage {

    private static final Logger logger = LogManager.getLogger(ArtistsPage.class);
    public static final int SHARE_LIMIT = 10;

    private final AppiumDriver driver;

    // ===== HEADER =====
    private final By TITLE_ARTISTS = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Artists\").className(\"android.view.View\")");
    // descriptionContains("artists") (lowercase) on dinh hon descriptionMatches (non-breaking
    // space) - chi count "N artists" co lowercase; title/nav la "Artists" (hoa) nen khong dung.
    private final By ARTISTS_COUNT = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"artists\")");
    private final By SORT_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.Button\").clickable(true).instance(0)");

    // ===== ARTIST CARDS =====
    // Artist card = ImageView clickable co content-desc dang "name\nN tracks"
    private final By ARTIST_CARDS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.ImageView\").clickable(true).descriptionContains(\"tracks\")");

    // ===== EDIT BOTTOM SHEET (artist-level) =====
    // descriptionContains("songs") - sheet header "name\nN songs". Card list dung "N tracks" nen
    // khong dung. On dinh hon descriptionMatches (\n + non-breaking space).
    private final By SHEET_ARTIST_HEADER = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"songs\")");
    private final By SHEET_PLAY = AppiumBy.accessibilityId("Play");
    private final By SHEET_ADD_QUEUE = AppiumBy.accessibilityId("Add to playing queue");
    private final By SHEET_ADD_PLAYLIST = AppiumBy.accessibilityId("Add to playlist");
    private final By SHEET_SHARE = AppiumBy.accessibilityId("Share track");
    private final By SCRIM = AppiumBy.accessibilityId("Scrim");

    // ===== BOTTOM NAV =====
    private final By NAV_ARTISTS = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Artists\").className(\"android.widget.ImageView\")");

    public ArtistsPage(AppiumDriver driver) {
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

    public void openArtistsFromBottomNav() {
        logger.info("Click Artists tab");
        click(NAV_ARTISTS);
        sleep(2500);
    }

    public boolean isOnArtistsScreen() {
        // Title "Artists" + (count "N artists" HOAC co artist card). Count label doc qua UA2 hay
        // flaky (non-breaking space) nen khong bat buoc.
        return isDisplayed(TITLE_ARTISTS)
                && (isDisplayed(ARTISTS_COUNT) || !getArtistCards().isEmpty());
    }

    // ============== HEADER ==============

    public boolean isTitleDisplayed() { return isDisplayed(TITLE_ARTISTS); }

    public boolean isCountDisplayed() { return isDisplayed(ARTISTS_COUNT); }

    public int getArtistsCount() {
        for (int i = 0; i < 3; i++) {
            try {
                for (WebElement el : driver.findElements(ARTISTS_COUNT)) {
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

    public boolean isSortButtonDisplayed() { return isDisplayed(SORT_BUTTON); }

    /** Mini player (content-desc dang "32%, <artist>"). descriptionContains("%,") on dinh hon. */
    public boolean isMiniPlayerDisplayed() {
        return isDisplayed(AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"%,\")"));
    }

    // ============== ARTIST CARDS ==============

    public List<WebElement> getArtistCards() {
        try { return driver.findElements(ARTIST_CARDS); }
        catch (Exception e) { return new ArrayList<>(); }
    }

    public int getDisplayedArtistsCount() {
        return getArtistCards().size();
    }

    /**
     * Lay artist info "name\nN tracks" cho index.
     */
    public String getArtistDesc(int index) {
        List<WebElement> cards = getArtistCards();
        if (index >= cards.size()) return null;
        try { return cards.get(index).getAttribute("content-desc"); }
        catch (Exception e) { return null; }
    }

    public String getArtistName(int index) {
        String desc = getArtistDesc(index);
        if (desc == null || !desc.contains("\n")) return desc;
        return desc.split("\n")[0];
    }

    public int getArtistTrackCount(int index) {
        String desc = getArtistDesc(index);
        if (desc == null) return -1;
        Matcher m = Pattern.compile("(\\d+)\\D{0,5}tracks?").matcher(desc);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    /**
     * Click vao body cua artist card (tranh edit button o goc duoi phai).
     */
    public void clickArtistByIndex(int index) {
        List<WebElement> cards = getArtistCards();
        if (index >= cards.size()) throw new RuntimeException("Index out of range");
        WebElement card = cards.get(index);
        Rectangle r = card.getRect();
        // Tap vung tren cua card, tranh edit button o goc duoi
        int tapX = r.getX() + r.getWidth() / 2;
        int tapY = r.getY() + r.getHeight() / 3;
        tap(tapX, tapY);
        sleep(2500);
    }

    /**
     * Click edit button (3-dot) o goc duoi phai cua artist card.
     * Edit button bounds vd: [752,1317][860,1425] - khoang 100px tu goc.
     */
    public void clickEditButtonByIndex(int index) {
        List<WebElement> cards = getArtistCards();
        if (index >= cards.size()) throw new RuntimeException("Index out of range");
        WebElement card = cards.get(index);
        Rectangle r = card.getRect();
        // Edit button (3-dot) o goc duoi phai card. Vd card [0,476][860,1536], button center
        // (806,1371) ~ cach day card 165px, ~84.5% chieu cao.
        int tapX = r.getX() + r.getWidth() - 54;
        int tapY = r.getY() + (int) (r.getHeight() * 0.845);
        tap(tapX, tapY);
        sleep(2000);
    }

    // ============== EDIT BOTTOM SHEET (4 actions) ==============

    public boolean isEditSheetOpen() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_SHARE)
                && isDisplayed(SHEET_ADD_QUEUE) && isDisplayed(SHEET_ADD_PLAYLIST);
    }

    public boolean isAllArtistActionsDisplayed() {
        return isDisplayed(SHEET_PLAY) && isDisplayed(SHEET_ADD_QUEUE)
                && isDisplayed(SHEET_ADD_PLAYLIST) && isDisplayed(SHEET_SHARE);
    }

    public String getSheetArtistInfo() {
        try {
            List<WebElement> els = driver.findElements(SHEET_ARTIST_HEADER);
            if (!els.isEmpty()) return els.get(0).getAttribute("content-desc");
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Parse "N songs" tu sheet header.
     */
    public int getSheetArtistSongCount() {
        String info = getSheetArtistInfo();
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

    // ============== SHARE LIMIT CHECK ==============

    /**
     * Kiem tra trang thai sau khi click Share:
     *  - Neu N <= 10: share intent resolver mo (current package = intentresolver)
     *  - Neu N > 10: sheet dong + toast hien (van o app cu, sheet khong con)
     */
    public boolean isShareIntentResolverOpen() {
        try {
            String pkg = ((AndroidDriver) driver).getCurrentPackage();
            return pkg != null && pkg.contains("intentresolver");
        } catch (Exception e) { return false; }
    }

    /**
     * Sau khi share fail (> 10 songs), sheet phai dong va van o app.
     */
    public boolean isSheetClosedAndStillInApp() {
        try {
            String pkg = ((AndroidDriver) driver).getCurrentPackage();
            return pkg != null && pkg.contains("musicdownloadapp") && !isEditSheetOpen();
        } catch (Exception e) { return false; }
    }
}