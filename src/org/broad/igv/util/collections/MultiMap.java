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

package org.broad.igv.util.collections;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * A "map-like" class that supports multiple values for a given key.
 * Note this violates the map contract,  thus "map-like".
 * Created to support GFF column 9 and Genbank tag-value pairs.
 */
public class MultiMap<K, V> {

    LinkedHashMap<K, Object> map;
    protected static Logger log = Logger.getLogger(MultiMap.class);
    int size = 0;

    public MultiMap() {
        this(10);
    }

    public MultiMap(int size) {
        map = new LinkedHashMap<K, Object>(size);
    }

    public void put(K key, V value) {
        Object currentValue = map.get(key);
        if (currentValue == null) {
            map.put(key, value);
        } else if (currentValue instanceof List) {
            ((List) currentValue).add(value);
        } else {
            List<V> valueList = new ArrayList<V>();
            valueList.add((V) currentValue);
            valueList.add(value);
            map.put(key, valueList);
        }
        size++;
    }

    public V get(K key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        } else if (value instanceof List) {
            return ((List<V>) value).get(0);
        } else {
            return (V) value;
        }
    }

    public V remove(K key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof List) {
            size -= ((List) value).size();
            map.remove(key);
            return (V) ((List) value).get(0);
        } else {
            size--;
            map.remove(key);
            return (V) value;
        }
    }

    public int size() {
        return size;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    private static final int MAX_CHARS_PER_LINE = 50;

    public void printHtml(StringBuffer buffer, int max) {
        printHtml(buffer, max, null);
    }

    public void printHtml(StringBuffer buffer, int max, HashMap<String,Color> highlightcolors) {

        if (map == null || map.isEmpty()) return;

        int count = 0;
        //buffer.append("<br>");
        for (Map.Entry<K, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            String key = (String) entry.getKey();
            boolean gotcolor = false;
            if (highlightcolors != null && highlightcolors.size()>0) {
             //   log.info("Got hightlightcolors: "+highlightcolors.keySet());
                Color c = highlightcolors.get(key);
                
                if (key != null && c != null) {
                   
                    gotcolor = true;
                    String hex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
                    key = "<font color='"+hex+"'><b>"+key;
                 //    log.info("Got key "+key+" with value "+c+", key is now: "+key);
                }
            }
            if (value instanceof List) {
                for (V v : ((List<V>) value)) {
                    buffer.append(key);
                    buffer.append("=");
                    buffer.append(v.toString());
                    buffer.append("<br/>");
                    count++;
                }
            } else {
                buffer.append(key);
                buffer.append("=");
                String ts = lineWrapString(value.toString(), MAX_CHARS_PER_LINE);

                buffer.append(ts);
                buffer.append("<br/>");
                count++;
            }
            if (gotcolor) buffer.append("</b></font>");
            if (++count > max) {
                buffer.append("...");
                break;
            }

        }
    }

    private String lineWrapString(String input, int maxCharsPerLine) {
        int lines = input.length() / maxCharsPerLine + 1;
        if (lines == 1) return input;

        String result = input.substring(0, maxCharsPerLine);
        for (int lineNum = 1; lineNum < lines; lineNum++) {
            int start = lineNum * maxCharsPerLine;
            int end = Math.min(start + maxCharsPerLine, input.length());
            result += "<br/>" + input.substring(start, end);
        }
        return result;
    }

    public List<V> values() {
        List<V> allValues = new ArrayList<V>(map.size());
        for (Map.Entry<K, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List) {
                allValues.addAll((List) value);

            } else {
                allValues.add((V) value);
            }

        }
        return allValues;
    }
}
