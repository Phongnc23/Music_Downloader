package helpers;

import constants.AppConstants;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-detect va dong dialog xuat hien sau khi tat quang cao.
 *
 * Pattern dac biet cua Music Downloader app:
 *  - Dialog Update co title content-desc="Update app"
 *  - Co 2 button: CLOSE (uppercase content-desc) va UPDATE
 *  - KHONG dung AlertDialog class, ma la android.view.View long nhau
 *  - Co overlay content-desc="Dismiss" full screen (CANH BAO: KHONG tap vao day)
 *
 * Cach dung: {@code new DialogHelper(driver).dismissDialog();}
 */
public class DialogHelper {

    private static final long INITIAL_WAIT_MS = 4_000;
    private static final long POLL_INTERVAL_MS = 500;
    private static final long ACTION_DELAY_MS = 1_000;

    private final AndroidDriver driver;
    private final String appPackage;

    public DialogHelper(AndroidDriver driver) {
        this.driver = driver;
        this.appPackage = AppConstants.APP_PACKAGE;
    }

    /**
     * Cho dialog xuat hien (toi da INITIAL_WAIT_MS), neu co thi dong.
     */
    public void dismissDialog() {
        if (!waitForDialog(INITIAL_WAIT_MS)) {
            System.out.println("✅ No dialog detected");
            return;
        }

        System.out.println("📣 Dialog detected - trying to close");

        if (clickFirst(dialogCloseLocators())) {
            sleep(ACTION_DELAY_MS);
            // Verify dialog da dong
            if (!isDialogShowing()) {
                System.out.println("📕 Dialog closed successfully");
            } else {
                System.out.println("⚠ Dialog still showing - retry once");
                clickFirst(dialogCloseLocators());
                sleep(ACTION_DELAY_MS);
            }
        } else {
            System.out.println("⚠ Could not find close locator for this dialog");
        }
    }

    private boolean waitForDialog(long waitMs) {
        long deadline = System.currentTimeMillis() + waitMs;
        while (true) {
            if (isDialogShowing()) return true;
            if (System.currentTimeMillis() >= deadline) return false;
            sleep(POLL_INTERVAL_MS);
        }
    }

    /**
     * Detect dialog dang hien.
     * Cover nhieu pattern: AlertDialog chuan, custom dialog dung content-desc,
     * dialog co cap nut CLOSE+UPDATE (Update dialog cua Music Downloader).
     */
    private boolean isDialogShowing() {
        // ====== Music Downloader Update dialog pattern ======
        if (isPresent(AppiumBy.accessibilityId("Update app"))) return true;
        if (isPresent(AppiumBy.accessibilityId("UPDATE"))) return true;

        // ====== Cap button CLOSE + UPDATE/OK chung -> day la dialog ======
        boolean hasNegativeButton = isPresent(AppiumBy.accessibilityId("CLOSE"))
                || isPresent(AppiumBy.accessibilityId("Close"))
                || isPresent(AppiumBy.accessibilityId("Later"))
                || isPresent(AppiumBy.accessibilityId("Cancel"));
        boolean hasPositiveButton = isPresent(AppiumBy.accessibilityId("UPDATE"))
                || isPresent(AppiumBy.accessibilityId("Update"))
                || isPresent(AppiumBy.accessibilityId("OK"))
                || isPresent(AppiumBy.accessibilityId("Allow"));
        if (hasNegativeButton && hasPositiveButton) return true;

        // ====== Android system AlertDialog ======
        if (isPresent(AppiumBy.id("android:id/alertTitle"))) return true;
        if (isPresent(AppiumBy.id("android:id/button1"))) return true;

        // ====== Generic dialog class ======
        if (isPresent(AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.app.AlertDialog\")"))) return true;
        if (isPresent(AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"androidx.appcompat.app.AlertDialog\")"))) return true;

        // ====== Resource-id chua dialog/popup/modal ======
        if (isPresent(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"(?i).*(dialog|popup|modal|prompt).*\")"))) return true;

        return false;
    }

    /**
     * Locator cho nut dong dialog.
     * Thu tu uu tien: nut AN TOAN (close/cancel/later) TRUOC, nut confirm (OK/Update) SAU CUNG.
     * QUAN TRONG: KHONG click vao "Dismiss" overlay (View toan man hinh) vi se tap vao dialog content.
     */
    private List<By> dialogCloseLocators() {
        List<By> list = new ArrayList<>();

        // ====== Music Downloader Update dialog - UPPERCASE content-desc (UU TIEN CAO NHAT) ======
        list.add(AppiumBy.accessibilityId("CLOSE"));
        list.add(AppiumBy.accessibilityId("CANCEL"));

        // ====== Content-desc cho close button (multi-case, multi-lang) ======
        for (String desc : new String[]{
                "Close", "close",
                "Cancel", "cancel",
                "Đóng", "Hủy", "Bỏ qua",
                "Later", "Maybe Later", "Not Now",
                "Cerrar", "Fermer", "Schließen", "閉じる", "닫기", "关闭", "關閉"}) {
            list.add(AppiumBy.accessibilityId(desc));
        }

        // ====== Button co text close-related ======
        for (String text : new String[]{
                "CLOSE", "Close", "CANCEL", "Cancel", "DISMISS", "Dismiss",
                "Skip", "SKIP", "Later", "Maybe later", "Maybe Later",
                "Not now", "Not Now", "No thanks", "No Thanks", "No, thanks",
                "Hủy", "Đóng", "Bỏ qua", "Để sau", "Không, cảm ơn", "Không phải bây giờ",
                "Cancelar", "Cerrar", "Más tarde",
                "Annuler", "Fermer", "Plus tard",
                "Abbrechen", "Schließen", "Später"}) {
            list.add(textButton(text));
            list.add(textTextView(text));
        }

        // ====== Standard Android dialog button (negative/neutral) ======
        // KHONG dung button1 (positive) vi co the la "Update", "Allow", "Buy"
        list.add(AppiumBy.id("android:id/button2"));
        list.add(AppiumBy.id("android:id/button3"));

        // ====== App-specific resource-id (them khi inspect duoc) ======
        for (String id : new String[]{
                // ⬇ Thêm resource-id của dialog trong app vào đây ⬇

                // ⬆ Thêm resource-id của dialog trong app vào đây ⬆
        }) {
            list.add(AppiumBy.id(appPackage + ":id/" + id));
        }

        // ====== Nut "dong y" - thu sau cung vi co the confirm action ngoai y muon ======
        // Chi dung khi dialog welcome/onboarding chi co 1 nut
        for (String text : new String[]{
                "OK", "Ok", "Got it", "GOT IT", "Continue",
                "Đồng ý", "Tiếp tục", "Đã hiểu"}) {
            list.add(textButton(text));
            list.add(textTextView(text));
        }

        return list;
    }

    // ====== Helpers ======

    private By textButton(String text) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.Button\").text(\"" + text + "\")");
    }

    private By textTextView(String text) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.TextView\").text(\"" + text + "\")");
    }

    private boolean clickFirst(List<By> locators) {
        for (By locator : locators) {
            if (clickIfPresent(locator)) return true;
        }
        return false;
    }

    private boolean isPresent(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement el : elements) {
                if (el.isDisplayed()) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean clickIfPresent(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            if (elements.isEmpty()) return false;
            WebElement el = elements.get(0);
            if (!el.isDisplayed()) return false;
            el.click();
            System.out.println("📕 Closed dialog via: " + locator);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}