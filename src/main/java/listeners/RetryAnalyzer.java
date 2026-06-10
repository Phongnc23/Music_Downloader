package listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retry 1 lan cho test FAIL. UA2/Flutter chay run dai hay flaky ngau nhien (stale tree, sheet mo
 * cham, UA2 'socket hang up'). Retry chay LAI sau @AfterMethod + @BeforeMethod (co recoverSessionIfDead
 * + resetToTracks) nen lan 2 thuong co session/man hinh sach -> pass.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger logger = LogManager.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY = 1;
    private int count = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (count < MAX_RETRY) {
            count++;
            logger.warn(">>> RETRY (" + count + "/" + MAX_RETRY + ") test FAIL: "
                    + result.getMethod().getMethodName());
            return true;
        }
        return false;
    }
}
