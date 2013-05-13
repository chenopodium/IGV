/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;

/**
 *
 * @author Chantal
 */
public abstract class PreferencesTabPanel extends JPanel implements PreferencesTab {

    private static Logger log = Logger.getLogger(PreferencesTabPanel.class);
    protected boolean inputValidated = true;
    protected PreferenceManager prefMgr = PreferenceManager.getInstance();
    protected Map<String, String> updatedPreferenceMap = Collections.synchronizedMap(new HashMap<String, String>() {
        @Override
        public String put(String k, String v) {
            String oldValue = prefMgr.get(k);
            if ((v == null && oldValue != null) || !v.equals(oldValue)) {
                return super.put(k, v);
            }
            return v;
        }
    });

    protected void p(String s) {
        log.info(s);
        //System.out.println("PreferencesTabPanel: "+s);
    }

    @Override
    public void okButtonClicked() {
        p("Ok clicked");
        if (inputValidated) {
            // Store the changed preferences
            prefMgr.putAll(updatedPreferenceMap);
            updatedPreferenceMap.clear();
        } else {
            p("Input is NOT valudated - resetting");
            resetValidation();
        }
    }

    @Override
    public abstract void initValues();

    @Override
    public Map<String, String> getUpdatedPreferenceMap() {
        return updatedPreferenceMap;
    }

    protected void resetValidation() {
        // Assume valid input until proven otherwise
        inputValidated = true;
    }
}
