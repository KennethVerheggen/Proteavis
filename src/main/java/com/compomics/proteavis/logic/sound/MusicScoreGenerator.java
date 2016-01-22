/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.proteavis.logic.sound;

import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.model.ProteinMusicScore;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import jm.JMC;
import jm.midi.MidiParser;
import jm.midi.SMF;
import jm.music.data.Score;

/**
 *
 * @author compomics
 */
public abstract class MusicScoreGenerator implements JMC {

    protected LinkedList<AminoAcidResult> aminoAcidPresses = new LinkedList<>();
    protected Score score;

    public MusicScoreGenerator() {
    }

    public LinkedList<AminoAcidResult> getAminoAcidPresses() {
        return aminoAcidPresses;
    }

    /**
     * composes the protein music score into a MIDI format
     *
     * @param p the protein music score
     */
    public abstract void compose(ProteinMusicScore p);

    public Score getScore() {
        return score;
    }

    /**
     * saves the score to an output stream
     *
     * @param outputStream the outputstream to write to
     */
    public void save(OutputStream outputStream) {
        //Score s = adjustTempo(scr);
        SMF smf = new SMF();
        try {
            smf.clearTracks();
            MidiParser.scoreToSMF(score, smf);
            smf.write(outputStream);
        } catch (IOException e) {
            JOptionPane.showConfirmDialog(null, "An error has occurred while saving the file \n" + e, "Could not import selection", JOptionPane.ERROR_MESSAGE);
        }
    }

    public abstract long getAverageNoteLength();

}
