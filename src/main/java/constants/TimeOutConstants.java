package constants;

/**
 * Constants cho timeout va sleep.
 */
public class TimeOutConstants {

    // Driver session
    // 300s: bypass quang cao co the mat ~100s+ (Google interstitial co countdown dai
    // truoc khi nut Close co tac dung) — newCommandTimeout phai du lon de session khong
    // bi het han giua luc dang xu ly ad.
    public static final int NEW_COMMAND_TIMEOUT = 300;
    // 2s: man Tracks (Flutter) render element qua accessibility hoi cham -> de 1s gay flaky
    // "element not found" cho cac positive check ngay sau navigate (vd track count, char count,
    // mini player). 2s du grace ma van khong qua lau cho negative check. (Menu test van pass o 2s.)
    public static final int IMPLICIT_WAIT = 2;

    // Smart wait (explicit wait - WebDriverWait)
    public static final int SHORT_WAIT = 3;
    public static final int MEDIUM_WAIT = 8;
    public static final int LONG_WAIT = 15;

    // Sleep cung (han che dung)
    public static final int SLEEP_SHORT = 500;
    public static final int SLEEP_MEDIUM = 1500;
    public static final int SLEEP_LONG = 3000;

    // ====== AD HANDLER ======
    public static final int AD_DETECT_TIMEOUT = 5000;    // 5s cho quang cao xuat hien
    public static final int AD_MAX_WAIT = 25000;         // 25s tong thoi gian xu ly ad
    public static final int AD_POLL_INTERVAL = 500;      // poll moi 0.5s
    public static final int AD_ACTION_WAIT = 1500;       // doi sau khi tap

    private TimeOutConstants() {}
}