package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object cho Hamburger Drawer Menu + cac dialog lien quan.
 *
 * Cau truc drawer:
 *  - Header: App icon, "Music Downloader", "Enjoy Listening"
 *  - 9 menu items: Equalizer, Downloaded, Sleep timer, Privacy policy,
 *    Rate us, Share app, Settings, Version 9999, Exit app
 *
 * Sleep Timer dialog co 2 state:
 *  - INITIAL: chua set timer, button "Set timer", khong co countdown
 *  - ACTIVE: timer dang chay, button "Reset", co "Timer: Xm Ys" countdown
 *
 * Custom Sleep Timer dialog: mo khi click "Custom" trong Sleep Timer dialog.
 *  - Title "Custom sleep timer", EditText input minutes, Cancel + Done
 */
public class MenuPage {

    private static final Logger logger = LogManager.getLogger(MenuPage.class);

    private final AppiumDriver driver;
    private final WebDriverWait wait;

    // ===== HEADER =====
    private final By APP_NAME = AppiumBy.accessibilityId("Music Downloader");
    private final By APP_TAGLINE = AppiumBy.accessibilityId("Enjoy Listening");

    // ===== MENU ITEMS =====
    private final By EQUALIZER_ITEM = AppiumBy.accessibilityId("Equalizer");
    private final By DOWNLOADED_ITEM = AppiumBy.accessibilityId("Downloaded");
    private final By SLEEP_TIMER_ITEM = AppiumBy.accessibilityId("Sleep timer");
    private final By PRIVACY_POLICY_ITEM = AppiumBy.accessibilityId("Privacy policy");
    private final By RATE_US_ITEM = AppiumBy.accessibilityId("Rate us");
    private final By SHARE_APP_ITEM = AppiumBy.accessibilityId("Share app");
    private final By SETTINGS_ITEM = AppiumBy.accessibilityId("Settings");
    private final By VERSION_ITEM = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"Version\")");
    private final By EXIT_APP_ITEM = AppiumBy.accessibilityId("Exit app");

    // ===== SLEEP TIMER DIALOG =====
    private final By TIMER_15_MIN = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"15\").descriptionContains(\"min\")");
    private final By TIMER_30_MIN = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"30\").descriptionContains(\"min\")");
    private final By TIMER_45_MIN = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"45\").descriptionContains(\"min\")");
    private final By TIMER_60_MIN = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"60\").descriptionContains(\"min\")");
    private final By TIMER_90_MIN = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionContains(\"90\").descriptionContains(\"min\")");
    private final By TIMER_CUSTOM = AppiumBy.accessibilityId("Custom");

    private final By TIMER_CANCEL_BTN = AppiumBy.accessibilityId("Cancel");
    private final By TIMER_SET_BTN = AppiumBy.accessibilityId("Set timer");
    private final By TIMER_RESET_BTN = AppiumBy.accessibilityId("Reset");
    private final By TIMER_COUNTDOWN = AppiumBy.androidUIAutomator(
            "new UiSelector().descriptionStartsWith(\"Timer:\")");

    // ===== CUSTOM SLEEP TIMER DIALOG =====
    private final By CUSTOM_DIALOG_TITLE = AppiumBy.accessibilityId("Custom sleep timer");
    private final By CUSTOM_MINUTES_LABEL = AppiumBy.accessibilityId("Minutes");
    private final By CUSTOM_MINUTES_INPUT = AppiumBy.className("android.widget.EditText");
    private final By CUSTOM_DONE_BTN = AppiumBy.accessibilityId("Done");
    // Custom Cancel - dung Cancel (chi co 1 Cancel khi Custom mo)
    // Sleep timer Cancel khong xuat hien cung luc voi Custom dialog

    // ===== EXIT CONFIRMATION DIALOG =====
    private final By EXIT_DIALOG_TITLE = AppiumBy.accessibilityId("Are you sure you want to exit?");
    private final By EXIT_DIALOG_EXIT_BTN = AppiumBy.accessibilityId("Exit");
    private final By EXIT_DIALOG_CANCEL_BTN = AppiumBy.accessibilityId("Cancel");

    public MenuPage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    // ============================================
    // HELPERS
    // ============================================

    private boolean isDisplayed(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void click(By locator) {
        try {
            driver.findElement(locator).click();
            Thread.sleep(450);
        } catch (Exception e) {
            logger.warn("Loi click " + locator + ": " + e.getMessage());
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        }
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ============================================
    // DRAWER MENU VERIFICATION
    // ============================================

    public boolean isDrawerOpen() {
        return isDisplayed(APP_NAME) && isDisplayed(EXIT_APP_ITEM);
    }

    public boolean isAppNameDisplayed() {
        return isDisplayed(APP_NAME);
    }

    public boolean isAppTaglineDisplayed() {
        return isDisplayed(APP_TAGLINE);
    }

    public boolean isAllMenuItemsDisplayed() {
        return isDisplayed(EQUALIZER_ITEM)
                && isDisplayed(DOWNLOADED_ITEM)
                && isDisplayed(SLEEP_TIMER_ITEM)
                && isDisplayed(PRIVACY_POLICY_ITEM)
                && isDisplayed(RATE_US_ITEM)
                && isDisplayed(SHARE_APP_ITEM)
                && isDisplayed(SETTINGS_ITEM)
                && isDisplayed(VERSION_ITEM)
                && isDisplayed(EXIT_APP_ITEM);
    }

    public String getVersionText() {
        try {
            List<WebElement> els = driver.findElements(VERSION_ITEM);
            if (els.isEmpty()) return null;
            return els.get(0).getAttribute("content-desc");
        } catch (Exception e) {
            return null;
        }
    }

    // ============================================
    // DRAWER MENU - CLICK ITEMS
    // ============================================

    public void clickEqualizer() { logger.info("Click Equalizer"); click(EQUALIZER_ITEM); sleep(1500); }
    public void clickDownloaded() { logger.info("Click Downloaded"); click(DOWNLOADED_ITEM); sleep(1200); }
    public void clickSleepTimer() { logger.info("Click Sleep timer"); click(SLEEP_TIMER_ITEM); sleep(900); }
    public void clickPrivacyPolicy() { logger.info("Click Privacy policy"); click(PRIVACY_POLICY_ITEM); sleep(1500); }
    public void clickRateUs() { logger.info("Click Rate us"); click(RATE_US_ITEM); sleep(1500); }
    public void clickShareApp() { logger.info("Click Share app"); click(SHARE_APP_ITEM); sleep(1500); }
    public void clickSettings() { logger.info("Click Settings"); click(SETTINGS_ITEM); sleep(1200); }
    public void clickVersion() { logger.info("Click Version"); click(VERSION_ITEM); sleep(900); }
    public void clickExitApp() { logger.info("Click Exit app"); click(EXIT_APP_ITEM); sleep(1000); }

    /**
     * Dong drawer menu. QUAN TRONG: KHONG dung device BACK — BACK o drawer (top-level)
     * chi BUNG dialog "Are you sure you want to exit?" chu KHONG dong drawer. Drawer truot
     * tu ben TRAI nen dong bang cach VUOT phai -> trai (giong HomePage.closeMenuDrawer da
     * pass o TC_HOME_15). Diem bat dau o lung ben phai (~70% chieu rong, khong sat mep),
     * keo sang sat mep trai de day drawer ra khoi man.
     */
    public void closeDrawer() {
        // KHONG BACK (BACK o drawer chi bung exit dialog). Tin cay nhat: TAP LAI hamburger
        // goc tren-trai -> toggle dong drawer; fallback vuot phai->trai. Verify isDrawerOpen.
        for (int attempt = 0; attempt < 4; attempt++) {
            if (!isDrawerOpen()) {
                if (attempt > 0) logger.info("Drawer da dong sau " + attempt + " lan thu");
                return;
            }
            try {
                Dimension size = driver.manage().window().getSize();
                int w = size.getWidth();
                int h = size.getHeight();
                if (attempt % 2 == 0) {
                    int hx = (int) (w * 0.037);
                    int hy = (int) (h * 0.053);
                    driver.executeScript("mobile: clickGesture",
                            java.util.Map.of("x", hx, "y", hy));
                    logger.info("Tap lai hamburger (" + hx + "," + hy + ") de toggle dong drawer");
                } else {
                    driver.executeScript("mobile: dragGesture", java.util.Map.of(
                            "startX", (int) (w * 0.70), "startY", (int) (h * 0.50),
                            "endX", (int) (w * 0.02), "endY", (int) (h * 0.50),
                            "speed", 1500));
                    logger.info("Vuot phai->trai de dong drawer");
                }
            } catch (Exception e) {
                logger.warn("closeDrawer attempt " + attempt + " loi: " + e.getMessage());
            }
            sleep(600);
        }
        if (isDrawerOpen()) logger.warn("closeDrawer: drawer VAN MO sau 4 lan thu");
    }

    // ============================================
    // SLEEP TIMER DIALOG - STATE CHECKS
    // ============================================

    public boolean isSleepTimerDialogOpen() {
        return isDisplayed(TIMER_CANCEL_BTN)
                && (isDisplayed(TIMER_SET_BTN) || isDisplayed(TIMER_RESET_BTN));
    }

    public boolean isInInitialState() {
        return isDisplayed(TIMER_SET_BTN) && !isDisplayed(TIMER_COUNTDOWN);
    }

    public boolean isInActiveState() {
        return isDisplayed(TIMER_RESET_BTN) && isDisplayed(TIMER_COUNTDOWN);
    }

    public boolean isSetTimerButtonVisible() {
        return isDisplayed(TIMER_SET_BTN);
    }

    public boolean isResetButtonVisible() {
        return isDisplayed(TIMER_RESET_BTN);
    }

    public boolean isTimerCountdownVisible() {
        return isDisplayed(TIMER_COUNTDOWN);
    }

    public String getTimerCountdownText() {
        try {
            List<WebElement> els = driver.findElements(TIMER_COUNTDOWN);
            if (els.isEmpty()) return null;
            return els.get(0).getAttribute("content-desc");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAllTimerOptionsDisplayed() {
        return isDisplayed(TIMER_15_MIN)
                && isDisplayed(TIMER_30_MIN)
                && isDisplayed(TIMER_45_MIN)
                && isDisplayed(TIMER_60_MIN)
                && isDisplayed(TIMER_90_MIN)
                && isDisplayed(TIMER_CUSTOM);
    }

    public boolean isCustomOptionDisplayed() {
        return isDisplayed(TIMER_CUSTOM);
    }

    // ============================================
    // SLEEP TIMER DIALOG - ACTIONS
    // ============================================

    public void selectTimer15Min() { logger.info("Select 15 mins"); click(TIMER_15_MIN); }
    public void selectTimer30Min() { logger.info("Select 30 mins"); click(TIMER_30_MIN); }
    public void selectTimer45Min() { logger.info("Select 45 mins"); click(TIMER_45_MIN); }
    public void selectTimer60Min() { logger.info("Select 60 mins"); click(TIMER_60_MIN); }
    public void selectTimer90Min() { logger.info("Select 90 mins"); click(TIMER_90_MIN); }

    /**
     * Click "Custom" -> mo Custom Sleep Timer dialog (sub-dialog).
     */
    public void selectTimerCustom() {
        logger.info("Click Custom -> mo Custom dialog");
        click(TIMER_CUSTOM);
        sleep(1200);
    }

    public void clickSetTimer() {
        logger.info("Click Set timer (initial -> closes dialog, timer started)");
        click(TIMER_SET_BTN);
        sleep(1000);
    }

    public void clickReset() {
        logger.info("Click Reset (active -> initial, dialog STAYS open)");
        click(TIMER_RESET_BTN);
        sleep(1000);
    }

    public void clickTimerCancel() {
        logger.info("Click Cancel in Sleep timer dialog -> closes dialog");
        click(TIMER_CANCEL_BTN);
        sleep(700);
    }

    // ============================================
    // CUSTOM SLEEP TIMER DIALOG
    // ============================================

    /**
     * Custom dialog dang mo:
     *  - Co title "Custom sleep timer"
     *  - Co Done button
     */
    public boolean isCustomDialogOpen() {
        return isDisplayed(CUSTOM_DIALOG_TITLE) && isDisplayed(CUSTOM_DONE_BTN);
    }

    public boolean isCustomMinutesLabelVisible() {
        return isDisplayed(CUSTOM_MINUTES_LABEL);
    }

    public boolean isCustomMinutesInputVisible() {
        return isDisplayed(CUSTOM_MINUTES_INPUT);
    }

    public boolean isCustomDoneButtonVisible() {
        return isDisplayed(CUSTOM_DONE_BTN);
    }

    /**
     * Nhap so phut vao input cua Custom dialog.
     * VD: enterCustomMinutes("25") -> set timer 25 phut.
     */
    public void enterCustomMinutes(String minutes) {
        logger.info("Nhap custom minutes: " + minutes);
        try {
            WebElement input = driver.findElement(CUSTOM_MINUTES_INPUT);
            String current = input.getText();
            if (current != null && !current.isEmpty()) {
                input.clear();
                sleep(300);
            }
            input.sendKeys(minutes);
            sleep(500);
        } catch (Exception e) {
            logger.warn("Loi nhap custom minutes: " + e.getMessage());
        }
    }

    public String getCustomMinutesValue() {
        try {
            return driver.findElement(CUSTOM_MINUTES_INPUT).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Click Done -> set timer voi value da nhap, ca 2 dialog (Custom + Sleep timer) DONG.
     */
    public void clickCustomDone() {
        logger.info("Click Done in Custom dialog (set custom timer)");
        // KHONG hideKeyboard truoc: tren UiAutomator2, hideKeyboard thuong gui BACK ->
        // BACK DONG Custom dialog ve dialog cha (KHONG commit value) -> nut Done bien mat ->
        // click truot -> timer khong set. Nut Done HIEN NGAY TREN ban phim nen click thang
        // duoc (da kiem chung: Done voi keyboard dang hien -> set timer + dong ca 2 dialog ve Home).
        click(CUSTOM_DONE_BTN);
        sleep(1500);
    }

    /**
     * Click Cancel trong Custom dialog -> dong Custom, ve Sleep timer dialog (initial).
     * KHONG anh huong timer dang chay (neu co).
     *
     * Luu y: dung locator Cancel chung. Khi Custom mo, chi co 1 Cancel (cua Custom).
     */
    public void clickCustomCancel() {
        logger.info("Click Cancel in Custom dialog (-> back to Sleep timer dialog)");
        // KHONG hideKeyboard (gui BACK lam dong dialog ngoai y muon). Nut Cancel hien tren
        // ban phim -> click thang.
        click(TIMER_CANCEL_BTN);
        sleep(1000);
    }

    // ============================================
    // EXIT CONFIRMATION DIALOG
    // ============================================

    public boolean isExitDialogOpen() {
        return isDisplayed(EXIT_DIALOG_TITLE)
                && isDisplayed(EXIT_DIALOG_EXIT_BTN)
                && isDisplayed(EXIT_DIALOG_CANCEL_BTN);
    }

    public void clickExitCancel() {
        logger.info("Click Cancel trong Exit dialog");
        click(EXIT_DIALOG_CANCEL_BTN);
        sleep(1000);
    }

    public void clickExitConfirm() {
        logger.warn("Click Exit - APP SE DONG");
        click(EXIT_DIALOG_EXIT_BTN);
    }
}