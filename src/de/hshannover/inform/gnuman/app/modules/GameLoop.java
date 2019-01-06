package de.hshannover.inform.gnuman.app.modules;

import java.util.ConcurrentModificationException;
import javafx.animation.AnimationTimer;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.abstracts.LoopInstruction;

/**
 * Game loop that runs on a fixed time interval.<p>
 * Orientated at http://svanimpe.be/blog/game-loops<br>
 * FPS measurement taken from StackOverflow. https://stackoverflow.com/a/28287949
 * @author Marc Herschel
 */

public class GameLoop extends AnimationTimer {
    private LoopInstruction instructions;
    private float loopTimeStep;
    private boolean recordFps, isRunning;

    //Loop
    private float accumulatedTime = 0, maximumStep = Float.MAX_VALUE;
    private long previousTime = 0;

    //FPS
    private long[] frameTimes = new long[100];
    private boolean arrayFilled = false ;
    private int frameRate = Integer.MIN_VALUE, frameTimeIndex = 0 ;

    /**
     * Construct a game loop.
     * @param instructions Instructions to execute on tick.
     * @param fps to target for.
     * @param recordFps if we want to record the fps.
     */
    public GameLoop(LoopInstruction instructions, int fps, boolean recordFps) {
        this.instructions = instructions;
        this.loopTimeStep = 1.0f/fps;
        this.recordFps = recordFps;
        Log.info(getClass().getSimpleName(), "Framerate set to " + fps + " fps.");
    }

    /**
     *
     * @param fps of the loop
     * @param recordFps true if record fps
     * @throws ConcurrentModificationException if the loop is running.
     */
    public void changeSettings(int fps, boolean recordFps) {
        if(isRunning) { throw new ConcurrentModificationException("Cannot modify a running game loop!"); }
        this.loopTimeStep = 1.0f/fps;
        this.recordFps = recordFps;
    }

    /**
     * Returns the frame rate.
     * @return Integer.MIN_VALUE if reporting is disabled or the frame rate else frame rate.
     */
    public int getFramerate() {
        return frameRate;
    }

    @Override
    public void handle(long currentTime)
    {
        if (previousTime == 0) {
            previousTime = currentTime;
            return;
        }

        float secondsElapsed = (currentTime - previousTime) / 1e9f;
        float secondsElapsedCapped = Math.min(secondsElapsed, maximumStep);
        accumulatedTime += secondsElapsedCapped;
        previousTime = currentTime;

        while (accumulatedTime >= loopTimeStep) {
            accumulatedTime -= loopTimeStep;
            instructions.handle(currentTime);

          //FPS measurement
            if(recordFps) {
                frameTimes[frameTimeIndex] = currentTime ;
                frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length ;
                if (frameTimeIndex == 0) {
                    arrayFilled = true ;
                }
                if (arrayFilled) {
                    frameRate = (int) (1_000_000_000.0 / ((currentTime - frameTimes[frameTimeIndex]) / frameTimes.length)) ;
                }
            }
        }
    }

    @Override
    public void start() {
        isRunning = true;
        super.start();
    }

    @Override
    public void stop()
    {
        isRunning = false;
        previousTime = 0;
        accumulatedTime = 0;
        super.stop();
    }
}
