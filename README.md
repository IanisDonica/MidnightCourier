# Maze Runner Game - Complete System Documentation

A feature-rich 2D maze runner game built with libGDX, featuring advanced audio management, achievement system, graphics configuration, and developer tools.

## Overview

Maze Runner is an action-packed game where players navigate mazes, collect points, unlock achievements, and face various obstacles. The system includes sophisticated management systems for audio, graphics, gameplay progression, and development utilities.

## System Architecture

### 1. Audio Management System
**File**: `AudioManager.java`

Comprehensive audio management with asynchronous playback, caching, and volume control.

#### Key Features:
- **Playlist Management**: Play sequential music tracks with automatic progression
- **Shuffle Mode**: Randomize playlist order using Fisher-Yates algorithm
- **Auto-Looping**: Automatically loops back to first track after last one finishes
- **Sound Effects**: Basic and advanced playback with pitch and pan controls
- **Looping Sounds**: Ambient effects and background loops with independent control
- **Volume Control**:
  - Master volume for all audio
  - Independent sound effects volume
  - Independent music volume
  - Real-time volume adjustment for active sounds
  - Persistent volume settings via ConfigManager
- **Asynchronous Operations**: Thread pool executor for non-blocking audio operations
- **Audio Caching**: Efficient loading and caching of sounds and music
- **Completion Listeners**: Automatic track progression detection

**Methods**:
```java
playPlaylist(float volume, boolean shuffle, String... musicPaths)
playMusic(String musicPath, float volume, boolean loop)
playSound(String soundPath, float volume)
playSoundLooping(String soundPath, float volume)
stopMusic() / stopPlaylist()
pauseMusic(boolean paused)
setMasterVolume(float volume)
setMusicVolume(float volume)
setSoundEffectsVolume(float volume)
```

---

### 2. Achievement System
**Files**: `Achievement.java`, `AchievementManager.java`

Persistent achievement tracking with progress management and unlock notifications.

#### Key Features:
- **Progress Tracking**: Track progress toward achievement goals
- **Auto-Unlock Detection**: Automatic unlock when targets are met
- **Popup Notifications**: Show achievement unlock popups
- **Persistent Storage**: JSON-based achievement data persistence
- **Static Manager**: Singleton pattern for global achievement access
- **Default Achievements**:
  - Certified Delivery Man (100 deliveries)
  - First Delivery (1 delivery)
  - First time for Everything (get arrested once)
  - First Upgrade (buy upgrade)
  - Deep Pockets (mastery upgrade)
  - Three Strike Rule (arrested 3 times)
  - Vacation Money (complete campaign level 5)
  - German Engineering (hit by BMW)

**Methods**:
```java
init()
getAchievements() → List<Achievement>
getAchievement(String id) → Achievement
incrementProgress(String id, int amount)
resetAll()
setPopupScreen(AchievementPopupScreen screen)
```

---

### 3. Graphics Management System
**File**: `GraphicsManager.java`

Comprehensive graphics settings with persistence and runtime application.

#### Key Features:
- **Frame Rate Control**: Configurable FPS (15-360) with VSync support
- **Display Modes**:
  - Windowed (resizable)
  - Borderless Windowed (no decorations)
  - Fullscreen
- **Anti-Aliasing**: Multiple MSAA modes (Disabled, 2x, 4x, 8x, 16x)
- **Resolution Management**: Configurable width/height with aspect ratio tracking
- **Settings Persistence**: JSON-based configuration storage
- **Dynamic Application**: Apply settings at runtime

**Methods**:
```java
load() / save()
applyFrameRateSettings()
applySettings()
setFrameLimit(int fps)
setResolution(int width, int height)
setVsyncEnabled(boolean enabled)
setAntiAliasingMode(AAMode mode)
setFullscreen() / setWindowed() / setBorderless()
getTargetFrameRate() / getTargetAspectRatio()
getGraphicsDeviceInfo()
logCurrentSettings()
```

---

### 4. Point & Score Management
**File**: `PointManager.java`

Dynamic score calculation with time-based decay and server submission.

#### Key Features:
- **Base Points**: Initial delivery bonus points
- **Time-Based Points**: Decreasing points over time in campaign mode
- **Survival Mode**: Time-accumulation points in survival mode
- **Dynamic Decay**: Logarithmic point decrease for balanced gameplay
- **Safety Period**: 5-second grace period before decay starts
- **Local Persistence**: Save scores to `assets/data/highscore.json`
- **Server Integration**: Fire-and-forget HTTP submission to remote endpoint
- **Timestamps**: Record completion time and player HP at end

**Methods**:
```java
getPoints() → int (total score)
getTimePoints() → int
setPoints(int points)
setTimePoints(int timePoints)
add(int amount)
decreasePoints()
increasePoints()
act(float delta)
saveScore(int playerHp)
getElapsedTime() → float
```

---

### 5. Configuration Management
**File**: `ConfigManager.java`

Centralized configuration for key bindings and audio settings.

#### Key Features:
- **Key Bindings**:
  - Customizable controls (arrow keys, WASD alternatives)
  - Default bindings for movement, sprint, shop, pause, zoom, effects
  - Runtime key binding changes with persistence
- **Audio Settings**:
  - Master volume
  - Sound effects volume
  - Music volume
  - Auto-save on change
- **JSON Persistence**: Separate config files for bindings and audio

**Methods**:
```java
loadKeyBindings() / saveKeyBindings()
loadAudioSettings() / saveAudioSettings()
getKeyBinding(String action) → int
setKeyBinding(String action, int keyCode)
getKeyBindingName(String action) → String
getVolume(String type) → float
setVolume(String type, float volume)
```

---

### 6. Developer Console
**File**: `DevConsole.java`

In-game debugging and entity spawning utility.

#### Key Features:
- **UI Console**: Scrollable input/output terminal
- **Debug Commands**:
  - `tp <x> <y>` - Teleport player
  - `speed <multiplier>` - Set movement speed
  - `sethp <value>` - Set player HP
  - `setmaxhp <value>` - Set max HP
  - `setcredits <value>` - Set player credits
  - `godmode [on|off]` - Toggle invincibility
  - `giveenergydrink` - Apply energy boost
  - `givekey` - Add key item
  - `spawn <type> <x> <y>` - Spawn enemy/trap/BMW
  - `spawnbmws <count>` - Spawn BMW enemies
  - `spawnenemies <count>` - Spawn random enemies
  - `whereami` - Show current position
  - `openshop` - Navigate to shop
  - `help` - Show all commands
- **Toggle**: Backtick (`) to open/close
- **Output History**: Maintains 100-line history

---

### 7. Player Movement System
**File**: `DriftyMovementController.java`

Physics-based movement with inertia, friction, and smooth rotation.

#### Key Features:
- **Velocity-Based Movement**: Acceleration/deceleration mechanics
- **Inertia**: Smooth momentum with configurable friction
- **Smooth Rotation**: Gradual orientation changes toward input direction
- **Sprint Mode**: 1.5x speed multiplier
- **Drift Detection**: Identifies when player slides (velocity ≠ orientation)
- **Speed Capping**: Configurable maximum speed
- **Deceleration Tracking**: Detects when player is slowing down

**Methods**:
```java
update(float deltaTime, boolean moveUp/Down/Left/Right, boolean sprinting)
getVelocity() → Vector2
getOrientation() → float (degrees)
getSpeed() → float
getMaxSpeed() → float
setAcceleration(float acceleration)
setMaxSpeed(float maxSpeed)
setFriction(float friction)
setRotationSpeed(float rotationSpeed)
reset()
```

---

### 8. Particle Effects System
**File**: `DriftParticleSystem.java`

Visual and audio feedback for drifting and movement actions.

#### Key Features:
- **Drift Particles**: Dust clouds behind player during slides (> 25° angle difference)
- **Sound Integration**:
  - `tires_loop.wav` - Continuous tire skid (speed-based volume)
  - `tires.wav` - Screech sound on drift end
  - `pedal.wav` - Cycling/pedaling sound
  - `decelerating.wav` - Braking/deceleration sound
- **Particle Physics**:
  - 0.6s lifetime per particle
  - Spread pattern behind player
  - Alpha fade based on age
- **Speed-Based Volume**: Audio volume scales with velocity
- **Particle Pooling**: Efficient particle updates and removal

**Methods**:
```java
act(float delta)
draw(Batch batch, float parentAlpha)
dispose()
isDrifting() → boolean
```

---

## File Structure

```
system/
├── AudioManager.java                 (4.2 KB) - Audio management & playlists
├── Achievement.java                  (3.7 KB) - Achievement data model
├── AchievementManager.java           (8.2 KB) - Achievement persistence
├── CollisionHandler.java             (3.1 KB) - Collision detection
├── CollectibleData.java              (0.7 KB) - Collectible item data
├── ConfigManager.java                (7.7 KB) - Configuration management
├── DevConsole.java                   (15.5 KB) - Developer debugging console
├── DriftyMovementController.java      (8.2 KB) - Physics-based movement
├── DriftParticleSystem.java           (8.8 KB) - Visual particle effects
├── GameContext.java                  (0.7 KB) - Game context data
├── GameState.java                    (6.0 KB) - Game state management
├── GraphicsManager.java              (11.7 KB) - Graphics configuration
├── PointManager.java                 (7.2 KB) - Score management
├── UiUtils.java                      (2.5 KB) - UI utilities
├── EnemyData.java                    (0.5 KB) - Enemy data model
└── README.md                         (This file)
```

---

## Integration Points

### AudioManager Integration
- Used by `DriftParticleSystem` for sound effects
- Manages all game audio including music, SFX, and ambient sounds
- Integrates with `ConfigManager` for volume settings

### Achievement System Integration
- Triggered by gameplay events (deliveries, arrests, upgrades)
- Shows popup notifications on unlock
- Persists achievement progress to disk

### Graphics System
- Applied on startup and when settings change
- Integrates with game configuration
- Supports multiple display modes and resolutions

### Point Manager
- Tracks player score throughout gameplay
- Saves scores locally and to server
- Includes player HP and timestamps

### DevConsole
- Toggleable with backtick key
- Useful for testing and debugging
- Allows runtime entity spawning

---

## Configuration Files

### Location: `config/`
- `keybindings.json` - Player input key mappings
- `audio.json` - Volume levels (master, effects, music)
- `graphics-settings.json` - Display and performance settings

### Location: `assets/data/`
- `achievements.json` - Achievement progress persistence
- `highscore.json` - Score records with timestamps

---

## Dependencies

### Core Libraries
- **libGDX 1.8+** - Game framework
- **Java 8+** - Language runtime
- **JSON Processing** - libGDX's Json utility

### External Services
- **Remote Score Server** - Railway-hosted endpoint for score submission

---

## Performance Considerations

- **Audio**: Asynchronous loading with thread pool executor
- **Graphics**: Configurable frame rate and anti-aliasing
- **Particles**: Efficient particle lifecycle management
- **Caching**: Sound and music asset caching to prevent redundant loads
- **Threading**: Non-blocking operations for audio and file I/O

---

## Future Enhancements

- Music crossfade transitions between tracks
- Configurable achievement categories and custom achievements
- Advanced graphics settings (resolution scaling, post-processing)
- Leaderboard UI integration
- Performance profiling tools in DevConsole
- Accessibility options (colorblind modes, input remapping)

---
**Last Updated**: February 2026
