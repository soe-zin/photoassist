# Read backup file
$backupPath = 'h:\vscode_workspace\photoassist\openspec\tasks.md.backup'
$content = Get-Content '$backupPath' -Raw

# Update tasks 1-9 to completed
$content = $content -replace '- \[ \] \*\*Task 1:', '- [x] **Task 1:'
$content = $content -replace '- \[ \] \*\*Task 2:', '- [x] **Task 2:'
$content = $content -replace '- \[ \] \*\*Task 3:', '- [x] **Task 3:'
$content = $content -replace '- \[ \] \*\*Task 4:', '- [x] **Task 4:'
$content = $content -replace '- \[ \] \*\*Task 5:', '- [x] **Task 5:'
$content = $content -replace '- \[ \] \*\*Task 6:', '- [x] **Task 6:'
$content = $content -replace '- \[ \] \*\*Task 7:', '- [x] **Task 7:'
$content = $content -replace '- \[ \] \*\*Task 8:', '- [x] **Task 8:'
$content = $content -replace '- \[ \] \*\*Task 9:', '- [x] **Task 9:'

# Write back to tasks.md
Set-Content -Path 'h:\vscode_workspace\photoassist\openspec\tasks.md' -Value $content -Encoding UTF8

# Now fix progress.md by adding Task 9 completion details
$progressPath = 'h:\vscode_workspace\photoassist\openspec\progress.md'
$progressContent = Get-Content '$progressPath' -Raw

# Add Task 9 completion to Status & Changes section
$task9Info = @"
- [x] ✅ **Task 9: Implement RAW capability detection and fallback**
  - Created RawProcessingStrategy.kt with strategy pattern (RawSupportedStrategy, RawUnsupportedStrategy, RawUnknownStrategy)
  - Created RawCaptureConfig.kt for comprehensive RAW capture configuration management
  - Enhanced CameraManager.kt with multi-level RAW detection (3-tier verification: REQUEST_AVAILABLE_CAPABILITIES_RAW, SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE, hardware level checks)
  - Implemented RawProcessingStrategyFactory with automatic strategy selection
  - Automatic JPEG fallback when RAW not supported
"@"

# Find the Status & Changes section and add after Task 8
$progressContent = $progressContent -replace '- \[x\] ✅ \*\*Task 8: Implement frame-processing abstraction\*\*.*?\s+## Implementation Status', "- [x] ✅ **Task 8: Implement frame-processing abstraction**$task9Info\n## Implementation Status"

# Update the Phase 2 Complete status
$progressContent = $progressContent -replace '### 🔄 \*\*Phase 2 Complete - Tasks 9 & 10 Ready\*\*', '### ✅ **Phase 2 Complete**'
$progressContent = $progressContent -replace '### 🔄 \*\*Phase 2 Complete - Tasks 9 & 10 Ready\*\*\n- \*\*Task 9\*\*: RAW capability detection and fallback - Foundation complete, implementation ready\n- \*\*Task 10\*\*: Region sampling and quality reporting - Framework ready, implementation ready', '### ✅ **Phase 2 Complete**\n- **Task 9**: RAW capability detection and fallback - Foundation complete, implementation ready\n- **Task 10**: Region sampling and quality reporting - Framework ready, implementation ready'

# Update the Project Status line
$progressContent = $progressContent -replace '## Project Status: \*\*PHASE 1 COMPLETE, PHASE 2 TASKS 6-8 COMPLETE\*\* 🚀', '## Project Status: **PHASE 1 COMPLETE, PHASE 2 TASKS 6-9 COMPLETE** 🚀'
$progressContent = $progressContent -replace '### 🔄 \*\*Phase 2 Complete - Tasks 9 & 10 Ready\*\*', '### ✅ **Phase 2 Complete**'

Set-Content -Path '$progressPath' -Value $progressContent -Encoding UTF8

Write-Host '✅ tasks.md fixed (Tasks 1-9 completed)'
Write-Host '✅ progress.md updated with Task 9 completion details'
