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

public class AudioManager {
    private final ConfigManager configManager;
    private final ExecutorService executor;
    private final Map<String, Sound> soundCache;
    private final Map<String, Music> musicCache;
    private Music currentMusic;
    private float masterVolume, soundEffectsVolume, musicVolume;


    public AudioManager(MazeRunnerGame game) {
        this.configManager = game.getConfigManager();
        executor = Executors.newFixedThreadPool(2);
        soundCache = new HashMap<>();
        musicCache = new HashMap<>();
        configManager.loadAudioSettings();
        masterVolume = configManager.getVolume("masterVolume");
        soundEffectsVolume = configManager.getVolume("soundEffectsVolume");
        musicVolume = configManager.getVolume("musicVolume");
    }

    // Play sound effect asynchronously
    public void playSound(String soundPath, float volume) {
        executor.submit(() -> {
            Sound sound = getOrLoadSound(soundPath);
            if (sound != null) {
                sound.play(volume * soundEffectsVolume * masterVolume);
            }
        });
    }

    // Play sound with pitch and pan control
    public void playSound(String soundPath, float volume, float pitch, float pan) {
        executor.submit(() -> {
            Sound sound = getOrLoadSound(soundPath);
            if (sound != null) {
                long id = sound.play(volume * soundEffectsVolume * masterVolume);
                sound.setPitch(id, pitch);
                sound.setPan(id, pan, volume * soundEffectsVolume * masterVolume);
            }
        });
    }

    // Play looping sound (e.g., ambient effects)
    public void playSoundLooping(String soundPath, float volume) {
        executor.submit(() -> {
            Sound sound = getOrLoadSound(soundPath);
            if (sound != null) {
                long id = sound.play(volume * soundEffectsVolume * masterVolume);
                sound.setLooping(id, true);
            }
        });
    }

    // Play music track with optional looping
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
            }
        });
    }

    public void stopMusic() {
        executor.submit(() -> {
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.stop();
            }
        });
    }


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
    public void preloadSounds(String... soundPaths) {
        executor.submit(() -> {
            for (String path : soundPaths) {
                getOrLoadSound(path);
            }
        });
    }

    // Clear all cached audio and dispose of resources
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

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(1f, volume));
        configManager.setVolume("masterVolume", masterVolume);
        executor.submit(() -> {
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.setVolume(currentMusic.getVolume() * masterVolume);
            }
        });
    }

    public float getSoundEffectsVolume() {
        return soundEffectsVolume;
    }

    public void setSoundEffectsVolume(float volume) {
        configManager.setVolume("soundEffectsVolume", soundEffectsVolume);
        this.soundEffectsVolume = MathUtils.clamp(volume, 0f, 1f);
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = MathUtils.clamp(volume, 0f, 1f);
        configManager.setVolume("musicVolume", musicVolume);
        executor.submit(() -> {
            if (currentMusic != null && currentMusic.isPlaying()) {
                currentMusic.setVolume(currentMusic.getVolume() * masterVolume);
            }
        });
    }
}

