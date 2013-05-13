/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.ui;

import org.broad.igv.ui.Main.IGVArgs;

/**
 *
 * @author Chantal
 */
public interface ArgumentHandler {
    /**
     * # if IGV should deal with special arguments, both via .jnlp file or via command line
        # you can specify a handler here. For instance, these might be properties for dealing with tokens,
        # or for setting certain gui or other properties etc
     * @param igvargs
     * @param nonoptionalargs any additional paramaters that IGV did not recognize 
     */
    public void parseArgs(IGVArgs igvargs, String[] nonoptionalargs);
}
