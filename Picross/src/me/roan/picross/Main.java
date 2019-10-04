package me.roan.picross;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class Main{

	public static void main(String[] args){
		showGameGUI();
	}
	
	private static final void showGameGUI(){
		UIManager.getDefaults().put("ScrollPane.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[]{}));

		JFrame frame = new JFrame("Picross");
		
		JPanel content = new JPanel(new BorderLayout());
		frame.add(content);
		
		final JLabel state = new JLabel("Game state");
		
		Board board = new Board(30, 15);
		
		JScrollPane pane = new JScrollPane(board);
		pane.getVerticalScrollBar().setUnitIncrement(Board.SIZE);
		pane.getHorizontalScrollBar().setUnitIncrement(Board.SIZE);
		content.add(pane, BorderLayout.CENTER);
		content.add(state, BorderLayout.PAGE_START);
		
		frame.setSize(1000, 800);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
