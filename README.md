# ShiftMark

> A paired Android phone + Samsung Galaxy Watch app for nurses who need accurate, hands-free shift documentation.

ShiftMark lets a nurse tap a button on their watch to silently record a timestamp. Each mark syncs instantly to the phone over Bluetooth. Data lives only on the device and auto-deletes 24 hours after the first mark of a session — no accounts, no servers, no lingering patient data.

<img width="400" height="400" alt="app_icon" src="https://github.com/user-attachments/assets/9d2e698a-654f-4bec-8948-dfd7fa50b059" />

---

## Features

### Watch
- Tap **Mark** to record a silent timestamp — vibrates to confirm
- Long-press **Mark** to dictate a title via voice (e.g. "pushed med")
- Scrollable history of today's marks directly on the watch
- Tap any history row to edit or delete on the watch
- Edits and deletions sync back to the phone

### Phone
- Live timeline of all marks, newest first
- **Time Until Autodelete** countdown in the header
- Tap any row to edit title and notes in full detail view
- Inline notes editing directly on the timeline
- Manual timestamp entry via the **+** floating action button
- Search by title or notes
- Filter by: All / Has title / Has notes / No title or notes
- PIN gate on launch — resets when data is wiped
- Manual & FAQ screen explaining every feature
- Delete Data wipes all timestamps and resets the session

### Auto-Delete
- 24-hour countdown begins when the first mark of a session is recorded
- Cancels if the user manually clears all timestamps
- Implemented via WorkManager — survives app close and process death

---

## Tech Stack

| | |
|---|---|
| **Language** | Kotlin |
| **Phone UI** | Jetpack Compose + Material 3 |
| **Watch UI** | Wear Compose |
| **Sync** | Google Play Services Wearable Data Layer |
| **Persistence** | SharedPreferences + JSON |
| **Security** | PBKDF2-SHA256 PIN + EncryptedSharedPreferences |
| **Auto-Delete** | WorkManager |
| **IDE** | Android Studio |

---

## Build & Run

### Requirements
- Android Studio Hedgehog or newer
- JDK 11
- Android SDK API 26+ (phone)
- Wear OS SDK API 30+ (watch)
- Samsung Galaxy Watch paired via Galaxy Wearable app

### Install

```bash
# Phone
./gradlew :app:installDebug

# Watch
./gradlew :wear:installDebug
```

Or use the **app** and **wear** run configurations in Android Studio.

> Both modules must be installed from the same machine so they share the same debug signing key. Wear OS pairs apps by application ID and signing key — a mismatch will break Bluetooth sync.

---

## Project Structure

```
ShiftMark/
├── app/                                        ← Phone module
│   └── src/main/java/com/example/shiftmark/
│       ├── MainActivity.kt                     ← Nav graph; decides PIN setup vs. login at launch
│       ├── Constants.kt                        ← Shared wire-format path strings
│       ├── Timestamp.kt                        ← Data class: id, time, title, notes
│       ├── TimestampRepository.kt              ← JSON-in-SharedPreferences persistence
│       ├── TimestampViewModel.kt               ← Compose state; wraps the repository
│       ├── TimestampListenerService.kt         ← Receives Wearable messages from the watch
│       ├── SecureStorage.kt                    ← PBKDF2 PIN hashing + EncryptedSharedPreferences
│       ├── AutoDeleteManager.kt               ← Records first-mark time; schedules WorkManager job
│       ├── DeleteWorker.kt                     ← WorkManager job that wipes data
│       ├── PinSetupScreen.kt                   ← First-launch PIN creation
│       └── ui/
│           ├── TimelineScreen.kt               ← Main screen: countdown, list, search, filter, FAB
│           ├── TimestampDetailScreen.kt        ← Full edit view for title and notes
│           ├── ManualEntryScreen.kt            ← Phone-side manual timestamp form
│           ├── ManualScreen.kt                 ← FAQ accordion
│           ├── MenuScreen.kt                   ← Sidebar: Manual and Delete Data
│           ├── LoginScreen.kt                  ← PIN entry; defines shared color constants
│           └── PinInfoIcon.kt                  ← Reusable ⓘ icon explaining the PIN model
│
└── wear/                                       ← Watch module
    └── src/main/java/com/example/wear/shiftmark/
        ├── MainActivity.kt                     ← ComponentActivity host
        ├── Constants.kt                        ← Same wire-format paths as phone
        └── ui/WatchHomeScreen.kt               ← Full watch UI: Mark button, history, voice, sync
```

---

## Documentation

Full developer documentation is available in the [project wiki](https://github.com/Kmorris1370/ShiftMark/wiki):

| Page | Description |
|------|-------------|
| [Architecture](https://github.com/Kmorris1370/ShiftMark/wiki/Architecture) | Sync flow, module structure, wire format |
| [Phone App](https://github.com/Kmorris1370/ShiftMark/wiki/Phone-App) | Screens, navigation, ViewModel, Repository |
| [Watch App](https://github.com/Kmorris1370/ShiftMark/wiki/Watch-App) | Watch UI, voice input, sync helpers |
| [Bluetooth Sync](https://github.com/Kmorris1370/ShiftMark/wiki/Bluetooth-Sync) | Data Layer API, manifest setup, debugging |
| [Security](https://github.com/Kmorris1370/ShiftMark/wiki/Security) | PIN hashing, EncryptedSharedPreferences, auto-delete |
| [Setup](https://github.com/Kmorris1370/ShiftMark/wiki/Setup) | Build instructions, emulators, ADB watch connection |
| [Theme](https://github.com/Kmorris1370/ShiftMark/wiki/Theme) | Color palette, typography, component styling |
| [Kotlin Basics](https://github.com/Kmorris1370/ShiftMark/wiki/Kotlin-Basic) | Kotlin and Compose concepts used in the project |
| [Future Developments](https://github.com/Kmorris1370/ShiftMark/wiki/Future-Developments) | Planned features and known gaps |
| [Deployment & Maintenance](https://github.com/Kmorris1370/ShiftMark/wiki/Maintenance) | Firebase, Play Store, sideloading, updates |
| [Known Issues](https://github.com/Kmorris1370/ShiftMark/wiki/Known-Issues) | Galaxy Watch quirks, Android 14 broadcasts, limitations |

---

## Known Limitations

- **Galaxy Watch side buttons** are reserved by Samsung for system functions. The on-screen Mark button is the only input available.
- **Voice input on Galaxy Watch** uses Samsung Keyboard's mic via the Wear OS input picker. Standard Android speech recognition (`RecognizerIntent`) is not supported on Samsung devices.
- **Phone → watch sync is one-way.** Edits made on the phone do not propagate back to the watch display.
- **Watch history is in-memory.** The watch list resets if the watch app's process is killed. The phone retains all data.
- **Auto-delete timing is approximate.** WorkManager can fire a few minutes late on devices in Doze mode.

---

## Academic Context

Developed as a Spring 2026 Software Engineering II project.
**Author:** Kaitlyn Morris | **Advisor:** Dr. Zhao | **Due:** May 4, 2026

---

## License

MIT License — see [LICENSE](LICENSE) for details.
