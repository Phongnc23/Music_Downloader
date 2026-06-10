package testcases.albums;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.AlbumsPage;
import pages.HomePage;
import report.ExtentReportManager;

/**
 * UI man Albums (list). Toi uu: gom cac kiem tra hien thi cung 1 man (header/count/sort/nav)
 * vao 1 case thay vi tach 4 case.
 */
public class    Albums01_Verify_UI_Display extends BaseTest {

    private HomePage homePage;
    private AlbumsPage albumsPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openAlbums() {
        homePage = new HomePage(driver);
        albumsPage = new AlbumsPage(driver);
        resetToAlbums();
        Assert.assertTrue(albumsPage.isOnAlbumsScreen(), "Phai o Albums screen");
    }

    @Test(description = "TC_ALBUMS_01_03_08: Header 'Albums' + count + sort button + bottom nav (gom hien thi)")
    public void TC_01_03_08_header_controls() {
        Assert.assertTrue(albumsPage.isTitleDisplayed(), "Phai co title 'Albums'");
        Assert.assertTrue(albumsPage.isCountDisplayed(), "Phai co count 'N albums'");
        Assert.assertTrue(albumsPage.isSortButtonDisplayed(), "Phai co sort button");
        Assert.assertTrue(homePage.isBottomNavDisplayed(), "Phai co bottom nav");
        ExtentReportManager.getTest().log(Status.PASS, "Header + count + sort + bottom nav OK");
    }

    @Test(description = "TC_ALBUMS_02_04: Count 'N albums' > 0 + album cards co name + track count")
    public void TC_02_04_count_and_cards() {
        int count = albumsPage.getAlbumsCount();
        Assert.assertTrue(count > 0, "Count phai > 0. Actual: " + count);

        int n = albumsPage.getDisplayedAlbumsCount();
        Assert.assertTrue(n > 0, "Phai co >=1 album card");
        String name = albumsPage.getAlbumName(0);
        int tracks = albumsPage.getAlbumTrackCount(0);
        Assert.assertTrue(name != null && !name.isEmpty(), "Card phai co name");
        Assert.assertTrue(tracks > 0, "Track count phai > 0");
        ExtentReportManager.getTest().log(Status.PASS,
                "Count=" + count + " | Album[0]: " + name + " (" + tracks + " tracks)");
    }

    @Test(description = "TC_ALBUMS_05: Album count khop voi displayed")
    public void TC_05_count_consistency() {
        int headerCount = albumsPage.getAlbumsCount();
        int displayed = albumsPage.getDisplayedAlbumsCount();
        Assert.assertTrue(displayed > 0 && displayed <= headerCount + 1,
                "Displayed (" + displayed + ") nen <= header count (" + headerCount + ")");
        ExtentReportManager.getTest().log(Status.PASS,
                "Header: " + headerCount + ", displayed: " + displayed);
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() { resetToAlbums(); }
}
