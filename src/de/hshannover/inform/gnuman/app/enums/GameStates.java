package de.hshannover.inform.gnuman.app.enums;

/**
 * Represents the states the game supervisor can be in.
 * @author Marc Herschel
 */

public enum GameStates {
    UNDEFINED,
    WAIT_FOR_PLAYER,
    PAUSED,
    RUNNING,
    WAIT_NEXT_LEVEL,
    NO_HIGHSCORE_CUSTOM_MAP,
    NO_HIGHSCORE_YOU_SUCK,
    MADE_HIGHSCORE,
    LIFE_LOST
}
