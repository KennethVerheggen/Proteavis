package com.compomics.proteavis.logic.processing.synchronizer;

import com.compomics.proteavis.model.AminoAcidResult;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

/**
 *
 * @author compomics
 */
public class SequenceSynchronizer {

    private Iterator<AminoAcidResult> aminoAcidResultIterator;
    private final LinkedList<AminoAcidResult> aminoAcidResults;
    private final Sequence sequence;

    public SequenceSynchronizer(Sequence sequence, LinkedList<AminoAcidResult> aminoAcidResults, int trailingNotes) throws InvalidMidiDataException, IOException {
        this.aminoAcidResults = aminoAcidResults;
        this.aminoAcidResultIterator = aminoAcidResults.iterator();
        this.sequence = sequence;
        createSynchronizationTrack(trailingNotes);
    }

    private void createSynchronizationTrack(int trailingNotes) throws InvalidMidiDataException, IOException {
        Track newTrack = sequence.createTrack();
        LinkedList<AminoAcidResult> preprocessAminoAcidMap =aminoAcidResults;//preprocessAminoAcidMap(aminoAcidResults);
        Track correspondingTrack = getCorrespondingTrack();
        aminoAcidResultIterator = preprocessAminoAcidMap.iterator();
        long currentTick = 0;
        for (int i = 0; i < correspondingTrack.size(); i++) {
            if (aminoAcidResultIterator.hasNext()) {
                AminoAcidResult nextResult = aminoAcidResultIterator.next();
                byte[] serialize = nextResult.toByteArray();
                currentTick = (correspondingTrack.get(i).getTick());
                newTrack.add(new MidiEvent(new MetaMessage(43, serialize, serialize.length), currentTick));
            } else {
                //trim it back down
                //@ToDo figure out why there are more notes than we add in !
                correspondingTrack.remove(correspondingTrack.get(i));
            }
        }
        //add trailing notes if required to help the GUI
        AminoAcidResult nextResult = new AminoAcidResult('!', 0.0);
        byte[] serialize = nextResult.toByteArray();
        for (int i = 0; i <= trailingNotes; i++) {
            currentTick += 100;
            newTrack.add(new MidiEvent(new MetaMessage(43, serialize, serialize.length), currentTick));
        }
        newTrack.add(new MidiEvent(new MetaMessage(42, serialize, serialize.length), currentTick));
        for (Track aTrack : sequence.getTracks()) {
            for (int i = 0; i < aTrack.size(); i++) {
                if (i > newTrack.size()) {
                    aTrack.remove(aTrack.get(i));
                }
            }
        }
    }

    public static LinkedList<AminoAcidResult> preprocessAminoAcidMap(LinkedList<AminoAcidResult> aminoAcidResults) {
        //if two subsequent results are equal, they have to be merged !
        LinkedList<AminoAcidResult> tempList = new LinkedList<>();
        for (AminoAcidResult result : aminoAcidResults) {
            if (tempList.size() > 1 && tempList.getLast().equals(result)&&tempList.getLast().getCharacter()!='!') {
                //maybe modify the amino acid so the gui knows there were originally 2 or more???
                tempList.getLast().setRepeated(tempList.getLast().getRepeated() + 1);
            } else {
                tempList.add(result);
            }
        }
        return tempList;
    }

    private static byte[] doubleToBytes(double x) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.putDouble(x);
        return buffer.array();
    }

    public static double bytesToDouble(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.put(bytes);
        //buffer.flip();//need flip 
        return buffer.getDouble();
    }

    private Track getCorrespondingTrack() {
        Track similarTrack = sequence.getTracks()[0];
        int largestTrack = Integer.MIN_VALUE;
        for (Track aTrack : sequence.getTracks()) {
            if (aTrack.size() > largestTrack) {
                largestTrack = aTrack.size();
                similarTrack = aTrack;
            } else if (aTrack.size() == aminoAcidResults.size()) {
                return aTrack;
            }
        }
        return similarTrack;
    }

}
