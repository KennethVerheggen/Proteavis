package com.compomics.proteavis.logic.processing.listener;

import com.compomics.proteavis.model.SoundAlphabet;
import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.view.Visualiser;
import com.compomics.proteavis.view.game.panels.GameEngine;
import javax.sound.midi.MetaMessage;

public class GameEventListener extends VisualisationEventListener {

    private GameEngine mp;

    public GameEventListener(Visualiser visualiser, SoundAlphabet.SoundAlphabetType type) {
        super(visualiser);
    }

    public void setGameEnginePanel(GameEngine gameEnginePanel) {
        this.mp = gameEnginePanel;
    }

    @Override
    protected void processEvent(MetaMessage meta) {
        if (meta.getType() == 43) {
            AminoAcidResult deserialize;
            try {
                currentIndex++;
                deserialize = AminoAcidResult.deserialize(meta.getData());
                //add more notes if there are more to be displayed?
                for (int i = 0; i <= deserialize.getRepeated(); i++) {
                    mp.addAminoAcid(deserialize);
                }
                visualiser.setSoundBarValue(currentIndex, deserialize.getCharacter());
            } catch (Exception ex) {
                visualiser.setSoundBarValue(currentIndex);
            }
        } else if (meta.getType() == 42) {
            mp.addAminoAcid(new AminoAcidResult('?', Double.MAX_VALUE));
        }
    }

}
