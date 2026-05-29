package utils;

import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

/**
 * Utility class cho tap/swipe gesture.
 * Su dung W3C Actions (chuan moi cua Appium 2.x).
 */
public class GestureUtils {

    private static final Logger logger = LogManager.getLogger(GestureUtils.class);

    /**
     * Tap tai toa do voi hold 100ms (mo phong tap nguoi dung).
     * CAN THIET cho cac element clickable=false (vd: TextView trong WebView quang cao).
     */
    public static void tap(AppiumDriver driver, int x, int y) {
        logger.info("Tap at (" + x + ", " + y + ")");

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(100)))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(tap));
    }

    /**
     * Tap voi hold time custom.
     * 150ms = sweet spot natural tap.
     * 250-500ms = simulate long tap.
     */
    public static void tapAndHold(AppiumDriver driver, int x, int y, int holdMs) {
        logger.info("Tap and hold " + holdMs + "ms at (" + x + ", " + y + ")");

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO,
                        PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(new Pause(finger, Duration.ofMillis(holdMs)))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(tap));
    }

    private GestureUtils() {}
}