/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

/**
 *
 * @author Chantal Roth
 */
public class MapPosition {
    private int left_position;
    private int right_position;
    private int chr;
    private String name;
    private String type;
    

    /**
     * @return the left_position
     */
    public int getLeft_position() {
        return left_position;
    }

    /**
     * @param left_position the left_position to set
     */
    public void setLeft_position(int left_position) {
        this.left_position = left_position;
    }

    /**
     * @return the right_position
     */
    public int getRight_position() {
        return right_position;
    }

    /**
     * @param right_position the right_position to set
     */
    public void setRight_position(int right_position) {
        this.right_position = right_position;
    }

    /**
     * @return the chr
     */
    public int getChr() {
        return chr;
    }

    /**
     * @param chr the chr to set
     */
    public void setChr(int chr) {
        this.chr = chr;
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

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
}
