package testcases.playlists;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.PlaylistsPage;
import report.ExtentReportManager;

/**
 * UI Playlists list. Tat ca check deu tren CUNG 1 man -> gom vao 2 case (1 navigation/case nho
 * thay vi 8). resetToPlaylists giu nguyen man giua cac test (khong vong qua Home).
 */
public class Playlists01_Verify_UI_Display extends BaseTest {

    private HomePage homePage;
    private PlaylistsPage playlistsPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openPlaylists() {
        homePage = new HomePage(driver);
        playlistsPage = new PlaylistsPage(driver);
        resetToPlaylists();
        Assert.assertTrue(playlistsPage.isOnPlaylistsScreen(), "Phai o Playlists screen");
    }

    @Test(description = "TC_PL_01: Header + Local label + Create button + Bottom nav (gom)")
    public void TC_01_ui_elements() {
        Assert.assertTrue(playlistsPage.isTitleDisplayed(), "Header 'Playlists'");
        Assert.assertTrue(playlistsPage.isLocalLabelDisplayed(), "Section 'Local playlist'");
        Assert.assertTrue(playlistsPage.isCreateButtonDisplayed(), "Nut 'Create new playlist'");
        Assert.assertTrue(homePage.isBottomNavDisplayed(), "Bottom nav");
        ExtentReportManager.getTest().log(Status.PASS,
                "Header + Local label + Create button + Bottom nav OK");
    }

    @Test(description = "TC_PL_02: Local playlists (My Favorite/Recently Played) + counts + user playlists")
    public void TC_02_sections_and_counts() {
        Assert.assertTrue(playlistsPage.isMyFavoriteDisplayed(), "My Favorite hien thi");
        Assert.assertTrue(playlistsPage.isRecentlyPlayedDisplayed(), "Recently Played hien thi");
        Assert.assertTrue(playlistsPage.isMyPlaylistLabelDisplayed(), "Label 'My playlist (N)'");

        int fav = playlistsPage.getTrackCountOf("My Favorite");
        int recent = playlistsPage.getTrackCountOf("Recently Played");
        int myCount = playlistsPage.getMyPlaylistCount();
        int total = playlistsPage.getAllPlaylistNames().size();
        Assert.assertTrue(myCount >= 0, "My playlist count phai >= 0");
        Assert.assertTrue(total >= 2, "Phai co >= 2 playlist (Local + user). Actual: " + total);

        ExtentReportManager.getTest().log(Status.PASS,
                "My Favorite=" + fav + " tracks, Recently Played=" + recent
                        + " tracks, My playlist(" + myCount + "), tong items=" + total);
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() { resetToPlaylists(); }
}
