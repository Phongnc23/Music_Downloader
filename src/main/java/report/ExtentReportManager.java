package report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Quan ly ExtentReports - tao report HTML sau khi chay test.
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            createInstance();
        }
        return extent;
    }

    private static void createInstance() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String reportPath = "reports/extent-report-" + timestamp + ".html";

        new File("reports").mkdirs();

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("Music Downloader Automation Report");
        sparkReporter.config().setReportName("Music Downloader Test Results");
        sparkReporter.config().setEncoding("UTF-8");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("App", "Music Downloader");
        extent.setSystemInfo("Device", "Oppo Pad Neo (Android 14)");
        extent.setSystemInfo("Tester", "BlueSoftware");
    }

    public static synchronized ExtentTest createTest(String name, String description) {
        ExtentTest test = getInstance().createTest(name, description);
        TEST.set(test);
        return test;
    }

    public static ExtentTest getTest() {
        return TEST.get();
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }

    public static void removeTest() {
        TEST.remove();
    }

    private ExtentReportManager() {}
}