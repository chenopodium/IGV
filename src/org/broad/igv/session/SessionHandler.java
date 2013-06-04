/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.session;

import org.broad.igv.ui.IGV;

/**
 *
 * @author Chantal
 */
public interface SessionHandler {
    public SessionReader getSessionReader(IGV igv);
}
