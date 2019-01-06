package de.hshannover.inform.gnuman.app.model.storage;

import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.app.util.Helper;

/**
 * Stores scores of the high score.
 * @author Marc Herschel
 */

public class HighscoreScore implements Comparable<HighscoreScore> {
    private String name;
    private int score;

    public HighscoreScore(int score, String name) {
        String escaped = Helper.onlyAscii(name).replaceAll("(\\\"|\\,)*", "");
        this.score = score; this.name = (escaped.length() == 0) ? Constants.HIGHSCORE_UNKNOWN_PLAYER : escaped;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }
    public String format() {
        return String.format(Constants.HIGHSCORE_RANK_FORMAT, name, score);
    }
    @Override
    public int compareTo(HighscoreScore o) {
        return Integer.compare(o.getScore(), score);
    }
    @Override
    public String toString() {
        return "HighscoreScore [Name: " + name + " Score: " + score + "]";
    }
}
