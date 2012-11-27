/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.utils;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.pgmacquisition.RawType;
import com.iontorrent.wellmodel.WellFlowDataResult;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.*;
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
        
        String expinfo = PreferenceManager.getInstance().get(PreferenceManager.BAM_FILE);        
        ExperimentContext exp = new ExperimentContext();        
        exp.setExperimentInfo(expinfo);
        long id = exp.getId();
        String searchterm = "";
        if (id > 0) {
            searchterm = ""+id;
        }
        else if (exp.getBamFileName() != null) {
            searchterm = exp.getBamFileName();
        }
        else {
            searchterm =exp.getResultsName();
        }
        
        if (server == null || server.length() < 1) {
            server = JOptionPane.showInputDialog(IGV.getMainFrame(), "I am not sure which Torrent server to use. \nPlease enter the url (example: myserver.com)");
            p("Got no server, using user input " + server);
        }

        if (!server.startsWith("http")) {
            server = "http://" + server;
        }
       
        if (searchterm.endsWith("/")) {
            searchterm = searchterm.substring(0, searchterm.length() - 1);
        }
        int last = searchterm.lastIndexOf("/");
        if (last > 0 && last + 1 < searchterm.length()) {
            searchterm = searchterm.substring(last);
        }
        String url = server + "/TSL?restartApplication&searchdb=" + searchterm;        

        if (readnames != null && readnames.length() > 0) {
            url += "&read_names=" + readnames;
        }
        if (chromosome != null && chromosome.length() > 0) {
            url += "&chromosome=" + chromosome;
        }
        if (location > 0) {
            url += "&location=" + location;
        }

        JTextArea txt = new JTextArea();
        txt.setRows(8);
        txt.setColumns(120);
        txt.setText(url);
        
        Dimension dim = new Dimension(600,200);
        txt.setMaximumSize(dim);
        txt.setPreferredSize(dim);
        txt.setWrapStyleWord(false);
        
        if (!java.awt.Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(IGV.getMainFrame(), new JScrollPane(txt), "You can paste the url into a browser:", JOptionPane.OK_OPTION);
            return false;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        try {
            url = StringTools.replace(url,"&", "\n&");
            txt.setText(url);
            int ans =JOptionPane.showConfirmDialog(IGV.getMainFrame(),  new JScrollPane(txt), "Please verify that the experiment is from the specified server and change if necessary",JOptionPane.OK_CANCEL_OPTION );
            if (ans == JOptionPane.CANCEL_OPTION) return false;
            
            url = txt.getText();
            url = StringTools.replace(url,"\n", "&");
            java.net.URI uri = new java.net.URI(url);
            desktop.browse(uri);
            
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(IGV.getMainFrame(),  new JScrollPane(txt), "You can paste the url into a browser:", JOptionPane.OK_OPTION);
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
