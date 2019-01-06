package de.hshannover.inform.gnuman.app;

import de.hshannover.inform.gnuman.app.enums.AudioFiles;
import de.hshannover.inform.gnuman.app.model.storage.GameSettings;

/**
 * Sounds will be managed via static methods.
 * @author Marc Herschel
 */

public class AudioManager {
    public static GameSettings currentSettings;

    public static void adjustAll(double multiplicator) {
        for(AudioFiles f : AudioFiles.values()) {
            f.getAudioFile().setVolume(f.getBaseVolumeLevel()*multiplicator);
        }
        AudioFiles.MENU.getAudioFile().stop(); AudioFiles.MENU.getAudioFile().play();
    }

    public static void stopAllSounds() {
        for(AudioFiles a : AudioFiles.values()) { a.getAudioFile().stop(); }
     }

    public static void playSound(AudioFiles file) {
        if(currentSettings.isSoundOn()) { file.getAudioFile().play(); }
    }

    public static void decideMusic(boolean isElroy, boolean isFrightened) {
        stopGameMusic();
        if(isElroy && !isFrightened) { playSound(AudioFiles.MUSIC_ELROY); return; }
        if(isFrightened) { playSound(AudioFiles.MUSIC_FRIGHTENED); return; }
        playSound(AudioFiles.MUSIC_NORMAL);
    }

    public static void stopGameMusic() {
        if(AudioFiles.MUSIC_ELROY.getAudioFile().isPlaying()) { AudioFiles.MUSIC_ELROY.getAudioFile().stop(); }
        if(AudioFiles.MUSIC_FRIGHTENED.getAudioFile().isPlaying()) { AudioFiles.MUSIC_FRIGHTENED.getAudioFile().stop(); }
        if(AudioFiles.MUSIC_NORMAL.getAudioFile().isPlaying()) { AudioFiles.MUSIC_NORMAL.getAudioFile().stop(); }
    }

    public static void toggleMainTheme() {
        if(AudioFiles.MENU.getAudioFile().isPlaying()) { AudioFiles.MENU.getAudioFile().stop(); }
        if(!AudioFiles.MENU.getAudioFile().isPlaying() && currentSettings.isMusicOn()) { AudioFiles.MENU.getAudioFile().play(); }
    }

    public static void startUiMusic() {
        if(currentSettings.isMusicOn() && !AudioFiles.MENU.getAudioFile().isPlaying()) { AudioFiles.MENU.getAudioFile().play(); }
    }

    public static void stopUiMusic() {
        if(AudioFiles.MENU.getAudioFile().isPlaying()) { AudioFiles.MENU.getAudioFile().stop(); }
    }
}
