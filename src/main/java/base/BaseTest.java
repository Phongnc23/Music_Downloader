package base;

import constants.AppConstants;
import driver.DriverFactory;
import driver.DriverManager;
import helpers.AdHelper;
import helpers.DialogHelper;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import pages.HomePage;
import pages.SearchPage;
import report.ExtentReportManager;

/**
 * Base cho toan bo test.
 *
 * <p><b>Toi uu thoi gian:</b> app chi LAUNCH 1 LAN cho ca suite (@BeforeSuite) va GIU SONG
 * xuyen suot — KHONG cold start moi class, KHONG terminate/relaunch giua cac test. Quang cao
 * mo app duoc bypass 1 lan o @BeforeSuite (= ke thua tu Home01). Tu do ve sau:
 * <ul>
 *   <li>{@code @BeforeMethod} chi guard ad NHANH (reactive) — neu CO ad dang che thi dong,
 *       khong poll 15s nhu truoc.</li>
 *   <li>Cac test dieu huong ve Home bang BACK ({@link #resetToHome()}) — khong out app.</li>
 *   <li>Ad bung GIUA phien (sau ~40s cooldown, khi user thao tac) duoc bypass ngay tai cho
 *       (giu nguyen man) — day chinh la luong "test bypass ad giua phien" (Home02).</li>
 * </ul>
 */
public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected AppiumDriver driver;

    /** Chi khoi tao session 1 lan cho ca suite (phong khi @BeforeSuite bi goi lap). */
    private static boolean suiteStarted = false;

    /**
     * Chay 1 LAN truoc ca suite: tao driver, cold start app, bypass ad mo app + dialog Update.
     * Sau buoc nay app dang o HOME, ad da tat — moi test ke thua trang thai nay.
     */
    @BeforeSuite(alwaysRun = true)
    public void setUpSuite() {
        if (suiteStarted) return;
        suiteStarted = true;

        logger.info("========== SETUP SUITE (1 lan duy nhat) ==========");
        AppiumDriver d = DriverFactory.createDriver();
        DriverManager.setDriver(d);

        // Cold start de quang cao interstitial mo app fire on dinh.
        try {
            ((AndroidDriver) d).terminateApp(AppConstants.APP_PACKAGE);
            ((AndroidDriver) d).activateApp(AppConstants.APP_PACKAGE);
        } catch (Exception e) {
            logger.warn("Cold start relaunch loi: " + e.getMessage());
        }
        logger.info("App da launch (cold start): " + AppConstants.APP_PACKAGE);

        // Bypass ad mo app + dialog Update — CHI o day, khong lap lai moi test.
        long t0 = System.currentTimeMillis();
        try {
            new AdHelper((AndroidDriver) d).dismissAllAds();
        } catch (Exception e) {
            logger.warn("Loi xu ly quang cao mo app: " + e);
        }
        try {
            new DialogHelper((AndroidDriver) d).dismissDialog();
        } catch (Exception e) {
            logger.warn("Loi xu ly dialog mo app: " + e);
        }
        // App co the COLD-START khoi phuc route cu = man "Search in library" (khong phai Home) ->
        // BACK thoat search ve Home de Home tests dau suite khong fail. Chi BACK khi DUNG o search.
        try {
            pages.SearchLibraryPage slp = new pages.SearchLibraryPage(d);
            for (int i = 0; i < 2 && slp.isOnSearchScreen(); i++) {
                ((AndroidDriver) d).pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                        io.appium.java_client.android.nativekey.AndroidKey.BACK));
                Thread.sleep(1500);
            }
        } catch (Exception e) {
            logger.warn("Loi thoat man search mo app: " + e);
        }
        logger.info("[TIMING] bypass ad+dialog mo app: " + (System.currentTimeMillis() - t0) + "ms");
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        // Tai su dung session dung chung — KHONG tao driver/cold start moi class.
        driver = DriverManager.getDriver();
        if (driver == null) {
            // Defensive: neu chay 1 class le khong qua suite (hiem) -> tu khoi tao.
            setUpSuite();
            driver = DriverManager.getDriver();
        }
        logger.info("=== Bat dau class: " + getClass().getSimpleName() + " (dung session chung) ===");
    }

    /**
     * Truoc moi test: GUARD AD NHANH (reactive). Neu dang co ad che man (vd ad bung o cuoi
     * test truoc ma chua kip dong) thi dong ngay; neu khong co ad thi tra ve sau ~1s.
     * KHONG con poll 15s + dialog 4s nhu truoc -> tiet kiem ~20s/test.
     */
    @BeforeMethod(alwaysRun = true)
    public void handleAdBeforeTest() {
        // QUAN TRONG: UiAutomator2 instrumentation co the CRASH ngau nhien tren app Flutter
        // (WebView ad, gesture nang, hoac ngau nhien). Khi do MOI lenh qua instrumentation fail
        // -> ca shared session chet -> tat ca test sau cascade fail/skip. Recover: tao lai driver
        // + cold start app. Chi chay khi session that su chet (binh thuong la no-op nhanh).
        recoverSessionIfDead();
        if (driver == null) return;  // session tao that bai -> khong lam gi (tranh treo)
        try {
            new AdHelper((AndroidDriver) driver).handleAdIfAppears(400);
        } catch (Exception e) {
            logger.warn("Loi guard ad truoc test: " + e.getMessage());
        }
        // Dialog "Update app" co the bung GIUA RUN (sau cua so 4s cua setUpSuite) -> dong ngay
        // de khong ket. Nhanh, khong cho (no-op neu khong co dialog).
        try {
            new DialogHelper((AndroidDriver) driver).dismissUpdateDialogIfPresent();
        } catch (Exception e) {
            logger.warn("Loi dong update dialog truoc test: " + e.getMessage());
        }
    }

    /** Session (UA2 instrumentation) con song khong — probe bang 1 findElements re. */
    private boolean isSessionAlive() {
        if (driver == null) return false;
        try {
            driver.findElements(AppiumBy.androidUIAutomator(
                    "new UiSelector().packageName(\"__healthprobe__\")"));
            return true;   // tra empty -> song
        } catch (Exception e) {
            String m = e.getMessage();
            if (m != null && (m.contains("instrumentation process is not running")
                    || m.contains("not started")
                    || m.contains("session is either terminated")
                    || m.contains("cannot be proxied"))) {
                return false;
            }
            return true;   // loi khac -> coi nhu con song
        }
    }

    /**
     * Neu session chet (UA2 crash) -> quit driver cu, tao driver moi, cold start app + bypass ad.
     * Cap nhat ca DriverManager va this.driver de @BeforeMethod cua test class dung session moi.
     */
    protected void recoverSessionIfDead() {
        if (isSessionAlive()) return;
        logger.error("========== SESSION CHET (UA2 crash) -> RECOVER: tao lai driver ==========");
        try {
            AppiumDriver old = DriverManager.getDriver();
            if (old != null) old.quit();
        } catch (Exception e) {
            logger.warn("Quit driver cu loi (bo qua): " + e.getMessage());
        }
        DriverManager.removeDriver();
        try {
            AppiumDriver d = DriverFactory.createDriver();
            DriverManager.setDriver(d);
            this.driver = d;
            try {
                ((AndroidDriver) d).terminateApp(AppConstants.APP_PACKAGE);
                ((AndroidDriver) d).activateApp(AppConstants.APP_PACKAGE);
            } catch (Exception e) {
                logger.warn("Cold start sau recover loi: " + e.getMessage());
            }
            try { new AdHelper((AndroidDriver) d).dismissAllAds(); } catch (Exception ignored) {}
            try { new DialogHelper((AndroidDriver) d).dismissDialog(); } catch (Exception ignored) {}
            logger.info("========== RECOVER session THANH CONG ==========");
        } catch (Exception e) {
            logger.error("RECOVER session THAT BAI: " + e.getMessage());
            this.driver = null;
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterEachTest(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("Test FAILED: " + result.getName());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("Test PASSED: " + result.getName());
        }
    }

    /** Tear down 1 LAN sau ca suite: terminate + quit + flush report. */
    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        logger.info("========== TEARDOWN SUITE ==========");
        AppiumDriver d = DriverManager.getDriver();
        if (d != null) {
            try {
                ((AndroidDriver) d).terminateApp(AppConstants.APP_PACKAGE);
            } catch (Exception e) {
                logger.warn("Loi terminate: " + e.getMessage());
            }
            try {
                d.quit();
            } catch (Exception e) {
                logger.warn("Loi quit: " + e.getMessage());
            }
            DriverManager.removeDriver();
        }
        ExtentReportManager.flush();
    }

    // ============================================
    // HELPER METHODS CHO TEST CLASSES
    // ============================================

    /**
     * Guard ad reactive sau 1 thao tac: cho toi {@code windowMs} xem ad co bung khong; neu CO
     * thi bypass ngay tai cho (GIU NGUYEN man dang do — khong restart) roi test chay tiep; neu
     * KHONG co thi tra ve khi het window (dung lam luon "doi sau thao tac"). Dung method nay
     * THAY cho {@code sleep(...)} sau cac thao tac co the bung ad.
     */
    protected void guardAd(long windowMs) {
        if (driver == null) return;
        try {
            new AdHelper((AndroidDriver) driver).handleAdIfAppears(windowMs);
        } catch (Exception e) {
            logger.warn("guardAd loi: " + e.getMessage());
        }
    }

    /**
     * Ve Home (KHONG out app). Chon dung cach theo loai man dang dung:
     * <ul>
     *   <li><b>TAB bottom-nav</b> (Tracks/Artists/Albums/Playlists) — bottom nav hien nhung
     *       khong phai Home: PHAI bam <b>tab Home</b>. KHONG dung BACK vi BACK o tab top-level
     *       se bung dialog "Are you sure you want to exit?" (khong ve Home).</li>
     *   <li><b>Man PUSHED</b> (Settings/Downloaded/Search/full player) hoac dialog/drawer —
     *       khong co bottom nav: <b>BACK</b> pop ve Home (man con cua Home, khong bung exit
     *       dialog).</li>
     *   <li><b>Ngoai app</b> (vd Rate us -> Play Store): startActivity keo app tro lai.</li>
     *   <li><b>Exit dialog dang mo</b> (vd sau test exit): Cancel roi bam tab Home.</li>
     * </ul>
     */
    protected void resetToHome() {
        if (driver == null) return;  // session tao that bai -> khong reset (tranh treo vong lap)
        long t0 = System.currentTimeMillis();
        AndroidDriver d = (AndroidDriver) driver;
        AdHelper ad = new AdHelper(d);
        try {
            HomePage hp = new HomePage(driver);
            for (int round = 0; round < 6; round++) {
                ad.handleAdIfAppears(700);

                // 0) Bi day ra ngoai app (Rate us -> Play Store, ad redirect...) -> keo ve.
                String pkg = d.getCurrentPackage();
                if (pkg != null && !AppConstants.APP_PACKAGE.equals(pkg)) {
                    logger.info("Ngoai app (" + pkg + ") -> startActivity keo ve");
                    bringMainActivityToFront();
                    sleep(1200);
                    continue;
                }

                // 1) Da ve Home -> xong.
                if (hp.isHomeScreenDisplayed()) {
                    logger.info("[TIMING] resetToHome: " + (System.currentTimeMillis() - t0) + "ms");
                    return;
                }

                // 1.5) Drawer (hamburger) dang mo -> dong bang closeMenuDrawer (tap lai
                //    hamburger / vuot), KHONG BACK: BACK o drawer chi bung exit dialog ->
                //    loop. Chi trigger khi drawer that su mo (Home/Search khong bao gio vao
                //    nhanh nay vi da dong drawer truoc do).
                if (hp.isMenuDrawerOpen()) {
                    logger.info("Drawer mo -> closeMenuDrawer (khong BACK)");
                    hp.closeMenuDrawer();
                    sleep(1000);
                    continue;
                }

                // 2) Exit dialog dang mo (vd sau test exit, hoac lo BACK o top-level) -> Cancel
                //    de o lai roi bam tab Home ve Home.
                if (hp.isExitDialogDisplayed()) {
                    logger.info("Exit dialog mo -> Cancel + bam tab Home");
                    hp.clickExitCancel();
                    sleep(700);
                    hp.clickBottomNavHome();
                    sleep(1000);
                    continue;
                }

                // 3) Dang o 1 TAB bottom-nav that (tab Home hien) -> bam tab Home ve Home.
                //    KHONG BACK o tab (BACK o tab top-level bung exit dialog -> thoat app).
                if (hp.isHomeTabDisplayed()) {
                    logger.info("Dang o tab bottom-nav (co tab Home) -> bam tab Home");
                    hp.clickBottomNavHome();
                    sleep(1000);
                    continue;
                }

                // 4) Man PUSHED (Settings/Downloaded/full player) hoac SEARCH / dialog / drawer
                //    -> BACK pop ve Home. Rieng man Search can BACK 2 lan (lan 1 dong ban phim,
                //    lan 2 ve Home) -> vong lap se BACK lai o round sau.
                logger.info("Man pushed/search/dialog -> BACK");
                hp.pressBack();
                sleep(1000);
            }

            // Last resort: dua MainActivity len (khong terminate toan app).
            if (!hp.isHomeScreenDisplayed()) {
                logger.info("Van chua ve Home -> startActivity MainActivity (last resort)");
                bringMainActivityToFront();
                sleep(1200);
                ad.handleAdIfAppears(1500);
            }
        } catch (Exception e) {
            logger.warn("Loi resetToHome (" + e.getMessage() + ") -> startActivity");
            bringMainActivityToFront();
        }
        logger.info("[TIMING] resetToHome: " + (System.currentTimeMillis() - t0) + "ms");
    }

    /**
     * Neu exit dialog ("Are you sure you want to exit?") dang mo -> bam Cancel de o lai app.
     * Tra ve true neu dialog co mo (da xu ly), false neu khong mo.
     *
     * Exit dialog co kem 1 NATIVE AD ben trong -> ad load cham/che nut Cancel khien 1 lan tap
     * khong an -> dialog treo (phai cancel tay). Retry tap Cancel toi 3 lan, moi lan verify;
     * lan cuoi fallback BACK.
     */
    protected boolean dismissExitDialogIfOpen(HomePage hp) {
        if (!hp.isExitDialogDisplayed()) return false;
        logger.info("Exit dialog mo -> Cancel (retry phong native ad che nut)");
        for (int i = 0; i < 3; i++) {
            hp.clickExitCancel();
            sleep(800);
            if (!hp.isExitDialogDisplayed()) {
                logger.info("Exit dialog da dong sau " + (i + 1) + " lan Cancel");
                return true;
            }
        }
        logger.warn("Cancel chua dong duoc exit dialog -> fallback BACK");
        hp.pressBack();
        sleep(800);
        return true;
    }

    /**
     * Reset ve man TRACKS LIST (KHONG vong qua Home). Dung cho cac test Tracks de tranh
     * round-trip Tracks->Home->Tracks moi case (cham). Neu DANG o Tracks list -> tra ve ngay.
     * Neu dang o overlay (sheet/dialog/player/queue/select mode) -> BACK pop ve; neu o Home/
     * tab khac -> bam tab Tracks; neu ngoai app -> keo ve. KHONG BACK khi da o Tracks list
     * (tranh bung exit dialog).
     */
    protected void resetToTracks() {
        if (driver == null) return;
        long t0 = System.currentTimeMillis();
        AndroidDriver d = (AndroidDriver) driver;
        AdHelper ad = new AdHelper(d);
        HomePage hp = new HomePage(driver);
        pages.TracksPage tp = new pages.TracksPage(driver);
        try {
            // Fast-path: thuong da o Tracks (vd @AfterMethod test truoc da dua ve) -> return ngay,
            // KHONG poll ad / getCurrentPackage (tiet kiem ~500-700ms moi no-op call).
            // Exit dialog phu len Tracks van lam isOnTracksScreen()==true (Tracks thay phia sau)
            // -> phai chac dialog KHONG mo truoc khi fast-return, neu khong se de dialog treo.
            if (tp.isOnTracksScreen() && !hp.isExitDialogDisplayed()) {
                logger.info("[TIMING] resetToTracks: " + (System.currentTimeMillis() - t0) + "ms (fast)");
                return;
            }
            for (int round = 0; round < 6; round++) {
                ad.handleAdIfAppears(300);

                String pkg = d.getCurrentPackage();
                if (pkg != null && !AppConstants.APP_PACKAGE.equals(pkg)) {
                    bringMainActivityToFront();
                    sleep(1000);
                    continue;
                }
                // Exit dialog mo (sau case Share/Delete: BACK ra man he thong lo ve top-level)
                // -> Cancel chu dong. Xu ly TRUOC isOnTracksScreen vi Tracks van hien phia sau.
                if (dismissExitDialogIfOpen(hp)) {
                    continue;
                }
                if (tp.isOnTracksScreen()) {
                    logger.info("[TIMING] resetToTracks: " + (System.currentTimeMillis() - t0) + "ms");
                    return;
                }
                // O Home hoac tab khac (con bottom nav) -> bam tab Tracks.
                if (hp.isHomeTabDisplayed()) {
                    hp.clickBottomNavTracks();
                    sleep(900);
                    continue;
                }
                // Overlay (sheet/dialog/player/queue/select mode) -> BACK pop ve.
                hp.pressBack();
                sleep(800);
            }
            // Last resort: neu van chua o Tracks -> ve Home roi bam tab Tracks.
            if (!tp.isOnTracksScreen()) {
                resetToHome();
                hp.clickBottomNavTracks();
                sleep(900);
            }
        } catch (Exception e) {
            logger.warn("resetToTracks loi: " + e.getMessage());
        }
        logger.info("[TIMING] resetToTracks: " + (System.currentTimeMillis() - t0) + "ms");
    }

    /**
     * Reset ve man ARTISTS LIST (KHONG vong qua Home). Dung cho cac test Artists de tranh
     * round-trip Artists->Home->Artists moi case (cham). Neu DANG o Artists list -> tra ve ngay.
     * Neu o detail/folder/sheet/player -> BACK pop ve; neu o Home/tab khac -> bam tab Artists;
     * ngoai app -> keo ve.
     */
    protected void resetToArtists() {
        if (driver == null) return;
        long t0 = System.currentTimeMillis();
        AndroidDriver d = (AndroidDriver) driver;
        AdHelper ad = new AdHelper(d);
        HomePage hp = new HomePage(driver);
        pages.ArtistsPage ap = new pages.ArtistsPage(driver);
        try {
            if (ap.isOnArtistsScreen() && !hp.isExitDialogDisplayed()) {
                logger.info("[TIMING] resetToArtists: " + (System.currentTimeMillis() - t0) + "ms (fast)");
                return;
            }
            for (int round = 0; round < 7; round++) {
                ad.handleAdIfAppears(300);

                String pkg = d.getCurrentPackage();
                if (pkg != null && !AppConstants.APP_PACKAGE.equals(pkg)) {
                    // Share resolver / EQ / ngoai app -> BACK truoc, neu van ngoai thi keo ve front.
                    hp.pressBack();
                    sleep(800);
                    pkg = d.getCurrentPackage();
                    if (pkg != null && !AppConstants.APP_PACKAGE.equals(pkg)) {
                        bringMainActivityToFront();
                        sleep(1000);
                    }
                    continue;
                }
                // Exit dialog mo (case Share >10 / BACK lo ve top-level) -> Cancel chu dong.
                if (dismissExitDialogIfOpen(hp)) {
                    continue;
                }
                if (ap.isOnArtistsScreen()) {
                    logger.info("[TIMING] resetToArtists: " + (System.currentTimeMillis() - t0) + "ms");
                    return;
                }
                // Con bottom nav (Home/Tracks/tab khac) -> bam tab Artists.
                if (hp.isHomeTabDisplayed()) {
                    ap.openArtistsFromBottomNav();
                    sleep(900);
                    continue;
                }
                // Overlay (detail/folder/sheet/player) -> BACK pop ve.
                hp.pressBack();
                sleep(800);
            }
            if (!ap.isOnArtistsScreen()) {
                resetToHome();
                ap.openArtistsFromBottomNav();
                sleep(900);
            }
        } catch (Exception e) {
            logger.warn("resetToArtists loi: " + e.getMessage());
        }
        logger.info("[TIMING] resetToArtists: " + (System.currentTimeMillis() - t0) + "ms");
    }

    /**
     * Reset ve man ALBUMS LIST (KHONG vong qua Home). Dung cho cac test Albums (giong
     * resetToArtists). DANG o Albums -> tra ve ngay; o detail/sheet/sort/player -> BACK pop ve;
     * o Home/tab khac -> bam tab Albums; ngoai app (share resolver) -> BACK roi keo ve front.
     */
    protected void resetToAlbums() {
        if (driver == null) return;
        long t0 = System.currentTimeMillis();
        AndroidDriver d = (AndroidDriver) driver;
        AdHelper ad = new AdHelper(d);
        HomePage hp = new HomePage(driver);
        pages.AlbumsPage ap = new pages.AlbumsPage(driver);
        try {
            if (ap.isOnAlbumsScreen() && !hp.isExitDialogDisplayed()) {
                logger.info("[TIMING] resetToAlbums: " + (System.currentTimeMillis() - t0) + "ms (fast)");
                return;
            }
            for (int round = 0; round < 7; round++) {
                ad.handleAdIfAppears(300);

                String pkg = d.getCurrentPackage();
                if (pkg != null && !AppConstants.APP_PACKAGE.equals(pkg)) {
                    hp.pressBack();
                    sleep(800);
                    pkg = d.getCurrentPackage();
                    if (pkg != null && !AppConstants.APP_PACKAGE.equals(pkg)) {
                        bringMainActivityToFront();
                        sleep(1000);
                    }
                    continue;
                }
                if (dismissExitDialogIfOpen(hp)) {
                    continue;
                }
                if (ap.isOnAlbumsScreen()) {
                    logger.info("[TIMING] resetToAlbums: " + (System.currentTimeMillis() - t0) + "ms");
                    return;
                }
                if (hp.isHomeTabDisplayed()) {
                    ap.openAlbumsFromBottomNav();
                    sleep(900);
                    continue;
                }
                hp.pressBack();
                sleep(800);
            }
            if (!ap.isOnAlbumsScreen()) {
                resetToHome();
                ap.openAlbumsFromBottomNav();
                sleep(900);
            }
        } catch (Exception e) {
            logger.warn("resetToAlbums loi: " + e.getMessage());
        }
        logger.info("[TIMING] resetToAlbums: " + (System.currentTimeMillis() - t0) + "ms");
    }

    /**
     * Reset ve man PLAYLISTS LIST (KHONG vong qua Home). Giong resetToAlbums: dang o Playlists ->
     * tra ve ngay; o detail/sheet/dialog/player -> BACK pop ve; o Home/tab khac -> bam tab Playlists;
     * ngoai app (share resolver) -> BACK roi keo ve front.
     */
    protected void resetToPlaylists() {
        if (driver == null) return;
        long t0 = System.currentTimeMillis();
        AndroidDriver d = (AndroidDriver) driver;
        AdHelper ad = new AdHelper(d);
        HomePage hp = new HomePage(driver);
        pages.PlaylistsPage pp = new pages.PlaylistsPage(driver);
        try {
            if (pp.isOnPlaylistsScreen() && !hp.isExitDialogDisplayed()) {
                logger.info("[TIMING] resetToPlaylists: " + (System.currentTimeMillis() - t0) + "ms (fast)");
                return;
            }
            for (int round = 0; round < 7; round++) {
                ad.handleAdIfAppears(300);

                String pkg = d.getCurrentPackage();
                if (pkg != null && !AppConstants.APP_PACKAGE.equals(pkg)) {
                    hp.pressBack();
                    sleep(800);
                    pkg = d.getCurrentPackage();
                    if (pkg != null && !AppConstants.APP_PACKAGE.equals(pkg)) {
                        bringMainActivityToFront();
                        sleep(1000);
                    }
                    continue;
                }
                if (dismissExitDialogIfOpen(hp)) {
                    continue;
                }
                if (pp.isOnPlaylistsScreen()) {
                    logger.info("[TIMING] resetToPlaylists: " + (System.currentTimeMillis() - t0) + "ms");
                    return;
                }
                if (hp.isHomeTabDisplayed()) {
                    pp.openPlaylistsFromBottomNav();
                    sleep(900);
                    continue;
                }
                hp.pressBack();
                sleep(800);
            }
            if (!pp.isOnPlaylistsScreen()) {
                resetToHome();
                pp.openPlaylistsFromBottomNav();
                sleep(900);
            }
        } catch (Exception e) {
            logger.warn("resetToPlaylists loi: " + e.getMessage());
        }
        logger.info("[TIMING] resetToPlaylists: " + (System.currentTimeMillis() - t0) + "ms");
    }

    /**
     * Mo man Search tu Home va dam bao san sang nhap lieu. Luong:
     * <ol>
     *   <li>Bam search bar tren Home; {@link #guardAd} bypass ad mo search neu co.</li>
     *   <li>Cho man Search hien (retry bam search bar neu ad bypass lo day ve Home).</li>
     *   <li>{@code prepareKeyboard=true}: goi {@link SearchPage#ensureKeyboardShown()} —
     *       no-ad thi ban phim da hien; co-ad-bypass thi tap textbox them 1 lan.</li>
     * </ol>
     *
     * @param prepareKeyboard co can dam bao ban phim hien (de go) hay khong
     * @return SearchPage dang o man Search
     */
    protected SearchPage openSearch(boolean prepareKeyboard) {
        HomePage hp = new HomePage(driver);
        SearchPage sp = new SearchPage(driver);
        // Mo search hay BUNG AD. Bypass ad bang ADB BACK co the POP luon man search ve Home, va
        // man search co the hien CHOP NHOANG roi mat -> phai kiem tra search ON DINH moi thoat.
        // Lan click thu 2 (sau khi ad da bypass, qua cooldown) thuong khong bung ad -> vao on dinh.
        for (int attempt = 0; attempt < 5 && !isSearchScreenStable(sp); attempt++) {
            if (!hp.isHomeScreenDisplayed()) {
                resetToHome();   // cold-start / ad-bypass lac man -> dua ve Home that su
            }
            hp.clickSearchBar();
            guardAd(3000);       // ad bung khi mo search -> bypass tai cho
            sp.waitForSearchScreen(5);
        }
        if (prepareKeyboard && sp.isOnSearchScreen()) {
            sp.ensureKeyboardShown();
        }
        return sp;
    }

    /** Man search co ON DINH khong (con tren search sau 800ms, khong phai flicker do ad-bypass). */
    private boolean isSearchScreenStable(SearchPage sp) {
        if (!sp.isOnSearchScreen()) return false;
        sleep(800);
        return sp.isOnSearchScreen();
    }

    /**
     * Reset ve Home tu cac man Search/Results. QUAN TRONG: man ket qua search la WebView nang,
     * nhan DEVICE BACK len no co the lam CRASH UiAutomator2 instrumentation (giong ad WebView)
     * -> session chet, moi test sau fail. Vi vay roi man Search bang NUT BACK IN-APP (Flutter
     * control, an toan) truoc, roi moi {@link #resetToHome()} (luc nay thuong da o Home nen
     * khong can device BACK).
     */
    protected void leaveSearchAndReset() {
        if (driver == null) return;
        try {
            SearchPage sp = new SearchPage(driver);
            for (int i = 0; i < 3 && sp.isOnSearchScreen(); i++) {
                sp.clickBack();      // in-app back arrow (khong phai device BACK)
                guardAd(800);
            }
        } catch (Exception e) {
            logger.warn("leaveSearchAndReset loi: " + e.getMessage());
        }
        resetToHome();
    }

    /**
     * Go tu khoa + submit + cho results, RETRY 1 lan neu chua co (vd ad bung khi mo search
     * lam gian doan trang thai, hoac mang cham). Moi lan: dam bao ban phim, clear, go lai,
     * kiem tra text da vao box chua (chua thi tap + go lai), submit, guard ad, cho results.
     *
     * @return true neu co results trong {@code resultTimeoutSec}
     */
    protected boolean searchWithRetry(SearchPage sp, String query, int resultTimeoutSec) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            sp.ensureKeyboardShown();
            sp.clearTextbox();
            sp.typeQuery(query);
            String v = sp.getTextboxValue();
            if (v == null || v.trim().isEmpty()) {
                // Text chua vao box (ban phim/focus chap chon sau ad) -> tap roi go lai.
                sp.tapSearchBox();
                sp.typeQuery(query);
            }
            sp.submitSearch();
            guardAd(3000);                 // submit cung co the bung ad
            if (sp.waitForResults(resultTimeoutSec)) return true;
            logger.info("Search '" + query + "' lan " + attempt + " chua co results -> thu lai");
        }
        return false;
    }

    /** Dua MainActivity len foreground qua ADB-level startActivity (khong terminate ca app). */
    protected void bringMainActivityToFront() {
        if (driver == null) return;
        try {
            ((AndroidDriver) driver).executeScript("mobile: startActivity",
                    java.util.Map.of("component",
                            AppConstants.APP_PACKAGE + "/" + AppConstants.APP_ACTIVITY,
                            "wait", true));
            // Mo lai app co the kich dialog "Update app" -> dong ngay.
            try {
                new DialogHelper((AndroidDriver) driver).dismissUpdateDialogIfPresent();
            } catch (Exception ignored) {}
        } catch (Exception e) {
            logger.warn("Loi bringMainActivityToFront: " + e.getMessage());
        }
    }

    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
