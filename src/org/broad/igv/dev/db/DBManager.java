/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

package org.broad.igv.dev.db;

import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.LoginDialog;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for prototyping database connections.  Prototype only -- hardcoded for mysql,  connects to single database,
 * keeps single connection, etc.
 *
 * @author Jim Robinson
 * @date 10/31/11
 */
public class DBManager {

    private static Logger log = Logger.getLogger(DBManager.class);

    static Map<String, Connection> connectionPool =
            Collections.synchronizedMap(new HashMap<String, Connection>());

    private static Map<String, String> driverMap;

    static {
        driverMap = new HashMap<String, String>(2);
        driverMap.put("mysql", "com.mysql.jdbc.Driver");
        driverMap.put("sqlite", "org.sqlite.JDBC");
    }

    public static Connection getConnection(ResourceLocator locator) {
        String url = locator.getPath();
        if (connectionPool.containsKey(url)) {
            Connection conn = connectionPool.get(url);
            try {
                if (conn == null || conn.isClosed()) {
                    connectionPool.remove(url);
                } else {
                    return conn;
                }
            } catch (SQLException e) {
                log.error("Bad connection", e);
                connectionPool.remove(url);
            }
        }


        // No valid connections
        Connection conn = connect(locator);
        if (conn != null) {
            connectionPool.put(url, conn);
            log.info("Connection pool size: " + connectionPool.size());
        }
        return conn;

    }


    public static void closeConnection(ResourceLocator locator) {
        String url = locator.getPath();
        if (connectionPool.containsKey(url)) {
            Connection conn = connectionPool.get(url);
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    connectionPool.remove(url);
                }
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private static Connection connect(ResourceLocator locator) {
        try {
            return DriverManager.getConnection(locator.getPath(),
                    locator.getUsername(), locator.getPassword());
        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            if (errorCode == 1044 || errorCode == 1045) {
                String resource = locator.getPath();

                Frame parent = Globals.isHeadless() ? null : IGV.getMainFrame();
                LoginDialog dlg = new LoginDialog(parent, false, resource, false);
                dlg.setVisible(true);
                if (dlg.isCanceled()) {
                    throw new RuntimeException("Must login to access" + resource);
                }
                locator.setUsername(dlg.getUsername());
                locator.setPassword(new String(dlg.getPassword()));
                return connect(locator);

            } else {
                MessageUtils.showMessage("<html>Error connecting to database: <br>" + e.getMessage());
                return null;
            }

        }
    }

    public static void shutdown() {
        for (Connection conn : connectionPool.values()) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {

                }
            }
        }
        connectionPool.clear();
    }


    static String createConnectionURL(String subprotocol, String host, String db, String port) {
        String driver = driverMap.get(subprotocol);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to create driver for protocol " + subprotocol);
        }

        //If the host is a local file, don't want the leading "//"
        if (!(new File(host)).exists()) {
            host = "//" + host;
        }
        String url = "jdbc:" + subprotocol + ":" + host;
        if (port != null && !port.equals("")) {
            try {
                int iPort = Integer.parseInt(port);
                if (iPort >= 0) {
                    url += ":" + iPort;
                }
            } catch (NumberFormatException e) {
                log.error("Invalid port: " + port);
            }
        }
        if (db != null) {
            url += "/" + db;
        }

        return url;
    }

    /**
     * Open connection using parameters specified in the given
     * profile.
     *
     * @param profilePath
     * @return
     */
    public static ResourceLocator getStoredConnection(String profilePath) {
        InputStream profileStream = null;
        try {
            profileStream = new FileInputStream(profilePath);
            Document document = Utilities.createDOMDocumentFromXmlStream(profileStream);
            Node db = document.getElementsByTagName("database").item(0);
            NamedNodeMap attr = db.getAttributes();
            String host = attr.getNamedItem("host").getTextContent();
            String path = attr.getNamedItem("path").getTextContent();
            String subprotocol = attr.getNamedItem("subprotocol").getTextContent();

            String port = Utilities.getNullSafe(attr, "port");
            String username = Utilities.getNullSafe(attr, "username");
            String password = Utilities.getNullSafe(attr, "password");

            ResourceLocator locator = new ResourceLocator(createConnectionURL(subprotocol, host, path, port));
            locator.setUsername(username);
            locator.setPassword(password);

            return locator;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (profileStream != null) profileStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Close the specified resources
     *
     * @param rs
     * @param st
     * @param conn
     */
    static void closeResources(ResultSet rs, Statement st, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Error closing sql connection", e);
            }
        }

    }

}
