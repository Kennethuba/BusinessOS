# BusinessOS

An offline Android app for small Kenyan businesses: market stalls, garages, or both under one owner.
Kotlin, Jetpack Compose, Room, MVVM. Everything stays on the phone.

## Features
- Sales with cash, M-Pesa or credit (deni), and stock tracking
- Job orders for customers or to make stock
- Cashbook, profit and a double-entry ledger that always balances
- Staff with PINs and roles: Owner, Manager, Cashier, Viewer
- English and Kiswahili, one at a time
- Backup to any app you share to, and restore from a file

## Build
The project builds with Gradle and JDK 17.

    ./gradlew testDebugUnitTest
    ./gradlew assembleDebug

GitHub Actions runs the tests and builds the debug APK on every push. Download it from the run's Artifacts.

### Release build
Set these environment variables, then run `./gradlew assembleRelease` (or `bundleRelease` for an AAB):

    BUSINESSOS_KEYSTORE        path to your .jks file
    BUSINESSOS_KEYSTORE_PASSWORD
    BUSINESSOS_KEY_ALIAS
    BUSINESSOS_KEY_PASSWORD

Without them the release build is created unsigned. Keep the keystore safe: losing it means you cannot update the app.

## Data safety
- Money is stored as whole cents.
- Sales and expenses are voided with a reason, never deleted.
- Backup copies the whole database after flushing it. Restore checks the file first and keeps your current data if anything fails.
- Last crash details are saved in the app's files folder as `last_crash.txt`.
