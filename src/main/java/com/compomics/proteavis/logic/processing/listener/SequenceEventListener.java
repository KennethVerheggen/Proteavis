package com.compomics.proteavis.logic.processing.listener;

import com.compomics.proteavis.model.AminoAcidResult;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author compomics
 */
public class SequenceEventListener implements MetaEventListener, ControllerEventListener {

    protected long currentIndex = 0;

    public SequenceEventListener() {

    }

    @Override
    public void meta(MetaMessage meta) {
        processEvent(meta);
    }

    @Override
    public void controlChange(ShortMessage event) {
        processControl(event);
    }

    protected void processEvent(MetaMessage meta) {
        AminoAcidResult deserialize;
        try {
            if (meta.getType() == 43) {
                deserialize = AminoAcidResult.deserialize(meta.getData());
                System.out.println(currentIndex + "\t" + deserialize.getCharacter() + "\t" + deserialize.getValue());
                currentIndex++;
            }
        } catch (IOException ex) {
            Logger.getLogger(VisualisationEventListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(VisualisationEventListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processControl(ShortMessage event) {
        int command = event.getCommand();
        switch (command) {
            case ShortMessage.NOTE_ON:
                System.out.println("CEL - note on!");
                break;
            case ShortMessage.NOTE_OFF:
                System.out.println("CEL - note off!");
                break;
            default:
                System.out.println("CEL - unknown: " + command);
                break;
        }
    }

    public void reset() {
        currentIndex = 0;
    }
}
