/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */
package org.broad.igv.ui.action;

import com.iontorrent.utils.StringTools;
import java.awt.BorderLayout;
import org.apache.log4j.Logger;
import org.broad.igv.ui.IGV;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import org.broad.igv.Globals;
import org.broad.igv.batch.CommandExecutor;
import org.broad.igv.ui.WaitCursorManager;

public class RunCommandMenuAction extends MenuAction {

    static Logger log = Logger.getLogger(RunCommandMenuAction.class);
    IGV mainFrame;

    public RunCommandMenuAction(String label, int mnemonic, IGV mainFrame) {
        super(label, null, mnemonic);
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand().equalsIgnoreCase("run commands...")) {
            final JTextArea area = new JTextArea();
            area.setRows(10);
            area.setColumns(80);
            String help = getHelpString();
            area.setToolTipText("<html>" + help + "</html>");

            JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add("Center", new JScrollPane(area));
            p.add("North", new JLabel("<html>Examples:<br>goto chr1:1234-5432msg hello<br>set foo=bar</html>"));
            int ans = JOptionPane.showConfirmDialog(IGV.getMainFrame(), p, "Enter commands", JOptionPane.OK_CANCEL_OPTION);
            if (ans == JOptionPane.OK_OPTION) {
                SwingWorker worker = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        String res = runCommands(area.getText());
                        area.setText(res);

                        JOptionPane.showMessageDialog(IGV.getMainFrame(), area, "Result of commands", JOptionPane.INFORMATION_MESSAGE);
                        return null;
                    }
                };

                worker.execute();

            }
        }
    }

    private String ul(String help, String name, String desc) {
        return help + "<li><b>" + name + ":</b> " + desc + "</li>";
    }

    private String runCommands(String text) {

        CommandExecutor cmdExe = new CommandExecutor();
        // Globals.setSuppressMessages(true);
        Globals.setBatch(true);

        text = text.replace(";", "\n");
        ArrayList<String> lines = StringTools.parseList(text, "\n");

        String res = "";
         WaitCursorManager.CursorToken cursorToken = null;
          cursorToken = WaitCursorManager.showWaitCursor();
        for (String line : lines) {
            if (line != null && line.length() > 0) {
                if (!(line.startsWith("#") || line.startsWith("//"))) {
                    line = line.replace("=", " ");
                    line = line.replace(",", " ");

                    log.info("Executing Command: " + line);
                    String s = cmdExe.execute(line);
                    res += line + ": ";
                    if (s != null) {
                        res += s;
                    } else {
                        res += "no result";
                    }
                    res += "\n";
                }
            }
        }
        if (cursorToken != null) WaitCursorManager.removeWaitCursor(cursorToken);
        Globals.setSuppressMessages(false);
        Globals.setBatch(false);
        return res;

    }

    private String getHelpString() {
        String help = "Available commands:<ul>";
        help = ul(help, "genome", "load a genome");
        help = ul(help, "gototrack <trackname>", "load a track");
        help = ul(help, "loadfile <file>", "load a file");
        help = ul(help, "region <chr:start-end>", "define a region");
        help = ul(help, "collapse <track>", "collapse a track");
        help = ul(help, "setCredentials <username> <pw>", "");
        help = ul(help, "clearCredentials", "");
        help = ul(help, "version", "");
        help = ul(help, "msg <text>", "");
        help = ul(help, "new or reset or clear", "new session");
        help = ul(help, "sort <sortarg> <locus> ", "");
        help = ul(help, "set <name> <value> ", "define a parameter");
        help += "</ul>";
        return help;
    }
}
