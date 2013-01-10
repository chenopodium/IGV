/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.basic;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.*;

import javax.swing.*;

public abstract class BasicAction extends AbstractAction {
	protected JButton btn;

	protected JButton smallbtn;

	protected JButton iconbtn;

	protected Component parent;

	protected String name;

	protected boolean toolbarAction = true;

	protected KeyStroke keyStroke;

	public BasicAction(String name, Icon icon, Component parent) {
		super(name, icon);
		this.name = name;
		this.parent = parent;
		btn = new JButton(name, icon);
		smallbtn = new JButton(icon);
		iconbtn = new JButton(icon);
		init();
	}

	public BasicAction(String name, Icon icon) {
		this(name, icon, null);
	}

	public void setName(String n) {
	 
	    this.name = n;
	    btn.setName(n);
	}
	public void setIcon(Icon icon) {
        putValue(Action.SMALL_ICON, icon);
		smallbtn.setIcon(icon);
	}

	public BasicAction(String name) {
		this(name, null, null);
	}

	public BasicAction(String name, Component parent) {
		super(name);
		this.parent = parent;
		this.name = name;
		btn = new JButton(name);
		smallbtn = new JButton(name);
		iconbtn = new JButton();
		init();
	}

	private void init() {
		smallbtn.setMargin(new Insets(1, 1, 1, 1));
		smallbtn.setBorderPainted(false);
		iconbtn.setMargin(new Insets(1, 1, 1, 1));
		btn.addActionListener(this);
		//btn.setMargin(new Insets(1, 1, 1, 1));

		smallbtn.addActionListener(this);
		iconbtn.addActionListener(this);
		smallbtn.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent evt) {
				smallbtn.setBorderPainted(true);
			}

			public void mouseExited(MouseEvent evt) {
				smallbtn.setBorderPainted(false);
			}
		});

	}

	public String getToolTipText() {
		return btn.getToolTipText();
	}

	public void setToolTipText(String s) {
		btn.setToolTipText(s);
		smallbtn.setToolTipText(s);
		iconbtn.setToolTipText(s);
	}

	public JButton getButton() {
		return btn;
	}

	public JButton getIconButton() {
		return iconbtn;
	}

	public JButton getSmallButton() {
		return smallbtn;
	}

	public String getName() {
		return name;
	}

	public abstract void actionPerformed(ActionEvent e);

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		btn.setEnabled(enabled);
		smallbtn.setEnabled(enabled);
		iconbtn.setEnabled(enabled);
	}

	public boolean isToolbarAction() {
		return toolbarAction;
	}

	public void setToolbarAction(boolean b) {
		toolbarAction = b;
	}

	public void setKeyStroke(KeyStroke keystroke) {
		this.keyStroke = keystroke;
	}

	public KeyStroke getKeyStroke() {
		return keyStroke;
	}
}