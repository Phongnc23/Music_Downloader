package testcases.artists;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ArtistsPage;
import pages.HomePage;
import report.ExtentReportManager;

public class Artists03_Verify_Edit_Sheet extends BaseTest {

    private HomePage homePage;
    private ArtistsPage artistsPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        artistsPage = new ArtistsPage(driver);
        resetToArtists();
        Assert.assertTrue(artistsPage.isOnArtistsScreen());

        // Mo edit sheet cua artist[0]
        artistsPage.clickEditButtonByIndex(0);
    }

    @Test(description = "TC_ARTISTS_06: Click 3-dot artist -> sheet mo")
    public void TC_06_sheet_opens() {
        Assert.assertTrue(artistsPage.isEditSheetOpen(),
                "Edit sheet phai mo");
        ExtentReportManager.getTest().log(Status.PASS, "Sheet mo OK");
    }

    @Test(description = "TC_ARTISTS_19: Sheet co 4 actions (KHONG co Rename/Delete/FileInfo)")
    public void TC_19_four_actions_only() {
        Assert.assertTrue(artistsPage.isAllArtistActionsDisplayed(),
                "Phai co 4 actions: Play/Queue/Playlist/Share");
        ExtentReportManager.getTest().log(Status.PASS, "4 actions OK");
    }

    @Test(description = "TC_ARTISTS_20: Sheet hien artist info (name + N songs)")
    public void TC_20_sheet_header() {
        String info = artistsPage.getSheetArtistInfo();
        Assert.assertNotNull(info, "Sheet header info phai co");
        Assert.assertTrue(info.contains("song"), "Phai contains 'song'");

        int songCount = artistsPage.getSheetArtistSongCount();
        Assert.assertTrue(songCount > 0);
        ExtentReportManager.getTest().log(Status.PASS,
                "Sheet info: " + info);
    }

    @Test(description = "TC_ARTISTS_21: Tap Scrim -> dong sheet")
    public void TC_21_close_by_scrim() {
        artistsPage.closeEditSheetByScrim();
        Assert.assertFalse(artistsPage.isEditSheetOpen(),
                "Sheet phai dong sau Scrim");
        ExtentReportManager.getTest().log(Status.PASS, "Scrim close OK");
    }

    @Test(description = "TC_ARTISTS_22: Press BACK -> dong sheet")
    public void TC_22_close_by_back() {
        artistsPage.closeEditSheetByBack();
        Assert.assertFalse(artistsPage.isEditSheetOpen());
        Assert.assertTrue(artistsPage.isOnArtistsScreen());
        ExtentReportManager.getTest().log(Status.PASS, "BACK close OK");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try { if (artistsPage.isEditSheetOpen()) artistsPage.closeEditSheetByBack(); }
        catch (Exception ignored) {}
        resetToArtists();
    }
}