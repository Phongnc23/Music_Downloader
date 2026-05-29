package base;

import constants.AppConstants;
import driver.DriverFactory;
import driver.DriverManager;
import helpers.AdHelper;
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
        logger.info("App da launch: " + AppConstants.APP_PACKAGE);
    }

    @BeforeMethod(alwaysRun = true)
    public void handleAdBeforeTest() {
        try {
            new AdHelper((AndroidDriver) driver).dismissAllAds();
        } catch (Exception e) {
            logger.warn("Loi xu ly quang cao: " + e.getMessage());
        }
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
            try {
                ((AndroidDriver) driver).terminateApp(AppConstants.APP_PACKAGE);
            } catch (Exception e) {
                logger.warn("Loi terminate: " + e.getMessage());
            }
            driver.quit();
            DriverManager.removeDriver();
        }
        ExtentReportManager.flush();
    }
}