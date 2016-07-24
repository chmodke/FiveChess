//实验名称，实验内容,实验目的,步骤,实验结果
package game;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class FiveChess {
	public static void main(String[] args) {
		GameF game = new GameF();
		game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game.show();
	}
}

class GameF extends JFrame {
	public static int wight=50;
	public GameF() {
		Container contentPane = getContentPane();
		final Panel panel = new Panel();
		panel.setBackground(new Color(255, 182, 147));
		contentPane.setBackground(new Color(255, 182, 147));
		contentPane.add(panel);
		setSize(560, 600);
		setTitle("FiveChess v1.0");
		this.setIconImage((new ImageIcon(FiveChess.class.getResource("black.png")).getImage()));
		setResizable(false);
		setLocationRelativeTo(null);
		panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("选项");
		JMenuItem menuStart = new JMenuItem("开始游戏");
		menuStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.ResetGame();
				panel.repaint();
			}
		});
		JMenuItem menuExit = new JMenuItem("退出");
		menuExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		JMenu level=new JMenu("难度");
		JMenuItem easyMenu = new JMenuItem("简单");
		easyMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wight=50;
				panel.ResetGame();
				panel.repaint();
			}
		});
		JMenuItem midMenu = new JMenuItem("一般");
		midMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wight=110;
				panel.ResetGame();
				panel.repaint();
			}
		});
		JMenuItem hightMenu = new JMenuItem("困难");
		hightMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wight=160;
				panel.ResetGame();
				panel.repaint();
			}
		});
		menuBar.add(menu);
		menuBar.add(level);
		menu.add(menuStart);
		menu.add(menuExit);
		level.add(easyMenu);
		level.add(midMenu);
		level.add(hightMenu);
		this.setJMenuBar(menuBar);
	}
}

class Panel extends JPanel {
	private ImageIcon black = new ImageIcon(FiveChess.class.getResource("black.png"));
	private ImageIcon white = new ImageIcon(FiveChess.class.getResource("white.png"));
	private ImageIcon current = new ImageIcon(FiveChess.class.getResource("current.png"));
	private int i, j, k, m, n, icount;//常用计数变量，m n是棋盘上的行列号，icount是获胜组合的序号
	private int[][] board = new int[16][16];// 棋盘状态，2 空白，1 电脑，0 用户落子位置
	private boolean[][][] ptable = new boolean[16][16][672];//棋盘上每一颗棋子是否在用户的获胜组合中
	private boolean[][][] ctable = new boolean[16][16][672];//棋盘上每一颗棋子是否在电脑的获胜组合中
	private int[][] cgrades = new int[16][16];// 电脑在当前所有下子位置的权值
	private int[][] pgrades = new int[16][16];// 用户在当前所有下子位置的权值
	private int cgrade, pgrade;// 电脑目前下子的最高权值//用户目前下子的最高权值
	private int[][] win = new int[2][672];//电脑和用户在各个获胜组合中所填入的棋子数
	private int oldx, oldy;//用户落子位置
	private int pcount, ccount;// 电脑落子数//用户落子数
	private boolean player, computer, over, pwin, cwin, tie, start;// 用户落子//电脑落子//结束//用户胜利//电脑胜利//平局//开始
	private int mat, nat, mde, nde;//白黑子落子的最佳位置//电脑攻击位置电脑防守位置
	/**
	 * 672种是
	 * 横向(16-4)*16
	 * 纵向16*(16-4)
	 * 右对角线(1+2+3+4+5+6+7+8+9+10+11)*2+12
	 * 左对角线(1+2+3+4+5+6+7+8+9+10+11)*2+12
	 * 合计672种组合
	 */
	public Panel() {
		addMouseListener(new Xiazi());
		this.ResetGame();
	}

	public void paintComponent(Graphics g) {//绘制背景，棋盘
		super.paintComponent(g);
		for (int i = 0; i < 16; i++)
			for (int j = 0; j < 16; j++) {
				g.drawLine(45, 45 + j * 30, 495, 45 + j * 30);
			}
		for (int i = 0; i < 16; i++)
			for (int j = 0; j < 16; j++) {
				g.drawLine(45 + j * 30, 45, 45 + j * 30, 495);
			}
		for (int i = 0; i < 16; i++) {
			String number = Integer.toString(i);
			g.drawString(number, 40 + 30 * i, 42);
		}
		for (int i = 1; i < 16; i++) {
			String number = Integer.toString(i);
			g.drawString(number, 29, 50 + 30 * i);
		}
		updatePaint(g);
	}

	class Xiazi extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (!over) {
				oldx = e.getX();
				oldy = e.getY();
				mouseClick();
				repaint();
			}
		}
	}

	// 游戏初始化
	public void ResetGame() {
		// 初始化棋盘
		for (i = 0; i < 16; i++)
			for (j = 0; j < 16; j++) {
				this.pgrades[i][j] = 0;
				this.cgrades[i][j] = 0;
				this.board[i][j] = 2;
			}
		// 遍历所有的五连子可能情况的权值
		// 横
		for (i = 0; i < 16; i++)
			for (j = 0; j < 12; j++) {
				for (k = 0; k < 5; k++) {
					this.ptable[j + k][i][icount] = true;
					this.ctable[j + k][i][icount] = true;
				}
				icount++;
			}
		// 竖
		for (i = 0; i < 16; i++)
			for (j = 0; j < 12; j++) {
				for (k = 0; k < 5; k++) {
					this.ptable[i][j + k][icount] = true;
					this.ctable[i][j + k][icount] = true;
				}
				icount++;
			}
		// 右斜
		for (i = 0; i < 12; i++)
			for (j = 0; j < 12; j++) {
				for (k = 0; k < 5; k++) {
					this.ptable[j + k][i + k][icount] = true;
					this.ctable[j + k][i + k][icount] = true;
				}
				icount++;
			}
		// 左斜
		for (i = 0; i < 12; i++)
			for (j = 15; j >= 4; j--) {
				for (k = 0; k < 5; k++) {
					this.ptable[j - k][i + k][icount] = true;
					this.ctable[j - k][i + k][icount] = true;
				}
				icount++;
			}
		for (i = 0; i <= 1; i++)
			// 初始化黑子白子上的每个权值上的连子数
			for (j = 0; j < 672; j++)
				this.win[i][j] = 0;
		this.player = true;
		this.icount = 0;
		this.ccount = 0;
		this.pcount = 0;
		this.start = true;
		this.over = false;
		this.pwin = false;
		this.cwin = false;
		this.tie = false;
//		this.bout = 1;
	}

	public void ComTurn() { // 找出电脑（白子）最佳落子点
		for (i = 0; i <= 15; i++)
			// 遍历棋盘上的所有坐标
			for (j = 0; j <= 15; j++) {
				this.pgrades[i][j] = 0; // 该坐标的黑子奖励积分清零
				if (this.board[i][j] == 2) // 在还没下棋子的地方遍历
					for (k = 0; k < 672; k++)
						// 遍历该棋盘可落子点上的黑子所有权值的连子情况，并给该落子点加上相应奖励分
						if (this.ptable[i][j][k]) {
							switch (this.win[0][k]) {
							case 1: // 一连子
								this.pgrades[i][j] += 5;//5
								break;
							case 2: // 两连子
								this.pgrades[i][j] += GameF.wight;//50
								break;
							case 3: // 三连子
								this.pgrades[i][j] += 180;//180
								break;
							case 4: // 四连子
								this.pgrades[i][j] += 400;//400
								break;
							}
						}
				this.cgrades[i][j] = 0;// 该坐标的白子的奖励积分清零
				if (this.board[i][j] == 2) // 在还没下棋子的地方遍历
					for (k = 0; k < 672; k++)
						// 遍历该棋盘可落子点上的白子所有权值的连子情况，并给该落子点加上相应奖励分
						if (this.ctable[i][j][k]) {
							switch (this.win[1][k]) {
							case 1: // 一连子
								this.cgrades[i][j] += 5;//5
								break;
							case 2: // 两连子
								this.cgrades[i][j] += 52;//52
								break;
							case 3: // 三连子
								this.cgrades[i][j] += 100;//100
								break;
							case 4: // 四连子
								this.cgrades[i][j] += 400;//400
								break;
							}
						}
			}
		if (this.start) { // 开始时白子落子坐标
			if (this.board[4][4] == 2) {
				m = 4;
				n = 4;
			} else {
				m = 5;
				n = 5;
			}
			this.start = false;
		} else {
			for (i = 0; i < 16; i++)
				for (j = 0; j < 16; j++)
					if (this.board[i][j] == 2) { // 找出棋盘上可落子点的黑子白子的各自最大权值，找出各自的最佳落子点
						if (this.cgrades[i][j] >= this.cgrade) {
							this.cgrade = this.cgrades[i][j];
							this.mat = i;
							this.nat = j;
						}
						if (this.pgrades[i][j] >= this.pgrade) {
							this.pgrade = this.pgrades[i][j];
							this.mde = i;
							this.nde = j;
						}
					}
			//判断进攻还是防守
			if (this.cgrade >= this.pgrade) { // 如果白子的最佳落子点的权值比黑子的最佳落子点权值大，则电脑的最佳落子点为白子的最佳落子点，否则相反
				m = mat;
				n = nat;
			} else {
				m = mde;
				n = nde;
			}
		}
		this.cgrade = 0;
		this.pgrade = 0;
		this.board[m][n] = 1; // 电脑下子位置
		ccount++;
		if ((ccount == 50) && (pcount == 50)) // 平局判断
		{
			this.tie = true;
			this.over = true;
		}
		for (i = 0; i < 672; i++) {
			if (this.ctable[m][n][i] && this.win[1][i] != 7)
				this.win[1][i]++; // 给白子的所有五连子可能的加载当前连子数
			if (this.ptable[m][n][i]) {
				this.ptable[m][n][i] = false;
				this.win[0][i] = 7;
			}
		}
		this.player = true; // 该人落子
		this.computer = false; // 电脑落子结束
	}

	public void mouseClick() {
		if (!this.over)
			if (this.player) {
				if (this.oldx < 520 && this.oldy < 520) {
					int m1 = m, n1 = n;
					m = (oldx - 33) / 30;
					n = (oldy - 33) / 30;
					if (this.board[m][n] == 2) {
						this.board[m][n] = 0;
						pcount++;
						if ((ccount == 50) && (pcount == 50)) {
							this.tie = true;
							this.over = true;
						}
						for (i = 0; i < 672; i++) {
							if (this.ptable[m][n][i] && this.win[0][i] != 7)
								this.win[0][i]++; // 给黑子的所有五连子可能的加载当前连子数
							if (this.ctable[m][n][i]) {
								this.ctable[m][n][i] = false;
								this.win[1][i] = 7;
							}
						}
						this.player = false;
						this.computer = true;
					} else {
						m = m1;
						n = n1;
					}
				}
			}
	}

	public void updatePaint(Graphics g) {
		if (!this.over) { // 如果是轮到电脑下
			if (this.computer)
				this.ComTurn(); // 得到最佳下子点
			// 遍历当前棋盘上的五连子情况，判断输赢
			for (i = 0; i <= 1; i++)
				for (j = 0; j < 672; j++) {
					if (this.win[i][j] == 5)
						if (i == 0) { // 人赢
							this.pwin = true;
							this.over = true; // 游戏结束
							break;
						} else {
							this.cwin = true; // 电脑赢
							this.over = true;
							break;
						}
					if (this.over) // 一遇到五连子退出棋盘遍历
						break;
				}
			g.setFont(new Font("微软雅黑", 0, 20));
			g.setColor(Color.BLUE);
			// 画出当前棋盘所有棋子
			for (i = 0; i <= 15; i++)
				for (j = 0; j <= 15; j++) { // 如果board元素值为0，则该坐标处为黑子
					if (this.board[i][j] == 0) {
						g.drawImage(
								black.getImage(),
								i * 30 + 31,
								j * 30 + 31,
								black.getImage().getWidth(
										black.getImageObserver()) - 3,
								black.getImage().getHeight(
										black.getImageObserver()) - 3, black
										.getImageObserver());
					}
					// 如果board元素值为1，则该坐标处为白子
					if (this.board[i][j] == 1) {
						g.drawImage(
								white.getImage(),
								i * 30 + 31,
								j * 30 + 31,
								white.getImage().getWidth(
										white.getImageObserver()) - 3,
								white.getImage().getHeight(
										white.getImageObserver()) - 3, white
										.getImageObserver());
					}
				}
			// 画出白子（电脑）当前所下子，便于辨认
			if (this.board[m][n] != 2)
				g.drawImage(
						current.getImage(),
						m * 30 + 31,
						n * 30 + 31,
						current.getImage().getWidth(current.getImageObserver()) - 4,
						current.getImage()
								.getHeight(current.getImageObserver()) - 4,
						current.getImageObserver());
			// 判断输赢情况
			// 人赢
			if (this.pwin)
				g.drawString("您太厉害了！再来一次请重新开始游戏..", 80, 540);
			// 电脑赢
			if (this.cwin)
				g.drawString("很遗憾，你输了!再来一次请重新开始游戏..", 80, 540);
			// 平局
			if (this.tie)
				g.drawString("不分胜负!再来一次请重新开始游戏..", 80, 540);
			g.dispose();
		}
	}
}
