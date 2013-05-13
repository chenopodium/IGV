/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.batch;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 *
 * @author Chantal
 */
public interface CommandListenerIF {
    
     public static final String NOTHANDLED = "NOTHANDLED";
     
     public String processGet(String command, Map<String, String> params, CommandExecutor cmdExe, PrintWriter out) throws IOException;
}
