# ShiftMark 

A mobile and smartwatch application designed to help medical professionals quickly and accurately document shift activities for charting purposes.

---

## Academic Context

| | |
|---|---|
| **Course** | Software Engineering II — Spring 2026 |
| **Author** | Kaitlyn Morris |
| **Advising Professor** | Dr. Zhao |
| **Due Date** | May 4, 2026 |

---

## Project Overview

ShiftMark is a two-part application installed on an Android phone and a Samsung Galaxy Watch connected via Bluetooth. It is designed for nurses and other healthcare professionals who need to log timestamped actions during their shift to simplify and improve the accuracy of charting.

From the watch, a user can press a button or use a voice command to instantly mark the current time. All logged timestamps and notes are then synced to the phone app, where they can be reviewed and used for charting. All data is automatically deleted after 24 hours to protect patient and user privacy.

---

## Target Users

- Registered Nurses (RN)
- Licensed Practical Nurses (LPN)
- Certified Nursing Assistants (CNA)
- Any healthcare professional requiring shift documentation

---

## Features

### Watch Application
- **Button Timestamp** — Press the top button to instantly log the current time
- **Voice Controls** — Use voice commands to mark time or add a note hands-free
- **Timestamp Labels** — Attach an optional title or note to each timestamp
- **Offline Functionality** — Logs timestamps even without an active connection
- **Background Operations** — Runs quietly in the background during a shift

### Phone Application
- **Timeline View** — See all logged timestamps in a clean, chronological list
- **Search Timestamps** — Quickly find a specific logged event
- **Secure Log-in** — Protected access to keep shift data private
- **Customization Settings** — Personalize the app to fit your workflow
- **Auto-Delete** — All data is wiped after 24 hours

---

## Technology Stack

| Component | Technology |
|---|---|
| IDE | Android Studio |
| Language | Kotlin |
| Framework | Jetpack Compose |
| Watch Platform | Samsung Galaxy Watch (Wear OS) |
| Communication | Bluetooth |

---

## Repository Structure

```
ShiftMark/
│
├── app/                                   # Main application module
│   ├── src/main/
│   │   ├── java/com/shiftmark/app/
│   │        ├── complication/             # Watch complication code
│   │        ├── presentation/             # UI / Jetpack Compose screens
│   │        └── tile/                     # Watch tile code
│   │   ├── res                           # Resources
│   ├── .gitignore
│   ├── build.gradle.kts                   # App-level build config
│   ├── lint                               # Lint config
│   └── proguard-rules.pro
│
├── docs/
│   └── diagrams/
│       ├── ActivityDiagram                # Activity diagram
│       ├── PrototypeScreens               # Screen templates
│       └── SystemArchitecture             # System architecture diagram
│
├── .gitignore
├── build.gradle.kts                       # Project-level build config
├── gradlew
├── gradlew.bat
├── README.md
└── settings.gradle.kts
```

---

## Getting Started

### Prerequisites
- Android Studio (latest stable version)
- Android SDK
- A physical Android device or emulator running Android 8.0+
- Samsung Galaxy Watch (for watch module testing)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Kmorris1370/ShiftMark.git
   ```
2. Open the project in Android Studio
3. Let Gradle sync and resolve dependencies
4. Run the **phone** module on your Android device or emulator
5. Run the **watch** module on your paired Samsung Galaxy Watch

---

## Deliverables

- [ ] Completed Phone Application
- [ ] Completed Watch Application
- [ ] Project Journal

---

## Future Developments

- Celsius to Fahrenheit converter
- Multi-timer support

---

## Author
#### Kaitlyn Morris
