package me.roan.picross;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main{
	
	private static final String TITLE = "Picross";
	private static final JFrame frame = new JFrame(TITLE);
	private static JPanel gameContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private static Board board = null;

	public static void main(String[] args){
		showGameGUI();
	}
	
	private static final void showGameGUI(){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1){
		}
		UIManager.getDefaults().put("ScrollPane.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{}));

		
		JPanel content = new JPanel(new BorderLayout());
		frame.add(content);
		
		JMenuBar bar = new JMenuBar();
		
		JMenu help = new JMenu("Help");
		JMenuItem controls = new JMenuItem("Controls");
		controls.addActionListener(e->showControls());
		help.add(controls);
		
		
		
		JMenu game = new JMenu("Game");
		JMenuItem fromRandom = new JMenuItem("New game...");
		//JMenuItem fromImage = new JMenuItem("New game from image...");
		JMenuItem fromSeed = new JMenuItem("New game from seed...");
		
		JMenuItem quickA = new JMenuItem("New 10x10 @ 0.8 game");
		JMenuItem quickB = new JMenuItem("New 15x15 @ 0.8 game");
		JMenuItem quickC = new JMenuItem("New 30x15 @ 0.8 game");
		
		quickA.addActionListener(e->openGame(new Seed(10, 10, 0.8D)));
		quickB.addActionListener(e->openGame(new Seed(15, 15, 0.8D)));
		quickC.addActionListener(e->openGame(new Seed(30, 15, 0.8D)));

		
		
		game.add(fromRandom);
		//game.add(fromImage);
		game.add(fromSeed);
		game.addSeparator();
		game.add(quickA);
		game.add(quickB);
		game.add(quickC);
		
		
		
		bar.add(game);
		bar.add(help);
		
		
		frame.setJMenuBar(bar);
		gameContainer.setFocusable(true);
		
		final JLabel state = new JLabel("Game state");
		
		JScrollPane pane = new JScrollPane(gameContainer);
		pane.getVerticalScrollBar().setUnitIncrement(Board.SIZE);
		pane.getHorizontalScrollBar().setUnitIncrement(Board.SIZE);
		content.add(pane, BorderLayout.CENTER);
		content.add(state, BorderLayout.PAGE_START);
		
		frame.setSize(1000, 800);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	private static void openGame(Seed seed){
		gameContainer.removeAll();
		gameContainer.removeMouseListener(board);
		gameContainer.add(board = new Board(seed, gameContainer));
		gameContainer.revalidate();
		gameContainer.repaint();
	}
	
	private static void showControls(){
		JPanel help = new JPanel(new BorderLayout());
		
		JLabel controls = new JLabel("<html>- Left mouse button to fill a tile<br>- Right mouse button to place a cross<html>");
		controls.setBorder(BorderFactory.createTitledBorder("Playing"));
		
		JLabel test = new JLabel("<html>- T to enter test mode<br>- C to leave test mode and save changes<br>- V to leave test mode and undo changes<html>");
		test.setBorder(BorderFactory.createTitledBorder("Test mode"));
		
		JLabel check = new JLabel("<html>- R to view the original solution (note that other solutions might also be valid)</html>");
		check.setBorder(BorderFactory.createTitledBorder("Check"));
		
		help.add(controls, BorderLayout.PAGE_START);
		help.add(test, BorderLayout.CENTER);
		help.add(check, BorderLayout.PAGE_END);
		
		JOptionPane.showMessageDialog(frame, help, TITLE, JOptionPane.INFORMATION_MESSAGE);
	}
}
