package de.hshannover.inform.gnuman.app.model;

import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;

/**
 * Ghostwalls have to know about their orientation. (although its not used at the moment).
 * @author Marc Herschel
 */

public class GhostWall extends Static {
    private boolean horizontal;

    public GhostWall(double x, double y, int w, int h, StaticObjects blockType, int alignment) {
        super(x, y, w, h, blockType);
        this.horizontal = (alignment == 1 || alignment == 0);
    }

    public boolean isHorizontal() {
        return horizontal;
    }
}
