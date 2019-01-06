package de.hshannover.inform.gnuman.app.enums;

import javafx.scene.media.AudioClip;
import de.hshannover.inform.gnuman.Constants;

/**
 * God enum for audio files.
 * @author Marc Herschel
 */

public enum AudioFiles {
    MENU("track1.mp3", 0.07, AudioClip.INDEFINITE),
    MUSIC_ELROY("music_elroy_active.mp3", 0.05, AudioClip.INDEFINITE),
    MUSIC_NORMAL("music_speed_normal.mp3", 0.05, AudioClip.INDEFINITE),
    MUSIC_FRIGHTENED("music_frightened.mp3", 0.05, AudioClip.INDEFINITE),
    EATING_DOTS("eating-continues.mp3", 0.12, AudioClip.INDEFINITE),
    CLICK("click.mp3", 0.12, 1),
    TYPING("typing.mp3", 0.2, 1),
    ADD_HIGHSCORE("add_highscore.mp3", 0.2, 1),
    EATING_GHOST("eating-ghost.mp3", 0.2, 1),
    EATING_POWERUP("eat-pill.mp3", 0.1, 1),
    STARTING_MUSIC("start-music.mp3", 0.1, 1),
    DIE("die.mp3", 0.14, 1),
    BONUS("bonus.mp3", 0.15, 1),
    GHOST_ATE_ITEM("ghost_ate_item.mp3", 0.15, 1),
    LEVEL_FINISHED("level_finished.wav", 0.2, 1),
    EXTRA_LIFE("extra_life.mp3", 0.2, 1);

    private AudioClip clip;
    private double baseLevel;

    AudioFiles(String location, double baseLevel, int cycle) {
        this.clip = new AudioClip(getClass().getResource(Constants.AUDIO_FILE_PREFIX + location).toExternalForm());
        this.baseLevel = baseLevel;
        clip.setVolume(baseLevel);
        clip.setCycleCount(cycle);
    }

    /**
     * @return Audio file mapped to enum constant.
     */
    public AudioClip getAudioFile() {
        return clip;
    }

    /**
     * @return hard coded base volume for audio file.
     */
    public double getBaseVolumeLevel() {
        return baseLevel;
    }
}
