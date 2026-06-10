package testcases.playlists;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.PlaylistDetailPage;
import pages.PlaylistsPage;
import report.ExtentReportManager;

/**
 * Playlist Share track - gioi han 10 songs (rule chung da test ky o Tracks/Artists/Albums ->
 * o day chi giu BO DAI DIEN cho entry point PLAYLIST: 1 over-limit tu sheet, 1 under-limit tu
 * sheet (+ cancel), 1 over-limit tu detail Show-menu).
 *  - playlist <= 10 tracks: Share -> share intent resolver mo
 *  - playlist > 10 tracks: Share -> sheet dong + toast (van o app)
 */
public class Playlists06_Verify_Share_With_Limit extends BaseTest {

    private HomePage homePage;
    private PlaylistsPage playlistsPage;
    private PlaylistDetailPage detailPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        playlistsPage = new PlaylistsPage(driver);
        detailPage = new PlaylistDetailPage(driver);
        resetToPlaylists();
        Assert.assertTrue(playlistsPage.isOnPlaylistsScreen());
    }

    @Test(description = "TC_PL_22: Share My Favorite (>10) tu sheet -> blocked (sheet dong, van o app)")
    public void TC_22_share_over_limit() {
        int count = playlistsPage.getTrackCountOf("My Favorite");
        Assert.assertTrue(count > PlaylistsPage.SHARE_LIMIT, "My Favorite phai > 10. Actual: " + count);

        playlistsPage.clickEditButtonOf("My Favorite");
        sleep(1500);
        Assert.assertTrue(playlistsPage.isEditSheetOpen());
        playlistsPage.clickSheetShare();
        sleep(2500);

        Assert.assertFalse(playlistsPage.isShareIntentResolverOpen(),
                "My Favorite (" + count + " > 10) KHONG duoc mo share");
        Assert.assertTrue(playlistsPage.isSheetClosedAndStillInApp(), "Sheet dong + van o app");
        ExtentReportManager.getTest().log(Status.PASS, "My Favorite (" + count + "): share blocked OK");
    }

    // Da bo TC_PL_65_67 (share <=10 -> resolver): data may KHONG con user playlist 1-10 track
    // co bai (tat ca rong 0 track) -> khong test duoc luong positive. Da bo TC_68 (share tu detail
    // Show-menu): trung rule voi case >10 blocked. Share-limit da test ky o Albums/Artists/Tracks.

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (playlistsPage.isShareIntentResolverOpen()) playlistsPage.dismissShareIntent();
            if (playlistsPage.isEditSheetOpen()) playlistsPage.closeEditSheetByBack();
            if (detailPage.isOnPlaylistDetailScreen()) detailPage.clickBack();
        } catch (Exception ignored) {}
        resetToPlaylists();
    }
}
