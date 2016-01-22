package com.compomics.proteavis.view.model;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author compomics
 */
public class VisualisationTableModel extends DefaultTableModel {

    private static final List<String> columnNames = Arrays.asList(new String[]{"Protein", "Instrument", " "});

    public VisualisationTableModel(DefaultTableModel model) {
        super(model.getDataVector(), new Vector(columnNames));
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 2) {
            return Boolean.class;
        } else {
            return super.getColumnClass(column);
        }
    }

}
