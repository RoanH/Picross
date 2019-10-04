package me.roan.picross;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JPanel;

public class Board extends JPanel implements KeyListener, MouseListener{
	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 6310638885364285013L;
	private static final double RANDOMISATIONS = 0.5;
	public static final int SIZE = 50;
	private final Random random;
	private final boolean[][] solution;
	private final Tile[][] state;
	private final int width;
	private final int height;
	//private BufferedImage background = null;
	private boolean clear = false;

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
		
		state = new Tile[width][height];
		solution = new boolean[width][height];
		
		initialiseGrid();
	}
	
	public void setClear(boolean flag){
		clear = flag;
	}
	
//	public boolean hasBackground(){
//		return background != null;
//	}
	
	public int getTileCount(){
		return width * height;
	}
	
	private final void initialiseGrid(){
		for(int n = 0; n < RANDOMISATIONS * width * height; n++){
			solution[random.nextInt(width)][random.nextInt(height)] = true;
		}
	}
	
	@Override
	public Dimension getPreferredSize(){
		return new Dimension(width * SIZE, height * SIZE);
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
		
		g.setColor(Color.DARK_GRAY);
		for(int x = 0; x < width; x++){
			g.fillRect(x * SIZE, 0, 2, this.getHeight());
		}
		for(int y = 0; y < height; y++){
			g.fillRect(0, y * SIZE, this.getWidth(), 2);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e){		
	}

	@Override
	public void mousePressed(MouseEvent e){
//		switch(e.getButton()){
//		case MouseEvent.BUTTON1:
//			getTileAt(e.getX() / SIZE, e.getY() / SIZE).ifPresent(tile->{
//				setActiveTile(tile);
//				revealTile(tile);
//			});
//			break;
//		case MouseEvent.BUTTON3:
//			getTileAt(e.getX() / SIZE, e.getY() / SIZE).ifPresent(tile->{
//				tile.toggleFlag();
//				listener.update(getFlagsPlaced(), getRevealedTiles());
//			});
//			this.repaint();
//			break;
//		}
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
		}
	}

	@Override
	public void keyReleased(KeyEvent e){		
	}
}
