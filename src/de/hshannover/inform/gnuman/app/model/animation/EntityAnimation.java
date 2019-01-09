package de.hshannover.inform.gnuman.app.model.animation;

import java.util.HashMap;
import java.util.LinkedList;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.model.Player;
import javafx.scene.image.Image;

/**
 * Handle animations and states for entities.
 * @author Marc Herschel
 */

public class EntityAnimation {
    private HashMap<EntityObjects, HashMap<Directions, AnimatedSprite>> lookup;
    private HashMap<FrightenedStates, AnimatedSprite> lookupFrightened;
    private HashMap<Directions, Image> lookupDead;
    private LinkedList<AnimatedSprite> toUpdate;
    private AnimationToggle frightenedSwitchAnimation;
    private int updateEvery, internalTicks;

    public enum FrightenedStates {
        BLUE, WHITE
    }

    /**
     * Construct a new entity animation.
     * @param fps to target.
     */
    public EntityAnimation(int fps) {
        this.updateEvery = fps/6;
        this.frightenedSwitchAnimation = new AnimationToggle((int) (fps*0.5));
        this.toUpdate = new LinkedList<>();
        this.lookupDead = new HashMap<>();
        this.lookupFrightened = new HashMap<>();
        this.lookup = new HashMap<>();
    }

    /**
     * Update entire animation state
     * @param isFrightened true if we want to update the frightening tracker as well
     */
    public void update(boolean isFrightened) {
        if(isFrightened) { frightenedSwitchAnimation.update(); }
        if(++internalTicks > updateEvery) {
            internalTicks = 0;
            toUpdate.forEach(AnimatedSprite::nextFrame);
        }
    }

    /**
     * Register a normal entity animation.
     * @param entity to register
     * @param direction to register
     * @param frames of animation
     */
    public void registerAnimation(EntityObjects entity, Directions direction, Image[] frames) {
        lookup.putIfAbsent(entity, new HashMap<>());
        if(!lookup.get(entity).containsKey(direction)) {
            AnimatedSprite s = new AnimatedSprite(frames);
            toUpdate.add(s);
            lookup.get(entity).put(direction, s);
            Log.info(getClass().getSimpleName(), "Loaded Entity Animation: " + entity + " -> " + direction + ".");
        } else {
            Log.warning(getClass().getSimpleName(), "Entity Animation: " + entity + " -> " + direction + " already exists!");
        }
        if(direction == Directions.UP && entity != EntityObjects.PLAYER) { registerAnimation(entity, Directions.NONE, frames); }
        if(direction == Directions.DOWN && entity == EntityObjects.PLAYER)  { registerAnimation(entity, Directions.NONE, frames); }
    }

    /**
     * Register a frightened animation for the ghosts.
     * @param state of frightening for flicker
     * @param frames of animation
     */
    public void registerFrightenedAnimation(FrightenedStates state, Image[] frames) {
        AnimatedSprite s = new AnimatedSprite(frames);
        toUpdate.add(s);
        lookupFrightened.put(state, s);
    }

    /**
     * Register the sprite where the ghosts look dead
     * @param d directions they go
     * @param frame of animation
     */
    public void registerDeadAnimation(Directions d, Image frame) {
        if(d == Directions.UP) { lookupDead.put(Directions.NONE, frame); }
        lookupDead.put(d, frame);
    }

    /**
     * Normal movement
     * @param ghost to check
     * @return image fitting the direction.
     */
    public Image forGhost(AbstractGhost ghost) {
        return lookup.get(ghost.getEntityType()).get(ghost.getDirection()).currentFrame();
    }

    /**
     * Frightened movement
     * @param ghost to check
     * @return image fitting the direction.
     */
    public Image forGhostFrightened(AbstractGhost ghost) {
        if(ghost.isFrighteningFlashOver()) {
            return lookupFrightened.get(frightenedSwitchAnimation.isDisplay() ? FrightenedStates.WHITE : FrightenedStates.BLUE).currentFrame();
        }
        return lookupFrightened.get(FrightenedStates.BLUE).currentFrame();
    }

    /**
     * Dead movement
     * @param ghost to check
     * @return image fitting the direction.
     */
    public Image forGhostDead(AbstractGhost ghost) {
        return lookupDead.get(ghost.getDirection());
    }

    /**
     * Movement image for player
     * @param player to check
     * @return image fitting the direction
     */
    public Image forPlayer(Player player) {
        if(player.isMoving()) {
            return lookup.get(EntityObjects.PLAYER).get(player.getDirection()).currentFrame();
        } else {
            return lookup.get(EntityObjects.PLAYER).get(player.getDirection()).firstFrame();
        }
    }

    /**
     * @param object to lookup for
     * @param direction to lookup for
     * @return image for animations outside of the game.
     */
    public Image forAnimationWithoutObject(EntityObjects object, Directions direction) {
        return lookup.get(object).get(direction).currentFrame();
    }

    /**
     * Reset the frightening flash state so the frightening animation always starts at the same point.
     */
    public void resetFrighteningFlashState() { frightenedSwitchAnimation.reset(); }
}
