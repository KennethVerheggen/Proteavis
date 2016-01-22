package com.compomics.proteavis.model;


import static com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType.SOLUBILITY;
import com.compomics.proteavis.model.enums.AminoAcidProperty;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author compomics
 */
public class SoundAlphabet implements Comparator {

    private static final int ladder_step_size = 4;
    private static SoundAlphabet instance;
    private SoundAlphabetType type;

    private SoundAlphabet() {

    }

    public static SoundAlphabet getInstance() {
        if (instance == null) {
            instance = new SoundAlphabet();
        }
        return instance;
    }
    
    /**
     * Returns the tone alphabet for the selected alphabet type
     * @param type the type of ordening that is request
     * @return a list that contains all "bins" of notes
     */
    public List<List<AminoAcidProperty>> getAminoAcidToneLadder(SoundAlphabetType type) {
        if (type != null) {
            this.type = type;
        } else {
            this.type = SOLUBILITY;
        }
        List<AminoAcidProperty> aminoAcidProperties = new ArrayList<>(AminoAcidProperty.values().length);
        aminoAcidProperties.addAll(Arrays.asList(AminoAcidProperty.values()));
        aminoAcidProperties.sort(getInstance());
        return Lists.partition(aminoAcidProperties, ladder_step_size);
    }

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof AminoAcidProperty && o2 instanceof AminoAcidProperty)) {
            return -1;
        } else {
            AminoAcidProperty acp1 = (AminoAcidProperty) o1;
            AminoAcidProperty acp2 = (AminoAcidProperty) o2;
            switch (type) {
                case LETTER:
                    if (acp1.getSingleLetter() > acp2.getSingleLetter()) {
                        return 1;
                    } else if (acp1.getSingleLetter() == acp2.getSingleLetter()) {
                        return 0;
                    } else {
                        return -1;
                    }
                case HYDROPATHY:
                    if (acp1.getHydropathy() > acp2.getHydropathy()) {
                        return 1;
                    } else if (acp1.getHydropathy() == acp2.getHydropathy()) {
                        return 0;
                    } else {
                        return -1;
                    }
                case CHARGE:
                    if (acp1.getCharge() > acp2.getCharge()) {
                        return 1;
                    } else if (acp1.getCharge() == acp2.getCharge()) {
                        return 0;
                    } else {
                        return -1;
                    }
                case pKa_NH2:
                    if (acp1.getpKa_NH2() > acp2.getpKa_NH2()) {
                        return 1;
                    } else if (acp1.getpKa_NH2() == acp2.getpKa_NH2()) {
                        return 0;
                    } else {
                        return -1;
                    }
                case pKa_COOH:
                    if (acp1.getpKa_COOH() > acp2.getpKa_COOH()) {
                        return 1;
                    } else if (acp1.getpKa_COOH() == acp2.getpKa_COOH()) {
                        return 0;
                    } else {
                        return -1;
                    }
                case SOLUBILITY:
                    if (acp1.getSolubility() > acp2.getSolubility()) {
                        return 1;
                    } else if (acp1.getSolubility() == acp2.getSolubility()) {
                        return 0;
                    } else {
                        return -1;
                    }
                case MONO_MASS:
                    if (acp1.getMono_isotopic_mass() > acp2.getMono_isotopic_mass()) {
                        return 1;
                    } else if (acp1.getMono_isotopic_mass() == acp2.getMono_isotopic_mass()) {
                        return 0;
                    } else {
                        return -1;
                    }
                case AVG_MASS:
                    if (acp1.getAvg_isotopic_mass() > acp2.getAvg_isotopic_mass()) {
                        return 1;
                    } else if (acp1.getAvg_isotopic_mass() == acp2.getAvg_isotopic_mass()) {
                        return 0;
                    } else {
                        return -1;
                    }
                default:
                    return -1;
            }
        }
    }

    public static enum SoundAlphabetType {
        LETTER("Single Letter Alphabet"),
        HYDROPATHY("Amino acid hydropapthy"),
        CHARGE("Amino acid charge"),
        pKa_NH2("Amino acid basicity"),
        pKa_COOH("Amino acid acidity"),
        SOLUBILITY("Amino acid solubility"),
        MONO_MASS("Amino acid mono isotopic mass"),
        AVG_MASS("Amino acid average isotopic mass");
        private String displayName;

        private SoundAlphabetType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

    }

}
