package Main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import Objects.*;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int SIZE_X = 750;
	private static final int SIZE_Y = 600;
	private final int TOADO_X = 40;
	private final int TOADO_Y = 40;
	private final int DELAY = 10;
	public static Hero hero;
	private Boss boss;
	private boolean boss_died = false; // boss chết -> game over
	private boolean boss_appeared = false;
	private Timer timer;
	public static Map m;
	Color bgcolor = new Color(207, 207, 207);
	private boolean ingame;
	private List<Monster> monsters;
	private final int[][] position = { { 250, 250 }, { 100, 100 }, { 180, 300 }, { 210, 520 }, { 520, 520 },
			{ 440, 400 }, { 520, 50 }, { 260, 260 }, { 200, 200 }, { 450, 150 }, { 450, 31 },

	};
	Rectangle startButton = new Rectangle(325, 300, 120, 50);
	Rectangle quitButton = new Rectangle(325, 400, 120, 50);
	int mainMenu = 0;

	public Board() {
		initBoard();
	}

	private void initBoard() {
		addKeyListener(new TAdapter());
		addMouseListener(new MouseHandler());
		addMouseMotionListener(new MouseHandler());
		setFocusable(true);
		setBackground(bgcolor);
		m = new Map("map1.txt");
		setDoubleBuffered(true);
		setPreferredSize(new Dimension(SIZE_X, SIZE_Y));
		ingame = true;
		ImageIcon img = new ImageIcon("src/images/d0.png");
		hero = new Hero(TOADO_X, TOADO_Y, 30, 30, img.getImage());
		ImageIcon img1 = new ImageIcon("src/images/monster-face.png");
		boss = new Boss(300, 300, 60, 60, img1.getImage());
		initMonsters();
		timer = new Timer(DELAY, this);
		timer.start();
	}

	public void initMonsters() {
		monsters = new ArrayList<>();
		ImageIcon img = new ImageIcon("src/images/monster_d.png");
		for (int[] p : position) {
			monsters.add(new Monster(p[0], p[1], 30, 30, img.getImage()));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!ingame) {
			timer.stop();
		}
		updateFires();
		updateHero();
		updateMonster();
		updateBoss();
		if (boss_appeared)
			updateBossFire();
		checkCollisions();
		repaint();
	}

	private void updateFires() {
		List<Fire> fires = hero.getFires();
		for (int i = 0; i < fires.size(); i++) {
			Fire fire = fires.get(i);
			if (fire.getTontai()) {
				fire.move();
			} else {
				fires.remove(i);
			}
		}
	}

	private void updateBossFire() {
		List<Fire> stones = boss.getFires();
		for (int i = 0; i < stones.size(); i++) {
			Fire stone = stones.get(i);
			if (stone.getTontai()) {
				stone.move();
			} else
				stones.remove(i);
		}
	}

	private void updateHero() {
		if (hero.getTontai()) {
			hero.move();
		} else
			ingame = false;
	}

	private void updateBoss() {
		if (boss_appeared) {
			boss.move(hero.getX(), hero.getY());
		}
	}

	private void updateMonster() {
		// if (monsters.isEmpty()) {
		// ingame = false;
		// return;
		// }
		if (monsters.isEmpty()) {
			if (hero.getLevel() == 1) {
				this.initMonsters();
				hero.setX(40);
				hero.setY(40);
				hero.levelUp();
			}
			if (monsters.isEmpty() && (hero.getLevel() == 2)) {
				hero.levelUp();
				hero.setX(40);
				hero.setY(40);
			}
			if (boss.getHp() == 0) { // boss chết -> thắng
				boss_died = true;
				ingame = false;
			}
			return;
		}
		for (int i = 0; i < monsters.size(); i++) {
			Monster m = monsters.get(i);
			if (m.getTontai()) {
				m.move();
			} else
				monsters.remove(i);
		}
	}

	private void checkCollisions() {
		Rectangle hr = hero.getBounds();
		for (Monster monster : monsters) {
			Rectangle ms = monster.getBounds();
			if (hr.intersects(ms)) {
				hero.setTontai(false);
				monster.setTontai(false);
				ingame = false;
			}
		}
		List<Fire> frs = hero.getFires();
		for (Fire fr : frs) {
			Rectangle khung_fr = fr.getBounds();
			for (Monster monster : monsters) {
				Rectangle ms = monster.getBounds();
				if (ms.intersects(khung_fr)) {
					fr.setTontai(false);
					monster.setTontai(false);
					hero.upExp();
				}
			}
		}
		if (boss_appeared) {
			Rectangle bossBound = boss.getBounds();
			for (Fire fr : frs) {
				Rectangle khung_fr = fr.getBounds();
				if (khung_fr.intersects(bossBound)) { // xử lý đạn bắn vào boss
					fr.setTontai(false);
					boss.setHp(boss.getHp() - hero.getShoot_force());
					boss_appeared = boss.getTontai();
					ingame = boss.getTontai();
				}
			}
			List<Fire> stones = boss.getFires(); // xử lý đạn boss vào hero
			for (Fire stone : stones) {
				Rectangle st_rec = stone.getBounds();
				if (hr.intersects(st_rec)) {
					hero.setTontai(false);
				}
			}
			if (hr.intersects(bossBound)) {
				hero.setTontai(false);
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		if (mainMenu == 1) {
			super.paintComponent(g);
			ImageIcon treeImage = new ImageIcon("src/images/tree.png");
			ImageIcon wallImage = new ImageIcon("src/images/wall.png");
			ImageIcon groundImage = new ImageIcon("src/images/ground.png");
			if (hero.getLevel() == 2) {
				m = new Map("map2.txt");
			}
			if (hero.getLevel() == 3) {
				m = new Map("map3.txt");
			}
			if (boss_died) {
				g.setColor(Color.black);
				String msg = "YOU WIN";
				Font small = new Font("Helvetica", Font.BOLD, 30);
				FontMetrics fm = getFontMetrics(small);
				g.setColor(Color.black);
				g.setFont(small);
				g.drawString(msg, (SIZE_X - fm.stringWidth(msg)) / 2, SIZE_Y / 2);
			} else if (ingame) {
				for (int y = 0; y < 20; y++)
					for (int x = 0; x < 20; x++) {
						if (m.getMap(x, y).equals("1")) {
							g.drawImage(new Tree(x, y, 30, 30, treeImage.getImage()).getImage(), x * 30, y * 30, null);
						} else if (m.getMap(x, y).equals("2")) {
							g.drawImage(new Wall(x, y, 30, 30, wallImage.getImage()).getImage(), x * 30, y * 30, null);
						} else if (m.getMap(x, y).equals("0")) {
							g.drawImage(new Ground(x, y, 30, 30, groundImage.getImage()).getImage(), x * 30, y * 30,
									null);
						}
					}
				g.drawImage(hero.getImage(), hero.getX(), hero.getY(), this);
				List<Fire> fires = hero.getFires();
				for (Fire fire : fires) {
					g.drawImage(fire.getImage(), fire.getX(), fire.getY(), this);
				}
				List<Fire> stones = boss.getFires();
				for (Fire stone : stones) {
					g.drawImage(stone.getImage(), stone.getX(), stone.getY(), this);
				}
				if (monsters.isEmpty() && boss_appeared == false && hero.getLevel() == 3) {
					boss_appeared = true;
				}
				if (boss_appeared)
					g.drawImage(boss.getImage(), boss.getX(), boss.getY(), this);
				for (Monster monster : monsters) {
					if (monster.getTontai()) {
						g.drawImage(monster.getImage(), monster.getX(), monster.getY(), this);
					}
				}
				g.setColor(Color.black);
				if (monsters.isEmpty() && hero.getLevel() == 3) {
					g.drawString("Level : " + hero.getLevel(), SIZE_X - 120, SIZE_Y / 4);
					g.drawString("HERO", SIZE_X - 120, SIZE_Y / 4 + 20);
					g.drawString("Shoot force: " + hero.getShootForce(), SIZE_X - 120, SIZE_Y / 4 + 40);
					g.drawString("EXP : " + hero.getExp(), SIZE_X - 120, SIZE_Y / 4 + 60);

					g.drawString("BOSS ", SIZE_X - 120, SIZE_Y / 4 + 100);
					g.drawString("HP : " + boss.getHp(), SIZE_X - 120, SIZE_Y / 4 + 120);
				} else {
					g.drawString("Level : " + hero.getLevel(), SIZE_X - 120, SIZE_Y / 4);
					g.drawString("HERO", SIZE_X - 120, SIZE_Y / 4 + 20);
					g.drawString("Shoot force: " + hero.getShootForce(), SIZE_X - 120, SIZE_Y / 4 + 40);
					g.drawString("EXP : " + hero.getExp(), SIZE_X - 120, SIZE_Y / 4 + 60);
					g.drawString("MONSTER", SIZE_X - 120, SIZE_Y / 4 + 100);
					g.drawString("Monsters: " + monsters.size(), SIZE_X - 120, SIZE_Y / 4 + 120);

				}
			} else {
				String msg = "Game Over";
				Font small = new Font("Helvetica", Font.BOLD, 30);
				FontMetrics fm = getFontMetrics(small);
				g.setColor(Color.black);
				g.setFont(small);
				g.drawString(msg, (SIZE_X - fm.stringWidth(msg)) / 2, SIZE_Y / 2);
				g.drawString("EXP : " + hero.getExp(), (SIZE_X - 120) / 2, SIZE_Y / 2 + 40);
			}
			Toolkit.getDefaultToolkit().sync();
		} else {
			setBackground(bgcolor);
			g.setFont(new Font("Arial", Font.BOLD, 48));
			g.setColor(Color.RED);
			g.drawString("Run Now", 280, 200);
			g.setColor(Color.PINK);
			g.fillRect(startButton.x, startButton.y, startButton.width, startButton.height);
			g.setFont(new Font("Arial", Font.BOLD, 24));
			g.setColor(Color.BLACK);
			g.drawString("Start", startButton.x + 35, startButton.y + 35);
			g.setColor(Color.PINK);
			g.fillRect(quitButton.x, quitButton.y, quitButton.width, quitButton.height);
			g.setColor(Color.BLACK);
			g.drawString("Quit", quitButton.x + 35, quitButton.y + 35);
                        g.setColor(Color.BLUE);
                        g.drawString("Press Arrow keys to move", 100, 550 );
                        g.drawString("Press Space to fire", 450, 550);
		}
	}

	private class TAdapter extends KeyAdapter {
		@Override
		public void keyReleased(KeyEvent e) {
			hero.keyReleased(e);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			hero.keyPressed(e);
		}
	}

	public class MouseHandler extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			int mx = e.getX();
			int my = e.getY();
			if (mx > startButton.x && mx < startButton.x + startButton.width && my > startButton.y
					&& my < startButton.y + startButton.height)
				mainMenu = 1;
			if (mx > quitButton.x && mx < quitButton.x + quitButton.width && my > quitButton.y
					&& my < quitButton.y + quitButton.height)
				System.exit(0);
		}
	}

	public static int getSizeX() {
		return SIZE_X;
	}

	public static int getSizeY() {
		return SIZE_Y;
	}
}
