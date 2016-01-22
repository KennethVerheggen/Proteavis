package com.compomics.proteavis.model.enums;

/**
 *
 * @author compomics
 */
public enum AminoAcidProperty {

    Arginine(new Character('R'), 1, 1, 9.09, 2.18, 71.8, 97.05276, 97.1167, 1),
    Asparagine(new Character('N'), 1, 0, 8.8, 2.02, 2.4, 147.06841, 147.1766, 2),
    Aspartate(new Character('D'), 1, -1, 9.6, 1.88, 0.42, 103.00919, 103.1388, 3),
    Glutamate(new Character('E'), 1, -1, 9.67, 2.19, 0.72, 129.04259, 129.1155, 4),
    Glutamine(new Character('Q'), 1, 0, 9.13, 2.17, 2.6, 97.05276, 97.1167, 5),
    Lysine(new Character('K'), 1, 1, 10.28, 8.9, 0, 128.09496, 128.1741, 6),
    Serine(new Character('S'), 1, 0, 9.15, 2.21, 36.2, 87.03203, 87.0782, 7),
    Threonine(new Character('T'), 1, 0, 9.12, 2.15, 0, 101.04768, 101.1051, 8),
    Cysteine(new Character('C'), 0, 0, 10.78, 1.71, 0, 103.00919, 103.1388, 9),
    Histidine(new Character('H'), 0, 1, 8.97, 1.78, 4.19, 129.04259, 129.1155, 10),
    Methionine(new Character('M'), 0, 0, 9.21, 2.28, 5.14, 131.04049, 131.1926, 11),
    Alanine(new Character('A'), -1, 0, 9.87, 2.35, 15.8, 71.03711, 71.0788, 12),
    Valine(new Character('V'), -1, 0, 9.72, 2.29, 5.6, 101.04768, 101.1051, 13),
    Glycine(new Character('G'), -1, 0, 9.6, 2.34, 22.5, 129.04259, 129.1155, 14),
    Isoleucine(new Character('I'), -1, 0, 9.76, 2.32, 3.36, 113.08406, 113.1594, 15),
    Leucine(new Character('L'), -1, 0, 9.6, 2.36, 2.37, 128.09496, 128.1741, 16),
    Phenylalanine(new Character('F'), -1, 0, 9.24, 2.58, 2.7, 129.04259, 129.1155, 17),
    Proline(new Character('P'), -1, 0, 10.6, 1.99, 1.54, 97.05276, 97.1167, 18),
    Tryptophan(new Character('W'), -1, 0, 9.39, 2.38, 1.06, 186.07931, 186.2132, 19),
    Tyrosine(new Character('Y'), -1, 0, 9.11, 2.2, 0.038, 163.06333, 163.176, 20);

    private final int hydropathy;
    private final int charge;
    private final double pKa_NH2;
    private final double pKa_COOH;
    private final double solubility;
    private final double mono_isotopic_mass;
    private final double avg_isotopic_mass;
    private final char singleLetter;
    private final int columnNumber;

    AminoAcidProperty(char singleLetter,
            int hydropathy,
            int charge,
            double pKa_NH2,
            double pKa_COOH,
            double solubility,
            double mono_isotopic_mass,
            double avg_isotopic_mass,
            int columnNumber) {
        this.singleLetter = singleLetter;
        this.hydropathy = hydropathy;
        this.charge = charge;
        this.pKa_NH2 = pKa_NH2;
        this.pKa_COOH = pKa_COOH;
        this.solubility = solubility;
        this.mono_isotopic_mass = mono_isotopic_mass;
        this.avg_isotopic_mass = avg_isotopic_mass;
        this.columnNumber = columnNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public int getHydropathy() {
        return hydropathy;
    }

    public int getCharge() {
        return charge;
    }

    public double getpKa_NH2() {
        return pKa_NH2;
    }

    public double getpKa_COOH() {
        return pKa_COOH;
    }

    public double getSolubility() {
        return solubility;
    }

    public double getMono_isotopic_mass() {
        return mono_isotopic_mass;
    }

    public double getAvg_isotopic_mass() {
        return avg_isotopic_mass;
    }

    public char getSingleLetter() {
        return singleLetter;
    }

    public static AminoAcidProperty getAminoAcidProperty(char singleLetter) {
        for (AminoAcidProperty aProperty : values()) {
            if (aProperty.getSingleLetter() == singleLetter) {
                return aProperty;
            }
        }
        return Alanine;
    }

}
