package de.hshannover.inform.gnuman.app.enums;

/**
 * Represents the UI States of the game. Each state has one FXML file that it corresponds to.
 * @author Marc Herschel
 */

public enum UIStates {
    MAIN_MENU("MainWindow.fxml"),
    NEW_GAME("NewGameWindow.fxml"),
    HIGHSCORE("HighscoreWindow.fxml"),
    OPTIONS("OptionsWindow.fxml"),
    OPTIONS_GRAPHIC("OptionsGraphicsWindow.fxml"),
    OPTIONS_AUDIO("OptionsAudioWindow.fxml"),
    PAUSED("PauseWindow.fxml"),
    GAME_WINDOW("GameWindow.fxml"),
    ADD_HIGHSCORE("AddHighscoreWindow.fxml"),
    HELP("HelpWindow.fxml"),
    EXTRAS("ExtrasWindow.fxml"),
    CREDITS("CreditsWindow.fxml"),
    LECTURE("LectureWindow.fxml");

    private String file;
    UIStates(String file) {
        this.file = file;
    }

    /**
     * Returns a FXML location for a state.
     * @return String of the file location.
     */
    public String getFxmlLocation() {
        return file;
    }
}
