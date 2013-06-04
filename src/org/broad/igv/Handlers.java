/*
 * Copyright (c) 2007-2013 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 * Author: Chantal Roth
 */
package org.broad.igv;

import com.iontorrent.utils.ErrorHandler;
import java.util.*;
import org.apache.log4j.Logger;
import org.broad.igv.batch.CommandExecutorIF;
import org.broad.igv.batch.CommandListenerIF;
import org.broad.igv.sam.AlignmentTrackHandler;
import org.broad.igv.session.SessionHandler;
import org.broad.igv.ui.ArgumentHandler;
import org.broad.igv.ui.action.SearchCommandHandler;
import org.broad.igv.util.HttpHandler;

/**
 * User: Chantal Roth Date: May 13th, 2013
 */
public class Handlers {

    private static Logger log = Logger.getLogger(Handlers.class);
   
    public static String HTTPHANDLER;
    public static String COMMANDEXECUTOR;
    public static String COMMANDLISTENER;
    public static String SEARCHCOMMANDHANDLER;
    public static String ARGUMENTHANDLER;
    public static String SESSIONHANDLER;
    public static String ALIGNMENTTRACKHANDLER;

    static {
        Properties handlerproperties = new Properties();
        try {
            handlerproperties.load(Globals.class.getResourceAsStream("/resources/handler.properties"));
        } catch (Exception e) {
            log.info("*** Was unable to load handler.properties, will use default handlers ***", e);
        }
        HTTPHANDLER = handlerproperties.getProperty("httphandler", null);
        COMMANDEXECUTOR = handlerproperties.getProperty("commandexecutor", null);
        COMMANDLISTENER = handlerproperties.getProperty("commandlistener", null);
        SEARCHCOMMANDHANDLER = handlerproperties.getProperty("searchcommandhandler", null);
        ARGUMENTHANDLER = handlerproperties.getProperty("argumenthandler", null);
        SESSIONHANDLER = handlerproperties.getProperty("sessionhandler", null);
        ALIGNMENTTRACKHANDLER = handlerproperties.getProperty("alignmenttrackhandler", null);
    }

    /**
     * # If nothing is specified, it will just use HttpUtils as is without any special handler
# Otherwise, it will try to create an instance of this class (implementing HttpHandler), which will be
# called whenever an URL is created.
# This is for instance useful of a token mechanism is used and something needs to be written to the HTTP header
     * @return 
     */
    public static HttpHandler getHttpHandler() {
        if (HTTPHANDLER == null) {
            return null;
        }
        try {
            HttpHandler handler = (HttpHandler) Class.forName(HTTPHANDLER).newInstance();
            return handler;
        } catch (Exception e) {
            log.error("Was unable to create HttpHandler from " + HTTPHANDLER + ", check the resources/handler.properties file for errors:" + ErrorHandler.getString(e));
        }
        return null;
    }
/**
 * # if additional commands should be handled by the command executor (for processing batch files or when a user enters a command via menu item)
# an instance of CommandExecutorIF can be specified here, which will use that class to execute those extra commands.
# IGV will then invoke this command listener first, and only if the command
# has not been handled, will execute the default commands.
 * @return 
 */
    public static CommandExecutorIF getCommandExecutor() {
        if (COMMANDEXECUTOR == null) {
            log.info("Got no commandexecutor");
            return null;
        }
        try {
            CommandExecutorIF handler = (CommandExecutorIF) Class.forName(COMMANDEXECUTOR).newInstance();
            return handler;
        } catch (Exception e) {
            log.error("Was unable to create CommandExecutorIF from " + COMMANDEXECUTOR + ", check the resources/handler.properties file for errors:" + ErrorHandler.getString(e));
        }
        return null;
    }

      /**
         * # if additional commands should be handled by the command executor (for processing batch files for instance)
           # an instance of CommandExecutorIF can be specified here, which will use that class to execute those extra commands
         */
    public static CommandListenerIF getCommandListener() {
        if (COMMANDLISTENER == null) {
            return null;
        }
        try {
            CommandListenerIF handler = (CommandListenerIF) Class.forName(COMMANDLISTENER).newInstance();
            return handler;
        } catch (Exception e) {
            log.error("Was unable to create CommandListenerIF from " + COMMANDLISTENER + ", check the resources/handler.properties file for errors:" + ErrorHandler.getString(e));
        }
        return null;
    }
    
    /**
     * # If the search command (finding genes or similar) should do something special,
       # then a search command handler can be specified here. If this is defined, it will invoke this execute
       # method instead of the default execute method of SearchCommand
     * @return 
     */
     public static SearchCommandHandler getSearchCommandHandler() {
        if (SEARCHCOMMANDHANDLER == null) {
            return null;
        }
        try {
            SearchCommandHandler handler = (SearchCommandHandler) Class.forName(SEARCHCOMMANDHANDLER).newInstance();
            return handler;
        } catch (Exception e) {
            log.error("Was unable to create SearchCommandHandler from " + SEARCHCOMMANDHANDLER + ", check the resources/handler.properties file for errors:" + ErrorHandler.getString(e));
        }
        return null;
    }
     
     /**
      * # if IGV should deal with special arguments, both via .jnlp file or via command line
    # you can specify a handler here. For instance, these might be properties for dealing with tokens,
    # or for setting certain gui or other properties etc
      * @return 
      */
     public static ArgumentHandler getArgumentHandler() {
        if (ARGUMENTHANDLER == null) {
            return null;
        }
        try {
            ArgumentHandler handler = (ArgumentHandler) Class.forName(ARGUMENTHANDLER).newInstance();
            return handler;
        } catch (Exception e) {
            log.error("Was unable to create ArgumentHandler from " + ARGUMENTHANDLER + ", check the resources/handler.properties file for errors:" + ErrorHandler.getString(e));
        }
        return null;
    }
     
       /**
      * When reading a session from a remote server, there could be problems that are not dealt with IGV.
      * 
      * @return 
      */
     public static SessionHandler getSessionHandler() {
        if (SESSIONHANDLER == null) {
            return null;
        }
        try {
            SessionHandler handler = (SessionHandler) Class.forName(SESSIONHANDLER).newInstance();
            return handler;
        } catch (Exception e) {
            log.error("Was unable to create a SessionHandler from " + SESSIONHANDLER + ", check the resources/handler.properties file for errors:" + ErrorHandler.getString(e));
        }
        return null;
    }
     
     /** adds custom menus and actions to alignment track */
      public static AlignmentTrackHandler getAlignmentTrackHandler() {
        if (ALIGNMENTTRACKHANDLER == null) {
            return null;
        }
        try {
            AlignmentTrackHandler handler = (AlignmentTrackHandler) Class.forName(ALIGNMENTTRACKHANDLER).newInstance();
            return handler;
        } catch (Exception e) {
            log.error("Was unable to create an AlignmentTrackHandler from " + ALIGNMENTTRACKHANDLER + ", check the resources/handler.properties file for errors:" + ErrorHandler.getString(e));
        }
        return null;
    }
}
