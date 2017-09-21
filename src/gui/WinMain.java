package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import java.awt.Color;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JTextArea;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.*;
import java.io.*;

public class WinMain {
	public static final int INIT=0;
	public static final int DEFAULT=1; // 浅蓝色
	public static final int OK=2; // 绿色
	public static final int ERROR=3; // 红色
	public static Color getColor(int value)
	{
		switch(value)
		{
		case DEFAULT:
			return new Color(0,153,255);
		case OK:
			return new Color(0,204,0);
		case ERROR:
			return new Color(204,51,0);
		default:
			return new Color(255,255,255);
		}
	}

	public static final String AS_DEFAULT="使用默认值";
	// 窗口组件
	private JFrame frame;
	private JTextField tfImportStatus;
	private JTextArea textArea;
	private PathItem[] pathItems;
	private JSeparator separator;
	private JButton button_start;
	private JButton button_save_as_default;
	private JButton button_load_default;
	
	// File组
	File[] paths=new File[9];
	

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WinMain window = new WinMain();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public WinMain() {
		initialize();
	}
	
	private void initialize() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);
		frame.setSize(860, 700);
		frame.getContentPane().setLayout(null);
		
		button_start = new JButton("一键执行");
		button_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for(PathItem temp:pathItems)
				{
					if(temp.status!=OK || temp.status!=DEFAULT)
						temp.redo.doClick();
				}
				boolean good=true;
				for(PathItem temp:pathItems)
				{
					if(temp.status==OK||temp.status==DEFAULT)
					{
						;
					}
					else
					{
						good=false;
						break;
					}
				}
				if(good)
				{
					button_save_as_default.setEnabled(false);
					button_load_default.setEnabled(false);
					button_start.setEnabled(false);
				}
			}
		});
		button_start.setFont(new Font("微软雅黑 Light", Font.PLAIN, 16));
		button_start.setBounds(271, 380, 200, 30);
		frame.getContentPane().add(button_start);
		
		tfImportStatus = new JTextField();
		tfImportStatus.setBackground(Color.BLACK);
		tfImportStatus.setForeground(Color.WHITE);
		tfImportStatus.setFont(new Font("微软雅黑 Light", Font.PLAIN, 16));
		tfImportStatus.setText("导入状态");
		tfImportStatus.setBounds(68, 595, 314, 30);
		frame.getContentPane().add(tfImportStatus);
		tfImportStatus.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(485, 13, 360, 639);
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBackground(Color.BLACK);
		textArea.setForeground(Color.WHITE);
		scrollPane.setViewportView(textArea);
		textArea.setFont(new Font("微软雅黑 Light", Font.PLAIN, 16));
		
		separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		separator.setBounds(14, 365, 457, 2);
		frame.getContentPane().add(separator);
		
		button_start = new JButton("开始生成");
		button_start.setFont(new Font("微软雅黑 Light", Font.PLAIN, 16));
		button_start.setBounds(271, 423, 200, 30);
		frame.getContentPane().add(button_start);
		
		button_save_as_default = new JButton("存为默认配置");
		button_save_as_default.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean allright=true;
				for(PathItem temp:pathItems)
				{
					if(temp.status==INIT || temp.status==ERROR)
					{
						allright=false;
						break;
					}
				}
				if(allright)
				{
					saveAsDefault();
					button_load_default.setEnabled(false);
					button_save_as_default.setEnabled(false);
				}
			}
		});
		button_save_as_default.setFont(new Font("微软雅黑 Light", Font.PLAIN, 16));
		button_save_as_default.setBounds(14, 380, 200, 30);
		frame.getContentPane().add(button_save_as_default);
		
		button_load_default = new JButton("读取默认配置");
		button_load_default.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean temp=loadDefault();
				button_save_as_default.setEnabled(!temp);
				button_load_default.setEnabled(!temp);
				button_start.setEnabled(!temp);
			}
		});
		button_load_default.setFont(new Font("微软雅黑 Light", Font.PLAIN, 16));
		button_load_default.setBounds(14, 423, 200, 30);
		frame.getContentPane().add(button_load_default);
		
		pathItems=addPathItems();
	}
	
	public void println(String contentIn)
	{
		textArea.setText(textArea.getText()+contentIn+'\n');
	}

	public void saveAsDefault()
	{
		println("尝试存为默认配置");
		File fileout=new File("cfg.ini");
		try {
		if(!fileout.exists()||fileout.isDirectory())
			fileout.createNewFile();
		Writer out=new FileWriter(fileout);
		for(PathItem temp:pathItems)
		{
			out.write(temp.getText().trim().equals(AS_DEFAULT)?AS_DEFAULT:temp.getText().trim());
			out.write("\r\n");
		}
		out.flush();
		out.close();
		println("成功存为默认配置");
		}
		catch (IOException e) {
			e.printStackTrace();
			if(fileout!=null)
				fileout.delete();
			println("保存默认配置出错");
		}
	}
	public boolean loadDefault()
	{
		println("尝试读取默认配置");
		File filein=new File("cfg.ini");
		try {
			Scanner in=new Scanner(filein);
			for(int step=0;in.hasNextLine() && step<9;step++)
			{
				String line=in.nextLine();
				paths[step]=line.equals(AS_DEFAULT)?null:new File(line);
				if(line.trim().equals(AS_DEFAULT))
					pathItems[step].setStatus(DEFAULT);
				else
					pathItems[step].setStatus(OK);
				
				pathItems[step].input.setText(line);
			}
			pathItems[1].input.setText(paths[1].getName());
			println("成功读取默认配置");
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			println("读取默认配置出错");
			for(int step=0;step<9;step++)
			{
				paths[step]=null;
			}
			return false;
		}
	}
	
	private PathItem[] addPathItems()
	{
		PathItem[] i=new PathItem[9];
		i[0]=new PathItem(frame, "目标src路径","","检测");
		i[0].redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String strPath=i[0].getText();
				paths[0]=new File(strPath);
				if(paths[0].exists() && paths[0].isDirectory())
				{
					i[0].setStatus(OK);
					println("目标src路径检测可用");
				}
				else
				{
					paths[0]=null;
					i[0].setStatus(ERROR);
					println("目标src路径检测出错");
				}
			}
		});
		i[1]=new PathItem(frame,"目标mod名称","","检测");
		i[1].redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str=i[1].getText().trim();
				if(str.length()==0)
				{
					i[1].setStatus(ERROR);
					println("目标mod名称不能为空");
				}
				else
				{
					for(char temp:str.toCharArray())
					{
						if(Character.isLetter(temp)||Character.isDigit(temp))
							;
						else
						{
							i[1].setStatus(ERROR);
							println("目标mod名称包含非法字符");
						}
					}
					i[1].setStatus(OK);
					println("目标mod名称可用");
				}
			}
		});
		i[2]=new PathItem(frame,"默认物品材质路径","","导入");
		i[2].redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str=i[2].getText().trim();
				if(str.length()==0)
				{
					i[2].setStatus(DEFAULT);
					println("物品材质使用默认值");
				}
				else
				{
					paths[2]=new File(str);
					if(paths[2].exists() && paths[2].isFile())
					{
						i[2].setStatus(OK);
						println("物品材质导入成功");
					}
					else
					{
						i[2].setStatus(ERROR);
						println("物品材质导入出错");
						paths[2]=null;
					}
				}
			}
		});
		i[3]=new PathItem(frame,"默认物品模型路径","","导入");
		i[3].redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str=i[3].getText().trim();
				if(str.length()==0)
				{
					i[3].setStatus(DEFAULT);
					println("物品模型使用默认值");
				}
				else
				{
					paths[3]=new File(str);
					if(paths[3].exists() && paths[3].isFile())
					{
						i[3].setStatus(OK);
						println("物品模型导入成功");
					}
					else
					{
						i[3].setStatus(ERROR);
						println("物品模型导入出错");
						paths[3]=null;
					}
				}
			}
		});
		i[4]=new PathItem(frame,"默认方块材质路径","","导入");
		i[4].redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str=i[4].getText().trim();
				if(str.length()==0)
				{
					i[4].setStatus(DEFAULT);
					println("方块材质使用默认值");
				}
				else
				{
					paths[4]=new File(str);
					if(paths[4].exists() && paths[4].isFile())
					{
						i[4].setStatus(OK);
						println("方块材质导入成功");
					}
					else
					{
						i[4].setStatus(ERROR);
						println("方块材质导入出错");
						paths[4]=null;
					}
				}
			}
		});
		i[5]=new PathItem(frame,"默认方块模型路径","","导入");
		i[5].redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str=i[5].getText().trim();
				if(str.length()==0)
				{
					i[5].setStatus(DEFAULT);
					println("方块模型使用默认值");
				}
				else
				{
					paths[5]=new File(str);
					if(paths[5].exists() && paths[5].isFile())
					{
						i[5].setStatus(OK);
						println("方块模型导入成功");
					}
					else
					{
						i[5].setStatus(ERROR);
						println("方块模型导入出错");
						paths[5]=null;
					}
				}
			}
		});
		i[6]=new PathItem(frame,"额外物品资源路径","","导入");
		i[6].redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str=i[6].getText().trim();
				if(str.length()==0)
				{
					i[6].setStatus(DEFAULT);
					println("额外物品资源文件使用默认值");
				}
				else
				{
					paths[6]=new File(str);
					if(paths[6].exists() && paths[6].isDirectory())
					{
						i[6].setStatus(OK);
						println("额外物品资源文件导入成功");
					}
					else
					{
						i[6].setStatus(ERROR);
						println("额外物品资源文件导入出错");
						paths[6]=null;
					}
				}
			}
		});
		i[7]=new PathItem(frame,"额外方块资源路径","","导入");
		i[7].redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str=i[7].getText().trim();
				if(str.length()==0)
				{
					i[7].setStatus(DEFAULT);
					println("额外方块资源文件使用默认值");
				}
				else
				{
					paths[7]=new File(str);
					if(paths[7].exists() && paths[7].isDirectory())
					{
						i[7].setStatus(OK);
						println("额外方块资源文件导入成功");
					}
					else
					{
						i[7].setStatus(ERROR);
						println("额外方块资源文件导入出错");
						paths[7]=null;
					}
				}
			}
		});
		i[8]=new PathItem(frame,"默认流体材质路径","","导入");
		i[8].redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String str=i[8].getText().trim();
				if(str.length()==0)
				{
					i[8].setStatus(DEFAULT);
					println("流体材质使用默认值");
				}
				else
				{
					paths[8]=new File(str);
					if(paths[8].exists() && paths[8].isFile())
					{
						i[8].setStatus(OK);
						println("流体材质导入成功");
					}
					else
					{
						i[8].setStatus(ERROR);
						println("流体材质导入出错");
						paths[8]=null;
					}
				}
			}
		});
		
		int step=0;
		for(PathItem tempI:i)
		{
			tempI.setLocations(13, 13+38*step++);
			tempI.setStatus(INIT);
		}
		
		return i;
	}
	
	private class PathItem
	{
		
		public int x,y;
		public JLabel caption;
		public JTextField input;
		public JButton redo;
		
		public int status;
		
		public PathItem(JFrame panelIn,String captionIn,String inputIn,String redoIn)
		{
			status=DEFAULT;
			
			caption=new JLabel(captionIn);
			caption.setSize(130,30);
			// caption.setLocation(x, y);
			caption.setFont(new Font("微软雅黑 Light", Font.PLAIN, 16));
			
			input=new JTextField();
			input.setText(inputIn);
			input.setSize(230, 30);
			// input.setLocation(x+124,y);
			input.setFont(new Font("微软雅黑 Light", Font.PLAIN, 16));
			
			redo=new JButton(redoIn);
			redo.setSize(70,30);
			// redo.setLocation(x+368,y);
			redo.setFont(new Font("微软雅黑 Light", Font.PLAIN, 16));
			
			panelIn.getContentPane().add(caption);
			panelIn.getContentPane().add(input);
			panelIn.getContentPane().add(redo);
		}
		
		public void setLocations(int xIn,int yIn)
		{
			x=xIn;
			y=yIn;
			
			caption.setLocation(x, y);
			input.setLocation(x+144,y);
			redo.setLocation(x+388,y);
		}
		
		public String getText()
		{
			return new String(input.getText());
		}
		
		public void setStatus(int value)
		{
			status=value;
			redo.setBackground(getColor(value));
			if(value==INIT)
				return;
			if(value==OK || value==DEFAULT)
			{
				redo.setEnabled(false);
				redo.setText("锁定");
				input.setEnabled(false);
			}
			if(value==DEFAULT)
				input.setText(AS_DEFAULT);
		}
	}
}
