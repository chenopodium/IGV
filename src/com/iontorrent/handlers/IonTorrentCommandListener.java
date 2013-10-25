/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.handlers;

import com.iontorrent.prefs.IonTorrentPreferencesManager;
import java.awt.Frame;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.batch.CommandExecutor;
import org.broad.igv.batch.CommandListener;
import org.broad.igv.batch.CommandListenerIF;
import org.broad.igv.ui.IGV;
import org.broad.igv.util.LongRunningTask;

/**
 *
 * @author Chantal
 */
public class IonTorrentCommandListener implements CommandListenerIF{

    
    private static IonTorrentCommandListener itlistener;
    
    
    private static final String CRNL = "\r\n";
    private static final String CONTENT_TYPE = "Content-Type: ";
    private static final String HTTP_RESPONSE = "HTTP/1.1 200 OK";
    private static final String HTTP_NO_RESPONSE = "HTTP/1.1 204 No Response";
    private static final String CONTENT_LENGTH = "Content-Length: ";
    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    private static final String CONNECTION_CLOSE = "Connection: close";
    private static final String NO_CACHE = "Cache-Control: no-cache, no-store";
    /**
     * CR: to allow the control of the IGV client from a JavaScript method in a
     * browser
     */
    private static final String ACCESSCONTROL = "Access-Control-Allow-Origin: *";

   
    public void sendHTTPResponse(PrintWriter out, String result) {

        out.println(result == null ? HTTP_NO_RESPONSE : HTTP_RESPONSE);
        if (result != null) {
            out.print(CONTENT_TYPE + CONTENT_TYPE_TEXT_HTML);
            out.print(CRNL);
            out.print(CONTENT_LENGTH + (result.length()));
            out.print(CRNL);
            out.print(NO_CACHE);
            out.print(CRNL);
            out.print(CONNECTION_CLOSE);
            out.print(CRNL);
            /**
             * CR: to allow the control of the IGV client from a JavaScript
             * method in a browser
             */
            out.print(ACCESSCONTROL);
            out.print(CRNL);
            out.print(CRNL);
            out.print(result);
            out.print(CRNL);
        }
        out.close();
    }

    /** Returns OK if the result has been dealt with or null if it has already sent the response,
     * and returns NOTHANDLED if it has not processsed this command */
    
    @Override
    public String processGet(String command, Map<String, String> params, CommandExecutor cmdExe, PrintWriter out) throws IOException {

        String result = CommandListenerIF.NOTHANDLED;
        
        
        if (command.equals("/load")) {
            String file = params.get("file");
            if (file == null) {
                file = params.get("bigDataURL"); // <- UCSC track line
            }
            if (file == null) {
                file = params.get("sessionURL");  // <- older IGV option
            }
            if (file == null) {
                file = params.get("dataURL"); // <- Another UCSC option
            }

            if (file != null) {
                PreferenceManager.getInstance().put(IonTorrentPreferencesManager.IONTORRENT_RESULTS, file);
                String genome = params.get("genome");
                if (genome == null) {
                    genome = params.get("db");  // <- UCSC track line param
                }
                if (genome != null) {
                    genome = URLDecoder.decode(genome, "UTF-8");
                    if (IGV.getInstance().getSelectableGenomeIDs().contains(genome)) {
                        IGV.getInstance().selectGenomeFromList(genome);
                    } else {
                        IGV.getInstance().loadGenome(genome.trim(), null);
                    }
                }

                String mergeValue = params.get("merge");
                if (mergeValue != null) {
                    mergeValue = URLDecoder.decode(mergeValue, "UTF-8");
                }


                // Default for merge is "false" for session files,  "true" otherwise
                boolean merge;
                if (mergeValue != null) {
                    // Explicit setting
                    merge = mergeValue.equalsIgnoreCase("true");
                } else if (file.endsWith(".xml") || file.endsWith(".php") || file.endsWith(".php3")) {
                    // Session file
                    merge = false;
                } else {
                    // Data file
                    merge = true;
                }

                String name = params.get("name");

                String locus = params.get("locus");
                if (locus != null) {
                    locus = URLDecoder.decode(locus, "UTF-8");
                }
                result = "OK (executing load " + file + " in thread)";
                sendHTTPResponse(out, result);
                result = null;
                LongRunningTask.submit(new CommandExecutor.LoadRunnable(file, locus, merge, name, params, cmdExe));
                // result = file;//cmdExe.loadFiles(file, locus, merge, name, params);

            } else {
                return ("ERROR Parameter \"file\" is required");
            }
        } else if (command.equals("/reload") || command.equals("/goto") || command.equals("goto")) {
            String locus = params.get("locus");           
            result = "OK (executing " + command + " in thread)";
            sendHTTPResponse(out, result);
            result = null;
            LongRunningTask.submit(new GotoRunnable(locus));

        } else {
            if (command.startsWith("/")) {
                command = command.substring(1);
            }
            // now add parameters!
            Iterator it = params.keySet().iterator();
            String args = "";
            for (; it.hasNext();) {
                String key = (String) it.next();
                String val = params.get(key);
                args += " " + key;
              //  p("Adding param " + key + " to args");
                if (val != null) {
                    args += "=" + val;
                }
              //  p("Adding =" + val + " to args");
            }
            
            result = "OK (executing " + command + " in thread)";
            String cmd = command + args;
            if (cmd.startsWith("get")) {
                result = cmdExe.execute(command + args);
            } else {
                result = "OK (executing load " + command + " in thread)";
                getListener().sendHTTPResponse(out, result);
                result = null;
                LongRunningTask.submit(new OtherRunnable(command + args, cmdExe));
            }
        }

        return result;
    }

    private class OtherRunnable implements Runnable {

        private String cmd;
        private CommandExecutor cmdExe;

        private OtherRunnable(String cmd, CommandExecutor cmdExe) {
            this.cmd = cmd;
            this.cmdExe = cmdExe;
        }

        @Override
        public void run() {
            cmdExe.execute(cmd);
        }
    }

    private class GotoRunnable implements Runnable {

        private String locus;

        private GotoRunnable(String locus) {
            this.locus = locus;
        }

        @Override
        public void run() {
            // p("Goto: starting goToLocus");
            IGV.getInstance().goToLocus(locus);
        }
    }

    public static CommandListener getListener() {
        return CommandListener.getListener();
    }

    private void p(String s) {
        Logger.getLogger(IonTorrentCommandListener.class).info(s);
    }
}
