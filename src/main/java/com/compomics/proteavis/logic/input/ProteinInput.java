/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.proteavis.logic.input;

import com.compomics.proteavis.model.ProteinMusicScore;
import com.google.common.io.LineReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;
import javax.swing.JOptionPane;

/**
 *
 * @author compomics
 */
public class ProteinInput {

    private static final TreeMap<String, String> proteinMap = new TreeMap<>();
    private static LinkedList<ProteinMusicScore> musicScores = new LinkedList<>();

    public static TreeMap<String, String> getProteinMap() {
        return proteinMap;
    }

    public static void loadFasta(File file, String blockSeparator) throws IOException {
        //THE most naive implementation, structurally and computationally
        try {
            LineReader reader = new LineReader(new FileReader(file));
            String line = reader.readLine();
            StringBuilder sequenceBuilder = new StringBuilder();
            String header = "";
            while (line != null) {
                if (line.startsWith(blockSeparator)) {
                    //add limiting check for protein store to avoid growing
                    if (sequenceBuilder.length() > 0) {
                        proteinMap.put(header, sequenceBuilder.toString().trim());
                        sequenceBuilder.setLength(0);
                    }
                    header = line;
                } else {
                    sequenceBuilder.append(line);
                }
                line = reader.readLine();
            }
            proteinMap.put(header.split("\\|")[1], sequenceBuilder.toString().trim());
        } catch (Exception ex) {
            proteinMap.clear();
            JOptionPane.showConfirmDialog(null, "Could not parse fasta. Please verify the integrity of the file \n"+ex, "Could not import selection", JOptionPane.ERROR_MESSAGE);
        }
    }
}
