package com.compomics.proteavis.view.game.panels.impl;

import com.compomics.proteavis.logic.util.AminoAcidColorFactory;
import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.model.SoundAlphabet;
import com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType;
import com.compomics.proteavis.model.enums.AminoAcidProperty;
import com.compomics.proteavis.view.Visualiser;
import com.compomics.proteavis.view.game.panels.FireworksPanel;
import com.compomics.proteavis.view.game.panels.GameEngine;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

public class ZenVisualisationPanel extends JPanel implements GameEngine {

    private static final long serialVersionUID = 1L;
    LinkedList<Integer> x = new LinkedList<>();
    LinkedList<Integer> y = new LinkedList<>();
    LinkedList<String> labels = new LinkedList<>();
    private static int frameCap = 30;

    int score = 0;
    int currentScoreCap = 0;
    int level = 1;
    int combo = 0;
    int maxCombo = 0;
    boolean alive = true;
    private LinkedList<AminoAcidResult> aminoAcids = new LinkedList<>();

    private SoundAlphabetType type;
    private boolean done = false;
    private Thread gameThread;
    private boolean paused;
    private int height = 0;
    private Random random = new Random();
    private HashMap<Character, Integer> columnMapping;
    private int minimalDistance;
    private Visualiser visualiser;
    private AminoAcidColorFactory colorFactory = AminoAcidColorFactory.getInstance();
    private FireworksPanel fireworksPanel;
    private int difficulty;
    private int maximalDistance;
    private int targetDistance;
    private Boolean zenMode = false;
    private boolean randomizeColors;
    private boolean randomizePosition;

    public ZenVisualisationPanel() {
        super();
    }

    public void setZenMode(Boolean zenMode) {
        this.zenMode = zenMode;
        if (zenMode) {
            targetDistance = getHeight() / 2;
        }
    }

    public void setFireWorksPanel(FireworksPanel fireworksPanel) {
        this.fireworksPanel = fireworksPanel;
    }

    public void setVisualiser(Visualiser visualiser) {
        this.visualiser = visualiser;
    }

    public void setSoundAlphabetType(SoundAlphabetType type) {
        this.type = type;
    }

    public void reset() {
        y.clear();
        x.clear();
        labels.clear();
        aminoAcids.clear();
    }

    public void pause() {
        paused = true;
    }

    public void unpause() {
        paused = false;
    }


    private double calculateColumnSize() {
        int width = getWidth() - 4;
        int colums = AminoAcidProperty.values().length;
        double columnWidth = (int) width / colums;
        return columnWidth;
    }

    private HashMap<Character, Integer> getColumnMapping() {
        columnMapping = new HashMap<>();
        double columnSize = calculateColumnSize();
        List<List<AminoAcidProperty>> aminoAcidToneLadder = SoundAlphabet.getInstance().getAminoAcidToneLadder(type);
        int columnCenter = (int) columnSize / 2;
        for (List<AminoAcidProperty> aapList : aminoAcidToneLadder) {
            for (AminoAcidProperty aap : aapList) {
                columnMapping.put(aap.getSingleLetter(), columnCenter);
                columnCenter += columnSize;
            }
        }
        columnMapping.put('!', Integer.MAX_VALUE);
        return columnMapping;
    }

    public void addAminoAcid(AminoAcidResult result) {
        if (columnMapping == null || columnMapping.isEmpty()) {
            columnMapping = getColumnMapping();
        }
        if (result.getCharacter() == '?') {
            try {
                //the ? note signifies the end of the stream...After this, no new letters should be added but they all should still come down?
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ZenVisualisationPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            alive = false;
        } else if (zenMode && result.getCharacter() != '!' && result.getCharacter() != '?') {
            int x = getWidth() / 2;
            int y = getHeight() / 2;
            if (randomizePosition) {
                x = (int) (Math.random() * getWidth());
                y = (int) (Math.random() * getHeight());
            }
            fireworksPanel.explodeAminoAcid(result, type, x, y, randomizeColors
            );
        } else if (result.getCharacter() != '?') {
                aminoAcids.addLast(result);
                labels.add("" + result.getCharacter());
                int startingX = columnMapping.get(result.getCharacter());
                x.add(startingX);
                int startingY = 0;
                y.add(startingY);
        }
    }

    public int getMinimalDistance() {
        return minimalDistance;
    }

    public int getMaximalDistance() {
        return maximalDistance;
    }

    public int getTargetDistance() {
        return targetDistance;
    }

    public boolean isZenMode() {
        return zenMode;
    }

    private void paintTargetBar(Graphics g, int height) {
        int y = getHeight();
        if (!zenMode) {
            g.setColor(Color.red.brighter().brighter().brighter());
            g.drawRect(0, y - (4 * height), getWidth() - 1, y - (3 * height));
            g.setColor(Color.green.brighter().brighter().brighter());
            g.drawRect(0, y - (3 * height), getWidth() - 1, y - 2 * height);
            g.setColor(Color.red.brighter().brighter().brighter());
            g.drawRect(0, y - (2 * height), getWidth() - 1, y + 1);
        } else {
            y = y / 2;
        }
        minimalDistance = y - 4 * height;
        maximalDistance = y - (2 * height);
        targetDistance = (int) ((maximalDistance - minimalDistance) / 2);

    }

    private void paintEndScreen(Graphics g) {
        if (!zenMode) {
            //   fireworksPanel.explodeGiant(getSize().width / 2, getSize().height / 2);
            g.setColor(Color.GREEN);
            g.setFont(new Font("   ", Font.BOLD, 50));
            g.drawString("Congratulations!", 0, getSize().height / 2);
            g.setFont(new Font("   ", Font.BOLD, 30));
            g.drawString("Score   " + score, 0, (getSize().height / 2) + 35);
            g.drawString("Level   " + level, 0, (getSize().height / 2) + 70);
            g.drawString("Difficulty   " + difficulty, 0, (getSize().height / 2) + 105);
            g.drawString("Highest Combo   " + maxCombo, 0, (getSize().height / 2) + 140);
        }
    }

    private void paintNormalScreen(Graphics g) {
        g.setFont(new Font("  Arial  ", Font.BOLD, 30));
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (!zenMode) {
            next();
            if (y.size() > 0) {
                for (int i = 0; i < y.size(); i++) {
                    if (aminoAcids.get(i).getCharacter() != '!' && y.get(i) != Integer.MAX_VALUE) {
                        g.setColor(colorFactory.getColorForAminoAcid(type, aminoAcids.get(i), randomizeColors));
                        String label = labels.get(i);
                        g.drawString(label, x.get(i), y.get(i));
                    }
                }
            }
            paintScore(g);
        }
        //wait for firework to finish?
        if (aminoAcids.size() > 0 && aminoAcids.getLast().getCharacter() == '!') {
            reset();
            visualiser.stop();
        }
    }

    private void paintScore(Graphics g) {
        if (!zenMode) {
            g.setFont(new Font("  Arial  ", Font.BOLD, 15));
            g.setColor(Color.WHITE);
            g.drawString("Score", 20, 50);
            g.drawString("" + score, 80, 50);
            g.drawString("Level", 20, 75);
            g.drawString("" + level, 80, 75);
            g.drawString("Combo", 20, 100);
            g.drawString("" + combo, 80, 100);
        }
    }

    @Override
    public void paint(Graphics g) {
        if (alive == false && fireworksPanel.sparksLeft() == 0) {
            if (!zenMode) {
                paintEndScreen(g);
            }
        } else {
            int levelheight = Math.max(10, 50 - (level - 1));
            paintTargetBar(g, levelheight);
            paintNormalScreen(g);
        }
    }

    private int lastExplodedIndex = 0;

    public void next() {
        if (!zenMode && alive) {
            for (int i = 0; i < y.size(); i++) {
                Integer yValue = y.get(i);
                if (y.get(i) < Integer.MAX_VALUE) {
                    if (yValue < getHeight() + 1) {
                        y.set(i, yValue + 1);
                    } else if (yValue >= getHeight() + 1) {
                        //these are missed values
                        increaseScore(-25);
                        resetCombo();
                    }
                }
            }
        }
    }

    public LinkedList<AminoAcidResult> getAminoAcidList() {
        return aminoAcids;
    }

    public int getScore() {
        return score;
    }

    public void increaseScore(int i) {
        score += i * Math.max(1, combo);
        if (i < 0 && score <= currentScoreCap) {
            score = currentScoreCap;
        }
    }

    public int getLevel() {
        return level;
    }

    public void increaseLevel(int i) {
        level += i;
        currentScoreCap = score;
    }

    public int getCombo() {
        return combo;
    }

    public void setCombo(int combo) {
        this.combo = combo;
        this.maxCombo = Math.max(maxCombo, combo);
    }

    public void resetCombo() {
        setCombo(0);
    }

    public LinkedList<Integer> getXList() {
        return x;
    }

    public LinkedList<Integer> getYList() {
        return y;
    }

    public LinkedList<String> getLabels() {
        return labels;
    }

    public void setDifficulty(int value) {
        this.difficulty = value;
    }

    public void setRandomColor(boolean selected) {
        this.randomizeColors = selected;
    }

    public void setRandomPosition(boolean selected) {
        this.randomizePosition = selected;
    }

}
