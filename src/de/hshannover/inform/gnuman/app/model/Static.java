package de.hshannover.inform.gnuman.app.model;

import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;

/**
 * Map objects that don't move.
 * @author Marc Herschel
 */

public class Static extends GameObject {
    private StaticObjects blockType;

    /**
     * Construct a static.
     * @param x position
     * @param y position
     * @param w width
     * @param h height
     * @param blockType type of static.
     */
    public Static(double x, double y, int w, int h, StaticObjects blockType) {
        super(x, y, w, h);
        this.blockType = blockType;
    }

    /**
     * @return type of static.
     */
    public StaticObjects getBlockType() {
        return blockType;
    }

    @Override
    public String toString() {
        return this.blockType + " -> " + super.toString();
    }
}
