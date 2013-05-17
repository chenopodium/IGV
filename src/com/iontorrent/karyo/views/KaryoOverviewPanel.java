/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import com.iontorrent.event.SelectionEvent;
import com.iontorrent.event.SelectionListener;
import com.iontorrent.guiutils.GuiUtils;

import com.iontorrent.karyo.data.Chromosome;
import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.IgvAdapter;
import com.iontorrent.karyo.data.KaryoFeature;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.drawables.GuiChromosome;
import com.iontorrent.karyo.drawables.GuiFeatureTree;
import com.iontorrent.karyo.drawables.PhysicalCoord;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.utils.StringTools;
import com.iontorrent.views.basic.GuiCanvas;
import com.iontorrent.views.basic.StaticText;
import com.iontorrent.views.basic.Text;
import com.iontorrent.views.basic.ZoomCanvas;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.broad.igv.ui.IGV;

import org.broad.igv.util.LongRunningTask;

/**
 *
 * @author Chantal Roth
 */
public class KaryoOverviewPanel extends JPanel {

    private KaryoManager man;
    private ArrayList<GuiChromosome> guichromosomes;
    private ArrayList<Chromosome> chromosomes;
    private IgvAdapter igvadapter;
    private ZoomCanvas mainview;
    private ZoomCanvas detailview;
    private JSplitPane split;
    private int current_location;
    private GuiChromosome current_chromosome;
    private int detailzoom = 100;
    private String msg;
    
    private static final int TRACKDX = 20;
    public KaryoOverviewPanel(KaryoManager man) {
        this.man = man;
        igvadapter = man.getIgvAdapter();
        setLayout(new BorderLayout());

    }

    public void setSliderValue(int v) {
           p(" === SET SLIDER VALUE TO "+v);
        mainview.setSliderValuePercent(v);
        mainview.repaint(); 
    }
    
    public void createView() {
        if (split != null) {
            remove(split);
        }
        mainview = createOverviewView();
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.add(mainview);
        
        Rectangle r = mainview.getVisibleRect();
        int rw = r.width;
        int rh = r.height;
        add("Center", split);
    mainview.scrollRectToVisible(new Rectangle(0,0, rw, rh ));
       
        split.setDividerLocation(1000);
        if (guichromosomes != null && guichromosomes.size() > 1) {
            if (detailview != null) {
                this.detailzoom = (int) detailview.getZoomValue();
                p("Old detail zoom value was: "+detailzoom);
            }
            detailview = createDetailView(guichromosomes.get(0));            
            split.add(detailview);
            //detailview.setSliderValuePercent(100);
        }
        p("Main View created with bands without tracks");
        if (msg != null) {
            // got a message
           JLabel  messagelabel = new JLabel();
            messagelabel.setText(msg);
            add("South", messagelabel);
        }
    }

    /**
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * @param msg the msg to set
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    private class TaskLoader implements Runnable {

        @Override
        public void run() {

            p("================== Starting load Tracks thread");
            man.loadTracks(new TaskListener() {
                @Override
                public void taskDone(Task task) {
                    p("==================Loading tracks done, adding " + man.getSelectdKaryoTracks().size() + " tracks to overview");
                    addTracksToOverview();
                    man.getControl().recreateView(false);
                }
            });
            // we need to show some message in the meantime in the control panel!
        }
    }

    public void loadTracks() {
        LongRunningTask.submit(new TaskLoader());

    }

    /**
     * add any newly loaded tracks
     */
    public void addTracksToOverview() {
        p("=========== addTracksToOverview: Got " + man.getSelectdKaryoTracks().size() + " after loadTracks done");

        ArrayList<GuiFeatureTree> guitrees = new ArrayList<GuiFeatureTree>();

        ArrayList<KaryoTrack> karyotracks = man.getSelectdKaryoTracks();

        //     p("adding " + karyotracks.size() + " karyotracks");
        
        for (int c = 0; c < guichromosomes.size(); c++) {
            final GuiChromosome guichr = guichromosomes.get(c);
            final Chromosome chr = guichr.getChromosome();
            if (this.current_chromosome == null) {
                current_chromosome = guichr;
            }
            int trees = 0;
            int trackx;
            int totaltrees = karyotracks.size();
            for (KaryoTrack kt : karyotracks) {
                if (!kt.isVisible()) {
                    continue;
                }
                // p("Adding track " + kt + " to chr " + c);
                FeatureTree tree = chr.getTree(kt);
                if (tree == null) {
                    //  p("Tree is null for this chromosome and kt, nee to load it");
                    tree = igvadapter.createTree(kt, chr);
                    chr.addTree(kt, tree);
                }

                 trackx = getTrackX(trees, guichr, 1, totaltrees);
                 
                if (tree == null) {
                    p("Could not create tree for chr " + chr);
                } else {
                    if (tree.getTotalNrChildren() == 0) {
                        //  p("Got a tree, but it has no items, won't show it");
                    } else {
                        
                        // p("Got variant tree  " + tree.getName() + " for chr " + chr.getName() + " with " + tree.getTotalNrChildren() + " features");
                        final GuiFeatureTree guitree = kt.getRenderType().getGuiTree(mainview.getCanvas(), guichr, tree, trackx);

                        guitrees.add(guitree);

                        mainview.addDrawable(guitree);
                        
                        Text st = new Text(kt.getShortName(),  guitree.getX()+3, guitree.getY()- 3, Color.black);
                        mainview.addText(st);
                       // p("Added text: "+st.getText()+", at "+st.getX()+"/"+st.getY());
                        guitree.addSelectionListener(new SelectionListener() {
                            @Override
                            public void selectionPerformed(SelectionEvent evt) {
                                // guichr.useMouseLocation(evt.getY());
                                int loc = guichr.getCurrentLocation();
                                //guichr.getLinkedView().setCurLocation(loc);
                                current_location = loc;
                                current_chromosome = guichr;
                                for (GuiChromosome gc : guichromosomes) {
                                    gc.setSelected(false, false);
                                }
                                guichr.setSelected(true, false);
                                p("Tree got selected of chr :" + guichr.getName() + ", location=" + current_location);
                                current_chromosome.setCurLocation(current_location);
                                if (evt.getActionCommand().equalsIgnoreCase("DOUBLE")) {
                                    p("Showing location " + loc + " in IGV");
                                    showLocation(guitree, guichr, loc);
                                }
                                p("Updating detail view and location, chr " + guichr.getName());
                                updateDetailView();
                                mainview.repaint();
                            }
                        });
                       
                        trees++;
                    }
                }
            }
        }
        man.setGuitrees(guitrees);
        man.filterTrees();
        updateDetailView();
    }

    public ZoomCanvas createOverviewView() {


        //  int dy = GuiChromosome.defaultheight + 50;
        chromosomes = man.getChromosomes();

        if (chromosomes == null) {
            return null;
        }
        int DTRACK = GuiFeatureTree.WIDTH + TRACKDX;
        int nrchr = chromosomes.size();
        int DXCHROMO = Math.max(120, man.getNrSelectdKaryoTracks() * DTRACK+TRACKDX);
        int width = 1400;
        int nrperrow = (width - 100) / DXCHROMO;
        int nrrows = nrchr / nrperrow + 1;
        int rowheights[] = new int[nrrows];
        int chrheights[] = new int[nrrows];
        int row = 0;
        int dy = 50;
        int height = 50;

        for (int c = 0; c < chromosomes.size(); c++) {
            Chromosome chr = chromosomes.get(c);
            int rh = (int) (GuiChromosome.getDefaultHeight(chr.getLength()));
            int cl = (int) chr.getLength();
            rowheights[row] = Math.max(rh, rowheights[row]);
            chrheights[row] = Math.max(cl, chrheights[row]);
            if (c >= (row * nrperrow + nrperrow)) {
                height += rowheights[row] + dy;
                row++;
            }
        }
        height += rowheights[row] + 2 * dy;

        final ZoomCanvas view = new ZoomCanvas((JFrame) man.getFrame(), width, height, GuiCanvas.NONE, false, true);
        p("Min zoom: "+view.getMinZoom());
     //   view.zoomOut();
        int startx = Math.max(120, man.getNrSelectdKaryoTracks() * DTRACK/2+TRACKDX);
        int starty = 50;

        int x = startx;
        int y = starty;

        p("Creating overview, DTRACK = " + DTRACK + ", nr visible tracks = " + man.getNrSelectdKaryoTracks());
       
        guichromosomes = new ArrayList<GuiChromosome>();

        int h = 0;
        row = 0;
       
        for (int c = 0; c < chromosomes.size(); c++) {
            final Chromosome chr = chromosomes.get(c);
            //   p("Got chromosome: "+chr);
            chr.findCenter();
           
            if (c> 0 && c % nrperrow == 0) {
                x = startx;
                y += rowheights[row] + dy;
                row++;                
            }

            //  h = (int) GuiChromosome.getDefaultHeight(chr.getLength());
            Point start = new Point(x, y);
            final GuiChromosome guichr = new GuiChromosome(view.getCanvas(), chr, start);
            guichr.setMovable(true);
            guichromosomes.add(guichr);
            if (this.current_chromosome == null) {
                current_chromosome = guichr;
            }
            view.addDrawable(guichr);
            // also create feature tree

            guichr.addSelectionListener(new SelectionListener() {
                @Override
                public void selectionPerformed(SelectionEvent evt) {
                    guichr.useMouseLocation(evt.getY());
                    int loc = guichr.getCurrentLocation();
                    current_location = loc;
                    current_chromosome = guichr;
                    p("Chr got selected:" + evt.toString());
                    if (evt.getActionCommand().equalsIgnoreCase("DOUBLE")) {
                        p("Showing location in IGV");
                        showLocation(guichr, loc);
                    }
                    updateDetailView();
                    view.repaint();
                }
            });

            x += DXCHROMO;
        }

        x = 20;
        y = starty;


        for (row = 0; row < nrrows; row++) {
            int rh = rowheights[row];
            int cl = chrheights[row];

            PhysicalCoord coord = new PhysicalCoord(view.getCanvas(), x, y, rh, cl);
            view.addDrawable(coord);
            y += rowheights[row] + dy;
        }

       
        
        return view;
    }

    protected void updateDetailView() {

        if (current_chromosome != null) {
            int where = Math.max(split.getDividerLocation(), 500);
            split.remove(detailview);

             if (detailview != null) {
                this.detailzoom = (int) detailview.getZoomValue();
             //   p("Old detail zoom value was: "+detailzoom);
            }
            detailview = createDetailView(current_chromosome);
            split.add(detailview);
            split.setDividerLocation(where);
            
            if (this.getCurrent_location()>0){ 
                double percent = (double)this.getCurrent_location()/(double)GuiChromosome.largest;
                int targety = (int)(detailview.getHeight()*percent);
                
                int vish = detailview.getVisibleRect().height;
                int half = vish/2;
             //   p("trying to scroll to "+targety +" in detail view, vish = "+vish);
                detailview.getCanvas().scrollRectToVisible(new Rectangle(0,targety-half, detailview.getViewRect().width, vish ));
            }
        } else {
            p("Got no currently selected chromosome, won't create detail view");
        }

    }

    protected int getTrackX(int treenr, final GuiChromosome guichr, int SCALEX, int totaltrees) {
        int trackx;
        int dx = 20;
        int half = totaltrees/2;
        // treenr starts at 0
        if (treenr >= half) {
            // right
            trackx = guichr.getWidth() + SCALEX * dx * (treenr-half + 1);
        } else {
            // left
            trackx = (int) (-dx * SCALEX * (half-treenr-0.5))+SCALEX*4 ;
        }
        return trackx;
    }

    public ZoomCanvas createDetailView(final GuiChromosome parentchr) {
        int SCALE = 5;
        int SCALEX = 2;
        final int width = 400;
        final int height = GuiChromosome.defaultheight * SCALE + 100;
        ArrayList<KaryoTrack> karyotracks = man.getSelectdKaryoTracks();

        int startx = Math.max(130, 120 + ((karyotracks.size()) / 2) * 30);
        int starty = 100;
        final ZoomCanvas view = new ZoomCanvas((JFrame) man.getFrame(), width, height, GuiCanvas.NONE, false, true);

        int x = startx;
        int y = starty;
        ArrayList<GuiFeatureTree> guitrees = new ArrayList<GuiFeatureTree>();

        final Chromosome chr = parentchr.getChromosome();

        Point start = new Point(x, y);

        final GuiChromosome guichr = new GuiChromosome(view.getCanvas(), chr, start, SCALE);


       // p("Current location of detal chr: " + current_location + "/" + parentchr.getCurrentLocation());
        guichr.setCurLocation(parentchr.getCurrentLocation());
        guichr.setSelected(true, false);
        guichr.setLinkedView(parentchr);
        parentchr.setLinkedView(guichr);
        // int h = (int) guichr.getHeight(chr.getLength());
        view.addDrawable(guichr);
        // also create feature tree
        int trackx = -5 * SCALEX;

        guichr.addSelectionListener(new SelectionListener() {
            @Override
            public void selectionPerformed(SelectionEvent evt) {
                guichr.useMouseLocation(evt.getY());

                int loc = guichr.getCurrentLocation();
                parentchr.setCurLocation(loc);
                current_location = loc;
                current_chromosome = guichr;
                p("Detail Chr got selected:" + evt.toString());
                p("Also update loc of parent");
                for (GuiChromosome gc : guichromosomes) {
                    gc.setSelected(false, false);
                }
               
                guichr.getLinkedView().setCurLocation(current_location);
                guichr.getLinkedView().setSelected(true, false);
                
                if (evt.getActionCommand().equalsIgnoreCase("DOUBLE")) {
                    p("Showing location in IGV");
                    showLocation(null, guichr, loc);
                }
                view.repaint();
                mainview.repaint();
            }
        });
        int trees = 0;
        int totaltrees = karyotracks.size();
        for (KaryoTrack kt : karyotracks) {
            if (!kt.isVisible()) {
                continue;
            }
            FeatureTree tree = chr.getTree(kt);
            if (tree == null) {
                tree = igvadapter.createTree(kt, chr);
                chr.addTree(kt, tree);
            }
             trackx = getTrackX(trees, guichr, SCALEX, totaltrees);

            if (tree != null) {
                //  p("Got variant tree  " + tree.getName() + " for chr " + chr.getName() + " with " + tree.getTotalNrChildren() + " features");
                if (tree.getTotalNrChildren() == 0) {
                    // not adding tree
                } else {
                    final GuiFeatureTree guitree = kt.getRenderType().getGuiTree(mainview.getCanvas(), guichr, tree, trackx);

                     
                    Text st = new Text(kt.getShortName(),  guitree.getX()-1, guitree.getY() - 3, Color.black);
                    view.addDrawable(st);
                        
                    //GuiFeatureTree guitree = new GuiFeatureTree(kt, view.getCanvas(), guichr, tree, trackx);
                    guitree.setWidth(SCALEX * guitree.getWidth());
                    guitrees.add(guitree);
                    guitree.filter();
                    guitree.addSelectionListener(new SelectionListener() {
                        @Override
                        public void selectionPerformed(SelectionEvent evt) {
                            guichr.useMouseLocation(evt.getY());

                            int loc = guichr.getCurrentLocation();
                            parentchr.setCurLocation(loc);
                            current_location = loc;
                            current_chromosome = guichr;
                             for (GuiChromosome gc : guichromosomes) {
                                    gc.setSelected(false, false);
                             }
                             guichr.setSelected(true, false);
                             guichr.getLinkedView().setSelected( true, false);
                             guichr.getLinkedView().setCurLocation( current_location);
                            p("Detail tree got selected of chr :" + guichr.getName() + ", location=" + current_location);
                            current_chromosome.setCurLocation(current_location);
                            if (evt.getActionCommand().equalsIgnoreCase("DOUBLE")) {
                                p("Showing location in IGV");
                                showLocation(guitree, guichr, loc);
                            }
                            view.repaint();
                            mainview.repaint();
                        }
                    });

                    view.addDrawable(guitree);
                   
                    trees++;
                }
            } else {
                p("Got no tree for chr " + chr.getName());
            }
        }

        x = 20;
        y = starty;

        int largest = (int) GuiChromosome.largest;

        int h = (int) GuiChromosome.getDefaultHeight(largest);
        PhysicalCoord coord = new PhysicalCoord(view.getCanvas(), x, y, h * SCALE, largest);
        view.addDrawable(coord);
        view.setSliderValuePercent(detailzoom);
        return view;
    }

    public void showLocation(GuiChromosome gchr, int loc) {
        showLocation(null, gchr, loc);
    }

    public void showLocation(GuiFeatureTree gtree, GuiChromosome gchr, int loc) {
        // check exactly there are features
        Chromosome chr = gchr.getChromosome();
        int s = 0;
        int e = 0;
        int MB = 1000000;
        int nrMB = 10;
        int WINDOW = 150000;
        ArrayList<GuiFeatureTree> trees;
        if (gtree != null) {
            trees = new ArrayList<GuiFeatureTree>();
            trees.add(gtree);
            p("Searching in tree " + gtree.getClass().getName());
        } else {
            p("Searching in ALL visible trees");
            trees = gchr.getTrees();
        }
        if (trees != null) {
            p("Finding closest feature at " + loc + " with window " + nrMB + "MB");
            boolean found = false;
            for (GuiFeatureTree gt : trees) {
                if (gt.isVisible()) {
                    List<KaryoFeature> features = gt.getTree().getFeaturesAt(loc, nrMB * MB);
                    if (features != null) {
                        for (KaryoFeature f : features) {
                            int delta = Math.abs(loc - (f.getStart() + f.getEnd()) / 2);
                            if (delta < Math.abs(loc - s)) {
                                s = f.getStart();
                                e = f.getEnd();
                                p("Found closer feature " + f);
                                found = true;
                            }
                        }
                    }
                }
            }
            if (!found) {
                p("Found no feature there for selected tree");
            }
        }
        if (s == e || s == 0) {
            s = loc;
            e = loc + WINDOW;
        }
        s = Math.max(s, 0);
        int delta = e - s;
        if (e - s < WINDOW + WINDOW / 4) {
            int add = (WINDOW - delta) / 2 + WINDOW / 4;
            s = Math.max(0, s - add);
            e = (int) Math.min(chr.getLength(), e + add);
        }

        e = Math.max(s, e);
        String name = chr.getName();
        name = StringTools.replace(name, "chrchr", "chr");
        p("Got chr name: " + chr.getName() + "/" + name);
        igvadapter.showLocation(name, s, e);
    }

    private void p(String msg) {
        // Logger.getLogger("KaryoOverviewPanel").info(msg);
        System.out.println("KaryoOverviewPanel: " + msg);
    }

    /**
     * @return the current_location
     */
    public int getCurrent_location() {
        return current_location;
    }

    /**
     * @return the current_chromosome
     */
    public GuiChromosome getCurrent_chromosome() {
        return current_chromosome;
    }

    /**
     * @param current_chromosome the current_chromosome to set
     */
    public void setCurrent_chromosome(GuiChromosome current_chromosome) {
        this.current_chromosome = current_chromosome;
    }
}
