/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

package org.broad.igv.feature;

import com.iontorrent.karyo.data.Range;


public class Cytoband implements NamedFeature {
    String chromosome;
    String name;
    String longName;
    int end;
    int start;
    char type; // p, n, or c or x for par on sex chromosomes
    short stain;


    public Cytoband(String chromosome, String name, int start, int end) {
        this(chromosome);
        this.start = start;
        this.end = end;
        this.stain =0;
        this.type='x';
        this.name=name;
        
    }
    public Cytoband(String chromosome) {
        this.chromosome = chromosome;
        this.name = "";
    }

    public boolean isPar() {
        return type == 'x';
    }
    
    public Range getRange() {
        return new Range(start, end);
    }
    
    public boolean overlaps(Range r) {
        return getRange().overlaps(r);
    }
    public void trim() {

        // @todo -- trim arrays
    }

    @Override
    public String toString() {
        return "Band "+start+"-"+end+":"+type+"="+name;
    }
    @Override
    public String getChr() {
        return chromosome;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLongName() {
        if(longName == null) {
            longName = chromosome.replace("chr", "") + name;
        }
        return longName;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public void setType(char type) {
        this.type = type;
    }

    public char getType() {
        return type;
    }

    public void setStain(short stain) {
        this.stain = stain;
    }

    public short getStain() {
        return stain;
    }


}

