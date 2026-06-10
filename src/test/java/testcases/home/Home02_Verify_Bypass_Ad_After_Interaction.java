package testcases.home;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import constants.AppConstants;
import io.appium.java_client.android.AndroidDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import report.ExtentReportManager;

/**
 * TC_02: BYPASS quang cao bung GIUA PHIEN roi CHAY TIEP — KHONG out app, KHONG sleep 45s.
 *
 * <p>App bung ad interstitial khi user thao tac sau ~40s ke tu ad truoc — CO HOAC KHONG
 * (ngau nhien). Class nay duoc xep chay SAU cac test display/nav (Home03, Home05, Home04)
 * trong suite, nen luc nay da qua moc cooldown 40s tu lan bypass ad mo app — thao tac
 * "nhan o tim kiem" co kha nang cao bung ad. Du ad co bung hay khong, {@link #guardAd}
 * deu xu ly: neu CO ad -> bypass tai cho (giu state) roi chay tiep; neu KHONG -> chay tiep
 * ngay. Test KHONG assert "ad phai xuat hien" (vi ad ngau nhien) — chi assert session khong
 * bi gay va van o trong app sau thao tac.
 */
public class Home02_Verify_Bypass_Ad_After_Interaction extends BaseTest {

    /** Cua so cho ad co the bung sau thao tac (rong hon cac test khac vi day la test ad-bypass). */
    private static final long AD_GUARD_WINDOW_MS = 7_000;

    private HomePage homePage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void initHomePage() {
        homePage = new HomePage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed(),
                "Test phai bat dau tu Home screen");
    }

    @Test(description = "TC_02: Tu dong bypass quang cao bung giua phien va chay tiep")
    public void TC_02_auto_bypass_mid_session_ad_and_continue() {
        ExtentReportManager.getTest().log(Status.INFO,
                "Test: thao tac search -> neu ad bung thi bypass tai cho roi chay tiep (khong out app)");

        // 1) Thao tac co kha nang bung ad: nhan o tim kiem.
        homePage.clickSearchBar();

        // 2) GUARD: cho toi 7s xem ad co bung; neu CO -> bypass giu state, neu KHONG -> chay tiep.
        guardAd(AD_GUARD_WINDOW_MS);

        // 3) Sau guard van phai dang o trong app (khong ket o AdActivity / Play Store).
        assertInApp("sau khi nhan search + guard ad");

        // 4) CHAY TIEP: BACK ve man truoc -> guard ad lan nua -> van trong app.
        homePage.pressBack();
        guardAd(3_000);
        assertInApp("sau khi BACK + guard ad");

        ExtentReportManager.getTest().log(Status.PASS,
                "Thao tac giua phien hoan tat — ad (neu co) da duoc tu dong bypass, session khong gay");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfterTest() {
        resetToHome();
    }

    /** Verify dang o dung app va KHONG ket o AdActivity. */
    private void assertInApp(String ctx) {
        AndroidDriver d = (AndroidDriver) driver;
        String pkg = d.getCurrentPackage();
        Assert.assertEquals(pkg, AppConstants.APP_PACKAGE,
                "Phai dang o app (" + ctx + "), nhung foreground = " + pkg);
        String act = null;
        try {
            act = d.currentActivity();
        } catch (Exception ignored) {
        }
        boolean stuckInAd = act != null && (
                act.contains("AdActivity")
                        || act.contains("com.google.android.gms.ads")
                        || act.contains("com.facebook.ads")
                        || act.contains("com.unity3d")
                        || act.contains("ATPortraitActivity")
                        || act.contains("ATLandscapeActivity"));
        Assert.assertFalse(stuckInAd,
                "Khong duoc ket o AdActivity (" + ctx + "). Current: " + act);
        logger.info("OK — dang o app " + ctx + " (activity=" + act + ")");
    }
}
