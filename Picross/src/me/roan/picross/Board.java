package me.roan.picross;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JPanel;

public class Board extends JPanel implements KeyListener, MouseListener{
	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 6310638885364285013L;
	private static final double RANDOMISATIONS = 0.8;
	private static final Font NUMBERS = new Font("Dialog", Font.BOLD, 15);
	public static final int SIZE = 50;
	private final Random random;
	private final boolean[][] solution;
	private final int[][] rowHints;
	private final int[][] colHints;
	private final Tile[][] state;
	private final int width;
	private final int height;
	private int dx = 200;
	private int dy = 200;
	//private BufferedImage background = null;
	private boolean clear = false;
	private boolean reveal = false;
	private boolean trialMode = false;

//	public static final Board fromImage(File img){
//		try{
//			BufferedImage bg = ImageIO.read(img);
//			int w = (int)Math.ceil((double)bg.getWidth() / Tile.SIZE);
//			int h = (int)Math.ceil((double)bg.getHeight() / Tile.SIZE);
//			return new Board(w, h, (int)(w * h * MINE_PERCENTAGE), bg);
//		}catch(IOException e){
//			return new Board(20, 15, 40);
//		}
//	}
	
	public Board(int width, int height){
		this(width, height, ThreadLocalRandom.current().nextLong());
	}
	
	public Board(int width, int height, BufferedImage bg){
		this(width, height, ThreadLocalRandom.current().nextLong(), bg);
	}
	
	public Board(int width, int height, long seed){
		this(width, height, seed, null);
	}
	
	public Board(int width, int height, long seed, BufferedImage bg){
		this.setFocusable(true);
		this.addKeyListener(this);
		this.addMouseListener(this);
		
		//this.background = bg;
		random = new Random(seed);
		this.width = width;
		this.height = height;
		
		solution = new boolean[width][height];
		state = new Tile[width][height];
		for(int i = 0; i < width; i++){
			Arrays.fill(state[i], Tile.EMPTY);
		}
		rowHints = new int[height][];
		colHints = new int[width][];
		
		initialiseGrid();
	}
		
	public void setClear(boolean flag){
		clear = flag;
	}
	
//	public boolean hasBackground(){
//		return background != null;
//	}
	
	public void setClicked(int px, int py, Tile newState){
		int x = (px - dx) / SIZE;
		int y = (py - dy) / SIZE;
		if(x >= 0 && y >= 0 && x < width && y < height){
			Tile old = state[x][y];
			if(trialMode){
				if(state[x][y] == Tile.EMPTY || state[x][y].isTrial()){
					newState = newState.toTest();
					state[x][y] = (old == newState) ? Tile.EMPTY : newState;
				}
			}else{
				state[x][y] = (old == newState) ? Tile.EMPTY : newState;
			}
		}
	}
	
	public int getTileCount(){
		return width * height;
	}
	
	private final void initialiseGrid(){
		for(int n = 0; n < RANDOMISATIONS * (width * height); n++){
			solution[random.nextInt(width)][random.nextInt(height)] = true;
		}
		
		int[] buffer = new int[Math.max(width, height)];
		
		//row numbers
		int nums = 0;
		for(int y = 0; y < height; y++){
			int n = 0;
			for(int x = 0; x <= width; x++){
				if(x != width && solution[x][y]){
					n++;
				}else if(n != 0){
					buffer[nums] = n;
					nums++;
					n = 0;
				}
			}
			rowHints[y] = Arrays.copyOf(buffer, nums);
			Arrays.fill(buffer, 0);
			nums = 0;
		}
				
		//column numbers
		for(int x = 0; x < width; x++){
			int n = 0;
			for(int y = 0; y <= height; y++){
				if(y != height && solution[x][y]){
					n++;
				}else if(n != 0){
					buffer[nums] = n;
					nums++;
					n = 0;
				}
			}
			colHints[x] = Arrays.copyOf(buffer, nums);
			Arrays.fill(buffer, 0);
			nums = 0;
		}
	}
	
	private boolean[] computeFoundRowNums(int y){
		boolean[] found = new boolean[rowHints[y].length];
		int x = 0;
		int f = 0;
		int h = 0;
		int dir = 1;
		boolean bad = false;
		while(x >= 0 && x < width){
			boolean black = (state[x][y] == Tile.BLACK) || (state[x][y] == Tile.TRY_BLACK);
			if(black){
				f++;
			}
			if(state[x][y] == Tile.WHITE || state[x][y] == Tile.TRY_WHITE || (x == 0 && black && dir == -1) || (x == width - 1 && black && dir == 1)){
				if(h < 0 || h >= found.length){
					bad = true;
					break;
				}
				if(f == rowHints[y][h]){
					found[h] = true;
					h += dir;
					f = 0;
				}else{
					bad = true;
					break;
				}
			}
			if(state[x][y] == Tile.EMPTY){
				if(dir == 1){
					dir = -1;
					x = width;
					h = found.length - 1;
					f = 0;
				}else{
					break;
				}
			}
			x += dir;
		}
		return bad ? null : found;
	}
	
	@Override
	public Dimension getPreferredSize(){
		return new Dimension(width * SIZE + dx, height * SIZE + dy);
	}
	
	@Override
	public void paintComponent(Graphics g1){
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
//		if(background != null){
//			g.drawImage(background, (Math.min(this.getWidth(), width * Tile.SIZE) - background.getWidth()) / 2, (Math.min(this.getHeight(), height * Tile.SIZE) - background.getHeight()) / 2, this);
//			if(clear){
//				return;
//			}
//		}
		
		
		//origin at the top left corner of the grid
		g.translate(dx, dy);
		
		//grid
		g.setColor(Color.GRAY);
		for(int x = 0; x < width; x++){
			g.fillRect(x * SIZE - 1, 0, 2, height * SIZE);
		}
		for(int y = 0; y < height; y++){
			g.fillRect(0, y * SIZE - 1, width * SIZE, 2);
		}
		
		g.setColor(Color.BLACK);
		g.drawRect(-1, -1, 1, 1);
		for(int x = 0; x <= width; x += 5){
			g.fillRect(x * SIZE - 1, 0, 2, height * SIZE + 1);
		}
		for(int y = 0; y <= height; y += 5){
			g.fillRect(0, y * SIZE - 1, width * SIZE + 1, 2);
		}
		
		//cell status
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				switch(state[x][y]){
				case BLACK:
					g.setColor(Color.BLACK);
					g.fillRect(x * SIZE + 5, y * SIZE + 5, SIZE - 10, SIZE - 10);
					break;
				case WHITE:
					g.setColor(Color.BLACK);
					g.drawLine(x * SIZE + 5, y * SIZE + 5, x * SIZE + SIZE - 5, y * SIZE + SIZE - 5);
					g.drawLine(x * SIZE + SIZE - 5, y * SIZE + 5, x * SIZE + 5, y * SIZE + SIZE - 5);
					break;
				case EMPTY:
					break;
				case TRY_BLACK:
					g.setColor(Color.BLUE);
					g.fillRect(x * SIZE + 5, y * SIZE + 5, SIZE - 10, SIZE - 10);
					break;
				case TRY_WHITE:
					g.setColor(Color.BLUE);
					g.drawLine(x * SIZE + 5, y * SIZE + 5, x * SIZE + SIZE - 5, y * SIZE + SIZE - 5);
					g.drawLine(x * SIZE + SIZE - 5, y * SIZE + 5, x * SIZE + 5, y * SIZE + SIZE - 5);
					break;
				}

				if(reveal && solution[x][y]){
					g.setColor(Color.RED);
					g.fillRect(x * SIZE + 15, y * SIZE + 15, 20, 20);
				}
			}
		}
		
		g.setFont(NUMBERS);
		g.setColor(Color.BLACK);
		
		//row numbers
		System.out.println("----- start -----");
		for(int y = 0; y < height; y++){
			boolean[] found = computeFoundRowNums(y);
			int offset = -15;
			for(int i = rowHints[y].length - 1; i >= 0; i--){
				g.setColor(found == null ? Color.RED : (found[i] ? Color.GRAY : Color.BLACK));
				g.drawString(String.valueOf(rowHints[y][i]), offset, y * SIZE + (SIZE + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2);
				offset -= 20;
			}
		}
		
		//column numbers
		for(int x = 0; x < width; x++){
			int offset = -5;
			for(int i = colHints[x].length - 1; i >= 0; i--){
				String str = String.valueOf(colHints[x][i]);
				g.drawString(str, x * SIZE + (SIZE - g.getFontMetrics().stringWidth(str)) / 2, offset);
				offset -= 20;
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e){		
	}

	@Override
	public void mousePressed(MouseEvent e){
		switch(e.getButton()){
		case MouseEvent.BUTTON1:
			setClicked(e.getX(), e.getY(), Tile.BLACK);
			break;
		case MouseEvent.BUTTON3:
			setClicked(e.getX(), e.getY(), Tile.WHITE);
			break;
		}
		this.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e){		
	}

	@Override
	public void mouseEntered(MouseEvent e){		
	}

	@Override
	public void mouseExited(MouseEvent e){		
	}

	@Override
	public void keyTyped(KeyEvent e){		
	}

	@Override
	public void keyPressed(KeyEvent e){
		switch(e.getKeyCode()){
//		case KeyEvent.VK_UP:
//			setActiveTile(activeTile.up);
//			break;
//		case KeyEvent.VK_DOWN:
//			setActiveTile(activeTile.down);
//			break;
//		case KeyEvent.VK_RIGHT:
//			setActiveTile(activeTile.right);
//			break;
//		case KeyEvent.VK_LEFT:
//			setActiveTile(activeTile.left);
//			break;
//		case KeyEvent.VK_W:
//		case KeyEvent.VK_SPACE:
//			revealTile(activeTile);
//			break;
//		case KeyEvent.VK_E:
//		case KeyEvent.VK_SHIFT:
//			activeTile.toggleFlag();
//			listener.update(getFlagsPlaced(), getRevealedTiles());
//			this.repaint();
//			break;
//		case KeyEvent.VK_F:
//			if(activeTile.hasMine && !activeTile.hasFlag()){
//				activeTile.toggleFlag();
//				this.repaint();
//			}else{
//				revealTile(activeTile);
//			}
//			break;
		case KeyEvent.VK_S:
			clear = !clear;
			this.repaint();
			break;
		case KeyEvent.VK_T:
			trialMode = true;
			this.repaint();
			break;
		case KeyEvent.VK_R:
			reveal = !reveal;
			this.repaint();
			break;
		case KeyEvent.VK_V:
			for(int x = 0; x < width; x++){
				for(int y = 0; y < height; y++){
					if(state[x][y].isTrial()){
						state[x][y] = Tile.EMPTY;
					}
				}
			}
			trialMode = false;
			this.repaint();
			break;
		case KeyEvent.VK_C:
			for(int x = 0; x < width; x++){
				for(int y = 0; y < height; y++){
					if(state[x][y].isTrial()){
						state[x][y] = state[x][y].toReal();
					}
				}
			}
			trialMode = false;
			this.repaint();
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e){		
	}
}
