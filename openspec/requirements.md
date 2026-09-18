# Photography Assistant for Android — Requirements Specification

## 1. Document Purpose

This document defines the functional, technical, architectural, and non-functional requirements for an offline-first Android photography assistant application.

The document is intended to be placed in the `openspec` folder and used by Cline as the implementation reference.

The application will provide:

1. Exposure Meter
2. Combined Light Meter / Lux Meter
3. Sunrise, Sunset, Golden Hour, and Sun Direction tools
4. Photography utility calculators
5. Measurement history and project organization

The following features are explicitly excluded from the initial scope:

- Light Flicker Meter
- Color Meter
- Depth of Field Calculator
- Field of View Calculator

The application must not require network access.

---

## 2. Product Principles

### 2.1 Offline-first and privacy-preserving

The application must operate entirely offline.

The application must not request, declare, or use:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

The `android.permission.INTERNET` permission must be omitted from the final manifest.

No feature may depend on:

- Cloud APIs
- Remote services
- Analytics SDKs that require network access
- Advertisement SDKs
- Online geocoding
- Online weather services
- Online map tiles
- Remote configuration
- Account registration

GPS/location data must be used locally only for solar calculations. The application must not transmit location data.

### 2.2 Measurement honesty

The application must distinguish between:

- Calculated values
- Camera-derived estimates
- Calibrated measurements
- Unsupported or unavailable measurements

The app must not present camera-derived lux or exposure values as laboratory-grade measurements.

The user interface must display an appropriate accuracy or calibration status.

### 2.3 Separation of concerns

The user interface must be separated from camera acquisition, frame processing, measurement mathematics, and persistence.

Heavy processing must not run on the Android main thread.

### 2.4 Deterministic mathematical core

All exposure, lux-estimation, solar-position, and related calculations must be implemented in testable, deterministic Kotlin code that does not depend on Android UI classes.

The mathematical core must have unit tests covering normal values, boundary values, and invalid inputs.

---

## 3. Platform and Build Requirements

### 3.1 Android version

- Minimum SDK: Android 8.0, API Level 26
- Target SDK: Use the current project-approved target SDK
- Language: Kotlin
- UI framework: Jetpack Compose is recommended
- Architecture: MVVM or MVI with a clean separation between presentation and domain/data layers

### 3.2 Recommended technology choices

| Area | Requirement / Recommendation |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Camera | Camera2 API for advanced control and RAW access |
| Camera preview | Camera2 or CameraX where appropriate |
| Persistence | Room or SQLite through a repository abstraction |
| Coroutines | Kotlin Coroutines |
| Dependency injection | Optional; use only if it reduces complexity |
| Charts | Local/offline charting implementation or offline-compatible library |
| Location | Android location APIs |
| Sensors | Android sensor APIs, if needed for compass direction |
| Export | Local CSV and JSON export |
| Testing | JUnit, instrumented tests, and UI tests as appropriate |

Do not add a dependency that requires network access at runtime.

---

## 4. Initial Feature Scope

### 4.1 Included modules

1. Exposure Meter
2. Light Meter / Lux Meter
3. Sun and Golden Hour
4. Photography Calculators
5. Measurement History and Projects
6. Device Capability and Calibration Information

### 4.2 Excluded modules

The following must not be implemented in the initial release:

- Flicker Meter
- Color Meter
- Depth of Field Calculator
- Field of View Calculator

The architecture should remain extensible so these features can be added in a future release without restructuring the application.

---

## 5. Application Navigation

The main navigation should contain the following sections:

- Exposure
- Light / Lux
- Sun
- Calculators
- Projects
- Settings

Suggested navigation structure:

```text
Photography Assistant
├── Exposure
│   ├── Live Meter
│   ├── Exposure Calculator
│   └── Camera Profile
├── Light / Lux
│   ├── Live Lux Estimate
│   ├── Relative Brightness
│   └── Calibration
├── Sun
│   ├── Today
│   ├── Select Date
│   └── Saved Locations
├── Calculators
│   └── Exposure Utilities
├── Projects
│   ├── Project List
│   └── Measurement Details
└── Settings
    ├── Units
    ├── Camera Selection
    ├── Calibration
    └── Privacy / Permissions
```

---

# 6. Exposure Meter Requirements

## 6.1 Purpose

The Exposure Meter shall estimate scene brightness and recommend equivalent combinations of:

- Aperture
- Shutter speed
- ISO

The user shall be able to lock one, two, or all three exposure variables and calculate the remaining value or possible combinations.

The module must support both:

1. Mathematical exposure calculations
2. Live camera-based exposure estimation

## 6.2 Exposure Value definition

The application shall use the standard photographic exposure value definition at ISO 100:

```text
EV100 = log2(N² / t)
```

Where:

- `N` = f-number/aperture
- `t` = exposure time in seconds

Examples:

```text
f/4 at 1/125 s:
EV100 = log2(4² / 0.008)
      = log2(2000)
      ≈ 10.97
```

For arbitrary ISO:

```text
EVISO = EV100 + log2(ISO / 100)
```

The application must clearly define whether a displayed EV is:

- EV100
- EV adjusted for the selected ISO
- A camera-derived estimate

The preferred internal representation is `EV100`.

## 6.3 Reverse exposure formulas

Given EV100 and aperture:

```text
t = N² / 2^EV100
```

Given EV100 and shutter time:

```text
N = sqrt(t × 2^EV100)
```

Given EV100, aperture, and shutter time, the required ISO is:

```text
ISO = 100 × (N² / t) / 2^EV100
```

Equivalent exposure relationships:

- Doubling ISO increases exposure value by 1 stop.
- Doubling exposure time decreases the required aperture exposure value by 1 stop.
- Increasing the f-number by a factor of sqrt(2) changes exposure by approximately 1 stop.

All calculations must use `Double` internally.

The UI may round values for display, but calculations must use unrounded values.

## 6.4 Exposure settings model

The domain model should include:

```text
ExposureSettings
- apertureFNumber: Double?
- shutterSeconds: Double?
- iso: Double?
- ev100: Double?
- exposureCompensationEv: Double
- lockedParameter: ExposureParameter?
```

`ExposureParameter` should support:

- APERTURE
- SHUTTER
- ISO
- NONE

The implementation may use sealed classes or enums as appropriate.

## 6.5 Aperture support

The application shall support standard full-stop and third-stop aperture values.

Suggested standard values:

```text
1.0, 1.1, 1.2, 1.4, 1.6, 1.8, 2.0, 2.2, 2.5,
2.8, 3.2, 3.5, 4.0, 4.5, 5.0, 5.6, 6.3, 7.1,
8.0, 9.0, 10.0, 11.0, 13.0, 14.0, 16.0,
18.0, 20.0, 22.0, 25.0, 29.0, 32.0
```

The list should be configurable and should not be hard-coded into UI components.

Users should be able to select:

- Common aperture values
- A custom aperture value, subject to validation

## 6.6 Shutter speed support

The application shall support common shutter speeds, including:

```text
1/8000, 1/4000, 1/2000, 1/1000, 1/500,
1/250, 1/125, 1/60, 1/30, 1/15, 1/8,
1/4, 1/2, 1, 2, 4, 8, 15, 30 seconds
```

The list must be extensible.

The application should display shutter speeds in photographer-friendly notation:

- `1/250`
- `1/60`
- `0.5 s`
- `2 s`

The internal representation must be seconds as `Double`.

## 6.7 ISO support

The application shall support standard ISO values:

```text
50, 64, 80, 100, 125, 160, 200, 250, 320, 400,
500, 640, 800, 1000, 1250, 1600, 2000, 2500,
3200, 4000, 5000, 6400, 8000, 10000, 12800,
16000, 20000, 25600, 51200, 102400
```

The supported ISO range should be configurable per camera profile.

The application must distinguish between:

- Native or supported ISO values
- User-selected preferred ISO values
- Theoretical ISO values calculated mathematically

## 6.8 Exposure combination recommendations

The user shall be able to lock one parameter and request several equivalent combinations.

Examples:

- Lock aperture and calculate shutter/ISO alternatives
- Lock shutter speed and calculate aperture/ISO alternatives
- Lock ISO and calculate aperture/shutter alternatives
- Lock aperture and ISO and calculate shutter speed
- Lock aperture and shutter speed and calculate ISO

The recommendation engine must support user constraints:

```text
ExposureConstraints
- preferredIso: Double?
- minimumShutterSeconds: Double?
- maximumShutterSeconds: Double?
- minimumAperture: Double?
- maximumAperture: Double?
- minimumIso: Double?
- maximumIso: Double?
- allowedApertures: List<Double>
- allowedShutterSpeeds: List<Double>
- allowedIsos: List<Double>
```

Recommendations should be sorted according to configurable priorities, such as:

1. Minimize ISO
2. Avoid excessively slow shutter speeds
3. Stay near the selected aperture
4. Prefer standard camera settings
5. Avoid unsupported settings

The initial implementation must document the sorting strategy and test it.

## 6.9 Exposure compensation

The application shall support exposure compensation in EV/stops.

For a target compensation value:

```text
targetEV100 = measuredEV100 - exposureCompensationEv
```

The sign convention must be documented clearly in the UI.

The application must include unit tests verifying that positive compensation results in a brighter recommended exposure and negative compensation results in a darker recommended exposure.

## 6.10 Metering modes

The initial release should support:

- Center-region measurement
- Spot measurement
- Average measurement
- Adjustable measurement region

The measurement region should be represented independently of the UI.

Possible model:

```text
MeteringRegion
- centerX: Float  // normalized 0.0 to 1.0
- centerY: Float  // normalized 0.0 to 1.0
- width: Float    // normalized 0.0 to 1.0
- height: Float   // normalized 0.0 to 1.0
```

The system must validate region boundaries.

## 6.11 Live exposure display

The live exposure screen should display:

- Current estimated EV100
- Current estimated lux, if available
- Recommended aperture/shutter/ISO
- Selected or locked parameter
- Exposure compensation
- Measurement stability indicator
- Camera capability status
- Calibration status
- Warning when the camera cannot provide the required data

The UI must not update on every raw frame if doing so would cause unnecessary recomposition. The processing pipeline may process frames continuously, but the UI should receive throttled state updates.

Suggested UI update rate:

- 5–15 updates per second for live numerical values
- Configurable based on performance testing

---

# 7. Camera Acquisition and Frame Processing

## 7.1 General requirements

The application shall use Camera2 where low-level camera access is required.

The camera layer must be isolated behind an interface, for example:

```text
CameraFrameSource
- open()
- close()
- startPreview()
- startMeasurementStream()
- stopMeasurementStream()
- getCameraCapabilities()
```

The rest of the application must not directly depend on `CameraDevice`, `ImageReader`, or other Android camera implementation classes.

## 7.2 RAW support

The application should detect whether the selected camera supports:

```text
CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW
```

A device that supports this capability can provide RAW_SENSOR output and metadata suitable for processing raw sensor data.

RAW support is device-dependent and must not be assumed merely because the Android version is supported.

The application must provide a fallback mode when RAW capture is unavailable.

Android's `DngCreator` may be used for optional DNG export in a future feature, but DNG export is not required for the initial release.

## 7.3 Continuous frame processing

The application shall support continuous frame-by-frame measurement processing.

The processing pipeline should follow this structure:

```text
Camera2 / ImageReader
        │
        ▼
Raw or supported frame buffer
        │
        ▼
Frame acquisition adapter
        │
        ▼
Frame preprocessing
        │
        ▼
Region-of-interest extraction
        │
        ▼
Sensor normalization
        │
        ▼
Exposure / brightness calculations
        │
        ▼
Measurement result
        │
        ▼
Stability filtering
        │
        ▼
Throttled StateFlow / observable state
        │
        ▼
ViewModel
        │
        ▼
Compose UI
```

## 7.4 Threading requirements

- Camera callbacks must not perform heavy calculations directly.
- Frame processing must run on a dedicated coroutine dispatcher or bounded processing executor.
- The UI must run on the main thread.
- The application must avoid unbounded frame queues.
- The system should use a latest-frame-wins strategy for live measurement.
- Old frames should be discarded when processing falls behind.
- Image buffers must always be closed, including error paths.
- Memory allocations inside the hot frame-processing loop should be minimized.

Suggested processing behavior:

```text
Incoming frame
    │
    ├── If processor is idle: process latest frame
    └── If processor is busy: replace pending frame with newest frame
```

## 7.5 Frame-rate and resolution controls

The application should allow the processing pipeline to select a suitable measurement resolution rather than always using the maximum RAW resolution.

The implementation must prioritize:

1. Measurement stability
2. Low latency
3. Low battery consumption
4. Low memory usage

The application should expose diagnostic information for:

- Input frame rate
- Processed frame rate
- Dropped frame count
- Processing duration
- Average processing duration
- Current buffer size
- Thermal or performance warnings, where available

## 7.6 Camera controls

Where supported, the application should use or expose:

- Manual exposure
- Manual ISO
- Manual exposure time
- Manual focus, if needed
- Manual white balance, if needed
- AE lock
- AWB lock

The app must detect unsupported controls and disable related UI options.

The app must distinguish between requested settings and actual settings reported by `CaptureResult`.

---

# 8. RAW Frame Processing Requirements

## 8.1 RAW processing caveat

RAW processing must be implemented only when the device provides sufficient metadata.

The processing layer should account for:

- Bayer color filter arrangement
- Black-level pattern
- White level
- Sensor active array
- Pixel stride and row stride
- Bit depth and packing
- Sensor sensitivity
- Exposure time
- Sensor crop or active region
- Lens shading information, where available

The application must not assume a universal Bayer pattern or universal raw bit depth.

## 8.2 Black-level normalization

For each RAW channel, normalized intensity should be calculated using an appropriate black level and white level.

Conceptually:

```text
normalized = (rawValue - blackLevel) / (whiteLevel - blackLevel)
```

The result must be clamped to the valid range.

Where per-channel black levels are available, they must be applied per channel.

If required metadata is unavailable, the app must mark the result as estimated or unsupported.

## 8.3 Region sampling

The processing engine must support sampling a region of interest rather than processing every pixel unnecessarily.

The region sampler should support:

- Center region
- Spot region
- Average region
- Configurable normalized coordinates
- Optional downsampling
- Robust statistics

The initial release may use mean or trimmed-mean values for brightness estimation.

The implementation should avoid using saturated pixels as ordinary measurement samples.

## 8.4 Saturation handling

The system must detect clipping and saturation.

The measurement result should include:

```text
MeasurementQuality
- isValid: Boolean
- saturationFraction: Double
- underexposureFraction: Double
- stabilityScore: Double
- calibrationStatus: CalibrationStatus
- warnings: List<MeasurementWarning>
```

If too many pixels are clipped, the app should warn that the measurement may be unreliable.

---

# 9. Light Meter / Lux Meter Requirements

## 9.1 Module organization

Light Meter and Lux Meter shall be combined into one module with separate views:

- Exposure view
- Lux view
- Relative brightness view
- Calibration view

The two measurements must not be treated as identical.

Exposure value describes a photographic exposure relationship. Lux describes illuminance in lumens per square metre.

## 9.2 Lux estimation

The application may estimate lux from camera data, but the result must be labelled:

- `Estimated lux`, or
- `Calibrated lux`

The application must not claim universal accuracy across all Android devices.

Lux estimation requires a device-specific calibration model.

The model should support:

```text
LuxCalibrationProfile
- cameraId: String
- calibrationVersion: String
- coefficients: List<Double>
- referenceMeasurements: List<CalibrationReference>
- createdAt: Instant
- notes: String?
```

The exact model may initially be a simple fitted relationship between normalized sensor brightness and reference lux.

The implementation must document the model and its limitations.

## 9.3 Lux display

The lux screen shall display:

- Current lux estimate
- Measurement range
- Minimum observed lux
- Maximum observed lux
- Average lux
- Stability indicator
- Calibration status
- Measurement warnings
- Optional time-series graph

The user shall be able to start and stop a measurement session.

## 9.4 Relative brightness mode

Because absolute lux may be unreliable without calibration, the app shall provide a relative brightness mode.

Relative brightness should allow users to compare:

- One location with another
- One lighting setup with another
- Different areas of an artwork or scene
- Lighting changes over time

Relative brightness may be represented as:

- Normalized percentage
- Relative EV difference
- Relative brightness ratio

For two linear brightness values:

```text
relativeDifferenceEV = log2(valueA / valueB)
```

The application must handle zero and invalid values safely.

## 9.5 Exposure and lux relationship

The app must not incorrectly convert EV directly into lux without documenting assumptions.

The relationship between reflected scene luminance, incident illuminance, camera calibration, exposure settings, and photographic exposure value depends on the measurement model and calibration.

Any conversion from camera signal to lux must be implemented through a documented calibration model.

---

# 10. Sun, Sunrise, Sunset, and Golden Hour Requirements

## 10.1 Purpose

The Sun module shall calculate solar events locally using:

- Device date and time
- User-selected date
- GPS coordinates or manually entered coordinates
- Time-zone information available on the device

No network connection may be used.

## 10.2 Location sources

The app shall support:

1. Current device location, when the user grants location permission
2. Manually entered latitude and longitude
3. Saved locations

The user must be able to use the module without granting location permission by entering coordinates manually.

The app must explain why location permission is requested.

The application must not transmit location data.

## 10.3 Solar events

The module shall calculate or display:

- Sunrise
- Sunset
- Solar noon
- Civil dawn
- Civil dusk
- Nautical dawn
- Nautical dusk
- Astronomical dawn
- Astronomical dusk
- Morning golden hour
- Evening golden hour

The app must document the definition used for golden hour.

The default definition may use the Sun altitude interval:

```text
Sun altitude: -4 degrees to +6 degrees
```

The exact definition must be configurable.

## 10.4 Solar position

For a selected date and time, the app shall calculate:

- Solar altitude/elevation
- Solar azimuth
- Whether the Sun is above the horizon
- Approximate sun direction

Angles must be documented:

- Azimuth measured clockwise from true north
- North = 0 degrees
- East = 90 degrees
- South = 180 degrees
- West = 270 degrees

## 10.5 Compass integration

The app may use the device's magnetic sensors to display the Sun's direction relative to the device.

The UI must distinguish between:

- True azimuth
- Magnetic heading
- Device-relative direction

The application should provide a compass calibration instruction.

Magnetic declination may not be available offline unless a local geomagnetic model is bundled. If magnetic declination is not applied, the app must clearly state that the compass direction is approximate.

## 10.6 Polar and extreme-latitude behavior

The solar calculation engine must handle cases where an event does not occur on a given date, such as:

- No sunrise
- No sunset
- Continuous daylight
- Continuous night

The result model must support nullable event times and explicit statuses such as:

```text
SolarEventStatus
- OCCURS
- DOES_NOT_OCCUR
- ALWAYS_ABOVE_HORIZON
- ALWAYS_BELOW_HORIZON
- CALCULATION_ERROR
```

## 10.7 Solar calculation accuracy

The solar-position implementation must use a documented astronomical algorithm or a well-tested offline implementation.

The implementation must include tests against known reference cases.

All time conversions must be handled carefully, including:

- UTC
- Local time
- Time-zone offsets
- Daylight-saving changes where applicable
- Date boundaries

The application must not depend on online time-zone lookup.

---

# 11. Photography Utility Calculators

The initial calculator section should focus on exposure-related utilities only.

## 11.1 Included calculators

The initial release should include:

- EV calculator
- Aperture/shutter/ISO equivalence calculator
- Exposure compensation calculator
- Stop-difference calculator
- Shutter-speed conversion utility
- ISO conversion utility

## 11.2 Excluded calculators

The following are explicitly excluded from the initial release:

- Depth of Field Calculator
- Hyperfocal Distance Calculator
- Field of View Calculator

These may be added in a later release.

---

# 12. Measurement History and Projects

## 12.1 Purpose

Users should be able to save measurements and organize them into projects.

This is particularly useful for repeatable photography setups and artwork documentation, even though the Color Meter is out of scope.

## 12.2 Project model

Suggested fields:

```text
Project
- id
- name
- description
- createdAt
- updatedAt
- notes
```

## 12.3 Measurement record model

Suggested fields:

```text
MeasurementRecord
- id
- projectId
- measurementType
- timestamp
- cameraId
- ev100
- luxEstimate
- aperture
- shutterSeconds
- iso
- latitude
- longitude
- calibrationProfileId
- qualityStatus
- notes
```

Only relevant fields should be populated for each measurement type.

Location fields should be optional and must not be saved unless the user explicitly enables location recording.

## 12.4 History features

The user shall be able to:

- Save a measurement
- Edit measurement notes
- Associate a measurement with a project
- View measurement details
- Delete a measurement
- Delete a project
- Export measurements
- Compare measurements
- View a time-series graph for a measurement session

## 12.5 Export

The application should support local export to:

- JSON
- CSV

Exports must be generated locally.

The application must use Android's Storage Access Framework or another appropriate local file-sharing mechanism.

No network permission is required for sharing a file through the Android system picker.

---

# 13. Calibration Requirements

## 13.1 Calibration architecture

Calibration must be represented as a separate domain concept.

Suggested model:

```text
CalibrationProfile
- id
- name
- cameraId
- calibrationType
- coefficients
- referenceUnit
- createdAt
- updatedAt
- notes
- isActive
```

Calibration types may include:

- Exposure estimate
- Lux estimate
- Relative brightness
- Future measurement types

## 13.2 Calibration workflow

The initial calibration workflow should allow the user to:

1. Select a camera
2. Select a calibration type
3. Enter a reference measurement
4. Capture or record a corresponding camera measurement
5. Save the calibration profile
6. Activate or deactivate a profile
7. Delete a profile

The app must validate reference values and reject invalid inputs.

## 13.3 Calibration warnings

The UI must clearly communicate:

- No calibration available
- Calibration is device-specific
- Calibration may not transfer between cameras
- Calibration may vary with camera mode
- Calibration may vary with lighting conditions
- The measurement is only an estimate

---

# 14. Device Capability Report

The application shall provide a device capability screen.

It should report:

- Available cameras
- Camera identifiers
- Sensor orientation
- RAW support
- Manual sensor support
- Manual exposure support
- Manual focus support
- Manual white-balance support
- Supported ISO range
- Supported exposure-time range
- Supported RAW output sizes
- Supported preview sizes
- Whether the selected camera is suitable for measurement processing

The capability screen must not imply that a capability is available if it has not been verified through Camera2 metadata.

---

# 15. Permission Requirements

## 15.1 Required permissions

The app may request:

```text
android.permission.CAMERA
```

The app may request:

```text
android.permission.ACCESS_COARSE_LOCATION
android.permission.ACCESS_FINE_LOCATION
```

only when the user chooses to use current device location for solar calculations.

The application must request permissions at runtime only when the related feature is used.

## 15.2 Explicitly prohibited permission

The application must not declare:

```text
android.permission.INTERNET
```

The application must not use network sockets, HTTP clients, cloud SDKs, or remote APIs.

## 15.3 Optional permissions

Avoid adding optional permissions unless a feature explicitly requires them.

The application should not request storage permissions when the Android Storage Access Framework can be used.

---

# 16. UI and UX Requirements

## 16.1 General design

The interface should be:

- Clear
- Fast
- Readable in outdoor lighting
- Usable with one hand where practical
- Suitable for landscape and portrait orientations
- Explicit about measurement uncertainty

Large measurement values should be visually prominent.

## 16.2 Live measurement screen

The live measurement screen should include:

- Large primary measurement
- Secondary measurements
- Camera preview
- Measurement region overlay
- Lock controls
- Exposure recommendation controls
- Calibration indicator
- Measurement quality indicator
- Start/stop measurement action
- Save measurement action

## 16.3 Error states

The app must provide clear messages for:

- Camera permission denied
- Location permission denied
- No compatible camera
- RAW unsupported
- Camera unavailable
- Camera in use by another app
- Invalid calibration
- Insufficient light
- Excessive saturation
- Processing overload
- Unsupported camera control
- Solar event does not occur
- Invalid coordinates

Error messages should explain what the user can do next.

## 16.4 Accessibility

The app should support:

- Content descriptions
- Scalable text
- Adequate contrast
- Touch targets of appropriate size
- Avoiding color as the only way to communicate status
- Screen-reader-compatible labels

---

# 17. Performance Requirements

## 17.1 Main-thread policy

No heavy image processing, RAW decoding, mathematical batch processing, database operation, or file generation may run on the main thread.

## 17.2 Frame processing

The system should:

- Process only the latest useful frame
- Avoid unbounded queues
- Reuse buffers where practical
- Release ImageReader images promptly
- Limit processing resolution when possible
- Throttle UI updates
- Monitor processing time

## 17.3 Responsiveness targets

Initial targets:

- UI interactions should respond promptly under normal load
- Live measurement display should feel continuous
- Measurement latency should generally remain below 500 ms on a supported midrange device
- The app should avoid persistent high CPU usage when measurement is paused
- Camera resources must be released when the screen is left or the app is backgrounded

These targets must be validated through profiling rather than assumed.

## 17.4 Battery and thermal behavior

The application should:

- Stop live processing when not visible
- Reduce processing frequency when the device becomes hot
- Avoid unnecessary full-resolution RAW processing
- Provide a low-power measurement mode
- Release camera resources when no longer needed

---

# 18. Data and State Architecture

## 18.1 Recommended layers

```text
Presentation Layer
├── Compose screens
├── ViewModels
└── UI state models

Domain Layer
├── Exposure calculation use cases
├── Measurement use cases
├── Solar calculation use cases
├── Recommendation engine
├── Calibration logic
└── Validation rules

Data Layer
├── Camera data source
├── Sensor data source
├── Location data source
├── Room repositories
├── Calibration repository
└── File export repository

Processing Layer
├── Frame acquisition
├── RAW decoder/normalizer
├── Region sampler
├── Brightness estimator
├── Lux estimator
├── Stability filter
└── Performance metrics
```

## 18.2 StateFlow

Live measurement results should be exposed to the presentation layer through a lifecycle-aware observable mechanism, preferably `StateFlow`.

The ViewModel should not contain raw image-processing code.

Example conceptual flow:

```text
FrameProcessor -> MeasurementRepository -> ViewModel -> UI
```

The UI must not directly consume `Image`, `ByteBuffer`, or camera-specific objects.

## 18.3 Immutable state

UI state models should be immutable.

The state should include:

- Current measurement
- Measurement status
- Camera status
- Permission status
- Calibration status
- User settings
- Error information
- Performance diagnostics, where enabled

---

# 19. Testing Requirements

## 19.1 Unit tests

The project must include unit tests for:

### Exposure mathematics

- EV100 calculation
- EV calculation at different ISO values
- Shutter calculation
- Aperture calculation
- ISO calculation
- Exposure compensation
- Stop differences
- Equivalent exposure combinations
- Rounding and display conversion
- Invalid and zero values

### Solar mathematics

- Solar altitude
- Solar azimuth
- Sunrise and sunset
- Civil, nautical, and astronomical twilight
- Golden hour
- High-latitude edge cases
- Date and time-zone handling

### Measurement processing

- Black-level normalization
- White-level normalization
- Saturation detection
- Region-of-interest sampling
- Relative brightness
- Stability filtering
- Calibration transformations

## 19.2 Instrumented tests

Instrumented tests should cover:

- Camera permission flow
- Camera opening and closing
- Lifecycle handling
- ImageReader buffer release
- Rotation handling
- Location permission flow
- Local export through the Storage Access Framework

## 19.3 UI tests

UI tests should cover:

- Navigation
- Exposure parameter selection
- Locked-parameter workflows
- Recommendation display
- Error states
- Saving measurements
- Project creation
- Settings changes

## 19.4 Mathematical verification

Reference values should be generated independently where possible.

The implementation must not test a formula only against values generated by the same implementation.

---

# 20. Logging and Diagnostics

The application should provide local diagnostic logging that can be disabled.

Logs must not contain:

- Personal identifiers
- Unnecessary location data
- Camera images
- Raw frame contents
- Sensitive user notes

Diagnostic information may include:

- Camera capability summary
- Frame processing duration
- Dropped frame count
- Measurement validity
- Calibration profile identifier
- Error codes

The application must not upload logs.

A local diagnostic report may be exported by the user.

---

# 21. Security and Privacy Requirements

- No Internet permission
- No network access
- No analytics that require network access
- No user account
- No cloud synchronization
- No remote image processing
- No automatic location transmission
- Camera frames must remain local
- Saved data must remain on the device unless explicitly exported by the user

If sensitive data is stored locally, the implementation should use appropriate Android storage protections.

---

# 22. Out-of-Scope Future Features

The following features may be considered after the initial release:

- Light Flicker Meter
- Color Meter
- RAW-based color analysis
- ColorChecker calibration
- Depth of Field Calculator
- Hyperfocal Distance Calculator
- Field of View Calculator
- Artwork reproduction assistant
- Lighting uniformity heatmap
- External Bluetooth light meters
- External colorimeters
- External spectrometers
- External photodiode flicker sensors
- AR sun-position overlay
- Cloud backup, only if the zero-network requirement is explicitly changed

---

# 23. Acceptance Criteria

The initial release is acceptable when all of the following are true:

1. The application builds successfully with minimum SDK 26.
2. `android.permission.INTERNET` is absent from the manifest and merged manifest.
3. The app can run without network connectivity.
4. The app does not attempt network access.
5. The Exposure Meter calculates EV and equivalent exposure combinations using tested formulas.
6. The user can lock aperture, shutter speed, or ISO and receive valid recommendations.
7. The app detects camera capabilities rather than assuming RAW or manual controls exist.
8. Camera processing is separated from UI code.
9. Heavy frame processing does not run on the main thread.
10. The frame-processing pipeline avoids unbounded queues.
11. Image buffers are released correctly.
12. The Light/Lux module clearly labels estimates and calibration status.
13. Relative brightness comparison works without absolute lux calibration.
14. Solar calculations work offline.
15. Sunrise, sunset, golden hour, and solar direction are available for manually entered coordinates.
16. Solar edge cases are handled explicitly.
17. Measurements can be saved locally.
18. Measurements can be exported locally as JSON or CSV.
19. Unit tests cover exposure formulas and solar calculations.
20. Permission requests occur only when required.
21. Camera and location data are not transmitted.
22. The app provides clear error messages for unsupported hardware and invalid measurements.
23. The app releases camera resources when the relevant screen is closed or backgrounded.
24. The app remains usable on supported midrange hardware under normal conditions.

---

# 24. Implementation Guidance for Cline

Cline should implement the project incrementally.

Recommended implementation order:

1. Create the Android project and enforce the no-Internet requirement.
2. Implement the domain models and validation utilities.
3. Implement and test exposure mathematics.
4. Implement the exposure recommendation engine.
5. Implement the basic navigation and UI shell.
6. Implement camera capability discovery.
7. Implement camera preview and lifecycle handling.
8. Implement the frame-processing abstraction.
9. Implement RAW capability detection and a fallback camera-data path.
10. Implement region sampling and measurement-quality reporting.
11. Implement estimated brightness and lux processing.
12. Implement calibration profile storage.
13. Implement the Sun calculation engine and tests.
14. Implement location permission and manual-coordinate workflows.
15. Implement projects and measurement history.
16. Implement local JSON/CSV export.
17. Add performance instrumentation.
18. Add integration and UI tests.
19. Verify the merged manifest and confirm no network access.
20. Perform device testing on multiple Android devices with different camera capabilities.

Cline must not silently add excluded features.

When a hardware limitation prevents a requested measurement, the implementation should provide a clearly labelled fallback or unsupported-state message rather than fabricating precision.

---

# 25. Important Technical Notes

1. Android API 26 is the minimum operating-system version, but camera capabilities are device-specific.
2. RAW_SENSOR support is optional and must be discovered through Camera2 metadata.
3. Camera-derived lux is an estimate unless a suitable calibration process is applied.
4. Exposure Value calculations are mathematically deterministic, but the accuracy of a live camera-derived EV depends on camera calibration and measurement methodology.
5. A camera's reported ISO, exposure time, and aperture should be taken from actual capture metadata where available.
6. Continuous RAW processing can be computationally expensive. The implementation must support lower-resolution or lower-frequency processing.
7. A live UI should receive stable, throttled measurement results rather than every processed frame.
8. Solar calculations must be local and must explicitly handle locations and dates where sunrise or sunset does not occur.
9. The application should preserve a clear distinction between measured, estimated, calculated, and calibrated values.
