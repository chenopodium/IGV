/*
*	Copyright (C) 2011 Life Technologies Inc.
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 2 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
* 
*  @Author Chantal Roth
*/
package com.iontorrent.views.karyo;

import com.iontorrent.data.karyo.KaryoTrack;
import com.iontorrent.data.karyo.filter.KaryoFilter;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.JLabel;

/**
 *
 * @author Chantal Roth
 */
public class FilterListPanel extends javax.swing.JPanel {

      KaryoManager man;
  
    /**
     * Creates new form FilterListPanel
     */
    public FilterListPanel(  KaryoManager man) {
        this.man = man;
        initComponents();
        int nr = 0;
        for (KaryoTrack t: man.getKaryoTracks()) {
            nr += t.getPossibleFilters().size();
            nr++;
        }
       
        setLayout(new GridLayout(nr, 1));
        for (KaryoTrack t: man.getKaryotracks()) {
            ArrayList<KaryoFilter> filters = t.getPossibleFilters();
            for (KaryoFilter filter: filters) {
                add(new JLabel("Filters for "+t.getName()));
                if (filter.isInitialized()) {
                    FilterPanel p = FilterPanel.createPanel(filter, t);
                    if (p != null) {
                        add(p);
                    }
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(FilterListPanel.class, "FilterListPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 203, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 273, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
