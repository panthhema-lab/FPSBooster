# FPS Booster (Android App)

## Ye app kya karti hai
- 📋 Installed apps ki list dikhati hai, games ko "GAME" tag ke saath highlight karti hai
- ⚡ **Boost Now**: background me chal rahe apps ko close karti hai (RAM free karne ke liye), protected apps ko chhod kar
- 🌙 **Game Mode**: Do Not Disturb on karti hai taaki gaming ke time calls/notifications distract na karein
- 📊 RAM usage ka live status dikhati hai

## ⚠️ Sach mein samajh lein
Bina **root** ke koi bhi Android app GPU clock speed, CPU governor, ya game ke internal FPS ko directly change nahi kar sakta. Play Store ke 90% "FPS Booster / Game Booster" apps yehi karte hain jo ye app kar rahi hai — background apps band karna aur notifications silent karna. Isse device thoda responsive lag sakta hai, lekin ye "guaranteed FPS increase" nahi hai.

## Build kaise karein
1. [Android Studio](https://developer.android.com/studio) install karein (latest version)
2. Ye poora `FPSBooster` folder open karein: **File → Open**
3. Gradle sync hone dein (pehli baar internet chahiye, dependencies download hongi)
4. Ek Android phone/emulator connect karein (minimum Android 7.0 / API 24)
5. ▶️ Run button dabayein

## Permissions jo app maangegi
- **Kill background processes** — auto-granted, kuch karne ki zarurat nahi
- **Do Not Disturb access** — Game Mode on karte waqt ek Settings screen khulegi, wahan allow karna hoga
- **Query all packages** — sirf sideload/personal use ke liye theek hai; Play Store pe publish karna ho to isko replace karna padega (README ke neeche note dekhein)

## Play Store pe publish karne se pehle
`QUERY_ALL_PACKAGES` permission Google Play review me restrict hai. Agar publish karna ho to `AndroidManifest.xml` me:
```xml
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" tools:ignore="QueryAllPackagesPermission" />
```
ko hata kar iski jagah `<queries>` tag use karke sirf specific intent categories query karein.

## Project structure
```
FPSBooster/
├── app/
│   └── src/main/
│       ├── java/com/boosterapp/fps/
│       │   ├── MainActivity.kt      -> Main screen logic
│       │   ├── BoosterUtils.kt      -> Boost/RAM/DND logic
│       │   ├── AppInfo.kt           -> Data model
│       │   └── AppListAdapter.kt    -> RecyclerView adapter
│       ├── res/layout/               -> UI screens
│       ├── res/values/               -> Colors, strings, theme
│       └── AndroidManifest.xml
├── build.gradle.kts
└── settings.gradle.kts
```
