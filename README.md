<p align="center">
  <img src="./images/loopy_logo.png" width="180">
</p>

<h1 align="center">Loopy</h1>
<h3 align="center">Distributed Health Monitoring System</h3>

<p align="center">
  A modular distributed health monitoring system based on a wearable Raspberry Pi armband,
  Android app, cloud backend and ML/AI analysis modules.
</p>

<!--=========================================================================-->

Loopy is a **distributed health monitoring system** based on a wearable armband device. It continuously collects, processes, and visualizes physiological and activity-related data through a modular and scalable architecture.

The system is composed of:
- a wearable embedded device (Raspberry Pi),
- an Android mobile application,
- multiple backend servers,
- data analysis and machine learning modules.

---

## Project Overview

Loopy is designed to provide **high-quality health monitoring** with improved comfort compared to traditional smartwatches.

**Main features:**
- Continuous acquisition of physiological data (heart rate, movement, temperature, blood oxygenation, etc.).
- Cloud-based data processing and storage.
- Advanced analytics using machine learning.
- AI-powered chatbot for personalized insights.
- Real-time visualization through a mobile application.

---

## System Architecture

The architecture is **modular and distributed**, composed of four main layers:

### 1. Embedded Layer (Raspberry Pi 3B+)
- Hosts the wearable device logic.
- Collects sensor data via Python scripts.
- Runs a JVM-based embedded application server.
- Sends data to backend servers.

### 2. Backend Layer
Two cloud-based application servers:
- **Application Server 1**: Handles user authentication, device management,data storage and communicates directly with the mobile app.
- **Application Server 2**: Dedicated to data processing, machine learning models, and the AI-powered chatbot.

### 3. Mobile Application Layer
- Android application written in Kotlin.
- Displays health data and manages the wearable device.
- Enables interaction with the AI assistant.

### 4. Data Flow Overview
1. Sensors collect physiological data.
2. Data is transmitted to backend servers.
3. Backend servers process and analyze the data.
4. Results are sent to the mobile application for visualization.

---

## Project Layout

The repository is organized as follows:

```
├── application-server-1
│   ├── build.gradle.kts
│   ├── Downloads
│   ├── gradle
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   ├── settings.gradle.kts
│   └── src
│       └── main
│           └── kotlin
│               ├── database
│               │   ├── dao
│               │   │   ├── TabellaAccelerometroEntity.kt
│               │   │   ├── TabellaElettrodiEntity.kt
│               │   │   ├── TabellaPpgEntity.kt
│               │   │   ├── TabellaSensorsStatusEntity.kt
│               │   │   ├── TabellaTermometroEntity.kt
│               │   │   └── TabellaUserEntity.kt
│               │   ├── DatabaseConfig.kt
│               │   ├── QueryManager.kt
│               │   └── tables
│               │       ├── TabellaAccelerometroTable.kt
│               │       ├── TabellaElettrodiTable.kt
│               │       ├── TabellaPpgTable.kt
│               │       ├── TabellaSensorsStatusTable.kt
│               │       ├── TabellaTermometroTable.kt
│               │       └── TabellaUserTable.kt
│               ├── Main.kt
│               ├── models
│               │   ├── AccelerometerData.kt
│               │   ├── ElectrodeData.kt
│               │   ├── PPGData.kt
│               │   └── TermometerData.kt
│               ├── scripts
│               │   └── MainScript.kt
│               └── server
│                   ├── jsonModels
│                   │   ├── inputJsons
│                   │   │   ├── AgentJson.kt
│                   │   │   ├── RegisterJson.kt
│                   │   │   ├── SaveDataJson.kt
│                   │   │   └── UserJson.kt
│                   │   └── outputJsons
│                   │       ├── AccountJson.kt
│                   │       ├── CsvDataJson.kt
│                   │       ├── PredictJson.kt
│                   │       ├── ReturnDataJson.kt
│                   │       ├── StatusJson.kt
│                   │       └── UserDataJson.kt
│                   └── ServerConfig.kt
├── application-server-2
│   ├── build.gradle.kts
│   ├── gradle
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   ├── settings.gradle.kts
│   └── src
│       └── main
│           └── kotlin
│               ├── aiAgent
│               │   ├── AgentCreation.kt
│               │   ├── customModels
│               │   │   └── OpenRouterCustomModels.kt
│               │   └── strategies
│               │       └── SimpleStrategy.kt
│               ├── database
│               │   ├── dao
│               │   │   ├── TabellaActivityEntity.kt
│               │   │   ├── TabellaGlucosioEntity.kt
│               │   │   ├── TabellaSleepEntity.kt
│               │   │   └── TabellaStressEntity.kt
│               │   ├── DatabaseConfig.kt
│               │   ├── QueryManager.kt
│               │   └── table
│               │       ├── TabellaActivityTable.kt
│               │       ├── TabellaGlucosioTable.kt
│               │       ├── TabellaSleepTable.kt
│               │       └── TabellaStressTable.kt
│               ├── graph
│               │   └── GraphsManagement.kt
│               ├── Main.kt
│               ├── script
│               │   └── MainScript.kt
│               ├── server
│               │   ├── inputJsons
│               │   │   ├── AgentJson.kt
│               │   │   ├── CsvDataJson.kt
│               │   │   ├── RegisterJson.kt
│               │   │   ├── ReturnDataJson.kt
│               │   │   ├── SaveDataJson.kt
│               │   │   ├── UserDataJson.kt
│               │   │   └── UserJson.kt
│               │   ├── outputJsons
│               │   │   ├── AccountJson.kt
│               │   │   ├── PredictJson.kt
│               │   │   ├── ReturnSSAGDataJson.kt
│               │   │   └── StatusJson.kt
│               │   └── ServerConfig.kt
│               └── utils
│                   └── URL.kt
├── documentation
│   ├── Functional_Sheet_Loopy.pdf
│   └── Technical_Sheet_Loopy.pdf
├── glucose-calculator-ML
│   ├── csvs
│   │   └── result.csv
│   ├── models
│   │   └── model_rf.pkl
│   ├── predict.py
│   └── train.py
├── metric-calculator
│   ├── calc_metriche_diurne.py
│   ├── calc_metriche_notturne.py
│   ├── db_utils.py
│   └── TODO.txt
├── mobile-app
│   ├── app
│   │   ├── build.gradle.kts
│   │   ├── proguard-rules.pro
│   │   └── src
│   │       ├── androidTest
│   │       ├── main
│   │       │   ├── AndroidManifest.xml
│   │       │   ├── ic_launcher-playstore.png
│   │       │   ├── java
│   │       │   │   └── com
│   │       │   │       └── example
│   │       │   │           └── loopy
│   │       │   │               ├── chat
│   │       │   │               │   ├── ChatActivity.kt
│   │       │   │               │   └── scripts
│   │       │   │               │       ├── AgentJson.kt
│   │       │   │               │       └── ChatCaller.kt
│   │       │   │               ├── data
│   │       │   │               │   ├── DataActivity.kt
│   │       │   │               │   └── models
│   │       │   │               │       ├── DataDisplay.kt
│   │       │   │               │       ├── DataViewModel.kt
│   │       │   │               │       └── input
│   │       │   │               │           ├── ReturnDataJson.kt
│   │       │   │               │           └── ReturnSSAGDataJson.kt
│   │       │   │               ├── devicemanager
│   │       │   │               │   ├── DeviceManagerActivity.kt
│   │       │   │               │   └── models
│   │       │   │               │       └── StatusJson.kt
│   │       │   │               ├── login
│   │       │   │               │   ├── LoginActivity.kt
│   │       │   │               │   ├── models
│   │       │   │               │   │   ├── input
│   │       │   │               │   │   │   ├── RegisterJson.kt
│   │       │   │               │   │   │   └── UserJson.kt
│   │       │   │               │   │   └── output
│   │       │   │               │   │       └── AccountJson.kt
│   │       │   │               │   ├── RegisterActivity.kt
│   │       │   │               │   └── RegisterListSet.kt
│   │       │   │               ├── MainActivity.kt
│   │       │   │               ├── network
│   │       │   │               │   └── KtorClient.kt
│   │       │   │               ├── profile
│   │       │   │               │   ├── json
│   │       │   │               │   │   └── UserDataJson.kt
│   │       │   │               │   └── ProfileActivity.kt
│   │       │   │               ├── settings
│   │       │   │               │   ├── EditAccountActivity.kt
│   │       │   │               │   ├── EditAccountListSet.kt
│   │       │   │               │   └── SettingsActivity.kt
│   │       │   │               ├── ui
│   │       │   │               │   └── theme
│   │       │   │               │       ├── Color.kt
│   │       │   │               │       ├── Theme.kt
│   │       │   │               │       └── Type.kt
│   │       │   │               └── utils
│   │       │   │                   ├── BaseActivity.kt
│   │       │   │                   ├── GraphAdapter.kt
│   │       │   │                   ├── SessionManager.kt
│   │       │   │                   └── URL.kt
│   │       │   └── res
│   │       │       ├── color
│   │       │       │   └── bottom_nav_selector.xml
│   │       │       ├── drawable
│   │       │       │   ├── agent_message_text_background.xml
│   │       │       │   ├── bg_chat.png
│   │       │       │   ├── bg_empty.png
│   │       │       │   ├── bg_home.png
│   │       │       │   ├── bg_login.png
│   │       │       │   ├── bg_profile.png
│   │       │       │   ├── bg_register.png
│   │       │       │   ├── bottom_nav_bg.xml
│   │       │       │   ├── circular_container.xml
│   │       │       │   ├── edit_text_background.xml
│   │       │       │   ├── ic_chatbot.xml
│   │       │       │   ├── ic_data.xml
│   │       │       │   ├── ic_dm.xml
│   │       │       │   ├── ic_home.xml
│   │       │       │   ├── ic_launcher_background.xml
│   │       │       │   ├── ic_launcher_foreground.xml
│   │       │       │   ├── ic_profile.xml
│   │       │       │   ├── ic_settings.xml
│   │       │       │   ├── login_button_background.xml
│   │       │       │   ├── loopy_bot_avatar.png
│   │       │       │   ├── loopy_hompage_recap.xml
│   │       │       │   ├── loopy_logo.png
│   │       │       │   ├── rounded_button.xml
│   │       │       │   └── user_message_text_background.xml
│   │       │       ├── font
│   │       │       │   ├── adlam_display.xml
│   │       │       │   ├── alex_brush.ttf
│   │       │       │   ├── alfa_slab_one.xml
│   │       │       │   └── dancingscript_variablefont_wght.ttf
│   │       │       ├── layout
│   │       │       │   ├── chat_activity.xml
│   │       │       │   ├── data_activity.xml
│   │       │       │   ├── dm_activity.xml
│   │       │       │   ├── edit_account_activity.xml
│   │       │       │   ├── edit_profile_activity.xml
│   │       │       │   ├── item_graph.xml
│   │       │       │   ├── login_activity.xml
│   │       │       │   ├── main_activity.xml
│   │       │       │   ├── profile_activity.xml
│   │       │       │   ├── register_activity.xml
│   │       │       │   ├── settings_activity.xml
│   │       │       │   └── view_bottom_nav.xml
│   │       │       ├── menu
│   │       │       │   └── bottom_nav_menu.xml
│   │       │       ├── mipmap-anydpi-v26
│   │       │       │   ├── ic_launcher_round.xml
│   │       │       │   └── ic_launcher.xml
│   │       │       ├── mipmap-hdpi
│   │       │       │   ├── ic_launcher_round.webp
│   │       │       │   └── ic_launcher.webp
│   │       │       ├── mipmap-mdpi
│   │       │       │   ├── ic_launcher_round.webp
│   │       │       │   └── ic_launcher.webp
│   │       │       ├── mipmap-xhdpi
│   │       │       │   ├── ic_launcher_round.webp
│   │       │       │   └── ic_launcher.webp
│   │               ├── mipmap-xxhdpi
│   │               │   ├── ic_launcher_round.webp
│   │               │   └── ic_launcher.webp
│   │               ├── mipmap-xxxhdpi
│   │               │   ├── ic_launcher_round.webp
│   │               │   └── ic_launcher.webp
│   │               ├── values
│   │               │   ├── colors.xml
│   │               │   ├── font_certs.xml
│   │               │   ├── preloaded_fonts.xml
│   │               │   ├── strings.xml
│   │               │   └── themes.xml
│   │               └── xml
|   |               ├── backup_rules.xml
|   |               └── data_extraction_rules.xml
│   ├── build.gradle.kts
│   ├── gradle
│   │   ├── libs.versions.toml
│   │   └── wrapper
│   │       ├── gradle-wrapper.jar
│   │       └── gradle-wrapper.properties
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle.kts
├── raspberry-pi-server-logic
│   ├── main-server
│   │   ├── build.gradle.kts
│   │   ├── gradle
│   │   │   └── wrapper
│   │   │       ├── gradle-wrapper.jar
│   │   │       └── gradle-wrapper.properties
│   │   ├── gradle.properties
│   │   ├── gradlew
│   │   ├── gradlew.bat
│   │   ├── settings.gradle.kts
│   │   └── src
│   │       └── main
│   │           └── kotlin
│   │               ├── database
│   │               │   ├── dao
│   │               │   │   ├── TabellaAccelerometroEntity.kt
│   │               │   │   ├── TabellaElettrodiEntity.kt
│   │               │   │   ├── TabellaPpgEntity.kt
│   │               │   │   └── TabellaTermometroEntity.kt
│   │               │   ├── DatabaseConfig.kt
│   │               │   ├── QueryManagement.kt
│   │               │   └── tables
│   │               │       ├── TabellaAccelerometroTable.kt
│   │               │       ├── TabellaElettrodiTable.kt
│   │               │       ├── TabellaPpgTable.kt
│   │               │       └── TabellaTermometroTable.kt
│   │               ├── Main.kt
│   │               ├── models
│   │               │   ├── AccelerometerData.kt
│   │               │   ├── ElectrodeData.kt
│   │               │   ├── PPGData.kt
│   │               │   └── TermometerData.kt
│   │               ├── scripts
│   │               │   └── MainScript.kt
│   │               └── server
│   │                   ├── inputJsons
│   │                   │   └── SaveDataJson.kt
│   │                   ├── outputJsons
│   │                   │   ├── ReturnDataJson.kt
│   │                   │   └── StatusJson.kt
│   │                   └── ServerConfig.kt
│   └── raspberry-sensors-logic
│       ├── accelerometer_sensor.py
│       ├── electrode_sensor.py
│       ├── ppg_sensor.py
│       └── termometrer_sensor.py
```

### Source Code Organization

* **`mobile-app/`**
  Android application developed in Kotlin for data visualization and user interaction.

* **`application-server-1/`**
  Backend server responsible for authentication, device management, and data storage.

* **`application-server-2/`**
  Backend server dedicated to data analysis, machine learning, and the AI chatbot.

* **`raspberry-pi-3B+-server-logic/`**
  Software running on the wearable device:
  * `main-server/`: JVM-based embedded server.
  * `raspberry-sensors-logic/`: Python scripts for direct sensor interaction.

* **`metric-calculator/`**
  Python module for computing daily and nightly health metrics.

* **`glucose-calculator-ML/`**
  Machine learning module for glucose level prediction.

* **`documentation/`**
  Project documentation, slides, and additional resources.

---

## Hardware and Software Requirements

### Hardware Requirements
- Raspberry Pi 3B+ (wearable device)
- Health monitoring sensors
- Android smartphone
- Cloud infrastructure (e.g., AWS EC2 or equivalent)

<p align="center">
  <img src="./images/Raspberry_pi_3B+.jpg" width="420">
</p>

### Software Requirements
- Android Studio (latest stable version)
- Java Development Kit (JDK)
- Python 3
- Gradle (via Gradle Wrapper)
- Git 

---

## How to Build and Run the Project

### Mobile Application
1. Open the `mobile-app/` directory with Android Studio.
2. Sync the project with Gradle files.
3. Build and run the application on an Android device or emulator.

### Backend Application Servers
1. Navigate to `application-server-1/` or `application-server-2/`.
2. Build the project using the Gradle Wrapper:

        ./gradlew build

3. Run the server as a standalone JVM application. Can be deployed on cloud infrastructure or run locally.

### Embedded Raspberry Pi Components
- **JVM Server**: Build using Gradle and run on the Raspberry Pi 3B+.
- **Python Scripts**: Execute directly on the Raspberry Pi to interface with sensors.

### Data Processing and ML Modules
- Python-based modules found in `metric-calculator/` and `glucose-calculator-ML/`.
- Can be executed independently for data analysis, model training, and predictions.

---

## User Guide

### Account Management
- **Registration**: Create an account via the mobile application.
- **Profile**: Manage personal information in the settings page.
- **Deletion**: Accounts can be deleted at any time from settings.

### Application Usage
- **Home Page**: Overview of current health status and recent metrics.
- **Data Page**: Detailed numerical health data and history.
- **Chat Page**: Interact with the AI-powered assistant for health advice.
- **Device Manager**: Check the status of the wearable device and sensors.

---

## Team Members and Contributions

* **Nicola Avellino**: Embedded development, data management logic, Raspberry Pi 3B+ sensor implementation, Android mobile application development.
* **Simone Battisti**: Backend services, data processing modules, AI/ML integration, system architecture, Android mobile application development, documentation.
* **Liam Demattè**: Embedded system logic, sensor integration, hardware architecture, Android mobile application development.
* **Riccardo Gonzato**: Android mobile application development, system integration, documentation.

All team members collaborated on system design, testing, and overall project integration.

---

## External Resources

* **Project Repository**: (https://github.com/Sim0Batt/Loopy).
* **Presentation Slides**: Available in the `documentation/` folder.
* **Demo Video**: [YouTube Link](link_to_video).
