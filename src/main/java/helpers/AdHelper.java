package helpers;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import constants.AppConstants;


public class AdHelper {

    /**
     * Cửa sổ chờ ad launch xuất hiện (detect theo currentActivity). Ad thường hiện
     * ~6-10s sau khi mở app; để 15s cho dư margin (mạng chậm / SDK load lâu) — nếu
     * ngắn quá (vd 7s) ad hiện ngay sau khi cửa sổ đóng → "No ad detected" rồi test
     * assert trúng ad → FAIL. App này luôn có ad nên detect xong là thoát ngay,
     * không phải chờ đủ 15s.
     */
    private static final long INITIAL_LOAD_WAIT_MS = 15_000;
    private static final long LOAD_POLL_INTERVAL_MS = 700;
    /** Ad video có countdown — chờ Skip enable tối đa bấy nhiêu. */
    private static final long SKIP_COUNTDOWN_TIMEOUT_MS = 25_000;
    private static final long SKIP_POLL_INTERVAL_MS = 800;
    /** Lặp xử lý ad chồng ad (multi-tier — hỗ trợ tới ~12 layers). */
    private static final int MAX_DISMISS_ROUNDS = 12;
    /** Delay giữa các vòng (cho ad layer tiếp theo render). */
    private static final long ROUND_DELAY_MS = 1_500;
    /** Sau khi đóng 1 layer, chờ tối đa bấy nhiêu cho ad layer tiếp theo xuất hiện. */
    private static final long NEXT_LAYER_WAIT_MS = 4_000;

    private final AndroidDriver driver;
    private final String appPackage;

    public AdHelper(AndroidDriver driver) {
        this.driver = driver;
        this.appPackage = AppConstants.APP_PACKAGE;
    }

    public enum AdType {
        NONE,
        VIDEO_SKIP_COUNTDOWN,
        GOOGLE_AD,
        FACEBOOK_AD,
        UNITY_AD,
        APPLOVIN_AD,
        IRONSOURCE_AD,
        VUNGLE_AD,
        MINTEGRAL_AD,
        PANGLE_AD,
        SMARTDIGIMKT_AD,
        APP_POPUP,
        UNKNOWN
    }

    /**
     * Đảm bảo app foreground là app đang test. Quảng cáo có thể redirect sang
     * Play Store / Chrome / Samsung Internet / etc. khi user (hoặc auto-redirect)
     * click vào ad. Method này:
     *
     * <ol>
     *   <li>Check {@code currentPackage} — nếu == app package → OK, return true</li>
     *   <li>Thử BACK 2 lần (ít destructive)</li>
     *   <li>Force-stop wrong app + start lại app via Activity intent</li>
     * </ol>
     *
     * @return true nếu cuối cùng đã về app, false nếu vẫn không recover được
     */
    public boolean ensureInOurApp() {
        try {
            String currentPkg = driver.getCurrentPackage();
            if (appPackage != null && appPackage.equals(currentPkg)) return true;

            System.out.println("⚠ Wrong package: " + currentPkg + " (expected: " + appPackage + ") → recovering");

            // Step 1: BACK 2 lần (đa số case Play Store/Chrome ra được)
            for (int i = 0; i < 2; i++) {
                try {
                    driver.pressKey(new KeyEvent(AndroidKey.BACK));
                    sleep(800);
                    if (appPackage.equals(driver.getCurrentPackage())) {
                        System.out.println("✅ Back to app via BACK (attempt " + (i + 1) + ")");
                        return true;
                    }
                } catch (Exception ignored) {}
            }

            // Step 2: force-stop wrong app + start app
            try {
                if (currentPkg != null && !currentPkg.equals(appPackage)) {
                    driver.terminateApp(currentPkg);
                }
            } catch (Exception ignored) {}
            try {
                String activity = AppConstants.APP_ACTIVITY;
                if (activity != null) {
                    driver.executeScript("mobile: startActivity", java.util.Map.of("component", appPackage + "/" + activity, "wait", true));
                    sleep(2000);
                    boolean back = appPackage.equals(driver.getCurrentPackage());
                    System.out.println(back ? "✅ Restored app via startActivity" : "⚠ Restart attempted but still not in app");
                    return back;
                }
            } catch (Exception e) {
                System.out.println("⚠ ensureInOurApp restart failed: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("⚠ ensureInOurApp error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Entry point duy nhất — phiên bản CHO APP FLUTTER.
     *
     * <p>App này là Flutter và ad (Google interstitial) render trong WebView đè lên
     * canvas Flutter. Hệ quả thực nghiệm:
     * <ul>
     *   <li>UI Flutter/ad KHÔNG nằm trong accessibility tree → mọi locator
     *       ({@code findElements}) đều không thấy gì.</li>
     *   <li>Ad WebView THƯỜNG làm CRASH tiến trình UiAutomator2 instrumentation. Sau
     *       crash, mọi lệnh đi qua instrumentation ({@code findElements}, {@code getWindowSize},
     *       {@code pressKey}, {@code clickGesture}) đều fail HOẶC TREO lâu.</li>
     * </ul>
     *
     * <p>Vì vậy luồng này CHỈ dùng lệnh cấp ADB (sống sót qua instrumentation crash):
     * phát hiện ad bằng {@code currentActivity} và bypass bằng cách restart
     * MainActivity ({@code mobile: startActivity}). KHÔNG đụng tới findElements /
     * toạ độ / BACK — những thứ gây "lúc được lúc không" và treo trước đây.
     */
    public void dismissAllAds() {
        // Đảm bảo đang ở app (recover nếu ad đã redirect ra Play Store/Chrome).
        // Lúc này app vừa launch, instrumentation còn sống nên an toàn.
        ensureInOurApp();

        // Chờ ad đầu tiên xuất hiện — CHỈ theo currentActivity (ADB), không findElements.
        long deadline = System.currentTimeMillis() + INITIAL_LOAD_WAIT_MS;
        AdType type = AdType.NONE;
        while (System.currentTimeMillis() < deadline) {
            type = detectByCurrentActivity();
            if (type != AdType.NONE) break;
            sleep(LOAD_POLL_INTERVAL_MS);
        }

        if (type == AdType.NONE) {
            System.out.println("✅ No ad detected (activity-based)");
            return;
        }

        System.out.println("🔎 Detected ad activity: " + type);
        // Đóng ad IN-PLACE bằng ADB tap (idle-safe). Lý do thực nghiệm trên Oppo Pad Neo:
        // home là canvas Flutter KHÔNG BAO GIỜ idle → "adb uiautomator dump" báo
        // "could not get idle state" và findElements (qua instrumentation) lúc được lúc
        // không. ADB "input tap" đi thẳng qua adb nên không cần idle → tin cậy. Tap đóng
        // ad giữ home đã load sẵn (KHÔNG restart — restart đẩy app về splash khiến dialog
        // Update hiện SAU khi DialogHelper đã chạy → home bị dialog che → test fail).
        if (!closeAdInPlaceByAdbTap(45_000)) {
            System.out.println("⚠ Không đóng được ad in-place → last resort restart");
            restartToMainActivity();
        }
    }

    /**
     * Đóng ad full-screen bằng ADB {@code input tap} vào nút Close — KHÔNG qua Appium
     * findElements. Google interstitial của app này đặt nút Close (content-desc="Close")
     * ở GÓC TRÊN-PHẢI (đo thực tế ~0.945w, 0.039h trên Oppo Pad Neo 1720×2408). Tap góc
     * phải TRƯỚC vì phần còn lại của ad là vùng click-to-install → tap nhầm sẽ redirect
     * Play Store. Sau mỗi tap, nếu văng ra app khác thì BACK (ADB) kéo về.
     *
     * @return true nếu đã rời ad activity (giữ nguyên màn, home lộ ra); false nếu hết giờ
     */
    private boolean closeAdInPlaceByAdbTap(long maxWaitMs) {
        if (adbScreenSize() == null) {
            System.out.println("⚠ Không gọi được ADB (wm size) → không đóng ad được");
            return false;
        }
        System.out.println("🎯 Đóng ad IN-PLACE: dump tìm nút Close + ADB BACK (an toàn, không click content → không redirect Play Store)");
        long deadline = System.currentTimeMillis() + maxWaitMs;
        while (System.currentTimeMillis() < deadline) {
            // (0) RECOVER nếu bị redirect ra app khác (Play Store / game do ad mở) hoặc launcher.
            //     Check TRƯỚC khi kết luận "vào home" — vì rời AdActivity sang app khác cũng làm
            //     detectByCurrentActivity()==NONE (dễ nhầm là đã về home).
            String pkg = currentPackageSafe();
            if (pkg != null && !pkg.isEmpty() && !pkg.equals(appPackage)) {
                System.out.println("⚠ Foreground = " + pkg + " (ad redirect / không phải app) → BACK rồi startActivity kéo về");
                adbBack();
                sleep(800);
                if (!appPackage.equals(currentPackageSafe())) startMainActivity();
                sleep(1200);
                continue;
            }
            // (1) Đã về HOME THẬT — đang ở ĐÚNG app mình VÀ không còn AdActivity.
            if (detectByCurrentActivity() == AdType.NONE) {
                System.out.println("✅ Đã đóng ad, vào HOME (giữ state, không restart)");
                return true;
            }

            // (2) Nếu creative CÓ expose nút Close (content-desc) → tap CHÍNH XÁC center của nó
            //     (an toàn tuyệt đối, không trúng content). Thường phải chờ countdown mới render
            //     nên có thể null ở các vòng đầu → khi đó dùng BACK ở (3).
            int[] close = findCloseButtonCenterViaDump();
            if (close != null) {
                adbTap(close[0], close[1]);
                sleep(900);
                continue;  // quay lại (0)/(1) xác nhận
            }

            // (3) Creative KHÔNG expose nút đóng (WebView rỗng) → dùng ADB BACK để đóng.
            //     BACK đóng Google interstitial SAU khi countdown xong (trước đó ad lờ BACK),
            //     và KHÔNG click vào content → KHÔNG bị redirect Play Store/game (khác hẳn tap
            //     toạ độ mù trước đây hay trúng vùng install). Thực nghiệm: ~6 BACK (~15-18s).
            adbBack();
            sleep(2_000);  // chờ countdown tick + ad xử lý BACK rồi kiểm tra lại
        }
        System.out.println("⚠ Không đóng được ad in-place trong " + (maxWaitMs / 1000) + "s");
        // Chỉ coi là thành công khi ĐANG Ở ĐÚNG APP MÌNH và không còn ad (tránh false-positive
        // khi đang kẹt ở app khác do ad redirect).
        return appPackage.equals(currentPackageSafe()) && detectByCurrentActivity() == AdType.NONE;
    }

    /**
     * Có đang bị ad full-screen che, HOẶC bị ad đẩy văng sang app khác (Play Store/game)?
     * Chỉ dùng lệnh ADB + currentActivity (nhanh, idle-safe) nên gọi thường xuyên được.
     */
    public boolean isAdShowing() {
        if (detectByCurrentActivity() != AdType.NONE) return true;
        String pkg = currentPackageSafe();
        return pkg != null && !pkg.isEmpty() && !pkg.equals(appPackage);
    }

    /**
     * AD-GUARD cho thao tác GIỮA PHIÊN. Gọi NGAY SAU mỗi thao tác (tap/swipe/back…) trong
     * test. App có thể bung ad bất chợt sau ~40s + thao tác — CÓ HOẶC KHÔNG. Method này:
     * <ul>
     *   <li>Chờ tối đa {@code appearWindowMs} xem ad có bung lên không.</li>
     *   <li>Nếu CÓ → bypass bằng {@link #closeAdInPlaceByAdbTap} (đóng bằng ADB BACK, GIỮ
     *       NGUYÊN màn đang dở — KHÔNG restart) → test tiếp tục từ ĐÚNG chỗ trước khi có ad.</li>
     *   <li>Nếu KHÔNG bung trong cửa sổ đó → trả về false ngay, test chạy tiếp bình thường.</li>
     * </ul>
     *
     * <p>Vì đóng ad theo kiểu giữ-state (BACK), sau khi xử lý xong màn hình vẫn ở nguyên
     * trạng thái trước khi ad đè lên → "chạy tiếp những gì đang chạy" một cách liền mạch.
     *
     * @param appearWindowMs thời gian chờ ad có thể bung sau thao tác (vd 5000-7000ms)
     * @return true nếu đã phát hiện & bypass 1 ad; false nếu không có ad nào bung
     */
    public boolean handleAdIfAppears(long appearWindowMs) {
        long deadline = System.currentTimeMillis() + appearWindowMs;
        while (System.currentTimeMillis() < deadline) {
            if (isAdShowing()) {
                System.out.println("📺 Ad bung giữa phiên → bypass (giữ state) rồi tiếp tục test");
                boolean ok = closeAdInPlaceByAdbTap(45_000);
                System.out.println(ok ? "▶ Đã bypass ad giữa phiên, tiếp tục test"
                        : "⚠ Bypass ad giữa phiên chưa chắc chắn (caller nên kiểm tra lại)");
                return true;
            }
            sleep(500);
        }
        return false;  // không có ad → không làm gì, test chạy tiếp
    }

    /**
     * Dump UI hierarchy qua ADB ({@code uiautomator dump} rồi {@code cat}) và tìm nút
     * đóng ad — trả về center (x,y) để tap qua ADB. Màn ad (WebView Google interstitial)
     * idle nên dump được; home Flutter thì KHÔNG (đó là lý do chỉ dùng cho ad screen).
     *
     * <p>Nhận diện nút Close: node {@code clickable="true"} có content-desc khớp
     * close/skip/dismiss/X (đa ngôn ngữ). Ưu tiên node nằm nửa trên màn hình (Google
     * đặt Close ở góc trên) để tránh chọn nhầm nút "Cài đặt/Install" ở dưới.
     *
     * @return {x,y} center của nút Close, hoặc null nếu chưa dump ra (countdown chưa xong)
     */
    private int[] findCloseButtonCenterViaDump() {
        String xml = adbDumpUi();
        if (xml == null || xml.isEmpty()) return null;
        java.util.regex.Pattern descP = java.util.regex.Pattern.compile("content-desc=\"([^\"]*)\"");
        java.util.regex.Pattern boundsP =
                java.util.regex.Pattern.compile("bounds=\"\\[(\\d+),(\\d+)\\]\\[(\\d+),(\\d+)\\]\"");
        int[] sz = adbScreenSize();
        int screenH = (sz != null) ? sz[1] : Integer.MAX_VALUE;
        int[] topCandidate = null;
        for (String node : xml.split("<node")) {
            if (!node.contains("clickable=\"true\"")) continue;
            java.util.regex.Matcher dm = descP.matcher(node);
            String desc = dm.find() ? dm.group(1) : "";
            if (!desc.matches("(?i)(close|close ad|close advertisement|dismiss|skip|skip ad"
                    + "|đóng|đóng quảng cáo|bỏ qua|x|✕|✖|×)")) continue;
            java.util.regex.Matcher bm = boundsP.matcher(node);
            if (!bm.find()) continue;
            int x1 = Integer.parseInt(bm.group(1)), y1 = Integer.parseInt(bm.group(2));
            int x2 = Integer.parseInt(bm.group(3)), y2 = Integer.parseInt(bm.group(4));
            int cx = (x1 + x2) / 2, cy = (y1 + y2) / 2;
            // Ưu tiên nút ở nửa trên (Close thật). Nút dưới (Install) bỏ qua nếu có nút trên.
            if (cy < screenH / 2) return new int[]{cx, cy};
            if (topCandidate == null) topCandidate = new int[]{cx, cy};
        }
        return topCandidate;
    }

    /** Dump UI ra file trên device rồi cat về (cả 2 đều qua adb shell — idle-safe cho ad). */
    private String adbDumpUi() {
        adbShell("uiautomator", "dump", "/sdcard/adh_dump.xml");
        return adbShell("cat", "/sdcard/adh_dump.xml");
    }

    private AdType waitAndDetectAdType(long waitMs) {
        long deadline = System.currentTimeMillis() + waitMs;
        while (true) {
            AdType type = detectAdType();
            if (type != AdType.NONE) return type;
            if (System.currentTimeMillis() >= deadline) return AdType.NONE;
            sleep(LOAD_POLL_INTERVAL_MS);
        }
    }

    /**
     * Phân loại ad. Ưu tiên type có strategy đặc biệt (video countdown) trước,
     * rồi đến từng SDK theo packageName, cuối cùng UNKNOWN cho dấu hiệu mơ hồ.
     */
    private AdType detectAdType() {
        // Liftoff/Vungle WebView ad — content-desc fingerprint
        if (isPresent(AppiumBy.accessibilityId("Liftoff Privacy"))) {
            return AdType.VUNGLE_AD;
        }
        // SDM reward video ad (smartdigimkt) — nested trong app package, không có activity riêng
        if (isPresent(AppiumBy.id(appPackage + ":id/sdm_reward_root_container"))
                || isPresent(AppiumBy.id(appPackage + ":id/sdm_top_control"))) {
            return AdType.SMARTDIGIMKT_AD;
        }

        // Check current activity TRƯỚC — phát hiện được ad ngay cả khi WebView chưa render
        AdType byActivity = detectByCurrentActivity();
        if (byActivity != AdType.NONE) return byActivity;

        if (isPresent(AppiumBy.androidUIAutomator(
                "new UiSelector().textMatches(\"(?i).*(skip in|skip ad|ad\\\\s*[·:]|quảng cáo|広告|광고|跳过|跳過).*\")"))) {
            return AdType.VIDEO_SKIP_COUNTDOWN;
        }

        if (matchesPackage("com\\.google\\.android\\.gms")
                || hasRawResourceId("dismiss-button", "close-button", "skip-button",
                "abgb", "abgc", "abgcp", "app-interstitial-slot")) {
            return AdType.GOOGLE_AD;
        }
        if (matchesPackage("com\\.facebook\\.ads.*")) return AdType.FACEBOOK_AD;
        if (matchesPackage("com\\.unity3d\\.(ads|services).*")) return AdType.UNITY_AD;
        if (matchesPackage("com\\.applovin.*")) return AdType.APPLOVIN_AD;
        if (matchesPackage("com\\.ironsource.*")) return AdType.IRONSOURCE_AD;
        if (matchesPackage("com\\.vungle.*")) return AdType.VUNGLE_AD;
        if (matchesPackage("com\\.mbridge\\.msdk.*")) return AdType.MINTEGRAL_AD;
        if (matchesPackage("com\\.bytedance\\.sdk.*|com\\.pangle.*")) return AdType.PANGLE_AD;
        if (matchesPackage("com\\.smartdigimkt.*")) return AdType.SMARTDIGIMKT_AD;

        if (isPresent(AppiumBy.id(appPackage + ":id/text_cancel"))) return AdType.APP_POPUP;
        for (String id : new String[]{"btn_close", "btn_cancel", "btn_no"}) {
            if (isPresent(AppiumBy.id(appPackage + ":id/" + id))) return AdType.APP_POPUP;
        }
        // ⚠ KHÔNG detect Button content-desc="Close" generic ở đây — nó match cả
        // banner X, dialog non-blocking, in-app close button của các UI hợp lệ
        // → gây nhầm và phá flow. Chỉ detect ad qua activity/SDK package
        // (full-screen) hoặc resource-id app-specific cho popup.

        if (isPresent(AppiumBy.androidUIAutomator(
                "new UiSelector().descriptionMatches(\"(?i).*(advertisement|sponsored).*\")"))
                || isPresent(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"(?i).*(interstitial|app-interstitial|ad-slot|ad_container).*\")"))) {
            return AdType.UNKNOWN;
        }

        return AdType.NONE;
    }

    private boolean dismissByType(AdType type) {
        switch (type) {
            case VIDEO_SKIP_COUNTDOWN: return dismissVideoSkipAd();
            case GOOGLE_AD:      return bypassAdByRestart(40_000);
            case FACEBOOK_AD:    return clickFirst(facebookCloseLocators())   || dismissAnyCloseButton();
            case UNITY_AD:       return clickFirst(unityCloseLocators())      || dismissAnyCloseButton();
            case APPLOVIN_AD:    return clickFirst(applovinCloseLocators())   || dismissAnyCloseButton();
            case IRONSOURCE_AD:  return clickFirst(ironsourceCloseLocators()) || dismissAnyCloseButton();
            case VUNGLE_AD:      return clickFirst(vungleCloseLocators()) || dismissWebViewAdByCornerTap() || dismissAnyCloseButton();
            case MINTEGRAL_AD:   return clickFirst(mintegralCloseLocators())  || dismissAnyCloseButton();
            case PANGLE_AD:      return clickFirst(pangleCloseLocators())     || dismissAnyCloseButton();
            case SMARTDIGIMKT_AD: return dismissSdmRewardAd()
                    || pollAndClose(smartdigimktCloseLocators(), 15_000);
            case APP_POPUP:      return pollClickFirst(appPopupCloseLocators(), 15_000);
            // App Flutter: ad lạ/không rõ SDK cũng render WebView/activity → locator vô dụng
            // và dismissAnyCloseButton (quét findElements) sẽ TREO vì chờ implicit-wait qua
            // hàng chục locator không tồn tại. Dùng poll-restart (ADB-level) cho chắc & không treo.
            case UNKNOWN:        return bypassAdByRestart(40_000);
            default: return false;
        }
    }

    /** Ad video: poll đến khi countdown chạy hết và Skip clickable. */
    private boolean dismissVideoSkipAd() {
        System.out.println("⏳ Video ad countdown — waiting for Skip…");
        long deadline = System.currentTimeMillis() + SKIP_COUNTDOWN_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (clickFirst(skipButtonLocators())) {
                long waited = SKIP_COUNTDOWN_TIMEOUT_MS - (deadline - System.currentTimeMillis());
                System.out.println("✅ Skipped after " + waited + "ms");
                return true;
            }
            sleep(SKIP_POLL_INTERVAL_MS);
        }
        System.out.println("⚠ Skip button did not appear within "
                + (SKIP_COUNTDOWN_TIMEOUT_MS / 1000) + "s");
        return false;
    }

    /**
     * Đóng ad với fallback chain. Mỗi vòng poll thử từ rẻ → đắt:
     *   1. Primary locator (id chuẩn của SDK)
     *   2. X/Skip locator (text "X", content-desc Skip…)
     *   3. Corner icon detection (icon clickable nhỏ ở góc trên)
     *   4. Fallback A — tap tọa độ cứng góc trên-phải/trái
     *   5. Fallback B — press device BACK button
     * Nếu hết 15s không đóng được:
     *   6. Brute force toàn bộ locator
     *
     * KHÔNG còn force-restart fallback ở đây — caller (ClickUtils.clickUntilSuccess)
     * sẽ tự restart khi element không tìm thấy. Tránh cascading restart làm
     * UiAutomator2 instrumentation crash.
     */
    private boolean pollAndClose(List<By> primaryLocators, long maxWaitMs) {
        long deadline = System.currentTimeMillis() + maxWaitMs;
        while (System.currentTimeMillis() < deadline) {
            if (clickFirst(primaryLocators)) return true;
            if (clickFirst(xOrSkipLocators())) return true;
            if (clickCornerCloseButton()) return true;
            // BACK trước raw tap: BACK pop activity stack có định nghĩa rõ → an toàn.
            // Raw tap dùng tọa độ cứng → có thể trúng nội dung ad gây redirect.
            if (pressBackToCloseAd()) return true;
            if (tapTopCornerCoordinates()) return true;
            if (detectByCurrentActivity() == AdType.NONE) {
                System.out.println("✅ Ad activity closed naturally");
                return true;
            }
            sleep(800);
        }
        if (dismissAnyCloseButton()) return true;
        // Trả false → caller xử lý (ClickUtils retry/restart). Không tự restart
        // ở đây để tránh cascading với ClickUtils restart → instrumentation crash.
        System.out.println("⚠ pollAndClose timeout — ad không đóng được, caller xử lý");
        return false;
    }

    /**
     * Fallback A — Tap RAW tọa độ góc trên-phải rồi trên-trái.
     * Hữu ích khi close button render trong WebView/Canvas mà UIAutomator không thấy.
     * SAFETY: chỉ tap khi ở AdActivity, verify thoát BẰNG cách check
     * currentPackage = app package (không chỉ "không phải ad" — vì click có thể
     * mở browser/ChromeActivity gây test ra khỏi context app).
     */
    private boolean tapTopCornerCoordinates() {
        if (detectByCurrentActivity() == AdType.NONE) return false;
        try {
            Dimension s = driver.manage().window().getSize();
            int w = s.getWidth(), h = s.getHeight();
            // Top-right (Google ad mặc định đặt close X ở đây)
            driver.executeScript("mobile: clickGesture",
                    Map.of("x", (int)(w * 0.93), "y", (int)(h * 0.06)));
            sleep(500);
            if (isBackInOurApp()) {
                System.out.println("🛑 Closed via raw tap top-right corner");
                return restartAfterRiskyTap();
            }
            // Top-left
            driver.executeScript("mobile: clickGesture",
                    Map.of("x", (int)(w * 0.07), "y", (int)(h * 0.06)));
            sleep(500);
            if (isBackInOurApp()) {
                System.out.println("🛑 Closed via raw tap top-left corner");
                return restartAfterRiskyTap();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Sau raw tap dù {@link #isBackInOurApp()} pass, vẫn có thể tap đã trúng nội
     * dung ad → app đã navigate vào webview của ad URL (cùng package nên không
     * detect được). Force restart về main activity để đảm bảo về home thay vì
     * mắc kẹt trên page lạ → các step tiếp theo find element home sẽ fail.
     */
    private boolean restartAfterRiskyTap() {
        restartToMainActivity();
        return true;
    }

    /**
     * BYPASS AD BẰNG POLL + RESTART — cách DUY NHẤT đáng tin cho app Flutter này.
     *
     * <p>Lý do: ad WebView (Google interstitial) đè lên Flutter làm CRASH tiến trình
     * UiAutomator2 instrumentation một cách bất thường ("lúc bypass được lúc không").
     * Khi instrumentation đã chết thì MỌI thao tác UI đều fail —
     * {@code getWindowSize}, {@code pressKey(BACK)}, {@code clickGesture} (tap toạ độ),
     * {@code findElements} (mọi locator). Chỉ 2 thứ SỐNG SÓT vì đi đường ADB chứ
     * không qua instrumentation:
     * <ul>
     *   <li>{@link #detectByCurrentActivity()} — đọc currentActivity (adb dumpsys)</li>
     *   <li>{@code mobile: startActivity} — bật lại MainActivity</li>
     * </ul>
     *
     * <p>Vòng lặp: thấy đang ở ad activity → startActivity MainActivity; coi là
     * thành công khi currentActivity RỜI ad VÀ ổn định (không bật lại) qua vài
     * lần kiểm tra liên tiếp — tránh trường hợp ad vừa tắt lại bật lên ngay làm
     * test assert trúng ad.
     */
    private boolean bypassAdByRestart(long maxWaitMs) {
        System.out.println("🎯 Bypass ad bằng poll + restart MainActivity (ADB-level, sống sót instrumentation crash)");
        final int STABLE_CHECKS = 2;             // số lần liên tiếp thấy non-ad mới coi là ổn định
        final long CHECK_INTERVAL_MS = 1_500;
        long deadline = System.currentTimeMillis() + maxWaitMs;
        int stable = 0;
        while (System.currentTimeMillis() < deadline) {
            if (detectByCurrentActivity() != AdType.NONE) {
                System.out.println("🔄 Vẫn ở ad activity → startActivity MainActivity");
                startMainActivity();
                stable = 0;
                sleep(CHECK_INTERVAL_MS);
                continue;
            }
            stable++;
            if (stable >= STABLE_CHECKS) {
                System.out.println("✅ Đã thoát ad, MainActivity ổn định");
                return true;
            }
            sleep(CHECK_INTERVAL_MS);
        }
        System.out.println("⚠ bypassAdByRestart hết " + (maxWaitMs / 1000) + "s vẫn còn ad");
        return detectByCurrentActivity() == AdType.NONE;
    }

    /** Bật lại MainActivity qua {@code mobile: startActivity} (ADB-level — không cần instrumentation). */
    private void startMainActivity() {
        String pkg = AppConstants.APP_PACKAGE;
        String activity = AppConstants.APP_ACTIVITY;
        if (pkg == null || activity == null) return;
        try {
            driver.executeScript("mobile: startActivity",
                    java.util.Map.of("component", pkg + "/" + activity, "wait", true));
        } catch (Exception e) {
            System.out.println("⚠ startActivity failed: " + e.getMessage());
        }
    }

    /**
     * ĐÓNG AD GIỮ NGUYÊN MÀN — KHÔNG restart app. Dùng cho ad bật lên GIỮA lúc test
     * điều hướng (bấm nút / vào màn khác), khi {@link #bypassAdByRestart} sẽ làm mất
     * state vì đá về splash/home.
     *
     * <p>Cơ chế: tap nút đóng bằng <b>ADB {@code input tap}</b> (KHÔNG qua Appium):
     * <ul>
     *   <li>Sống sót khi UiAutomator2 instrumentation crash (giống startActivity).</li>
     *   <li>Chỉ tap đóng ad → activity ad bị pop → trả về ĐÚNG màn Flutter đang đứng,
     *       KHÔNG load lại app.</li>
     * </ul>
     *
     * <p>Nút đóng nằm trong WebView (locator mù), vị trí khác nhau theo creative và
     * chỉ hiện sau countdown (~15-18s). → poll tới {@code maxWaitMs}, mỗi vòng tap
     * góc trên-TRÁI ("Đóng") rồi góc trên-PHẢI (X), toạ độ relative theo screen size
     * (lấy qua {@code wm size}). Tap trái trước vì góc phải lúc video hay là nút
     * "Cài đặt"/Install → tránh redirect; lỡ văng ra app khác thì BACK (ADB) kéo về.
     *
     * <p>BACK đơn thuần KHÔNG đủ (nhiều creative lờ BACK) nên ưu tiên tap toạ độ.
     *
     * @return true nếu đã rời ad (giữ nguyên màn); false nếu hết giờ vẫn còn ad
     *         (caller có thể gọi {@link #bypassAdByRestart} chấp nhận mất state).
     */
    public boolean dismissAdKeepState(long maxWaitMs) {
        if (detectByCurrentActivity() == AdType.NONE) return true;
        int[] sz = adbScreenSize();
        if (sz == null) {
            System.out.println("⚠ Không lấy được screen size qua ADB → không tap được");
            return false;
        }
        int w = sz[0], h = sz[1];
        // Ứng viên nút đóng: góc trên-TRÁI trước (an toàn), rồi góc trên-PHẢI.
        int[][] candidates = {
                {(int) (w * 0.08), (int) (h * 0.045)},  // top-left "Đóng"
                {(int) (w * 0.05), (int) (h * 0.03)},   // top-left sát mép
                {(int) (w * 0.95), (int) (h * 0.04)},   // top-right X
        };
        System.out.println("🎯 Đóng ad GIỮ-STATE bằng ADB tap (không restart) — poll chờ end-card");
        long deadline = System.currentTimeMillis() + maxWaitMs;
        while (System.currentTimeMillis() < deadline) {
            if (detectByCurrentActivity() == AdType.NONE) {
                System.out.println("✅ Đã đóng ad, GIỮ NGUYÊN màn (không restart)");
                return true;
            }
            for (int[] c : candidates) {
                adbTap(c[0], c[1]);
                sleep(800);
                // Lỡ tap trúng content → văng ra Play Store/Chrome → BACK (ADB) kéo về
                String pkg = currentPackageSafe();
                if (pkg != null && !pkg.isEmpty() && !pkg.equals(appPackage)) {
                    System.out.println("⚠ Tap văng ra " + pkg + " → BACK (ADB)");
                    adbBack();
                    sleep(800);
                }
                if (detectByCurrentActivity() == AdType.NONE) {
                    System.out.println("🛑 Đóng ad qua ADB tap (" + c[0] + "," + c[1] + ") — giữ state");
                    return true;
                }
            }
            sleep(1200);
        }
        System.out.println("⚠ Không đóng được ad giữ-state trong " + (maxWaitMs / 1000) + "s");
        return detectByCurrentActivity() == AdType.NONE;
    }

    // ===== ADB-level helpers (đi thẳng qua adb, KHÔNG qua UiAutomator2 instrumentation
    //       → sống sót kể cả khi instrumentation đã crash vì ad WebView) =====

    /** Chạy {@code adb [-s <udid>] shell <args...>}, trả stdout (rỗng nếu lỗi). */
    private String adbShell(String... shellArgs) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add("adb");
            String udid = AppConstants.UDID;
            if (udid != null && !udid.isEmpty()) {
                cmd.add("-s");
                cmd.add(udid);
            }
            cmd.add("shell");
            for (String a : shellArgs) cmd.add(a);
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes());
            p.waitFor();
            return out;
        } catch (Exception e) {
            System.out.println("⚠ adb shell failed: " + e.getMessage());
            return "";
        }
    }

    private void adbTap(int x, int y) {
        adbShell("input", "tap", String.valueOf(x), String.valueOf(y));
    }

    private void adbBack() {
        adbShell("input", "keyevent", "4");
    }

    /** Kích thước màn qua {@code wm size} ("Physical size: WxH") — không cần instrumentation. */
    private int[] adbScreenSize() {
        String out = adbShell("wm", "size");
        try {
            java.util.regex.Matcher m =
                    java.util.regex.Pattern.compile("(\\d+)x(\\d+)").matcher(out);
            if (m.find()) {
                return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** Package foreground qua {@code dumpsys activity activities} (ADB) — survive crash. */
    private String currentPackageSafe() {
        String out = adbShell("dumpsys", "activity", "activities");
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("topResumedActivity=\\S+ \\S+ ([\\w.]+)/").matcher(out);
            if (m.find()) return m.group(1);
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Fallback B — Press device BACK button. Hầu hết ad respond, một số rewarded thì không.
     * SAFETY: chỉ press khi đang ở AdActivity, verify quay về app (không phải minimize hay launcher).
     */
    private boolean pressBackToCloseAd() {
        if (detectByCurrentActivity() == AdType.NONE) return false;
        try {
            driver.pressKey(new KeyEvent(AndroidKey.BACK));
            sleep(800);
            if (isBackInOurApp()) {
                System.out.println("🛑 Closed via device BACK button");
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Verify đang ở context app của chúng ta (không phải ad, browser, launcher…). */
    private boolean isBackInOurApp() {
        try {
            String currentPkg = driver.getCurrentPackage();
            String appPkg = AppConstants.APP_PACKAGE;
            if (currentPkg == null || appPkg == null) return false;
            if (!currentPkg.equals(appPkg)) return false;
            // Đảm bảo không phải AdActivity nested trong app
            return detectByCurrentActivity() == AdType.NONE;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fallback C — Force-restart về main activity. Last resort khi mọi cách
     * khác đều fail (ad rewarded không thể skip, ad render canvas không hit được).
     * Bypass 100% mọi loại ad nhưng mất state nhỏ.
     *
     * Verify thoát AdActivity bằng cách poll currentActivity tới 5s — nếu vẫn
     * ở AdActivity sau restart (rất hiếm) thì retry 1 lần.
     */
    private boolean restartToMainActivity() {
        String pkg = AppConstants.APP_PACKAGE;
        String activity = AppConstants.APP_ACTIVITY;
        if (pkg == null || activity == null) return false;

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                driver.executeScript("mobile: startActivity", java.util.Map.of("component", pkg + "/" + activity, "wait", true));
                System.out.println("🔄 Force-restarted to " + activity + " (attempt " + attempt + ")");
                // Poll tới 5s để chờ Splash → Main load và verify thoát AdActivity
                long deadline = System.currentTimeMillis() + 5000;
                while (System.currentTimeMillis() < deadline) {
                    sleep(500);
                    if (detectByCurrentActivity() == AdType.NONE) {
                        System.out.println("✅ Restart successful, no longer in AdActivity");
                        return true;
                    }
                }
                System.out.println("⚠ Still in AdActivity after restart attempt " + attempt);
            } catch (Exception e) {
                System.out.println("⚠ Restart attempt " + attempt + " failed: " + e.getMessage());
            }
        }
        return false;
    }

    /** Locator cho nút X / Skip dùng text + content-desc. */
    private List<By> xOrSkipLocators() {
        List<By> list = new ArrayList<>();
        // Text "X" / unicode close characters
        for (String text : new String[]{"X", "x", "✕", "✖", "×", "⨯"}) {
            list.add(textButton(text));
            list.add(textTextView(text));
        }
        // Content-desc cho icon X / Skip / Close (đa biến thể)
        for (String desc : new String[]{
                "Close", "CLOSE", "close",
                "X", "Close X", "Close button", "Close Ad", "Close ad",
                "Skip", "SKIP", "Skip Ad", "Skip ad",
                "Dismiss", "Hủy",
                "Bỏ qua", "Bỏ qua quảng cáo", "Đóng", "Đóng quảng cáo"}) {
            list.add(AppiumBy.accessibilityId(desc));
        }
        // Bất kỳ element nào có description chứa "close"/"skip" (case-insensitive)
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().descriptionMatches(\"(?i).*close.*\")"));
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().descriptionMatches(\"(?i).*skip.*\")"));
        // ImageButton/ImageView có description chứa "close"/"skip"
        for (String kw : new String[]{"close", "skip"}) {
            list.add(AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.ImageButton\").descriptionContains(\"" + kw + "\")"));
            list.add(AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.ImageView\").descriptionContains(\"" + kw + "\")"));
        }
        return list;
    }

    /**
     * Tìm và click icon nhỏ clickable nằm ở 1 trong 4 góc màn (TOP/BOTTOM x LEFT/RIGHT).
     * Dùng khi ad không có resource-id/content-desc rõ ràng — Google ad đặt X ở
     * top corners; một số ad khác đặt X ở bottom (dưới nội dung). Loại trừ vùng
     * navigation bar dưới cùng (~8%) để tránh tap nhầm system buttons.
     */
    private boolean clickCornerCloseButton() {
        try {
            Dimension screen = driver.manage().window().getSize();
            int sw = screen.getWidth();
            int sh = screen.getHeight();
            int topZoneY = sh / 4;                  // 25% trên
            int bottomZoneY = sh * 3 / 4;            // 75% trở xuống
            int navBarExclude = (int)(sh * 0.92);    // loại trừ 8% dưới (nav bar)
            int leftZoneX = sw / 4;                  // 25% trái
            int rightZoneX = sw * 3 / 4;             // 75% phải trở đi
            int maxButtonSize = Math.min(sw, sh) / 5;

            String[] classes = {"android.widget.ImageButton", "android.widget.ImageView",
                    "android.widget.Button"};
            for (String cls : classes) {
                List<WebElement> elements = driver.findElements(AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"" + cls + "\").clickable(true)"));
                for (WebElement el : elements) {
                    if (!el.isDisplayed()) continue;
                    Rectangle rect = el.getRect();
                    int cx = rect.getX() + rect.getWidth() / 2;
                    int cy = rect.getY() + rect.getHeight() / 2;

                    boolean isSmall = rect.getWidth() < maxButtonSize && rect.getHeight() < maxButtonSize;
                    boolean isTop = cy < topZoneY;
                    boolean isBottom = cy > bottomZoneY && cy < navBarExclude; // bottom nhưng tránh nav bar
                    boolean isLeftCorner = cx < leftZoneX;
                    boolean isRightCorner = cx > rightZoneX;
                    // Mở rộng zone: cả corners + center-edge (ad có X ở giữa-trên/giữa-dưới)
                    boolean isHorizontalEdge = (isTop || isBottom);

                    if (isSmall && isHorizontalEdge) {
                        el.click();
                        sleep(500);
                        // Verify thực sự đóng được ad (không phải click trúng banner/link)
                        if (isBackInOurApp()) {
                            String vertical = isTop ? "TOP" : "BOTTOM";
                            String horizontal = isLeftCorner ? "LEFT"
                                    : isRightCorner ? "RIGHT" : "CENTER";
                            System.out.println("🛑 Closed via " + vertical + "-" + horizontal
                                    + " edge icon at (" + cx + "," + cy + ") size="
                                    + rect.getWidth() + "x" + rect.getHeight());
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Poll cho close button xuất hiện và clickable. Dùng cho ad in-app có countdown trước khi X render. */
    private boolean pollClickFirst(List<By> locators, long maxWaitMs) {
        long deadline = System.currentTimeMillis() + maxWaitMs;
        while (System.currentTimeMillis() < deadline) {
            if (clickFirst(locators)) return true;
            sleep(500);
        }
        return false;
    }

    /** Brute-force fallback — quét tất cả locator của mọi SDK. */
    private boolean dismissAnyCloseButton() {
        return clickFirst(skipButtonLocators())
                || clickFirst(googleCloseLocators())
                || clickFirst(facebookCloseLocators())
                || clickFirst(unityCloseLocators())
                || clickFirst(applovinCloseLocators())
                || clickFirst(ironsourceCloseLocators())
                || clickFirst(vungleCloseLocators())
                || clickFirst(mintegralCloseLocators())
                || clickFirst(pangleCloseLocators())
                || clickFirst(smartdigimktCloseLocators())
                || clickFirst(appPopupCloseLocators())
                || clickFirst(genericCloseLocators());
    }

    private boolean clickFirst(List<By> locators) {
        for (By locator : locators) {
            if (clickIfPresent(locator)) return true;
        }
        return false;
    }

    // ===== Locator groups per ad SDK =====

    private List<By> skipButtonLocators() {
        List<By> list = new ArrayList<>();
        // Đa ngôn ngữ: EN/VI/ES/FR/DE/JA/KO/ZH
        for (String text : new String[]{
                "Skip", "SKIP", "Skip Ad", "Skip ad",
                "Bỏ qua", "Bỏ qua quảng cáo",
                "Saltar", "Omitir", "Passer", "Ignorer",
                "Überspringen", "スキップ", "건너뛰기", "跳过", "跳過"}) {
            list.add(textButton(text));
            list.add(textTextView(text));
        }
        for (String desc : new String[]{"Skip ad", "Skip Ad", "Skip", "Bỏ qua quảng cáo"}) {
            list.add(AppiumBy.accessibilityId(desc));
        }
        for (String id : new String[]{"btn_skip", "skip_button", "tv_skip", "skip"}) {
            list.add(AppiumBy.id(appPackage + ":id/" + id));
        }
        return list;
    }

    private List<By> googleCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.id("com.google.android.gms:id/close_button"));
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"com\\\\.google\\\\.android\\\\.gms:id/(close|dismiss|skip).*\")"));
        // ⚠ KHÔNG dùng abgb/abgc/abgcp ở đây — đó là "AdChoices/Why this ad?" badge,
        // click vào sẽ mở popup info chứ không close ad. Chỉ dùng làm dấu hiệu detect.
        for (String rawId : new String[]{"dismiss-button", "close-button", "skip-button"}) {
            list.add(AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"" + rawId + "\")"));
        }
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"(?i)(dismiss|close|skip)[-_]?(button|btn)?\")"));
        for (String desc : new String[]{
                "Close ad", "Close advertisement", "Close interstitial ad", "Dismiss ad",
                "Đóng quảng cáo", "Bỏ qua quảng cáo"}) {
            list.add(AppiumBy.accessibilityId(desc));
        }
        return list;
    }

    private List<By> facebookCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"com\\\\.facebook\\\\.ads:id/.*close.*\")"));
        list.add(AppiumBy.accessibilityId("Close ad"));
        list.add(AppiumBy.accessibilityId("Interstitial close button"));
        return list;
    }

    private List<By> unityCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"com\\\\.unity3d\\\\.(ads|services):id/.*(close|skip).*\")"));
        for (String rawId : new String[]{"webplayer_close", "unityads_close",
                "unityads_button_skip", "unityads_button_close"}) {
            list.add(AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"" + rawId + "\")"));
        }
        return list;
    }

    private List<By> applovinCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"com\\\\.applovin.*:id/.*(close|skip|dismiss).*\")"));
        for (String rawId : new String[]{"applovin_close_button", "applovin_skip_button"}) {
            list.add(AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"" + rawId + "\")"));
        }
        return list;
    }

    private List<By> ironsourceCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"com\\\\.ironsource.*:id/.*(close|skip).*\")"));
        for (String rawId : new String[]{"ironsource_close_button", "is_close_btn", "close_btn"}) {
            list.add(AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"" + rawId + "\")"));
        }
        return list;
    }

    /**
     * Vungle/Liftoff/PineDrama WebView ad — tap raw vào X content có rủi ro hit ad
     * content → redirect Play Store. An toàn hơn: dùng BACK (Vungle ads thường
     * respond BACK = close) + wait countdown nếu X chưa enable.
     *
     * <p>Strategy:
     * <ol>
     *   <li>Press BACK → check ad mất chưa (an toàn, không redirect)</li>
     *   <li>Lặp BACK + wait countdown tối đa 6 lần (cover 20-30s countdown)</li>
     *   <li>Nếu BACK đẩy ra app khác → trả false để {@link #ensureInOurApp} recover</li>
     * </ol>
     */
    private boolean dismissWebViewAdByCornerTap() {
        if (!isPresent(AppiumBy.accessibilityId("Liftoff Privacy"))) return false;
        System.out.println("🎯 Liftoff/Vungle ad — try BACK (skip countdown ads)");

        for (int attempt = 1; attempt <= 6; attempt++) {
            try {
                driver.pressKey(new KeyEvent(AndroidKey.BACK));
                sleep(1500);

                // Check redirect: nếu BACK đẩy ra app khác → để ensureInOurApp xử lý
                String currentPkg = null;
                try { currentPkg = driver.getCurrentPackage(); } catch (Exception ignored) {}
                if (currentPkg != null && appPackage != null && !appPackage.equals(currentPkg)) {
                    System.out.println("⚠ BACK navigated to " + currentPkg);
                    return false;
                }

                // Ad đã đóng?
                if (!isPresent(AppiumBy.accessibilityId("Liftoff Privacy"))) {
                    System.out.println("🛑 Closed Liftoff ad via BACK (attempt " + attempt + ")");
                    return true;
                }
                // Còn countdown — chờ 2s rồi thử BACK tiếp
                sleep(2000);
            } catch (Exception ignored) {}
        }
        System.out.println("⚠ Liftoff ad không đóng được sau 6 BACK attempts");
        return false;
    }

    private List<By> vungleCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"com\\\\.vungle.*:id/.*(close|skip).*\")"));
        for (String rawId : new String[]{"vungle_close_button", "vungle_close",
                "vungle_skip_button"}) {
            list.add(AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"" + rawId + "\")"));
        }
        return list;
    }

    private List<By> mintegralCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"com\\\\.mbridge\\\\.msdk.*:id/.*(close|skip).*\")"));
        for (String rawId : new String[]{"mbridge_iv_iv_close", "mbridge_video_common_alertview_cancel_button",
                "mbridge_full_player_close"}) {
            list.add(AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"" + rawId + "\")"));
        }
        return list;
    }

    /**
     * SDM reward video ad — full-screen video với countdown (~10-25s). X close
     * chỉ xuất hiện SAU khi countdown end. Strategy: lặp BACK + wait countdown.
     */
    private boolean dismissSdmRewardAd() {
        By rootBy = AppiumBy.id(appPackage + ":id/sdm_reward_root_container");
        By topBy = AppiumBy.id(appPackage + ":id/sdm_top_control");
        if (!isPresent(rootBy) && !isPresent(topBy)) return false;
        System.out.println("🎯 SDM reward ad — wait countdown + BACK");

        for (int attempt = 1; attempt <= 8; attempt++) {
            try {
                driver.pressKey(new KeyEvent(AndroidKey.BACK));
                sleep(1500);

                String currentPkg = null;
                try { currentPkg = driver.getCurrentPackage(); } catch (Exception ignored) {}
                if (currentPkg != null && appPackage != null && !appPackage.equals(currentPkg)) {
                    System.out.println("⚠ BACK navigated to " + currentPkg);
                    return false;
                }
                if (!isPresent(rootBy) && !isPresent(topBy)) {
                    System.out.println("🛑 Closed SDM reward ad via BACK (attempt " + attempt + ")");
                    return true;
                }
                sleep(2000);  // wait for countdown
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * smartdigimkt (Việt Nam ad SDK) — không biết chính xác close-button ID,
     * dùng pattern phổ biến + để pollAndClose fallback (X/Skip, corner tap, BACK).
     */
    private List<By> smartdigimktCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"com\\\\.smartdigimkt.*:id/.*(close|skip|dismiss).*\")"));
        // App nested ad → resource-id thường mang appPackage
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"" + appPackage.replace(".", "\\\\.")
                        + ":id/.*(close|skip|dismiss|btn_close|iv_close).*\")"));
        for (String rawId : new String[]{"close_button", "skip_button", "btn_close",
                "iv_close", "ic_close", "img_close", "ad_close"}) {
            list.add(AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"" + rawId + "\")"));
        }
        return list;
    }

    private List<By> pangleCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"com\\\\.bytedance\\\\.sdk.*:id/.*(close|skip).*\")"));
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().resourceIdMatches(\"com\\\\.pangle.*:id/.*(close|skip).*\")"));
        for (String rawId : new String[]{"tt_video_ad_close_layout", "tt_item_videocache_cover",
                "tt_video_ad_close", "tt_full_skip"}) {
            list.add(AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"" + rawId + "\")"));
        }
        return list;
    }

    private List<By> appPopupCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.id(appPackage + ":id/text_cancel"));
        for (String id : new String[]{"btn_close", "btn_cancel", "btn_no",
                "iv_close", "ic_close", "img_close", "tv_close", "dismiss_button"}) {
            list.add(AppiumBy.id(appPackage + ":id/" + id));
        }
        // In-app ad: Button content-desc="Close"/"Đóng", clickable, không có resource-id
        for (String desc : new String[]{"Close", "CLOSE", "close", "Đóng", "đóng"}) {
            list.add(AppiumBy.accessibilityId(desc));
        }
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.Button\").descriptionMatches(\"(?i)^(close|đóng)$\").clickable(true)"));
        for (String text : new String[]{"No, thanks", "No Thanks", "Hủy", "Đóng",
                "Cancel", "CANCEL", "Cerrar", "Annuler", "Abbrechen"}) {
            list.add(textButton(text));
        }
        return list;
    }

    /** Locator chung — dùng cuối cùng trong fallback vì dễ false-positive. */
    private List<By> genericCloseLocators() {
        List<By> list = new ArrayList<>();
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().descriptionMatches(\"(?i).*(close|dismiss|skip).*ad.*\")"));
        list.add(AppiumBy.androidUIAutomator(
                "new UiSelector().descriptionMatches(\"(?i).*ad.*(close|dismiss|skip).*\")"));
        for (String text : new String[]{"Close", "CLOSE", "Dismiss", "X", "✕", "✖"}) {
            list.add(textButton(text));
            list.add(textTextView(text));
        }
        for (String desc : new String[]{"Close", "Dismiss", "Đóng",
                "Cerrar", "Fermer", "Schließen", "閉じる", "닫기", "关闭", "關閉"}) {
            list.add(AppiumBy.accessibilityId(desc));
        }
        return list;
    }

    // ===== Detection helpers =====

    private boolean matchesPackage(String regex) {
        return isPresent(AppiumBy.androidUIAutomator(
                "new UiSelector().packageNameMatches(\"" + regex + "\")"));
    }

    /**
     * Phát hiện ad qua tên activity hiện tại — reliable hơn UI element vì
     * activity tồn tại ngay khi ad bắt đầu, kể cả lúc WebView chưa render.
     */
    private AdType detectByCurrentActivity() {
        try {
            String activity = driver.currentActivity();
            if (activity == null) return AdType.NONE;
            if (activity.contains("com.google.android.gms.ads") || activity.contains("AdActivity"))
                return AdType.GOOGLE_AD;
            if (activity.contains("com.facebook.ads"))      return AdType.FACEBOOK_AD;
            if (activity.contains("com.unity3d.ads") || activity.contains("com.unity3d.services"))
                return AdType.UNITY_AD;
            if (activity.contains("com.applovin"))          return AdType.APPLOVIN_AD;
            if (activity.contains("com.ironsource"))        return AdType.IRONSOURCE_AD;
            if (activity.contains("com.vungle"))            return AdType.VUNGLE_AD;
            if (activity.contains("com.mbridge.msdk"))      return AdType.MINTEGRAL_AD;
            if (activity.contains("com.bytedance.sdk") || activity.contains("com.pangle"))
                return AdType.PANGLE_AD;
            // smartdigimkt SDK: ATPortraitActivity / ATLandscapeActivity nested trong app package
            if (activity.contains("com.smartdigimkt")
                    || activity.contains("ATPortraitActivity")
                    || activity.contains("ATLandscapeActivity"))
                return AdType.SMARTDIGIMKT_AD;
        } catch (Exception ignored) {
        }
        return AdType.NONE;
    }

    private boolean hasRawResourceId(String... rawIds) {
        for (String rawId : rawIds) {
            if (isPresent(AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"" + rawId + "\")"))) {
                return true;
            }
        }
        return false;
    }

    private By textButton(String text) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.Button\").text(\"" + text + "\")");
    }

    private By textTextView(String text) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().className(\"android.widget.TextView\").text(\"" + text + "\")");
    }

    // ===== Click utilities =====

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
            System.out.println("🛑 Closed via: " + locator);
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
