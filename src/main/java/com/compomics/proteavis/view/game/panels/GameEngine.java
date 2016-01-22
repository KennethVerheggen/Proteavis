package com.compomics.proteavis.view.game.panels;

import com.compomics.proteavis.model.SoundAlphabet;
import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.view.Visualiser;
import java.util.LinkedList;

/**
 *
 * @author compomics
 */
public interface GameEngine {

    int getHeight();

    void addAminoAcid(AminoAcidResult result);

    LinkedList<AminoAcidResult> getAminoAcidList();

    int getCombo();

    LinkedList<String> getLabels();

    int getLevel();

    int getMinimalDistance();

    int getMaximalDistance();

    int getScore();

    LinkedList<Integer> getXList();

    LinkedList<Integer> getYList();

    void increaseLevel(int i);

    void increaseScore(int i);

    void next();

    void pause();

    void reset();

    void resetCombo();

    void setCombo(int combo);

    void setSoundAlphabetType(SoundAlphabet.SoundAlphabetType type);

    void setVisualiser(Visualiser visualiser);

    void unpause();

    public boolean isZenMode();

    public int getTargetDistance();

}
