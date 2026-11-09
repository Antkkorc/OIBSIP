# Build and run

Two routes. Android Studio is the short one and it is what I used. VS Code works if
you have the Android command line tools and you are comfortable with a terminal.

Nothing here needs Node, Python or a paid plugin. The Gradle wrapper is committed, so a
separate Gradle install is optional.

## What you need

- Android Studio Hedgehog 2023.1.1 or newer. Iguana or Jellyfish are fine too.
- Android SDK Platform 34, plus Build Tools 34.
- JDK 17. Studio ships one called jbr 17. Check it in Settings, then Build, Execution,
  Deployment, Build Tools, Gradle, and read the Gradle JDK field.
- Internet on the first sync, for the AndroidX downloads.

The apps run on any device from API 26. They are designed at 360 x 640 dp and scale up
from there.

## Get the code

```
git clone https://github.com/Antkkorc/OIBSIP.git
cd OIBSIP
```

From the GitHub page you can also use Code, then Download ZIP and unzip it.

## Android Studio, step by step

1. Open Android Studio and choose File, then Open.
2. Pick one project folder, not the repository root. For example
   `OIBSIP/Antonio_Task3_Calculator`.

   The root has no `settings.gradle` on purpose. Five separate apps cannot share one
   Gradle build, and opening the root would make Studio guess.

3. Accept the prompt that asks whether you trust the project.
4. Let the sync finish. The first one downloads the Android Gradle Plugin 8.6.1, Gradle
   8.7 and the AndroidX libraries. This takes a few minutes on a fresh machine.
5. If Studio warns that the Gradle JDK is not set, choose the embedded JDK 17 in
   Settings, Build, Execution, Deployment, Build Tools, Gradle.
6. Open Device Manager and create a device. Pixel 6 with the API 34 system image is a
   safe choice. Any image from API 26 up will work.
7. Pick the device in the toolbar and press Run, or Shift + F10.

To build the APK for a demo without a cable, use Build, Build Bundle(s) / APK(s), Build
APK(s). The file lands in `app/build/outputs/apk/debug/app-debug.apk`. Install it with
`adb install -r app-debug.apk`.

## VS Code, step by step

This route assumes no Android Studio at all.

1. Install a JDK 17. Temurin is a good default.
2. Install the Android command line tools from the Android developer site, then set
   `ANDROID_HOME` to the folder you unpacked.
3. Install the SDK packages:

   ```
   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```

4. Accept the licences:

   ```
   sdkmanager --licenses
   ```

5. Open the repo in VS Code and install these extensions: Extension Pack for Java,
   Gradle for Java, and an Android XML extension for layout highlighting.
6. Point Gradle at the SDK. Either export `ANDROID_HOME` in your shell, or create
   `local.properties` inside the task folder with one line:

   ```
   sdk.dir=/path/to/your/Android/sdk
   ```

7. Build from the terminal, inside the task folder:

   ```
   ./gradlew assembleDebug
   ```

   On Windows use `gradlew.bat assembleDebug`.

8. Create and start an emulator:

   ```
   avdmanager create avd -n pixel34 -k "system-images;android-34;google_apis;x86_64"
   emulator -avd pixel34
   ```

9. Install and launch:

   ```
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb shell monkey -p bw.oibsip.calc -c android.intent.category.LAUNCHER 1
   ```

   Change the package to match the app: `bw.oibsip.converter`, `bw.oibsip.tasks`,
   `bw.oibsip.calc`, `bw.oibsip.quiz`, `bw.oibsip.chrono`.

10. For breakpoints, use the Java extension debugger with the "Attach to Android
    process" configuration, or add `android:debuggable` to a debug build and attach to
    the app process by name.

## Rebuilding a project from scratch

If you would rather start from an empty project than clone, this is the order that
works. It is also the path to follow if a file goes missing.

1. In Android Studio choose New Project, then Empty Views Activity.
2. Name it after the task. Set the package to the value in this table, the language to
   Java, and the minimum SDK to 26.

   | Task | Package |
   | ---- | ------- |
   | 1 | `bw.oibsip.converter` |
   | 2 | `bw.oibsip.tasks` |
   | 3 | `bw.oibsip.calc` |
   | 4 | `bw.oibsip.quiz` |
   | 5 | `bw.oibsip.chrono` |

3. Open `app/build.gradle`. Add the CardView line:

   ```gradle
   implementation 'androidx.cardview:cardview:1.0.0'
   ```

   RecyclerView is already there in a new project. If it is not, add
   `implementation 'androidx.recyclerview:recyclerview:1.3.2'`.

4. Copy `app/src/main/res` from the repo over the generated one. That brings the
   layouts, drawables, colours, styles, fonts and launcher icons in one step. Delete the
   starter `activity_main.xml` if the repo does not have one by that name.
5. Copy the Java files into `app/src/main/java/<package>/`. Every app needs
   `GlassApp.java` and `GlassUi.java` as well as its own screens.
6. Open `AndroidManifest.xml`. Replace the `<application>` block with the one from the
   repo. The important parts are `android:name=".GlassApp"`, the theme
   `@style/Theme.Glass`, and the activity list for that app.
7. Copy `app/src/test` if you want the unit tests.
8. Sync, then run.

The first build after a copy usually fails on one of two things: a missing dependency or
an old `MainActivity` left over from the template. Both are covered below.

## Common problems

**Gradle sync failed, unable to resolve androidx.cardview.** You are offline, or the
line was not added. Connect once and sync again.

**SDK location not found.** Create `local.properties` in the task folder with
`sdk.dir=` pointing at your SDK, or set `ANDROID_HOME`.

**`Unsupported class file major version`.** Studio is using a JDK that is too new or too
old. Set the Gradle JDK to 17.

**Cannot resolve symbol R.** The XML did not compile. Look at the Build output for the
first error, which is usually a missing drawable or a bad attribute value, and fix that
one. R appears as soon as resources compile.

**App opens on a white screen.** The manifest still points at the template
`MainActivity` or the theme is not `Theme.Glass`. Check both.

**Fonts look like Roboto.** The `res/font` folder did not copy. The names matter:
`poppins.ttf`, `poppins_light.ttf`, `poppins_medium.ttf`, `poppins_semibold.ttf`,
`poppins_bold.ttf`.

**Emulator is slow or will not start.** Use an x86_64 image rather than ARM, and enable
hardware acceleration in the BIOS if you are on Windows.

**A screen looks cramped.** The layouts are built for 360dp wide. On a very wide tablet
the cards stretch, which is intended, but two column rows can feel loose. Rotate to
portrait.
