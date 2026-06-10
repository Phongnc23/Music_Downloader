package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object cho man hinh Home cua Music Downloader app.
 *
 * Self-contained: khong extend BasePage, tu chua driver/logger/helpers.
 *
 * Luu y locator:
 *  - Hau het element dung content-desc -> AppiumBy.accessibilityId
 *  - "Home" co 2 element (title View + bottom tab ImageView) -> phan biet bang class
 *  - Hamburger va search icon top khong co content-desc -> dung instance(0)/instance(1)
 */
public class HomePage {

    private static final Logger logger = LogManager.getLogger(HomePage.class);
    private static final int DEFAULT_TIMEOUT_SEC = 5;

    private final AppiumDriver driver;
    private final WebDriverWait wait;

    // ===== HEADER =====
    private final By HOME_TITLE = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Home\").className(\"android.view.View\")");

    private final By MENU_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.ImageView\").clickable(true).instance(0)");

    private final By SEARCH_ICON_TOP_RIGHT = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.ImageView\").clickable(true).instance(1)");

    // ===== SEARCH BAR =====
    private final By SEARCH_BAR = AppiumBy.accessibilityId("Search music online...");

    // ===== 4 QUICK ACTION BUTTONS =====
    private final By DOWNLOADED_BUTTON = AppiumBy.accessibilityId("Downloaded");
    private final By SLEEP_TIMER_BUTTON = AppiumBy.accessibilityId("Sleep timer");
    private final By RATE_US_BUTTON = AppiumBy.accessibilityId("Rate us");
    private final By SETTINGS_BUTTON = AppiumBy.accessibilityId("Settings");

    // ===== MINI PLAYER =====
    private final By MINI_PLAYER_CONTAINER = AppiumBy.androidUIAutomator(
            "new UiSelector().scrollable(true).clickable(true)");

    private final By MINI_PLAYER_TRACK_INFO = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"%, \")");

    // ===== BOTTOM NAVIGATION =====
    private final By BOTTOM_NAV_HOME = AppiumBy.androidUIAutomator(
            "new UiSelector().description(\"Home\").className(\"android.widget.ImageView\")");
    private final By BOTTOM_NAV_TRACKS = AppiumBy.accessibilityId("Tracks");
    private final By BOTTOM_NAV_ARTISTS = AppiumBy.accessibilityId("Artists");
    private final By BOTTOM_NAV_ALBUMS = AppiumBy.accessibilityId("Albums");
    private final By BOTTOM_NAV_PLAYLISTS = AppiumBy.accessibilityId("Playlists");

    // ===== EXIT CONFIRM DIALOG (hien khi nhan BACK o Home) =====
    // Pattern: title content-desc="Are you sure you want to exit?" + 2 nut "Exit"/"Cancel"
    // (co kem 1 native ad ben trong dialog).
    private final By EXIT_DIALOG_TITLE = AppiumBy.accessibilityId("Are you sure you want to exit?");
    private final By EXIT_DIALOG_EXIT_BUTTON = AppiumBy.accessibilityId("Exit");
    private final By EXIT_DIALOG_CANCEL_BUTTON = AppiumBy.accessibilityId("Cancel");

    public HomePage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SEC));
    }

    // ============================================
    // LOCAL HELPER METHODS
    // ============================================

    private boolean isDisplayed(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement el : elements) {
                if (el.isDisplayed()) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void click(By locator) {
        try {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            el.click();
        } catch (Exception e) {
            // Element clickable=false -> tap by coordinate
            logger.info("Element khong clickable - tap by coordinate: " + locator);
            tapByBounds(locator);
        }
    }

    /**
     * Tap vao tam element theo bounds (bypass clickable=false).
     */
    private void tapByBounds(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            if (elements.isEmpty()) {
                logger.warn("Khong tim thay element de tap: " + locator);
                return;
            }
            WebElement el = elements.get(0);
            Rectangle r = el.getRect();
            int cx = r.getX() + r.getWidth() / 2;
            int cy = r.getY() + r.getHeight() / 2;
            driver.executeScript("mobile: clickGesture",
                    java.util.Map.of("x", cx, "y", cy));
        } catch (Exception e) {
            logger.error("tapByBounds error: " + e.getMessage());
        }
    }

    // ============================================
    // VERIFICATION METHODS
    // ============================================

    public boolean isHomeScreenDisplayed() {
        return isDisplayed(BOTTOM_NAV_TRACKS) && isDisplayed(SETTINGS_BUTTON);
    }

    public boolean isHomeTitleDisplayed() {
        return isDisplayed(HOME_TITLE);
    }

    public boolean isMenuButtonDisplayed() {
        return isDisplayed(MENU_BUTTON);
    }

    public boolean isSearchIconDisplayed() {
        return isDisplayed(SEARCH_ICON_TOP_RIGHT);
    }

    public boolean isSearchBarDisplayed() {
        return isDisplayed(SEARCH_BAR);
    }

    public boolean isDownloadedButtonDisplayed() {
        return isDisplayed(DOWNLOADED_BUTTON);
    }

    public boolean isSleepTimerButtonDisplayed() {
        return isDisplayed(SLEEP_TIMER_BUTTON);
    }

    public boolean isRateUsButtonDisplayed() {
        return isDisplayed(RATE_US_BUTTON);
    }

    public boolean isSettingsButtonDisplayed() {
        return isDisplayed(SETTINGS_BUTTON);
    }

    public boolean isMiniPlayerDisplayed() {
        return isDisplayed(MINI_PLAYER_CONTAINER);
    }

    public boolean isBottomNavDisplayed() {
        return isDisplayed(BOTTOM_NAV_HOME)
                && isDisplayed(BOTTOM_NAV_TRACKS)
                && isDisplayed(BOTTOM_NAV_ARTISTS)
                && isDisplayed(BOTTOM_NAV_ALBUMS)
                && isDisplayed(BOTTOM_NAV_PLAYLISTS);
    }

    public boolean isAllQuickActionsDisplayed() {
        return isDownloadedButtonDisplayed()
                && isSleepTimerButtonDisplayed()
                && isRateUsButtonDisplayed()
                && isSettingsButtonDisplayed();
    }

    /**
     * Tab HOME cua bottom navigation co dang hien khong (desc "Home", class ImageView).
     *
     * <p>Dung de phan biet dang o 1 TAB bottom-nav that (Tracks/Artists/Albums/Playlists tab —
     * deu co tab Home) voi man PUSHED nhu SEARCH. Luu y: man Search co dai filter "All/Tracks/
     * Albums/Artists" trung content-desc voi cac tab khac, nhung KHONG co "Home" -> chi check
     * rieng tab Home moi phan biet dung. Tab that -> bam tab Home ve; man Search -> BACK.
     */
    public boolean isHomeTabDisplayed() {
        return isDisplayed(BOTTOM_NAV_HOME);
    }

    /** Dialog xac nhan thoat (hien khi nhan BACK o Home) co dang hien khong. */
    public boolean isExitDialogDisplayed() {
        return isDisplayed(EXIT_DIALOG_TITLE)
                || (isDisplayed(EXIT_DIALOG_EXIT_BUTTON) && isDisplayed(EXIT_DIALOG_CANCEL_BUTTON));
    }

    // ============================================
    // ACTION METHODS - EXIT DIALOG / BACK
    // ============================================

    /** Nhan nut BACK cua thiet bi. O Home se bung dialog xac nhan thoat. */
    public void pressBack() {
        logger.info("Press device BACK");
        driver.navigate().back();
    }

    /** Bam "Cancel" tren dialog thoat -> o lai trong app. */
    public void clickExitCancel() {
        logger.info("Click Cancel tren exit dialog (o lai app)");
        click(EXIT_DIALOG_CANCEL_BUTTON);
    }

    /** Bam "Exit" tren dialog thoat -> thoat app. */
    public void clickExitConfirm() {
        logger.info("Click Exit tren exit dialog (thoat app)");
        click(EXIT_DIALOG_EXIT_BUTTON);
    }

    // ============================================
    // ACTION METHODS - HEADER
    // ============================================

    public void clickMenuButton() {
        logger.info("Click hamburger menu");
        click(MENU_BUTTON);
    }

    /**
     * Dong drawer menu (mo boi hamburger). Drawer truot tu ben TRAI nen dong bang cach
     * VUOT phai -> trai. Diem bat dau o LUNG ben phai (~70% chieu rong) — KHONG sat mep phai
     * vi vung sat mep la scrim/home he lo, vuot o do khong "tom" duoc drawer. Keo sang trai
     * (~15%) de day drawer ra khoi man.
     */
    public void closeMenuDrawer() {
        // QUAN TRONG: KHONG dung device BACK — BACK o drawer (top-level) chi BUNG dialog
        // "Are you sure you want to exit?" chu KHONG dong drawer.
        // Cach tin cay nhat (kiem chung thuc te): TAP LAI nut hamburger goc tren-trai ->
        // toggle dong drawer. Fallback: vuot phai->trai. Moi lan deu verify bang
        // isMenuDrawerOpen() de chac chan drawer da dong.
        for (int attempt = 0; attempt < 4; attempt++) {
            if (!isMenuDrawerOpen()) {
                if (attempt > 0) logger.info("Drawer da dong sau " + attempt + " lan thu");
                return;
            }
            try {
                Dimension size = driver.manage().window().getSize();
                int w = size.getWidth();
                int h = size.getHeight();
                if (attempt % 2 == 0) {
                    // Tap lai hamburger (top-left ~0.037w, 0.053h) -> toggle dong drawer.
                    int hx = (int) (w * 0.037);
                    int hy = (int) (h * 0.053);
                    driver.executeScript("mobile: clickGesture",
                            java.util.Map.of("x", hx, "y", hy));
                    logger.info("Tap lai hamburger (" + hx + "," + hy + ") de toggle dong drawer");
                } else {
                    // Fallback: vuot phai->trai day drawer ra.
                    driver.executeScript("mobile: dragGesture", java.util.Map.of(
                            "startX", (int) (w * 0.70), "startY", (int) (h * 0.50),
                            "endX", (int) (w * 0.02), "endY", (int) (h * 0.50),
                            "speed", 1500));
                    logger.info("Vuot phai->trai de dong drawer menu");
                }
            } catch (Exception e) {
                logger.warn("closeMenuDrawer attempt " + attempt + " loi: " + e.getMessage());
            }
            try { Thread.sleep(900); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
        if (isMenuDrawerOpen()) {
            logger.warn("closeMenuDrawer: drawer VAN MO sau 4 lan thu");
        }
    }

    /**
     * Drawer (hamburger menu) co dang mo khong. Nhan biet bang item "Exit app" — chi
     * xuat hien trong drawer, KHONG co o Home (Home chi co quick action Downloaded/
     * Sleep timer/Rate us/Settings, khong co Exit app/Equalizer).
     */
    public boolean isMenuDrawerOpen() {
        return isDisplayed(AppiumBy.accessibilityId("Exit app"));
    }

    public void clickSearchIcon() {
        logger.info("Click search icon (top-right)");
        click(SEARCH_ICON_TOP_RIGHT);
    }

    // ============================================
    // ACTION METHODS - SEARCH & QUICK ACTIONS
    // ============================================

    public void clickSearchBar() {
        logger.info("Click search bar");
        click(SEARCH_BAR);
    }

    public void clickDownloaded() {
        logger.info("Click Downloaded button");
        click(DOWNLOADED_BUTTON);
    }

    public void clickSleepTimer() {
        logger.info("Click Sleep timer button");
        click(SLEEP_TIMER_BUTTON);
    }

    public void clickRateUs() {
        logger.info("Click Rate us button");
        click(RATE_US_BUTTON);
    }

    public void clickSettings() {
        logger.info("Click Settings button");
        click(SETTINGS_BUTTON);
    }

    // ============================================
    // ACTION METHODS - BOTTOM NAVIGATION
    // ============================================

    public void clickBottomNavHome() {
        logger.info("Click Home tab (bottom nav)");
        click(BOTTOM_NAV_HOME);
    }

    public void clickBottomNavTracks() {
        logger.info("Click Tracks tab");
        click(BOTTOM_NAV_TRACKS);
    }

    public void clickBottomNavArtists() {
        logger.info("Click Artists tab");
        click(BOTTOM_NAV_ARTISTS);
    }

    public void clickBottomNavAlbums() {
        logger.info("Click Albums tab");
        click(BOTTOM_NAV_ALBUMS);
    }

    public void clickBottomNavPlaylists() {
        logger.info("Click Playlists tab");
        click(BOTTOM_NAV_PLAYLISTS);
    }

    // ============================================
    // ACTION METHODS - MINI PLAYER
    // ============================================

    public void clickMiniPlayer() {
        logger.info("Click mini player track info");
        click(MINI_PLAYER_TRACK_INFO);
    }

    /**
     * Click play/pause button trong mini player.
     * Khong co content-desc -> tim ImageView clickable o vung bounds bottom-right cua mini player.
     */
    public void clickMiniPlayerPlayPause() {
        logger.info("Click play/pause in mini player");
        List<WebElement> playerButtons = driver.findElements(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.widget.ImageView\").clickable(true)"));
        for (WebElement el : playerButtons) {
            try {
                Rectangle r = el.getRect();
                // Play/pause o bounds [1372,1995][1523,2116]
                if (r.getY() > 1900 && r.getX() > 1300 && r.getX() < 1500) {
                    int cx = r.getX() + r.getWidth() / 2;
                    int cy = r.getY() + r.getHeight() / 2;
                    driver.executeScript("mobile: clickGesture",
                            java.util.Map.of("x", cx, "y", cy));
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        logger.warn("Khong tim duoc play/pause button");
    }

    public void clickMiniPlayerQueue() {
        logger.info("Click queue button in mini player");
        // Nut queue = ImageView clickable PHAI NHAT trong dai mini player (tren bottom nav).
        // KHONG dung nguong x>1500 cung: app update doi vi tri queue [1523..] -> [1484..] lam
        // tap truot. Dai mini player center-y ~1979..2136, bottom nav y>=2153.
        List<WebElement> playerButtons = driver.findElements(
                AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"android.widget.ImageView\").clickable(true)"));
        WebElement queueBtn = null;
        int maxCenterX = -1;
        for (WebElement el : playerButtons) {
            try {
                Rectangle r = el.getRect();
                int cy = r.getY() + r.getHeight() / 2;
                if (cy > 1950 && cy < 2150) {
                    int cx = r.getX() + r.getWidth() / 2;
                    if (cx > maxCenterX) { maxCenterX = cx; queueBtn = el; }
                }
            } catch (Exception ignored) {
            }
        }
        if (queueBtn != null) {
            Rectangle r = queueBtn.getRect();
            driver.executeScript("mobile: clickGesture",
                    java.util.Map.of("x", r.getX() + r.getWidth() / 2, "y", r.getY() + r.getHeight() / 2));
        } else {
            logger.warn("Khong tim duoc queue button");
        }
    }
}