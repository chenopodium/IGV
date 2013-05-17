/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.handlers;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.batch.CommandExecutorIF;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.RuntimeUtils;
import org.broad.igv.util.StringUtils;
import org.broad.igv.util.collections.LRUCache;

/**
 *
 * @author Chantal
 */
public class IonTorrentCommandExecutor implements CommandExecutorIF{

    private static Logger log = Logger.getLogger(IonTorrentCommandExecutor.class);
    private static PreferenceManager prefs = PreferenceManager.getInstance();
    private IGV igv;
    private int sleepInterval = 100;

    public IonTorrentCommandExecutor() {
        igv = IGV.getInstance();
    }

    private List<String> getArgs(String[] tokens) {
        List<String> args = new ArrayList(tokens.length);
        for (String s : tokens) {
            if (s.trim().length() > 0) {
                args.add(s.trim());
            }
        }
        return args;
    }

    public String execute(String command) {
        
        //log.info("Executing "+command);
        List<String> args = getArgs(StringUtils.breakQuotedString(command, ' ').toArray(new String[]{}));


        String result = CommandExecutorIF.NOTHANDLED;

        try {
            if (args.size() > 0) {
                String cmd = args.get(0).toLowerCase();
                String param1 = args.size() > 1 ? args.get(1) : null;
                String param2 = args.size() > 2 ? args.get(2) : null;
                String param3 = args.size() > 3 ? args.get(3) : null;
                String param4 = args.size() > 4 ? args.get(4) : null;

                if (cmd.equalsIgnoreCase("echo")) {
                    result = cmd;
                } else if (cmd.equalsIgnoreCase("msg")) {
                    String msg = param1;
                    if (param2 != null) {
                        msg += "\n" + param2;
                    }
                    if (param3 != null) {
                        msg += "\n" + param3;
                    }
                    if (param4 != null) {
                        msg += "\n" + param4;
                    }
                    MessageUtils.showMessage("Got msg: " + msg);
                    result = param1;
                } else if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit")) {
                    boolean ok = MessageUtils.confirm("IGV was asked to exit. Is that ok?");

                    if (ok) {
                        try {
                            IGV.getInstance().saveStateForExit();
                            Frame mainFrame = IGV.getMainFrame();
                            // Hide and close the application
                            mainFrame.setVisible(false);
                            mainFrame.dispose();
                        } finally {
                            System.exit(0);
                        }
                    }
                } else if (cmd.equalsIgnoreCase("set")) {
                    String parts[] = param1.split("=", 2);
                    String key = "";
                    String value = "";
                    if (parts.length > 1) {
                        key = parts[0];
                        value = parts[1];
                    }
                    if (param2 != null) {
                        value += " " + param2;
                    }
                    if (param3 != null) {
                        value += " " + param3;
                    }
                    if (param4 != null) {
                        value += " " + param4;
                    }
                    if (key != null && value != null && key.length() > 0) {
                        if (IGV.DEBUG) {
                            MessageUtils.showMessage("CommandExecutor: Setting " + key + "=" + value);
                        }
                        if (prefs.contains(key)) {
                            //     log.info("Set: regular preference: put("+key+ ","+value+")");
                            prefs.put(key, value);
                            prefs.putTemp(key, value);
                            // checking for some of the settings
                            if (key.equalsIgnoreCase(PreferenceManager.SHOW_ATTRIBUTE_VIEWS_KEY)) {
                                log.info("Dealing with " + key);
                                IGV.getInstance().doShowAttributeDisplay(value.equalsIgnoreCase("true"));
                            }
                        }
                        log.info("Set: putTemp ("+key+ ","+value+")");
                        prefs.putTemp(key, value);
                        result = value;
                    } else {
                        result = "set requires 2 parameters, but I got: " + param1;
                    }
                } else if (cmd.equalsIgnoreCase("get")) {
                    log.info("Get, param1: " + param1);
                    if (param1 != null) {
                        result = prefs.getTemp(param1);
                        if (result == null) {
                            result = " ";
                        }

                    } else {
                        result = "Need to specify a variable";
                    }
                } else if (cmd.equalsIgnoreCase("goto")) {
                    result = goto1(args);
                } else if (cmd.equalsIgnoreCase("gototrack")) {
                    boolean res = IGV.getInstance().scrollToTrack(param1);
                    result = res ? "OK" : String.format("Error: Track %s not found", param1);
                } else if (cmd.equalsIgnoreCase("setSleepInterval")) {
                    return this.setSleepInterval(param1);                
                } else {
                    if (command.indexOf("=") > 0) {
                        log.info("Could be a set command: " + command);
                        return this.execute("set " + command);
                    } else {                        
                        return "NOTHANDLED";
                    }
                }
            } else {
                return "Empty command string";
            }

            Runnable r = new AfterCommandRunnable(command);
            r.run();
        } catch (Exception e) {
            log.error(e);
            result = "Error: " + e.getMessage();
        }
        
        return result;
    }

    private class AfterCommandRunnable implements Runnable {

        private String command;

        private AfterCommandRunnable(String command) {
            this.command = command;

        }

        @Override
        public void run() {
            if (!command.startsWith("set ")) {
                igv.doRefresh();
            }
            if (RuntimeUtils.getAvailableMemoryFraction() < 0.5) {
                log.debug("Clearing caches");
                LRUCache.clearCaches();
            }
            if (!command.contains("nosleep")) {
                if (sleepInterval > 0 && !command.startsWith("set ")) {

                    try {
                        Thread.currentThread().sleep(sleepInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private String setSleepInterval(String param1) {
        try {
            sleepInterval = Integer.parseInt(param1.trim());
            return "OK";
        } catch (NumberFormatException e) {
            return "ERROR - sleep interval value ('" + param1 + ".) must be an integer number";
        }
    }

    private String goto1(List<String> args) {
        if (args == null || args.size() < 2) {
            return "ERROR: missing locus parameter";
        }
        String locus = args.get(1);
        for (int i = 2; i < args.size(); i++) {
            locus += (" " + args.get(i));
        }
        igv.goToLocus(locus);
        return "OK";
    }
}
