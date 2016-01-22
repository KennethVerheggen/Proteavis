package com.compomics.proteavis.logic.input;

import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.model.ProteinMusicScore;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author compomics
 */
public class ClustalMSAFileImport {

    private static final String splitter = "      ";

    public static LinkedList<ProteinMusicScore> combineClustalResults(LinkedHashMap<String, StringBuilder> alignment) {

        LinkedList<ProteinMusicScore> musicScores = new LinkedList<>();
        Iterator<Map.Entry<String, StringBuilder>> iterator = alignment.entrySet().iterator();
        //templateAllignment should be the first...
        //Consensus allignment is the last...
        Map.Entry<String, StringBuilder> next = iterator.next();
        ProteinMusicScore master = new ProteinMusicScore(next.getKey(), next.getValue().toString()+"!");
        musicScores.addFirst(master);
        ProteinMusicScore nextMusicScore = null;
        while (iterator.hasNext()) {
            Map.Entry<String, StringBuilder> temp = iterator.next();
         //   if (!temp.getKey().equalsIgnoreCase("CONSENSUS")) {
                nextMusicScore = new ProteinMusicScore(temp.getKey(), temp.getValue().toString());
                musicScores.add(nextMusicScore);
          //  }
        }
        ProteinMusicScore consensus = nextMusicScore;
        //use the consensus to set values for the master?
        for (int i = 0; i < consensus.size(); i++) {
            AminoAcidResult masterAa = master.get(i);
            AminoAcidResult consensusAa = consensus.get(i);
            if (masterAa != null) {
                /**
                 * An * (asterisk) indicates positions which have a single,
                 * fully conserved residue. A : (colon) indicates conservation
                 * between groups of strongly similar properties - scoring > 0.5
                 * in the Gonnet PAM 250 matrix. A . (period) indicates
                 * conservation between groups of weakly similar properties -
                 * scoring =< 0.5 in the Gonnet PAM 250 matrix.
                 */
                switch (consensusAa.getCharacter()) {
                    case '*':
                        masterAa.setValue(masterAa.getValue() + 100);
                        break;
                    case ':':
                        masterAa.setValue(masterAa.getValue() + 50);
                        break;
                    case '.':
                        masterAa.setValue(masterAa.getValue() - 100);
                        break;
                    default:
                        break;
                }
                masterAa.setConsidered(true);
            }
        }
        //TODO HERE ADD SOME CODE TO MERGE ALL AND SET IDENTIFIED PIECES?
        return musicScores;
    }

    public static LinkedHashMap<String, StringBuilder> readAlignment(File inputFile) throws IOException {
        LinkedHashMap<String, StringBuilder> allignmentMap = new LinkedHashMap<>();
        //preread the headers to figure out the longest one to parse with
        longestHeaderLength = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("CLUSTAL O") && !line.isEmpty()) {
                    String header = line.split(splitter)[0];
                    if (!header.isEmpty()) {
                        longestHeaderLength = Math.max(longestHeaderLength, header.length() + splitter.length());
                        allignmentMap.put(header, allignmentMap.getOrDefault(header, new StringBuilder()));
                    }
                }
            }
        }
        allignmentMap.put("CONSENSUS", new StringBuilder());
        //read making substrings
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("CLUSTAL O") && !line.isEmpty()) {
                    String header = line.split(splitter)[0];
                    if (header.isEmpty() | line.contains("*") | line.contains(".") | line.contains(":")) {
                        header = "CONSENSUS";
                    }
                    StringBuilder builder = allignmentMap.get(header);
                    builder.append(line.substring(longestHeaderLength));
                }
            }
        }
        return allignmentMap;
    }

    private static int longestHeaderLength = 0;

}
