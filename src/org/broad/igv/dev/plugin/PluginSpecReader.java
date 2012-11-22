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

package org.broad.igv.dev.plugin;

import org.apache.log4j.Logger;
import org.broad.igv.DirectoryManager;
import org.broad.igv.util.FileUtils;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

/**
 * Class used to parse a plugin specification file (currently XML).
 * <p/>
 * User: jacob
 * Date: 2012-Aug-03
 */
public class PluginSpecReader {

    private static Logger log = Logger.getLogger(PluginSpecReader.class);

    protected String specPath;
    protected Document document;

    public static final String CUSTOM_PLUGIN_FILENAME = "custom_plugins.txt";

    /**
     * List of plugins tha IGV knows about
     */
    private static List<PluginSpecReader> pluginList;

    private PluginSpecReader(String path) {
        this.specPath = path;
    }

    /**
     * Create a new reader. Returns null if
     * the input path does not represent a valid plugin spec,
     * or if there was any other problem parsing the document
     *
     * @param path
     * @return
     */
    public static PluginSpecReader create(String path) {
        PluginSpecReader reader = new PluginSpecReader(path);
        if (!reader.parseDocument()) return null;
        return reader;
    }

    public static boolean isToolPathValid(String execPath) {
        execPath = FileUtils.findExecutableOnPath(execPath);
        File execFile = new File(execPath);
        boolean pathValid = execFile.isFile();
        if (pathValid && !execFile.canExecute()) {
            log.error(execPath + " exists but is not executable. ");
            pathValid = false;
        }

        return pathValid;
    }

    public String getSpecPath() {
        return specPath;
    }

    public String getId() {
        return document.getDocumentElement().getAttribute("id");
    }

    private boolean parseDocument() {
        boolean success = false;
        try {
            document = Utilities.createDOMDocumentFromXmlStream(
                    ParsingUtils.openInputStream(specPath)
            );
            success = document.getDocumentElement().getTagName().equals("igv_plugin");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public List<Element> getTools() {
        return getElementsByTag(document.getDocumentElement(), "tool");
    }

    public List<Element> getCommands(Element tool) {
        return getElementsByTag(tool, "command");
    }

    public List<Argument> getArguments(Element tool, Element command) {
        List<Element> argEls = getElementsByTag(command, "arg");
        if (argEls.size() == 0) {
            argEls = getDefaultValues(tool, "arg");
        }
        return elementsToArguments(argEls);
    }

    private List<Element> getDefaultValues(Element tool, String tag) {
        NodeList defaults = tool.getElementsByTagName("default");
        assert defaults.getLength() == 1;
        return getElementsByTag((Element) defaults.item(0), tag);
    }

    private List<Argument> elementsToArguments(List<Element> argEls) {
        List<Argument> argumentList = new ArrayList<Argument>();
        for (Element argEl : argEls) {
            argumentList.add(Argument.parseFromNode(argEl, this.getSpecPath()));
        }
        return argumentList;
    }

    private List<Element> getElementsByTag(Element topElement, String tag) {
        NodeList nodes = topElement.getElementsByTagName(tag);
        List<Element> outNodes = new ArrayList<Element>(nodes.getLength());
        for (int nn = 0; nn < nodes.getLength(); nn++) {
            outNodes.add((Element) nodes.item(nn));
        }
        return outNodes;

    }

    public Map<String, String> getParsingAttributes(Element tool, Element command) {
        List<Element> parserEls = getElementsByTag(command, "parser");
        if (parserEls.size() == 0) {
            parserEls = getDefaultValues(tool, "parser");
        }
        //Parser element not required, if not found we use default values
        //Definitely cannot have more than one, though
        if (parserEls.size() == 0) return null;
        assert parserEls.size() == 1;
        return Utilities.getAttributes(parserEls.get(0));
    }

    public static List<PluginSpecReader> getPlugins() {
        if (pluginList == null) {
            pluginList = generatePluginList();
        }
        return pluginList;
    }

    /**
     * Return a list of plugin specification files present on the users computer.
     * Checks the IGV installation directory (jar) as well as IGV data directory
     *
     * @return
     */
    private static List<PluginSpecReader> generatePluginList() {
        List<PluginSpecReader> readers = new ArrayList<PluginSpecReader>();
        //Guard against loading plugin multiple times. May want to reconsider this,
        //and override equals of PluginSpecReader
        Set<String> pluginIds = new HashSet<String>();

        try {
            File[] checkDirs = new File[]{
                    DirectoryManager.getIgvDirectory(), new File(FileUtils.getInstallDirectory()),
                    new File(".")
            };
            for (File checkDir : checkDirs) {
                File plugDir = new File(checkDir, "plugins");
                File[] possPlugins = plugDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".xml");
                    }
                });

                if (possPlugins == null) {
                    possPlugins = new File[0];
                }

                List<String> possPluginsList = new ArrayList<String>(possPlugins.length);

                for (File fi : possPlugins) {
                    possPluginsList.add(fi.getAbsolutePath());
                }

                //When a user adds a plugin, we store the path here
                File customFile = new File(checkDir, CUSTOM_PLUGIN_FILENAME);
                if (customFile.canRead()) {
                    BufferedReader br = new BufferedReader(new FileReader(customFile));
                    String customPluginPath = "";
                    while ((customPluginPath = br.readLine()) != null) {
                        possPluginsList.add(customPluginPath);
                    }
                }

                for (String possPlugin : possPluginsList) {
                    PluginSpecReader reader = PluginSpecReader.create(possPlugin);
                    if (reader != null && !pluginIds.contains(reader.getId())) {
                        readers.add(reader);
                        pluginIds.add(reader.getId());
                    }
                }


            }

        } catch (Exception e) {
            log.error(e);
            //Guess this user won't be able to use plugins
        }
        return readers;
    }

    /**
     * @param absolutePath Full path (can be URL) to plugin
     */
    public static void addCustomPlugin(String absolutePath) throws IOException {
        File outFile = new File(DirectoryManager.getIgvDirectory(), CUSTOM_PLUGIN_FILENAME);

        outFile.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
        writer.write(absolutePath);
        writer.write("\n");
        writer.flush();
        writer.close();

        pluginList = generatePluginList();
    }
}
