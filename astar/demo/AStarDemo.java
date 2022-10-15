package com.ubird.astar.demo;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.ubird.astar.ui.AStarMenuBar;
import com.ubird.astar.ui.AstarPanel;
import com.ubird.astar.ui.ControlPanel;
import com.ubird.astar.ui.StatusPanel;
import com.ubird.astar.ui.UFrame;

public class AStarDemo {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				UFrame frame = new UFrame();
				AstarPanel astarPanel = new AstarPanel(15,15,60,40);
				StatusPanel statusPanel = new StatusPanel();
				astarPanel.setStatusPanel(statusPanel);
				frame.getContentPane().add(astarPanel, BorderLayout.CENTER);
				frame.getContentPane().add(new ControlPanel(astarPanel), BorderLayout.NORTH);
				frame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
				frame.setJMenuBar(new AStarMenuBar(astarPanel));
				frame.pack();
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				astarPanel.requestFocus();
			}
		});
	}
}
