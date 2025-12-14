# Agent Instructions

## Commands
- **Build**: `./gradlew assembleDebug` (Strict mode: `allWarningsAsErrors = true`)
- **Test**: `export ANDROID_SERIAL=emulator-5554 && ./gradlew connectedAndroidTest`
  - *Note:* Requires stable API 34+ emulator (e.g. `PIXEL_API_34`). Avoid API 36 Preview.
  - *Important:* Ensure the emulator is running before starting tests to protect physical device data.
- **Single Test**: `./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=package.ClassName#testName`

## Code Style
- **Kotlin**: Lift assignments out of `if` (e.g., `val x = if (c) a else b`).
- **Compose**: Follow State Hoisting. Use `remember { mutableStateOf(...) }`.
- **Formatting**: 4 spaces indent. LF line endings.
- **Imports**: No wildcards. Keep `androidx` and `io.github` distinct.
- **Naming**: `PascalCase` for Composables, `camelCase` for state/functions.
- **Linting**: Unused variables will fail the build. Remove them.
- **Architecture**: MVVM with Room. `WeightViewModel` manages UI state.
