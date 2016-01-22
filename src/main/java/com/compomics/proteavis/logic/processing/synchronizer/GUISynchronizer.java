package com.compomics.proteavis.logic.processing.synchronizer;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 *
 * @author compomics
 */
public class GUISynchronizer implements Runnable {

    private JComponent[] panelsToSynchronize;
    private Thread updatingThread;
    private static GUISynchronizer instance;

    private GUISynchronizer() {

    }

    public static GUISynchronizer getInstance() {
        if (instance == null) {
            instance = new GUISynchronizer();
        }
        return instance;
    }

    public void start() {
        if (updatingThread == null) {
            updatingThread = new Thread(this,"GUI-sync");
        }
        updatingThread.start();
    }

    public void stop() {
        if (updatingThread == null) {
            updatingThread.interrupt();
        }
        updatingThread = null;
    }

    public void setComponents(JComponent... componentsToSynchronize) {
        this.panelsToSynchronize = componentsToSynchronize;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(33);
            } catch (InterruptedException ex) {
                Logger.getLogger(GUISynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                for (JComponent aComponent : panelsToSynchronize) {
                    aComponent.repaint();
                }
            }
        }
    }

}
