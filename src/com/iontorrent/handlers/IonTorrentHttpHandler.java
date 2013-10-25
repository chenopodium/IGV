/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.handlers;

import org.broad.igv.util.HttpHandler;
import com.iontorrent.utils.Encryptor;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.StringTools;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;

/**
 *
 * @author Chantal
 */
public class IonTorrentHttpHandler implements HttpHandler {

    private static int HEADER_CHECKS;
    private static Logger log = Logger.getLogger(IonTorrentHttpHandler.class);
    //private static boolean HEADER_SHOWN = false;
    private static Map<String, String> name_ip_map;

    public IonTorrentHttpHandler() {
    }
    
    public void setupHttpConnection() {
        disableHostnameVerifier();
    }

     private void disableHostnameVerifier() {
        HttpsURLConnection.setDefaultHostnameVerifier(getHostnameVerifier());
    }

    private static HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                return true;
            }
        };
    }
    @Override
    public void handle(URL url, HttpURLConnection conn) {
        checkForHeaderParameters(url, conn);
    }

    public void checkForHeaderParameters(URL url, HttpURLConnection conn) {
        PreferenceManager pref = PreferenceManager.getInstance();
        // get any header params
        String header_key = pref.getTemp("header_key");
        String header_value = pref.getTemp("header_value");
        boolean header_encrypt = pref.getTempAsBoolean("header_encrypt");
        String server = pref.getTemp("server");
        if (server != null) {
            int col = server.indexOf(":");
            if (col > -1) {
                server = server.substring(0, col);
            }
        }
        // else p("Got no server");
        if (header_value != null && header_key == null) {
            header_key = "Authorization";
        }
        boolean show =  HEADER_CHECKS < 3;/// || url.toString().endsWith(".seg") || url.toString().endsWith(".bed");
//        if (show) {
//            log.info("checkForHeaderParameters: header key and value: " + header_key + "=" + header_value + ", ecnryption is: " + header_encrypt + ", will add it to connection header");
//            log.info("checkForHeaderParameters: server:" + server + ", URL is: " + url.toString());
//        }
        if (header_key != null && header_value != null && server != null) {
            // by default no ecnryption is assumed.

            if (name_ip_map == null) {
                name_ip_map = new HashMap<String, String>();
            }
            String ipAddress = name_ip_map.get(server);
            if (ipAddress == null) {
                try {
                    InetAddress inetAddress = InetAddress.getByName(server);
                    ipAddress = inetAddress.getHostAddress().toString();
                    p("Got IP address " + ipAddress + "  for " + server);
                } catch (Exception e) {
                    p("Could not get IP of " + server + ":" + e.getMessage());
                }
                if (ipAddress == null) {
                    ipAddress = server;
                }
                name_ip_map.put(server, ipAddress);
            }
            String ipURL = name_ip_map.get(url.getHost());;
            if (ipURL == null) {
                try {
                    InetAddress inetAddress = InetAddress.getByName(url.getHost());
                    ipURL = inetAddress.getHostAddress().toString();
                    p("Got ipURL address " + ipURL + "  for " + url.getHost());
                } catch (Exception e) {
                    p("Could not get ipURL of  HOST " + url.getHost() + ":" + e.getMessage());
                }
                if (ipURL == null) {
                    ipURL = url.getHost();
                }

                name_ip_map.put(url.getHost(), ipURL);
            }
            
            String surl = url.toString();
            boolean useToken = surl.indexOf(server) > -1 || surl.indexOf(ipAddress) > -1
                    || server.indexOf(ipURL) > -1 || server.indexOf(url.getHost()) > -1;
            
            // TESTING/DEBUG ON UAT: we also use the token if it NOT broad
            if (!useToken) {
                surl = surl.toLowerCase();
                if (surl.indexOf("broad")<0 && surl.indexOf("hgdown")<0) {
                    useToken = true;
                    p("I would not use the token for "+surl+", but for debugging etc we are still using it");
                }
            }
            
            if (useToken) {
                if (header_encrypt) {
                    String encrypted = header_value;

                    String algo = pref.getTemp("algorithm");
                    if (algo == null) {
                        algo = Encryptor.getDefaultAlgorithm();
                    }

//                    if (show) {
//                        p("checkForHeaderParameters: About to decrypt with algo " + algo + " and url " + server + ", value length is " + header_value.length() + " is " + header_value);
//                    }
                    try {
                        header_value = Encryptor.decrypt(algo, header_value, "IGVKEY123");
                    } catch (Exception e) {
                        p("checkForHeaderParameters: Could not decrypt: " + ErrorHandler.getString(e));
                        byte b[] = fromHexadecimal(header_value);
                    }

                    if (show) {
                        //  p("header encrypt is: " + header_encrypt + ", value is: " + header_value + ", host is: " + url.getHost());
                        //  p("Got algorithm:"+algo);
                        if (header_value == null) {
                            log.error("Decrypted " + encrypted + " resulted in null. Algo is " + algo);
                        } else {
                            //    p("Decrypted token is: " + header_value);
                        }
                    }

                } else {
                    // replace _ with space

                    header_value = StringTools.replace(header_value, "_", " ");
                    if (show) {
                        p("checkForHeaderParameters: Token was not not encrypted! encrypt=" + pref.getTemp("header_encrypt") + ", Using token " + header_value);
                    }
                }
                conn.setRequestProperty("Accept", "*/*");
                if (header_value != null) {
                    if (show) {
                        p("SETTING " + header_key + "=" + header_value+" for url "+ url.toString());
                    }
                    conn.addRequestProperty(header_key, header_value);
                }
            } else {
                log.info("checkForHeaderParameters: NOT using token becaue server is not in URL: server:" + server + ", URL is: " + url.toString());
            }
        } else {
            if (show) {
                p("checkForHeaderParameters: Got no header key or value or no server " + server + ":" + url);
            }
        }
        HEADER_CHECKS++;
    }

    public byte[] fromHexadecimal(String message) {
        if (message == null) {
            return null;
        }
        if ((message.length() % 2) != 0) {
            p("Message length should be % 2 = 0");
            return null;
        }
        try {
            byte[] result = new byte[message.length() / 2];
            for (int i = 0; i < message.length(); i = i + 2) {
                int first = Integer.parseInt("" + message.charAt(i), 16);
                int second = Integer.parseInt("" + message.charAt(i + 1), 16);
                result[i / 2] = (byte) (0x0 + ((first & 0xff) << 4) + (second & 0xff));
            }
            return result;
        } catch (Exception e) {
            p("Got an error: " + ErrorHandler.getString(e));
        }
        return null;
    }

    private void p(String s) {
        log.info(s);
        //System.out.println("HttpUtils: " + s);

    }
}
