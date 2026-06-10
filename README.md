# Music Downloader - Automation Test (Appium + TestNG)

Bo test UI tu dong cho app Android **Music Downloader** (Appium UiAutomator2 + TestNG + Java/Gradle).

## 1. Yeu cau

- JDK 17+
- Android SDK (`adb` trong PATH)
- Appium Server 2.x (`appium` chay tai `http://127.0.0.1:4723`)
- 1 thiet bi Android / emulator da bat USB debugging

## 2. Cau hinh thiet bi (.env)  ⬅ QUAN TRONG

Cau hinh phu thuoc may (UDID, device, Appium URL...) nam trong file **`.env`** o thu muc goc.
File `.env` da duoc `.gitignore` nen **moi nguoi tu cau hinh rieng**, khong de len git.

**Lan dau setup:**

```bash
# Windows
copy .env.example .env

# Mac / Linux
cp .env.example .env
```

Sau do mo `.env` va sua **UDID** cho dung may cua ban (lay bang `adb devices`):

```
UDID=VSO7UO6DINEQBQPF
DEVICE_NAME=Oppo Pad Neo
PLATFORM_VERSION=14
APPIUM_SERVER_URL=http://127.0.0.1:4723
```

Cac key khac (app package/activity, package he thong) xem trong `.env.example` - hiem khi can doi.

> Thu tu uu tien gia tri: **bien moi truong he thong** > **`-Dkey=value`** (JVM) > **file `.env`** > default trong code.
> Vi du chay nhanh voi thiet bi khac ma khong sua `.env`:
> `./gradlew test -Psuite=tracks-only -DUDID=emulator-5554`

## 3. Chay test

```bash
# Chay 1 nhom (suite trong src/test/resources/<ten>.xml)
./gradlew test -Psuite=tracks-only

# Vi du suite khac
./gradlew test -Psuite=menu-only
```

Bao cao ExtentReport sinh ra trong thu muc `reports/` sau khi chay.

## 4. Ghi chu

- Test dung **1 session Appium chung** cho ca suite (toi uu toc do) + tu dong recover neu UiAutomator2 crash.
- Co `RetryAnalyzer` tu dong chay lai 1 lan cho test fail (chong flaky tren Flutter).
- Mot so testcase Tracks co **xoa file that** tren thiet bi (delete) - chay tren may test, khong dung du lieu that.
