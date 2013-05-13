/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.util;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Chantal
 */
public interface HttpHandler {
    
    /** do something to the http connection, such as setting header parameters or changing the url etc */
      public void handle(URL url, HttpURLConnection conn);
      
}
