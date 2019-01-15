package de.hshannover.inform.gnuman.app.interfaces;

/**
 * Operations that have to be supported by an audio implementation.
 * @author Marc Herschel
 */

public interface GameAudio {

    /**
     * Get the relative audio of the file.
     * @return Relative audio between 0 and 1.
     */
    public float getVolume();

    /**
     * Initial audio volume for scaling via options.
     * @return the initial volume of the audio.
     */
    public float getInitialVolume();

    /**
     * Set the relative audio of the file.
     * @param volume relative audio between 0 and 1.
     */
    public void setVolume(float volume);

    /**
     * Stop all instances of this audio.
     */
    public void stopAllInstances();
}