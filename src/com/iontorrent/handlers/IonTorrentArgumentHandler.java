/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.handlers;

import com.iontorrent.utils.Encryptor;
import com.iontorrent.utils.ErrorHandler;
import jargs.gnu.CmdLineParser;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.batch.CommandListener;
import org.broad.igv.ui.ArgumentHandler;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.Main;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.StringUtils;

/**
 *
 * @author Chantal
 */
public class IonTorrentArgumentHandler implements ArgumentHandler {
    private Main.IGVArgs igvargs;
    private static Logger log = Logger.getLogger(IonTorrentArgumentHandler.class);
    
    @Override
     public void parseArgs(Main.IGVArgs igvargs, String[] nonOptionArgs) {
            this.igvargs = igvargs;
            
            if (nonOptionArgs != null && nonOptionArgs.length > 0) {               
                // Alternative implementation
                String firstArg = StringUtils.decodeURL(nonOptionArgs[0]);
                firstArg = checkEqualsAndExtractParamter(firstArg);
                if (firstArg != null && !firstArg.equals("ignore")) {
                    log.info("session file is: " + firstArg);
                    
                    if (firstArg.endsWith(".xml") || firstArg.endsWith(".php") || firstArg.endsWith(".php3")
                            || firstArg.endsWith(".session")) {
                        igvargs.setSessionFile(firstArg);
                        if (IGV.DEBUG) MessageUtils.showMessage("Got session file "+firstArg);

                    } else {
                        igvargs.setDataFileString(firstArg);
                        
                        if (IGV.DEBUG) MessageUtils.showMessage("Got data file "+firstArg);
                    }
                }

                if (nonOptionArgs.length > 1) {
                    // check if arg contains = for all args
                    for (String arg : nonOptionArgs) {
                        arg = checkEqualsAndExtractParamter(arg);
                        if (arg != null) {
                            if (!arg.startsWith("http") && arg.length()< 50) {
                                log.info("parseArgs: Got locus string: "+arg);
                                igvargs.setLocusString(arg);                                
                            }
                        }
                    }
                }
            }
        }

    private String checkEqualsAndExtractParamter(String arg) {
            if (arg == null) {
                return null;
            }
            PreferenceManager prefs = PreferenceManager.getInstance();
            int eq = arg.indexOf("=");
            boolean isUrl = arg.startsWith("http:") ||  arg.startsWith("https:");
            if (eq > 0 && ( !isUrl || (isUrl && arg.indexOf("getFile")<0))) {
                // we got a key=value
                String key = arg.substring(0, eq);
                String val = arg.substring(eq + 1);
                
              // log.info("Jnlp Argument: key="+key+", value="+val);
               if (key.startsWith("session") || key.equalsIgnoreCase("file")) {
                    if (val.endsWith(".xml") || val.endsWith(".php") || val.endsWith(".php3")
                            || val.endsWith(".session")) {
                        log.info("Got session: " + key + "=" + val);
                        igvargs.setSessionFile(val);
                        if (IGV.DEBUG) MessageUtils.showMessage("Got sesson File "+val);

                    } else {
                        log.info("Got dataFileString: " + key + "=" + val);
                        if (IGV.DEBUG) MessageUtils.showMessage("Got data File "+val);
                         igvargs.setDataFileString(val);
                    }
                    PreferenceManager.getInstance().putTemp(key, val);
                    return null;
                } else if (key.equalsIgnoreCase("batchFile") || key.equalsIgnoreCase("batch")) {
                    log.info("Got batch file: " + key + "=" + val);
                    igvargs.setBatchFile(val);
                    if (IGV.DEBUG) MessageUtils.showMessage("Got batchFile  "+val);
                    PreferenceManager.getInstance().putTemp(key, val);
                    return null;
                } else if (key.equalsIgnoreCase("signature") || key.equalsIgnoreCase("hashcode")) {
                    log.info("Got hash code/signature for session resources" + key + "=" + val);                    
                   // if (IGV.DEBUG) MessageUtils.showMessage("Got signature  "+val);
                    PreferenceManager.getInstance().putTemp("signature", val);
                    return null;
                } else if (key.equalsIgnoreCase("locus") || key.equalsIgnoreCase("position")) {
                    log.info("Got locus: " + key + "=" + val);
                    igvargs.setLocusString(val);
                    arg = val;
                } else if (key.equalsIgnoreCase(PreferenceManager.PORT_NUMBER)) {
                    log.info("+++++++++ setting PORT_NUNBER: " + key+"="+val);
                    PreferenceManager.getInstance().put(PreferenceManager.PORT_ENABLED, true);
                    PreferenceManager.getInstance().put(PreferenceManager.PORT_NUMBER, val);
                    this.igvargs.setPort(val);
                    return null;      
                } else {
                    log.info("Adding to preferences TMP: set " + key + "=" + val);
                    if (IGV.DEBUG) MessageUtils.showMessage("Currently not handled specifically, adding it to preferences: "+ key + "=" + val);
                    if (prefs.contains(key)) {
                        prefs.put(key, val);
                    //    log.info("Got general preference setting: " + key + "=" + val);
                    }
                  //  else log.info("Got temp preference setting: " + key + "=" + val);
                    if (key.equalsIgnoreCase("header_value")) {
                        String algo = Encryptor.getDefaultAlgorithm();                   
                        try {
                            String token = Encryptor.decrypt(algo, val, "IGVKEY123");
                            log.info("Got token from args: "+token);
                        } catch (Throwable e) {
                            log.info("ArgumentHandler: Could not decrypt: " + ErrorHandler.getString(e));                            
                        }                            
                     }
                    PreferenceManager.getInstance().putTemp(key, val);                   
                    return null;                             
                }
            }
            return arg;
        }
}
