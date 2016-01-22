package com.compomics.proteavis.logic.processing.listener;

import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.view.Visualiser;
import javax.sound.midi.MetaMessage;
import javax.swing.JOptionPane;

/**
 *
 * @author compomics
 */
public class VisualisationEventListener extends SequenceEventListener {

    final Visualiser visualiser;

    public VisualisationEventListener(Visualiser visualiser) {
        super();
        this.visualiser = visualiser;
    }

    @Override
    protected void processEvent(MetaMessage meta) {
        if (meta.getType() == 43) {
            AminoAcidResult deserialize;
            try {
                deserialize = AminoAcidResult.deserialize(meta.getData());
                //add more notes if there are more to be displayed?
                for (int i = 0; i <= deserialize.getRepeated(); i++) {
                    visualiser.addNote(deserialize.getCharacter(), deserialize.getValue());
                    visualiser.setSoundBarValue(currentIndex, deserialize.getCharacter());
                }
            } catch (Exception ex) {
                JOptionPane.showConfirmDialog(null, "An error occured when handling a note :\n" + ex, "Could not interpret note", JOptionPane.ERROR_MESSAGE);

            } finally {
                visualiser.setSoundBarValue(currentIndex);
                currentIndex++;
            }
        } else if (meta.getType() == 42) {
            System.out.println("Done");
            visualiser.stop();
        }
    }

}
