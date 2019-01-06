package de.hshannover.inform.gnuman.gui.components;

import java.util.Arrays;
import javafx.scene.control.Label;
import de.hshannover.inform.gnuman.app.enums.Difficulty;

/**
 * Label that can carry multiple sets of data and change what to display based on a state.
 * @author Marc Herschel
 */

public class StateLabel extends Label {
    private String[] states;

    public StateLabel(String cssId, int stateCount) {
        this.states = new String[stateCount];
        this.setId(cssId);
    }

    public void setState(Difficulty difficulty, String data) {
        this.states[difficulty.ordinal()] = data;
    }

    public void switchState(Difficulty difficulty) {
        this.setText(states[difficulty.ordinal()]);
    }

    @Override
    public String toString() {
        return Arrays.toString(states);
    }
}
