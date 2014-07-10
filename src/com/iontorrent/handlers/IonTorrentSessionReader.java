/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.handlers;

import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.io.FileTools;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.session.IGVSessionReader;
import org.broad.igv.session.Session;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.Utilities;
import org.w3c.dom.*;

/**
 *
 * @author Chantal
 */
public class IonTorrentSessionReader extends IGVSessionReader {

    private static Logger log = Logger.getLogger(IonTorrentSessionReader.class);
    private String path;
    private static final boolean DEBUG = true;
    
    public IonTorrentSessionReader(IGV igv) {
        super(igv);
    }

    @Override
    protected String checkAccessToResources(Collection<ResourceLocator> dataFiles, Collection<ResourceLocator>  invalidFiles) {
        if (dataFiles == null) {
            return null;
        }
        String signature = PreferenceManager.getInstance().getTemp("signature");
        if (signature == null) {
            log.warn("got no signature in temp preferences, assuming resources are ok for now");
            return null;
        }
        long lsignature = 0;
        if (signature != null && signature.length() > 0){
            try {
                //lsignature = Long.parseLong(signature);
            }
            catch (Exception e){
                log.warn("Could not interpret signature "+signature);
            }
        }
        long hash = 0;
        for (ResourceLocator file : dataFiles) {
            if (file != null && file.getPath() != null) {
                String path = file.getPath().toLowerCase();
                
                if (path.indexOf("txt.gz")<0) {
                    int h = path.hashCode();
                    hash += h;
                    if (DEBUG)  log.info("Adding sig hash for: "+file.getPath().toLowerCase()+":"+h);
                }
            }
            else log.warn("Got no file or path for resourcelocator: "+file);
        }
        if ( lsignature > 0 && lsignature != hash ) {
            if (DEBUG) log.info("Signature " + signature + "/" + lsignature + " is not equals to hash code " + hash+", or invalid files");

            StringBuilder message = new StringBuilder();
            message.append("<html>Some resources could not be accessed due to security concerns. ");
            // for debugging
            if (DEBUG) {
                message.append("<ul>");
                for (ResourceLocator file : invalidFiles) {
                    if (file.isLocal()) {
                        message.append("<li>");
                        message.append(file.getPath());
                        message.append("</li>");
                    } else {
                        message.append("<li>");
                        message.append(file.getPath());
                        message.append("</li>");
                    }
                }
                message.append("</ul>");
            }
            message.append("<br>It looks like the session.xml file has been tampered with");
            message.append("</html>");

            //log(message);
            //MessageUtils.showMessage(message.toString());
            // XXX TODO to activate, uncommoent below
            //return message.toString();
            return null;
        } else {
            return null;
        }
    }

    @Override
    public boolean loadSession(Session session, String sessionPath, int tries) throws IOException {
        this.path = sessionPath;
        log.info("loadSession, try: " + tries);
        InputStream inputStream = null;

        if (tries > 2) {
            log.info("===== loadSession: Too many tries, failed");
            return false;

        }
        if (tries > 1) {
            PreferenceManager pref = PreferenceManager.getInstance();
            log.info("===== loadSession: More than one try. Will check token and use a default IGV token if there was one");

            String header_value = pref.getTemp("header_value");

            if (header_value != null) {
                header_value = "__IGV__";
                pref.putTemp("header_value", header_value);
                pref.putTemp("header_encrypt", "false");
                // turn off encryption
                log.info("There is a token. Setting it to default " + pref.getTemp("header_value") + ", encrypt is now " + pref.getTempAsBoolean("header_encrypt"));

            }
        }
        try {
            inputStream = new BufferedInputStream(ParsingUtils.openInputStreamGZ(new ResourceLocator(sessionPath)));

        } catch (Exception e0) {
            log.error("loadDocument: Load session error (will try a SECOND time first)", e0);
            try {
                inputStream = new BufferedInputStream(ParsingUtils.openInputStreamGZ(new ResourceLocator(sessionPath)));
            } catch (Exception e) {

                log.error("loadDocument: Load session error (will try to show content)", e);
                // also try to read from input stream to see WHERE the error happened!          
                String content = "";
                String reason = e.getMessage();
                try {
                    if (inputStream != null) {
                        content = FileTools.getIsAsString(inputStream);
                    }
                    if (content != null && content.length() > 0) {
                        log.error("Content: " + content);
                        reason = getMessageForContent(content, sessionPath);
                    } else {
                        log.info("Got no content. Reason is: " + e.getMessage());
                        reason = getMessageForContent(e.getMessage(), sessionPath);
                    }
                } catch (Exception e1) {
                    log.error("loadDocument: Could not read input stream:" + ErrorHandler.getString(e1));
                }
                if(reason == null)  reason = e.getMessage();
                Exception usererror = new Exception(reason);
                usererror.setStackTrace(e.getStackTrace());                
                throw new RuntimeException(usererror);
            }
        }

        return loadSession(inputStream, session, sessionPath, tries++);

    }

    @Override
    protected boolean handleError(Exception e, String path, List<String> errors) {
        String reason = getMessageForContent(e.getMessage(), path);
        if (reason ==null )reason= e.getMessage();
        
        log.info("Handling error: " + e.getMessage() + " -> reason=" + reason);
        if (!errors.contains(reason)) {
            errors.add(reason);
        }
        if (reason != null && reason.indexOf("token") > -1) {
            Exception usererror = new Exception(reason);
            usererror.setStackTrace(e.getStackTrace());
            throw new RuntimeException(usererror);

        }
        return true;
    }

    @Override
    protected Document loadDocument(InputStream inputStream) throws RuntimeException {
        Document document = null;
        try {
            document = Utilities.createDOMDocumentFromXmlStream(inputStream);
        } catch (Exception e) {
            log.error("loadDocument: Load session error (will try to show content)", e);
            // also try to read from input stream to see WHERE the error happened!

            String content = "";
            String reason = e.getMessage();
            try {
                content = FileTools.getIsAsString(inputStream);
                if (content != null && content.length() > 0) {
                    log.error("Content: " + content);
                    reason = getMessageForContent(content, path);
                    // add content

                }
            } catch (Exception e1) {
                log.error("loadDocument: Could not read input stream:" + ErrorHandler.getString(e1));
            }
            Exception usererror = new Exception(reason);
            usererror.setStackTrace(e.getStackTrace());

            throw new RuntimeException(usererror);
        }
        return document;
    }

    private String getMessageForContent(String content, String path) {
        String reason = content;
        if (content == null ) {
            return null;
            
        }
        content = content.toLowerCase();

        //String contact = "<br>Please contact a system administrator";
        if (content.indexOf("token") > -1) {
            reason = "<b>IGV uses an authentication token for securely connecting to the server.</b><br>";          
            reason += "<br><br>" + gray("The exception was:<br>" + content);
            return reason;
        } else if (content.indexOf("user") > -1) {
            reason = "";
            if (content.indexOf("http access") > -1) {
                // IR
                if (path.indexOf("wsVerRest") > -1) {
                    reason = "<b>IGV uses your authentication token for securely connecting to the server.</b><br>";
                    reason += "<b>Maybe IGV did not get your token</b> - "
                            + b(blue("please make sure you launch IGV from the analysis result page in IR"));
                } else {
                    // ion torrent
                    reason += "<br>" + content;
                }
            } else {
                reason +=  content;
            }
        } else if (content.indexOf("organization") > -1) {
            reason = "It looks like there is a problem with your organization:<br>" + content;
        } else {
            log.info("Not sure what the reason is:" + content);
        }
        return reason;
    }

    private String b(String s) {
        return "<b>" + s + "</b>";
    }

    private String blue(String s) {
        return "<font color='000099'>" + s + "</font>";
    }

    private String red(String s) {
        return "<font color='990000'>" + s + "</font>";
    }

    private String gray(String s) {
        return "<font color='7777777'>" + s + "</font>";
    }
}