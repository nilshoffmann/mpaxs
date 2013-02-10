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

import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.UUID;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.sf.mpaxs.api.job.IJob;
import net.sf.mpaxs.spi.server.Host;
import net.sf.mpaxs.spi.server.MasterServer;
import net.sf.mpaxs.spi.server.messages.IComputeHostEventListener;
import net.sf.mpaxs.api.event.IJobEventListener;
import net.sf.mpaxs.spi.server.settings.Settings;
import net.sf.mpaxs.spi.server.messages.IReceiver;
import net.sf.mpaxs.api.job.Status;

/**
 *
 * @author Kai Bernd Stadermann
 */
public class MainFrame implements IReceiver, IJobEventListener,
        IComputeHostEventListener {

    private MasterServer master;
    private Settings settings = Settings.getInstance();
    private DefaultListModel messages = new DefaultListModel();
    private DefaultListModel hosts = new DefaultListModel();
    private DefaultListModel waiting = new DefaultListModel();
    private DefaultListModel running = new DefaultListModel();
    private DefaultListModel done = new DefaultListModel();
    private DefaultListModel failed = new DefaultListModel();
    private Container c = null;

    /** Creates new form MainFrame */
    public MainFrame(MasterServer master, Container c) {
        if (c == null) {
            this.c = new JFrame("Mpaxs MasterServer");
        } else {
            this.c = c;
        }
        this.master = master;
        initComponents();
        master.addListener(this);
        masterServerIP.setText(settings.getLocalIP() + ":" + settings.getLocalPort());
        if (this.c instanceof Window) {
            ((Window) this.c).addWindowListener(new CleanExiting(master, (Window) this.c));
        }
        setUpListSelectionListeners();
        this.c.setVisible(true);
        
    }

    private void setUpListSelectionListeners() {
        computeHostList.addMouseListener(new MouseListener(ListType.HOSTS, this));
        waitingJobs.addMouseListener(new MouseListener(ListType.WAITING, this));
        runningJobs.addMouseListener(new MouseListener(ListType.RUNNING, this));
        doneJobs.addMouseListener(new MouseListener(ListType.DONE, this));
        failedJobs.addMouseListener(new MouseListener(ListType.FAILED, this));
    }

    public void updateFailedJobs(String name) {
        failed.addElement(name);
    }
    
    @Override
    public void hostAdded(Host host) {
        hosts.addElement(host);
    }

    @Override
    public void hostRemoved(Host host) {
        hosts.removeElement(host);
    }

    @Override
    public void jobChanged(final IJob job) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                Status status = job.getStatus();
                switch (status) {
                    case WAITING:
                        running.removeElement(job.getId());
                        waiting.addElement(job.getId());
                        break;
                    case RUNNING:
                        waiting.removeElement(job.getId());
                        running.addElement(job.getId());
                        break;
                    case DONE:
                        running.removeElement(job.getId());
                        done.addElement(job.getId());
                        break;
                    case ERROR:
                        running.removeElement(job.getId());
                        waiting.removeElement(job.getId());
                        failed.addElement(job.getId());
                        break;
                    case CANCELED:
                        running.removeElement(job.getId());
                        waiting.removeElement(job.getId());
                        failed.addElement(job.getId());
                        break;
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    @Override
    public void newMessage(final String message) {
        messages.addElement(message);
    }

    protected void openComputeHostInfo() {
        int index = computeHostList.getSelectedIndex();
        if (index < hosts.size() && 0 < hosts.size()) {
            new ComputeHostInfo((Host) hosts.get(index), master);
        }
    }

    protected void openJobInfo(ListType type) {
        int index;
        switch (type) {
            case WAITING:
                index = waitingJobs.getSelectedIndex();
                if (index < waiting.size() && 0 < waiting.size()) {
                    new JobInfo((UUID) waiting.get(index), master);
                }
                break;
            case RUNNING:
                index = runningJobs.getSelectedIndex();
                if (index < running.size() && 0 < running.size()) {
                    new JobInfo((UUID) running.get(index), master);
                }
                break;
            case DONE:
                index = doneJobs.getSelectedIndex();
                if (index < done.size() && 0 < done.size()) {
                    new JobInfo((UUID) done.get(index), master);
                }
                break;
            case FAILED:
                index = failedJobs.getSelectedIndex();
                if (index < failed.size() && 0 < failed.size()) {
                    try {
                        UUID id = (UUID) failed.get(index);
                        new JobInfo(id, master);
                    } catch (ClassCastException ex) {
                        String name = (String) failed.get(index);
                        new JobInfo(name);
                    }

                }
                break;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPanel = new javax.swing.JPanel();
        server_ip_label = new javax.swing.JLabel();
        masterServerIP = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        computeHostList = new javax.swing.JList(hosts);
        pathToConfig = new javax.swing.JTextField();
        openFileChooser = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        messageArea = new javax.swing.JList(messages);
        jLabel6 = new javax.swing.JLabel();
        tabPane = new javax.swing.JTabbedPane();
        waitingJobsScrollPane = new javax.swing.JScrollPane();
        waitingJobs = new javax.swing.JList(waiting);
        runningJobsScrollPane = new javax.swing.JScrollPane();
        runningJobs = new javax.swing.JList(running);
        doneJobsScrollPane = new javax.swing.JScrollPane();
        doneJobs = new javax.swing.JList(done);
        failedJobsScrollPane = new javax.swing.JScrollPane();
        failedJobs = new javax.swing.JList(failed);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        server_ip_label.setText("MasterServer IP:Port:");

        masterServerIP.setEditable(false);

        jLabel1.setText("Compute Hosts:");

        computeHostList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(computeHostList);

        pathToConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pathToConfigActionPerformed(evt);
            }
        });

        openFileChooser.setText("...");
        openFileChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileChooserActionPerformed(evt);
            }
        });

        jButton1.setText("Submit Job");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane6.setViewportView(messageArea);

        jLabel6.setText("Recent system messages:");

        waitingJobsScrollPane.setName("Waiting"); // NOI18N

        waitingJobs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        waitingJobsScrollPane.setViewportView(waitingJobs);

        tabPane.addTab("tab1", waitingJobsScrollPane);

        runningJobsScrollPane.setName("Running"); // NOI18N

        runningJobs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        runningJobsScrollPane.setViewportView(runningJobs);

        tabPane.addTab("tab2", runningJobsScrollPane);

        doneJobsScrollPane.setName("Finished"); // NOI18N

        doneJobs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        doneJobsScrollPane.setViewportView(doneJobs);

        tabPane.addTab("tab3", doneJobsScrollPane);

        failedJobsScrollPane.setName("Failed"); // NOI18N

        failedJobs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        failedJobsScrollPane.setViewportView(failedJobs);

        tabPane.addTab("tab4", failedJobsScrollPane);

        tabPane.addTab("Waiting Jobs",waitingJobsScrollPane);
        tabPane.addTab("Running Jobs",runningJobsScrollPane);
        tabPane.addTab("Finished Jobs",doneJobsScrollPane);
        tabPane.addTab("Failed Jobs",failedJobsScrollPane);

        javax.swing.GroupLayout contentPanelLayout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, 0, 167, Short.MAX_VALUE)
                    .addComponent(masterServerIP, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(contentPanelLayout.createSequentialGroup()
                        .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(contentPanelLayout.createSequentialGroup()
                                .addComponent(pathToConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(openFileChooser)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addComponent(jLabel6))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(contentPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(server_ip_label)
                        .addComponent(jLabel1))
                    .addContainerGap(563, Short.MAX_VALUE)))
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contentPanelLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(masterServerIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(pathToConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(openFileChooser)
                        .addComponent(jButton1)))
                .addGap(44, 44, 44)
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(contentPanelLayout.createSequentialGroup()
                        .addComponent(tabPane, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addGap(13, 13, 13))
            .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(contentPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(server_ip_label)
                    .addGap(54, 54, 54)
                    .addComponent(jLabel1)
                    .addContainerGap(570, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(contentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pathToConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pathToConfigActionPerformed
        jButton1ActionPerformed(evt);
    }//GEN-LAST:event_pathToConfigActionPerformed

    private void openFileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileChooserActionPerformed
        new FileChooser(pathToConfig).setVisible(true);
    }//GEN-LAST:event_openFileChooserActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (!pathToConfig.getText().isEmpty()) {
            File job = new File(pathToConfig.getText());
            job.renameTo(new File(settings.getInputDir() + File.separator + job.getName()));
            pathToConfig.setText("");
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList computeHostList;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JList doneJobs;
    private javax.swing.JScrollPane doneJobsScrollPane;
    private javax.swing.JList failedJobs;
    private javax.swing.JScrollPane failedJobsScrollPane;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTextField masterServerIP;
    private javax.swing.JList messageArea;
    private javax.swing.JButton openFileChooser;
    private javax.swing.JTextField pathToConfig;
    private javax.swing.JList runningJobs;
    private javax.swing.JScrollPane runningJobsScrollPane;
    private javax.swing.JLabel server_ip_label;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JList waitingJobs;
    private javax.swing.JScrollPane waitingJobsScrollPane;
    // End of variables declaration//GEN-END:variables

    private void setDefaultCloseOperation(int DO_NOTHING_ON_CLOSE) {
        if (this.c instanceof Window) {
            ((Window)this.c).addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent we) {
                    System.out.println("Closing Window!");
                    master.shutdown();
                }
            });
        }
        if (this.c instanceof JFrame) {
            ((JFrame) this.c).setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
    }

    private Container getContentPane() {
        if (this.c instanceof JFrame) {
            return ((JFrame) this.c).getContentPane();
        }
        return this.c;
    }

    private void pack() {
        if (this.c instanceof JFrame) {
            ((JFrame) this.c).pack();
        }
    }
}
