package de.hshannover.inform.gnuman.app.mapeditor;

import java.awt.Point;

import de.hshannover.inform.gnuman.app.mapeditor.Enums.TwoPointsSelectionStates;

/**
 * Helper if we want to get user input for two points on the map.
 * @author Marc Herschel
 */

public class TwoPointInput {
    TwoPointsSelectionStates state;
    Point[] points;

    public TwoPointInput() {
        state = TwoPointsSelectionStates.NONE;
        points = new Point[2];
    }

    void addPoint(int x, int y) {
        if (state == TwoPointsSelectionStates.NONE) {
            points[0] = new Point(x, y);
            state = TwoPointsSelectionStates.FIRST_SET;
            return;
        }
        if (state == TwoPointsSelectionStates.FIRST_SET) {
            points[1] = new Point(x, y);
            state = TwoPointsSelectionStates.BOTH_SET;
        }
    }

    void reset() {
        state = TwoPointsSelectionStates.NONE;
    }
}
