package base;

import constants.AppConstants;
import driver.DriverFactory;
import driver.DriverManager;
import helpers.AdHelper;
import helpers.DialogHelper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import report.ExtentReportManager;

public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected AppiumDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        logger.info("========== SETUP TEST CLASS ==========");
        driver = DriverFactory.createDriver();
        DriverManager.setDriver(driver);

        // Force COLD START: terminate roi activate lai de quang cao interstitial fire
        // on dinh moi lan chay. Neu app dang o trang thai warm (van mo o home tu lan
        // truoc) thi Appium chi resume -> ad khong hien -> flow bypass khong deterministic.
        try {
            ((AndroidDriver) driver).terminateApp(AppConstants.APP_PACKAGE);
            ((AndroidDriver) driver).activateApp(AppConstants.APP_PACKAGE);
        } catch (Exception e) {
            logger.warn("Cold start relaunch loi: " + e.getMessage());
        }
        logger.info("App da launch (cold start): " + AppConstants.APP_PACKAGE);
    }

    /**
     * Truoc moi test:
     *   1. Dismiss quang cao (AdHelper)
     *   2. Dismiss dialog Update neu co (DialogHelper)
     */
    @BeforeMethod(alwaysRun = true)
    public void handleAdBeforeTest() {
        // Step 1: Tat quang cao
        long t0 = System.currentTimeMillis();
        try {
            new AdHelper((AndroidDriver) driver).dismissAllAds();
        } catch (Exception e) {
            logger.warn("Loi xu ly quang cao: " + e);
            e.printStackTrace(System.out);
        }
        long t1 = System.currentTimeMillis();
        logger.info("[TIMING] dismissAllAds: " + (t1 - t0) + "ms");

        // Step 2: Tat dialog Update/Welcome xuat hien sau khi quang cao bien
        try {
            new DialogHelper((AndroidDriver) driver).dismissDialog();
        } catch (Exception e) {
            logger.warn("Loi xu ly dialog: " + e);
            e.printStackTrace(System.out);
        }
        long t2 = System.currentTimeMillis();
        logger.info("[TIMING] dismissDialog: " + (t2 - t1) + "ms");
    }

    @AfterMethod(alwaysRun = true)
    public void afterEachTest(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("Test FAILED: " + result.getName());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("Test PASSED: " + result.getName());
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        logger.info("========== TEARDOWN ==========");
        if (driver != null) {
            // Da vao home (test xong) -> cho 2s roi dong app luon (theo yeu cau: xem
            // home 2s roi quit). Khong xu ly gi them, chi delay ngan cho de quan sat.
            try {
                Thread.sleep(2_000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            long t0 = System.currentTimeMillis();
            try {
                ((AndroidDriver) driver).terminateApp(AppConstants.APP_PACKAGE);
            } catch (Exception e) {
                logger.warn("Loi terminate: " + e.getMessage());
            }
            long t1 = System.currentTimeMillis();
            logger.info("[TIMING] terminateApp: " + (t1 - t0) + "ms");
            driver.quit();
            logger.info("[TIMING] driver.quit: " + (System.currentTimeMillis() - t1) + "ms");
            DriverManager.removeDriver();
        }
        ExtentReportManager.flush();
    }
}