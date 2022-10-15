package com.ubird.astar.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ControlPanel extends JPanel {

	private static final long serialVersionUID = -3975281762676049494L;
	private AstarPanel astarPanel;
	
	public ControlPanel(){
		initComponents();
	}

	public ControlPanel(AstarPanel astarPanel) {
		this();
		this.astarPanel = astarPanel;
	}

	private void initComponents() {
		initStartButton();
	}

	private void initStartButton() {
		JButton startButton = new JButton("Start");
		add(startButton);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				astarPanel.requestFocus();
				astarPanel.startFind();
			}
		});
		
		JButton clearButton = new JButton("ClearPath");
		add(clearButton);
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				astarPanel.requestFocus();
				astarPanel.clearPath();
			}
		});
		
		JButton drawButton = new JButton("ClearMap");
		add(drawButton);
		drawButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				astarPanel.requestFocus();
				astarPanel.clearMap();
			}
		});
	}
}