package de.hshannover.inform.gnuman.app.enums;

/**
 * Representing the games difficulty levels, high score uses this to build itself.
 * @author Marc Herschel
 */

public enum Difficulty {
    SLOW(0.7),
    NORMAL(1),
    FAST(2.25);

    private double speedMultiplicator;
    Difficulty(double d) { this.speedMultiplicator = d; }

    /**
     * @return speed multiplicator for given difficulty.
     */
    public double getSpeedMultiplicator() { return speedMultiplicator; }
}
