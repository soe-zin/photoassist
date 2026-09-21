# Photography Assistant for Android — Implementation Tasks

## Progress Tracking

**Status**: 
**Total Tasks**: 20

## Phase 1: Foundation (Tasks 1-5)

- [x] **Task 1: Create Android project with no-Internet enforcement**
  - **Description**: Initialize Android project (minSdk 26, targetSdk 34), configure build.gradle, explicitly omit android.permission.INTERNET from manifest
  - **Acceptance**: Manifest validates no INTERNET permission; builds successfully
  - **Dependencies**: None
  - **Module Reference**: design.md §2.1, §5.1, §5.5

- [x] **Task 2: Implement domain models and validation utilities**
  - **Description**: Create domain models for Exposure, Lux, Solar calculations with validation logic
  - **Acceptance**: All domain models compile, validation utilities functional
  - **Dependencies**: Task 1
  - **Module Reference**: design.md §3.1, §5.1

- [x] **Task 3: Implement and test exposure mathematics**
   - **Description**: Implement exposure value calculations including EV, ISO, aperture, shutter speed
   - **Acceptance**: Unit tests pass for normal values, boundary values, and invalid inputs
   - **Dependencies**: Task 2
   - **Module Reference**: design.md §3.1.1, §8.1

- [x] **Task 4: Implement exposure recommendation engine**
  - **Description**: Create algorithm to generate exposure recommendations based on light meter readings
  - **Acceptance**: Exposure recommendation engine produces deterministic results
  - **Dependencies**: Task 3
  - **Module Reference**: design.md §3.1.1

- [x] **Task 5: Implement basic navigation and UI shell**
  - **Description**: Set up navigation structure and basic screen layouts
  - **Acceptance**: App launches with navigation structure; screens render correctly
  - **Dependencies**: Task 1
  - **Module Reference**: design.md §3.5.1, §5.1

## Phase 2: Camera Integration (Tasks 6-10)

- [x] **Task 6: Implement camera capability discovery**
  - **Description**: Use Camera2 API to discover available camera features and capabilities
  - **Acceptance**: App can detect cameras with/without RAW support, different resolutions
  - **Dependencies**: Task 5
  - **Module Reference**: design.md §3.2.1, §25.2

- [x] **Task 7: Implement camera preview and lifecycle handling**
  - **Description**: Set up CameraX or Camera2 preview with proper lifecycle management
  - **Acceptance**: Camera preview displays correctly; resources released on app pause
  - **Dependencies**: Task 6
  - **Module Reference**: design.md §3.2.1, §23

- [x] **Task 8: Implement frame-processing abstraction**
  - **Description**: Create abstraction layer for processing camera frames
  - **Acceptance**: Frame processor interfaces established; processing pipeline functional
  - **Dependencies**: Task 7
  - **Module Reference**: design.md §3.2.1

- [~] **Task 9: Implement RAW capability detection and fallback**
  - **Description**: Detect RAW sensor support; implement JPEG fallback when RAW unavailable
  - **Acceptance**: RAW capture works on supported devices; graceful fallback on unsupported
  - **Dependencies**: Task 6
  - **Module Reference**: design.md §3.2.1, §25.2

- [ ] **Task 10: Implement region sampling and quality reporting**
  - **Description**: Implement spot/center-weighted/average metering regions; exposure quality metrics
  - **Acceptance**: Multiple metering modes functional; quality scores reported
  - **Dependencies**: Task 8
  - **Module Reference**: design.md §3.2.1

## Phase 3: Light & Solar Calculations (Tasks 11-14)

- [ ] **Task 11: Implement lux estimation and calibration**
  - **Description**: Convert camera sensor data to lux estimates; apply calibration profiles
  - **Acceptance**: Lux estimation functional; calibration profiles can be applied
  - **Dependencies**: Task 8
  - **Module Reference**: design.md §3.1.2, §25

- [ ] **Task 12: Implement exposure metering with live preview**
  - **Description**: Real-time exposure analysis from camera frames; live preview integration
  - **Acceptance**: Live exposure meter updates; preview shows metering regions
  - **Dependencies**: Task 8, Task 11
  - **Module Reference**: design.md §3.2.1, §25

- [ ] **Task 13: Implement the Sun calculation engine and tests**
  - **Description**: Implement astronomical calculations for sunrise/sunset/golden hour
  - **Acceptance**: Solar calculations pass unit tests; edge cases handled
  - **Dependencies**: Task 2
  - **Module Reference**: design.md §3.1.3, §8.1

- [ ] **Task 14: Implement location permission and manual-coordinate workflows**
  - **Description**: Handle GPS permissions and manual coordinate input for solar calculations
  - **Acceptance**: Location permissions work; manual coordinate input functional
  - **Dependencies**: Task 13
  - **Module Reference**: design.md §3.5.2, §5.1

## Phase 4: Data Management (Tasks 15-16)

- [ ] **Task 15: Implement projects and measurement history**
  - **Description**: Create project system to organize measurements; implement history tracking
  - **Acceptance**: Projects can be created/managed; measurements can be organized
  - **Dependencies**: Task 2, Task 12
  - **Module Reference**: design.md §3.3.2, §4.1.5-4.1.6

- [ ] **Task 16: Implement local JSON/CSV export**
  - **Description**: Create export functionality for measurements in JSON and CSV formats
  - **Acceptance**: Export functionality works; files can be saved and shared
  - **Dependencies**: Task 15, Task 12
  - **Module Reference**: design.md §3.5.3

## Phase 5: Quality Assurance (Tasks 17-20)

- [ ] **Task 17: Add performance instrumentation**
  - **Description**: Implement performance monitoring and profiling
  - **Acceptance**: Performance instrumentation provides useful metrics
  - **Dependencies**: All previous tasks
  - **Module Reference**: design.md §5.6

- [ ] **Task 18: Add integration and UI tests**
  - **Description**: Create comprehensive test suite covering integration and UI
  - **Acceptance**: Test suite passes; integration tests cover critical paths
  - **Dependencies**: Task 17
  - **Module Reference**: design.md §8.2, §8.3

- [ ] **Task 19: Verify the merged manifest and confirm no network access**
  - **Description**: Final verification of AndroidManifest.xml; ensure no network permissions
  - **Acceptance**: Manifest contains only required permissions; no INTERNET permission
  - **Dependencies**: Task 1, Task 18
  - **Module Reference**: design.md §5.5

- [ ] **Task 20: Perform device testing on multiple Android devices with different camera capabilities**
  - **Description**: Test app on various devices to ensure compatibility
  - **Acceptance**: App functions on target devices; camera variations handled
  - **Dependencies**: Task 19
  - **Module Reference**: design.md §5.1

## Implementation Guidance

### Compliance Rules

1. **No Silent Feature Addition**: Must not implement excluded features (requirements §1)
2. **Hardware Limitations**: When hardware prevents requested measurement, provide clear fallback (requirements §24)
3. **Mathematical Determinism**: All calculations must be testable, deterministic Kotlin code (requirements §2)
4. **Privacy Preservation**: No data transmission; all processing local (requirements §2)

### Technical Requirements

1. **Mathematical Core**: Must have unit tests covering normal/boundary/invalid inputs (requirements §2)
2. **Main Thread**: Heavy processing must not run on Android main thread (requirements §2)
3. **Resource Management**: Camera resources released when screen closed/backgrounded (requirements §23)
4. **API 26 Minimum**: Camera capabilities are device-specific; handle limitations (requirements §25)
5. **Calibrated vs Estimated**: Lux measurements must distinguish between calibrated and estimated (requirements §25)

