package com.compomics.proteavis.logic.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author compomics
 */
public class ProteinHighlightImport {

    private static final HashMap<String, HashMap<String, Double>> highlightedPeptidesMap = new HashMap<>();
    private static double maximalValue = 0.0;

    public static HashMap<String, HashMap<String, Double>> getHighlightedPeptideMap() {
        return highlightedPeptidesMap;
    }

    public static double getMaximalValue() {
        if (maximalValue == 0) {
            return 1;
        }
        return maximalValue;
    }

    public static void addFileToComposition(File inputFile) throws IOException {
        TreeMap<String, String> proteinMap = ProteinInput.getProteinMap();
        if (proteinMap.isEmpty()) {
            throw new IOException("There are no sequences loaded into the factory yet, please load a Fasta file first !");
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("PROTEIN")) {
                    String[] split = line.split("\t");
                    String protein = split[0];
                    String peptide = split[1];
                    double value = Double.parseDouble(split[2]);
                    maximalValue = Math.max(value, maximalValue);
                    HashMap<String, Double> orDefault = highlightedPeptidesMap.getOrDefault(protein, new HashMap<>());
                    orDefault.put(peptide, value);
                    highlightedPeptidesMap.put(protein, orDefault);
                }
            }
        }
    }
}
