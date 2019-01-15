package de.hshannover.inform.gnuman.app.modules.audio;

import javax.sound.sampled.AudioFormat;

/**
 * Wraps data used by the sound implementation.
 * @author Marc Herschel
 */

public class SimultaneousClip {
    private AudioFormat format;
    private byte[] data;

    public SimultaneousClip(AudioFormat format, byte[] data) {
        this.format = format;
        this.data = data;
    }

    public AudioFormat getFormat() {
        return format;
    }

    public byte[] getData() {
        return data;
    }
}
