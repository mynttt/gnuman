package de.hshannover.inform.gnuman.app.model.storage;

import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.app.util.Helper;

/**
 * Store game settings.
 * @author Marc Herschel
 */

public class GameSettings {
    private boolean useArrows, soundOn, musicOn, showFps, dynCamera;
    private float baseLevelMultiplicator;
    private int fps, fpsIndex, blockDimension;
    private int[] possibleFrameRates;

    /**
     * Construct a new GameSettings objects
     * @param defaultBlockDimension for resolution.
     * @param possibleFrameRates to check for.
     */
    public GameSettings(int defaultBlockDimension, int[] possibleFrameRates) {
        useArrows = true;
        soundOn = musicOn = !Constants.DEBUG_NO_SOUND;
        showFps = false;
        fps = (Constants.DEBUG_USE_OWN_FPS) ? Constants.DEBUG_FPS : 60;
        fpsIndex = 0;
        blockDimension = defaultBlockDimension;
        this.possibleFrameRates = possibleFrameRates;
        this.baseLevelMultiplicator = 1.0f;
    }

    public boolean isMusicOn() { return musicOn; }
    public boolean isUseArrows() { return useArrows; }
    public boolean isSoundOn() { return soundOn; }
    public boolean trackFps() { return showFps; }
    public boolean dynCamera() { return dynCamera; }
    public int getFps() { return fps; }
    public int getBlockDimension() { return blockDimension; }
    public float getBaseLevelMultiplicator() { return baseLevelMultiplicator; }
    public void setBlockDimension(int newDimension) {
        if(newDimension < 5) { Log.critical(getClass().getSimpleName(), "Block Dimensions < 5 are not supported! Game not playable under your resolution."); Helper.exitOnCritical(); }
        blockDimension = newDimension;
    }
    public void setBaseLevelMultiplicator(float newBaseLevelMultiplicator) {
        if(newBaseLevelMultiplicator <= 0) { return; }
        baseLevelMultiplicator = newBaseLevelMultiplicator;
    }
    public void toggleMusic() { musicOn = !musicOn; }
    public void toggleSound() { soundOn = !soundOn; }
    public void toggleControls() { useArrows = !useArrows; }
    public void toggleFpsTracking() { showFps = !showFps; }
    public void toggleDynamicCamera() { dynCamera = !dynCamera; }
    public void cycleFps() { fps = possibleFrameRates[++fpsIndex%possibleFrameRates.length]; }
}
