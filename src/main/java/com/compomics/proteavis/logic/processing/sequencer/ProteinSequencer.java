package com.compomics.proteavis.logic.processing.sequencer;

import com.compomics.proteavis.logic.processing.listener.SequenceEventListener;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

/**
 *
 * @author compomics
 */
public class ProteinSequencer implements AutoCloseable {

    private final Sequencer sequencer;
    private long microsecondPosition;
    
    public ProteinSequencer() throws MidiUnavailableException {
        sequencer = MidiSystem.getSequencer();
        sequencer.open();
    }

    private void addSequenceEventListener(SequenceEventListener listener) {
        int[] types = new int[128];
        for (int ii = 0; ii < 128; ii++) {
            types[ii] = ii;
        }
        sequencer.addControllerEventListener(listener, types);
        sequencer.addMetaEventListener(listener);
    }

    public Sequencer getSequencer() {
        return sequencer;
    }

    public void setLoop() {
        sequencer.setLoopStartPoint(0);
        sequencer.setLoopEndPoint(sequencer.getTickLength());
    }

    public void play() {
        sequencer.setMicrosecondPosition(microsecondPosition);
        sequencer.start();
    }

    public void pause() {
        microsecondPosition = sequencer.getMicrosecondPosition();
        sequencer.stop();
    }

    public void reset() {
        microsecondPosition = 0;
        sequencer.stop();
    }

    public void setSequence(Sequence sequence, SequenceEventListener listener) throws InvalidMidiDataException, IOException {
        sequencer.setSequence(sequence);
        addSequenceEventListener(listener);
    }

    @Override
    public void close() throws Exception {
        sequencer.close();
    }

}
