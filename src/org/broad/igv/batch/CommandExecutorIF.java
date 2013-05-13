/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.batch;

/**
 *
 * @author Chantal
 */
public interface CommandExecutorIF {
    
    public static final String NOTHANDLED = "NOTHANDLED";
    
    public String execute(String cmd);
}
