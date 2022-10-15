package com.ubird.astar.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import com.ubird.astar.core.AStarMap;
import com.ubird.astar.core.AStarNode;

public class AstarPanel extends JPanel implements MouseListener,
		MouseMotionListener, KeyListener {

	private static final long serialVersionUID = 7286622284205423626L;

	private final static long FPS = 24;
	private final static long SPF = 1000 / 24;

	private final int gridWidth;
	private final int gridHeight;
	private final int xGridNum;
	private final int yGridNum;

	private final int totalWidth;
	private final int totalHeight;

	/** 线条颜色 */
	private final Color GRID_LINE_COLOR = new Color(64, 64, 64);
	/** 障碍物颜色 */
	private static final Color BARRIER_COLOR = new Color(128, 128, 128);

	/** 目标颜色 */
	private final Color TARGET_COLOR = new Color(255, 0, 0);
	/** 源颜色 */
	private final Color SOURCE_COLOR = new Color(0, 0, 255);

	AStarMap astarMap;

	private List<AStarNode> path;
	private int currentDrawIndex = 0;

	private static final Color DRAW_DATA_COLOR = new Color(255, 128, 64);
	private static final Composite DRAW_DATA_COMPOSITE = AlphaComposite
			.getInstance(AlphaComposite.SRC_OVER, 0.7f);
	private static final Composite NORMAL_COMPOSITE = AlphaComposite
			.getInstance(1);


	private Iterator<AStarNode> openList;
	private boolean isDrawOpenList;
	private static final Color OPEN_LIST_NODE_COLOR = new Color(64, 255, 64);


	private int[][] drawData;

	private boolean isShiftDown;
	private int lastX, lastY;
	private StatusPanel statusPanel;

	private static final Composite TIPS_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
	private static final Composite TIPS_COMPOSITE2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
	private static final Color TIPS_BG = new Color(0, 0, 0);
	private static final Color TIPS_FG = new Color(255, 255, 0);
	private Point tipsPoint = new Point();
	private boolean isDrawTips = false;


	public AstarPanel(int gridWidth, int gridHeight, int xGridNum, int yGridNum) {
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.xGridNum = xGridNum;
		this.yGridNum = yGridNum;
		drawData = new int[yGridNum][xGridNum];

		astarMap = new AStarMap(xGridNum, yGridNum);

		totalWidth = gridWidth * xGridNum;
		totalHeight = gridHeight * yGridNum;

		setPreferredSize(new Dimension(totalWidth + 1, totalHeight + 1));

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);

		startAnimatorThread();
	}

	private void startAnimatorThread() {
		new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(SPF);
						repaint();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		fillBarrier(g);
		fillTargetAndSourceGrid(g);
		fillOpenList(g);
		fillAStarPath(g);
		fillDrawData(g);
		drawGridLine(g);
		drawTips(g);
	}

	private void drawTips(Graphics g) {
		if (isDrawTips) {
			Graphics2D g2d = (Graphics2D) g.create();
			int width = 300;
			int height = 20;
			int arc = 5;
			int x = (tipsPoint.getLocation().x + 1) * gridWidth;
			int y = (tipsPoint.getLocation().y + 1) * gridHeight;
			if(x+width >= getPreferredSize().width){
				x = getPreferredSize().width - width - 15;
			}
			if(y+height >= getPreferredSize().height){
				y = getPreferredSize().height - height - 15;
			}
			g2d.setColor(TIPS_BG);
			g2d.setComposite(TIPS_COMPOSITE);
			g2d.fillRoundRect(x,  y , width, height, arc, arc);
			g2d.setComposite(TIPS_COMPOSITE2);
			g2d.setColor(TIPS_FG);
			g2d.drawString("Current grid num : (" + tipsPoint.getLocation().x + ", "+ tipsPoint.getLocation().y + ")", x + 15, y + 15);
			g2d.dispose();
		}
	}

	private void fillOpenList(Graphics g) {
		if (isDrawOpenList && astarMap != null) {
			openList = astarMap.getOpenList().iterator();
			if (openList != null) {
				while (openList.hasNext()) {
					fillNode(g, OPEN_LIST_NODE_COLOR, openList.next());
				}
			}
		}
	}

	private void fillDrawData(Graphics g) {
		if (drawData != null) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setComposite(DRAW_DATA_COMPOSITE);
			for (int i = 0; i < drawData.length; i++) {
				for (int j = 0; j < drawData[i].length; j++) {
					if (drawData[i][j] == 1) {
						fillNode(g2d, DRAW_DATA_COLOR, j * gridWidth, i
								* gridHeight);
					}
				}
			}
			g2d.setComposite(NORMAL_COMPOSITE);
		}
	}

	/**
	 * 绘制AStar路径
	 * 
	 * @param g
	 */
	private void fillAStarPath(Graphics g) {
		if (path != null) {
			for (int i = 0; i < currentDrawIndex; i++) {
				fillNode(g, new Color(255 * i / (path.size() - 1), 0, 255
						* (path.size() - i) / path.size()), path.get(i));
			}
			if (currentDrawIndex < path.size())
				currentDrawIndex++;
		}
	}

	/**
	 * 填充障碍物
	 * 
	 * @param g
	 */
	private void fillBarrier(Graphics g) {
		if (astarMap != null) {
			int[][] datas = astarMap.getAStarData();
			for (int i = 0; i < datas.length; i++) {
				for (int j = 0; j < datas[i].length; j++) {
					if (datas[i][j] == AStarMap.STATE_BARRIER) {
						fillNode(g, BARRIER_COLOR, j * gridWidth, i
								* gridHeight);
					}
				}
			}
		}
	}

	private void fillNode(Graphics g, Color barrierColor, int startXj,
			int startY) {
		g.setColor(barrierColor);
		g.fillRect(startXj, startY, gridWidth, gridHeight);
	}

	/**
	 * 填充目标和源格子
	 * 
	 * @param g
	 */
	private void fillTargetAndSourceGrid(Graphics g) {
		if (astarMap != null) {
			fillNode(g, TARGET_COLOR, astarMap.getTarget());
			fillNode(g, SOURCE_COLOR, astarMap.getSource());
		}
	}

	/**
	 * 填充AStar结点
	 * 
	 * @param g
	 * @param color
	 * @param target
	 */
	private void fillNode(Graphics g, Color color, AStarNode target) {
		if (target != null) {
			g.setColor(color);
			int x = target.getX() * gridWidth;
			int y = target.getY() * gridHeight;
			g.fillRect(x, y, gridWidth, gridHeight);
		}
	}

	/**
	 * 绘制格子线条
	 * 
	 * @param g
	 */
	private void drawGridLine(Graphics g) {
		g.setColor(GRID_LINE_COLOR);
		for (int i = 0; i <= yGridNum; i++) {
			g.drawLine(0, i * gridHeight, totalWidth, i * gridHeight);
		}
		for (int i = 0; i <= xGridNum; i++) {
			g.drawLine(i * gridWidth, 0, i * gridWidth, totalHeight);
		}
	}

	public void startFind() {
		if (astarMap != null) {
			if (astarMap != null) {
				long start = System.currentTimeMillis();
				path = astarMap.find();
				updateCostTimeMillis(System.currentTimeMillis() - start);
			}
		}
	}

	public void clearPath() {
		path = null;
		currentDrawIndex = 0;
		astarMap.clearOpenList();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = e.getX() / gridWidth;
			int y = e.getY() / gridHeight;
			if (isShiftDown) {
				for (int j = Math.min(y, lastY); j <= Math.max(y, lastY); j++) {
					for (int i = Math.min(x, lastX); i <= Math.max(x, lastX); i++) {
						if (j >= 0 && j < drawData.length && i >= 0
								&& i < drawData[0].length)
							drawData[j][i] = 1;
					}
				}
				rebuildAstarMap();
			} else {
				mouseDragged(e);
			}
			lastX = x;
			lastY = y;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			astarMap.initTargetAndSource(e.getX() / gridWidth, e.getY()
					/ gridHeight);
			clearPath();
			startFind();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		isDrawTips = true;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		isDrawTips = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getX() / gridWidth;
		int y = e.getY() / gridHeight;
		if (y >= 0 && y < drawData.length && x >= 0 && x < drawData[0].length)
			drawData[y][x] = 1;
		rebuildAstarMap();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		tipsPoint.setLocation(e.getX() / gridWidth, e.getY() / gridHeight);
	}

	public void rebuildAstarMap() {
		astarMap.loadData(drawData, 1, -1);
		if (drawData != null) {
			for (int i = 0; i < drawData.length; i++) {
				for (int j = 0; j < drawData[i].length; j++) {
					drawData[i][j] = 0;
				}
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		this.isShiftDown = e.isShiftDown();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		this.isShiftDown = e.isShiftDown();
	}

	private void updateCostTimeMillis(long time) {
		if (statusPanel != null)
			statusPanel.setCostTimeMillis(time);
	}

	public void setStatusPanel(StatusPanel statusPanel) {
		this.statusPanel = statusPanel;
	}

	public void clearMap() {
		for (int i = 0; i < drawData.length; i++) {
			for (int j = 0; j < drawData[i].length; j++) {
				drawData[i][j] = -1;
			}
		}
		rebuildAstarMap();
	}

	public int[][] getAStarData() {
		if (astarMap != null)
			return astarMap.getAStarData();
		return null;
	}

	public void setAStarData(int[][] data) {
		if (data != null && astarMap != null && this.drawData != null) {
			astarMap.loadData(data, 2, -1);
		}
	}

	public void drawOpenList(boolean isDrawOpenList) {
		this.isDrawOpenList = isDrawOpenList;
	}
}