package com.ubird.astar.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.FontUIResource;

public class AStarMenuBar extends JMenuBar implements ActionListener, ItemListener{

	private static final long serialVersionUID = -6451299661770509733L;
	
	private static final String LINE_DELEMITER = ";";
	private static final String DATA_DELEMITER = ",";

	enum MainMenu{

		File("File",  'F'),
		View("View",  'V'),
		Help("Help",  'H')
		;
		
		private String name;
		private char mnemonic;
		private SubMenu[] subMenus;
		MainMenu(String name, char mnemonic){
			this.name = name;
			this.mnemonic = mnemonic;
		}
		public String getName() {
			return name;
		}
		public char getMnemonic() {
			return mnemonic;
		}
		public SubMenu[] getSubMenus() {
			if(subMenus==null){
				List<SubMenu> tmp = new LinkedList<SubMenu>();
				for(SubMenu subMenu : SubMenu.values()){
					if(subMenu.getFatherMenu()==this){
						tmp.add(subMenu);
					}
				}
				subMenus = new SubMenu[tmp.size()];
				subMenus = tmp.toArray(subMenus);
			}
			return subMenus;
		}
	}
	
	enum SubMenu{
		
		LoadMap("Load Map", 'L', MainMenu.File),
		SaveMap("Save Map", 'S', MainMenu.File),
		Quit("Quit", 'Q', MainMenu.File),
		
		Tips("Tips", 'T', MainMenu.Help),
		About("About", 'A', MainMenu.Help),
		
		DrawOpenList("DrawOpenList", 'O', MainMenu.View, MenuType.CheckBox)
		;
		
		private String name;
		private char mnemonic;
		private MainMenu fatherMenu; 
		private MenuType menuType;		
		
		SubMenu(String name, char mnemonic, MainMenu father){
			this(name, mnemonic, father, MenuType.MenuItem);
		}
		
		SubMenu(String name, char mnemonic, MainMenu father, MenuType menuType){
			this.name = name;
			this.mnemonic = mnemonic;
			this.fatherMenu = father;
			this.menuType = menuType;
		}
		public String getName() {
			return name;
		}
		public char getMnemonic() {
			return mnemonic;
		}
		public MainMenu getFatherMenu(){
			return fatherMenu;
		}
		public MenuType getMenuType(){
			return menuType;
		}
		
		public static SubMenu getByName(String name){
			for(SubMenu subMenus : SubMenu.values()){
				if((subMenus.getName()+"("+subMenus.getMnemonic()+")").equals(name)){
					return subMenus;
				}
			}
			return null;
		}
	}
	
	enum MenuType{
		MenuItem,
		CheckBox
	}
	
	private AstarPanel astarPanel;
	
	public AStarMenuBar(){
		super();
		initMenus();
	}

	public AStarMenuBar(AstarPanel astarPanel) {
		this();
		this.astarPanel = astarPanel;
	}

	private void initMenus() {
		for(MainMenu meun : MainMenu.values()){
			JMenu jmenu = new JMenu(meun.getName()+"("+meun.getMnemonic()+")");
			jmenu.setMnemonic(meun.getMnemonic());
			
			for(SubMenu subMenu : meun.getSubMenus()){
				JMenuItem menuItem = new JMenuItem(subMenu.getName()+"("+subMenu.getMnemonic()+")");
				switch (subMenu.getMenuType()) {
				case MenuItem:
					menuItem = new JMenuItem(subMenu.getName()+"("+subMenu.getMnemonic()+")");
					menuItem.addActionListener(this);
					break;
				case CheckBox:
					menuItem = new JCheckBoxMenuItem(subMenu.getName()+"("+subMenu.getMnemonic()+")");
					menuItem.addItemListener(this);
					break;
				default:
					break;
				}
				menuItem.setMnemonic(subMenu.getMnemonic());
				jmenu.add(menuItem);
			}
			
			add(jmenu);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		SubMenu byName = SubMenu.getByName(e.getActionCommand());
		switch (byName) {
		case LoadMap:
			int[][] data = loadData();
			astarPanel.setAStarData(data );
			break;
		case SaveMap:
			int[][] drawData = astarPanel.getAStarData();
			saveData2File(drawData);
			break;
		case Quit:
			System.exit(0);
			break;
		case Tips:
			showTips();
			break;
		case About:
			showAbout();
			break;
		default:
			break;
		}
	}

	private void showTips() {
		JDialog dialog = new JDialog();
		dialog.setTitle("How to use");
		JTextArea tarea = new JTextArea();
		tarea.setWrapStyleWord(true);
		String tips = "----------------------------------------------------------------------------------------------------------\n" +
								"Start button : find the path between the source node and target node \n" +
								"ClearPath button : Clear the path which you have found on the screen\n" +
								"ClearMap Button : Clear the BARRIER which you set on the map\n" +
								"----------------------------------------------------------------------------------------------------------\n\n" +
								"Thx for your use!\n" +
								"You can report the bugs to me by email(327112182@qq.com).";
		tarea.setText(tips);
		tarea.setEditable(false);
		JScrollPane panel = new JScrollPane(tarea);
		panel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		dialog.add(panel);
		dialog.setSize(512, 384);
		dialog.setLocationRelativeTo(astarPanel);
		dialog.setVisible(true);
	}

	private void showAbout() {
		JDialog dialog = new JDialog();
		dialog.setTitle("About the author");
		dialog.setSize(512, 384);
			dialog.add(new JPanel(){
				private BufferedImage image;
				{
					try {
						image = ImageIO.read(getClass().getClassLoader().getResource("com/ubird/ui/res/about.jpg"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if(image!=null)
						g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
					
					
					int fontSize = 14;
					int shadowWidth = 3;
					Color shadowColor 	= new Color(0,0,255);
					Color fontColor		= new Color(200,200,255);
					
					drawShadowString(g, "The Author : JohnCha", fontSize, 20, 20, shadowWidth, shadowColor, fontColor);
					drawShadowString(g, "Now work for cndw.com", fontSize, 20, 40, shadowWidth, shadowColor, fontColor);
					drawShadowString(g, "You can contact me with : ", fontSize, 20, 60, shadowWidth, shadowColor, fontColor);
					drawShadowString(g, "E-mail : 327112182@qq.com", fontSize, 40, 100, shadowWidth, shadowColor, fontColor);
					drawShadowString(g, "QQ      : 327112182", fontSize, 40, 80, shadowWidth, shadowColor, fontColor);
				}
				
				private void drawShadowString(Graphics g, String text, int fontSize, int x, int y, int shadowWidth, Color shadowColor, Color fontColor){
					g.setFont(new Font("Monospace", Font.PLAIN, fontSize));
					
					g.setColor(shadowColor);
					Graphics2D g2d = (Graphics2D) g;
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
					for(int i=1; i<=shadowWidth; i++){
						g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f+0.2f*(shadowWidth-i)/shadowWidth));
						for(int j=0; j<8; j++){
							int offsetX = j%3 -1;
							int offsetY = j/3 -1;
							g.drawString(text, x+offsetX*i, y+offsetY*i);
						}
					}
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
					g.setColor(fontColor);
					g.drawString(text, x, y);
				}
			});
		dialog.setLocationRelativeTo(astarPanel);
		dialog.setVisible(true);
	}

	private int[][] loadData() {
		JFileChooser chooser = new JFileChooser();
		int showOpenDialog = chooser.showOpenDialog(astarPanel);
		if(showOpenDialog==JFileChooser.OPEN_DIALOG){
			File selectedFile = chooser.getSelectedFile();
			if(selectedFile.exists()){
				try {
					BufferedReader breader = new BufferedReader(new FileReader(selectedFile));
					StringBuilder sb = new StringBuilder();
					String tmp = null;
					while((tmp = breader.readLine())!=null){
						sb.append(tmp);
					}
					String[] split = sb.toString().split(LINE_DELEMITER);	//行
					int lineNum = split.length;
					String[] tmpSplit = split[0].split(DATA_DELEMITER);
					int rowNum = tmpSplit.length;
					
					int[][] data = new int[lineNum][rowNum];
					for(int i=0; i<split.length; i++){
						String[] split2 = split[i].split(DATA_DELEMITER);
						for(int j=0; j<split2.length; j++){
							data[i][j] = Integer.valueOf(split2[j].trim());
						}
					}
					return data;
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
					e.printStackTrace();
				} catch(Exception e){
					JOptionPane.showMessageDialog(null, "数据格式出错");
					e.printStackTrace();
				}
			}else{
				JOptionPane.showMessageDialog(null, "不存在文件："+selectedFile);
			}
		}
		return null;
	}

	private void saveData2File(int[][] drawData) {
		if(drawData!=null){
			JFileChooser chooser = new JFileChooser();
			int showOpenDialog = chooser.showOpenDialog(astarPanel);
			if(showOpenDialog==JFileChooser.OPEN_DIALOG){
				File selectedFile = chooser.getSelectedFile();
				
				final int CAN_WRITE = 0;
				final int PERMISSION_DENINE = 1;
				
				int status = -1;
				if(selectedFile.exists()){	//如果文件存在，提示是否覆盖
					int result = JOptionPane.showConfirmDialog(null, "该文件已经存在，是否覆盖");
					if(result == JOptionPane.YES_OPTION){
						if(selectedFile.canWrite())
							status = CAN_WRITE;
						else
							status = PERMISSION_DENINE;
					}
				}else{
					try {
						if(selectedFile.createNewFile()){	//尝试创建新文件
							status = CAN_WRITE;
						}else{
							status = PERMISSION_DENINE;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(status==PERMISSION_DENINE){
					JOptionPane.showMessageDialog(null, "Permission Denine");
				}else if(status==CAN_WRITE){
					try {
						FileWriter fwriter = new FileWriter(selectedFile);
						BufferedWriter bwriter = new BufferedWriter(fwriter);
						for(int i=0; i<drawData.length; i++){
							StringBuilder sb = new StringBuilder();
							for(int j=0; j<drawData[i].length; j++){
								if(j!=0)
									sb.append(DATA_DELEMITER);
								sb.append(drawData[i][j]);
							}
							if(i<drawData.length-1)
								sb.append(LINE_DELEMITER);
							bwriter.write(sb.toString());
						}
						bwriter.flush();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "文件写入失败");
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		JMenuItem item = (JMenuItem)e.getItem();
		SubMenu byName = SubMenu.getByName(item.getActionCommand());
		switch (byName) {
		case DrawOpenList:
			if(e.getStateChange() == ItemEvent.SELECTED){
				astarPanel.drawOpenList(true);
			}else{
				astarPanel.drawOpenList(false);
			}
			break;

		default:
			break;
		}
	}
}