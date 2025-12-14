# Weight Tracker App

A simple Android application built with Kotlin and Jetpack Compose to track daily weight entries.

## Features

- **Add Weight Entry**: Record your weight with a timestamp.
- **View History**: See a scrollable list (table) of all your weight entries.
- **Graphical View**: Toggle between a table view and a line graph to visualize your progress.
- **Data Persistence**: Uses Room Database to store data locally on the device.

## Prerequisites

- Android Studio Iguana (or newer)
- Android SDK (API Level 34 recommended)
- JDK 17 (Usually bundled with Android Studio)

## Project Structure

- **Data Layer**: `WeightEntry` (Entity), `WeightDao`, `WeightDatabase`.
- **UI Layer**: Built with Jetpack Compose.
  - `MainActivity`: Host activity.
  - `WeightTrackerApp`: Main navigation host.
  - `HomeScreen`: Displays the list and graph toggle.
  - `AddEntryScreen`: Form to add new data.
  - `Graph`: Custom canvas implementation for the line chart.

## Running Automated Tests

To run the unit and integration tests, it is **highly recommended** to use a dedicated Emulator to prevent wiping data on your physical device.

### 1. Set Up the Test Emulator
1.  Open **Android Studio**.
2.  Go to **Tools** > **Device Manager**.
3.  Click the **+** (Create Virtual Device) button.
4.  Select **Pixel 8** (or similar Phone hardware).
5.  Select the **API 34** system image (Release Name: "UpsideDownCake").
    *   *Note: Avoid "Preview" or "Extension" images for stability.*
6.  Name the AVD: `PIXEL_API_34`.
7.  Click **Finish**.

### 2. Start the Emulator
1.  Open **Device Manager**.
2.  Click the **Play** button next to `PIXEL_API_34`.
3.  Wait for the emulator to boot up.

### 3. Run Tests
Run the following command in the terminal (Git Bash or Terminal tab in Android Studio):

```bash
set ANDROID_SERIAL=emulator-5554 ; ./gradlew connectedAndroidTest
```

*Note: This command forces the tests to run ONLY on the emulator, keeping your physical phone safe.*

## Build Variants (Safe Data vs. Development)

To prevent accidental data loss while developing, this project is configured with distinct **Build Variants**.

-   **Debug (Default)**:
    -   **Package Name**: `io.github.twoquarterrican.weighttracker.debug`
    -   **Use Case**: Daily development, testing new features, running unit tests.
    -   **Safety**: Installing this creates a *separate app* on your phone. You can wipe its data or uninstall it freely without affecting your real logs.

-   **Release**:
    -   **Package Name**: `io.github.twoquarterrican.weighttracker`
    -   **Use Case**: Your personal "Production" version where you keep your real weight history.
    -   **Safety**: Only install this when you want to update your daily driver app.

### How to Switch Variants
1.  In Android Studio, find the **Build Variants** tool window (usually on the left sidebar).
2.  Click the dropdown next to `app`.
3.  Select **debug** for development (Safe).
4.  Select **release** to update your real app (Production).

## How to Load on Your Phone for Testing

To run this app on your physical Android device, follow these steps:

### 1. Enable Developer Options on Your Phone

1.  Open the **Settings** app on your phone.
2.  Scroll down to **About phone** (or **System** > **About phone**).
3.  Find the **Build number** entry (often under "Software information").
4.  Tap **Build number** 7 times repeatedly until you see a message saying "You are now a developer!".
5.  Go back to the main Settings menu, and you should now see **Developer options** (sometimes under **System**).

### 2. Connect Your Device

You can connect your device using either a USB cable (Preferred) or via Wi-Fi. Choose one of the following options for your debug session.

#### Option A: Connect via USB (Preferred)

This method is recommended for its speed and stability.

1.  Open **Developer options**.
2.  Scroll down to the **Debugging** section.
3.  Toggle the **USB debugging** switch to **ON**.
4.  Confirm the prompt if asked.
5.  Connect your phone to your computer via a USB cable.
6.  On your phone, you might see a prompt: "Allow USB debugging?". Check "Always allow from this computer" and tap **Allow**.

#### Option B: Connect via Wi-Fi (Android 11+)

1.  Ensure your computer and phone are connected to the same Wi-Fi network.
2.  Open **Developer options** on your phone.
3.  Scroll down to the **Debugging** section and toggle **Wireless debugging** to **ON**.
4.  Tap on the **Wireless debugging** text (not the toggle) to open its settings.
5.  In Android Studio, open the **Device Manager** (via the toolbar or `Tools > Device Manager`).
6.  Click the **+** (Add Device) button or look for a "Pair using Wi-Fi" option in the physical devices tab.
7.  Scan the QR code displayed in Android Studio using your phone's **Pair device with QR code** option, or enter the pairing code.

### 3. Run the App

1.  Open this project in **Android Studio**.
2.  Wait for Gradle to sync (this might take a few minutes the first time).
3.  In the toolbar at the top, look for the device dropdown menu.
4.  Select your connected device (USB or Wi-Fi) from the list.
5.  Click the green **Run** (Play) button (or press `Shift + F10`).
6.  The app will build, install, and open on your phone.

## Troubleshooting

-   **Gradle Sync Failed**: Check your internet connection and ensure you have the correct Android SDK platforms installed via the SDK Manager in Android Studio.
-   **Device Not Found**: Ensure your USB cable supports data transfer (not just charging) and that USB debugging is active.

