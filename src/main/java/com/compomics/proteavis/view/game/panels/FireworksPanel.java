package com.compomics.proteavis.view.game.panels;

import com.compomics.proteavis.logic.util.AminoAcidColorFactory;
import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType;
import com.compomics.proteavis.view.game.effects.Spark;
import com.compomics.proteavis.view.game.effects.impl.BubbleSpark;
import com.compomics.proteavis.view.game.effects.impl.CircleSpark;
import com.compomics.proteavis.view.game.effects.impl.GiantSpark;
import com.compomics.proteavis.view.game.effects.impl.MovingSpark;
import com.compomics.proteavis.view.game.effects.impl.PerfectCircleSpark;
import com.compomics.proteavis.view.game.effects.impl.TrigSpark;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

public class FireworksPanel extends JPanel {

    private static final long serialVersionUID = 1266778429484392409L;

    private LinkedList<Spark> sparks = new LinkedList<Spark>();

    private Dimension MAX_DIMENSION = new Dimension(800, 800);

    private Random generator = new Random();
    private double minValue;
    private double maxValue;

    public FireworksPanel() {
        super();
        setOpaque(false);
        Timer timer = new Timer(33, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (sparksLeft() > 0) {
                    repaint();
                }
            }
        });
        //timer.start();
    }

    public void init(Dimension MAX_DIMENSION) {
        this.MAX_DIMENSION = MAX_DIMENSION;
        this.setPreferredSize(MAX_DIMENSION);
        this.setMinimumSize(MAX_DIMENSION);
        this.setMaximumSize(MAX_DIMENSION);
        this.setLayout(null);
    }

    public int sparksLeft() {
        return sparks.size();
    }

    public boolean removeSpark(Spark s) {
        return this.sparks.remove(s);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 0));
        Rectangle clip = g.getClip().getBounds();
        g.fillRect(0, 0, clip.width, clip.height);
        Graphics2D g2d = (Graphics2D) g;
        Spark array[] = sparks.toArray(new Spark[2 * sparks.size()]);
        for (Spark s : array) {
            if (s == null) {
                break;
            }
            s.draw(g2d);
            s = null;
        }
        //sparks.clear();
        //    super.paintComponent(g);
    }

    public void explodeGiant(int x, int y) {
        int sparkCount = 20 + generator.nextInt(20);
        Color c = new Color(generator.nextInt(255), generator.nextInt(255), generator.nextInt(255));
        long lifespan = 1000 + generator.nextInt(1000);
        createGiantSpark(x, y, sparkCount, c, lifespan);
    }

    public void explodeAminoAcid(AminoAcidResult result, SoundAlphabetType type, boolean randomColor) {
        int x = (int) (Math.random() * getWidth());
        int y = (int) (Math.random() * getHeight());
        explodeAminoAcid(result, type, x, y, randomColor);
    }

    public void explodeAminoAcid(AminoAcidResult result, SoundAlphabetType type, int x, int y, boolean randomColor) {
        int baseSparks = 10;
        double explosionStrength = (double) result.getValue() / (double) Math.max(Math.abs(maxValue), Math.abs(minValue));
        int sparkCount = baseSparks+(int) (baseSparks * explosionStrength);
        Color c = AminoAcidColorFactory.getInstance().getColorForAminoAcid(type, result, randomColor);
        long lifespan = 500+(int)(500*explosionStrength);
        //random eplosions isn't that good of an idea...perhaps precalibrate this panel to know what the maximal values will be?
        int choice = generator.nextInt(100);
        if (choice < 18) {
            createCircleSpark(x, y, sparkCount, c, lifespan);
        } else if (choice < 36) {
            createPerfectCircleSpark(x, y, sparkCount, c, lifespan);
        } else if (choice < 54) {
            createMovingSpark(x, y, sparkCount, c, lifespan);
        } else if (choice < 72) {
            createBubbleSpark(x, y, sparkCount, c, lifespan);
        } else {
            createTrigSpark(x, y, sparkCount, c, lifespan);
        }
    }

    public void explode(int x, int y) {
        int sparkCount = 10 + generator.nextInt(20);
        Color c = new Color(generator.nextInt(255), generator.nextInt(255), generator.nextInt(255));
        long lifespan = 500 + generator.nextInt(1000);

        int choice = generator.nextInt(100);

        if (choice < 18) {
            createCircleSpark(x, y, sparkCount, c, lifespan);
        } else if (choice < 36) {
            createPerfectCircleSpark(x, y, sparkCount, c, lifespan);
        } else if (choice < 54) {
            createMovingSpark(x, y, sparkCount, c, lifespan);
        } else if (choice < 72) {
            createBubbleSpark(x, y, sparkCount, c, lifespan);
        } else {
            createTrigSpark(x, y, sparkCount, c, lifespan);
        }
    }

    private void createCircleSpark(int x, int y, int sparkCount, Color c, long lifespan) {
        for (int i = 0; i < sparkCount; i++) {
            double direction = 360 * generator.nextDouble();
            double speed = 10 * generator.nextDouble() + 5;
            sparks.addLast(new CircleSpark(this, direction, x, y, c, lifespan, speed));
        }
    }

    private void createPerfectCircleSpark(int x, int y, int sparkCount, Color c, long lifespan) {
        sparkCount *= 2;

        lifespan /= 2;

        double speed = 20 * generator.nextDouble() + 5;

        for (int i = 0; i < sparkCount; i++) {
            double direction = 360 * generator.nextDouble();
            sparks.addLast(new PerfectCircleSpark(this, direction, x, y, c, lifespan, speed));
        }
    }

    private void createGiantSpark(int x, int y, int sparkCount, Color c, long lifespan) {
        for (int i = 0; i < sparkCount; i++) {
            double direction = 360 * generator.nextDouble();
            double speed = 10 * generator.nextDouble() + 5;
            sparks.addLast(new GiantSpark(this, direction, x, y, c, lifespan, speed));
        }
    }

    private void createTrigSpark(int x, int y, int sparkCount, Color c, long lifespan) {
        for (int i = 0; i < sparkCount; i++) {
            double direction = 360 * generator.nextDouble();
            double speed = 10 * generator.nextDouble() + 5;
            sparks.addLast(new TrigSpark(this, direction, x, y, c, lifespan, speed));
        }
    }

    private void createMovingSpark(int x, int y, int sparkCount, Color c, long lifespan) {
        for (int i = 0; i < sparkCount; i++) {
            double direction = 360 * generator.nextDouble();
            double speed = 10 * generator.nextDouble() + 5;
            sparks.addLast(new MovingSpark(this, direction, x, y, c, lifespan, speed));
        }
    }

    private void createBubbleSpark(int x, int y, int sparkCount, Color c, long lifespan) {
        for (int i = 0; i < sparkCount; i++) {
            double direction = 360 * generator.nextDouble();
            double speed = 10 * generator.nextDouble() + 5;
            sparks.addLast(new BubbleSpark(this, direction, x, y, c, lifespan, speed));
        }
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

}
