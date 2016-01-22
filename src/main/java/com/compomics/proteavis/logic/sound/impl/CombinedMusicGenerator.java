package com.compomics.proteavis.logic.sound.impl;

/* Adapted from a class which generates music from words
*  Based on Guido d'Arezzo's lookup chart for generating 
*  pitches from syllabes (ca. 1000 A.D.)
*  @author Andrew Troedson
 */
import com.compomics.proteavis.logic.sound.MusicScoreGenerator;
import com.compomics.proteavis.logic.processing.synchronizer.SequenceSynchronizer;
import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.model.ProteinMusicScore;
import com.compomics.proteavis.model.SoundAlphabet;
import com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType;
import com.compomics.proteavis.model.enums.AminoAcidProperty;
import com.compomics.proteavis.model.enums.Instrument;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import jm.music.data.*;
import static jm.constants.Durations.C;
import static jm.constants.Durations.M;
import static jm.constants.Durations.MD;
import static jm.constants.Durations.Q;
import static jm.constants.Pitches.A2;
import static jm.constants.Pitches.A3;
import static jm.constants.Pitches.B2;
import static jm.constants.Pitches.B3;
import static jm.constants.Pitches.C3;
import static jm.constants.Pitches.C4;
import static jm.constants.Pitches.D3;
import static jm.constants.Pitches.D4;
import static jm.constants.Pitches.E3;
import static jm.constants.Pitches.E4;
import static jm.constants.Pitches.F3;
import static jm.constants.Pitches.F4;
import static jm.constants.Pitches.G2;
import static jm.constants.Pitches.G3;
import static jm.constants.Pitches.G4;
import static jm.constants.Pitches.REST;
import static jm.constants.Pitches.a4;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public final class CombinedMusicGenerator extends MusicScoreGenerator {

    /**
     * The beat tick for the baseline
     */
    private int tick = 4;
    
    /**
     * The phrase for the second background instrument tick for the baseline
     */
    private Phrase background_phrase;
    /**
     * The phrase for the second background instrument tick for the baseline
     */
    private Phrase foreground_phrase;
    /**
     * The phrase for the second background instrument tick for the baseline
     */
    private Phrase baseline_1_phrase;
    /**
     * The phrase for the second background instrument tick for the baseline
     */
    private Phrase baseline_2_phrase;
    /**
     * The tempo of the music to play
     */
    private static final int tempo = 140;
    /**
     * The metronome for the background beat
     */
    private int metronome = 0;
    /**
     * The amino acid tone ladder
     */
    private final List<List<AminoAcidProperty>> aminoAcidToneLadder;
    /**
     * An object to perform statistics (debugging mainly)
     */
    private final DescriptiveStatistics stat = new DescriptiveStatistics();

    /**
     * A constructor for new proteinmusicscore generators
     *
     * @param scoreName the name of the song
     * @param alphabetType the ordening that is required
     */
    public CombinedMusicGenerator(String scoreName, SoundAlphabetType alphabetType) {
        this.score = new Score(scoreName, tempo);
        aminoAcidToneLadder = SoundAlphabet.getInstance().getAminoAcidToneLadder(alphabetType);
        initPhrases();
    }

    private int introNotes = 0;

    private void initPhrases() {
        background_phrase = new Phrase(0.0);
        foreground_phrase = new Phrase(0.0);
       }

    public void setIntroNotes(int notes) {
        this.introNotes = notes;
    }

    /**
     * composes the protein music score into a MIDI format
     *
     * @param p the protein music score
     */
    @Override
    public void compose(ProteinMusicScore p) {
        Part protein_instrument_part = new Part("background", p.getProteinInstrument().getInstrumentNr(), 0);
        Part peptide_instrument_part = new Part("foreground", p.getPeptideInstrument().getInstrumentNr(), 1);
        Part baseline_part_1 = new Part("baseline_1", Instrument.SYNTH_DRUMS.getInstrumentNr(), 2);
        Part baseline_part_2 = new Part("baseline_2", Instrument.CYMBAL.getInstrumentNr(), 3);
        LinkedList<AminoAcidResult> aaList = SequenceSynchronizer.preprocessAminoAcidMap(p);
        Note nextNote;
        for (int i = 0; i < introNotes; i++) {
            addNote(new Note(REST, 33), new AminoAcidResult('!', 0.0));
        }
        Iterator<AminoAcidResult> aminoAcidIterator = aaList.iterator();
        //this variable is set each new note is created, and 
        //sets the Pitch of the new note
        //(this is chosen at random from the 3 or 4 options 
        //allowed for that vowel in d'Arezzo's lookup chart)

        while (aminoAcidIterator.hasNext()) {
            AminoAcidResult aminoAcidResult = aminoAcidIterator.next();
            addAminoAcid(aminoAcidResult);
        }
        //compomics gimmick
        addOuttro();
        addNote(new Note(REST, 33), new AminoAcidResult('!', 0.0));
        protein_instrument_part.addPhrase((background_phrase));
        peptide_instrument_part.addPhrase((foreground_phrase));

        //add part (instrument) to the score
        score.addPart(protein_instrument_part);
        score.addPart(peptide_instrument_part);
        if (baseline_part_1 != null) {
            score.addPart(baseline_part_1);
            score.addPart(baseline_part_2);
        }
    }

    private void addAminoAcid(AminoAcidResult aminoAcidResult) {
        int notePitch;
        //the variable used to make the random pitch selection
        double identificationQuality;
        Note nextNote;
        if (aminoAcidResult.isConsidered()) {
            identificationQuality = (aminoAcidResult.getValue() / 100);
        } else {
            identificationQuality = 0;
        }
        int category = getNoteCategory(aminoAcidResult);
        switch (category) {
            case 0:
                identificationQuality = (identificationQuality * 4);
                switch ((int) identificationQuality) {
                    case 0:
                        notePitch = G2;
                        break;
                    case 1:
                        notePitch = E3;
                        break;
                    case 2:
                        notePitch = C4;
                        break;
                    default:
                        notePitch = a4;
                        break;
                }
                nextNote = new Note(notePitch, setNoteLength(aminoAcidResult));
                addNote(nextNote, aminoAcidResult);
                break;
            case 1:
                identificationQuality = (identificationQuality * 3);
                switch ((int) identificationQuality) {
                    case 0:
                        notePitch = A2;
                        break;
                    case 1:
                        notePitch = F3;
                        break;
                    default:
                        notePitch = D4;
                        break;
                }
                nextNote = new Note(notePitch, setNoteLength(aminoAcidResult));
                addNote(nextNote, aminoAcidResult);
                break;
            case 2:
                identificationQuality = (identificationQuality * 3);
                switch ((int) identificationQuality) {
                    case 0:
                        notePitch = B2;
                        break;
                    case 1:
                        notePitch = G3;
                        break;
                    default:
                        notePitch = E4;
                        break;
                }
                nextNote = new Note(notePitch, setNoteLength(aminoAcidResult));
                addNote(nextNote, aminoAcidResult);
                break;
            case 3:
                identificationQuality = (identificationQuality * 3);
                switch ((int) identificationQuality) {
                    case 0:
                        notePitch = C3;
                        break;
                    case 1:
                        notePitch = A3;
                        break;
                    default:
                        notePitch = F4;
                        break;
                }
                nextNote = new Note(notePitch, setNoteLength(aminoAcidResult));
                addNote(nextNote, aminoAcidResult);
                break;
            case 4:
                identificationQuality = (identificationQuality * 3);
                switch ((int) identificationQuality) {
                    case 0:
                        notePitch = D3;
                        break;
                    case 1:
                        notePitch = B3;
                        break;
                    default:
                        notePitch = G4;
                        break;
                }
                nextNote = new Note(notePitch, setNoteLength(aminoAcidResult));
                addNote(nextNote, aminoAcidResult);
                break;
            case -1:
            //these are trailing notes, we don't need to do anything with them
            default:
                break;
        }
    }

    public long getAverageNoteLength() {
        return (long) stat.getMean();
    }

    /**
     * Returns the category of the amino acid
     *
     * @param result the amino acid result
     * @return an integer indicating the category of the amino acid
     */
    private int getNoteCategory(AminoAcidResult result) {
        int i = 0;
        for (List<AminoAcidProperty> aminoAcidSet : aminoAcidToneLadder) {
            for (AminoAcidProperty aminoAcid : aminoAcidSet) {
                if (aminoAcid.getSingleLetter() == result.getCharacter()) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    /**
     * Makes a phrase fluent by replacing duplicate notes with an elongated one
     *
     * @param input the input phrase
     * @return the fluent phrase
     */
    private void addOuttro() {
        addAminoAcid(new AminoAcidResult('C', 100));
        addAminoAcid(new AminoAcidResult('O', 100));
        addAminoAcid(new AminoAcidResult('M', 100));
        addAminoAcid(new AminoAcidResult('P', 100));
        addAminoAcid(new AminoAcidResult('O', 100));
        addAminoAcid(new AminoAcidResult('M', 100));
        addAminoAcid(new AminoAcidResult('I', 100));
        addAminoAcid(new AminoAcidResult('C', 100));
        addAminoAcid(new AminoAcidResult('S', 100));
        addNote(new Note(REST, 33), new AminoAcidResult('!', 0.0));
    }

    /**
     * adds a note to the score
     *
     * @param note the note to add
     * @param aminoAcidResult the amino acid result related to the note
     */
    private void addNote(Note note, AminoAcidResult aminoAcidResult) {
        if (!aminoAcidResult.isConsidered()) {
            background_phrase.add(note);
            foreground_phrase.add(new Note(REST, note.getRhythmValue()));
        } else {
            foreground_phrase.add(note);
            background_phrase.add(note);
        }
        aminoAcidPresses.add(aminoAcidResult);
        long tempDuration = getDurationInMilliSeconds(note);
        stat.addValue(tempDuration);

    }

    private long getDurationInMilliSeconds(Note note) {
        return (long) (note.getDuration() * 1000 * 60 / score.getTempo());
    }

    //an internal method which decides on the length of each note 
    //depending on the number of consonants (as well as spaces and 
    //other symbols) between vowels	
    private double setNoteLength(AminoAcidResult result) {
        int listNumber = 0;
        for (List<AminoAcidProperty> aminoAcidSet : aminoAcidToneLadder) {
            for (AminoAcidProperty aminoAcid : aminoAcidSet) {
                if (aminoAcid.getSingleLetter() == result.getCharacter()) {
                    listNumber++;
                    break;
                }
            }
        }
        double noteLength;
        switch (listNumber) {
            case (0):
                noteLength = Q;
                break;
            case (1):
                noteLength = Q;
                break;
            case (2):
                noteLength = C;
                break;
            case (3):
                noteLength = C;
                break;
            case (4):
                noteLength = M;
                break;
            default: //(ie more than 4 consonants)
                noteLength = MD;
                break;
        }
        return noteLength;
    }
}
