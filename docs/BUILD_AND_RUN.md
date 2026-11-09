# Build and run — Android Studio, step by step

You need: **Android Studio** (2023.1 “Giraffe” or newer), **Android SDK 34**, and a JDK **17**.
Android Studio bundles its own JDK (Settings → Build, Execution, Deployment → Build Tools →
Gradle → *Gradle JDK*: “jbr-17” or “Embedded JDK 17”). Nothing else — no Node, no Python, no paid
plugins.

## 1. Get the code

```bash
git clone https://github.com/Antkkorc/OIBSIP.git
cd OIBSIP
```

Or on GitHub: **Code → Download ZIP**, then unzip.

## 2. Open a task

Each task is its own Android Studio project. **Do not open the repository root** — it has no
`settings.gradle` on purpose, because five independent apps cannot share one Gradle build.

```
File → Open… →  OIBSIP/Antonio_Task1_UnitConverter        (then tasks 2…5 the same way)
```

Accept “Trust project”. Studio then runs a Gradle sync:

- First sync downloads Android Gradle Plugin **8.6.1**, Gradle **8.7** (already pinned in
  `gradle/wrapper/gradle-wrapper.properties`) and AndroidX — a few minutes on a fresh machine.
- The wrapper JAR is committed, so no separate Gradle install is needed.

## 3. Pick a device

- **Emulator:** Device Manager → Create device → e.g. Pixel 6, system image **API 34** → Finish →
  Run ▶. Anything from **API 26** works; the apps are designed at 360 × 640 dp and scale up.
- **Physical phone:** enable Developer options → USB debugging, plug in, allow the prompt, select
  the device in the toolbar, press Run ▶.

## 4. Run

Press **Run ▶** (`Shift+F10`). The launcher icon is labelled `01 Converter` … `05 Chronograph`.

## 5. Run the tests

In the Studio terminal, from inside a task folder:

```bash
./gradlew testDebugUnitTest
```

On Windows use `gradlew.bat testDebugUnitTest`. Reports: `app/build/reports/tests/`.

## 6. Build an APK to submit or demo

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`. Install it with
`adb install -r app-debug.apk`.

For a Play-style release build: **Build → Generate Signed Bundle / APK → APK**, create a keystore,
choose **release**, then `./gradlew assembleRelease`. The APK lands in
`app/build/outputs/apk/release/`. Do not commit keystores to the repository.

## 7. Common problems

| Symptom | Cause | Fix |
|---|---|---|
| `Could not find or load main class org.gradle.wrapper.GradleWrapperMain` | wrapper JAR missing (e.g. ZIP download dropped binaries) | re-clone with git, or File → Sync Project with Gradle Files; if it persists, `gradle wrapper --gradle-version 8.7` |
| `Unsupported class file major version` / `Unsupported Java` | Gradle JDK is 8–11 or 24+ | Settings → Gradle → *Gradle JDK* → pick **17** |
| `Failed to find target with hash string 'android-34'` | SDK 34 not installed | Tools → SDK Manager → SDK Platforms → tick **Android 14 (API 34)** → Apply |
| `Namespace not specified` | you opened the repo root | open a single `Antonio_Task…` folder |
| `R.id` / `R.string` unresolved everywhere | sync failed earlier | Build → Clean Project, then Sync Project with Gradle Files |
| Sync is stuck on "Downloading gradle-8.7-bin.zip" | corporate proxy | Settings → HTTP Proxy, or download the distribution manually and point `distributionUrl` at it |
| Emulator is painfully slow | no hardware acceleration | enable HAXM/WHPX in SDK Manager → SDK Tools, or use a physical device |
| App shows a blank white screen | a custom font failed to load on a very old API | confirm the device is API 26+ (`minSdk 26`) |

## 8. Project shape (every task)

```
Antonio_TaskN_…/
├── settings.gradle            repositories + include ':app'
├── build.gradle               AGP 8.6.1, apply false
├── gradle.properties          AndroidX on, 2 GB heap
├── gradle/wrapper/            committed wrapper (jar + properties)
├── gradlew, gradlew.bat
└── app/
    ├── build.gradle           namespace, compileSdk 34, minSdk 26, targetSdk 34
    └── src/
        ├── main/java/…        activities + pure logic classes
        ├── main/res/          layouts, values (colours, styles, dimens), drawables, fonts
        ├── main/AndroidManifest.xml
        └── test/java/…        JVM unit tests
```

`compileSdk 34` / `targetSdk 34` is deliberate: Android 15 enforces edge-to-edge for `targetSdk
35`, and these layouts intentionally honour the system bar in paper. When you move to 35, add
`WindowCompat.setDecorFitsSystemWindows(window, false)` plus inset padding on each root view.
