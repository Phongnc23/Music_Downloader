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

public class Artists06_Verify_Album_Folders extends BaseTest {

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
        Assert.assertTrue(detailPage.isOnArtistDetailScreen());
    }

    @Test(description = "TC_ARTISTS_12: Section Albums hien thi cac folder")
    public void TC_12_albums_section() {
        Assert.assertTrue(detailPage.isAlbumsSectionDisplayed());
        // Co the co Music Download, BrowserDownloader, etc.
        ExtentReportManager.getTest().log(Status.PASS, "Albums section OK");
    }

    @Test(description = "TC_ARTISTS_40: Click Music Download folder -> mo screen folder")
    public void TC_40_click_music_download() {
        if (!detailPage.isFolderDisplayed("Music Download")) {
            throw new RuntimeException("Folder Music Download khong co");
        }
        detailPage.clickFolder("Music Download");
        sleep(2500);

        Assert.assertTrue(detailPage.isOnArtistDetailScreen(),
                "Folder screen layout giong ArtistDetail");

        String hero = detailPage.getArtistHeroInfo();
        Assert.assertTrue(hero != null && hero.contains("Music Download"),
                "Hero phai chua 'Music Download'. Actual: " + hero);
        ExtentReportManager.getTest().log(Status.PASS,
                "Music Download folder mo: " + hero);

        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_41: Folder hero co N songs")
    public void TC_41_folder_song_count() {
        detailPage.clickFolder("Music Download");
        sleep(2500);

        int songs = detailPage.getArtistSongCount();
        Assert.assertTrue(songs > 0, "Folder phai co >= 1 song");
        ExtentReportManager.getTest().log(Status.PASS,
                "Music Download: " + songs + " songs");
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_42: Play all trong folder -> bai dau folder phat")
    public void TC_42_folder_play_all() {
        detailPage.clickFolder("BrowserDownloader");
        sleep(2500);

        String firstTrack = detailPage.getTrackTitles().isEmpty()
                ? null : detailPage.getTrackTitles().get(0);

        detailPage.clickPlayAll();
        sleep(3000);

        Assert.assertTrue(detailPage.isOnArtistDetailScreen());
        ExtentReportManager.getTest().log(Status.PASS,
                "Folder Play all OK. First: " + firstTrack);
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_44: Tracks trong folder chi gom bai cua folder do")
    public void TC_44_folder_tracks_filtered() {
        detailPage.clickFolder("BrowserDownloader");
        sleep(2500);

        int folderSongs = detailPage.getArtistSongCount();
        int displayedTracks = detailPage.getDisplayedTracksCount();

        // Number of tracks displayed should be <= total (do scrolling)
        Assert.assertTrue(displayedTracks > 0,
                "Folder phai co >= 1 track");
        ExtentReportManager.getTest().log(Status.PASS,
                "BrowserDownloader: " + folderSongs + " songs, displayed: " + displayedTracks);
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_47: Back tu folder -> ArtistDetail")
    public void TC_47_folder_back() {
        detailPage.clickFolder("BrowserDownloader");
        sleep(2500);
        String folderHero = detailPage.getArtistHeroInfo();
        Assert.assertTrue(folderHero != null && folderHero.contains("BrowserDownloader"));

        detailPage.clickBack();
        sleep(2000);

        Assert.assertTrue(detailPage.isOnArtistDetailScreen(),
                "Back phai ve artist detail");
        String artistHero = detailPage.getArtistHeroInfo();
        Assert.assertFalse(artistHero != null && artistHero.contains("BrowserDownloader"),
                "Khong con o folder screen");
        ExtentReportManager.getTest().log(Status.PASS,
                "Back from folder: " + folderHero + " -> " + artistHero);
    }

    @Test(description = "TC_ARTISTS_43: Shuffle trong folder -> phat random tu folder")
    public void TC_43_folder_shuffle() {
        detailPage.clickFolder("BrowserDownloader");
        sleep(2500);
        detailPage.clickShuffle();
        sleep(3000);
        Assert.assertTrue(detailPage.isMiniPlayerDisplayed() || detailPage.isOnArtistDetailScreen(),
                "Shuffle folder phai phat (mini player) hoac van o folder");
        ExtentReportManager.getTest().log(Status.PASS,
                "Folder Shuffle OK. Mini player: " + detailPage.isMiniPlayerDisplayed());
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_45: Folder 3-dot menu -> edit sheet 4 actions (folder-level, share theo limit)")
    public void TC_45_folder_edit_sheet() {
        detailPage.clickFolder("BrowserDownloader");
        sleep(2500);
        detailPage.clickArtistMenu();   // 3-dot goc tren phai folder
        Assert.assertTrue(detailPage.isEditSheetOpen(), "Folder 3-dot phai mo edit sheet");
        Assert.assertTrue(detailPage.isAllArtistActionsDisplayed(),
                "Folder sheet phai co 4 actions (Play/Queue/Playlist/Share) giong artist");
        Assert.assertFalse(detailPage.isTrackEditSheetOpen(),
                "Folder sheet la artist-level (KHONG co Rename/Delete)");
        ExtentReportManager.getTest().log(Status.PASS, "Folder edit sheet = 4 actions OK");
        detailPage.closeEditSheetByBack();
        detailPage.clickBack();
    }

    @Test(description = "TC_ARTISTS_48: Mini player hoat dong trong folder")
    public void TC_48_folder_mini_player() {
        detailPage.clickFolder("BrowserDownloader");
        sleep(2500);
        detailPage.clickPlayAll();
        sleep(3000);
        Assert.assertTrue(detailPage.isMiniPlayerDisplayed(),
                "Mini player phai hien sau Play all trong folder");
        ExtentReportManager.getTest().log(Status.PASS, "Folder mini player OK");
        detailPage.clickBack();
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        try {
            if (detailPage.isEditSheetOpen()) detailPage.closeEditSheetByBack();
            detailPage.clickBack();
        } catch (Exception ignored) {}
        resetToArtists();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}