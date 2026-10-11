# SnapRelay: Android camera + Telegram bot

Architecture: Android captures a photo, uploads it to a small backend over HTTPS, and the backend sends it to Telegram. The bot token is never shipped inside the APK.

## Run
1. Deploy `server/` on a server with HTTPS.
2. Set `TELEGRAM_BOT_TOKEN` and `TELEGRAM_CHAT_ID` on the server.
3. Put your HTTPS server URL in `android/app/src/main/java/com/snaprelay/app/MainActivity.kt`.
4. Open the `android` folder in Android Studio and run on an Android phone.

The app asks for camera permission, previews the photo, and only uploads after the user taps Send.

## Build a signed installable APK
This sandbox does not include the Android SDK/Gradle toolchain, so the APK binary cannot be compiled here. Push this folder to GitHub, open Actions, run **Build signed SnapRelay APK**, then download `snaprelay-debug-apk`; Android accepts this debug-signed APK for direct installation. Replace `YOUR-HTTPS-SERVER.example.com` in `MainActivity.kt` before building.
