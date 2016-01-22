package com.compomics.proteavis.logic.util;

import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType;
import static com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType.AVG_MASS;
import static com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType.CHARGE;
import static com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType.HYDROPATHY;
import static com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType.LETTER;
import static com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType.MONO_MASS;
import static com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType.SOLUBILITY;
import static com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType.pKa_COOH;
import static com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType.pKa_NH2;
import com.compomics.proteavis.model.enums.AminoAcidProperty;
import java.awt.Color;
import java.util.HashMap;
import no.uib.jsparklines.renderers.util.GradientColorCoding;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author compomics
 */
public class AminoAcidColorFactory {

    private static AminoAcidColorFactory instance;
    private static final HashMap<SoundAlphabetType, DescriptiveStatistics> soundAlphabetValuesMapping = new HashMap<>();

    private AminoAcidColorFactory() {
    }

    public static AminoAcidColorFactory getInstance() {
        if (instance == null) {
            instance = new AminoAcidColorFactory();
            for (SoundAlphabetType aType : SoundAlphabetType.values()) {
                DescriptiveStatistics stat = soundAlphabetValuesMapping.getOrDefault(aType, new DescriptiveStatistics());
                for (AminoAcidProperty property : AminoAcidProperty.values()) {
                    stat.addValue(getValueForAminoAcid(property, aType));
                }
                soundAlphabetValuesMapping.put(aType, stat);
            }

        }
        return instance;
    }

    private static double getValueForAminoAcid(AminoAcidProperty property, SoundAlphabetType type) {
        switch (type) {
            case AVG_MASS:
                return (property.getAvg_isotopic_mass());

            case MONO_MASS:
                return (property.getMono_isotopic_mass());

            case HYDROPATHY:
                return (property.getHydropathy());

            case CHARGE:
                return (property.getCharge());

            case SOLUBILITY:
                return (property.getSolubility());

            case pKa_COOH:
                return (property.getpKa_COOH());

            case pKa_NH2:
                return (property.getpKa_NH2());
            case LETTER:
                return property.getSingleLetter();
            default:
                return (0.0);
        }
    }

    public Color getColorForAminoAcid(SoundAlphabetType type, AminoAcidResult result, boolean randomColor) {
        double value = getValueForAminoAcid(AminoAcidProperty.getAminoAcidProperty(result.getCharacter()), type);
        Color gradientColor;
        if (soundAlphabetValuesMapping.containsKey(type) && !randomColor) {
            double max = soundAlphabetValuesMapping.get(type).getMax();
            double min = soundAlphabetValuesMapping.get(type).getMin();
            gradientColor = GradientColorCoding.findGradientColor(value, min, max, GradientColorCoding.ColorGradient.RedBlackGreen, (max > 0 && min > 0));
        } else {
            //give a random color?
            //so the difference between the maximum value and the minimal value = 255
            int red = (int) (Math.random() * 255);
            int green = (int) (Math.random() * 255);
            int blue = (int) (Math.random() * 255);
            gradientColor = new Color(red, green, blue);
        }
        return gradientColor;
    }
}
