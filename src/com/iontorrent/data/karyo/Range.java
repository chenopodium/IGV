/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

/**
 *
 * @author Chantal Roth
 */
public class Range {
    public int a;
    public int b;
    public Range(int a, int b) {
        this.a=a;
        this.b=b;
    }
    
    public boolean overlaps(Range r) {
        // a-------------b
        //     r.a---r.b
        //
        // a-------------b
        //     r.a-----------r.b        
        //
        //      a-----------------b
        //r.a-----------r.b
        boolean o = (a <r.a && r.a < b) || ( a<r.b && r.b < b);
        if (o) return o;
        
        o = (r.a <a && a < r.b) || ( r.a< b && b < r.b);
        return o;
    }
}
