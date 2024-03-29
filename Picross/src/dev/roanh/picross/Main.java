/*
 * Picross: A nonogram puzzle program. 
 * Copyright (C) 2019  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/Picross
 *
 * Picross is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Picross is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.picross;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import dev.roanh.util.ClickableLink;
import dev.roanh.util.Dialog;
import dev.roanh.util.Util;

/**
 * Picross (nonogram) puzzle program.
 * @author Roan
 * @see <a href="https://github.com/RoanH/Picross">GitHub (RoanH/Picross)</a>
 */
public class Main{
	/**
	 * Frame title used for all windows.
	 */
	private static final String TITLE = "Picross";
	/**
	 * Main frame for this program.
	 */
	private static final JFrame frame = new JFrame(TITLE);
	/**
	 * The panel holding the game board.
	 */
	private static JPanel gameContainer = new JPanel(new BorderLayout());
	/**
	 * The board that is currently shown.
	 */
	private static Board board = null;
	/**
	 * Field that displays the board seed.
	 */
	private static JTextField seedField;
	/**
	 * Field that displays the board type.
	 */
	private static JLabel infoField;
	/**
	 * Field that displays the elapsed time.
	 */
	private static JLabel timerField;
	/**
	 * Timer used to update the elapsed game time.
	 */
	private static Timer timer = new Timer(1000, e->{
		if(board != null){
			long ms = board.getPassedTime();
			timerField.setText(String.format("Time: %02d:%02d", ms / 60000, (ms % 60000) / 1000));
		}
	});

	/**
	 * Starts the program.
	 * @param args No valid command line arguments.
	 */
	public static void main(String[] args){
		showGameGUI();
	}
	
	/**
	 * Constructs and shows the GUI.
	 */
	private static final void showGameGUI(){
		Util.installUI();

		try{
			Image img = ImageIO.read(ClassLoader.getSystemResource("icon.png"));
			Dialog.setDialogIcon(img);
			frame.setIconImage(img);
		}catch(IOException e){
			//pity
		}
		Dialog.setParentFrame(frame);
		Dialog.setDialogTitle(TITLE);

		JPanel content = new JPanel(new BorderLayout());
		content.setFocusable(true);
		frame.add(content);
		
		seedField = new JTextField("");
		seedField.setBorder(null);
		seedField.setEditable(false);
		infoField = new JLabel("", SwingConstants.LEFT);
		timerField = new JLabel("", SwingConstants.CENTER);
		seedField.addMouseListener(new MouseListener(){
			
			@Override
			public void mouseReleased(MouseEvent e){
			}
			
			@Override
			public void mousePressed(MouseEvent e){
			}
			
			@Override
			public void mouseExited(MouseEvent e){
			}
			
			@Override
			public void mouseEntered(MouseEvent e){
			}
			
			@Override
			public void mouseClicked(MouseEvent e){
				seedField.select(7, seedField.getText().length());
				StringSelection seed = new StringSelection(board == null ? "" : board.getSeed().toString());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(seed, seed);
			}
		});
		
		JMenuBar bar = new JMenuBar();
		
		JMenu help = new JMenu("Help");
		JMenuItem controls = new JMenuItem("Controls");
		controls.addActionListener(e->showControls());
		JMenuItem rules = new JMenuItem("Rules");
		rules.addActionListener(e->showRules());
		help.add(rules);
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
		fromSeed.addActionListener(e->{
			JPanel form = new JPanel();
			form.add(new JLabel("Seed: "));
			JTextField field = new JTextField(32);
			form.add(field);
			
			if(Dialog.showSelectDialog(form)){
				Seed seed = null;
				try{
					seed = new Seed(field.getText());
				}catch(IllegalArgumentException e2){
					Dialog.showErrorDialog("The provided seed is invalid.");
					return;
				}
				openGame(seed);
			}
		});
		fromRandom.addActionListener(e->{
			JPanel form = new JPanel(new BorderLayout());
			JPanel spinners = new JPanel(new GridLayout(3, 1, 0, 2));
			JPanel labels = new JPanel(new GridLayout(3, 1, 0, 2));
			
			labels.add(new JLabel("Width: "));
			JSpinner width = new JSpinner(new SpinnerNumberModel(15, 1, Integer.MAX_VALUE, 5));
			spinners.add(width);
			
			labels.add(new JLabel("Height: "));
			JSpinner height = new JSpinner(new SpinnerNumberModel(15, 1, Integer.MAX_VALUE, 5));
			spinners.add(height);
			
			labels.add(new JLabel("Density: "));
			JSpinner density = new JSpinner(new SpinnerNumberModel(0.8D, 0.0D, 100.0D, 0.1D));
			spinners.add(density);
			
			form.add(labels, BorderLayout.LINE_START);
			form.add(spinners, BorderLayout.CENTER);
			form.add(new JLabel("Game settings: "), BorderLayout.PAGE_START);
			
			if(Dialog.showSelectDialog(form)){
				openGame(new Seed((int)width.getValue(), (int)height.getValue(), (double)density.getValue()));
			}
		});
		
		JMenuItem reset = new JMenuItem("Reset");
		reset.addActionListener(boardAction(Board::reset));
		
		JMenuItem showSolution = new JMenuItem("Show solution");
		showSolution.addActionListener(boardAction(b->b.showSolution(true)));
		
		JMenuItem hideSolution = new JMenuItem("Hide solution");
		hideSolution.addActionListener(boardAction(b->b.showSolution(false)));
		
		JMenuItem undo = new JMenuItem("Undo");
		undo.addActionListener(boardAction(Board::undo));
		
		JMenuItem redo = new JMenuItem("Redo");
		redo.addActionListener(boardAction(Board::redo));
		
		JMenuItem mistakes = new JMenuItem("Clear wrong tiles");
		mistakes.addActionListener(boardAction(Board::clearMistakes));
		
		game.add(fromRandom);
		//game.add(fromImage);
		game.add(fromSeed);
		game.addSeparator();
		game.add(quickA);
		game.add(quickB);
		game.add(quickC);
		game.addSeparator();
		game.add(showSolution);
		game.add(hideSolution);
		game.addSeparator();
		game.add(undo);
		game.add(redo);
		game.addSeparator();
		game.add(mistakes);
		game.add(reset);
		
		JMenu testMode = new JMenu("Test mode");
		
		JMenuItem enterTest = new JMenuItem("Enter test mode");
		enterTest.addActionListener(boardAction(Board::enterTestMode));
		
		JMenuItem leaveTestSave = new JMenuItem("Leave & save changes");
		leaveTestSave.addActionListener(boardAction(b->b.leaveTestMode(true)));
		
		JMenuItem leaveTestUndo = new JMenuItem("Leave & undo changes");
		leaveTestUndo.addActionListener(boardAction(b->b.leaveTestMode(false)));
		
		testMode.add(enterTest);
		testMode.addSeparator();
		testMode.add(leaveTestSave);
		testMode.add(leaveTestUndo);
		
		JMenu view = new JMenu("View");

		JMenuItem zoomIn = new JMenuItem("Zoom in");
		zoomIn.addActionListener(boardAction(b->b.changeZoom(b.getZoom() * 1.25D)));

		JMenuItem zoomOut = new JMenuItem("Zoom out");
		zoomOut.addActionListener(boardAction(b->b.changeZoom(Math.max(0.1D, b.getZoom() * 0.75D))));

		JMenuItem zoomReset = new JMenuItem("Reset zoom");
		zoomReset.addActionListener(boardAction(b->b.changeZoom(1.0D)));

		JMenuItem moveUp = new JMenuItem("Move view up");
		moveUp.addActionListener(boardAction(Board::moveViewUp));

		JMenuItem moveDown = new JMenuItem("Move view down");
		moveDown.addActionListener(boardAction(Board::moveViewDown));

		JMenuItem moveLeft = new JMenuItem("Move view left");
		moveLeft.addActionListener(boardAction(Board::moveViewLeft));

		JMenuItem moveRight = new JMenuItem("Move view right");
		moveRight.addActionListener(boardAction(Board::moveViewRight));

		JMenuItem moveReset = new JMenuItem("Reset translations");
		moveReset.addActionListener(boardAction(Board::resetTranslation));
		
		view.add(zoomIn);
		view.add(zoomOut);
		view.add(zoomReset);
		view.addSeparator();
		view.add(moveUp);
		view.add(moveDown);
		view.add(moveLeft);
		view.add(moveRight);
		view.add(moveReset);
		
		bar.add(game);
		bar.add(view);
		bar.add(testMode);
		bar.add(help);
		
		frame.setJMenuBar(bar);
		gameContainer.setFocusable(true);
		gameContainer.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
		
		JPanel state = new JPanel(new GridLayout(1, 3));
		state.add(infoField);
		state.add(timerField);
		JPanel rightFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rightFlow.add(seedField);
		state.add(rightFlow);
		
		JPanel footer = new JPanel(new GridLayout(1, 2));
		footer.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		footer.add(Util.getVersionLabel("Picross", "v1.4", false, SwingConstants.LEFT));//XXX Version number - don't forget build.gradle
		JLabel git = new JLabel("<html>GitHub: <font color=blue><u>RoanH/Picross</u></font></html>", SwingConstants.RIGHT);
		git.addMouseListener(new ClickableLink("https://github.com/RoanH/Picross"));
		footer.add(git);

		content.add(gameContainer, BorderLayout.CENTER);
		content.add(state, BorderLayout.PAGE_START);
		content.add(footer, BorderLayout.PAGE_END);
		
		frame.setSize(1000, 800);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	/**
	 * Constructs an action listener that invokes the given
	 * function on the current game board is one is present.
	 * @param fun The function to call on the game board.
	 * @return A newly constructed action listener.
	 */
	private static ActionListener boardAction(Consumer<Board> fun){
		return e->{
			if(board != null){
				fun.accept(board);
			}
		};
	}
	
	/**
	 * Opens a game with the given seed.
	 * @param seed The seed to generate the board with.
	 */
	private static void openGame(Seed seed){
		gameContainer.removeAll();
		gameContainer.add(board = new Board(seed));
		gameContainer.revalidate();
		gameContainer.repaint();
		seedField.setText(" Seed: " + board.getSeed());
		infoField.setText("Type: " + seed.width + "x" + seed.height + " @ " + seed.density);
		timerField.setText("Time: 00:00");
		timer.restart();
	}
	
	/**
	 * Shows a help dialog with the rules.
	 */
	private static void showRules(){
		JLabel text = new JLabel(
			"<html>"
			+ "The goal of the game is to change every tile in the grid to either a filled tile or a tile with a cross in it.<br>"
			+ "To do this the numbers in front of each row and the number above each column give hits as to which tiles have to<br>"
			+ "be filled. Each individual number represents a sequence of filled connected tiles on that row or column.<br>"
			+ "These sequences appear in the same order as their associated numbers. So for example the hint <tt>2 3 2 1</tt><br>"
			+ "means that in order there is a sequence of 2 filled tiles, followed by a sequence of 3 filled tiles, followed by a<br>"
			+ "sequence of 2 and finally followed by a sequence of 1 tile. Between these sequences are one or more cross tiles.<br><br>"
			+ "As you are playing the program will start graying out the numbers. When a number turns gray this means that you have<br>"
			+ "enclosed a sequence of that length and it is connected via a sequence of filled and/or cross tiles to the side of the<br>"
			+ "game board. If a number turns red this means that there is an error with the sequence representing the number that<br>"
			+ "turned red. If all the numbers turn red this means that either too many or too few sequences are present on a line/column<br>"
			+ "while the entire line is filled with filled tiles or cross tiles.<br><br>"
			+ "Filling the entire grid using these hints completes the game. Have fun!"
		);
		text.setBorder(BorderFactory.createTitledBorder("Rules"));
		
		Dialog.showMessageDialog(text);
	}
	
	/**
	 * Shows a help dialog with the controls.
	 */
	private static void showControls(){
		JPanel help = new JPanel();
		help.setLayout(new BoxLayout(help, BoxLayout.Y_AXIS));
		
		JLabel controls = new JLabel(
			"<html>"
			+ "<b>Mouse:</b><br>"
			+ "- Left mouse button to fill a tile<br>"
			+ "- Right mouse button to place a cross<br>"
			+ "- Left click a filled tile to empty it<br>"
			+ "- Right click a cross to remove it<br>"
			+ "- You can select an area by dragging to update multiple tiles at once<br><br>"
			+ "<b>Keyboard:</b><br>"
			+ "- W to move up<br>"
			+ "- S to move down<br>"
			+ "- A to move left<br>"
			+ "- D to move right<br>"
			+ "- Space bar to fill a tile<br>"
			+ "- Shift to place a cross<br>"
			+ "- Press space bar on a filled tile to empty it<br>"
			+ "- Press shift on a cross to remove it<html>"
		);
		controls.setBorder(BorderFactory.createTitledBorder("Playing"));
		
		JLabel moving = new JLabel(
			"<html>"
			+ "- Up arrow to move the view up (and board down)<br>"
			+ "- Down arrow to move the view down (and board up)<br>"
			+ "- Right arrow to move the view right (and board left)<br>"
			+ "- Left arrow to move the view left (and board right)<br>"
			+ "- Mouse scroll wheel to zoom in and out<br>"
			+ "- You can drag the game around with the mouse if you click and hold outside the grid or also hold down Ctrl<html>"
		);
		moving.setBorder(BorderFactory.createTitledBorder("Moving"));
		
		JLabel test = new JLabel(
			"<html>"
			+ "- T to enter test mode<br>"
			+ "- C to leave test mode and save changes<br>"
			+ "- V to leave test mode and undo changes<html>"
		);
		test.setBorder(BorderFactory.createTitledBorder("Test mode"));
		
		JLabel history = new JLabel(
			"<html>"
			+ "- Ctrl+Z to undo the last edit<br>"
			+ "- Ctrl+Y to redo the last edit<html>"
		);
		history.setBorder(BorderFactory.createTitledBorder("History"));
		
		JLabel check = new JLabel(
			"<html>"
			+ "- R to toggle showing the original solution (note that other solutions might also be valid)</html>"
		);
		check.setBorder(BorderFactory.createTitledBorder("Check"));
		
		help.add(controls);
		help.add(moving);
		help.add(test);
		help.add(history);
		help.add(check);
		
		Dialog.showMessageDialog(help);
	}
}
