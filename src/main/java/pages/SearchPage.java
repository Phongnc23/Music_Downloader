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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Page Object cho Search screen + Search Results.
 *
 * Cau truc DOM:
 *  - Header: Button (back, instance 0), EditText (textbox), Button/ImageView (clear)
 *  - Suggestions: ImageView long-clickable + child ImageView (push icon)
 *  - Results: ScrollView > ImageView long-clickable + child ImageView (download icon)
 *
 * Phan biet: results co content-desc chua "\n", suggestions thi khong.
 */
public class SearchPage {

    private static final Logger logger = LogManager.getLogger(SearchPage.class);
    private static final int TIMEOUT_SEC = 10;

    private final AppiumDriver driver;
    private final WebDriverWait wait;

    // ===== HEADER ELEMENTS =====
    private final By BACK_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.Button\").clickable(true).instance(0)");

    private final By SEARCH_TEXTBOX = AppiumBy.className("android.widget.EditText");

    // Clear button = Button instance(1) khi co text, ImageView khi rong
    private final By CLEAR_BUTTON = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.Button\").clickable(true).instance(1)");

    // ===== LIST ITEMS (suggestions hoac results) =====
    private final By LIST_ITEMS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.ImageView\").longClickable(true)");

    public SearchPage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SEC));
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

    private void tapByCoordinate(int x, int y) {
        driver.executeScript("mobile: clickGesture", Map.of("x", x, "y", y));
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ============================================
    // VERIFICATION
    // ============================================

    public boolean isOnSearchScreen() {
        return isDisplayed(SEARCH_TEXTBOX);
    }

    public boolean waitForSearchScreen(int timeoutSec) {
        long deadline = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (isOnSearchScreen()) return true;
            sleep(400);
        }
        return false;
    }

    public boolean isKeyboardShown() {
        try {
            return ((AndroidDriver) driver).isKeyboardShown();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Dam bao ban phim ao dang hien de nhap. Mo hinh dung luong cua app:
     * <ul>
     *   <li>Nhan search KHONG co quang cao -> ban phim TU hien -> {@link #isKeyboardShown()}
     *       da true -> khong can lam gi.</li>
     *   <li>Nhan search CO quang cao va da bypass -> ban phim KHONG hien -> phai TAP textbox
     *       1 lan nua moi hien -> tap roi kiem tra lai (thu toi 2 lan cho chac).</li>
     * </ul>
     *
     * @return true neu cuoi cung ban phim da hien
     */
    public boolean ensureKeyboardShown() {
        if (isKeyboardShown()) {
            logger.info("Ban phim da hien (khong co ad chen) -> khong tap them");
            return true;
        }
        for (int attempt = 1; attempt <= 2 && !isKeyboardShown(); attempt++) {
            if (!isOnSearchScreen()) {
                logger.warn("Khong o man Search -> khong tap textbox (tranh treo)");
                break;
            }
            logger.info("Ban phim chua hien (co the do ad da bypass) -> tap textbox lan " + attempt);
            tapSearchBox();
            sleep(700);
        }
        return isKeyboardShown();
    }

    public String getTextboxValue() {
        try {
            WebElement tb = driver.findElement(SEARCH_TEXTBOX);
            String text = tb.getText();
            return text == null ? "" : text;
        } catch (Exception e) {
            return "";
        }
    }

    public boolean hasResults() {
        try {
            // Result co content-desc chua "\n"
            List<WebElement> items = driver.findElements(LIST_ITEMS);
            for (WebElement el : items) {
                String desc = el.getAttribute("content-desc");
                if (desc != null && desc.contains("\n")) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean hasSuggestions() {
        try {
            List<WebElement> items = driver.findElements(LIST_ITEMS);
            for (WebElement el : items) {
                String desc = el.getAttribute("content-desc");
                if (desc != null && !desc.contains("\n") && !desc.isEmpty()) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    // ============================================
    // INPUT ACTIONS
    // ============================================

    /**
     * Tap vao textbox de focus va show keyboard.
     * Goi sau khi vao Search screen lan dau, hoac khi keyboard bi an.
     */
    public void tapSearchBox() {
        logger.info("Tap search textbox");
        try {
            List<WebElement> boxes = driver.findElements(SEARCH_TEXTBOX);
            if (boxes.isEmpty()) {
                logger.warn("Khong thay EditText de tap (chua o man Search)");
                return;
            }
            boxes.get(0).click();
            sleep(800);
        } catch (Exception e) {
            logger.warn("tapSearchBox loi: " + e.getMessage());
        }
    }

    public void typeQuery(String query) {
        logger.info("Type query: " + query);
        try {
            List<WebElement> boxes = driver.findElements(SEARCH_TEXTBOX);
            if (boxes.isEmpty()) {
                logger.warn("Khong thay EditText de go (chua o man Search)");
                return;
            }
            WebElement tb = boxes.get(0);
            tb.click();
            sleep(500);
            tb.sendKeys(query);
            sleep(800);
        } catch (Exception e) {
            logger.warn("typeQuery loi: " + e.getMessage());
        }
    }

    public void clearTextbox() {
        logger.info("Clear textbox");
        try {
            // Cach 1: tap clear button (X)
            if (isDisplayed(CLEAR_BUTTON)) {
                driver.findElement(CLEAR_BUTTON).click();
                sleep(500);
                return;
            }
        } catch (Exception ignored) {
        }
        // Cach 2: clear via EditText
        try {
            driver.findElement(SEARCH_TEXTBOX).clear();
            sleep(500);
        } catch (Exception e) {
            logger.warn("Cannot clear textbox: " + e.getMessage());
        }
    }

    /**
     * Press Enter/Search tren keyboard de trigger search.
     */
    public void submitSearch() {
        logger.info("Submit search (press ENTER)");
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.ENTER));
        sleep(2000);
    }

    /** Nhan DEVICE BACK (an toan tren man input Flutter — KHONG dung tren man results WebView). */
    public void pressDeviceBack() {
        logger.info("Press device BACK (tren man Search input)");
        ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
        sleep(900);
    }

    /**
     * Roi man Search bang nut back top-left (in-app). Neu sau 1 lan van con o Search
     * (vd ban phim hut lan back dau), bam them. Tra ve khi da roi Search hoac het luot.
     */
    public void clickBack() {
        logger.info("Click back button (roi man Search)");
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                driver.findElement(BACK_BUTTON).click();
            } catch (Exception e) {
                ((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
            }
            sleep(1000);
            if (!isOnSearchScreen()) return;
        }
    }

    // ============================================
    // SUGGESTIONS
    // ============================================

    /**
     * Lay danh sach text cua cac suggestion dang hien.
     */
    public List<String> getSuggestionTexts() {
        List<String> texts = new ArrayList<>();
        try {
            List<WebElement> items = driver.findElements(LIST_ITEMS);
            for (WebElement el : items) {
                String desc = el.getAttribute("content-desc");
                // Suggestion: khong co newline
                if (desc != null && !desc.isEmpty() && !desc.contains("\n")) {
                    texts.add(desc);
                }
            }
        } catch (Exception e) {
            logger.warn("Loi lay suggestions: " + e.getMessage());
        }
        return texts;
    }

    public int getSuggestionCount() {
        return getSuggestionTexts().size();
    }

    /**
     * Click vao TEXT cua suggestion -> trigger search.
     * Tap o phia LEFT (text region), khong phai push icon ben phai.
     */
    public void clickSuggestionByText(String suggestionText) {
        logger.info("Click suggestion text: " + suggestionText);
        WebElement sugg = driver.findElement(AppiumBy.accessibilityId(suggestionText));
        Rectangle r = sugg.getRect();
        // Tap o center-left (text area)
        int tapX = r.getX() + 400;
        int tapY = r.getY() + r.getHeight() / 2;
        tapByCoordinate(tapX, tapY);
        sleep(2000);
    }

    public void clickSuggestionByIndex(int index) {
        List<String> texts = getSuggestionTexts();
        if (index >= texts.size()) {
            throw new RuntimeException("Suggestion index " + index + " out of range, total=" + texts.size());
        }
        clickSuggestionByText(texts.get(index));
    }

    /**
     * Click vao PUSH ICON cua suggestion -> chi FILL textbox, KHONG search.
     * Tap o phia RIGHT (~x=1605).
     */
    public void clickPushIconByText(String suggestionText) {
        logger.info("Click push icon for: " + suggestionText);
        WebElement sugg = driver.findElement(AppiumBy.accessibilityId(suggestionText));
        Rectangle r = sugg.getRect();
        // Push icon o ben phai (~x=1605)
        int tapX = r.getX() + r.getWidth() - 115;
        int tapY = r.getY() + r.getHeight() / 2;
        tapByCoordinate(tapX, tapY);
        sleep(1000);
    }

    public void clickPushIconByIndex(int index) {
        List<String> texts = getSuggestionTexts();
        if (index >= texts.size()) {
            throw new RuntimeException("Suggestion index " + index + " out of range");
        }
        clickPushIconByText(texts.get(index));
    }

    // ============================================
    // RESULTS
    // ============================================

    /**
     * Lay danh sach content-desc cua results.
     */
    public List<String> getResultDescs() {
        List<String> descs = new ArrayList<>();
        try {
            List<WebElement> items = driver.findElements(LIST_ITEMS);
            for (WebElement el : items) {
                String desc = el.getAttribute("content-desc");
                // Result: co newline (title \n creator * duration)
                if (desc != null && desc.contains("\n")) {
                    descs.add(desc);
                }
            }
        } catch (Exception e) {
            logger.warn("Loi lay results: " + e.getMessage());
        }
        return descs;
    }

    /**
     * Lay title (dong dau truoc \n) cua results.
     */
    public List<String> getResultTitles() {
        return getResultDescs().stream()
                .map(d -> d.split("\n")[0])
                .collect(Collectors.toList());
    }

    public int getResultCount() {
        return getResultDescs().size();
    }

    public boolean waitForResults(int timeoutSec) {
        long deadline = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (hasResults()) return true;
            sleep(500);
        }
        return false;
    }

    public boolean waitForSuggestions(int timeoutSec) {
        long deadline = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (hasSuggestions()) return true;
            sleep(500);
        }
        return false;
    }

    /**
     * Click vao download icon cua result o index N.
     * Download icon nam o goc tren-phai cua result (~x=1644, y=offset 80 tu top).
     */
    public void clickDownloadByIndex(int index) {
        logger.info("Click download icon - result index " + index);
        List<WebElement> results = getResultElements();
        if (index >= results.size()) {
            throw new RuntimeException("Result index " + index + " out of range");
        }
        WebElement result = results.get(index);
        Rectangle r = result.getRect();
        // Download icon: bounds [1569,376][1720,526] cho result 1 -> x=1644 (center)
        int tapX = r.getX() + r.getWidth() - 76;  // ~1644
        int tapY = r.getY() + 83 + 75;  // offset ~83 from top + 75 = center of icon
        tapByCoordinate(tapX, tapY);
        sleep(1500);
    }

    public String getResultTitleByIndex(int index) {
        List<String> titles = getResultTitles();
        if (index >= titles.size()) return null;
        return titles.get(index);
    }

    private List<WebElement> getResultElements() {
        List<WebElement> results = new ArrayList<>();
        try {
            List<WebElement> items = driver.findElements(LIST_ITEMS);
            for (WebElement el : items) {
                String desc = el.getAttribute("content-desc");
                if (desc != null && desc.contains("\n")) {
                    results.add(el);
                }
            }
        } catch (Exception ignored) {
        }
        return results;
    }
}