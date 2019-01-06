package de.hshannover.inform.gnuman.app.model.animation;

import javafx.scene.image.Image;

/**
 * Wrapper for animated sprites.
 * @author Marc Herschel
 */

public class AnimatedSprite {
    private int frame;
    private Image[] frames;

    /**
     * Create an animated sprite.
     * @param frames of the sprite.
     */
    public AnimatedSprite(Image[] frames) {
        if(frames == null ) { throw new IllegalArgumentException("Frames must not be null!"); }
        for(Image frame : frames) { if(frame == null ) { throw new IllegalArgumentException("One frame is not loaded properly!!"); } }
        this.frames = frames;
    }

    /**
     * Set next frame, start again if end is reached.
     */
    public void nextFrame() { frame = ++frame%frames.length; }

    /**
     * @return current frame of sprite.
     */
    public Image currentFrame() { return frames[frame]; }

    /**
     * @return the first frame of the animation.
     */
    public Image firstFrame() { return frames[0]; }

    /**
     * Reset to initial index.
     */
    public void reset() { frame = 0; }


}
