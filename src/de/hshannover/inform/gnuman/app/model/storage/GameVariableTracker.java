package de.hshannover.inform.gnuman.app.model.storage;

import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.app.enums.Difficulty;
import de.hshannover.inform.gnuman.app.rules.GhostFrighteningRules;
import de.hshannover.inform.gnuman.app.rules.GeneralRules;

/**
 * Tracks game related variables and deals with global game state such as extra lifes, life management, score or frightened mode.
 * @author Marc Herschel
 */

public class GameVariableTracker {
    private int score, lifes, level, pacDotsEaten, ghostsEatenInFrightenedMultiplier, fourGhostsEatenInOneFrighteningCycle, allPacDots, givenExtraLifes, extraLifeScore;
    private boolean frightened, elroyAfterLifeLost, elroy, allFourGhostsEaten;
    private long frightenedExpiresAt, frightenedFlashAt, frightenedDuration;
    private Difficulty difficulty;

    /**
     * Construct a game variable tracker for a session.
     * @param difficulty of the game
     * @param allPacDots on the map
     */
    public GameVariableTracker(Difficulty difficulty, int allPacDots) {
        this.difficulty = difficulty;
        this.allPacDots = allPacDots;
        loadDefaultValues();
    }

    public int getScore() { return score; }
    public int getLifes() { return lifes; }
    public int getLevel() { return level; }
    public int getEatenPacDots() { return pacDotsEaten; }
    public int getAllPacDots() { return allPacDots; }
    public long getFrightenedDuration() { return frightenedDuration; }
    public boolean isFrightenedExpired() { return System.currentTimeMillis() > frightenedExpiresAt; }
    public boolean isFrightenedFlash() { return System.currentTimeMillis() > frightenedFlashAt; }
    public boolean isFrightened() { return frightened; }
    public boolean isElroyAfterLifeLost() { return elroyAfterLifeLost; }
    public boolean isElroy() { return elroy; }
    public Difficulty getDifficulty() { return difficulty; }
    public void enableElroy() { elroy = true; }
    public void removeLife() { lifes--; }
    public void incrementPacDots() { pacDotsEaten++; }
    public void disableFrightening() { frightened = false; allFourGhostsEaten = false; }
    public void adjustDeltaTime(long timeDelta) { frightenedExpiresAt += timeDelta; frightenedFlashAt += timeDelta; }

    public void addToScore(int points) {
        if((GeneralRules.POSSIBLE_EXTRALIVES < 0 || givenExtraLifes < GeneralRules.POSSIBLE_EXTRALIVES) && extraLifeScore <= 0) {
            lifes++; givenExtraLifes++;
            extraLifeScore = GeneralRules.EXTRA_LIVE_EVERY;
            GameLauncher.am().playSound("EXTRA_LIFE"); }
        score += points; extraLifeScore -= points;
    }

    public void enableFrightening() {
        frightened = true;
        ghostsEatenInFrightenedMultiplier = 1;
        frightenedDuration = GhostFrighteningRules.getFrighteningTimeInMs(level, difficulty);
        frightenedExpiresAt = System.currentTimeMillis() + frightenedDuration;
        frightenedFlashAt = frightenedExpiresAt - (frightenedDuration / 2);
    }

    public void extendFrightening() {
        frightenedDuration = GhostFrighteningRules.getFrighteningTimeInMs(level, difficulty);
        frightenedExpiresAt += frightenedDuration;
        frightenedFlashAt += frightenedDuration;
    }

    public void loadDefaultValues() {
        extraLifeScore = GeneralRules.EXTRA_LIVE_EVERY;
        score = givenExtraLifes = 0;
        lifes = 3;
        level = 1;
        sharedReset();
    }

    public void nextLevel() {
        addToScore(fourGhostsEatenInOneFrighteningCycle == 4 ? 12000 : 0);
        level++;
        sharedReset();
    }

    public void eatGhost() {
        addToScore(200*ghostsEatenInFrightenedMultiplier);
        if(ghostsEatenInFrightenedMultiplier < 8) { ghostsEatenInFrightenedMultiplier*=2; }
        if(ghostsEatenInFrightenedMultiplier == 8 && !allFourGhostsEaten) { fourGhostsEatenInOneFrighteningCycle++; allFourGhostsEaten = true; }
    }

    public void resetLevelAfterLifeLoss() {
        ghostsEatenInFrightenedMultiplier = 1;
        frightened = false;
        elroyAfterLifeLost = true;
        elroy = false;
    }

    public boolean canSpawnBonusItem() {
        return (int) (0.35*allPacDots) == pacDotsEaten || (int) (0.85*allPacDots) == pacDotsEaten;
    }

    private void sharedReset() {
        ghostsEatenInFrightenedMultiplier = 1;
        fourGhostsEatenInOneFrighteningCycle = 0;
        pacDotsEaten = 0;
        frightened = elroyAfterLifeLost = elroy = allFourGhostsEaten = false;
    }

}
