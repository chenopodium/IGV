package com.iontorrent.event;

import java.util.ArrayList;
import javax.swing.Action;

public interface ContextActionHandlerIF {
    public ArrayList<Action> getContextSensitiveActions(ActionContext ctx);
}
