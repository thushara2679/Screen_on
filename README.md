# Display On: wake an Android screen remotely

Flow: Controller app -> Firestore `commands` -> Cloud Function -> FCM high-priority message -> Target app lights the screen.

## Setup
1. Create a Firebase project (Blaze plan, required for Cloud Functions).
2. Enable **Authentication -> Anonymous** and create a **Firestore** database.
3. Add an Android app with package `com.example.displayon`; download `google-services.json` into `app/`.
4. Deploy the backend:
   ```
   npm i -g firebase-tools
   firebase login
   firebase use --add
   cd functions && npm install && cd ..
   firebase deploy --only functions,firestore:rules
   ```
5. Build the APK in the cloud (no Android Studio needed):
   1. Create a GitHub repository and push this folder to it.
   2. In the repo: Settings -> Secrets and variables -> Actions -> New secret named `GOOGLE_SERVICES_JSON`; paste the full contents of `google-services.json`.
   3. Open the **Actions** tab, run **Build APK** (it also runs on every push), and download the `app-debug` artifact (a zip containing `app-debug.apk`).
   4. Copy the APK to each phone and install it (allow "install unknown apps" for your file manager/browser).
6. Open the app on both phones. Each phone shows **its own 6-digit code**; type the other phone's code into **Other phone's code** and tap **Turn other phone's display on**. It works in both directions. Grant the three permission buttons on every phone.

## Web controller (optional)
`public/index.html` is a browser alternative for sending the command from a PC or any device.
1. In Firebase console, add a **Web app** and paste its config into `firebaseConfig` in `public/index.html`.
2. `firebase deploy --only hosting`, then open the printed `*.web.app` URL from any device.
3. Enter the target's 6-digit code and press **Turn display on**.

## Limits
- Lights the screen only; it does not unlock a secured lockscreen.
- A force-stopped app receives no FCM until reopened; some OEMs need extra "autostart" / battery whitelisting.
