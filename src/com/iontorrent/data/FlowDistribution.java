/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data;

import java.awt.datatransfer.StringSelection;
import java.util.TreeMap;

/**
 *
 * @author Chantal Roth
 */
public class FlowDistribution {

    private TreeMap<Short, Integer> map;
    private String name;

    public FlowDistribution(TreeMap<Short, Integer> map, String name) {
        this.map = map;
        this.name = name;
    }

    public String toCsv(int binsize) {
        int[] bins = getBinnedData(binsize);
        String nl = "\n";
        StringBuilder csv = new StringBuilder();
        csv = csv.append(getName());
        csv = csv.append("flow value, count\n");
        for (int b = 0; b < bins.length; b++) {
            csv = csv.append(b * binsize).append(",").append(bins[b]).append(nl);
        }
        csv = csv.append(nl);
        return csv.toString();
    }

    public String toJson() {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        for (Short key : map.keySet()) {
            buf.append("    \"").append(key).append("\" : \"").append(map.get(key)).append("\"\n");
        }
        buf.append("}\n");
        return buf.toString();
    }

    public int[] getBinnedData(int binsize) {
        int maxx = 0;
        for (Short x : map.keySet()) {
            if (x > maxx) {
                maxx = x;
            }
        }
        int nrbins = maxx / binsize + 1;
        int bins[] = new int[nrbins];
        for (Short x : map.keySet()) {
            int y = map.get(x);
            bins[x / binsize] += y;
        }
        return bins;
    }

    /**
     * @return the map
     */
    public TreeMap<Short, Integer> getMap() {
        return map;
    }

    /**
     * @param map the map to set
     */
    public void setMap(TreeMap<Short, Integer> map) {
        this.map = map;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
