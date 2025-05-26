# <a href="Playback Music Logo"><img src="/logo.png" align="left" height="60px" width="60px" ></a> Playback Music Player

A simple Android application that plays locally stored audio files, allowing playback in the background while using other apps. This project leverages modern Android libraries to provide a seamless user experience.

## 📱 Features

- Play audio files stored locally on the device.
- Background playback while using other apps.
- Easy-to-use interface for browsing and selecting audio files.
- Supports modern Android architecture with dependency injection, coroutine support, and more.

## 🧰 Tech Stack & Open-source Libraries

- **Minimum SDK**: 21
- **Language**: [Kotlin](https://kotlinlang.org/)
- **Async**: [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) for background tasks and main-safe operations

### ⚙️ Jetpack Components
- **ViewBinding**: Simplifies code interaction with views
- **Lifecycle**: Observes Android lifecycle changes for better UI state handling
- **ViewModel**: Holds and manages UI-related data in a lifecycle-conscious way
- **Navigation**: Handles fragment navigation with safe args
- **Room**: Provides an abstraction layer over SQLite
- **DataStore + Proto**: Modern data storage solution replacing SharedPreferences
- **WorkManager**: Schedules deferrable background tasks
- **SplashScreen API**: Handles launch animation on supported devices
- **Palette API**: Extracts prominent colors from images
- **Media3**: Official Jetpack media playback library

### 🛠 Architecture
- **MVVM**: Clean separation of concerns between UI, logic, and data layers
- **Repository Pattern**: Abstracts data sources (e.g., Room, Media3, etc.)
- **KSP**: [Kotlin Symbol Processing](https://github.com/google/ksp) for annotation processing

### 🔌 Dependency Injection
- **[Hilt](https://dagger.dev/hilt/)**: Modern DI framework for Android

### 🎧 Media & UI Enhancements
- **Media3 (ExoPlayer)**: Media playback with media3 exoplayer with media3 session.
- **Coil**: Lightweight image loading library with GIF support

### 🎨 UI & Google Services
- **Material Components**: Official material design UI components.
- **Play Core (In-app updates)**: Provides In-App Updates.
- **Gson**: JSON serialization/deserialization.


## Acknowledgments

- [Android Developers](https://developer.android.com/)
- [Material Design](https://material.io/design)


# License
```xml
Designed and developed by 2020 Ravikant Sharma

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```