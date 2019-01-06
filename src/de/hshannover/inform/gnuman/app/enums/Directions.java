package de.hshannover.inform.gnuman.app.enums;

/**
 * Represents the directions an entity can move to.<br>
 * Additional parameters are required for the Ghost AI.
 * @author Marc Herschel
 */

public enum Directions {
    UP(0, -1, 2),
    LEFT(-1, 0, 2),
    DOWN(0, 1, -2),
    RIGHT(1, 0, -2),
    NONE(0, 0, 0);

    private int xModifier, yModifier, inversionModifier;
    Directions(int x, int y, int i) { this.xModifier = x; this.yModifier = y; this.inversionModifier = i; }

    /**
     * @return xModifier for leaving spawn area.
     */
    public int getXModifier() { return xModifier; }

    /**
     * @return yModifier for leaving spawn area.
     */
    public int getYModifier() { return yModifier; }

    /**
     * @return the inversion of the direction.
     */
    public Directions invert() { return Directions.values()[ordinal()+ inversionModifier]; }

    /**
     * @return all directions without the none value.
     */
    public static Directions[] loadOrder() {
        return new Directions[] { UP, DOWN, LEFT, RIGHT };
    }
}
