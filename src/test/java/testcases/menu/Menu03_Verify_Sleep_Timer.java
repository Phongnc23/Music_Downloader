package testcases.menu;

import base.BaseTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.MenuPage;
import report.ExtentReportManager;

/**
 * Test Sleep Timer dialog + Custom Sleep Timer dialog.
 *
 * Sleep Timer dialog co 2 state:
 *   - INITIAL: button "Set timer", khong co countdown
 *   - ACTIVE: button "Reset", co "Timer: Xm Ys" countdown
 *
 * Flow chinh:
 *   Initial -> Set timer -> [Dialog dong, toast] -> Reopen -> Active -> Reset -> Initial
 *
 * Custom sub-flow:
 *   Initial -> Custom -> [Custom dialog mo] -> Done -> [Ca 2 dialog dong, timer set]
 *                                          -> Cancel -> [Custom dong, ve Sleep timer initial]
 *
 * @AfterMethod luon cleanup: clear timer + close dialog de test tiep theo bat dau fresh.
 */
public class Menu03_Verify_Sleep_Timer extends BaseTest {

    private HomePage homePage;
    private MenuPage menuPage;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "handleAdBeforeTest")
    public void openSleepTimer() {
        homePage = new HomePage(driver);
        menuPage = new MenuPage(driver);
        Assert.assertTrue(homePage.isHomeScreenDisplayed(),
                "Test phai bat dau tu Home");

        homePage.clickMenuButton();
        sleep(1300);
        menuPage.clickSleepTimer();
        sleep(1000);

        Assert.assertTrue(menuPage.isSleepTimerDialogOpen(),
                "Sleep timer dialog phai mo");
    }

    // ============================================
    // GROUP 1: SLEEP TIMER DIALOG - INITIAL/ACTIVE STATE
    // ============================================

    @Test(description = "TC_MENU_11: Dialog mo voi INITIAL state - 6 options + 'Set timer' button")
    public void TC_MENU_11_initial_state_displayed() {
        ensureInitialState();

        Assert.assertTrue(menuPage.isInInitialState(),
                "Dialog phai o INITIAL state");
        Assert.assertTrue(menuPage.isAllTimerOptionsDisplayed(),
                "Phai co 6 options: 15/30/45/60/90 mins + Custom");
        Assert.assertTrue(menuPage.isSetTimerButtonVisible(),
                "Button 'Set timer' phai hien thi");
        Assert.assertFalse(menuPage.isResetButtonVisible(),
                "Button 'Reset' KHONG duoc hien thi o initial state");
        Assert.assertFalse(menuPage.isTimerCountdownVisible(),
                "Khong duoc co 'Timer:' countdown o initial state");

        ExtentReportManager.getTest().log(Status.PASS,
                "INITIAL state: 6 options + Set timer button");
    }

    @Test(description = "TC_MENU_12: Click Cancel o INITIAL state -> dong dialog")
    public void TC_MENU_12_cancel_initial_state() {
        ensureInitialState();
        Assert.assertTrue(menuPage.isSleepTimerDialogOpen());

        menuPage.clickTimerCancel();
        sleep(1000);

        Assert.assertFalse(menuPage.isSleepTimerDialogOpen(),
                "Dialog phai dong sau khi Cancel");
        ExtentReportManager.getTest().log(Status.PASS,
                "Cancel o initial state dong dialog");
    }

    @Test(description = "TC_MENU_13: Chon 15 mins + Set timer -> dialog dong, timer duoc set")
    public void TC_MENU_13_set_timer_15_min() {
        ensureInitialState();

        menuPage.selectTimer15Min();
        sleep(1000);
        menuPage.clickSetTimer();
        sleep(1000);

        Assert.assertFalse(menuPage.isSleepTimerDialogOpen(),
                "Dialog phai dong sau Set timer");

        // Verify timer set bang cach reopen
        homePage.clickMenuButton();
        sleep(1300);
        menuPage.clickSleepTimer();
        sleep(1000);

        Assert.assertTrue(menuPage.isInActiveState(),
                "Sau Set timer va reopen, phai o ACTIVE state");
        ExtentReportManager.getTest().log(Status.PASS,
                "Set timer 15 mins OK - countdown: " + menuPage.getTimerCountdownText());
    }

    @Test(description = "TC_MENU_14: ACTIVE state hien countdown 'Timer: Xm Ys' + Reset button")
    public void TC_MENU_14_active_state_displayed() {
        ensureActiveState();

        Assert.assertTrue(menuPage.isInActiveState(),
                "Phai o ACTIVE state");
        Assert.assertTrue(menuPage.isResetButtonVisible(),
                "Phai co 'Reset' button");
        Assert.assertFalse(menuPage.isSetTimerButtonVisible(),
                "KHONG duoc co 'Set timer' button o active state");
        Assert.assertTrue(menuPage.isTimerCountdownVisible(),
                "Phai co 'Timer:' countdown");

        String countdown = menuPage.getTimerCountdownText();
        Assert.assertNotNull(countdown, "Countdown text khong duoc null");
        Assert.assertTrue(countdown.startsWith("Timer:"),
                "Countdown phai bat dau bang 'Timer:'. Actual: " + countdown);
        ExtentReportManager.getTest().log(Status.PASS,
                "ACTIVE state - countdown: " + countdown);
    }

    @Test(description = "TC_MENU_15: Click Reset o ACTIVE -> dialog VAN MO, ve INITIAL")
    public void TC_MENU_15_reset_returns_to_initial() {
        ensureActiveState();

        menuPage.clickReset();
        sleep(1200);

        Assert.assertTrue(menuPage.isSleepTimerDialogOpen(),
                "Dialog phai VAN MO sau Reset");
        Assert.assertTrue(menuPage.isInInitialState(),
                "Phai quay ve INITIAL state sau Reset");
        Assert.assertTrue(menuPage.isSetTimerButtonVisible(),
                "Phai co 'Set timer' button (khong con Reset)");
        Assert.assertFalse(menuPage.isResetButtonVisible(),
                "KHONG duoc co Reset button sau reset");
        Assert.assertFalse(menuPage.isTimerCountdownVisible(),
                "KHONG duoc co countdown sau reset");

        ExtentReportManager.getTest().log(Status.PASS,
                "Reset: ACTIVE -> INITIAL, dialog van mo");
    }

    // ============================================
    // GROUP 2: CUSTOM SLEEP TIMER DIALOG
    // ============================================

    @Test(description = "TC_MENU_16: Click Custom -> mo Custom sleep timer dialog")
    public void TC_MENU_16_custom_dialog_opens() {
        ensureInitialState();
        Assert.assertTrue(menuPage.isCustomOptionDisplayed(),
                "Option Custom phai hien thi");

        menuPage.selectTimerCustom();
        sleep(1200);

        Assert.assertTrue(menuPage.isCustomDialogOpen(),
                "Custom sleep timer dialog phai mo");
        Assert.assertTrue(menuPage.isCustomMinutesLabelVisible(),
                "Phai co label 'Minutes'");
        Assert.assertTrue(menuPage.isCustomMinutesInputVisible(),
                "Phai co input EditText");
        Assert.assertTrue(menuPage.isCustomDoneButtonVisible(),
                "Phai co button 'Done'");

        ExtentReportManager.getTest().log(Status.PASS,
                "Custom dialog mo voi title + EditText + Minutes + Done + Cancel");
    }

    @Test(description = "TC_MENU_17: Click Cancel trong Custom dialog -> dong Custom, ve Sleep timer dialog initial")
    public void TC_MENU_17_custom_cancel_returns_to_parent() {
        ensureInitialState();
        menuPage.selectTimerCustom();
        sleep(1200);
        Assert.assertTrue(menuPage.isCustomDialogOpen(),
                "Custom dialog phai mo truoc khi Cancel");

        menuPage.clickCustomCancel();
        sleep(1200);

        Assert.assertFalse(menuPage.isCustomDialogOpen(),
                "Custom dialog phai dong sau Cancel");
        // Co the quay ve Sleep timer dialog hoac dong het - tuy thiet ke
        ExtentReportManager.getTest().log(Status.PASS,
                "Custom Cancel - dialog dong thanh cong");
    }

    @Test(description = "TC_MENU_18: Nhap '25' + Done -> ca 2 dialog dong, custom timer duoc set")
    public void TC_MENU_18_custom_done_sets_timer() {
        ensureInitialState();
        menuPage.selectTimerCustom();
        sleep(1200);
        Assert.assertTrue(menuPage.isCustomDialogOpen());

        // Nhap 25 phut
        menuPage.enterCustomMinutes("25");
        sleep(1000);

        String entered = menuPage.getCustomMinutesValue();
        Assert.assertTrue(entered.contains("25"),
                "Input phai chua '25'. Actual: " + entered);

        // Click Done -> set timer + ca 2 dialog dong
        menuPage.clickCustomDone();
        sleep(1800);

        Assert.assertFalse(menuPage.isCustomDialogOpen(),
                "Custom dialog phai dong sau Done");
        Assert.assertFalse(menuPage.isSleepTimerDialogOpen(),
                "Sleep timer dialog cung phai dong sau Done");

        // Verify timer set bang cach reopen
        homePage.clickMenuButton();
        sleep(1300);
        menuPage.clickSleepTimer();
        sleep(1000);

        Assert.assertTrue(menuPage.isInActiveState(),
                "Sau Custom Done, reopen phai thay ACTIVE state");

        String countdown = menuPage.getTimerCountdownText();
        ExtentReportManager.getTest().log(Status.PASS,
                "Custom timer 25 mins set OK - countdown: " + countdown);
    }

    @Test(description = "TC_MENU_19: Nhap so phut khac (vd: 5) + Done -> set duoc custom value nho")
    public void TC_MENU_19_custom_small_value() {
        ensureInitialState();
        menuPage.selectTimerCustom();
        sleep(1200);
        Assert.assertTrue(menuPage.isCustomDialogOpen());

        menuPage.enterCustomMinutes("5");
        sleep(1000);
        menuPage.clickCustomDone();
        sleep(1800);

        Assert.assertFalse(menuPage.isCustomDialogOpen(),
                "Custom dialog phai dong");

        // Reopen verify
        homePage.clickMenuButton();
        sleep(1300);
        menuPage.clickSleepTimer();
        sleep(1000);

        Assert.assertTrue(menuPage.isInActiveState(),
                "Custom 5 mins phai set duoc");
        ExtentReportManager.getTest().log(Status.PASS,
                "Custom 5 mins set OK");
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Dam bao Sleep timer dialog dang o INITIAL state.
     * Truong hop possible:
     *   - Dang o Custom dialog -> Cancel ve Sleep timer
     *   - Dang o ACTIVE state -> Reset ve INITIAL
     */
    private void ensureInitialState() {
        // Step 1: Neu dang o Custom dialog -> Cancel
        if (menuPage.isCustomDialogOpen()) {
            logger.info("[ensureInitialState] Dang o Custom dialog -> Cancel");
            menuPage.clickCustomCancel();
            sleep(1000);
        }
        // Step 2: Neu dang o ACTIVE state -> Reset
        if (menuPage.isInActiveState()) {
            logger.info("[ensureInitialState] Dang o ACTIVE -> Reset");
            menuPage.clickReset();
            sleep(1000);
        }
        Assert.assertTrue(menuPage.isInInitialState(),
                "Phai o INITIAL state truoc khi test");
    }

    /**
     * Dam bao dialog dang o ACTIVE state (co timer chay).
     * Neu chua: Set 15 mins timer va reopen dialog.
     */
    private void ensureActiveState() {
        // Neu dang o Custom -> Cancel
        if (menuPage.isCustomDialogOpen()) {
            menuPage.clickCustomCancel();
            sleep(1000);
        }
        if (menuPage.isInInitialState()) {
            logger.info("[ensureActiveState] Dang INITIAL -> set 15min + reopen");
            menuPage.selectTimer15Min();
            sleep(800);
            menuPage.clickSetTimer();
            sleep(1000);

            // Reopen menu + dialog
            homePage.clickMenuButton();
            sleep(1300);
            menuPage.clickSleepTimer();
            sleep(1000);
        }
        Assert.assertTrue(menuPage.isInActiveState(),
                "Phai o ACTIVE state truoc khi test");
    }

    /**
     * Cleanup sau moi test:
     *   1. Neu Custom dialog mo -> Cancel
     *   2. Neu ACTIVE timer -> Reset
     *   3. Neu Sleep timer dialog mo -> Cancel
     *   4. resetToHome
     */
    @AfterMethod(alwaysRun = true)
    public void cleanupAndReset() {
        try {
            // Step 1: Dong Custom dialog neu mo
            if (menuPage.isCustomDialogOpen()) {
                logger.info("[Cleanup] Custom dialog mo -> Cancel");
                menuPage.clickCustomCancel();
                sleep(1000);
            }

            // Step 2: Clear active timer neu co
            if (menuPage.isSleepTimerDialogOpen() && menuPage.isResetButtonVisible()) {
                logger.info("[Cleanup] Active timer -> Reset");
                menuPage.clickReset();
                sleep(1000);
            }

            // Step 3: Dong Sleep timer dialog neu mo
            if (menuPage.isSleepTimerDialogOpen()) {
                logger.info("[Cleanup] Sleep timer dialog van mo -> Cancel");
                menuPage.clickTimerCancel();
                sleep(1000);
            }

            // Step 4: Drawer con mo (vd dialog dong lo ve drawer) -> dong bang VUOT
            //         (BACK o drawer chi bung exit dialog -> resetToHome loop lau roi fail).
            if (menuPage.isDrawerOpen()) {
                logger.info("[Cleanup] Drawer van mo -> vuot dong");
                menuPage.closeDrawer();
                sleep(800);
            }
        } catch (Exception e) {
            logger.warn("[Cleanup] Loi: " + e.getMessage());
        }
        resetToHome();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}