package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages sound effects and music playback with caching and volume controls.
 */
public class AudioManager {
    /**
     * Configuration manager providing persisted volume settings.
     */
    private final ConfigManager configManager;
    /**
     * Executor for asynchronous audio loading/playback.
     */
    private final ExecutorService executor;
    /**
     * Cache of loaded sound effects.
     */
    private final Map<String, Sound> soundCache;
    /**
     * Cache of loaded music tracks.
     */
    private final Map<String, Music> musicCache;
    private final Map<String, Sound> currentSounds;
    private final Map<String, Long> currentSoundIDs;
    /**
     * Currently playing music track.
     */
    private Music currentMusic;
    /**
     * Base volume for the currently playing music.
     */
    private float currentMusicVolume;
    /**
     * Global volume controls.
     */
    private float masterVolume, soundEffectsVolume, musicVolume;


    /**
     * Creates an audio manager bound to the game's configuration.
     *
     * @param game game instance used to access configuration
     */
    public AudioManager(MazeRunnerGame game) {
        this.configManager = game.getConfigManager();
        executor = Executors.newFixedThreadPool(2);
        soundCache = new HashMap<>();
        musicCache = new HashMap<>();
        currentSounds = new HashMap<>();
        currentSoundIDs = new HashMap<>();
    }

    /**
     * Loads audio settings from configuration.
     */
    public void loadSettings() {
        configManager.loadAudioSettings();
        masterVolume = configManager.getVolume("masterVolume");
        soundEffectsVolume = configManager.getVolume("soundEffectsVolume");
        musicVolume = configManager.getVolume("musicVolume");
    }

    // Play sound effect asynchronously

    /**
     * Plays a sound effect asynchronously.
     *
     * @param soundPath path relative to the sound folder
     * @param volume    base volume multiplier
     */
    public void playSound(String soundPath, float volume) {
        executor.submit(() -> {
            Sound sound = getOrLoadSound(soundPath);
            if (sound != null) {
                long id = sound.play(volume * soundEffectsVolume * masterVolume);
                currentSounds.put(soundPath, sound);
                currentSoundIDs.put(soundPath, id);
            }
        });
    }

    // Play sound with pitch and pan control

    /**
     * Plays a sound effect with pitch and pan controls asynchronously.
     *
     * @param soundPath path relative to the sound folder
     * @param volume    base volume multiplier
     * @param pitch     pitch multiplier
     * @param pan       stereo pan (-1 left to 1 right)
     */
    public void playSound(String soundPath, float volume, float pitch, float pan) {
        executor.submit(() -> {
            Sound sound = getOrLoadSound(soundPath);
            if (sound != null) {
                internalPlaySound(soundPath, volume, pitch, pan, sound);
            }
        });
    }

    // Play looping sound (e.g., ambient effects)

    /**
     * Plays a looping sound effect asynchronously.
     *
     * @param soundPath path relative to the sound folder
     * @param volume    base volume multiplier
     */
    public void playSoundLooping(String soundPath, float volume) {
        executor.submit(() -> {
            Sound sound = getOrLoadSound(soundPath);
            if (sound != null) {
                long soundID = sound.play(volume * soundEffectsVolume * masterVolume);
                sound.setLooping(soundID, true);
                currentSounds.put(soundPath, sound);
                currentSoundIDs.put(soundPath, soundID);
            }
        });
    }

    public void playSoundLooping(String soundPath, float volume, float pitch, float pan) {
        executor.submit(() -> {
            Sound sound = getOrLoadSound(soundPath);
            if (sound != null) {
                long soundID = internalPlaySound(soundPath, volume, pitch, pan, sound);
                sound.setLooping(soundID, true);
            }
        });
    }

    private long internalPlaySound(String soundPath, float volume, float pitch, float pan, Sound sound) {
        long soundID = sound.play(volume * soundEffectsVolume * masterVolume);
        currentSounds.put(soundPath, sound);
        currentSoundIDs.put(soundPath, soundID);
        sound.setPitch(soundID, pitch);
        sound.setPan(soundID, pan, volume * soundEffectsVolume * masterVolume);
        return soundID;
    }

    public void stopSound(String soundPath) {
        executor.submit(() -> {
            if (currentSounds.containsKey(soundPath)) {
                Sound sound = currentSounds.get(soundPath);
                Long soundID = currentSoundIDs.get(soundPath);
                if (sound != null && soundID != null) {
                    sound.stop(soundID);
                }
                currentSounds.remove(soundPath);
                currentSoundIDs.remove(soundPath);
            } else {
                Sound sound = getOrLoadSound(soundPath);
                if (sound != null) {
                    sound.stop();
                }
            }
        });
    }

    public void stopAllSounds() {
        executor.submit(() -> {
            for (Sound sound : currentSounds.values()) {
                if (sound != null) sound.stop();
            }
            currentSounds.clear();
            currentSoundIDs.clear();
        });
    }
    // Play music track with optional looping

    /**
     * Plays a music track asynchronously.
     *
     * @param musicPath path relative to the music folder
     * @param volume    base volume multiplier
     * @param loop      whether the music should loop
     */
    public void playMusic(String musicPath, float volume, boolean loop) {
        executor.submit(() -> {
            // Stop current music before starting new
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.stop();
            }

            Music music = getOrLoadMusic(musicPath);
            if (music != null) {
                music.setVolume(volume * musicVolume * masterVolume);
                music.setLooping(loop);
                music.play();
                currentMusic = music;
                currentMusicVolume = volume;
            }
        });
    }

    /**
     * Stops the currently playing music asynchronously.
     */
    public void stopMusic() {
        executor.submit(() -> {
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.stop();
            }
        });
    }


    /**
     * Pauses or resumes the current music asynchronously.
     *
     * @param paused {@code true} to pause, {@code false} to resume
     */
    public void pauseMusic(boolean paused) {
        executor.submit(() -> {
            if (currentMusic != null) {
                if (paused && currentMusic.isPlaying()) {
                    currentMusic.pause();
                } else if (!paused && !currentMusic.isPlaying()) {
                    currentMusic.play();
                }
            }
        });
    }

    // Load sound into cache if not already loaded

    /**
     * Gets a cached sound or loads it if missing.
     *
     * @param path relative sound path
     * @return loaded sound, or {@code null} if loading fails
     */
    private Sound getOrLoadSound(String path) {
        return soundCache.computeIfAbsent(path, p -> {
            try {
                return Gdx.audio.newSound(Gdx.files.internal("Sound/" + p));
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load sound: " + p, e);
                return null;
            }
        });
    }

    // Load music into cache if not already loaded

    /**
     * Gets a cached music track or loads it if missing.
     *
     * @param path relative music path
     * @return loaded music, or {@code null} if loading fails
     */
    private Music getOrLoadMusic(String path) {
        return musicCache.computeIfAbsent(path, p -> {
            try {
                return Gdx.audio.newMusic(Gdx.files.internal("Music/" + p));
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load music: " + p, e);
                return null;
            }
        });
    }

    // Preload sounds on a background thread (useful for startup)

    /**
     * Preloads multiple sound effects asynchronously.
     *
     * @param soundPaths sound paths to preload
     */
    public void preloadSounds(String... soundPaths) {
        executor.submit(() -> {
            for (String path : soundPaths) {
                getOrLoadSound(path);
            }
        });
    }

    // Clear all cached audio and dispose of resources

    /**
     * Disposes cached audio resources and shuts down the executor.
     */
    public void dispose() {
        executor.submit(() -> {
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.stop();
            }

            soundCache.values().forEach(Sound::dispose);
            musicCache.values().forEach(Music::dispose);
            soundCache.clear();
            musicCache.clear();
        });

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns the master volume.
     *
     * @return master volume [0,1]
     */
    public float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Sets the master volume and persists it.
     *
     * @param volume new master volume
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(1f, volume));
        configManager.setVolume("masterVolume", masterVolume);
        handleActiveMusic();
    }


    /**
     * Returns the sound effects volume.
     *
     * @return sound effects volume [0,1]
     */
    public float getSoundEffectsVolume() {
        return soundEffectsVolume;
    }

    /**
     * Sets the sound effects volume and persists it.
     *
     * @param volume new sound effects volume
     */
    public void setSoundEffectsVolume(float volume) {
        configManager.setVolume("soundEffectsVolume", soundEffectsVolume);
        this.soundEffectsVolume = MathUtils.clamp(volume, 0f, 1f);
    }

    /**
     * Returns the music volume.
     *
     * @return music volume [0,1]
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Sets the music volume and persists it.
     *
     * @param volume new music volume
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = MathUtils.clamp(volume, 0f, 1f);
        configManager.setVolume("musicVolume", musicVolume);
        handleActiveMusic();
    }

    public void setActiveSoundVolume(String soundPath, float volume) {
        executor.submit(() -> {
            Sound sound = currentSounds.get(soundPath);
            Long soundID = currentSoundIDs.get(soundPath);
            if (sound != null && soundID != null) {
                sound.setVolume(soundID, volume * soundEffectsVolume * masterVolume);
            }
        });
    }

    /**
     * Applies volume changes to the currently playing music.
     */
    public void handleActiveMusic() {
        executor.submit(() -> {
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.setVolume(currentMusicVolume * musicVolume * masterVolume);
            }
        });
    }
}
