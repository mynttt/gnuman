package de.hshannover.inform.gnuman;

import de.hshannover.inform.gnuman.app.AudioManager;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.app.model.storage.GameSettings;
import de.hshannover.inform.gnuman.app.util.Helper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Launch the game
 * @author Marc Herschel
 */

public class GameLauncher extends Application {
    private static AudioManager audioManager;
    private static Stage primaryStage = null;
    private SceneManager m;
    
    /*
     * Mitigates the primaryStage.setResizable(false); bug with JavaFX that exists on Linux and stops the stage from resizing
     * It calculates an offset once (platform dependant window decorations) and then forbids resizing by setting max/min width in the scene manager
     * This allows to forbid resizing without using the bugged setResizable(false)
     */
    
    public static boolean RUN_LINUX_MITIGATION = false;
    public static double LINUX_RESIZE_BUG_MITIGATION_OFFSET_X, LINUX_RESIZE_BUG_MITIGATION_OFFSET_Y = 0;
    
    static {
        String os = System.getProperty("os.name").toLowerCase();
        RUN_LINUX_MITIGATION = os.contains("nix") || os.contains("nux") || os.contains("aix");
    }
    
    @Override
    public void start(Stage primaryStage) {
        GameLauncher.primaryStage = primaryStage;

        if(Constants.DEBUG_LOG) {
            if(Log.hasBeenBootstrapped()) {
                Log.logger().reset();
            } else {
                Log.bootstrapLogger(Constants.DEBUG_LOG_FILE, Constants.DEBUG_LEVEL_MIN, Constants.DEBUG_LOG_TIME_FORMAT);
            }
            Log.info(getClass().getSimpleName(), "Starting GNUMAN...");
        }

        primaryStage.getIcons().clear();
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(Constants.GAME_ICON)));
        primaryStage.setTitle("GNUMAN");

        Canvas c = new Canvas(875, 600);
        Scene disclaimer = new Scene(new AnchorPane(c));
        c.getGraphicsContext2D().setFill(Color.BLACK);
        c.getGraphicsContext2D().drawImage(new Image(getClass().getResourceAsStream("/data/disclaimer.png")), 0, 0);
        primaryStage.setScene(disclaimer);
        primaryStage.sizeToScene();
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
        primaryStage.requestFocus();
        primaryStage.setAlwaysOnTop(false);
        
        new Thread(() -> {
            m = new SceneManager(primaryStage);
        }).start();
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                while(m == null) { Thread.sleep(500); }
                Platform.runLater(() -> mainMenu());
            } catch (InterruptedException e) {}
        }).start();
        
        if(RUN_LINUX_MITIGATION) {
            LINUX_RESIZE_BUG_MITIGATION_OFFSET_X = primaryStage.getWidth() - primaryStage.getScene().getWidth();
            LINUX_RESIZE_BUG_MITIGATION_OFFSET_Y = primaryStage.getHeight() - primaryStage.getScene().getHeight();
            primaryStage.setMaxWidth(primaryStage.getWidth());
            primaryStage.setMaxHeight(primaryStage.getHeight());
            primaryStage.setMinWidth(primaryStage.getWidth());
            primaryStage.setMinHeight(primaryStage.getHeight());
        } else {
            primaryStage.setResizable(false);
        }
    }

    private void mainMenu() {
        m.callInitialScene(UIStates.MAIN_MENU);
        audioManager.startUiMusic();
        if(Constants.DEBUG_AUDIO_TEST) { audioManager.testAudioFiles(); }
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch(Exception e) {
            e.printStackTrace();
            if(Log.hasBeenBootstrapped()) {
                Log.critical(GameLauncher.class.getSimpleName(), "Critical Error has been detected. It is not possible to recover from this.\n" + Helper.stackTraceToString(e));
            }
            System.exit(1);
        }
    }

    public static AudioManager am() { return audioManager; }
    public static void setFPS(int fps) { primaryStage.setTitle("FPS: " + fps); }
    public static void disableFPSTracking() { primaryStage.setTitle("GNUMAN"); }
    public static void hide() { primaryStage.hide(); }
    public static void show() { primaryStage.show(); primaryStage.sizeToScene(); }
    public static Stage getStage() { return primaryStage; }
    public static void createAudioManager(GameSettings settings) { if(audioManager == null) { audioManager = AudioManager.createAudioManager(settings); }}
}
