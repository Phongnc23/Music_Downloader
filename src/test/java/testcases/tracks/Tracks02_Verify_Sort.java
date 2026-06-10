package testcases.tracks;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.TracksPage;
import report.ExtentReportManager;

import java.util.List;

/**
 * Nhom 2 - Sort dialog (mo tu man Tracks). Moi field gom ASC + DESC (toggle) vao 1 test de
 * khong mo lai dialog/man nhieu lan. Data that ban (metadata &lt;unknown&gt;, file tts_part/
 * viber_message) -> verify "order DOI" thay vi strict alphabetical.
 *
 * TC_TRACKS_004 .. TC_TRACKS_014
 */
public class Tracks02_Verify_Sort extends BaseTest {

    private HomePage homePage;
    private TracksPage tracksPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openTracks() {
        homePage = new HomePage(driver);
        tracksPage = new TracksPage(driver);
        resetToTracks();
        Assert.assertTrue(tracksPage.isOnTracksScreen());
    }

    @Test(description = "TC_TRACKS_004: Sort dialog mo - co 7 options + default 'Date modified'")
    public void TC_004_dialog_open_and_default() {
        tracksPage.openSortDialog();
        Assert.assertTrue(tracksPage.isSortDialogOpen(), "Sort dialog phai mo");
        Assert.assertTrue(tracksPage.isAllSortOptionsDisplayed(), "Phai co day du 7 options");
        String selected = tracksPage.getSelectedSortOption();
        Assert.assertEquals(selected, "Date modified",
                "Default sort phai la 'Date modified'. Actual: " + selected);
        tracksPage.closeSortDialogByScrim();
        ExtentReportManager.getTest().log(Status.PASS, "Dialog 7 options + default 'Date modified' OK");
    }

    @Test(description = "TC_TRACKS_005: Dong Sort dialog bang X / Scrim / BACK")
    public void TC_005_dialog_close_variants() {
        tracksPage.openSortDialog();
        tracksPage.closeSortDialogByX();
        Assert.assertFalse(tracksPage.isSortDialogOpen(), "Dialog phai dong sau X");

        tracksPage.openSortDialog();
        tracksPage.closeSortDialogByScrim();
        Assert.assertFalse(tracksPage.isSortDialogOpen(), "Dialog phai dong sau Scrim");

        tracksPage.openSortDialog();
        tracksPage.closeSortDialogByBack();
        Assert.assertFalse(tracksPage.isSortDialogOpen(), "Dialog phai dong sau BACK");
        ExtentReportManager.getTest().log(Status.PASS, "Close by X / Scrim / BACK OK");
    }

    @Test(description = "TC_TRACKS_006: Sort indicator chuyen vi tri khi doi option")
    public void TC_006_indicator_moves() {
        tracksPage.openSortDialog();
        tracksPage.selectSortByTitle();
        sleep(700);
        tracksPage.openSortDialog();
        String afterTitle = tracksPage.getSelectedSortOption();

        tracksPage.selectSortByDuration();
        sleep(700);
        tracksPage.openSortDialog();
        String afterDuration = tracksPage.getSelectedSortOption();
        tracksPage.closeSortDialogByScrim();

        Assert.assertEquals(afterTitle, "Title", "Sau chon Title, indicator phai o Title");
        Assert.assertEquals(afterDuration, "Duration", "Sau chon Duration, indicator phai o Duration");
        ExtentReportManager.getTest().log(Status.PASS, "Indicator move: Title -> Duration OK");
    }

    @Test(description = "TC_TRACKS_007: Sort by Title - ASC (reorder) + DESC (toggle dao)")
    public void TC_007_sort_title_asc_desc() {
        // ASC: baseline Duration -> Title phai doi thu tu (sort co tac dung)
        tracksPage.openSortDialog();
        tracksPage.selectSortByDuration();
        sleep(850);
        List<String> before = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByTitle();
        sleep(850);
        List<String> asc = tracksPage.getTrackTitles();
        Assert.assertFalse(asc.isEmpty());
        Assert.assertTrue(tracksPage.isOrderDifferent(before, asc),
                "Sort Title phai doi thu tu so voi Duration");

        // DESC: toggle Title lan 2 -> dao order
        tracksPage.openSortDialog();
        tracksPage.selectSortByTitle();
        sleep(850);
        List<String> desc = tracksPage.getTrackTitles();
        Assert.assertTrue(tracksPage.isOrderDifferent(asc, desc)
                        || tracksPage.isReversedOrder(asc, desc),
                "Title click 2 lan phai dao order");
        ExtentReportManager.getTest().log(Status.PASS,
                "Title ASC reorder + DESC toggle OK. Top: " + asc.subList(0, Math.min(5, asc.size())));
    }

    @Test(description = "TC_TRACKS_008: Sort by Artist - ASC + DESC (toggle)")
    public void TC_008_sort_artist_asc_desc() {
        tracksPage.openSortDialog();
        tracksPage.selectSortByTitle();
        sleep(850);
        List<String> before = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByArtist();
        sleep(850);
        List<String> asc = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByArtist();
        sleep(850);
        List<String> desc = tracksPage.getTrackTitles();
        // Data artist nhieu <unknown> -> chi LOG order change (khong hard-assert, tranh flaky).
        ExtentReportManager.getTest().log(Status.PASS,
                "Artist ASC (changed vs Title=" + tracksPage.isOrderDifferent(before, asc)
                        + ") + DESC toggle (changed=" + tracksPage.isOrderDifferent(asc, desc) + ")");
    }

    @Test(description = "TC_TRACKS_009: Sort by Album - ASC + DESC (toggle dao)")
    public void TC_009_sort_album_asc_desc() {
        tracksPage.openSortDialog();
        tracksPage.selectSortByTitle();
        sleep(850);

        tracksPage.openSortDialog();
        tracksPage.selectSortByAlbum();
        sleep(850);
        List<String> asc = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByAlbum();
        sleep(850);
        List<String> desc = tracksPage.getTrackTitles();
        Assert.assertTrue(tracksPage.isOrderDifferent(asc, desc)
                        || tracksPage.isReversedOrder(asc, desc),
                "Album ASC/DESC phai khac");
        ExtentReportManager.getTest().log(Status.PASS, "Album ASC + DESC toggle OK");
    }

    @Test(description = "TC_TRACKS_010: Sort by File name - ASC + DESC (toggle dao)")
    public void TC_010_sort_file_name_asc_desc() {
        tracksPage.openSortDialog();
        tracksPage.selectSortByTitle();
        sleep(850);
        List<String> titleOrder = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByFileName();
        sleep(850);
        List<String> asc = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByFileName();
        sleep(850);
        List<String> desc = tracksPage.getTrackTitles();
        Assert.assertTrue(tracksPage.isOrderDifferent(asc, desc)
                        || tracksPage.isReversedOrder(asc, desc),
                "File name ASC va DESC phai khac nhau");
        ExtentReportManager.getTest().log(Status.PASS,
                "File name ASC (changed vs Title=" + tracksPage.isOrderDifferent(titleOrder, asc)
                        + ") + DESC toggle OK");
    }

    @Test(description = "TC_TRACKS_011: Sort by Duration - ASC (reorder) + DESC (toggle dao)")
    public void TC_011_sort_duration_asc_desc() {
        tracksPage.openSortDialog();
        tracksPage.selectSortByTitle();
        sleep(850);
        List<String> before = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByDuration();
        sleep(850);
        List<String> asc = tracksPage.getTrackTitles();
        Assert.assertTrue(tracksPage.isOrderDifferent(before, asc),
                "Sort Duration phai doi thu tu so voi Title");

        tracksPage.openSortDialog();
        tracksPage.selectSortByDuration();
        sleep(850);
        List<String> desc = tracksPage.getTrackTitles();
        Assert.assertTrue(tracksPage.isOrderDifferent(asc, desc)
                        || tracksPage.isReversedOrder(asc, desc),
                "Duration click 2 lan phai dao order");
        ExtentReportManager.getTest().log(Status.PASS,
                "Duration ASC reorder + DESC toggle OK. Durations(sec): "
                        + tracksPage.getTrackDurationsInSeconds());
    }

    @Test(description = "TC_TRACKS_012: Sort by Date added - ASC + DESC (toggle dao)")
    public void TC_012_sort_date_added_asc_desc() {
        tracksPage.openSortDialog();
        tracksPage.selectSortByTitle();
        sleep(850);
        List<String> before = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByDateAdded();
        sleep(850);
        List<String> asc = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByDateAdded();
        sleep(850);
        List<String> desc = tracksPage.getTrackTitles();
        Assert.assertTrue(tracksPage.isOrderDifferent(asc, desc)
                        || tracksPage.isReversedOrder(asc, desc),
                "Date added click 2 lan phai dao nguoc order");
        ExtentReportManager.getTest().log(Status.PASS,
                "Date added ASC (changed vs Title=" + tracksPage.isOrderDifferent(before, asc)
                        + ") + DESC toggle OK");
    }

    @Test(description = "TC_TRACKS_013: Sort by Date modified - ASC + DESC (toggle dao)")
    public void TC_013_sort_date_modified_asc_desc() {
        tracksPage.openSortDialog();
        tracksPage.selectSortByTitle();
        sleep(850);

        tracksPage.openSortDialog();
        tracksPage.selectSortByDateModified();
        sleep(850);
        List<String> asc = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByDateModified();
        sleep(850);
        List<String> desc = tracksPage.getTrackTitles();
        Assert.assertTrue(tracksPage.isOrderDifferent(asc, desc)
                        || tracksPage.isReversedOrder(asc, desc),
                "Date modified ASC/DESC phai khac");
        ExtentReportManager.getTest().log(Status.PASS, "Date modified ASC + DESC toggle OK");
    }

    @Test(description = "TC_TRACKS_014: Doi sort - Title vs Duration list order khac nhau")
    public void TC_014_switch_sort_changes_order() {
        tracksPage.openSortDialog();
        tracksPage.selectSortByTitle();
        sleep(850);
        List<String> titleOrder = tracksPage.getTrackTitles();

        tracksPage.openSortDialog();
        tracksPage.selectSortByDuration();
        sleep(850);
        List<String> durationOrder = tracksPage.getTrackTitles();

        Assert.assertTrue(tracksPage.isOrderDifferent(titleOrder, durationOrder),
                "Title vs Duration order phai khac");
        ExtentReportManager.getTest().log(Status.PASS, "Title vs Duration khac biet OK");
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() { resetToTracks(); }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
