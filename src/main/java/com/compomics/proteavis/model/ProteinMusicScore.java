package com.compomics.proteavis.model;

import com.compomics.proteavis.model.enums.Instrument;
import java.util.LinkedList;

/**
 *
 * @author compomics
 */
public class ProteinMusicScore extends LinkedList<AminoAcidResult> {

    /**
     * The protein accession
     */
    private final String accession;
    /**
     * The protein sequence
     */
    private String sequence;
    /**
     * The instrument to play this protein
     */
    private final Instrument proteinInstrument;
    /**
     * The instrument to play this protein
     */
    private final Instrument peptideInstrument;

    
        public ProteinMusicScore(String accession, String sequence) {
        this.accession = accession;
        this.sequence = sequence;
        this.proteinInstrument = Instrument.ACCORDION;
        this.peptideInstrument = Instrument.VIBRAPHONE;
        for (char letter : sequence.toCharArray()) {
            add(new AminoAcidResult(letter, 0));
        }
    }

    
    public ProteinMusicScore(String accession, String sequence, Instrument proteinInstrument, Instrument peptideInstrument) {
        this.accession = accession;
        this.sequence = sequence;
        this.proteinInstrument = proteinInstrument;
        this.peptideInstrument = peptideInstrument;
        for (char letter : sequence.toCharArray()) {
            add(new AminoAcidResult(letter, 0));
        }
    }

    @Override
    public String toString() {
        StringBuilder temp = new StringBuilder(getAccession() + "\t");
        for (AminoAcidResult result : this) {
            if (result.isConsidered()) {
                temp.append(("" + result.getCharacter()).toUpperCase());
            } else {
                temp.append(("" + result.getCharacter()).toLowerCase());
            }
        }
        return temp.toString();
    }

    public Instrument getProteinInstrument() {
        return proteinInstrument;
    }

    public Instrument getPeptideInstrument() {
        return peptideInstrument;
    }

    public String getAccession() {
        return accession;
    }

    public String getSequence() {
        return sequence;
    }

    /**
     * Adds an identification to the protein
     *
     * @param peptideSequence the peptide sequence
     * @param confidence the psm confidence
     */
    public void addIdentification(String peptideSequence, double confidence) {
        int lastIndex = 0;
        while (lastIndex != -1) {
            lastIndex = sequence.indexOf(peptideSequence, lastIndex);
            if (lastIndex != -1) {
                for (int i = lastIndex; i < (lastIndex + peptideSequence.length()); i++) {
                    get(i).setValue(confidence);
                    get(i).setConsidered(true);
                }
                lastIndex += peptideSequence.length();
            }
        }
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
