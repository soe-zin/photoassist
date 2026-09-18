# Photography Assistant for Android — Design Specification

## 1. Overview

This document outlines the system architecture and module breakdown for the Photography Assistant Android application. The application provides exposure metering, lux measurement, solar position calculations, and photography utilities with a focus on offline-first operation and privacy preservation.

## 2. System Architecture

### 2.1 Architecture Pattern

**MVVM (Model-View-ViewModel)** with clean separation of concerns:
- **Presentation Layer**: Jetpack Compose UI components
- **ViewModel Layer**: Business logic and UI state management
- **Domain Layer**: Core business logic and mathematical calculations
- **Data Layer**: Persistence, camera operations, and external integrations

### 2.2 Layer Separation

```
Presentation (UI)
├── Views (Jetpack Compose)
├── ViewModels (UI State & Business Logic)
│   ├── ExposureViewModel
│   ├── LuxMeterViewModel
│   ├── SolarPositionViewModel
│   └── CalculatorViewModel
Domain (Business Logic)
├── Models (Data classes, Value Objects)
├── Calculations (Exposure math, Solar math, Lux estimation)
├── Validators (Input validation)
│   └── CalibrationProfile
Data (Infrastructure)
├── Persistence (Room databases, JSON/CSV export)
├── Camera (Camera2 API, Preview handling)
├── Location (GPS, manual coordinate input)
└── Services (Background processing)
```

## 3. Module Breakdown

### 3.1 Core Mathematical Modules

#### 3.1.1 Exposure Calculator
- **Purpose**: Calculate exposure values and recommendations
- **Location**: `domain/exposure` package
- **Components**:
  - `ExposureCalculator` - Main calculation engine
  - `ExposureValue` - Value object for exposure calculations
  - `ExposureValidator` - Input validation

#### 3.1.2 Lux Calculator
- **Purpose**: Convert camera data to lux measurements
- **Location**: `domain/lux` package
- **Components**:
  - `LuxCalculator` - Lux estimation algorithms
  - `LuxMeasurement` - Value object for lux data
  - `CalibrationProfile` - Camera calibration data

#### 3.1.3 Solar Position Calculator
- **Purpose**: Calculate sunrise, sunset, golden hour, and sun direction
- **Location**: `domain/solar` package
- **Components**:
  - `SolarCalculator` - Astronomical calculations
  - `SolarPosition` - Position data structure
  - `GoldenHourCalculator` - Golden hour computations

### 3.2 Camera Integration Modules

#### 3.2.1 Camera Service
- **Purpose**: Camera2 API integration with lifecycle management
- **Location**: `data/camera` package
- **Components**:
  - `CameraManager` - Camera device discovery and configuration
  - `PreviewHandler` - Camera preview rendering
  - `FrameProcessor` - Frame-by-frame processing pipeline
  - `RawImageHandler` - RAW sensor data processing

#### 3.2.2 Exposure Meter Module
- **Purpose**: Live exposure metering from camera
- **Location**: `feature/exposure-meter` package
- **Components**:
  - `LiveExposureProvider` - Real-time exposure data
  - `ExposureHistoryStore` - Measurement persistence

### 3.3 Data Management Modules

#### 3.3.1 Measurement Repository
- **Purpose**: Unified access to all measurement data
- **Location**: `data/repository` package
- **Components**:
  - `MeasurementRepository` - Abstract data source
  - `LocalMeasurementDataSource` - Room database implementation
  - `ExposureDao`, `LuxDao`, `SolarDao` - Database access objects

#### 3.3.2 Project Management
- **Purpose**: Organize measurements into projects
- **Location**: `feature/projects` package
- **Components**:
  - `ProjectManager` - Project CRUD operations
  - `ProjectRepository` - Project data access

### 3.4 UI Modules

#### 3.4.1 Exposure Meter UI
- **Purpose**: Live exposure meter display with recommendations
- **Location**: `ui/exposure-meter` package
- **Features**:
  - Real-time meter display
  - EV, ISO, aperture, shutter speed controls
  - Historical chart visualization
  - Calibration status indicator

#### 3.4.2 Lux Meter UI
- **Purpose**: Combined light meter/lux meter display
- **Location**: `ui/lux-meter` package
- **Features**:
  - Live lux measurement
  - Calibration profile management
  - Relative brightness comparison
  - Historical trends

#### 3.4.3 Solar Position UI
- **Purpose**: Sunrise/sunset/golden hour calculator
- **Location**: `ui/solar-position` package
- **Features**:
  - Interactive sun direction visualization
  - Golden hour timing calculator
  - Manual coordinate input
  - Historical solar data

#### 3.4.4 Calculator UI
- **Purpose**: Photography utility calculators
- **Location**: `ui/calculator` package
- **Features**:
  - Depth of field calculator
  - Field of view calculator
  - Crop factor calculator
  - Lens focal length converter

### 3.5 Supporting Modules

#### 3.5.1 Navigation Module
- **Purpose**: App-wide navigation and routing
- **Location**: `feature/navigation` package
- **Components**:
  - `AppNavigation` - Navigation graph
  - `NavigationHost` - Container for all screens

#### 3.5.2 Authentication/Onboarding Module
- **Purpose**: Initial setup and calibration guidance
- **Location**: `feature/onboarding` package
- **Features**:
  - App introduction
- **Calibration setup**
  - Camera capability discovery
- Permission explanations

#### 3.5.3 Export Module
- **Purpose**: Data export functionality
- **Location**: `feature/export` package
- **Features**:
  - JSON/CSV export
  - Data sharing options
  - File management

## 4. Database Schema

### 4.1 Room Database Structure

#### 4.1.1 Exposure Measurements Table
```sql
CREATE TABLE exposure_measurements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,
    ev REAL NOT NULL,
    iso INTEGER NOT NULL,
    aperture REAL NOT NULL,
    shutter_speed REAL NOT NULL,
    camera_make TEXT,
    camera_model TEXT,
    calibration_profile_id INTEGER,
    location_latitude REAL,
    location_longitude REAL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

#### 4.1.2 Lux Measurements Table
```sql
CREATE TABLE lux_measurements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,
    lux REAL NOT NULL,
    estimated INTEGER NOT NULL,
    calibration_applied INTEGER NOT NULL,
    calibration_profile_id INTEGER,
    camera_make TEXT,
    camera_model TEXT,
    location_latitude REAL,
    location_longitude REAL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

#### 4.1.3 Solar Calculations Table
```sql
CREATE TABLE solar_calculations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp INTEGER NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    date INTEGER NOT NULL,
    sunrise REAL NOT NULL,
    sunset REAL NOT NULL,
    golden_hour_start REAL NOT NULL,
    golden_hour_end REAL NOT NULL,
    sun_direction REAL NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

#### 4.1.4 Calibration Profiles Table
```sql
CREATE TABLE calibration_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    camera_make TEXT NOT NULL,
    camera_model TEXT NOT NULL,
    calibration_data TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    is_default INTEGER NOT NULL
);
```

#### 4.1.5 Projects Table
```sql
CREATE TABLE projects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

#### 4.1.6 Project Measurements Junction Table
```sql
CREATE TABLE project_measurements (
    project_id INTEGER NOT NULL,
    measurement_id INTEGER NOT NULL,
    measurement_type TEXT NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (measurement_id) REFERENCES exposure_measurements(id),
    PRIMARY KEY (project_id, measurement_id, measurement_type)
);
```

## 5. Technical Specifications

### 5.1 Android Compatibility

- **Minimum SDK**: Android 8.0 (API Level 26)
- **Target SDK**: Android 34 (current)
- **Build Tools**: Android Gradle Plugin 8.0+
- **Kotlin**: Kotlin 1.9+
- **Coroutines**: Kotlinx Coroutines 1.7+

### 5.2 Dependencies

```kotlin
dependencies {
    // Core
    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"
    implementation "androidx.activity:activity-compose:1.8.2"
    implementation "androidx.compose:compose-bom:2023.10.00"
    
    // Architecture
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.6.2"
    implementation "androidx.navigation:navigation-compose:2.7.6"
    implementation "androidx.hilt:hilt-navigation-compose:1.1.0"
    
    // Camera
    implementation "androidx.camera:camera-camera2:1.3.0"
    implementation "androidx.camera:camera-lifecycle:1.3.0"
    implementation "androidx.camera:camera-view:1.3.0"
    implementation "androidx.camera:camera-extensions:1.3.0"
    
    // Persistence
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"
    
    //DI
    implementation "com.google.dagger:hilt-android:2.48"
    kapt "com.google.dagger:hilt-compiler:2.48"
    
    //Charts
    implementation "com.github.PhilJay:MPAndroidChart:3.0.3"
    
    // Testing
    testImplementation "junit:junit:4.13.2"
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.compose:compose-bom:2023.10.00"
    debugImplementation "androidx.compose:compose-bom:2023.10.00"
}
```

### 5.3 Multi-threading

- **Heavy Processing**: Run on `Dispatchers.IO`
- **UI Updates**: Main thread only
- **Camera Processing**: Dedicated worker threads
- **Database Operations**: Room's built-in threading

### 5.4 Memory Management

- **Image Processing**: Use `BitmapFactory.Options` for efficient decoding
- **Frame Processing**: Process frames in batches to reduce overhead
- **Resource Lifecycle**: Proper camera and image buffer release
- **Composition Local**: Use `CompositionLocal` for dependency injection

### 5.5 Offline-First Design

- **No Network Permissions**: Explicitly remove `android.permission.INTERNET`
- **Local-First Data**: All features work without network connectivity
- **Offline Validation**: Input validation works offline
- **Fallback Behavior**: Clear error messages for unsupported features

### 5.6 Performance Considerations

- **Throttling**: Throttled measurement updates (100ms intervals)
- **Resolution Scaling**: Lower resolution processing for performance
- **Lazy Loading**: Load data on-demand
- **Background Processing**: Use `WorkManager` for heavy tasks

## 6. Data Flow

### 6.1 Typical Flow - Exposure Meter

1. **UI Layer**: User opens Exposure Meter screen
2. **ViewModel**: Requests live exposure data
3. **Repository**: Queries camera service for exposure data
4. **Camera Service**: Uses Camera2 API to capture frame
5. **Frame Processing**: Calculates exposure values
6. **Domain**: Validates and stores exposure calculations
7. **Database**: Persists measurement
8. **UI**: Updates display with latest data

### 6.2 Calibration Flow

1. **UI**: User accesses calibration screen
2. **ViewModel**: Manages calibration profile creation
3. **Domain**: Validates calibration data
4. **Database**: Stores calibration profile
5. **UI**: Provides calibration feedback