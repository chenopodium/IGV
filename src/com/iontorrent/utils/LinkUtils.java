/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.utils;

import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.wellmodel.WellFlowDataResult;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.broad.igv.PreferenceManager;
import org.broad.igv.ui.IGV;

/**
 *
 * @author Chantal Roth
 */
public class LinkUtils {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LinkUtils.class);
    
    public static boolean linkToTSL(String readnames, String chromosome, long location) {
        String server = PreferenceManager.getInstance().get(PreferenceManager.IONTORRENT_SERVER);
        String res = PreferenceManager.getInstance().get(PreferenceManager.IONTORRENT_RESULTS);
        String bam = null;
        if (res.endsWith(".bam")) {
            bam = res;
            File f = new File(bam);
            res = f.getParent().toString();
        }
        if (server == null || server.length() < 1) {
            server = JOptionPane.showInputDialog(IGV.getMainFrame(), "I am not sure which Torrent server to use. \nPlease enter the url (example: myserver.com)");
            p("Got no server, using user input " + server);
        }

        if (!server.startsWith("http")) {
            server = "http://" + server;
        }

        String searchterm = res;
        if (searchterm.endsWith("/")) {
            searchterm = searchterm.substring(0, searchterm.length() - 1);
        }
        int last = searchterm.lastIndexOf("/");
        if (last > 0 && last + 1 < searchterm.length()) {
            searchterm = searchterm.substring(last);
        }
        String url = server + "/TSL?restartApplication&searchdb=" + searchterm;
        if (res != null && res.length() > 0) {
            url += "&res_dir=" + res;
        }
        if (bam != null && bam.length() > 0) {
            url += "&bam=" + bam;
        }

        if (readnames != null && readnames.length() > 0) {
            url += "&read_names=" + readnames;
        }
        if (chromosome != null && chromosome.length() > 0) {
            url += "&chromosome=" + chromosome;
        }
        if (location > 0) {
            url += "&location=" + location;
        }

        JTextField txt = new JTextField();
        txt.setText(url);
        if (!java.awt.Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(IGV.getMainFrame(), txt, "Please open a browser and paste the url below:", JOptionPane.OK_OPTION);
            return false;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        try {

            java.net.URI uri = new java.net.URI(url);
            desktop.browse(uri);

            JOptionPane.showMessageDialog(IGV.getMainFrame(), "When TSL opens, check the folder settings to make sure it got the right experiment", "Check Folders", JOptionPane.OK_OPTION);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(IGV.getMainFrame(), txt, "Please open a browser and paste the url below:", JOptionPane.OK_OPTION);
        }
        return false;
    }

    private static void p(String msg) {
        log.info(msg);
    }

    private static void err(String msg) {
        log.error(msg);
    }
}
