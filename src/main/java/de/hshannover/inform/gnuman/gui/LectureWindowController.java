package de.hshannover.inform.gnuman.gui;

import java.util.ArrayList;
import java.util.Scanner;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.app.abstracts.LoopInstruction;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.app.modules.GameLoop;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LectureWindowController extends CommonController {
    @FXML BorderPane rootPane;
    Canvas c;
    GraphicsContext g;
    String[] interjection;
    Stallman RMS;

    /*
     * Congratulations you just found the easter egg :D
     */
    class Stallman {
        boolean isLeave;
        int interjectionIndex, currentChar = -1, tick;
        GameLoop loop;

        public Stallman() {
            loop = new GameLoop(new LoopInstruction() {

                @Override
                public void handle(long now) {
                    if(interjectionIndex == interjection.length) { loop.stop(); render(false); isLeave = true; return; }
                    if(tick++ % (interjectionIndex == 0 ? 6 : 2) == 0) { currentChar++; } else { return; }
                    if(currentChar == interjection[interjectionIndex].length()) { currentChar = -1; interjectionIndex++; return; }
                    render(true);
                }

            } , 500, false);
        }

        void interject() { currentChar = 16; loop.start(); }
        boolean lectureFinished() { if(!isLeave) { stall(); return false; } else { return true; } }
        private void stall() { loop.stop(); isLeave = true; interjectionIndex = interjection.length; render(false); }

        private void render(boolean processing) {
            g.setFill(Color.BLACK);
            g.fillRect(0, 0, Constants.INTERJECTION_WIDTH, Constants.INTERJECTION_HEIGHT);
            g.setFill(Color.WHITE);
            int y = interjectionIndex*Constants.INTERJECTION_FONT_SIZE+Constants.INTERJECTION_FONT_SIZE*2;
            for(int i = 0; i < interjectionIndex; i++) {
                if(i < interjectionIndex) {
                    g.fillText(interjection[i], Constants.INTERJECTION_FONT_SIZE, i*Constants.INTERJECTION_FONT_SIZE+Constants.INTERJECTION_FONT_SIZE*2);
                } else {
                    g.fillText(interjection[interjectionIndex].substring(0, currentChar), Constants.INTERJECTION_FONT_SIZE, y);
                }
            }
            if(processing) { g.fillText(interjection[interjectionIndex].substring(0, currentChar), Constants.INTERJECTION_FONT_SIZE, y); }
        }
    }

    public void someoneCalledItLinuxInsteadOfGnuLinux() {
        rootPane.setPrefWidth(Constants.INTERJECTION_WIDTH);
        rootPane.setPrefHeight(Constants.INTERJECTION_HEIGHT);
        c = new Canvas(Constants.INTERJECTION_WIDTH, Constants.INTERJECTION_HEIGHT);
        rootPane.setCenter(c);
        manager.getScene(UIStates.LECTURE).setOnKeyPressed(e -> onAction());
        manager.getScene(UIStates.LECTURE).setOnMouseClicked(e -> onAction());
        ArrayList<String> tmp = new ArrayList<>();
        try (Scanner s = new Scanner(getClass().getResourceAsStream(Constants.INTERJECTION))) { while(s.hasNextLine()) { tmp.add(s.nextLine()); } }
        interjection = tmp.toArray(new String[tmp.size()]);
        c.getGraphicsContext2D().setFont(Font.loadFont(getClass().getResourceAsStream(Constants.INTERJECTION_FONT), Constants.INTERJECTION_FONT_SIZE));
        g = c.getGraphicsContext2D();
    }

    void interject() {
        RMS = new Stallman();
        RMS.interject();
    }

    private void onAction() {
        if(RMS.lectureFinished()) {
            g.setFill(Color.BLACK);
            g.fillRect(0, 0, Constants.INTERJECTION_WIDTH, Constants.INTERJECTION_HEIGHT);
            manager.switchScene(UIStates.MAIN_MENU);
            RMS = null;
        }
    }
}
