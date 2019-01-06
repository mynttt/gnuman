package de.hshannover.inform.gnuman.app.enums.gameobjects;

/**
 * Represents the different entities that move around the map and store spawn leave delay information.
 * @author Marc Herschel
 */

public enum EntityObjects {
    PLAYER(0),
    BLINKY(0),
    INKY(1000),
    PINKY(2000),
    CLYDE(3000);

    private int delay;
    EntityObjects(int delay) {this.delay = delay; }
    public int getLeaveDelay() { return delay; }
    public static EntityObjects[] ghosts() { return new EntityObjects[] {BLINKY, INKY, PINKY, CLYDE}; }
}
