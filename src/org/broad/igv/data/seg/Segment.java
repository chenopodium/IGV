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

package org.broad.igv.data.seg;

import java.text.DecimalFormat;
import java.util.HashMap;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.track.WindowFunction;

/**
 * @author Enter your name here...
 * @version Enter version here..., 09/01/09
 */
public class Segment implements LocusScore {

    protected int extendedStart = -1;
    protected int extendedEnd = -1;
    protected int start;
    protected int end;
    protected float score;
    protected String chr;
    protected String description;
    protected HashMap<String,String> atts;
    protected static DecimalFormat format = new DecimalFormat("#.##");

    public Segment(int start, int end, float score) {
        this.start = start;
        this.end = end;
        if (extendedStart < 0) {
            extendedStart = start;
        }
        if (extendedEnd < 0) {
            extendedEnd = end;
        }
        this.score = score;
    }

    @Override
    public String getName() {
        return description;
    }


    public Segment(String chr, int start, int origStart, int end, int origEnd, float value, String description, HashMap<String,String> atts) {
        this.start = start;
        this.chr = chr;
        this.end = end;
        this.atts = atts;
        this.extendedStart = origStart;
        this.extendedEnd = origEnd;
        this.score = value;
        this.description = description;
    }

    public Segment copy() {
        
        Segment seg = new  Segment(chr, start, extendedStart, end, extendedEnd, score, description, atts);        
        return seg;
    }
    public void setChr(String chr) {
        this.chr = chr;
    }

    public HashMap<String,String> getAttributes(){
        return atts;
    }
    /** Added by CR */
    @Override
    public String getChr() {
        return chr;  //To change body of implemented methods use File | Settings | File Templates.
    }
/** Added by CR */
    @Override
    public int getStart() {
        return start;
    }
/** Added by CR */
    @Override
    public int getEnd() {
        return end;
    }
/** Added by CR */
    @Override
    public float getScore() {
        return score;
    }
/** Added by CR */
    @Override
    public void setStart(int start) {
        this.start = start;
    }
/** Added by CR */
    @Override
    public void setEnd(int end) {
        this.end = end;
    }


    @Override
    public String getValueString(double position, WindowFunction ignored) {
        String valueString = "Value @ "+chr+":"+start+"-"+end+": <b>" + format.format(getScore())+"</b>";
        if (description != null) {
            valueString += "<br>"+description;
        }
        return valueString;
    }

    public String getDescription() {
        return description;
    }
}
