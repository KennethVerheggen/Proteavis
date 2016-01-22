package com.compomics.proteavis.view.impl;

import com.compomics.proteavis.logic.input.ClustalMSAFileImport;
import com.compomics.proteavis.logic.processing.listener.GameEventListener;
import com.compomics.proteavis.logic.processing.sequencer.ProteinSequencer;
import com.compomics.proteavis.logic.processing.synchronizer.SequenceSynchronizer;
import com.compomics.proteavis.logic.input.ProteinHighlightImport;
import com.compomics.proteavis.logic.input.ProteinInput;
import com.compomics.proteavis.logic.processing.synchronizer.GUISynchronizer;
import com.compomics.proteavis.logic.sound.MidiFactory;
import com.compomics.proteavis.logic.sound.impl.CombinedMusicGenerator;
import com.compomics.proteavis.model.AminoAcidResult;
import com.compomics.proteavis.model.ProteinMusicScore;
import com.compomics.proteavis.model.SoundAlphabet.SoundAlphabetType;
import com.compomics.proteavis.model.enums.Instrument;
import com.compomics.proteavis.view.Visualiser;
import com.compomics.proteavis.view.game.panels.impl.ZenVisualisationPanel;
import com.compomics.proteavis.view.model.VisualisationTableModel;
import com.compomics.proteavis.view.panel.CustomSequenceDialog;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import jm.music.data.Score;

/**
 * GUI to convert a protein into a MIDI string and visualize it
 *
 * @author Kenneth Verheggen
 */
public class ExplosionVisualiser extends javax.swing.JFrame implements Visualiser {

    /**
     * a filechooser for fasta
     */
    private JFileChooser fastaFileChooser;
    /**
     * a filechooser for evidence
     */
    private JFileChooser evidenceFileChooser;
    /**
     * a filechooser for output midi file
     */
    private JFileChooser saveFileChooser;
    /**
     * a filechooser for clustal output file
     */
    private JFileChooser clustalFileChooser;
    /**
     * The current music scores
     */
    private ArrayList<ProteinMusicScore> proteinMusicScores;
    /**
     * the list of amino acids for the currently selected protein
     */
    private LinkedList<AminoAcidResult> aminoAcidResults;
    /**
     * a the protein sequencer
     */
    private ProteinSequencer sequencer;
    /**
     * boolean indicating if the player was initialized
     */
    private boolean initialized = false;
    /**
     * boolean indicating if the player is playing
     */
    private boolean play = false;
    /**
     * the base value for the heatmap
     */
    private double baseValue = 50.0;
    /**
     * The maximal value for the progress bar
     */
    private long maxValue = 100;
    /**
     * The object that generates the music scores
     */
    private CombinedMusicGenerator musicScoreGenerator;
    /**
     * THe music score that will be played as a MIDI
     */
    private Score score;
    /**
     * The listener for input events
     */
    private GameEventListener gameEventListener;
    /**
     * The alphabet type for the amino acid ordering
     */
    private SoundAlphabetType alphabetType = SoundAlphabetType.LETTER;
    /**
     * The synchronizer to match midi events with the gui updates
     */
    private GUISynchronizer GUIsynchronizer;
    /**
     * The selected file for Clustal imports
     */
    private File selectedFile;
    /**
     * The protein sequence input map
     */
    private TreeMap<String, String> proteinSequenceMap;

    /**
     * The peptide instrument
     */
    private Instrument peptideInstrument = Instrument.VIBRAPHONE;

    public static void main(String[] args) {
        ExplosionVisualiser proteinMusicVisualiser = new ExplosionVisualiser();
        proteinMusicVisualiser.setVisible(true);
    }

    /**
     * Sets up the protein visualiser
     */
    public ExplosionVisualiser() {
        UIManager.put("ProgressBar.selectionForeground", Color.black);
        UIManager.put("ProgressBar.selectionBackground", Color.black);
        // set up the GUI
        initComponents();
        gamePanel.setZenMode(true);
        gamePanel.setVisualiser(this);
        //set visible to get dimensions straightened
        setVisible(true);
        // set additional GUI properties
        setAdditionalGuiProperties();
        //init combo box model
        initComboToTable();
        //init effects
        initEffects();
        //init repainting
        initRepainting();
    }

    private void initRepainting() {
        GUIsynchronizer = GUISynchronizer.getInstance();
        GUIsynchronizer.setComponents(jLayeredPane1);
        GUIsynchronizer.start();
    }

    private void initEffects() {
        fireworksPanel.init(gamePanel.getSize());
        gamePanel.setFireWorksPanel(fireworksPanel);
        soundProgressBar.setValue(0);
        soundProgressBar.setString("");
    }

    private void initComboToTable() {
        VisualisationTableModel vtm = new VisualisationTableModel((DefaultTableModel) allignmentTable.getModel());
        allignmentTable.setModel(vtm);
        TableColumn instrumentColumn = allignmentTable.getColumnModel().getColumn(1);
        JComboBox comboBox = new JComboBox();
        comboBox.setModel(getInstrumentModel());
        instrumentColumn.setCellEditor(new DefaultCellEditor(comboBox));
        TableColumn gradeColumn = allignmentTable.getColumnModel().getColumn(2);
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(false);
        gradeColumn.setCellEditor(new DefaultCellEditor(checkBox));
    }

    /**
     * Set up some additional GUI properties.
     */
    private void setAdditionalGuiProperties() {
        tableScrollPane.getViewport().setOpaque(false);
        setLocationRelativeTo(null);
        // locate the dialog in the middle of the screen
        setLocationRelativeTo(null);
    }

    /**
     *
     * @return a defaultcomboboxmodel with the available instruments
     */
    private DefaultComboBoxModel getInstrumentModel() {
        DefaultComboBoxModel instrumentModel = new DefaultComboBoxModel();
        for (Instrument instrument : Instrument.values()) {
            instrumentModel.addElement(instrument);
        }
        return instrumentModel;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        singleValuesJPanel = new javax.swing.JPanel();
        playButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        soundProgressBar = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableScrollPane = new javax.swing.JScrollPane();
        allignmentTable = new javax.swing.JTable();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        gamePanel = new com.compomics.proteavis.view.game.panels.impl.ZenVisualisationPanel();
        fireworksPanel = new com.compomics.proteavis.view.game.panels.FireworksPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        fastaMenuItem = new javax.swing.JMenuItem();
        evidenceMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        clustalMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        customImport = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        saveMenu = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        rbRandomLoc = new javax.swing.JRadioButtonMenuItem();
        rbRandomColor = new javax.swing.JRadioButtonMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        miSetPepInstrument = new javax.swing.JMenuItem();
        miSetAminoAcidGrouping = new javax.swing.JMenuItem();
        miExamples = new javax.swing.JMenu();
        miPlayExample = new javax.swing.JMenuItem();
        miPlayTNF = new javax.swing.JMenuItem();
        miPlayBActin = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Proteavis - Protein AudioVisual Symphony");

        backgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        singleValuesJPanel.setOpaque(false);

        playButton.setText("PLAY");
        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playButtonActionPerformed(evt);
            }
        });

        stopButton.setText("STOP");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        soundProgressBar.setBackground(new java.awt.Color(0, 153, 255));
        soundProgressBar.setFont(new java.awt.Font("DotumChe", 3, 18)); // NOI18N
        soundProgressBar.setForeground(new java.awt.Color(255, 255, 204));
        soundProgressBar.setString("");
        soundProgressBar.setStringPainted(true);

        jScrollPane1.setBorder(null);

        tableScrollPane.setBackground(new java.awt.Color(255, 255, 255));
        tableScrollPane.setBorder(null);

        allignmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Protein", "Instrument", " "
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableScrollPane.setViewportView(allignmentTable);

        jScrollPane1.setViewportView(tableScrollPane);

        gamePanel.setBackground(new java.awt.Color(0, 0, 0));
        gamePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Game"));

        javax.swing.GroupLayout gamePanelLayout = new javax.swing.GroupLayout(gamePanel);
        gamePanel.setLayout(gamePanelLayout);
        gamePanelLayout.setHorizontalGroup(
            gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 535, Short.MAX_VALUE)
        );
        gamePanelLayout.setVerticalGroup(
            gamePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 380, Short.MAX_VALUE)
        );

        fireworksPanel.setBackground(new java.awt.Color(102, 153, 0));
        fireworksPanel.setLayout(null);

        jLayeredPane1.setLayer(gamePanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(fireworksPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fireworksPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(gamePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(fireworksPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(gamePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout singleValuesJPanelLayout = new javax.swing.GroupLayout(singleValuesJPanel);
        singleValuesJPanel.setLayout(singleValuesJPanelLayout);
        singleValuesJPanelLayout.setHorizontalGroup(
            singleValuesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(singleValuesJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(singleValuesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(singleValuesJPanelLayout.createSequentialGroup()
                        .addComponent(soundProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(stopButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(playButton))
                    .addGroup(singleValuesJPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLayeredPane1)))
                .addContainerGap())
        );

        singleValuesJPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {playButton, stopButton});

        singleValuesJPanelLayout.setVerticalGroup(
            singleValuesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(singleValuesJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(singleValuesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLayeredPane1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(singleValuesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(soundProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(playButton)
                    .addComponent(stopButton))
                .addGap(8, 8, 8))
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(singleValuesJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(singleValuesJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jMenu1.setText("File");

        fastaMenuItem.setText("Import FASTA...");
        fastaMenuItem.setToolTipText("Import a FASTA file containing protein sequences");
        fastaMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastaMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(fastaMenuItem);

        evidenceMenuItem.setText("Import Peptide Highlights...");
        evidenceMenuItem.setToolTipText("Import a file that contains peptide sequences and related values (for example identification scores)");
        evidenceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evidenceMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(evidenceMenuItem);
        jMenu1.add(jSeparator2);

        clustalMenuItem.setText("Import Clustal...");
        clustalMenuItem.setToolTipText("Imports Clustal Omega results");
        clustalMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clustalMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(clustalMenuItem);
        jMenu1.add(jSeparator1);

        customImport.setText("Import Custom Sequence...");
        customImport.setToolTipText("Imports a custom sequence");
        customImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customImportActionPerformed(evt);
            }
        });
        jMenu1.add(customImport);
        jMenu1.add(jSeparator3);

        saveMenu.setText("Export Sound to MIDI...");
        saveMenu.setToolTipText("Exports the currently selected proteins to a MIDI file");
        saveMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuActionPerformed(evt);
            }
        });
        jMenu1.add(saveMenu);
        jMenu1.add(jSeparator4);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(exitMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Options");

        rbRandomLoc.setText("Random explosion locations");
        rbRandomLoc.setToolTipText("Randomize the location of explosions");
        rbRandomLoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbRandomLocActionPerformed(evt);
            }
        });
        jMenu2.add(rbRandomLoc);

        rbRandomColor.setText("Random explosion colors");
        rbRandomColor.setToolTipText("Randomize the explosion color");
        rbRandomColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbRandomColorActionPerformed(evt);
            }
        });
        jMenu2.add(rbRandomColor);
        jMenu2.add(jSeparator5);

        miSetPepInstrument.setText("Set peptide instrument");
        miSetPepInstrument.setToolTipText("Set the instrument used to play peptide highlights (if applicable)");
        miSetPepInstrument.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSetPepInstrumentActionPerformed(evt);
            }
        });
        jMenu2.add(miSetPepInstrument);

        miSetAminoAcidGrouping.setText("Set amino acid grouping");
        miSetAminoAcidGrouping.setToolTipText("Set the strategy to group amino acids on property. This will determine the coloring scheme");
        miSetAminoAcidGrouping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSetAminoAcidGroupingActionPerformed(evt);
            }
        });
        jMenu2.add(miSetAminoAcidGrouping);

        jMenuBar1.add(jMenu2);

        miExamples.setText("Examples");

        miPlayExample.setText("Insulin (human)");
        miPlayExample.setToolTipText("Play an example sequence");
        miPlayExample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miPlayExampleActionPerformed(evt);
            }
        });
        miExamples.add(miPlayExample);

        miPlayTNF.setText("P53 (human)");
        miPlayTNF.setToolTipText("Play an example sequence");
        miPlayTNF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miPlayTNFActionPerformed(evt);
            }
        });
        miExamples.add(miPlayTNF);

        miPlayBActin.setText("B-Actin");
        miPlayBActin.setToolTipText("Play an example sequence");
        miPlayBActin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miPlayBActinActionPerformed(evt);
            }
        });
        miExamples.add(miPlayBActin);

        jMenuBar1.add(miExamples);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void evidenceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evidenceMenuItemActionPerformed
        evidenceFileChooser = new JFileChooser();
        evidenceFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (evidenceFileChooser.getCurrentDirectory() == null) {
            evidenceFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        evidenceFileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".tsv");
            }

            @Override
            public String getDescription() {
                return "Evidence TSV File";
            }
        });
        int result = evidenceFileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = evidenceFileChooser.getSelectedFile();
            evidenceFileChooser.setCurrentDirectory(selectedFile.getParentFile());
            try {
                ProteinHighlightImport.addFileToComposition(selectedFile);
            } catch (IOException ex) {
                //show an error dialog
                JOptionPane.showConfirmDialog(null, "Could not read file \n" + ex, "Highlight import error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_evidenceMenuItemActionPerformed

    private void fastaMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastaMenuItemActionPerformed
        fastaFileChooser = new JFileChooser();
        fastaFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (fastaFileChooser.getCurrentDirectory() == null) {
            fastaFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        fastaFileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() | f.getName().toLowerCase().endsWith(".fasta");
            }

            @Override
            public String getDescription() {
                return "UniProt FASTA File";
            }
        });
        int result = fastaFileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fastaFileChooser.getSelectedFile();
            fastaFileChooser.setCurrentDirectory(selectedFile.getParentFile());
            try {
                ProteinInput.loadFasta(selectedFile, ">");
                proteinSequenceMap = ProteinInput.getProteinMap();
                updateProteinMap(false);
            } catch (IOException ex) {
                //show an error dialog
                JOptionPane.showConfirmDialog(null, "Could not read file \n" + ex, "Fasta import error", JOptionPane.ERROR_MESSAGE);

            }
        }
    }//GEN-LAST:event_fastaMenuItemActionPerformed
    /*
    Updates the protein list
     */
    private void updateProteinMap(boolean clustal) {
        //disable checkboxes
        TableColumn gradeColumn = allignmentTable.getColumnModel().getColumn(2);
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(true);
        checkBox.setEnabled(!clustal);
        gradeColumn.setCellEditor(new DefaultCellEditor(checkBox));
        DefaultTableModel model = (DefaultTableModel) allignmentTable.getModel();
        model.setRowCount(0);
        for (String aProtein : proteinSequenceMap.keySet()) {
            if (!aProtein.equalsIgnoreCase("CONSENSUS")) {
                model.addRow(new Object[]{aProtein, Instrument.ACCORDION, clustal});
            }
        }
        allignmentTable.setModel(model);
        repaint();
    }

    /**
     * Retrieves the selected proteins to play
     *
     * @return a list of selected proteins
     */
    private HashMap<String, Instrument> getProteinSelection() {
        HashMap<String, Instrument> proteins = new HashMap<>();
        DefaultTableModel model = (DefaultTableModel) allignmentTable.getModel();
        for (int row = 0; row < model.getRowCount(); row++) {
            if ((boolean) model.getValueAt(row, 2)) {
                proteins.put(model.getValueAt(row, 0).toString(), (Instrument) model.getValueAt(row, 1));
            }
        }
        return proteins;
    }

    /**
     * prepares the visual content for the sequence panel
     */
    private void prepareVisuals(HashMap<String, Instrument> proteins) {
        proteinMusicScores = new ArrayList<>();
        HashMap<String, HashMap<String, Double>> highlightedPeptideMap = ProteinHighlightImport.getHighlightedPeptideMap();

        double minValue = Double.MAX_VALUE;
        double maxValue = 0;
        for (Map.Entry<String, Instrument> protein : proteins.entrySet()) {
            String proteinSequence = proteinSequenceMap.get(protein.getKey());

            ProteinMusicScore background = new ProteinMusicScore(protein.getKey(), proteinSequence, protein.getValue(), peptideInstrument);
            for (Map.Entry<String, Double> aPeptide : highlightedPeptideMap.getOrDefault(protein, new HashMap<>()).entrySet()) {
                background.addIdentification(aPeptide.getKey(), aPeptide.getValue());
                if (minValue > aPeptide.getValue()) {
                    minValue = aPeptide.getValue();
                } else if (maxValue < aPeptide.getValue()) {
                    maxValue = aPeptide.getValue();
                }
            }
            proteinMusicScores.add(background);
        }
        fireworksPanel.setMinValue(minValue);
        fireworksPanel.setMaxValue(maxValue);
        //      paintAll(getGraphics());
    }

    /**
     * prepares the audio content for the sequence panel
     */
    private void initializeMidi() throws MidiUnavailableException {
        try {
            musicScoreGenerator = new CombinedMusicGenerator(
                    "Protein_Soundtrack",
                    alphabetType);
            MidiFactory.createMusicScore(proteinMusicScores, musicScoreGenerator);
            aminoAcidResults = musicScoreGenerator.getAminoAcidPresses();
            score = musicScoreGenerator.getScore();
            sequencer = new ProteinSequencer();
            Sequence sequence = MidiSystem.getSequence(MidiFactory.getMidiInputStream(score));
            SequenceSynchronizer sequenceSynchronizer = new SequenceSynchronizer(sequence, aminoAcidResults, gamePanel.getHeight());
            gamePanel.setSoundAlphabetType(alphabetType);
            gameEventListener = new GameEventListener(this, alphabetType);
            gameEventListener.setGameEnginePanel(gamePanel);
            sequencer.setSequence(sequence, gameEventListener);
            //sequencer.setLoop();
            setSoundBarMax(aminoAcidResults.size());
        } catch (IOException | MidiUnavailableException | InvalidMidiDataException ex) {
            JOptionPane.showConfirmDialog(null, "Could not construct midi sequence \n" + ex, "Midi creation error", JOptionPane.ERROR_MESSAGE);

        }
    }

    public void play(HashMap<String, Instrument> proteinSelection) {
        if (!proteinSelection.isEmpty()) {
            if (!play) {
                playButton.setText("PAUSE");
                if (!initialized) {
                    prepareVisuals(proteinSelection);
                    try {
                        initializeMidi();
                    } catch (MidiUnavailableException ex) {
                        JOptionPane.showConfirmDialog(null, "Midi could not be played back \n" + ex, "Playback error", JOptionPane.ERROR_MESSAGE);
                    }
                    initialized = true;
                }
                sequencer.play();
            } else {
                sequencer.pause();
                playButton.setText("PLAY ");
            }
            play = !play;
        }
    }

    private void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
        play(getProteinSelection());
    }//GEN-LAST:event_playButtonActionPerformed

    /**
     * Stops the current sequencer from playing
     */
    public void stop() {
        if (sequencer != null) {
            sequencer.reset();
        }
        initialized = false;
        playButton.setText("PLAY");
        play = false;
        resetGamePanel();
    }

    private void resetGamePanel() {
        gamePanel.reset();
        soundProgressBar.setValue(0);
        soundProgressBar.setString("");
    }

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        stop();
    }//GEN-LAST:event_stopButtonActionPerformed

    private void saveMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuActionPerformed
        if (score == null) {
            return;
        }
        if (!getProteinSelection().isEmpty()) {
            saveFileChooser = new JFileChooser() {
                @Override
                public void approveSelection() {
                    File f = getSelectedFile();
                    if (getDialogType() == SAVE_DIALOG) {
                        if (f.exists()) {
                            int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                            switch (result) {
                                case JOptionPane.YES_OPTION: {
                                    try {
                                        prepareVisuals(getProteinSelection());
                                        initializeMidi();
                                        MidiFactory.save(score, f);
                                    } catch (IOException | MidiUnavailableException ex) {
                                        JOptionPane.showConfirmDialog(null, "Could not save file \n" + ex, "Midi export error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                return;
                                case JOptionPane.NO_OPTION:
                                    return;
                                case JOptionPane.CLOSED_OPTION:
                                    return;
                                case JOptionPane.CANCEL_OPTION:
                                    cancelSelection();
                                    return;
                            }
                        } else {
                            try {
                                prepareVisuals(getProteinSelection());
                                initializeMidi();
                                MidiFactory.save(score, f);
                                return;
                            } catch (IOException | MidiUnavailableException ex) {
                                JOptionPane.showConfirmDialog(null, "Could not save file \n" + ex, "Midi export error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            };
            saveFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File suggestedFile = new File(saveFileChooser.getCurrentDirectory(), "My_awesome_protein_music.mid");
            int counter = 1;
            while (suggestedFile.exists()) {
                suggestedFile = new File(suggestedFile.getParentFile(), "My_awesome_protein_music.mid".replace("music.mid", "music_" + counter + ".mid"));
                counter++;
            }
            saveFileChooser.setSelectedFile(suggestedFile);
            saveFileChooser.showSaveDialog(this);
        }
    }//GEN-LAST:event_saveMenuActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void clustalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clustalMenuItemActionPerformed
        DefaultTableModel model = (DefaultTableModel) allignmentTable.getModel();
        model.setRowCount(0);
        clustalFileChooser = new JFileChooser();
        clustalFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (clustalFileChooser.getCurrentDirectory() == null) {
            clustalFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        clustalFileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() | f.getName().toLowerCase().endsWith(".clustal");
            }

            @Override
            public String getDescription() {
                return "Clustal result File";
            }
        });
        int result = clustalFileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = clustalFileChooser.getSelectedFile();
            clustalFileChooser.setCurrentDirectory(selectedFile.getParentFile());
            try {
                HashMap<String, StringBuilder> temp = ClustalMSAFileImport.readAlignment(selectedFile);
                proteinSequenceMap = new TreeMap<>();
                for (Map.Entry<String, StringBuilder> aString : temp.entrySet()) {
                    proteinSequenceMap.put(aString.getKey(), aString.getValue().toString());
                }
            } catch (IOException ex) {
                JOptionPane.showConfirmDialog(null, "Could not read clustal file \n" + ex, "Clustal import error", JOptionPane.ERROR_MESSAGE);

            }
            updateProteinMap(true);
        }
    }//GEN-LAST:event_clustalMenuItemActionPerformed

    private void rbRandomLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbRandomLocActionPerformed
        gamePanel.setRandomPosition(rbRandomLoc.isSelected());
    }//GEN-LAST:event_rbRandomLocActionPerformed

    private void rbRandomColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbRandomColorActionPerformed
        gamePanel.setRandomColor(rbRandomColor.isSelected());
    }//GEN-LAST:event_rbRandomColorActionPerformed

    private void miSetPepInstrumentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSetPepInstrumentActionPerformed
        Object[] possibilities = Instrument.values();
        Instrument instrument = (Instrument) JOptionPane.showInputDialog(
                this,
                "Select the instrument to play highlighted peptides\n\n"
                + "Current selection : " + peptideInstrument + "\n\n",
                "Peptide Instrument",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                Instrument.VIBRAPHONE);
        if ((instrument != null)) {
            peptideInstrument = instrument;
        }
    }//GEN-LAST:event_miSetPepInstrumentActionPerformed

    private void miSetAminoAcidGroupingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSetAminoAcidGroupingActionPerformed
        Object[] possibilities = SoundAlphabetType.values();
        SoundAlphabetType tempType = (SoundAlphabetType) JOptionPane.showInputDialog(
                this,
                "Select the grouping strategy for amino acids\n\n"
                + "Current selection : " + this.alphabetType + "\n\n",
                "Grouping Strategy",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                Instrument.VIBRAPHONE);
        if ((tempType != null)) {
            alphabetType = tempType;
        }
    }//GEN-LAST:event_miSetAminoAcidGroupingActionPerformed

    private void miPlayExampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miPlayExampleActionPerformed
        proteinSequenceMap = new TreeMap<>();
        proteinSequenceMap.put("example_insulin", "MALWMRLLPLLALLALWGPDPAAAFVNQH"
                + "LCGSHLVEALYLVCGERGFFYTPKTRREAEDLQGSLQPLALEGSLQKRGIVEQCCTSICSL"
                + "YQLENYCN");
        HashMap<String, Instrument> proteinSelection = new HashMap<>();
        proteinSelection.put("example_insulin", Instrument.BAGPIPES);
        play(proteinSelection);
    }//GEN-LAST:event_miPlayExampleActionPerformed

    private void miPlayTNFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miPlayTNFActionPerformed
        proteinSequenceMap = new TreeMap<>();
        proteinSequenceMap.put("example_insulin",
                "MEEPQSDPSVEPPLSQETFSDLWKLLPENNVLSPLPSQAMDDLMLSPDDIEQWFTEDPGP"
                + "DEAPRMPEAAPPVAPAPAAPTPAAPAPAPSWPLSSSVPSQKTYQGSYGFRLGFLHSGTAK"
                + "SVTCTYSPALNKMFCQLAKTCPVQLWVDSTPPPGTRVRAMAIYKQSQHMTEVVRRCPHHE"
                + "RCSDSDGLAPPQHLIRVEGNLRVEYLDDRNTFRHSVVVPYEPPEVGSDCTTIHYNYMCNS"
                + "SCMGGMNRRPILTIITLEDSSGNLLGRNSFEVRVCACPGRDRRTEEENLRKKGEPHHELP"
                + "PGSTKRALPNNTSSSPQPKKKPLDGEYFTLQIRGRERFEMFRELNEALELKDAQAGKEPG"
                + "GSRAHSSHLKSKKGQSTSRHKKLMFKTEGPDSD");
        HashMap<String, Instrument> proteinSelection = new HashMap<>();
        proteinSelection.put("example_insulin", Instrument.BAGPIPES);
        play(proteinSelection);
    }//GEN-LAST:event_miPlayTNFActionPerformed

    private void miPlayBActinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miPlayBActinActionPerformed
        proteinSequenceMap = new TreeMap<>();
        proteinSequenceMap.put("example_insulin",
                "MDDDIAALVVDNGSGMCKAGFAGDDAPRAVFPSIVGRPRHQGVMVGMGQKDSYVGDEAQS"
                + "KRGILTLKYPIEHGIVTNWDDMEKIWHHTFYNELRVAPEEHPVLLTEAPLNPKANREKMT"
                + "QIMFETFNTPAMYVAIQAVLSLYASGRTTGIVMDSGDGVTHTVPIYEGYALPHAILRLDL"
                + "AGRDLTDYLMKILTERGYSFTTTAEREIVRDIKEKLCYVALDFEQEMATAASSSSLEKSY"
                + "ELPDGQVITIGNERFRCPEALFQPSFLGMESCGIHETTFNSIMKCDVDIRKDLYANTVLS"
                + "GGTTMYPGIADRMQKEITALAPSTMKIKIIAPPERKYSVWIGGSILASLSTFQQMWISKQ"
                + "EYDESGPSIVHRKCF");
        HashMap<String, Instrument> proteinSelection = new HashMap<>();
        proteinSelection.put("example_insulin", Instrument.BAGPIPES);
        play(proteinSelection);
    }//GEN-LAST:event_miPlayBActinActionPerformed

    private void customImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customImportActionPerformed
        CustomSequenceDialog customSequenceDialog = new CustomSequenceDialog(this, true);
        customSequenceDialog.setLocationRelativeTo(this);
        customSequenceDialog.setVisible(true);
        if (!customSequenceDialog.isCancelled()) {
            if (proteinSequenceMap == null) {
                proteinSequenceMap = new TreeMap<>();
            }
            proteinSequenceMap.put(customSequenceDialog.getAccession(), customSequenceDialog.getSequence());
            updateProteinMap(false);
        }
    }//GEN-LAST:event_customImportActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable allignmentTable;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JMenuItem clustalMenuItem;
    private javax.swing.JMenuItem customImport;
    private javax.swing.JMenuItem evidenceMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem fastaMenuItem;
    private com.compomics.proteavis.view.game.panels.FireworksPanel fireworksPanel;
    private com.compomics.proteavis.view.game.panels.impl.ZenVisualisationPanel gamePanel;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JMenu miExamples;
    private javax.swing.JMenuItem miPlayBActin;
    private javax.swing.JMenuItem miPlayExample;
    private javax.swing.JMenuItem miPlayTNF;
    private javax.swing.JMenuItem miSetAminoAcidGrouping;
    private javax.swing.JMenuItem miSetPepInstrument;
    private javax.swing.JButton playButton;
    private javax.swing.JRadioButtonMenuItem rbRandomColor;
    private javax.swing.JRadioButtonMenuItem rbRandomLoc;
    private javax.swing.JMenuItem saveMenu;
    private javax.swing.JPanel singleValuesJPanel;
    private javax.swing.JProgressBar soundProgressBar;
    private javax.swing.JButton stopButton;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Retrieves the version number set in the pom file.
     *
     * @return the version number of JSparklines
     */
    public String getVersion() {

        java.util.Properties p = new java.util.Properties();

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("jsparklines.properties");
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return p.getProperty("jsparklines.version");

    }

    public ZenVisualisationPanel getGamePanel() {
        return gamePanel;
    }

    /**
     * Sets the maximal value for the progress bar
     *
     * @param maxTick the maximal value
     */
    @Override
    public void setSoundBarMax(long maxTick) {
        this.maxValue = maxTick;
    }

    /**
     * Sets the current value for the progress bar
     *
     * @param tick the current value
     */
    @Override
    public void setSoundBarValue(long tick) {
        int value = (int) (100 * ((double) tick / (double) maxValue));
        soundProgressBar.setValue(value);
        //  soundProgressBar.repaint();
    }

    /**
     * Sets the current value for the progress bar
     *
     * @param tick the current value
     * @param aminoAcid the current amino acid
     */
    @Override
    public void setSoundBarValue(long tick, char aminoAcid) {
        int value = (int) (100 * ((double) tick / (double) maxValue));
        soundProgressBar.setValue(value);
        String progressBarText = soundProgressBar.getString();
        if (progressBarText == null) {
            progressBarText = "";
        }
        if (progressBarText.length() > 100) {
            //remove the first, add to the end
            progressBarText = progressBarText.substring(1);
        }
        if (aminoAcid != '!') {
            progressBarText += aminoAcid;
        } else {
            progressBarText += " ";
        }
        soundProgressBar.setString(progressBarText);
        //    soundProgressBar.repaint();
    }

    @Override
    public void addNote(Character aa, double highlightValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
