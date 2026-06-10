package listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import report.ExtentReportManager;

/**
 * TestNG Listener - log va tao ExtentReport.
 */
public class TestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        logger.info("========== START SUITE: " + context.getName() + " ==========");
    }

    @Override
    public void onFinish(ITestContext context) {
        // DEDUP RETRY: neu 1 method co lan PASSED (retry thanh cong) thi bo cac ket qua FAILED cua
        // chinh no -> Gradle/TestNG khong tinh la build fail. (RetryAnalyzer retry 1 lan moi test.)
        java.util.Set<String> passedNames = new java.util.HashSet<>();
        for (org.testng.ITestResult r : context.getPassedTests().getAllResults()) {
            passedNames.add(r.getMethod().getMethodName());
        }
        java.util.List<org.testng.ITestResult> remove = new java.util.ArrayList<>();
        for (org.testng.ITestResult r : context.getFailedTests().getAllResults()) {
            if (passedNames.contains(r.getMethod().getMethodName())) {
                remove.add(r);
                logger.warn("DEDUP: bo FAILED (da PASS o retry): " + r.getMethod().getMethodName());
            }
        }
        for (org.testng.ITestResult r : remove) {
            context.getFailedTests().removeResult(r);
        }
        logger.info("========== END SUITE: " + context.getName() + " ==========");
        ExtentReportManager.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        if (description == null || description.isEmpty()) {
            description = testName;
        }

        logger.info(">>> START TEST: " + testName);
        ExtentTest test = ExtentReportManager.createTest(testName, description);
        test.log(Status.INFO, "Test started: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("<<< PASSED: " + testName);

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.PASS, "Test passed: " + testName);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        logger.error("<<< FAILED: " + testName, throwable);

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.FAIL, "Test failed: " + throwable.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.warn("<<< SKIPPED: " + testName);

        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.SKIP, "Test skipped: " + testName);
        }
    }
}