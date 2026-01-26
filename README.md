# Quick Route Map

An offline navigation application that provides audio guidance for routes on roads, in cities, and in nature, without requiring a data connection.

## Overview

Quick Route Map is designed for users who need reliable navigation in areas with poor or no data coverage. Once you load a route into the app, it automatically downloads the necessary maps from OpenStreetMap, allowing you to navigate completely offline. The app provides voice guidance even when your device screen is off, making it perfect for outdoor activities, remote areas, and data-conscious travelers.

## Key Features

- **Offline Navigation**: Navigate without internet connectivity after initial route and map download
- **Automatic Map Downloads**: Maps are automatically downloaded from OpenStreetMap based on your route
- **Audio Guidance**: Turn-by-turn voice instructions to keep you on track
- **Background Operation**: Receive guidance while your device screen is off or you're using other apps
- **Multi-Terrain Support**: Suitable for roads, urban environments, and natural trails

## Development Status

⚠️ **This application is currently in active development.** Features and functionality are being continuously improved and expanded. Expect updates and new capabilities in future releases.

## Android Permissions Required

The following permissions are necessary for Quick Route Map to function properly:

### Essential Permissions

- **Location Access** (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)
  - Required for GPS tracking and route navigation
  - Enables the app to determine your current position

- **Background Location** (`ACCESS_BACKGROUND_LOCATION`)
  - Allows the app to provide guidance when running in the background or when the screen is off
  - Required for Android 10 (API level 29) and above

- **Internet Access** (`INTERNET`)
  - Needed for initial route loading and map downloads from OpenStreetMap
  - Not required during actual navigation

- **Storage Access** (`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`)
  - Required to save downloaded maps locally
  - Enables offline functionality

- **Wake Lock** (`WAKE_LOCK`)
  - Keeps the device awake when providing audio guidance
  - Ensures uninterrupted navigation

### Granting Permissions

When you first launch Quick Route Map, you'll be prompted to grant these permissions. For optimal functionality:

1. Allow location access at all times (for background guidance)
2. Grant storage permissions to enable map downloads
3. Ensure the app is not restricted by battery optimization settings

You can modify these permissions later in your device's Settings under Apps → Quick Route Map → Permissions.

## Planned Features

The following functionality is planned for upcoming releases:

- **Internationalization**: Support for multiple languages with translatable texts
  - English
  - Spanish
  - German
  - French

- **Route Editing**: Ability to modify routes and customize guidance texts

- **Points of Interest**: Display POIs along your route or in nearby areas

- **Route Sharing**: Share your routes with other users

## Installation

*Installation instructions will be added when the app is released.*

## Usage

1. Load your desired route into the application
2. The app will automatically download necessary map tiles from OpenStreetMap
3. Start navigation and receive audio guidance
4. Navigate offline without worrying about data connectivity

## Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the issues page.

## License

[MIT License](LICENSE.txt)

## Contact

**David Pérez** [davidpm.itengineer@gmail.com](mailto://davidpm.itengineer@gmail.com)

---

**Note**: Currently, the application interface is available only in Spanish. Multi-language support will be implemented in future updates.
