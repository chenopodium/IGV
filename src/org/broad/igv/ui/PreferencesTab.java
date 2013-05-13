/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.ui;

import java.util.Map;

/**
 *
 * @author Chantal
 */
public interface PreferencesTab {

    public Map<String, String> getUpdatedPreferenceMap();

    public void initValues();

    public void okButtonClicked();
}
