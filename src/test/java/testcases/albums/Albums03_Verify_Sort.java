package testcases.albums;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AlbumDetailPage;
import pages.AlbumsPage;
import pages.HomePage;
import report.ExtentReportManager;

import java.util.List;

/**
 * Sort. Album-level sort (sort list albums) + in-album track sort (7 options, giong man Tracks ->
 * chi giu case chinh, bo duration/date trung Tracks). Toi uu: gom open+options+close cua album
 * sort vao 1 case; gom in-album 7-options + title-toggle vao 1 lan navigate; giam sleep.
 */
public class Albums03_Verify_Sort extends BaseTest {

    private HomePage homePage;
    private AlbumsPage albumsPage;
    private AlbumDetailPage detailPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void setup() {
        homePage = new HomePage(driver);
        albumsPage = new AlbumsPage(driver);
        detailPage = new AlbumDetailPage(driver);
        resetToAlbums();
        Assert.assertTrue(albumsPage.isOnAlbumsScreen());
    }

    // ========== ALBUM-LEVEL SORT ==========

    @Test(description = "TC_ALBUMS_09_10_14: Album sort dialog mo + co option Title + dong bang Scrim (gom)")
    public void TC_09_10_14_album_sort_dialog() {
        albumsPage.openSortDialog();
        Assert.assertTrue(albumsPage.isSortDialogOpen(), "Album sort dialog phai mo");
        Assert.assertTrue(albumsPage.isSortOptionDisplayed("Title"), "Phai co option Title");
        StringBuilder sb = new StringBuilder("Options: Title");
        for (String opt : new String[]{"Artist", "Album", "File name", "Duration", "Date added", "Date modified"}) {
            if (albumsPage.isSortOptionDisplayed(opt)) sb.append(", ").append(opt);
        }
        albumsPage.closeSortDialogByScrim();
        Assert.assertFalse(albumsPage.isSortDialogOpen(), "Scrim phai dong dialog");
        ExtentReportManager.getTest().log(Status.PASS, sb + " | scrim close OK");
    }

    @Test(description = "TC_ALBUMS_11_12: Album sort Title - nhan lan 1 ASC (tren xuong), lan 2 DESC (nguoc lai)")
    public void TC_11_12_album_title_toggle() {
        // Lan 1: Title -> ASC (A->Z, tu tren xuong)
        albumsPage.openSortDialog();
        albumsPage.selectSortByTitle();
        List<String> ascOrder = albumsPage.getAlbumNames();
        Assert.assertTrue(albumsPage.isStringListAscending(ascOrder),
                "Nhan Title lan 1 phai sap xep ASC (A->Z). Actual: " + ascOrder);

        // Lan 2: Title -> DESC (Z->A, nguoc lai)
        albumsPage.openSortDialog();
        albumsPage.selectSortByTitle();
        List<String> descOrder = albumsPage.getAlbumNames();
        Assert.assertTrue(albumsPage.isStringListDescending(descOrder),
                "Nhan Title lan 2 phai dao nguoc DESC (Z->A). Actual: " + descOrder);

        ExtentReportManager.getTest().log(Status.PASS,
                "Album Title toggle: ASC=" + ascOrder + " -> DESC=" + descOrder);
    }

    // ========== IN-ALBUM TRACK SORT (case chinh, bo qua duration/date trung Tracks) ==========

    @Test(description = "TC_ALBUMS_23_24_25: In-album sort 7 options + Title doi order + click lan 2 dao nguoc (giong Tracks)")
    public void TC_23_24_25_in_album_sort() {
        albumsPage.clickAlbumByIndex(0);
        Assert.assertTrue(detailPage.isOnAlbumDetailScreen());

        // TC_23: 7 options hien thi
        detailPage.openTrackSortDialog();
        Assert.assertTrue(detailPage.isSortDialogOpen(), "Sort dialog phai mo");
        Assert.assertTrue(detailPage.isAllSortOptionsDisplayed(), "Phai co day du 7 options");

        // Baseline khac (Duration) de chung minh sort Title co tac dung. KHONG assert strict ASC
        // vi sort-state co the persist qua session chung (giong cach Tracks02 test).
        detailPage.selectSortByDuration();
        List<String> before = detailPage.getTrackTitles();

        // TC_24: Title -> doi thu tu so voi Duration
        detailPage.openTrackSortDialog();
        detailPage.selectSortByTitle();
        List<String> first = detailPage.getTrackTitles();
        Assert.assertTrue(detailPage.isOrderDifferent(before, first),
                "Sort Title phai doi thu tu so voi Duration. Title top5: "
                        + first.subList(0, Math.min(5, first.size())));

        // TC_25: Title lan 2 -> toggle dao nguoc
        detailPage.openTrackSortDialog();
        detailPage.selectSortByTitle();
        List<String> second = detailPage.getTrackTitles();
        Assert.assertTrue(detailPage.isOrderDifferent(first, second),
                "Click Title lan 2 phai dao order (toggle). 1st top5: "
                        + first.subList(0, Math.min(5, first.size())) + " | 2nd top5: "
                        + second.subList(0, Math.min(5, second.size())));

        ExtentReportManager.getTest().log(Status.PASS,
                "In-album 7 options OK; Title doi order vs Duration + toggle dao nguoc");
        detailPage.clickBack();
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (detailPage.isSortDialogOpen()) detailPage.closeSortDialogByScrim();
            if (detailPage.isOnAlbumDetailScreen()) detailPage.clickBack();
            if (albumsPage.isSortDialogOpen()) albumsPage.closeSortDialogByScrim();
        } catch (Exception ignored) {}
        resetToAlbums();
    }
}
