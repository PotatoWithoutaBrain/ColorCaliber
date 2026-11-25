package com.ColorCaliber.frontend.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles sound effects and background music across the game.
 * Implemented as a singleton so any entity or manager can trigger audio
 * without having to pass references everywhere.
 */
public class AudioManager implements Disposable {

    private static AudioManager instance;

    // Cache loaded sounds so we don't hit the disk or reload them every time they play
    private final Map<String, Sound> soundCache;
    private Music currentMusic;
    private String currentMusicPath;

    private float sfxVolume = 1.0f;
    private float musicVolume = 0.6f;
    private boolean sfxMuted = false;
    private boolean musicMuted = false;

    private AudioManager() {
        soundCache = new HashMap<String, Sound>();
    }

    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    // --- Sound Effects ---

    /**
     * Looks up a sound in our cache, or loads it if it's the first time being played.
     * If the audio file doesn't exist in assets yet, we fail quietly so the game doesn't crash.
     */
    private Sound getOrLoadSound(String internalPath) {
        if (soundCache.containsKey(internalPath)) {
            return soundCache.get(internalPath);
        }

        if (Gdx.files == null || !Gdx.files.internal(internalPath).exists()) {
            // Audio file hasn't been added to assets yet, skip it for now
            return null;
        }

        try {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(internalPath));
            soundCache.put(internalPath, sound);
            return sound;
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Failed to load sound: " + internalPath, e);
            return null;
        }
    }

    public void playSound(String internalPath) {
        playSound(internalPath, 1.0f);
    }

    public void playSound(String internalPath, float volumeMultiplier) {
        if (sfxMuted) return;

        Sound sound = getOrLoadSound(internalPath);
        if (sound != null) {
            sound.play(sfxVolume * volumeMultiplier);
        }
    }

    // Quick helpers for frequent gameplay events

    public void playShootSound() {
        playSound("sounds/shoot.wav", 0.7f);
    }

    public void playDodgeRollSound() {
        playSound("sounds/dodge_roll.wav", 0.8f);
    }

    public void playEnemyHitSound() {
        playSound("sounds/enemy_hit.wav", 0.6f);
    }

    public void playEnemyDeathSound() {
        playSound("sounds/enemy_death.wav", 0.8f);
    }

    public void playPlayerHurtSound() {
        playSound("sounds/player_hurt.wav", 0.9f);
    }

    public void playPlayerDeathSound() {
        playSound("sounds/player_death.wav", 1.0f);
    }

    public void playBluePointCollectSound() {
        playSound("sounds/pickup.wav", 0.8f);
    }

    public void playUpgradeSound() {
        playSound("sounds/upgrade.wav", 1.0f);
    }

    // --- Background Music ---

    public void playLevelMusic() {
        if (Gdx.files != null && Gdx.files.internal("music/BGM_Level_1.mp3").exists()) {
            playMusic("music/BGM_Level_1.mp3", true);
        } else {
            playMusic("BGM_Level_1.mp3", true);
        }
    }

    public void playMusic(String internalPath, boolean loop) {
        if (internalPath == null) return;

        // If the same track is already playing, let it keep playing
        if (internalPath.equals(currentMusicPath) && currentMusic != null && currentMusic.isPlaying()) {
            return;
        }

        stopMusic();

        if (Gdx.files == null || !Gdx.files.internal(internalPath).exists()) {
            return;
        }

        try {
            currentMusic = Gdx.audio.newMusic(Gdx.files.internal(internalPath));
            currentMusicPath = internalPath;
            currentMusic.setLooping(loop);
            currentMusic.setVolume(musicMuted ? 0f : musicVolume);
            currentMusic.play();
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Failed to play music: " + internalPath, e);
            currentMusic = null;
            currentMusicPath = null;
        }
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
            currentMusicPath = null;
        }
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying() && !musicMuted) {
            currentMusic.play();
        }
    }

    // --- Volume & Settings ---

    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null && !musicMuted) {
            currentMusic.setVolume(this.musicVolume);
        }
    }

    public void toggleMute() {
        setMuted(!sfxMuted);
    }

    public void setMuted(boolean muted) {
        this.sfxMuted = muted;
        this.musicMuted = muted;
        if (currentMusic != null) {
            currentMusic.setVolume(muted ? 0f : musicVolume);
        }
    }

    public boolean isMuted() {
        return sfxMuted;
    }

    // --- Cleanup ---

    @Override
    public void dispose() {
        // Free native audio buffers to prevent OpenAL memory leaks
        for (Sound sound : soundCache.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }
        soundCache.clear();

        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
            currentMusicPath = null;
        }
    }
}
