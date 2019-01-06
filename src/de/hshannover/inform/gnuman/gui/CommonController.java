package de.hshannover.inform.gnuman.gui;

import de.hshannover.inform.gnuman.SceneManager;
import de.hshannover.inform.gnuman.app.model.storage.GameSettings;

/**
 * Provides common type and functionality to all controllers.
 * @author Marc Herschel
 */

public class CommonController {
    protected SceneManager manager;
    protected GameSettings gameOptions;

    /**
     * Get the controller.
     * @return The controller in the CommonControllerType type.
     */
    public CommonController getController() {
        return this;
    }

    /**
     * Set a SceneManager
     * @param sceneManager The manager to set for this controller.
     */
    public void setSceneManager(SceneManager sceneManager) {
        this.manager = sceneManager;
    }

    /**
     * Set GameOptions
     * @param options The options object to set for this controller.
     */
    public void setOptions(GameSettings options) {
        this.gameOptions = options;
    }

}
