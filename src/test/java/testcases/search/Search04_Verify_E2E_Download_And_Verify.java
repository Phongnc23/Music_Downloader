package testcases.search;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.DownloadedPage;
import pages.HomePage;
import pages.SearchPage;
import report.ExtentReportManager;

/**
 * E2E: Search -> Download 2 video bat ky trong ket qua -> back ve Home -> verify 2 bai
 * vua tai co trong danh sach Downloaded.
 * TC_SEARCH_E2E_01.
 */
public class Search04_Verify_E2E_Download_And_Verify extends BaseTest {

    private HomePage homePage;
    private SearchPage searchPage;
    private DownloadedPage downloadedPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void initPages() {
        homePage = new HomePage(driver);
        downloadedPage = new DownloadedPage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed(), "Phai bat dau tu Home");
    }

    @Test(description = "E2E happy path: Search -> download 2 tracks -> back ve Home -> verify ca 2 "
            + "xuat hien trong Downloaded (so tracks tang +2; app cho tai trung va hien thi)")
    public void verifyDownloadTwoTracksAppearInDownloadedList() {
        // ===== Step 1: Ghi nhan so track downloaded ban dau =====
        homePage.clickDownloaded();
        guardAd(3000);
        int countBefore = downloadedPage.isOnDownloadedScreen() ? downloadedPage.getTrackCount() : 0;
        if (countBefore < 0) countBefore = 0;
        ExtentReportManager.getTest().log(Status.INFO, "Track count truoc download: " + countBefore);
        resetToHome();
        Assert.assertTrue(homePage.isHomeScreenDisplayed(), "Phai ve Home sau khi xem Downloaded");

        // ===== Step 2: Vao Search + go tu khoa =====
        searchPage = openSearch(true);
        Assert.assertTrue(searchPage.isOnSearchScreen(), "Phai o Search screen");
        Assert.assertTrue(searchWithRetry(searchPage, "son tung", 18),
                "Phai co results sau khi search");

        int resultCount = searchPage.getResultCount();
        Assert.assertTrue(resultCount >= 2, "Can it nhat 2 results de download 2 bai, actual=" + resultCount);

        String track1 = searchPage.getResultTitleByIndex(0);
        String track2 = searchPage.getResultTitleByIndex(1);
        ExtentReportManager.getTest().log(Status.INFO, "Track 1: " + track1 + "\nTrack 2: " + track2);

        // ===== Step 3: Download video 1 =====
        searchPage.clickDownloadByIndex(0);
        guardAd(8000);     // download co the bung ad -> bypass tai cho
        sleep(2000);
        guardAd(2000);
        ExtentReportManager.getTest().log(Status.INFO, "Da kich hoat download video 1");

        // ===== Step 4: Download video 2 =====
        if (!searchPage.hasResults()) {
            sleep(1500);
            guardAd(2000);
        }
        Assert.assertTrue(searchPage.hasResults(),
                "Ket qua search phai con hien de download video 2");
        searchPage.clickDownloadByIndex(1);
        guardAd(8000);
        sleep(2000);
        guardAd(2000);
        ExtentReportManager.getTest().log(Status.INFO, "Da kich hoat download video 2");

        // ===== Step 5: Back ve Home (nut back in-app, tranh device BACK gay crash) =====
        leaveSearchAndReset();
        Assert.assertTrue(homePage.isHomeScreenDisplayed(), "Phai back ve Home thanh cong");

        // ===== Step 6: Verify - app CHO TAI TRUNG va HIEN THI, nen tai 2 video -> count tang +2.
        // Download can THOI GIAN hoan tat (doi khi chua hien ngay) -> POLL: mo lai Downloaded
        // nhieu lan toi khi count >= truoc+2 (toi da ~90s). =====
        int target = countBefore + 2;
        int countAfter = pollDownloadedCountAtLeast(target, 90);
        ExtentReportManager.getTest().log(Status.INFO,
                "Track count sau download: " + countAfter + " (truoc: " + countBefore + ", target: " + target + ")");

        Assert.assertTrue(countAfter >= target,
                "Phai tai them 2 bai (cho download hoan tat): sau (" + countAfter
                        + ") >= truoc+2 (" + target + ")");

        // Soft-check: ghi nhan title 2 bai co xuat hien khong (chi log, khong fail vi list co the
        // can cuon de thay het — phep do chinh la count tang +2 o tren).
        String kw1 = extractKeyword(track1);
        String kw2 = extractKeyword(track2);
        ExtentReportManager.getTest().log(Status.INFO,
                "Video 1 ('" + kw1 + "') trong list: " + downloadedPage.containsTrack(kw1)
                        + " | Video 2 ('" + kw2 + "') trong list: " + downloadedPage.containsTrack(kw2));

        ExtentReportManager.getTest().log(Status.PASS,
                "E2E: tai 2 video -> back Home -> count tang +2 (" + countBefore + " -> " + countAfter + ")");
    }

    /**
     * Poll cho download hoan tat: mo lai man Downloaded nhieu lan (refresh) toi khi so track
     * >= {@code target} hoac het {@code timeoutSec}. Download chay nen + can thoi gian hoan tat
     * nen phai cho/refresh thay vi doc 1 lan.
     */
    private int pollDownloadedCountAtLeast(int target, int timeoutSec) {
        long deadline = System.currentTimeMillis() + timeoutSec * 1000L;
        int count = 0;
        while (System.currentTimeMillis() < deadline) {
            if (!homePage.isHomeScreenDisplayed()) {
                resetToHome();
            }
            homePage.clickDownloaded();
            guardAd(2000);
            if (downloadedPage.isOnDownloadedScreen()) {
                count = downloadedPage.getTrackCount();
                ExtentReportManager.getTest().log(Status.INFO,
                        "Poll Downloaded: count = " + count + " (target " + target + ")");
                if (count >= target) {
                    resetToHome();
                    return count;
                }
            }
            resetToHome();   // ve Home roi cho, lan sau mo lai de refresh list
            sleep(6000);
        }
        return count;
    }

    /** Tach keyword dac trung tu title (vai tu giua, bo ky tu dac biet) de tim trong Downloaded. */
    private String extractKeyword(String title) {
        if (title == null || title.isEmpty()) return "";
        String cleaned = title.replaceAll("[|_\\-]", " ").trim();
        String[] words = cleaned.split("\\s+");
        if (words.length <= 3) return cleaned;
        int mid = words.length / 2;
        return (words[mid - 1] + " " + words[mid] + " " + words[mid + 1]).trim();
    }

    @AfterMethod(alwaysRun = true)
    public void resetAfter() {
        leaveSearchAndReset();
    }
}
