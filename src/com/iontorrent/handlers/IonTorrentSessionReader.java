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
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.session.IGVSessionReader;
import org.broad.igv.session.Session;
import org.broad.igv.ui.IGV;
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

    public IonTorrentSessionReader(IGV igv) {
        super(igv);
    }

    @Override
    public boolean loadSession(Session session, String sessionPath, int tries) throws IOException {
        this.path = sessionPath;
        InputStream inputStream = null;
        
        if (tries > 0) {
//            PreferenceManager pref = PreferenceManager.getInstance();
//            log.info("===== loadSession: More than one try. Will check token and use a default IGV token if there was one");
//             
//            String header_value = pref.getTemp("header_value");
//            
//            if (header_value != null) {
//                header_value = "__IGV__";
//                pref.putTemp("header_value", header_value);
//                pref.putTemp("header_encrypt", "false");
//                // turn off encryption
//                log.info("There is a token. Setting it to default "+pref.getTemp("header_value")+", encrypt is now "+pref.getTempAsBoolean("header_encrypt"));
//                
//            }
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
                Exception usererror = new Exception(reason);
                usererror.setStackTrace(e.getStackTrace());

                throw new RuntimeException(usererror);
            }
        }

        return loadSession(inputStream, session, sessionPath, tries);

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
        content = content.toLowerCase();

        //String contact = "<br>Please contact a system administrator";
        if (content.indexOf("token") > -1 || content.indexOf("does not exist") > -1) {
            reason = "<b>IGV uses your authentication token for securely connecting to the server.</b><br>";

            if (content.indexOf("expired") > -1) {
                reason += "<b>It looks like your token has expired - " + blue("please regenerate it from your Profile.") + "</b>";
            } else if (content.indexOf("invalid") > -1) {
                reason += "<b>It looks like your token is invalid - " + blue("please regenerate it from your Profile.") + "</b>";
            } else {
                reason += "<b>It could be that the token has expired - " + blue("please regenerate it from your Profile.") + "</b>";
            }
            reason += "<br><br>" + gray("The exception was:<br>" + content);
            return reason;
        } else if (content.indexOf("user") > -1) {
            reason = "It looks like there is a problem with your user account.";
            if (content.indexOf("http access") > -1) {
                // IR
                if (path.indexOf("wsVerRest") > -1) {
                    reason = "<b>IGV uses your authentication token for securely connecting to the server.</b><br>";
                    reason += "<b>But IGV did not get your token</b> - "
                            + b(blue("please make sure you launch IGV from the analysis result page in IR"));
                } else {
                    // ion torrent
                    reason += "<br>" + content;
                }
            } else {
                reason += "<br>" + content;
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