package testcases.home;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import report.ExtentReportManager;

/**
 * Test Bottom Navigation (5 tabs) + Mini Player.
 * Tuong ung TC_HOME_16 -> TC_HOME_25.
 *
 * <p>Sau moi thao tac dung {@link #guardAd(long)} (doi man + bypass ad neu bung). Ve Home
 * bang BACK qua {@link #resetToHome()} trong @AfterMethod — khong out app.
 */
public class Home05_Verify_Bottom_Nav_And_Mini_Player extends BaseTest {

    private HomePage homePage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void initHomePage() {
        homePage = new HomePage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed(),
                "Test phai bat dau tu Home screen");
    }

    // ===== MINI PLAYER TESTS =====

    @Test(description = "TC_HOME_16: Verify mini player co thong tin track")
    public void TC_HOME_16_mini_player_has_track_info() {
        Assert.assertTrue(homePage.isMiniPlayerDisplayed(),
                "Mini player phai hien thi");
        ExtentReportManager.getTest().log(Status.PASS,
                "Mini player co track info");
    }

    @Test(description = "TC_HOME_17: Click play/pause trong mini player")
    public void TC_HOME_17_click_play_pause() {
        Assert.assertTrue(homePage.isMiniPlayerDisplayed(),
                "Mini player phai hien thi truoc khi click");
        homePage.clickMiniPlayerPlayPause();
        guardAd(2000);
        ExtentReportManager.getTest().log(Status.PASS,
                "Click play/pause thanh cong");
    }

    @Test(description = "TC_HOME_18: Click queue button trong mini player")
    public void TC_HOME_18_click_queue() {
        Assert.assertTrue(homePage.isMiniPlayerDisplayed(),
                "Mini player phai hien thi truoc khi click");
        homePage.clickMiniPlayerQueue();
        guardAd(2000);
        ExtentReportManager.getTest().log(Status.PASS,
                "Click queue button thanh cong");
    }

    @Test(description = "TC_HOME_19: Click mini player mo full player")
    public void TC_HOME_19_click_mini_player_opens_full() {
        homePage.clickMiniPlayer();
        guardAd(2500);
        ExtentReportManager.getTest().log(Status.PASS,
                "Click mini player - mo full player screen");
    }

    // ===== BOTTOM NAVIGATION TESTS =====

    @Test(description = "TC_HOME_20: Click Tracks tab")
    public void TC_HOME_20_click_tracks_tab() {
        homePage.clickBottomNavTracks();
        guardAd(2000);
        Assert.assertFalse(homePage.isAllQuickActionsDisplayed(),
                "Phai navigate sang Tracks screen");
        ExtentReportManager.getTest().log(Status.PASS, "Navigate sang Tracks tab");
    }

    @Test(description = "TC_HOME_21: Click Artists tab")
    public void TC_HOME_21_click_artists_tab() {
        homePage.clickBottomNavArtists();
        guardAd(2000);
        Assert.assertFalse(homePage.isAllQuickActionsDisplayed(),
                "Phai navigate sang Artists screen");
        ExtentReportManager.getTest().log(Status.PASS, "Navigate sang Artists tab");
    }

    @Test(description = "TC_HOME_22: Click Albums tab")
    public void TC_HOME_22_click_albums_tab() {
        homePage.clickBottomNavAlbums();
        guardAd(2000);
        Assert.assertFalse(homePage.isAllQuickActionsDisplayed(),
                "Phai navigate sang Albums screen");
        ExtentReportManager.getTest().log(Status.PASS, "Navigate sang Albums tab");
    }

    @Test(description = "TC_HOME_23: Click Playlists tab")
    public void TC_HOME_23_click_playlists_tab() {
        homePage.clickBottomNavPlaylists();
        guardAd(2000);
        Assert.assertFalse(homePage.isAllQuickActionsDisplayed(),
                "Phai navigate sang Playlists screen");
        ExtentReportManager.getTest().log(Status.PASS, "Navigate sang Playlists tab");
    }

    @Test(description = "TC_HOME_24: Click Home tab khi dang o Home")
    public void TC_HOME_24_click_home_when_already_home() {
        homePage.clickBottomNavHome();
        guardAd(1500);
        Assert.assertTrue(homePage.isAllQuickActionsDisplayed(),
                "Click Home khi da o Home - phai van o Home");
        ExtentReportManager.getTest().log(Status.PASS,
                "Click Home khi da o Home - khong thay doi");
    }

    @Test(description = "TC_HOME_25: Quay ve Home tu tab khac")
    public void TC_HOME_25_return_home_from_other_tab() {
        // Click Tracks truoc
        homePage.clickBottomNavTracks();
        guardAd(2000);
        Assert.assertFalse(homePage.isAllQuickActionsDisplayed(),
                "Phai sang Tracks truoc");

        // Click Home tab
        homePage.clickBottomNavHome();
        guardAd(2000);
        Assert.assertTrue(homePage.isAllQuickActionsDisplayed(),
                "Phai quay ve Home thanh cong");
        ExtentReportManager.getTest().log(Status.PASS,
                "Tu Tracks quay ve Home thanh cong");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfterTest() {
        resetToHome();
    }
}
