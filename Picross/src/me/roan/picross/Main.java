package me.roan.picross;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main{
	
	private static final String TITLE = "Picross";
	private static final JFrame frame = new JFrame(TITLE);
	private static JPanel gameContainer = new JPanel(new BorderLayout());
	private static Board board = null;
	private static JTextField seedField;
	private static JLabel infoField;
	private static JLabel timerField;
	private static Timer timer = new Timer(1000, e->{
		if(board != null){
			long ms = board.getPassedTime();
			timerField.setText(String.format("Time: %02d:%02d", ms / 60000, (ms % 60000) / 1000));
		}
	});

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
		
		seedField = new JTextField("");
		seedField.setBorder(null);
		seedField.setEditable(false);
		infoField = new JLabel("", SwingConstants.LEFT);
		timerField = new JLabel("", SwingConstants.CENTER);
		
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
		
		quickA.addActionListener(e->openGame(new Seed(10, 7, 0.8D)));
		quickB.addActionListener(e->openGame(new Seed(15, 15, 0.8D)));
		quickC.addActionListener(e->openGame(new Seed(30, 15, 0.8D)));
		fromSeed.addActionListener(e->{
			JPanel form = new JPanel();
			form.add(new JLabel("Seed: "));
			JTextField field = new JTextField(32);
			form.add(field);
			if(JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(frame, form, TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, 0)){
				openGame(new Seed(field.getText()));
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
			
			if(JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(frame, form, TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, 0)){
				openGame(new Seed((int)width.getValue(), (int)height.getValue(), (double)density.getValue()));
			}
		});
		
		
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
		gameContainer.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
		
		JPanel state = new JPanel(new GridLayout(1, 3));
		state.add(infoField);
		state.add(timerField);
		JPanel rightFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rightFlow.add(seedField);
		state.add(rightFlow);

		content.add(gameContainer, BorderLayout.CENTER);
		content.add(state, BorderLayout.PAGE_START);
		
		frame.setSize(1000, 800);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher(){

			@Override
			public boolean dispatchKeyEvent(KeyEvent e){
				if(board != null){
					board.keyPressed(e);
				}
				return false;
			}
		});
	}
	
	private static void openGame(Seed seed){
		gameContainer.removeAll();
		gameContainer.removeMouseListener(board);
		gameContainer.add(board = new Board(seed));
		gameContainer.revalidate();
		gameContainer.repaint();
		seedField.setText(" Seed: " + board.getSeed());
		infoField.setText("Type: " + seed.width + "x" + seed.height + " @ " + seed.density);
		timerField.setText("Time: 00:00");
		timer.restart();
	}
	
	private static void showControls(){
		JPanel help = new JPanel();
		help.setLayout(new BoxLayout(help, BoxLayout.Y_AXIS));
		
		JLabel controls = new JLabel(
			"<html>"
			+ "- Left mouse button to fill a tile<br>"
			+ "- Right mouse button to place a cross<br>"
			+ "- Left click a filled tile to empty it<br>"
			+ "- Right click a cross to remove it<html>"
		);
		controls.setBorder(BorderFactory.createTitledBorder("Playing"));
		
		JLabel test = new JLabel(
			"<html>"
			+ "- T to enter test mode<br>"
			+ "- C to leave test mode and save changes<br>"
			+ "- V to leave test mode and undo changes<html>"
		);
		test.setBorder(BorderFactory.createTitledBorder("Test mode"));
		
		JLabel check = new JLabel(
			"<html>"
			+ "- R to view the original solution (note that other solutions might also be valid)<br>"
			+ "- H to hide the original solution</html>"
		);
		check.setBorder(BorderFactory.createTitledBorder("Check"));
		
		JLabel other = new JLabel(
			"<html>"
			+ "- You can drag the game around with the mouse if you click and hold outside the grid (useful if number are offscreen)"
		);
		other.setBorder(BorderFactory.createTitledBorder("Other"));
		
		help.add(controls);
		help.add(test);
		help.add(check);
		help.add(other);
		
		JOptionPane.showMessageDialog(frame, help, TITLE, JOptionPane.INFORMATION_MESSAGE);
	}
}
