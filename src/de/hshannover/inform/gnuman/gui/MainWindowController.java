package de.hshannover.inform.gnuman.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.gui.components.TextTicker;

/**
 * Controlling the MainWindow screen.
 * @author Marc Herschel
 */

public class MainWindowController extends CommonController {
    @FXML BorderPane textTickerRoot;
    TextTicker ticker;
    String[] wisdom;
    int wisdomIndex;

    /**
     * Start the ticker and load a new wisdom to display when we enter the menu.
     */
    public void startTickerWhenSwitchToMainMenuAgain() {
        if(wisdom != null) { wisdomIndex = ++wisdomIndex % wisdom.length; ticker.updateText(wisdom[wisdomIndex]); ticker.play(); }
    }
    
    public boolean isTickerPaused() { return ticker.isPaused(); }

    /**
     * Parse the RMS wisdom.
     */
    private void getWisdom() {
        LinkedList<String> tmp = new LinkedList<>();
        try (Scanner s = new Scanner(getClass().getResourceAsStream(Constants.MAIN_MENU_WISEDOM))) {
            while(s.hasNextLine()) { tmp.add(s.nextLine()); }
        } catch(Exception e) {
            tmp.add("FAILED TO LOAD WISDOM FROM: " + Constants.MAIN_MENU_WISEDOM);
        }
        Collections.shuffle(tmp);
        wisdom = tmp.toArray(new String[tmp.size()]);
    }

    /**
     * Stop the ticker when we leave the menu.
     * @param state State to switch to.
     */
    private void switchFromMenu(UIStates state) {
        if(wisdom != null) { ticker.pauseAndResetPosition(); }
        manager.switchScene(state);
    }

    @FXML
    private void initialize() {
        try { getWisdom(); } catch(Exception e) { Log.warning(getClass().getSimpleName(), "Failed to load wisdom: " + Constants.MAIN_MENU_WISEDOM); wisdom = null; }
        if(wisdom != null) {
            ticker = new TextTicker(textTickerRoot.getPrefWidth(), textTickerRoot.getPrefHeight(), e -> {
                wisdomIndex = ++wisdomIndex % wisdom.length;
                ticker.updateText(wisdom[wisdomIndex]);
            }, "text-ticker-text");
            textTickerRoot.setCenter(ticker);
            ticker.initiate(30);
        }
    }

    @FXML
    private void switchToNewGame() {
        switchFromMenu(UIStates.NEW_GAME);
    }

    @FXML
    private void switchToHighscore() {
        switchFromMenu(UIStates.HIGHSCORE);
    }

    @FXML
    private void switchToOptions() {
        switchFromMenu(UIStates.OPTIONS);
    }

    @FXML
    private void switchToExtras() {
        switchFromMenu(UIStates.EXTRAS);
    }

    @FXML
    private void exitGame() {
        System.exit(0);
    }
}
