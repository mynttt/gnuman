package de.hshannover.inform.gnuman.app;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import java.util.LinkedList;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.Difficulty;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.GameStates;
import de.hshannover.inform.gnuman.app.enums.gameobjects.ObjectTypes;
import de.hshannover.inform.gnuman.app.enums.gameobjects.OtherObjects;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.interfaces.TransientGameTask;
import de.hshannover.inform.gnuman.app.model.storage.GameVariableTracker;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.model.coordination.TimedTasks;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.GameSettings;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import de.hshannover.inform.gnuman.app.modules.Entities;
import de.hshannover.inform.gnuman.app.modules.Renderer;
import de.hshannover.inform.gnuman.app.modules.Textures;
import de.hshannover.inform.gnuman.app.modules.TileMap;
import de.hshannover.inform.gnuman.app.rules.BonusRules;
import de.hshannover.inform.gnuman.app.util.Helper;

/**
 * Provides an interface to the supervisor for the different game components.
 * @author Marc Herschel
 */

public class GameInstance {
    private DynamicVariables dyn;
    private GraphicsContext gc;
    private TileMap map;
    private Entities entities;
    private Renderer renderer;
    private Textures textures;
    private GameVariableTracker tracker;
    private KeyCode[] keyBoardLayout;
    private GameEnded gameEndState;
    private LinkedList<TransientGameTask> tasks;
    private TimedTasks timedTasks;
    private boolean isFinished, trackHighscore, playStartingMusic, frightenFromGhostEatingItem;
    private long lastDotEaten, bonusItemLeft;

    /**
     * How the game ended.
     * @author Marc Herschel
     */
    public enum GameEnded {
        GAME_LOST, NEXT_LEVEL, LIFE_LOST, NONE
    }

    /**
     * Create a new game instance.
     * @param gc context to draw on.
     * @param dyn Dynamic user options depending values.
     */
    public GameInstance(GraphicsContext gc, DynamicVariables dyn) {
        if(gc == null) { Log.critical(getClass().getSimpleName(), "Critical: Set valid GraphicsContext!"); System.exit(1); }
        map = new TileMap(dyn);
        entities = new Entities(map, dyn);
        timedTasks = new TimedTasks();
        tasks = new LinkedList<>();
        this.gc = gc;
        this.dyn = dyn;
        Log.info(getClass().getSimpleName(), "Game instance created.");
    }

    /**
     * Load a map.
     * @param mapData to load.
     * @param difficulty of the game.
     * @param options to take into consideration.
     */
    public void startGame(MapData mapData, Difficulty difficulty, GameSettings options) {
        playStartingMusic = true;
        gameEndState = GameEnded.NONE;
        isFinished = false;
        trackHighscore = mapData.trackHighscore();
        keyBoardLayout = (options.isUseArrows()) ? new KeyCode[] {KeyCode.UP, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.LEFT} : new KeyCode[] {KeyCode.W, KeyCode.S, KeyCode.D, KeyCode.A};
        textures = new Textures(dyn);
        textures.loadBaseTextures();
        textures.loadTexture(ObjectTypes.OTHER, OtherObjects.MAZE, Helper.scale(mapData.getBackground(),  mapData.getWidthInBlocks()*options.getBlockDimension(), mapData.getHeightInBlocks()*options.getBlockDimension(), true, false));
        textures.loadTexture(ObjectTypes.OTHER, OtherObjects.MAZE_FLICKER, Helper.scale(mapData.getBackgroundFlash(),  mapData.getWidthInBlocks()*options.getBlockDimension(), mapData.getHeightInBlocks()*options.getBlockDimension(), true, false));
        map.loadMap(mapData);
        tracker = new GameVariableTracker(difficulty, map.allPacDots());
        entities.createEntities(mapData, tracker);
        renderer = new Renderer(gc, textures, dyn, options.dynCamera(), entities.getPlayer());
        Log.info(getClass().getSimpleName(), "Instance loaded from Map! Game ready to go!");
    }

    /**
     * Unload current map if loaded.
     */
    public void shutdownGame() {
        keyBoardLayout = null;
        isFinished = trackHighscore = false;
        tracker = null;
        renderer = null;
        textures.unloadAll();
        textures = null;
        map.unloadMap();
        entities.unloadEntities();
        tasks.clear();
        timedTasks.clearTasks();
        Log.info(getClass().getSimpleName(), "Map unloaded successfully. Instance stopped.");
    }

    /**
     * Reset the game to its initial map state.
     */
    public void resetGame() {
        playStartingMusic = true;
        tracker.loadDefaultValues();
        map.resetMap(false);
        sharedReset();
    }

    /**
     * Prepare the next level.
     */
    public void prepareNextLevel() {
        tracker.nextLevel();
        map.resetMap(false);
        sharedReset();
        evaluateGameMusic();
    }

    /**
     * Prepare level if life has lost.
     */
    public void prepareLevelAfterLifeLoss() {
        tracker.resetLevelAfterLifeLoss();
        map.resetMap(true);
        sharedReset();
        evaluateGameMusic();
        if(map.bonusItemSpawned()) {
            timedTasks.createTask("bonus-item", bonusItemLeft);
            createItemTask();
        }
    }

    /**
     * Call the renderer.
     */
    public void render() {
        renderer.render(map, entities, tracker);
    }

    /**
     * Render cases that are not the running game on the canvas.
     * @param state to render.
     * @param renderEntitesAt show entities once this condition is met.
     */
    public void renderSpecial(GameStates state, long renderEntitesAt) {
        switch(state) {
            case WAIT_FOR_PLAYER:
                if(playStartingMusic && !GameLauncher.am().isMusicPlaying("STARTING_MUSIC")) { GameLauncher.am().playMusic("STARTING_MUSIC"); playStartingMusic = false; }
                renderer.renderWaitForPlayer(map, entities, renderEntitesAt);
                break;
            case WAIT_NEXT_LEVEL:
                renderer.renderWaitForNextLevel(map, entities, tracker.getLevel()+1);
                break;
            case LIFE_LOST:
                renderer.renderLifeLost(map, entities);
                break;
            case NO_HIGHSCORE_YOU_SUCK:
            case NO_HIGHSCORE_CUSTOM_MAP:
                renderer.failedHighscore(state, tracker);
                break;
            case MADE_HIGHSCORE:
                renderer.madeHighscore(tracker);
                break;
            default:
                System.out.println("UNDEFINDED STATE: " + state);
                return;
        }
    }

    /**
     * Update game on tick.
     */
    public void updateGame() {
        if(frightenFromGhostEatingItem) { evaluateFrighten(); frightenFromGhostEatingItem = false; }
        tasks.removeIf(TransientGameTask::isFinished);
        entities.updatePlayer();
    //Player pick ups
        StaticObjects s = map.checkItemIntersection(entities.getPlayer());
        if(s != null) {
            switch(s) {
            case FOOD:
                if(System.currentTimeMillis() > lastDotEaten && !GameLauncher.am().isMusicPlaying("EATING_DOTS") && !tracker.isFrightened()) {
                    GameLauncher.am().beginWithDotsSequence();
                }
                tracker.incrementPacDots();
                tracker.addToScore(10);
                lastDotEaten = System.currentTimeMillis() + 500;
                break;
            case POWERUP:
                evaluateFrighten();
                tracker.addToScore(50);
                GameLauncher.am().playSound("EATING_POWERUP");
                break;
            default: break;
        }
        }
        entities.updateGhosts();
    //Won, no times left
        if(map.numberOfItems() == 0) {
            GameLauncher.am().stopMusic("EATING_DOTS");
            isFinished = true;
            gameEndState = GameEnded.NEXT_LEVEL;
            Log.info(getClass().getSimpleName(), "Level won: " + tracker.getLevel());
        }
    //We meet a ghost, do we die? Does the game end?
        if(entities.checkIfCollisionKillsPlayer()) {
            GameLauncher.am().stopMusic("EATING_DOTS");
            isFinished = true;
            tracker.removeLife();
            gameEndState = (tracker.getLifes() > 0) ? GameEnded.LIFE_LOST : GameEnded.GAME_LOST;
            bonusItemLeft = timedTasks.getTimeLeftFor("bonus-item");
            Log.info(getClass().getSimpleName(), "Level lost: " + tracker.getLevel() + " Reason: " + gameEndState);
        }
     //Clear audio if not used
        if(System.currentTimeMillis() > lastDotEaten || tracker.isFrightened()) { GameLauncher.am().stopMusic("EATING_DOTS"); }
     //Bonus Item
        if(tracker.canSpawnBonusItem() && map.bonusItemSpawnable()) {
            map.spawnBonusItem();
            timedTasks.createOrOverrideTask("bonus-item", (long) (9750*tracker.getDifficulty().getTimingScale()));
            createItemTask();
        }
    }

    /**
     * Update a players direction input.
     * @param code key code to pass down
     */
    public void updatePlayerInputBuffer(KeyCode code) {
        entities.updatePlayerInputBuffer(evaluateKeyDirection(code));
    }

    /**
     * Update timers if the game has been paused.
     * @param pausedAt when the game was last paused
     */
    public void adjustDeltaTime(long pausedAt) {
        long toAdjust = System.currentTimeMillis() - pausedAt;
        tracker.adjustDeltaTime(toAdjust);
        timedTasks.adjustDeltaTimeForAllTasks(toAdjust);
        entities.adjustDeltaTime(toAdjust);
        lastDotEaten = 0;
    }

    /**
     * Evaluate current game music.
     */
    public void evaluateGameMusic() { GameLauncher.am().decideMusic(tracker.isElroy(), tracker.isFrightened()); }

    /**
     * If the game is finished.
     * @return true if finished.
     */
    public boolean isFinished() {
        return isFinished;
    }

    /**
     * @return how the game ended.
     */
    public GameEnded endedState() {
        return gameEndState;
    }

    /**
     * Do we track the high score?
     * @return true if yes.
     */
    public boolean trackHighscore() {
        return trackHighscore;
    }

    /**
     * Difficulty of the current session.
     * @return difficulty of the session.
     */
    public Difficulty getDifficulty() {
        return tracker.getDifficulty();
    }

    /**
     * Current score.
     * @return score of the session.
     */
    public int getScore() {
        return tracker.getScore();
    }

    /**
     * Evaluate a pressed key and return the responding direction.
     * @param code KeyCode to look up for.
     * @return null if the direction is not mapped or the direction.
     */
    private Directions evaluateKeyDirection(KeyCode code) {
        if(code == keyBoardLayout[0]) { return Directions.UP; }
        else if (code == keyBoardLayout[1]) { return Directions.DOWN; }
        else if (code == keyBoardLayout[2]) { return Directions.RIGHT; }
        else if (code == keyBoardLayout[3]) { return Directions.LEFT; }
        return null;
    }

    /**
     * Create the special task for the item.
     */
    private void createItemTask() {
        tasks.add(() -> {
            if(entities.getPlayer().intersects(map.getBonusItem().getBounds())) {
                map.removeBonusItem();
                GameLauncher.am().playSound("BONUS");
                tracker.addToScore(BonusRules.evaluate(tracker.getLevel()));
                timedTasks.cancelTask("bonus-item");
                return true;
            }
            //If ghosts eat the items they will turn frightened, in this case we need to set it with a flag to avoid the ConcurrentModificationException
            for(AbstractGhost g : entities.getGhosts()) {
                if(g.intersects(map.getBonusItem().getBounds())) {
                    GameLauncher.am().playSound("GHOST_ATE_ITEM");
                    map.removeBonusItem();
                    frightenFromGhostEatingItem = true;
                    timedTasks.cancelTask("bonus-item");
                    return true;
                }
            }
            if(timedTasks.isFinished("bonus-item")) {
                map.removeBonusItem();
                return true;
            }
            return false;
        });
    }

    /**
     * Frighten check.
     */
    private void evaluateFrighten() {
        if(!tracker.isFrightened()) {
            tracker.enableFrightening();
            evaluateGameMusic();
            timedTasks.createOrOverrideTask("frightentrigger", tracker.getFrightenedDuration());
            tasks.add(() -> {
                if(timedTasks.isFinished("frightentrigger")) {
                    entities.removeFrightening();
                    tracker.disableFrightening();
                    evaluateGameMusic();
                    renderer.resetFrighteningFlashState();
                    lastDotEaten = 0;
                    return true;
                }
                return false;
            });
        } else {
            tracker.extendFrightening();
            timedTasks.createOrExtendTask("frightentrigger", tracker.getFrightenedDuration());
            if(!tracker.isFrightenedFlash()) { renderer.resetFrighteningFlashState(); }
        }
        entities.applyFrightening();
    }

    /**
     * Shared calls for reset.
     */
    private void sharedReset() {
        tasks.clear();
        timedTasks.clearTasks();
        entities.resetEntities();
        isFinished = frightenFromGhostEatingItem = false;
        gameEndState = GameEnded.NONE;
    }
}
