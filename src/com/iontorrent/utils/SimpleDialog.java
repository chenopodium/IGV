/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.apache.log4j.Logger;

/**
 *
 * @author Chantal Roth
 */
public class SimpleDialog extends JDialog {
  
    private static Logger log = Logger.getLogger(SimpleDialog.class);
     
    
    
    public SimpleDialog(String title, JPanel mainpanel, int width, int height, int x, int y, Image image, boolean modal) {
        setLocationRelativeTo(null);
        this.setUndecorated(false);
        if (image != null) this.setIconImage(image);
        JPanel main = new JPanel(new BorderLayout());
        super.setTitle(title);
        this.add(main);
        this.setModal(modal);
        main.add(mainpanel, BorderLayout.CENTER);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        
        if (x <= 0) x = (int) Math.max(100, screen.getWidth() / 2 - 400);
        if (y <= 0) y = (int) Math.max(100, screen.getHeight() / 2 - 200);
        this.setLocation(x, y);
        
        width = (int) Math.min(screen.getWidth(), width-200);
        height = (int) Math.min(screen.getHeight()-200, height);
        this.setSize(width, height);
        this.toFront();       
        this.setVisible(true);
    }
     private void p(String msg) {
        log.info(msg);
    }
}
