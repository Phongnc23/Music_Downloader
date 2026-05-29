package helpers;

import io.appium.java_client.android.AndroidDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler don gian tap trung 2 pattern cua Music Downloader:
 *
 *   - Video NGAN (7-8s): chi co Close button -> tap Close
 *   - Video DAI (12s): co Skip button -> tap Skip -> sau do co Close -> tap Close
 *                       Worst case: doi 60s thi tu nhien co Close
 *
 * Logic:
 *   1. Poll page source moi 1s
 *   2. Quet XML tim text="Đóng" / text="Close" / text="Skip" / text="Bỏ qua"
 *   3. Lay bounds thuc te (vi tri ngau nhien trai/phai)
 *   4. Tap bang toa do (bypass clickable=false)
 *   5. Verify ad da bien
 *
 * Yeu cau: AndroidDriver da connect, app dang foreground.
 */
public class ScreenObserverAdHandler {

    private final AndroidDriver driver;

    // Tu khoa tim Close button (thu lan luot)
    private static final String[] CLOSE_KEYWORDS = {
            "Đóng", "ĐÓNG", "đóng",
            "Close", "CLOSE", "close",
            "X", "✕", "×"
    };

    // Tu khoa tim Skip button
    private static final String[] SKIP_KEYWORDS = {
            "Bỏ qua", "BỎ QUA",
            "Skip", "SKIP", "Skip Ad", "Skip ad"
    };

    // Thoi gian (ms)
    private static final long MAX_TOTAL_WAIT = 75_000;   // 75s du worst case 60s + buffer
    private static final long POLL_INTERVAL = 1_000;     // poll moi 1s
    private static final long VERIFY_DELAY = 2_000;      // doi 2s sau tap roi verify

    // Regex parse bounds dang [x1,y1][x2,y2]
    private static final Pattern BOUNDS_PATTERN =
            Pattern.compile("bounds=\"\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]\"");

    public ScreenObserverAdHandler(AndroidDriver driver) {
        this.driver = driver;
        applyFastSettings();
    }

    /**
     * Apply settings de UIAutomator2 khong cho UI idle.
     * Day la WORKAROUND quan trong: video ad lam UI khong bao gio idle,
     * neu khong set thi moi cau lenh cho 10s default.
     */
    private void applyFastSettings() {
        try {
            // Dung string thay vi Setting enum - tuong thich moi version
            driver.setSetting("waitForIdleTimeout", 100);
            driver.setSetting("waitForSelectorTimeout", 100);
            driver.setSetting("actionAcknowledgmentTimeout", 100);
            driver.setSetting("keyInjectionDelay", 0);
            log("Applied fast settings (waitForIdleTimeout=100ms)");
        } catch (Exception e) {
            log("WARN: Cannot apply fast settings: " + e.getMessage());
        }
    }

    // ====== PUBLIC API ======

    /**
     * Tat quang cao. Tra ve true neu thanh cong.
     */
    public boolean dismissAd() {
        log("========== START AD DISMISS ==========");

        // CHO quang cao xuat hien (toi da 12s)
        log("Waiting for ad to appear (up to 12s)...");
        long detectionDeadline = System.currentTimeMillis() + 12_000;
        boolean adFound = false;

        while (System.currentTimeMillis() < detectionDeadline) {
            String src = getPageSourceSafe();
            if (src != null && hasAd(src)) {
                long detectSec = (System.currentTimeMillis() - (detectionDeadline - 12_000)) / 1000;
                log("Ad APPEARED at " + detectSec + "s");
                adFound = true;
                break;
            }
            sleep(500);
        }

        if (!adFound) {
            log("No ad appeared after 12s. App da o MainActivity hoac quang cao bi disable.");
            return true;
        }

        // ... phan loop xu ly button giu nguyen nhu cu
        long startTime = System.currentTimeMillis();
        boolean skipTapped = false;
        int pollCount = 0;

        while (System.currentTimeMillis() - startTime < MAX_TOTAL_WAIT) {
            // ... giu nguyen logic cu
        }

        log("⚠ TIMEOUT after " + (MAX_TOTAL_WAIT / 1000) + "s. Ad not dismissed.");
        return false;
    }

    // ====== PRIVATE HELPERS ======

    /**
     * Check XML co quang cao khong.
     * Detect WebView + dau hieu interstitial slot cua app nay.
     */
    private boolean hasAd(String source) {
        if (source == null) return false;
        return source.contains("android.webkit.WebView")
                && (source.contains("app-interstitial-slot")
                || source.contains("interstitial")
                || source.contains("mys-creative")
                || source.contains("mys-wrapper"));
    }

    /**
     * Tim button theo danh sach keyword. Tra ve toa do tam {cx, cy} hoac null.
     * Scan XML tim element co text="keyword" hoac content-desc="keyword".
     */
    private int[] findButton(String source, String[] keywords, String label) {
        for (String keyword : keywords) {
            // Thu tim qua attribute text=
            int[] coords = findElementByAttribute(source, "text", keyword);
            if (coords != null) {
                log("Match by text='" + keyword + "' for " + label);
                return coords;
            }
            // Thu tim qua attribute content-desc=
            coords = findElementByAttribute(source, "content-desc", keyword);
            if (coords != null) {
                log("Match by content-desc='" + keyword + "' for " + label);
                return coords;
            }
        }
        return null;
    }

    /**
     * Scan XML, tim element opening tag co {attr}="{value}",
     * extract bounds tu cung element, tra ve toa do tam.
     */
    private int[] findElementByAttribute(String source, String attr, String value) {
        // Escape "&" trong value neu can
        String marker = attr + "=\"" + value + "\"";

        int searchFrom = 0;
        while (true) {
            int idx = source.indexOf(marker, searchFrom);
            if (idx == -1) return null;

            // Tim element tag chua marker nay
            int tagStart = source.lastIndexOf("<", idx);
            int tagEnd = source.indexOf(">", idx);
            if (tagStart == -1 || tagEnd == -1) {
                searchFrom = idx + marker.length();
                continue;
            }

            String tag = source.substring(tagStart, tagEnd + 1);

            // Parse bounds
            Matcher m = BOUNDS_PATTERN.matcher(tag);
            if (m.find()) {
                int x1 = Integer.parseInt(m.group(1));
                int y1 = Integer.parseInt(m.group(2));
                int x2 = Integer.parseInt(m.group(3));
                int y2 = Integer.parseInt(m.group(4));

                // Bounds phai hop ly
                if (x2 > x1 && y2 > y1) {
                    return new int[]{(x1 + x2) / 2, (y1 + y2) / 2};
                }
            }

            searchFrom = tagEnd + 1;
        }
    }

    /**
     * Tap bang mobile: clickGesture - bypass clickable=false.
     */
    private void tapAt(int x, int y) {
        try {
            driver.executeScript("mobile: clickGesture",
                    Map.of("x", x, "y", y));
        } catch (Exception e) {
            log("tapAt error: " + e.getMessage());
        }
    }

    /**
     * Tap va log vi tri (de debug random trai/phai).
     */
    private void tapAndLog(int[] center, String label, long elapsedSec) {
        int x = center[0];
        int y = center[1];
        String position = (x < 860) ? "LEFT" : "RIGHT";  // 860 = nua chieu rong 1720
        log(">>> TAP " + label + " at (" + x + "," + y + ") [" + position + "] "
                + "at " + elapsedSec + "s");
        tapAt(x, y);
    }

    private String getPageSourceSafe() {
        try {
            return driver.getPageSource();
        } catch (Exception e) {
            log("getPageSource error: " + e.getMessage());
            return null;
        }
    }

    private void log(String msg) {
        System.out.println("[ScreenObserver] " + msg);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}