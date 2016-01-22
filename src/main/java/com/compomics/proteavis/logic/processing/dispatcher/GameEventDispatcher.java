package com.compomics.proteavis.logic.processing.dispatcher;

import com.compomics.proteavis.view.game.panels.FireworksPanel;
import com.compomics.proteavis.view.game.panels.GameEngine;
import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author compomics
 */
public class GameEventDispatcher implements KeyEventDispatcher {

    private final ExecutorService exec = Executors.newCachedThreadPool();
    private GameEngine gamePanel;
    private FireworksPanel fireworksPanel;

    public GameEventDispatcher(GameEngine gamePanel, FireworksPanel fireworksPanel) {
        this.gamePanel = gamePanel;
        this.fireworksPanel = fireworksPanel;
    }

    public void setGameEnginePanel(GameEngine gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            //prefilter out anything else but letters
            if (e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z) {
                //check if the correct key was pressed, if not, take off from the score or break the combo?
                exec.submit((new Runnable() {
                    @Override
                    public void run() {
                        LinkedList<Integer> y = gamePanel.getYList();
                        LinkedList<String> labels = gamePanel.getLabels();
                        for (int i = 0; i < gamePanel.getAminoAcidList().size(); i++) {
                            int explosionType = 0;
                            Integer yValue = y.get(i);
                            String label = labels.get(i);
                            if (label.length() == 1) {
                                boolean fade = false;
                                if (Character.toUpperCase(e.getKeyChar()) == Character.toUpperCase(gamePanel.getAminoAcidList().get(i).getCharacter())
                                        && yValue != Integer.MAX_VALUE) {
                                    if (!gamePanel.isZenMode()) {
                                        //set the label on this one to something exciting, but make it stay in place for one cycle
                                        if (yValue < gamePanel.getMinimalDistance()) {
                                            labels.set(i, "TOO SOON");
                                            fade = true;
                                            gamePanel.resetCombo();
                                            gamePanel.increaseScore(-10);
                                        } else if (yValue > gamePanel.getMinimalDistance() && yValue < gamePanel.getMaximalDistance()) {
                                            //find the distance from the center of the target area
                                            double deviationFromIdeal = Math.abs((double) yValue / (double) (gamePanel.getHeight() - ((gamePanel.getHeight() - gamePanel.getMinimalDistance()) / 2)));
                                            if (deviationFromIdeal > 0.95) {
                                                labels.set(i, "PERFECT");
                                                gamePanel.increaseScore(100);
                                                explosionType = 1;
                                            } else if (deviationFromIdeal > 0.75) {
                                                labels.set(i, "EXCELLENT");
                                                gamePanel.increaseScore(75);
                                                explosionType = 1;
                                            } else {
                                                labels.set(i, "GOOD");
                                                gamePanel.increaseScore(50);
                                            }
                                            fade = true;
                                            gamePanel.setCombo(gamePanel.getCombo() + 1);
                                            gamePanel.increaseScore((int) (deviationFromIdeal * 100));
                                        } else if (yValue > gamePanel.getMaximalDistance()) {
                                            gamePanel.resetCombo();
                                            gamePanel.increaseScore(-25);
                                            labels.set(i, "OOPS");
                                            fade = true;
                                        }

                                        y.set(i, yValue - 1);
                                        //if the score reaches a certain threshold, level up!
                                        int currentLevel = gamePanel.getLevel();
                                        if (((gamePanel.getScore() - (currentLevel * 5000)) / (currentLevel * 5000)) > gamePanel.getLevel()) {
                                            gamePanel.increaseLevel(1);
                                        }
                                        break;
                                    }
                                    if (fade) {
                                        exec.submit(new LabelFader(i, gamePanel, explosionType));
                                    }
                                }
                            }
                        }
                    }
                }));
            }
        }
        return false;
    }

    private class LabelFader implements Runnable {

        private final int index;
        private final GameEngine panel;
        private final int explosion;

        private LabelFader(int index, GameEngine panel, int explosion) {
            this.index = index;
            this.panel = panel;
            this.explosion = explosion;
        }

        @Override
        public void run() {
            try {
                if (explosion > 0) {
                    int xValue = panel.getXList().get(index);
                    int yValue = panel.getYList().get(index);
                    switch (explosion) {
                        case 1:
                            fireworksPanel.explode(xValue, yValue);
                            break;
                        case 2:
                        //coming soon
                    }

                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(GameEventDispatcher.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                panel.getLabels().set(index, "");
                panel.getYList().set(index, Integer.MAX_VALUE);
            }
        }

    }

}
