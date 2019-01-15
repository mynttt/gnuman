package de.hshannover.inform.gnuman.app.modules.audio;

import java.net.URL;
import javax.sound.sampled.Clip;

import de.hshannover.inform.gnuman.app.interfaces.GameAudio;

/**
 * Implements Music which is a clip that loops.
 * @author Marc Herschel
 */

public class Music implements GameAudio {
    private Clip clip;
    private float initialVolume;

    /**
     * Create new music with an initial volume.
     * @param location of the file.
     * @param initialVolume initial volume between 0 and 1.
     */
    public Music(URL location, float initialVolume) {
        clip = ClipUtil.createClip(location);
        this.initialVolume = initialVolume;
        setVolume(initialVolume);
    }

    /**
     * Start the music.
     */
    public void start() { if(!clip.isActive()) { clip.setFramePosition(0); clip.loop(Clip.LOOP_CONTINUOUSLY); } }

    /**
     * Stop the music.
     */
    public void stop() { if(clip.isActive()) { clip.stop(); } }

    /**
     * @return true if playing
     */
    public boolean isPlaying() { return clip.isActive(); }

    @Override
    public float getVolume() { return ClipUtil.getVolume(clip); }

    @Override
    public void setVolume(float volume) { ClipUtil.setVolume(volume, clip); }

    @Override
    public void stopAllInstances() { stop(); }

    @Override
    public float getInitialVolume() {
        return initialVolume;
    }
}