/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */


package org.broad.igv.ui.util;

import com.jidesoft.swing.JideBoxLayout;
import org.apache.log4j.Logger;
import org.broad.igv.ui.FontManager;

import javax.swing.*;
import java.text.NumberFormat;
import java.awt.*;
import java.util.TimerTask;


/**
 * @author eflakes
 */
public class ApplicationStatusBar extends JPanel { //StatusBar {

    static Logger log = Logger.getLogger(ApplicationStatusBar.class);
    private JLabel messageBox;
    private JLabel messageBox2;
    private JLabel messageBox3;
    private JLabel memoryStatus;

    private boolean info32shown;
    private boolean infoshown;
    private boolean memWarningshown;
    java.util.Timer timer;
    private static int M = 1000000;

    public ApplicationStatusBar() {
        info32shown = false;
        memWarningshown = false;
        initialize();
    }

    private void initialize() {

        setBackground(new Color(240, 240, 240));
        Color messageBG = new Color(230, 230, 230);
        Font messageFont = FontManager.getFont(11);

        setMinimumSize(new Dimension(200, 20));
        setPreferredSize(new Dimension(800, 20));

        JideBoxLayout layout = new JideBoxLayout(this, JideBoxLayout.X_AXIS);
        layout.setGap(3);
        setLayout(layout);

        messageBox = createMessageField(messageBG, messageFont);
        messageBox.setMinimumSize(new Dimension(165, 10));
        messageBox.setPreferredSize(new Dimension(165, 20));
        add(messageBox, JideBoxLayout.FIX);

        messageBox2 = createMessageField(messageBG, messageFont);
        messageBox2.setMinimumSize(new Dimension(150, 10));
        messageBox2.setPreferredSize(new Dimension(150, 20));
        add(messageBox2, JideBoxLayout.FIX);

        messageBox3 = createMessageField(messageBG, messageFont);
        messageBox3.setMinimumSize(new Dimension(165, 10));
        messageBox3.setPreferredSize(new Dimension(165, 20));
        add(messageBox3, JideBoxLayout.VARY);

        memoryStatus = createMessageField(messageBG, messageFont);
        memoryStatus.setPreferredSize(new Dimension(100, 20));
        memoryStatus.setMinimumSize(new Dimension(100, 10));
        memoryStatus.setBackground(messageBG);
        add(memoryStatus, JideBoxLayout.FIX);

        MemoryUpdateTask updateTask = new MemoryUpdateTask(memoryStatus);
        timer = new java.util.Timer();
        timer.schedule(updateTask, 0, 5000);


    }

    public void setMessage(final String message) {
        UIUtilities.invokeOnEventThread(new Runnable() {
            public void run() {
                messageBox.setText(message);
                messageBox.paintImmediately(messageBox.getBounds());
            }
        });
    }

    public void setMessage2(final String message) {
        UIUtilities.invokeOnEventThread(new Runnable() {
            public void run() {
                messageBox2.setText(message);
                messageBox2.paintImmediately(messageBox2.getBounds());
            }
        });
    }


    private JLabel createMessageField(Color bg, Font font) {
        JLabel messageField = new JLabel();
        messageField.setBackground(bg);
        messageField.setFont(font);
        messageField.setBorder(BorderFactory.createLineBorder(Color.black));
        return messageField;

    }


    class MemoryUpdateTask extends TimerTask {

        JLabel textField;
        NumberFormat format;

        public MemoryUpdateTask(JLabel textField) {
            this.textField = textField;
            format = NumberFormat.getIntegerInstance();
        }

        @Override
        public void run() {
            Runtime runtime = Runtime.getRuntime();
            int freeMemory = (int) (runtime.freeMemory() / M);
            int totalMemory = (int) (runtime.totalMemory() / M);
            int usedMemory = (totalMemory - freeMemory);
            int maxMemory = (int)(runtime.maxMemory()/M) ;
            String um = format.format(usedMemory);
            String tm = format.format(totalMemory);
            String arch = System.getProperty("sun.arch.data.model");
            if (!infoshown) {
                Logger.getLogger("ApplicationStatusBar").info("arch="+arch+", max mem: "+maxMemory+", used="+usedMemory+", total="+totalMemory+", free="+freeMemory);
                infoshown = true;
            }
            if (!info32shown && maxMemory < 1600) {
                
                // user is running Java 32 bit!
                MessageUtils.showMessage("You seem to run Java <b>32 bit</b>. This means IGV has little memory to use. "
                        + "<br>If you run into performance problems, <b>please install Java 64 bit (and browser)</b> and <b>disable/remove Java 32</b>.<br>"
                        + "Alternatively, you can <b>save</b> the igv.jnlp file, and then start it from the <b>file explorer</b> in case you <b>do</b> have Java 64 bit.");
                info32shown = true;
            }
            if (! memWarningshown && maxMemory - usedMemory < 50) {
                // warning
                 MessageUtils.showMessage("Your memory is running low. This might cause IGV to hang - in case you are working with large data sets, consider loading less data.<br>"
                         + "In case you are running Java 32 bit, please upgrade the browser and Java to 64 bit.");
                memWarningshown = true;
            }
            textField.setText(um + "M of " + tm + "M");
        }

    }
}