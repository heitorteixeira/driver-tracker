# Driver Profit

An Android app for ride-hailing drivers (Uber, 99, inDrive, Bolt and others) to track daily earnings and expenses, see their real profit, and decide whether a ride offer is worth accepting.

All data stays on the device. There is no account, backend or internet permission.

## Features

- **First-run setup**: on first launch the app asks for language, currency and units (metric or imperial). All three can be changed later in Settings.
- **Home**: net profit, gross, expenses, distance, hours, gross per distance and gross per hour for today, this week or this month, plus a breakdown by platform.
- **Ride calculator**: enter the fare, trip distance, pickup distance and total time to get earnings per distance and per hour, estimated cost, net profit and margin. The verdict is green ("Worth it"), yellow ("Borderline") or red ("Skip it"), based on your own minimums.
- **History**: add, edit and delete daily entries (date, platform, gross earnings, distance, hours, fuel and other expenses), and export them to CSV.
- **Settings**: language, currency, units, vehicle costs (fuel price, efficiency, maintenance and wear) and your minimum earnings per distance and per hour.
- **Localization**: English, Spanish and Portuguese, including the in-app language picker (Android per-app language).

## How the ride verdict works

```
cost         = (pickup + trip distance) * (fuel price / efficiency + maintenance per distance)
net          = fare - cost
per distance = fare / (pickup + trip distance)
per hour     = fare / (minutes / 60)
```

- **Red**: net profit is zero or negative.
- **Green**: net is positive and both per-distance and per-hour meet your minimums.
- **Yellow**: net is positive but at least one minimum is not met.

## Tech stack

- Kotlin, Jetpack Compose and Material 3
- Navigation Compose
- Room (daily entries) with KSP
- DataStore Preferences (settings)
- AppCompat per-app locales for in-app language switching
- minSdk 26, targetSdk 36, Java 17

## Project structure

```
app/src/main/java/com/driverprofit/
├── MainActivity.kt        # Navigation and bottom bar
├── data/                  # Room database, DAO, DataStore settings
├── domain/                # Ride calculator, stats, platforms (pure Kotlin)
└── ui/                    # Compose screens, theme and formatting
app/src/main/res/
├── values/                # English strings
├── values-es/             # Spanish strings
└── values-pt/             # Portuguese strings
```

## Build and run

Requirements: Android Studio (recent version) with a JDK 17 or newer.

1. Open the project in Android Studio and let Gradle sync.
2. Select a device or emulator and press Run.

Or from the command line:

```bash
./gradlew assembleDebug      # build a debug APK
./gradlew installDebug       # install on a connected device
./gradlew test               # run unit tests
```

## Adding a language

1. Create `app/src/main/res/values-<code>/strings.xml` with all the strings from `values/strings.xml`.
2. Add `<locale android:name="<code>" />` to `app/src/main/res/xml/locales_config.xml`.
3. Add the language to the `LANGUAGES` list in `ui/SettingsScreen.kt`.
