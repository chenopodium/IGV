/*
*	Copyright (C) 2011 Life Technologies Inc.
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 2 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
* 
*  @Author Chantal Roth
*/
package com.iontorrent.views.basic;

import com.iontorrent.event.ActionContext;
import com.iontorrent.event.BasicAction;
import com.iontorrent.event.ContextActionHandlerIF;
import com.iontorrent.utils.ToolBox;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.border.*;

/**
 * @Author Chantal Roth @Date 7/25/2000 This class has some basic functionality,
 * such as notifying listeners, getting application properties and preferences,
 * getting default borders for panels, setting the look and feel and displaying
 * cursors The idea is to extend this class if any of these functionalities are
 * required, especially the event handling.
 */
public abstract class BasicPanel extends JPanel {

    protected JFrame parentframe = null;
    private ContextActionHandlerIF handler = null;
    private Vector actions;
    private int popup_x = 0;
    private int popup_y = 0;
    protected JPopupMenu popup = null;
    private int workingCursorRequests = 0;
    // *****************************************************************
    // DEBUG VARIABLES
    // *****************************************************************
    protected static boolean DEBUG = false;
    // *****************************************************************
    // GUI STUFF
    // *****************************************************************
    protected static Font boldFont = new Font("SansSerif", Font.BOLD, 12);
    protected static SimpleDateFormat format = new SimpleDateFormat(
            "MM/dd/yyyy h:mm a");
    // *****************************************************************
    // OTHER
    // *****************************************************************
    protected String viewer_name;

    // --------------------------------------------------------------------
    // *****************************************************************
    // CONSTRUCTOR
    // *****************************************************************
    protected BasicPanel(String viewer_name, JFrame parent) {
        super();
        this.viewer_name = viewer_name;
        this.parentframe = parent;


    }

    protected void warn(String string) {
        Logger.getLogger(viewer_name).warning(viewer_name + ": " + string);

    }

    protected BasicPanel(String viewer_name) {
        this(viewer_name, null);
    }

    protected BasicPanel(JFrame parent) {
        this("BasicPanel", parent);
    }

    protected BasicPanel() {
        this("BasicPanel", null);
    }
    // *****************************************************************
    // GET/SET
    // *****************************************************************

    public int getPopupX() {
        return popup_x;
    }

    public int getPopupY() {
        return popup_y;
    }

    public void setPopup(JPopupMenu popup) {
        this.popup = popup;
    }

    public void addAction(BasicAction action) {
        if (actions == null) {
            actions = new Vector();
        }
        actions.add(action);
    }

    public static void msg(String msg) {
        showMessage(msg, "Message");
    }

    public static void showMessage(String msg, String title) {
        showMessage(msg, title, false);
    }

    public static boolean getYes(String msg, String title) {
        //msg = "<html>" + msg; // + "</html>";
        int answer =
                JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
        return answer == JOptionPane.OK_OPTION;
    }

    public static void showMessage(String msg, String title, boolean containsHtml) {
        //Log.info("msg: " + msg);
        if (!containsHtml) {
            msg = ToolBox.makeHtmlCompatible(msg);

        }
        if (!msg.trim().toUpperCase().startsWith("<HTML>")) {
            msg = "<html>" + msg + "</html>";
        }

        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(String msg) {
        showError(msg, "Error", false);
    }

    public static void showError(String msg, String title) {
        showError(msg, title, false);
    }

    public static void showError(String msg, String title, boolean containsHtml) {
        if (!containsHtml) {
            msg = ToolBox.makeHtmlCompatible(msg);
        }
        if (!msg.trim().toUpperCase().startsWith("<HTML>")) {
            msg = "<html>" + msg + "</html>";
        }
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    // *****************************************************************
    // ACTIONS
    // *****************************************************************
    public static Border getBorder(String name) {
        Border border1 = BorderFactory.createEtchedBorder(Color.white, new java.awt.Color(148, 145, 140));
        return new TitledBorder(border1, name);

    }

    public static Border getLoweredBorder(String name) {
        Border border =
                new TitledBorder(new BevelBorder(BevelBorder.LOWERED), name, TitledBorder.LEFT, TitledBorder.TOP, boldFont);
        return border;
    }

    public static Border getRaisedBorder(String name) {
        Border border =
                new TitledBorder(
                new EtchedBorder(EtchedBorder.RAISED),
                name,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                boldFont);
        return border;
    }

    public class PopupListener
            extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
            requestFocus();
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            JPopupMenu totalpopup = null;
            if (e.isPopupTrigger() && (handler != null || actions != null)) {
                totalpopup = new JPopupMenu("totalactions");
                int total = 0;
                for (int i = 0; actions != null && i < actions.size(); i++) {
                    totalpopup.add((Action) actions.get(i));
                    total++;
                }

                if (handler != null) {
                    ActionContext ctx = new ActionContext(e.getX(), e.getY());
                    ArrayList<Action> moreactions = handler.getContextSensitiveActions(ctx);
                    if (moreactions != null && moreactions.size() > 0) {
                        totalpopup.addSeparator();
                    }
                    for (int i = 0; moreactions != null && i < moreactions.size(); i++) {
                        BasicAction a = (BasicAction) moreactions.get(i);
                        totalpopup.add(a);
                        total++;
                    }
                }

                if (total > 0) {
                    totalpopup.show(e.getComponent(),
                            e.getX(), e.getY());
                    popup_x = e.getX();
                    popup_y = e.getY();

                }

            }
        }
    }

    public void dataChanged() {
        p("handle any data change events");
    }

    // *****************************************************************
    // DEBUG
    // *****************************************************************
    protected void p(String s) {
        //	System.out.println(viewer_name + ":" + s);
        if (DEBUG) {
            Logger.getLogger(viewer_name).info(viewer_name + ":" + s);
            //       System.out.println(viewer_name+":"+s);

        }

    }

    protected void p(String s, boolean force) {
        if (DEBUG || force) {
            Logger.getLogger(viewer_name).info(viewer_name + ":" + s);

        }
    }

    protected void err(String s) {
        Logger.getLogger(viewer_name).warning(viewer_name + ":" + s);

    }

    protected void err(Exception e) {
        Logger.getLogger(viewer_name).warning(e.getMessage());

    }

    public ContextActionHandlerIF getHandler() {
        return handler;
    }

    public void setHandler(ContextActionHandlerIF handler) {
        this.handler = handler;
    }
}
