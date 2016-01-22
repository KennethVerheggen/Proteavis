/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.proteavis.view;

/**
 *
 * @author compomics
 */
public interface Visualiser {

    void addNote(Character aa, double highlightValue);

    /**
     * Sets the maximal value for the progress bar
     *
     * @param maxTick the maximal value
     */
    void setSoundBarMax(long maxTick);

    /**
     * Sets the current value for the progress bar
     *
     * @param tick the current value
     */
    void setSoundBarValue(long tick);

    /**
     * Sets the current value for the progress bar
     *
     * @param tick the current value
     * @param aminoAcid the current amino acid
     */
    void setSoundBarValue(long tick, char aminoAcid);

    public void stop();
    
}
