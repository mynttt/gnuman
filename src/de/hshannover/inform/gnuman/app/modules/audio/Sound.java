package de.hshannover.inform.gnuman.app.modules.audio;

import java.net.URL;
import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;

import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.interfaces.GameAudio;
import de.hshannover.inform.gnuman.app.util.Helper;

/**
 * Implements a sound, which is a clip that can be played n times simultaneously.
 * @author Marc Herschel
 *
 */
public class Sound implements GameAudio {
    private static final int MAX_INSTANCES = 50;
    private LinkedList<Clip> activeClips;
    private AudioFormat format;
    private byte[] soundData;
    private float volume, initialVolume;

    /**
     * Create a sound with an initial volume.
     * @param location of the file.
     * @param initialVolume Between 0 and 1.
     */
    public Sound(URL location, float initialVolume) {
        SimultaneousClip tmp = ClipUtil.createSimultaneousClip(location);
        format = tmp.getFormat();
        soundData = tmp.getData();
        volume = initialVolume;
        this.initialVolume = initialVolume;
        activeClips = new LinkedList<>();
    }

    /**
     * Create a new sound instance (all using the same raw data, no spamming of memory).
     */
    public void createInstance() {
        try {
            if(activeClips.size() > MAX_INSTANCES) { return; }
            Clip c = AudioSystem.getClip();
            c.open(format, soundData, 0, soundData.length);
            ClipUtil.setVolume(volume, c);
            c.addLineListener(e -> { if(e.getType() == LineEvent.Type.STOP) { removeClipOnceFinished(c); } });
            addClipToList(c);
            c.start();
        } catch (LineUnavailableException e) {
            Log.warning(getClass().getSimpleName(), "Failed to play sound. \n" + Helper.stackTraceToString(e));
        }
    }

    private synchronized void removeClipOnceFinished(Clip clip) { activeClips.remove(clip); }
    private synchronized void addClipToList(Clip clip) { activeClips.add(clip); }
    private synchronized void adjustVolumeForPlayingClips(float volume) { activeClips.forEach(c -> ClipUtil.setVolume(volume, c)); }

    @Override
    public float getVolume() { return volume; }

    @Override
    public void setVolume(float volume) {
        if (volume < 0f || volume > 1f) { throw new IllegalArgumentException("Volume not valid: " + volume); }
        this.volume = volume;
        adjustVolumeForPlayingClips(volume);
    }

    @Override
    public synchronized void stopAllInstances() {
        activeClips.forEach(Clip::stop);
        activeClips.clear();
    }

    @Override
    public float getInitialVolume() {
        return initialVolume;
    }
}
