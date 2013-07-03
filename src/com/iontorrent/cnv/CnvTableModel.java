/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import com.iontorrent.utils.StringTools;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Chantal
 */
public class CnvTableModel extends AbstractTableModel{

    private int maxrows = 20;
        
    ArrayList<String> lines;    
    ArrayList<String> header; 
    String sep;
    public CnvTableModel(ArrayList<String> lines, String sep) {
        this.lines = lines;
        this.sep = sep;
        header = StringTools.parseList(lines.get(0), sep);
    }
    public ArrayList<String> getHeader() {
        return header;
    }
    @Override
    public int getRowCount() {
       return Math.min(lines.size(), maxrows);
    }

    @Override
    public int getColumnCount() {
       return header.size();
    }

    @Override
    public String getColumnName(int col) {
        if (header == null || col >= header.size()) return "Column "+col;
        else return col+") "+ header.get(col);
    }
    @Override
    public Object getValueAt(int rowIndex, int col) {
        if (lines == null || lines.size() <= rowIndex+1) return "";
        String line = lines.get(rowIndex+1);
        line = line.replace(":", ".");
        ArrayList<String> items = StringTools.parseList(line, sep);
        if (items != null && col < items.size()) {
            String item = items.get(col);
            item = item.replace(",", ".");
            return item;
        }
        else return "";
    }
    
}
