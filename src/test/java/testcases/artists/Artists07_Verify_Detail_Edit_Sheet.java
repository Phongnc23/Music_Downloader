package testcases.artists;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ArtistDetailPage;
import pages.ArtistsPage;
import pages.HomePage;
import report.ExtentReportManager;

/**
 * Artist edit sheet tren MAN DETAIL (unknownArtists_edit) - 3-dot menu goc tren phai.
 * Theo yc: edit artist phai test DAY DU o ca homeArtists (Artists03) LAN detail (lop nay).
 * Sheet detail giong het home: 4 actions (Play/Queue/Playlist/Share), KHONG co Rename/Delete/FileInfo.
 *
 * TC_ARTISTS_17, 23, 24, 25
 */
public class Artists07_Verify_Detail_Edit_Sheet extends BaseTest {

    private HomePage homePage;
    private ArtistsPage artistsPage;
    private ArtistDetailPage detailPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        artistsPage = new ArtistsPage(driver);
        detailPage = new ArtistDetailPage(driver);
        resetToArtists();
        Assert.assertTrue(artistsPage.isOnArtistsScreen());
        artistsPage.clickArtistByIndex(0);
        Assert.assertTrue(detailPage.isOnArtistDetailScreen(), "Setup: phai o ArtistDetail");
        // Mo artist edit sheet bang 3-dot menu goc tren phai
        detailPage.clickArtistMenu();
    }

    @Test(description = "TC_ARTISTS_17: Click 3-dot menu tren ArtistDetail -> edit sheet mo")
    public void TC_17_detail_3dot_opens_sheet() {
        Assert.assertTrue(detailPage.isEditSheetOpen(),
                "3-dot menu tren detail phai mo artist edit sheet");
        ExtentReportManager.getTest().log(Status.PASS, "Detail 3-dot -> edit sheet OK");
    }

    @Test(description = "TC_ARTISTS_23: Edit sheet detail co 4 actions (giong home, khong Rename/Delete)")
    public void TC_23_detail_sheet_4_actions() {
        Assert.assertTrue(detailPage.isAllArtistActionsDisplayed(),
                "Sheet detail phai co 4 actions: Play / Add to queue / Add to playlist / Share track");
        // KHONG duoc co track-level sheet (Rename/Delete) - day la artist sheet
        Assert.assertFalse(detailPage.isTrackEditSheetOpen(),
                "Artist sheet KHONG duoc co Rename/Delete (do la track sheet)");
        ExtentReportManager.getTest().log(Status.PASS, "Detail sheet = 4 artist actions (giong home) OK");
    }

    @Test(description = "TC_ARTISTS_24: Sheet detail hien dung artist info (name + N songs)")
    public void TC_24_detail_sheet_header_info() {
        String info = detailPage.getSheetArtistInfo();
        Assert.assertNotNull(info, "Sheet header info phai co");
        Assert.assertTrue(info.contains("song"), "Sheet header phai chua 'songs'. Actual: " + info);
        ExtentReportManager.getTest().log(Status.PASS, "Detail sheet header: " + info);
    }

    @Test(description = "TC_ARTISTS_25: Sheet detail dong bang Scrim va BACK")
    public void TC_25_detail_sheet_close() {
        // Scrim
        detailPage.closeEditSheetByScrim();
        Assert.assertFalse(detailPage.isEditSheetOpen(), "Scrim phai dong sheet");
        Assert.assertTrue(detailPage.isOnArtistDetailScreen(), "Van o ArtistDetail sau scrim");

        // Mo lai roi dong bang BACK
        detailPage.clickArtistMenu();
        Assert.assertTrue(detailPage.isEditSheetOpen(), "Mo lai sheet OK");
        detailPage.closeEditSheetByBack();
        Assert.assertFalse(detailPage.isEditSheetOpen(), "BACK phai dong sheet");
        Assert.assertTrue(detailPage.isOnArtistDetailScreen(), "Van o ArtistDetail sau BACK (khong thoat man)");
        ExtentReportManager.getTest().log(Status.PASS, "Detail sheet close by Scrim + BACK OK");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (detailPage.isEditSheetOpen()) detailPage.closeEditSheetByBack();
            detailPage.clickBack();
        } catch (Exception ignored) {}
        resetToArtists();
    }
}
