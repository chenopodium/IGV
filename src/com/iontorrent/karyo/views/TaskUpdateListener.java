/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import com.iontorrent.threads.TaskListener;

/**
 *
 * @author Chantal
 */
public interface TaskUpdateListener extends TaskListener {
    
    public void taskUpdated(int count);
}
