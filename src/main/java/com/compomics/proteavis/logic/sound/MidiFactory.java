package com.compomics.proteavis.logic.sound;

import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.model.ProteinMusicScore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import jm.music.data.Score;
import jm.util.Write;

public class MidiFactory {

    public static void createMusicScore(Collection<ProteinMusicScore> proteinMusicScores, MusicScoreGenerator musicScoreGenerator) throws IOException, MidiUnavailableException, InvalidMidiDataException {
        ProteinMusicScore masterScore = new ProteinMusicScore("Master", "");
        for (ProteinMusicScore aScore : proteinMusicScores) {
            for (AminoAcidResult aResult : aScore) {
                masterScore.add(aResult);
                masterScore.setSequence(masterScore.getSequence() + aResult.getCharacter());
            }
            masterScore.setSequence(masterScore.getSequence() + "!!!!!");
        }
        musicScoreGenerator.compose(masterScore);
    }

    public static void save(Score score, File midiFile) throws FileNotFoundException, IOException {
        Write.midi(score, midiFile.getAbsolutePath());
    }

    public static InputStream getMidiInputStream(Score score) throws FileNotFoundException, IOException {
        //create temporary bayte array output stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Write.midi(score, baos);
        //create input stream from baos
        return new ByteArrayInputStream(baos.toByteArray());

    }

}
