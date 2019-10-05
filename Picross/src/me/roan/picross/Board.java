package me.roan.picross;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class Board extends JPanel implements KeyListener, MouseListener{
	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 6310638885364285013L;
	private static final Font NUMBERS = new Font("Dialog", Font.BOLD, 15);
	private final long startTime = System.currentTimeMillis();
	private Seed seed;
	public static final int SIZE = 50;
	private final Random random;
	private final boolean[][] solution;
	private final int[][] rowHints;
	private final int[][] colHints;
	private final Tile[][] state;
	private final int width;
	private final int height;
	private int dx = 250;
	private int dy = 200;
	private boolean clear = false;
	private boolean reveal = false;
	private boolean trialMode = false;
	private Runnable listener;
	
	public Board(Seed seed, JComponent parent){
		this.setFocusable(true);
		parent.addKeyListener(this);
		this.addMouseListener(this);
		
		this.seed = seed;
		random = new Random(seed.seed);
		this.width = seed.width;
		this.height = seed.height;
		
		solution = new boolean[width][height];
		state = new Tile[width][height];
		for(int i = 0; i < width; i++){
			Arrays.fill(state[i], Tile.EMPTY);
		}
		rowHints = new int[height][];
		colHints = new int[width][];
		
		initialiseGrid();
	}
	
	public long getPassedTime(){
		return System.currentTimeMillis() - startTime;
	}
	
	public void addListener(Runnable listener){
		this.listener = listener;
	}
	
	public boolean isTestMode(){
		return trialMode;
	}
	
	public int getTileCount(Tile type){
		int n = 0;
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				if(state[x][y] == type){
					n++;
				}
			}
		}
		return n;
	}
		
	public void setClear(boolean flag){
		clear = flag;
	}
	
	public Seed getSeed(){
		return seed;
	}
	
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
		if(listener != null){
			listener.run();
		}
	}
	
	public int getTileCount(){
		return width * height;
	}
	
	private final void initialiseGrid(){
		for(int n = 0; n < seed.density * (width * height); n++){
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
	
	private Boolean[] computeFoundRowNums(final int y){
		return computeJudgement(rowHints[y], width, x->state[x][y]);
	}
	
	private Boolean[] computeFoundColNums(final int x){
		return computeJudgement(colHints[x], height, y->state[x][y]);
	}
	
	private Boolean[] computeJudgement(int[] hints, int max, Function<Integer, Tile> state){
		//All are false initially
		Boolean[] found = new Boolean[hints.length];
		Arrays.fill(found, Boolean.FALSE);
		
		//x = current position, f = chain length found so far, h = current hint to look for
		int x = 0;
		int f = 0;
		int h = 0;
		
		//increment starts as 1 flips to -1 if the line is incomplete
		int dir = 1;
		while(x >= 0 && x < max){			
			//found an empty tile, start looking from the right
			if(state.apply(x) == Tile.EMPTY){
				if(dir == 1){
					dir = -1;
					x = max - 1;
					h = found.length - 1;
					f = 0;
					continue;
				}else{
					//break if we were already looking from the right
					break;
				}
			}
			
			//if the current tile is filled
			boolean black = (state.apply(x) == Tile.BLACK) || (state.apply(x) == Tile.TRY_BLACK);
			//if we're at the end of a search
			boolean end = (x == 0 && dir == -1) || (x == max - 1 && dir == 1);
			
			//increment chain length
			if(black){
				f++;
			}
			
			//if we hit the end or are on a white tile
			if(state.apply(x) == Tile.WHITE || state.apply(x) == Tile.TRY_WHITE || end){
				if(f != 0){
					//too many chains found
					if((h < 0 || h >= found.length)){
						Arrays.fill(found, null);
					}else{
						//check if the found chain length matches the one we're looking for
						if(f == hints[h]){
							found[h] = Boolean.TRUE;
						}else{
							//invalidate the chain that was found
							found[h] = null;
						}
						h += dir;
						f = 0;
					}
				}else{
					//hit the end without finding any chains
					if((x == max - 1 && dir == 1) && h == 0){
						Arrays.fill(found, null);
					}
				}
				//hit the end without finding enough chains
				if((x == max - 1 && dir == 1) && h != found.length){
					Arrays.fill(found, null);
				}
			}
			
			x += dir;
		}
		return found;
	}
	
	@Override
	public Dimension getPreferredSize(){
		return new Dimension(width * SIZE + dx * 2, height * SIZE + dy + 1);
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
		
		if(trialMode){
			g.setColor(Color.BLUE);
			g.drawString("Test mode", 0, 15);
		}
		
		FontMetrics fm = g.getFontMetrics();
		int black = getTileCount(Tile.BLACK);
		int tryBlack = getTileCount(Tile.TRY_BLACK);
		int white = getTileCount(Tile.WHITE);
		int tryWhite = getTileCount(Tile.TRY_WHITE);
		
		g.setColor(Color.BLACK);
		String line = "Filled: " + black;
		g.drawString(line, 0, 30);
		if(trialMode){
			g.setColor(Color.BLUE);
			g.drawString(" (+" + tryBlack + ")", fm.stringWidth(line), 30);
		}
		
		g.setColor(Color.BLACK);
		line = "Crossed: " + white;
		g.drawString(line, 0, 45);
		if(trialMode){
			g.setColor(Color.BLUE);
			g.drawString(" (+" + tryWhite + ")", fm.stringWidth(line), 45);
		}
		
		g.setColor(Color.BLACK);
		line = String.format("Done: %1$.2f", (100.0D * (black + white)) / getTileCount());
		g.drawString(line, 0, 60);
		if(trialMode){
			g.setColor(Color.BLUE);
			g.drawString(String.format(" (+%1$.2f)", (100.0D * (tryBlack + tryWhite)) / getTileCount()), fm.stringWidth(line), 60);
		}
		
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
		for(int y = 0; y < height; y++){
			Boolean[] found = computeFoundRowNums(y);
			int offset = -10;
			for(int i = rowHints[y].length - 1; i >= 0; i--){
				g.setColor(found[i] == null ? Color.RED : (found[i] ? Color.GRAY : Color.BLACK));
				String str = String.valueOf(rowHints[y][i]);
				g.drawString(str, offset - (g.getFontMetrics().stringWidth(str) / 2), y * SIZE + (SIZE + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2);
				offset -= 20;
			}
		}
		
		//column numbers
		for(int x = 0; x < width; x++){
			Boolean[] found = computeFoundColNums(x);
			int offset = -5;
			for(int i = colHints[x].length - 1; i >= 0; i--){
				g.setColor(found[i] == null ? Color.RED : (found[i] ? Color.GRAY : Color.BLACK));
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
