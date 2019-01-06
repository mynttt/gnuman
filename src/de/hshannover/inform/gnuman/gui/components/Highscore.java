package de.hshannover.inform.gnuman.gui.components;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.Difficulty;
import de.hshannover.inform.gnuman.app.model.storage.HighscoreScore;

/**
 * Represents the high score. <p>
 * It can load/write to a file, add entries, check if a player made the high score and contains a VBox that represents the high score.
 * @author Marc Herschel
 */

public class Highscore {
    private StateLabel highscoreHeadline;
    private VBox highscoreBox;
    private StateLabel[] rankLabels;
    private HashMap<Difficulty, ArrayList<HighscoreScore>> highscoreData;
    private int maxPlayersToDisplay;

    /**
     * Initialize the high score.
     * @param maxPlayersToDisplay Max. amount of players to track for each difficulty.
     * @param initState Initial state to display.
     */
    public Highscore(int maxPlayersToDisplay, Difficulty initState) {
        this.maxPlayersToDisplay = maxPlayersToDisplay;
        this.highscoreBox = new VBox();
        this.highscoreBox.setId(Constants.HIGHSCORE_BOX_CSS_ID);
        this.highscoreBox.setPrefWidth(Constants.HIGHSCORE_BOX_WIDTH);
        this.highscoreBox.setPrefHeight(Constants.HIGHSCORE_BOX_HEIGHT);
        this.highscoreHeadline = new StateLabel(Constants.HIGHSCORE_HEADLINE_CSS_ID, Difficulty.values().length);
        this.highscoreHeadline.setMaxWidth(Double.MAX_VALUE);
        this.highscoreHeadline.setAlignment(Pos.CENTER);
        this.rankLabels = new StateLabel[maxPlayersToDisplay];
        this.highscoreData = new HashMap<>();

        highscoreBox.getChildren().add(highscoreHeadline);

        for(Difficulty difficulty : Difficulty.values()) {
            this.highscoreHeadline.setState(difficulty, difficulty.toString());
            this.highscoreData.put(difficulty, new ArrayList<>());
        }

        highscoreHeadline.switchState(initState);

        for(int rank = 0; rank < maxPlayersToDisplay; rank++) {
            rankLabels[rank] = new StateLabel(Constants.HIGHSCORE_RESULT_CSS_ID, Difficulty.values().length);
            for(Difficulty difficulty : Difficulty.values()) {
                rankLabels[rank].setState(difficulty, String.format(Constants.HIGHSCORE_RANK_FORMAT, Constants.HIGHSCORE_EMPTY_NAME, Constants.HIGHSCORE_EMPTY_POINTS));
            }
        }

        highscoreBox.getChildren().addAll(rankLabels);
        loadHighscore();
        updateHighscore();

        for(int rank = 0; rank < maxPlayersToDisplay; rank++) {
            rankLabels[rank].switchState(initState);
        }

    }

    /**
     * Let the high score display entries for a given state.
     * @param difficulty to display for.
     */
    public void setState(Difficulty difficulty) {
        if(difficulty.ordinal() >= Difficulty.values().length) { return; }
        highscoreHeadline.switchState(difficulty);
        for(int rank = 0; rank < maxPlayersToDisplay; rank++) {
            rankLabels[rank].switchState(difficulty);
        }
    }

    /**
     * Save the high score data to a file.
     */
    public void saveHighscore() {
        try (FileWriter writer = new FileWriter(new File(Constants.HIGHSCORE_LOCATION), false)) {
            for(int difficulty = 0; difficulty < Difficulty.values().length; difficulty++) {
                for(int rank = 0; rank < highscoreData.get(Difficulty.values()[difficulty]).size(); rank++) {
                    writer.append(Difficulty.values()[difficulty]+",\""+highscoreData.get(Difficulty.values()[difficulty]).get(rank).getName()+"\","+highscoreData.get(Difficulty.values()[difficulty]).get(rank).getScore()+System.lineSeparator());
                }
            }
            writer.flush();
        } catch (Exception e) {
            Log.warning(getClass().getSimpleName(), "Failed to write highscore file. " + e.getMessage());
        }
    }

    /**
     * Add a player to the high score.
     * @param difficulty The difficulty the player played in.
     * @param score The score he made.
     * @param name His name.
     */
    public void addToHighscore(Difficulty difficulty, int score, String name) {
        highscoreData.get(difficulty).add(new HighscoreScore(score, name));
        updateHighscore();
        updateHighscoreLabelsAfterInsert(difficulty);
    }

    /**
     * Have we made the high score with our points?
     * @param difficulty The difficulty the player played in.
     * @param score The score he made.
     * @return if the player made the high score and is allowed to enter his name.
     */
    public boolean madeHighscore(Difficulty difficulty, int score) {
        if(highscoreData.get(difficulty).size() < maxPlayersToDisplay) { return true; }
        for(int rank = 0; rank < highscoreData.get(difficulty).size(); rank++) {
            if(highscoreData.get(difficulty).get(rank).getScore() < score) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the rank of the player in the high score.
     * @param difficulty The difficulty the player played in.
     * @param score The score he made.
     * @return Rank in the given difficulty
     */
    public int getHighscoreRank(Difficulty difficulty, int score) {
        int rank = 1;
        if(highscoreData.get(difficulty).size() == 0) { return rank; }
        for(HighscoreScore s : highscoreData.get(difficulty)) {
            if(s.getScore() <= score) { return rank; }
            rank++;
        }
        return rank;
    }

    /**
     * Parse high score from file.
     */
    private void loadHighscore() {
        try(Scanner scanner = new Scanner(new File(Constants.HIGHSCORE_LOCATION))) {
            while(scanner.hasNextLine()) {
                String[] split = scanner.nextLine().split(",");
                if(split.length != 3) { continue; }
                Difficulty difficulty;
                try {
                    difficulty = Difficulty.valueOf(split[0]);
                } catch (IllegalArgumentException e) {
                    difficulty = null;
                }
                if(difficulty != null) {
                    String name = split[1].substring(1, split[1].length()-1);
                    if(name.trim().length() == 0) { name = Constants.HIGHSCORE_UNKNOWN_PLAYER; }
                    if (name.trim().length() > Constants.HIGHSCORE_NAME_MAX_LENGTH) {
                        name = name.trim().substring(0, Constants.HIGHSCORE_NAME_MAX_LENGTH);
                    }
                    highscoreData.get(difficulty).add(new HighscoreScore(Integer.parseInt(split[2]), name));
                }
            }
        } catch(Exception e) {
            Log.warning(getClass().getSimpleName(), "Failed to load highscore file. " + e.getMessage());
        }
    }

    /**
     * Update the StateLabels.
     */
    private void updateHighscore() {
        prepareHighscore();
        for(Difficulty difficulty : Difficulty.values()) {
            if(highscoreData.get(difficulty).size() != 0) {
                for(int i = 0; i < highscoreData.get(difficulty).size(); i++) {
                    rankLabels[i].setState(difficulty, highscoreData.get(difficulty).get(i).format());
                }
            }
        }
    }

    /**
     * Updating labels after inserting a new score.
     * @param difficulty difficulty to update.
     */
    private void updateHighscoreLabelsAfterInsert(Difficulty difficulty) {
        for(StateLabel l : rankLabels) {
            l.switchState(difficulty);
        }
    }

    /**
     * Filter unnecessary elements and sort the high score.
     */
    private void prepareHighscore() {
        ArrayList<HighscoreScore> toDelete = new ArrayList<>();
        for(Difficulty difficulty : Difficulty.values()) {
            if(highscoreData.get(difficulty).size() != 0) {
                Collections.sort(highscoreData.get(difficulty));
            }
            if(highscoreData.get(difficulty).size() > maxPlayersToDisplay) {
                for(int rank = 0; rank < highscoreData.get(difficulty).size(); rank++) {
                    if(rank >= highscoreData.get(difficulty).size()-1) {
                        toDelete.add(highscoreData.get(difficulty).get(rank));
                    }
                }
                highscoreData.get(difficulty).removeAll(toDelete);
                toDelete.clear();
            }
        }
    }

    /**
     * High score Box
     * @return A VBox containing the actual high score.
     */
    public VBox getBox() {
        return this.highscoreBox;
    }

    /**
     * If the high score contains any useful data.
     * @return true if it contains data.
     */
    public boolean isEmpty() {
        for(Difficulty difficulty : Difficulty.values()) {
            if(highscoreData.get(difficulty).size() != 0) { return false; }
        }
        return true;
    }
}
