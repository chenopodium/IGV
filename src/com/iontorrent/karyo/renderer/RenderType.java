/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.drawables.GuiChromosome;
import com.iontorrent.karyo.drawables.GuiFeatureTree;
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.Color;
import org.apache.log4j.Logger;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal
 */
public class RenderType {

    private String name;
    private String description;
    
    protected Color color;
    protected Color color1;
    //  Color color;
    private String type;
    private String relevantAttName;

    public RenderType() {
        this("Histogram", "Histogram", "Standard histogram type rendering for whole chromosome view for any kind of feature");
    }

    public String getColorName() {
        return "Track color";
    }
    public String getColor1Name() {
        return null;
    }
    public RenderType(String name, String desc) {
        this(name, name, desc);
    }

    public RenderType(String type, String name, String desc) {
        this.type = type;
        this.name = name;
        this.description = desc;

    }
    @Override
    public String toString() {
        return name;
    }
    public String geType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public boolean isClassSupported(Feature featureClass) {
        return featureClass instanceof Feature;
    }

    public GuiFeatureTree getGuiTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        return new GuiFeatureTree(ktrack, canvas, chromo, tree, dx);
    }

    public Color getDefaultColor1(KaryoTrack ktrack) {
        return getDefaultColor(ktrack).brighter();
    }
    public Color getDefaultColor(KaryoTrack ktrack) {
        String type = ktrack.getName().toUpperCase();
//        if (sampleafeture != null) {
//            if (sampleafeture instanceof LocusScore) {
//                return new Color(200, 200, 255);
//            } else if (sampleafeture instanceof Segment) {
//                return new Color(220, 255, 220);
//            }
//        }
        if (type.indexOf("SNP") > -1) {
            return Color.blue.darker();
        } else if (type.indexOf("INDEL") > -1) {
            return Color.red.darker();
        } else if (type.indexOf("INSERTION") > -1) {
            return Color.green.darker();
        } else if (type.indexOf("DELETION") > -1) {
            return Color.red.darker();
        } else if (type.indexOf("CNV") > -1) {
            return new Color(200, 50, 240);
        } else if (type.indexOf("EXOME") > -1) {
            return Color.green.darker();
        } else if (type.indexOf("GENE") > -1) {
            return Color.blue.darker();
        } else if (type.indexOf("COVERAGE") > -1) {
            return Color.gray;

        } else {
            return Color.orange;
        }
    }

    private void err(String s) {
        Logger.getLogger("RenderType").warn(s);
    }

    private void p(String s) {
        Logger.getLogger("RenderType").info(s);
    }

    /**
     * @return the relevantAttName
     */
    public String getRelevantAttName() {
        return relevantAttName;
    }

    /**
     * @param relevantAttName the relevantAttName to set
     */
    public void setRelevantAttName(String relevantAttName) {
        this.relevantAttName = relevantAttName;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @return the color1
     */
    public Color getColor1() {
        return color1;
    }

    /**
     * @param color1 the color1 to set
     */
    public void setColor1(Color color1) {
        this.color1 = color1;
    }
}
