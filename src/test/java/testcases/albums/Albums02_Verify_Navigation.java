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

/**
 * Navigation + AlbumDetail UI. Toi uu: gom cac kiem tra tren CUNG man AlbumDetail vao 1 lan
 * navigate (TC_16/17/18/19/20) thay vi moi case tu clickAlbum + back.
 */
public class Albums02_Verify_Navigation extends BaseTest {

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

    @Test(description = "TC_ALBUMS_06: Click album -> mo AlbumDetail (name + N songs)")
    public void TC_06_click_album_opens_detail() {
        String name = albumsPage.getAlbumName(0);
        int trackCount = albumsPage.getAlbumTrackCount(0);

        albumsPage.clickAlbumByIndex(0);
        Assert.assertTrue(detailPage.isOnAlbumDetailScreen(), "Phai o AlbumDetail");
        int songs = detailPage.getAlbumSongCount();
        ExtentReportManager.getTest().log(Status.PASS,
                "Detail of '" + name + "': " + songs + " songs (was " + trackCount + " tracks)");
        detailPage.clickBack();
    }

    @Test(description = "TC_ALBUMS_16_17_18_19_20: AlbumDetail UI - header/hero/play-shuffle/tracks-section/tracks (gom 1 lan navigate)")
    public void TC_16_20_detail_ui_grouped() {
        albumsPage.clickAlbumByIndex(0);
        Assert.assertTrue(detailPage.isOnAlbumDetailScreen(), "Phai o AlbumDetail");

        // TC_16 header
        Assert.assertTrue(detailPage.isHeaderDisplayed(), "Phai co header Back");
        // TC_17 hero: name + N songs
        String hero = detailPage.getAlbumHeroInfo();
        Assert.assertNotNull(hero, "Hero phai co content-desc");
        Assert.assertTrue(hero.contains("song"), "Hero phai contains 'song'. Actual: " + hero);
        Assert.assertTrue(detailPage.getAlbumSongCount() > 0, "N songs phai > 0");
        // TC_18 Play all + Shuffle
        Assert.assertTrue(detailPage.isPlayAllAndShuffleDisplayed(), "Phai co Play all + Shuffle");
        // TC_19 section Tracks
        Assert.assertTrue(detailPage.isTracksSectionDisplayed(), "Phai co section Tracks");
        // TC_20 co tracks
        Assert.assertTrue(detailPage.getDisplayedTracksCount() > 0, "Album phai co >= 1 track");

        ExtentReportManager.getTest().log(Status.PASS,
                "AlbumDetail UI OK: hero='" + hero + "', tracks=" + detailPage.getDisplayedTracksCount());
        detailPage.clickBack();
    }

    @Test(description = "TC_ALBUMS_21: Back tu AlbumDetail -> AlbumsPage")
    public void TC_21_back_navigation() {
        albumsPage.clickAlbumByIndex(0);
        Assert.assertTrue(detailPage.isOnAlbumDetailScreen());
        detailPage.clickBack();
        Assert.assertTrue(albumsPage.isOnAlbumsScreen(), "Back phai ve Albums");
        ExtentReportManager.getTest().log(Status.PASS, "Back OK");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (detailPage.isOnAlbumDetailScreen()) detailPage.clickBack();
        } catch (Exception ignored) {}
        resetToAlbums();
    }
}
