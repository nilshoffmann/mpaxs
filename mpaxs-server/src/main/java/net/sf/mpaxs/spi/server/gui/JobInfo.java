/*
 * Mpaxs, modular parallel execution system. 
 * Copyright (C) 2010-2012, The authors of Mpaxs. All rights reserved.
 *
 * Project website: http://mpaxs.sf.net
 *
 * Mpaxs may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Mpaxs, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 * 
 * Mpaxs is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package net.sf.mpaxs.spi.server.gui;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.spi.server.Host;
import net.sf.mpaxs.spi.server.MasterServer;
import net.sf.mpaxs.api.job.Status;

/**
 *
 * @author Kai Bernd STadermann
 */
public class JobInfo extends javax.swing.JFrame {

    private MasterServer master;
    private UUID jobID;
    private ScheduledExecutorService scheduler = Executors.
            newScheduledThreadPool(1);

    public JobInfo(String name) {
        initComponents();
        configName.setText(name);
        jobMessage.setText(
                "This Job is in an error state. Please check your configuration file!");
        status.setText(Status.ERROR.toString());
        hostField.setText("");
        cancelButton.setEnabled(false);
        this.setVisible(true);
    }

    /** Creates new form JobInfo */
    public JobInfo(UUID jobID, MasterServer master) {
        initComponents();
        this.master = master;
        this.jobID = jobID;
        progressBar.setMaximum(0);
        progressBar.setMaximum(100);
        initialSetup();
        this.setVisible(true);
        if (!scheduler.isShutdown()) {
            permanentTextfieldUpdates();
        }
    }

    private void initialSetup() {
        IJob job = master.findJob(jobID);
        setupStaticTextFields(job);
        updateTextFields(job);
    }

    private void setupStaticTextFields(IJob job) {
        jobIDField.setText(jobID.toString());
        if (job.getConfigurationFile() != null || !job.getConfigurationFile().
                isEmpty()) {
            configName.setText(new File(job.getConfigurationFile()).getName());
        }

        className.setText(job.getClassToExecute().getClass().getSimpleName());
    }

    private void updateTextFields(final IJob job) {
        Runnable r = new Runnable() {

            Status jobStatus = job.getStatus();

            @Override
            public void run() {
                switch (jobStatus) {
                    case WAITING:
                        status.setText(Status.WAITING.toString());
                        hostField.setText("");
                        cancelButton.setEnabled(true);
                        showResults.setEnabled(false);
                        jobMessage.setText("");
                        break;
                    case RUNNING:
                        status.setText(Status.RUNNING.toString());
                        Host host = master.getHostJobIsRunningOn(jobID);
                        if (host != null) {
                            hostField.setText(host.getIP());
                            progressBar.setValue(master.getJobProgress(jobID).
                                    getProgressValue());
                        }
                        cancelButton.setEnabled(true);
                        showResults.setEnabled(false);
                        jobMessage.setText("");
                        break;
                    case DONE:
                        status.setText(Status.DONE.toString());
                        hostField.setText("");
                        progressBar.setValue(100);
                        cancelButton.setEnabled(false);
                        showResults.setEnabled(true);
                        jobMessage.setText("");
                        scheduler.shutdown();
                        break;
                    case ERROR:
                        status.setText(Status.ERROR.toString());
                        hostField.setText("");
                        progressBar.setValue(0);
                        cancelButton.setEnabled(false);
                        showResults.setEnabled(false);
                        jobMessage.setText(
                                "This Job is in an error state. Please check log.xml and your implementation!");
                        scheduler.shutdown();
                        break;
                    case CANCELED:
                        status.setText(Status.CANCELED.toString());
                        hostField.setText("");
                        progressBar.setValue(0);
                        cancelButton.setEnabled(false);
                        showResults.setEnabled(false);
                        jobMessage.setText("This Job has been canceled.");
                        scheduler.shutdown();
                        break;
                }
            }
        };
        SwingUtilities.invokeLater(r);
        RepaintManager.currentManager(this).markCompletelyDirty(status);
        RepaintManager.currentManager(this).markCompletelyDirty(hostField);
        RepaintManager.currentManager(this).markCompletelyDirty(progressBar);
        RepaintManager.currentManager(this).markCompletelyDirty(jobMessage);
    }

    private void permanentTextfieldUpdates() {
        scheduler.scheduleAtFixedRate(
                new Runnable() {

                    @Override
                    public void run() {
                        IJob job = master.findJob(jobID);
                        setupStaticTextFields(job);
                        updateTextFields(job);
                    }
                }, 0, 1, TimeUnit.SECONDS);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jobIDField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        configName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        className = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        status = new javax.swing.JTextField();
        hostField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jLabel7 = new javax.swing.JLabel();
        jobMessage = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        showResults = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Job information");
        setResizable(false);

        jLabel1.setText("ID:");

        jobIDField.setEditable(false);

        jLabel2.setText("Configuration file name:");

        configName.setEditable(false);

        jLabel3.setText("Class to execute:");

        className.setEditable(false);

        jLabel4.setText("Status:");

        jLabel5.setText("Running on host:");

        status.setEditable(false);

        hostField.setEditable(false);

        jLabel6.setText("Progress:");

        jLabel7.setText("Job message:");

        jobMessage.setEditable(false);

        cancelButton.setText("cancel job");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        showResults.setText("show result");
        showResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showResultsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                            .addComponent(jLabel3)
                            .addComponent(configName, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                            .addComponent(className, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                            .addComponent(jobIDField, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jobMessage)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel7)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel4)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(hostField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(status, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 377, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(showResults, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(showResults)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jobIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(configName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(className, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jobMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel6))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelButton.setEnabled(false);
        master.cancelJob(jobID);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void showResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showResultsActionPerformed
        IJob job = master.findJob(jobID);
        try {
            new ShowResult(job.getClassToExecute().get().toString());
        } catch (InterruptedException ex) {
            Logger.getLogger(JobInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(JobInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_showResultsActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField className;
    private javax.swing.JTextField configName;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField jobIDField;
    private javax.swing.JTextField jobMessage;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton showResults;
    private javax.swing.JTextField status;
    // End of variables declaration//GEN-END:variables
}
