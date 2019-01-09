package de.hshannover.inform.gnuman.gui.components;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * A news like TextTicker that lets Strings float from right to left. This Ticker allows for dynamic usage of Strings.<br>
 * You must set an EventHandler that loads the next String once the Animation has ended.<p>
 *
 * Example for the EventHandler: (textTicker = this object) <code> e -> textTicker.updateText(NEXT_STRING_HERE);</code><p>
 *
 * After you created the object you must use initiate(int factor) to set a constant factor for all String lengths. The factor must be > 0.<br>
 * The larger the factor, the slower the Animation. You will need to experiment with the factor. 2 is really fast and 50 kind of slow. Just take something that you like.
 *
 * @author Marc Herschel
 */

public class TextTicker extends Pane {
    private Timeline timeline;
    private Text textObject;
    private double width, height;
    private int speed;
    private boolean initiated = false;

    /**
     * Creates a new TextTicker. The speed = 0; setText("") is an evil hack that stops the Timeline from resetting itself in the middle on the<br>
     * first run. It's doing a dummy run that is so fast that it takes no time at all. If anyone knows a way how to not use this hack please tell me!!!
     * @param width Width of the Ticker
     * @param height Height of the Ticker
     * @param onFinished What to do one the run is finished (usually updating the text with updateText())
     * @param cssId If you want to style the text with CSS.
     */
    public TextTicker(double width, double height, EventHandler<ActionEvent> onFinished, String cssId) {
        textObject = new Text();
        textObject.setStrokeWidth(0);
        textObject.setId(cssId);
        textObject.applyCss();
        timeline = new Timeline();
        timeline.setAutoReverse(false);
        getChildren().add(textObject);
        this.width = width;
        this.height = height;
        this.speed = 0;
        textObject.setText("");
        setPrefSize(width, height);
        setClip(new Rectangle(width, height));
        setAnimationVariables();
        timeline.onFinishedProperty().set(onFinished);
    }

    /**
     * Creates a new TextTicker. The speed = 1; setText("") is an evil hack that stops the Timeline from resetting itself in the middle on the<br>
     * first run. It's doing a dummy run that is so fast that it takes no time at all.
     * @param width Width of the Ticker
     * @param height Height of the Ticker
     * @param onFinished What to do one the run is finished (usually updating the text with updateText())
     */
    public TextTicker(double width, double height, EventHandler<ActionEvent> onFinished) {
        this(width, height, onFinished, null);
    }

    /**
     * Update the KeyFrame for a new String.
     */
    private void setAnimationVariables() {
        timeline.getKeyFrames().clear();
        KeyValue value = new KeyValue(textObject.layoutXProperty(), -getTextWidth());
        KeyFrame frame = new KeyFrame(Duration.millis((speed == 0) ? 1 : (getTextWidth() * speed)), value);
        timeline.getKeyFrames().add(frame);
        textObject.relocate(width, (height / 2) - (getTextHeight() / 2));
    }

    /**
     * Compute width of the string.
     * @return width in pixel.
     */
    private double getTextWidth() {
        return textObject.getLayoutBounds().getWidth();
    }

    /**
     * Compute height of the string.
     * @return height in pixel.
     */
    private double getTextHeight() {
        return textObject.getLayoutBounds().getHeight();
    }

    /**
     * Update the text that the ticker displays.
     * @param text to set
     */
    public void updateText(String text) {
        timeline.stop();
        textObject.setText(text);
        setAnimationVariables();
        timeline.play();
    }

    /**
     * Start/Resume the ticker.
     */
    public void play() {
        if (timeline == null || !initiated ) { return; }
        timeline.play();
    }

    /**
     * Pause the ticker.
     */
    public void pause() {
        if (timeline == null || !initiated) { return; }
        timeline.pause();
    }

    /**
     * Stop and reset the ticker.<br>
     */
    public void pauseAndResetPosition() {
        if (timeline == null || !initiated) { return; }
        timeline.pause();
        timeline.jumpTo(Duration.ZERO);
    }
    
    /**
     * @return true if currently not actively ticking a text.
     */
    public boolean isPaused() {
        if(timeline == null || !initiated) { return true; }
        return timeline.getStatus() == Status.PAUSED;
    }

    /**
     * You must initiate the ticker with a speed after creation.
     * @param speedFactor A constant factor depending on the width of the text.
     */
    public void initiate(int speedFactor) {
        if(speedFactor <= 0) { throw new IllegalArgumentException("TextTicker: Speedfactor must be > 0!"); }
    //This is the dummy run that boots the Ticker and skips the first run that ends in the middle.
        timeline.play();
    //Set the speed factor to something normal, after this the text that will actually be displayed is loaded by the EventHandler.
        this.speed = speedFactor;
        initiated = true;
    }
}
