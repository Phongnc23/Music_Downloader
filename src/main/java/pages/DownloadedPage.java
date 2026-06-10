package pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Page Object cho Downloaded screen.
 *
 * DOM:
 *  - Header: Back button (Button instance 0), title "Downloaded", track count "N tracks"
 *  - Sort button: Button instance 1
 *  - Track list: ScrollView > View long-clickable, content-desc = "title-id\n<unknown> * duration"
 */
public class DownloadedPage {

    private static final Logger logger = LogManager.getLogger(DownloadedPage.class);

    private final AppiumDriver driver;
    private final WebDriverWait wait;

    private final By BACK_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.Button\").clickable(true).instance(0)");

    private final By DOWNLOADED_TITLE = AppiumBy.accessibilityId("Downloaded");

    private final By TRACK_ITEMS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.view.View\").longClickable(true)");

    public DownloadedPage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isOnDownloadedScreen() {
        try {
            return !driver.findElements(DOWNLOADED_TITLE).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /** Mau label tong so track: content-desc dang "5 tracks" / "1 track" (theo DOM). */
    private static final Pattern TRACK_COUNT_PATTERN = Pattern.compile("(?i)^\\s*(\\d+)\\s*tracks?\\s*$");

    /**
     * Lay TONG so track tu label "N tracks" tren header Downloaded (theo DOM:
     * content-desc="5 tracks"). KHONG dem item trong viewport vi list co the nhieu bai hon
     * so hien thi (~6 bai dau) -> dem item se sai. Quet tat ca content-desc, parse phia Java
     * (tranh quirk regex cua UiAutomator) va lay phan tu khop dung dang "N track(s)".
     *
     * @return tong so track, hoac -1 neu khong tim thay label.
     */
    public int getTrackCount() {
        try {
            List<WebElement> all = driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().descriptionMatches(\".+\")"));
            for (WebElement el : all) {
                String desc = el.getAttribute("content-desc");
                if (desc == null) continue;
                Matcher m = TRACK_COUNT_PATTERN.matcher(desc);
                if (m.matches()) {
                    return Integer.parseInt(m.group(1));
                }
            }
        } catch (Exception e) {
            logger.warn("Loi doc track count label: " + e.getMessage());
        }
        logger.warn("Khong tim thay label 'N tracks' tren man Downloaded");
        return -1;
    }

    /**
     * Lay danh sach content-desc cua cac track da download.
     */
    public List<String> getDownloadedDescs() {
        List<String> descs = new ArrayList<>();
        try {
            List<WebElement> items = driver.findElements(TRACK_ITEMS);
            for (WebElement el : items) {
                String desc = el.getAttribute("content-desc");
                if (desc != null && !desc.isEmpty() && !desc.equals("Downloaded")) {
                    descs.add(desc);
                }
            }
        } catch (Exception e) {
            logger.warn("Loi lay downloaded list: " + e.getMessage());
        }
        return descs;
    }

    /**
     * Check xem co track nao chua tu khoa keyword khong (case-insensitive).
     * VD: "MAKING MY WAY" se match "SON TUNG M-TP _ MAKING MY WAY _ OFFICIAL VISUALIZER-528520..."
     */
    public boolean containsTrack(String keyword) {
        if (keyword == null || keyword.isEmpty()) return false;
        String kw = normalize(keyword);
        if (kw.isEmpty()) return false;
        for (String desc : getDownloadedDescs()) {
            if (normalize(desc).contains(kw)) return true;
        }
        return false;
    }

    /** Chuan hoa de so khop title: bo ky tu phan tach (| _ -), gop khoang trang, lowercase. */
    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("[|_\\-]", " ").replaceAll("\\s+", " ").trim();
    }

    public void clickTrackByIndex(int index) {
        List<WebElement> items = driver.findElements(TRACK_ITEMS);
        if (index >= items.size()) {
            throw new RuntimeException("Track index " + index + " out of range");
        }
        items.get(index).click();
    }

    public void clickBack() {
        try {
            driver.findElement(BACK_BUTTON).click();
        } catch (Exception e) {
            ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        }
    }
}