package de.hshannover.inform.gnuman;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import de.hshannover.inform.gnuman.app.AudioManager;
import de.hshannover.inform.gnuman.app.enums.AudioFiles;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.app.model.storage.GameSettings;
import de.hshannover.inform.gnuman.app.util.Helper;
import de.hshannover.inform.gnuman.gui.AddHighscoreController;
import de.hshannover.inform.gnuman.gui.CommonController;
import de.hshannover.inform.gnuman.gui.LectureWindowController;
import de.hshannover.inform.gnuman.gui.GameWindowController;
import de.hshannover.inform.gnuman.gui.MainWindowController;
import de.hshannover.inform.gnuman.gui.OptionsGraphicsController;
import de.hshannover.inform.gnuman.gui.PauseWindowController;

/**
 * This class loads and manages the different FXML files. It allows for a switching of scenes on a stage and gives
 * easy access to the controllers. <p>
 *
 * The states and FXML files locations are stored in the UIStates enum.
 * @author Marc Herschel
 */

public class SceneManager {
    private UIStates current;
    private Stage rootStage = null;
    private Scene[] scenes;
    private CommonController[] controller;
    private boolean mainMenuCalled;

    /**
     * Constructs a new scene manager for a stage and loads all FXML files.
     * @param root The stage to use the manager.
     */
    public SceneManager(Stage root) {
        Log.info(getClass().getSimpleName(), "Initiating scenes.");
        this.rootStage = root;

        try {

            this.scenes = new Scene[UIStates.values().length];
            this.controller = new CommonController[UIStates.values().length];
            this.mainMenuCalled = false;

            GameSettings options = new GameSettings(Constants.DEFAULT_BLOCK_DIMENSIONS, Constants.POSSIBLE_FRAMERATES);
            AudioManager.currentSettings = options;

            for(UIStates state : UIStates.values()) {

            //Load FXML, load Controller and assign objects to the controller.
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_FILE_PREFIX + state.getFxmlLocation()));
                scenes[state.ordinal()] = new Scene(loader.load());
                scenes[state.ordinal()].getStylesheets().add(getClass().getResource(Constants.FXML_CSS_FILE).toExternalForm());
                controller[state.ordinal()] = loader.getController();
                controller[state.ordinal()].setSceneManager(this);
                controller[state.ordinal()].setOptions(options);

            //Assign sound to every existing button (but skip add highscore).
                if(state != UIStates.ADD_HIGHSCORE) {
                    for(Node node : Helper.getAllNodes(scenes[state.ordinal()].getRoot())) {
                        if(node instanceof Button) {
                            ((Button) node).addEventFilter(ActionEvent.ACTION, e -> AudioManager.playSound(AudioFiles.CLICK));
                        }
                    }
                }

                Log.info(getClass().getSimpleName(), "Initiated: " + controller[state.ordinal()].getClass().getSimpleName());
            }

            Log.info(getClass().getSimpleName(), "Starting with internal initialization.");
            internalInitialization();

        } catch(IOException e) {
            e.printStackTrace();
            Log.critical(getClass().getSimpleName(), "Failed to initiate scenes duo IOException. Check the FXML filepaths.");
            Helper.exitOnCritical();
        } catch(Exception e) {
            e.printStackTrace();
            Log.critical(getClass().getSimpleName(), "Unknown exception at startup of scene manager." + Helper.stackTraceToString(e));
            Helper.exitOnCritical();
        }
        Log.info(getClass().getSimpleName(), "Scenes and controllers initiated successfully.");

    }

    /**
     * After constructing the scene manager you can now call a scene.
     * @param state State to initiate with.
     */
    public void callInitialScene(UIStates state) {
        Log.info(getClass().getSimpleName(), "Initial state set to: " + state);
        switchScene(state);
        current = state;
        rootStage.setResizable(false);
        rootStage.sizeToScene();
        rootStage.show();
    }

    /**
     * This will cause the stage to switch to the given scene.
     * @param state State to switch to.
     */
    public void switchScene(UIStates state) {
        /*
         * Hack to synchronize options with options object in case the game had to lower the block size.
         */
        if(state == UIStates.OPTIONS_GRAPHIC) { ((OptionsGraphicsController) getController(UIStates.OPTIONS_GRAPHIC)).updateOnVisit(); }

        /**
         * Start the ticker if we switch to main menu
         */
        if(state == UIStates.MAIN_MENU) {
            /*
             * Ugly hack, if we come from a scene where we used a canvas previously the immediate start of the timer will cause the
             * content to be black. So we let the timer wait a bit and then start the ticker.
             */
            if(current == UIStates.LECTURE || current == UIStates.GAME_WINDOW) {
                 new Timer().schedule(new TimerTask(){
                     
                     @Override
                     public void run() {
                         if(current == UIStates.MAIN_MENU && ((MainWindowController) getController(UIStates.MAIN_MENU)).isTickerPaused()) {
                             startTicker(); 
                         }
                         this.cancel(); 
                     }
                     
                 }, 1000, 1000);
                 
            } else {
            /**
             * These scenes do not use a canvas and thus are allowed to instantly start the timer.
             */
                startTicker();
            }
        }
        
        rootStage.setScene(scenes[state.ordinal()]);
        current = state;
        
        Log.info(getClass().getSimpleName(), "UI_STATE -> " + state.toString());
    }

    /**
     * In case you need to access the scene to add listeners or elements this will be your friend.
     * @param state State to access.
     * @return The scene that corresponds to the state.
     */
    public Scene getScene(UIStates state) {
        return scenes[state.ordinal()];
    }

    /**
     * In case you need to access a controller, you can access this.
     * @param state State to access.
     * @return The controller in a CommonControllerType type. You NEED to cast if you want to use the unique methods of the controller.
     */
    public CommonController getController(UIStates state) {
        return controller[state.ordinal()];
    }

    /**
     * Initializations that are not possible by the FXML initialize() method after the scene manager loaded all.
     */
    private void internalInitialization() {
    //Assign Highscore to addHighscoreController so adding works and create bindings for enter key.
        ((AddHighscoreController) getController(UIStates.ADD_HIGHSCORE)).initializeAddHighscore();
    //Initialize keybinds for the pause window, set GameWindowController as well
        ((PauseWindowController) getController(UIStates.PAUSED)).setKeyBinds();
    //Initialize all reusable
        ((GameWindowController) getController(UIStates.GAME_WINDOW)).initializeGame();
    //Check default blocksize against the default map, update to largest possible blocksize.
        ((OptionsGraphicsController) getController(UIStates.OPTIONS_GRAPHIC)).triggerDimensionUpdate(Constants.DEFAULT_BLOCK_DIMENSIONS);
    //Oh no! Someone did not call it GNU/Linux...
        ((LectureWindowController) getController(UIStates.LECTURE)).someoneCalledItLinuxInsteadOfGnuLinux();
    }
    
    /*
     * Ugly hack to deal with the bugged first animation of the text ticker.
     */
    private void startTicker() {
         if(mainMenuCalled) { ((MainWindowController) getController(UIStates.MAIN_MENU)).startTickerWhenSwitchToMainMenuAgain(); } mainMenuCalled = true;
    }
}
