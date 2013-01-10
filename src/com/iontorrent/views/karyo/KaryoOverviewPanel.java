/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.karyo;

import com.iontorrent.data.karyo.*;
import com.iontorrent.event.SelectionEvent;
import com.iontorrent.event.SelectionListener;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.views.basic.GuiCanvas;
import com.iontorrent.views.basic.ZoomCanvas;
import com.iontorrent.views.karyo.drawables.*;
import java.awt.BorderLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

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
    
    public KaryoOverviewPanel(KaryoManager man) {
        this.man = man;
        igvadapter = man.getIgvAdapter();
        setLayout(new BorderLayout());

    }

    public void createView() {

        mainview = createOverviewView();
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.add(mainview);
        add("Center", split);
        split.setDividerLocation(800);
        if (guichromosomes != null && guichromosomes.size() > 1) {
            detailview = createDetailView(guichromosomes.get(0));
            split.add(detailview);
        }
        p("==============Main View created with bands without tracks");
        
    }

    public void loadTracks() {
        man.loadTracks(new TaskListener() {

            @Override
            public void taskDone(Task task) {
                p("==================Loading tracks done, adding them to overview");
                addTracksToOverview();
            }
        });
    }

    /**
     * add any newly loaded tracks
     */
    private void addTracksToOverview() {
        ArrayList<GuiFeatureTree> guitrees = new ArrayList<GuiFeatureTree>();

        ArrayList<KaryoTrack> karyotracks = man.getKaryoTracks();
        for (int c = 0; c < guichromosomes.size(); c++) {
            final GuiChromosome guichr = guichromosomes.get(c);
            final Chromosome chr = guichr.getChromosome();
            if (this.current_chromosome == null) current_chromosome = guichr;
            int trees = 0;
            int trackx = -5;
            for (KaryoTrack kt : karyotracks) {
                FeatureTree tree = igvadapter.createTree(kt, chr);

                if (tree != null) {
                    // p("Got variant tree  " + tree.getName() + " for chr " + chr.getName() + " with " + tree.getTotalNrChildren() + " features");
                    final GuiFeatureTree guitree;
                    if (kt.getTrack() instanceof FakeCnvTrack) {
                        guitree = new GuiCNVTree(kt, mainview.getCanvas(), guichr, tree, trackx);
                    } else if (kt.getTrack() instanceof FakeIndelTrack) {
                        guitree = new GuiIndelTree(kt, mainview.getCanvas(), guichr, tree, trackx);
                    } else {
                        guitree = new GuiFeatureTree(kt, mainview.getCanvas(), guichr, tree, trackx);
                    }

                    guitrees.add(guitree);

                    mainview.addDrawable(guitree);
                    guitree.addSelectionListener(new SelectionListener() {

                        @Override
                        public void selectionPerformed(SelectionEvent evt) {
                            guichr.useMouseLocation(evt.getY());
                            int loc = guichr.getCurrentLocation();
                            //guichr.getLinkedView().setCurLocation(loc);
                            current_location = loc;
                            current_chromosome = guichr;
                            p("Chr got selected:" + evt.toString());
                            if (evt.getActionCommand().equalsIgnoreCase("DOUBLE")) {
                                p("Showing location in IGV");
                                showLocation(guitree, guichr, loc);
                            }
                            mainview.repaint();
                        }
                    });
                    trackx = getTrackX(trees, guichr, 1);
                    trees++;
                } else {
                    p("Got no tree for chr " + chr.getName());
                }
            }
        }
        man.setGuitrees(guitrees);
        man.filterTrees();
        updateDetailView();
    }

    public ZoomCanvas createOverviewView() {

        int dy = GuiChromosome.defaultheight + 50;

        int width = 1400;
        int height = 1000;
        final ZoomCanvas view = new ZoomCanvas((JFrame) man.getFrame(), width, height, GuiCanvas.NONE, false, true);

        int startx = 150;
        int starty = 50;
    
        int x = startx;
        int y = starty;
        int dx = 110;
        chromosomes = man.getChromosomes();
        guichromosomes = new ArrayList<GuiChromosome>();

        int h = 0;
        for (int c = 0; c < chromosomes.size(); c++) {
            final Chromosome chr = chromosomes.get(c);
            chr.findCenter();
            if (x > width - dx) {
                x = startx;
                y += dy;
            }
            //  h = (int) GuiChromosome.getDefaultHeight(chr.getLength());
            Point start = new Point(x, y);
            final GuiChromosome guichr = new GuiChromosome(view.getCanvas(), chr, start);
            guichr.setMovable(true);
            guichromosomes.add(guichr);
            if (this.current_chromosome == null) current_chromosome = guichr;
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
                    } else {
                        p("Chr got selected for DETAIL view:" + evt.toString());
                        updateDetailView();

                    }
                    view.repaint();
                }
            });

            x+= dx;
        }

        x = startx - 100;
        y = starty;

        int largest = (int) GuiChromosome.largest;
        h = (int) GuiChromosome.getDefaultHeight(largest);
        PhysicalCoord coord = new PhysicalCoord(view.getCanvas(), x, y, h, largest);
        view.addDrawable(coord);

        y += dy;

        coord = new PhysicalCoord(view.getCanvas(), x, y, h / 2, largest / 2);
        view.addDrawable(coord);
        return view;
    }

    protected void updateDetailView() {
        
        if (current_chromosome != null) {
            int where = split.getDividerLocation();
            split.remove(detailview);

            detailview = createDetailView(current_chromosome);
            split.add(detailview);
            split.setDividerLocation(where);
         }
        else p("Got no currently selected chromosome, won't create detail view");
        
    }

    protected int getTrackX(int trees, final GuiChromosome guichr, int SCALEX) {
        int trackx;
        int dx = 15;
        if (trees % 2 == 0) {
            trackx = guichr.getWidth() + SCALEX * dx * (trees / 2 + 1);
        } else {
            trackx = -dx * SCALEX * (trees + 1) / 2;
        }
        return trackx;
    }

    public ZoomCanvas createDetailView(final GuiChromosome parentchr) {
        int SCALE = 5;
        int SCALEX = 2;
        final int width = 400;
        final int height = GuiChromosome.defaultheight * SCALE + 100;
        int startx = 100;
        int starty = 100;
        final ZoomCanvas view = new ZoomCanvas((JFrame) man.getFrame(), width, height, GuiCanvas.NONE, false, true);

        int x = startx;
        int y = starty;
        ArrayList<GuiFeatureTree> guitrees = new ArrayList<GuiFeatureTree>();
        ArrayList<KaryoTrack> karyotracks = man.getKaryoTracks();
        final Chromosome chr = parentchr.getChromosome();

        Point start = new Point(x, y);

        final GuiChromosome guichr = new GuiChromosome(view.getCanvas(), chr, start, SCALE);
        
        guichr.setCurLocation(parentchr.getCurLocation());
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
                p("Chr got selected:" + evt.toString());
                if (evt.getActionCommand().equalsIgnoreCase("DOUBLE")) {
                    p("Showing location in IGV");
                    showLocation(null, guichr, loc);
                }
                view.repaint();
            }
        });
        int trees = 0;
        for (KaryoTrack kt : karyotracks) {
            FeatureTree tree = igvadapter.createTree(kt, chr);

            if (tree != null) {
                //  p("Got variant tree  " + tree.getName() + " for chr " + chr.getName() + " with " + tree.getTotalNrChildren() + " features");
                final GuiFeatureTree guitree;
                if (kt.getTrack() instanceof FakeCnvTrack) {
                    guitree = new GuiCNVTree(kt, view.getCanvas(), guichr, tree, trackx);
                } else if (kt.getTrack() instanceof FakeIndelTrack) {
                    guitree = new GuiIndelTree(kt, view.getCanvas(), guichr, tree, trackx);
                } else {
                    guitree = new GuiFeatureTree(kt, view.getCanvas(), guichr, tree, trackx);
                }
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
                        p("Chr got selected:" + evt.toString());
                        if (evt.getActionCommand().equalsIgnoreCase("DOUBLE")) {
                            p("Showing location in IGV");
                            showLocation(guitree, guichr, loc);
                        }
                        view.repaint();
                    }
                });

                view.addDrawable(guitree);
                trackx = getTrackX(trees, guichr, SCALEX);
                trees++;
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
        int WINDOW = 3000;
        ArrayList<GuiFeatureTree> trees;
        if (gtree != null) {
            trees = new ArrayList<GuiFeatureTree>();
            trees.add(gtree);
        } else {
            trees = gchr.getTrees();
        }
        if (trees != null) {
            p("Finding closest feature at " + loc);
            for (GuiFeatureTree gt : trees) {
                List<KaryoFeature> features = gt.getTree().getFeaturesAt(loc, 4 * MB);
                if (features != null) {
                    for (KaryoFeature f : features) {
                        int delta = Math.abs(loc - f.getStart());
                        if (delta < Math.abs(loc - s)) {
                            s = f.getStart();
                            e = f.getEnd();
                            p("Found closer feature " + f);
                        }
                    }
                }
            }
        }
        if (s == e || s == 0) {
            s = loc;
            e = loc + WINDOW;
        }
         s = Math.max(s, 0);
        int delta = e - s;
        if (e - s < WINDOW) {
            int add = (WINDOW - delta) / 2;
            s = Math.max(0, s - add);
            e = (int) Math.min(chr.getLength(), e + add);
        }
       
        e = Math.max(s, e);
        igvadapter.showLocation(chr.getName(), s, e);
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
