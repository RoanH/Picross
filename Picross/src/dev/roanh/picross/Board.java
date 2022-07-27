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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import javax.swing.JPanel;

/**
 * Board class that keeps track of the game state
 * and displays the puzzle.
 * @author Roan
 * @see Tile
 * @see Seed
 */
public class Board extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
	/**
	 * Serial ID.
	 */
	private static final long serialVersionUID = 6310638885364285013L;
	/**
	 * Color used to indicate test mode related elements.
	 */
	private static final Color TEST_MODE_COLOR = Color.BLUE;
	/**
	 * Color used to indicate hint numbers that aren't marked correctly.
	 */
	private static final Color MISTAKE_COLOR = Color.RED;
	/**
	 * Color used to indicate elements of a solved puzzle.
	 */
	private static final Color SOLVED_COLOR = Color.GREEN.darker().darker();
	/**
	 * Background color for numbers when they start moving with the view.
	 */
	private static final Color BACKDROP = new Color(1.0F, 1.0F, 1.0F, 0.8F);
	/**
	 * Font to use to draw the hint numbers.
	 */
	private static final Font NUMBERS = new Font("Dialog", Font.BOLD, 15);
	/**
	 * Composite to draw area actions with.
	 */
	private static final Composite FADE_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2F);
	/**
	 * Size in pixels of the grid cells.
	 */
	private static final int SIZE = 50;
	/**
	 * Number of pixels to move by when moving the view.
	 */
	private static final int DELTA = 20;
	/**
	 * The time at which this board was created.
	 */
	private long startTime = System.currentTimeMillis();
	/**
	 * The time at which this board was solved or <code>-1L</code>.
	 */
	private long endTime = -1L;
	/**
	 * The seed for this board.
	 * @see Seed
	 */
	private final Seed seed;
	/**
	 * Random number generator for this board.
	 */
	private final Random random;
	/**
	 * Solution this puzzle is based on.
	 */
	private final boolean[][] solution;
	/**
	 * Hint numbers for each row.
	 */
	private final int[][] rowHints;
	/**
	 * Hint numbers for each column.
	 */
	private final int[][] colHints;
	/**
	 * Current state of each grid cell.
	 * @see Tile
	 */
	private final Tile[][] state;
	/**
	 * Number of columns in the grid.
	 */
	private final int width;
	/**
	 * Number of rows in the grid.
	 */
	private final int height;
	/**
	 * Grid translation along the x-axis.
	 */
	private double dx = 0;
	/**
	 * Grid translation along the y-axis.
	 */
	private double dy = 0;
	/**
	 * Whether or not the solution is shown.
	 */
	private boolean reveal = false;
	/**
	 * Whether or not test mode is enabled.
	 */
	private boolean testMode = false;
	/**
	 * Last drag location.
	 */
	private Point last;
	/**
	 * Last click location.
	 */
	private Point lastPress;
	/**
	 * x-coordinate of the currently selected grid cell.
	 * Will be <code>-1</code> if the users is not playing
	 * with the keyboard.
	 */
	private int x = -1;
	/**
	 * y-coordinate of the currently selected grid cell.
	 */
	private int y = 0;
	/**
	 * Current marking judgement for all the rows.
	 */
	private Boolean[][] rowJudgement;
	/**
	 * Current marking judgement for all the columns.
	 */
	private Boolean[][] colJudgement;
	/**
	 * Whether or not this puzzle is currently solved.
	 */
	private boolean solved = false;
	/**
	 * Additional tiles selected on the x-axis
	 * for the currently selected area.
	 */
	private int hx = 0;
	/**
	 * Additional tiles selected on the y-axis
	 * for the currently selected area.
	 */
	private int hy = 0;
	/**
	 * The type of tile to place when the mouse is released.
	 */
	private Tile nextType = null;
	/**
	 * Current tile type of the tile an area selection was started from.
	 */
	private Tile baseType = null;
	/**
	 * Current zoom level.
	 */
	private double zoom = 1.0D;
	/**
	 * Undo stack of moves that can be undone.
	 */
	private Deque<List<StateChange>> undoStack = new ArrayDeque<List<StateChange>>();
	/**
	 * Redo stack of moves that can be redone.
	 */
	private Deque<List<StateChange>> redoStack = new ArrayDeque<List<StateChange>>();
	
	/**
	 * Constructs a new board from
	 * the given seed.
	 * @param seed The seed to generate
	 *        the board from.
	 * @see Seed
	 */
	public Board(Seed seed){
		this.setFocusable(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.addMouseWheelListener(this);
		
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
		rowJudgement = new Boolean[height][];
		colJudgement = new Boolean[width][];
		
		initialiseGrid();
	}
	
	/**
	 * Gets the number of milliseconds that have
	 * passed since this board was created.
	 * @return The number of milliseconds that
	 *         have passed since this board was created.
	 */
	public long getPassedTime(){
		return (endTime == -1 ? System.currentTimeMillis() : endTime) - startTime;
	}
	
	/**
	 * Check whether or not test mode
	 * is currently enabled.
	 * @return True if test mode is enabled,
	 *         false if it is not.
	 */
	public boolean isTestMode(){
		return testMode;
	}
	
	/**
	 * Returns whether this board is solved.
	 * @return True if this board is solved,
	 *         false if it is not.
	 */
	public boolean isSolved(){
		return solved;
	}
	
	/**
	 * Returns the total number of tiles in the grid
	 * of the given type.
	 * @param type The type of tile to count.
	 * @return The total number of tiles in the grid
	 *         of the given type.
	 */
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
	
	/**
	 * Gets the seed for this board.
	 * @return The seed for this board.
	 */
	public Seed getSeed(){
		return seed;
	}
	
	/**
	 * Converts the given x-coordinate
	 * in pixel to a grid tile.
	 * @param px The x-coordinate to convert.
	 * @return The grid x-coordinate that
	 *         corresponds with the given
	 *         on screen x-coordinate.
	 */
	private int toGridX(int px){
		int lx = (int)((px - dx - ((this.getWidth() - width * SIZE) / 2)) / zoom);
		return lx < 0 ? -1 : (lx / SIZE);
	}
	
	/**
	 * Converts the given y-coordinate
	 * in pixel to a grid tile.
	 * @param py The y-coordinate to convert.
	 * @return The grid y-coordinate that
	 *         corresponds with the given
	 *         on screen y-coordinate.
	 */
	private int toGridY(int py){
		int ly = (int)((py - dy - ((this.getHeight() - height * SIZE) / 2)) / zoom);
		return ly < 0 ? -1 : (ly / SIZE);
	}
	
	/**
	 * Sets the new state for the tile at the
	 * given coordinates based on the current game state.
	 * @param x The x-coordinate for the tile.
	 * @param y The y-coordinate for the tile.
	 * @param newState The new state to change to.
	 */
	public void setNextState(int x, int y, Tile newState){
		Tile nextState = nextTileState(x, y, newState);
		if(state[x][y].canOverride(nextState, testMode, state[x][y])){
			undoStack.push(Collections.singletonList(applyStateChange(x, y, nextState)));
		}
	}
	
	/**
	 * Changes the state of the given title
	 * to the given new value.
	 * @param x The x-coordinate to update.
	 * @param y The y-coordinate to update.
	 * @param set The new title state.
	 * @return An event describing the change.
	 */
	private StateChange applyStateChange(int x, int y, Tile set){
		if(isWithinGridBounds(x, y) && !solved){
			StateChange event = new StateChange(x, y, state[x][y], set, testMode);
			event.apply();
			if(!testMode){
				checkSolution();
			}
			
			this.repaint();
			redoStack.clear();
			return event;
		}else{
			return null;
		}
	}
	
	/**
	 * Checks to see if the board is
	 * in a solved state and if so
	 * updates the internal state of
	 * this board to reflect that by
	 * stopping time from passing and
	 * setting the {@link #solved} flag
	 * to <code>true</code>.
	 * @see #endTime
	 * @see #solved
	 */
	private void checkSolution(){
		if(isGridComplete()){
			if(isSolutionValid()){
				endTime = System.currentTimeMillis();
				solved = true;
			}
		}
	}
	
	/**
	 * Checks to see if the entire grid is filled
	 * with final solution tiles. Final solution
	 * tiles are non-empty and non-test mode tiles.
	 * This means that the entire board has to be
	 * filled with {@link Tile#FILL} and
	 * {@link Tile#CROSS} tiles.
	 * @return Whether or not the entire grid is filled.
	 * @see Tile
	 */
	public boolean isGridComplete(){
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				if(state[x][y] == Tile.EMPTY || state[x][y].isTest()){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Checks to see if board in its
	 * current states contain no errors
	 * and that all hints have been met.
	 * Note that it is not checked if the
	 * entire grid is filled with tiles.
	 * @return Whether the current solution is valid.
	 * @see #isGridComplete()
	 */
	public boolean isSolutionValid(){
		for(int y = 0; y < height; y++){
			for(int i = 0; i < rowJudgement[y].length; i++){
				if(rowJudgement[y][i] != Boolean.TRUE){
					return false;
				}
			}
		}
		for(int x = 0; x < width; x++){
			for(int i = 0; i < colJudgement[x].length; i++){
				if(colJudgement[x][i] != Boolean.TRUE){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Enters test mode.
	 */
	public void enterTestMode(){
		if(!solved){
			testMode = true;
			this.repaint();
		}
	}
	
	/**
	 * Leaves test mode.
	 * @param save Whether to save the changes
	 *        made or to revert them.
	 */
	public void leaveTestMode(boolean save){
		if(testMode){
			List<StateChange> changes = new ArrayList<StateChange>();
			if(save){
				for(int x = 0; x < width; x++){
					for(int y = 0; y < height; y++){
						if(state[x][y].isTest()){
							changes.add(applyStateChange(x, y, state[x][y].toReal()));
						}
					}
				}
				checkSolution();
			}else{
				for(int x = 0; x < width; x++){
					for(int y = 0; y < height; y++){
						if(state[x][y].isTest()){
							changes.add(applyStateChange(x, y, Tile.EMPTY));
						}
					}
				}
			}
			if(!changes.isEmpty()){
				undoStack.push(changes);
			}
			testMode = false;
			this.repaint();
		}
	}
	
	/**
	 * Sets whether to show the solution or not.
	 * @param shown True to show the solution,
	 *        false to hide it.
	 */
	public void showSolution(boolean shown){
		reveal = shown;
		this.repaint();
	}
	
	/**
	 * Changes the current zoom level to the given level.
	 * @param newZoom The new zoom level.
	 */
	public void changeZoom(double newZoom){
		dx = (((dx - SIZE * (width / 2.0D)) * newZoom) / zoom) + SIZE * (width / 2.0D);
		dy = (((dy - SIZE * (height / 2.0D)) * newZoom) / zoom) + SIZE * (height / 2.0D);
		zoom = newZoom;
		this.repaint();
	}
	
	/**
	 * Gets the current zoom level.
	 * @return The current zoom level.
	 */
	public double getZoom(){
		return zoom;
	}
	
	/**
	 * Moves the view down.
	 */
	public void moveViewDown(){
		dy -= DELTA;
		this.repaint();
	}
	
	/**
	 * Moves the view up.
	 */
	public void moveViewUp(){
		dy += DELTA;
		this.repaint();
	}
	
	/**
	 * Moves the view right.
	 */
	public void moveViewRight(){
		dx -= DELTA;
		this.repaint();
	}
	
	/**
	 * Moves the view left.
	 */
	public void moveViewLeft(){
		dx += DELTA;
		this.repaint();
	}
	
	/**
	 * Resets all view translations.
	 */
	public void resetTranslation(){
		dx = 0;
		dy = 0;
		this.repaint();
	}
	
	/**
	 * Resets the board to its initial cleared state.
	 */
	public void reset(){
		x = -1;
		testMode = false;
		reveal = false;
		dx = 0;
		dy = 0;
		zoom = 1.0D;
		solved = false;
		endTime = -1;
		startTime = System.currentTimeMillis();
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				state[x][y] = Tile.EMPTY;
			}
			Arrays.fill(colJudgement[x], Boolean.FALSE);
		}
		for(int y = 0; y < height; y++){
			Arrays.fill(rowJudgement[y], Boolean.FALSE);
		}
		undoStack.clear();
		redoStack.clear();
		this.repaint();
	}
	
	/**
	 * Clears any titles that have a tile that doesn't
	 * match the original solution for the board.
	 */
	public void clearMistakes(){
		if(!solved){
			List<StateChange> changes = new ArrayList<StateChange>();
			for(int x = 0; x < width; x++){
				for(int y = 0; y < height; y++){
					if(state[x][y].isReal() && testMode){
						continue;
					}
					
					if(state[x][y].toReal() == Tile.FILL && !solution[x][y] || state[x][y].toReal() == Tile.CROSS && solution[x][y]){
						changes.add(applyStateChange(x, y, Tile.EMPTY));
					}
				}
			}
			
			if(!changes.isEmpty()){
				undoStack.push(changes);
			}
		}
	}
	
	/**
	 * Returns the total number of tiles in the grid.
	 * @return The total number of tiles in the grid.
	 */
	public int getTileCount(){
		return width * height;
	}
	
	/**
	 * Initialises the gird by computing the hints from the seed.
	 * @see #seed
	 * @see #rowHints
	 * @see #colHints
	 */
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
			rowJudgement[y] = new Boolean[nums];
			Arrays.fill(rowJudgement[y], Boolean.FALSE);
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
			colJudgement[x] = new Boolean[nums];
			Arrays.fill(colJudgement[x], Boolean.FALSE);
			Arrays.fill(buffer, 0);
			nums = 0;
		}
	}
	
	/**
	 * Computes the judgement for the given row and column.
	 * @param x The column to compute the judgement for.
	 * @param y The row to compute the judgement for.
	 * @see #computeJudgement(Boolean[], int[], int, Function)
	 * @see #computeColJudgement(int)
	 * @see #computeRowJudgement(int)
	 */
	private void computeJudgement(int x, int y){
		computeColJudgement(x);
		computeRowJudgement(y);
	}
	
	/**
	 * Computes the judgement for the given row.
	 * @param y The row to compute the judgement for.
	 * @see #computeJudgement(Boolean[], int[], int, Function)
	 * @see #computeColJudgement(int)
	 * @see #computeJudgement(int, int)
	 */
	private void computeRowJudgement(final int y){
		computeJudgement(rowJudgement[y], rowHints[y], width, x->state[x][y]);
	}
	
	/**
	 * Computes the judgement for the given column.
	 * @param x The row to compute the judgement for.
	 * @see #computeJudgement(Boolean[], int[], int, Function)
	 * @see #computeRowJudgement(int)
	 * @see #computeJudgement(int, int)
	 */
	private void computeColJudgement(final int x){
		computeJudgement(colJudgement[x], colHints[x], height, y->state[x][y]);
	}
	
	/**
	 * Computes the judgement for a row or column by
	 * stating which numbers have to be grayed out
	 * or which numbers have to be rendered in red
	 * because they are marked invalid.
	 * @param result The array to store the resulting
	 *        judgement in. The judgement for a row or column
	 *        is stored as an array of {@link Boolean}.
	 *        Each index of this array corresponds
	 *        to the hint at the same index in the
	 *        <code>hints</code> array. If the {@link Boolean}
	 *        value is <code>null</code> then there
	 *        is an error with the sequence the hint represents,
	 *        if the value is <code>true</code> then the
	 *        sequence is correctly marked, if the value
	 *        if <code>false</code> then the sequence is not marked yet.
	 * @param hints The hints or numbers for the row
	 *        or column to check.
	 * @param max The maximum row or column index.
	 * @param state A function that maps an integer
	 *        index to the row or column state at
	 *        that index.
	 * @see #computeColJudgement(int)
	 * @see #computeRowJudgement(int)
	 * @see #computeJudgement(int, int)
	 */
	private void computeJudgement(Boolean[] result, int[] hints, int max, Function<Integer, Tile> state){
		//All are false initially
		Arrays.fill(result, Boolean.FALSE);
		
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
					h = result.length - 1;
					f = 0;
					continue;
				}else{
					//break if we were already looking from the right
					break;
				}
			}
			
			//if the current tile is filled
			boolean filled = (state.apply(x) == Tile.FILL) || (state.apply(x) == Tile.TRY_FILL);
			//if we're at the end of a search
			boolean end = (x == 0 && dir == -1) || (x == max - 1 && dir == 1);
			
			//increment chain length
			if(filled){
				f++;
			}
			
			//if we hit the end or are on a cross tile
			if(state.apply(x) == Tile.CROSS || state.apply(x) == Tile.TRY_CROSS || end){
				if(f != 0){
					//too many chains found
					if((h < 0 || h >= result.length)){
						Arrays.fill(result, null);
					}else{
						//check if the found chain length matches the one we're looking for
						if(f == hints[h]){
							result[h] = Boolean.TRUE;
						}else{
							//invalidate the chain that was found
							result[h] = null;
						}
						h += dir;
						f = 0;
					}
				}else{
					//hit the end without finding any chains
					if((x == max - 1 && dir == 1) && h == 0){
						Arrays.fill(result, null);
					}
				}
				//hit the end without finding enough chains
				if((x == max - 1 && dir == 1) && h != result.length){
					Arrays.fill(result, null);
				}
			}
			
			x += dir;
		}
	}
	
	/**
	 * Gets the state of the tile at the given coordinates.
	 * Usually this is the actual state on the board of the
	 * tile. However when the given tile is part of the currently
	 * selected area this reflected the state the tile will
	 * change to when the area is realised.
	 * @param x The x coordinate of the tile to get.
	 * @param y The y coordinate of the tile to get.
	 * @return The state of the requested tile.
	 * @see Tile
	 */
	private Tile getState(int x, int y){
		if(lastPress != null && !solved){
			int mx = Math.min(lastPress.x, lastPress.x + hx);
			int my = Math.min(lastPress.y, lastPress.y + hy);
			if(x >= mx && x <= mx + Math.abs(hx) && y >= my && y <= my + Math.abs(hy)){
				if(state[x][y].canOverride(nextType, testMode, baseType)){
					return nextType.toSelection();
				}
			}
		}
		return state[x][y];
	}
	
	/**
	 * Gets the next tile stage for the tile at the given
	 * location given a specific base action. Typically the
	 * returned tile will either be the provided base tile or
	 * the test mode variant of the base tile. If the given
	 * grid cell is currently not empty the returned tile
	 * will be empty.
	 * @param x The x coordinate of the tile to check.
	 * @param y The y coordinate of the tile to check.
	 * @param base The base tile action.
	 * @return The next tile state for the requested tile.
	 */
	private Tile nextTileState(int x, int y, Tile base){
		Tile current = state[x][y];
		if(current == Tile.EMPTY){
			return testMode ? base.toTest() : base;
		}else if(testMode){
			return current == base.toTest() ? Tile.EMPTY : base.toTest();
		}else{
			return current == base ? Tile.EMPTY : base;
		}
	}
	
	/**
	 * Checks if the given on screen coordinate is within
	 * the game grid.
	 * @param p The point to check.
	 * @return True if the point is within the game grid.
	 */
	private boolean isWithinBounds(Point p){
		return isWithinGridBounds(toGridX(p.x), toGridY(p.y));
	}
	
	/**
	 * Checks if the grid cell denoted by the given
	 * point is within the bounds of the grid.
	 * @param p The point to check.
	 * @return True if the point is within the game grid.
	 */
	private boolean isWithinGridBounds(Point p){
		return isWithinGridBounds(p.x, p.y);
	}
	
	/**
	 * Checks if the cell denoted by the given
	 * coordinates is within the bounds of the grid.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @return True if the point is within the game grid.
	 */
	private boolean isWithinGridBounds(int x, int y){
		return x >= 0 && y >= 0 && x < width && y < height;
	}
	
	/**
	 * Undoes the last move executed.
	 */
	public void undo(){
		if(!undoStack.isEmpty() && !solved){
			List<StateChange> events = undoStack.pop();
			events.forEach(StateChange::undo);
			redoStack.push(events);
			this.repaint();
		}
	}
	
	/**
	 * Redoes the last move undone.
	 */
	public void redo(){
		if(!redoStack.isEmpty() && !solved){
			List<StateChange> events = redoStack.pop();
			events.forEach(StateChange::apply);
			undoStack.push(events);
			this.repaint();
		}
	}
	
	@Override
	public Dimension getPreferredSize(){
		return new Dimension((int)(width * SIZE + dx * 2), (int)(height * SIZE + dy + 1));
	}
	
	@Override
	public void paintComponent(Graphics g1){
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		if(testMode){
			g.setColor(TEST_MODE_COLOR);
			g.drawString(" Test mode", 0, 15);
		}else if(solved){
			g.setColor(SOLVED_COLOR);
			g.drawString(" Solved", 0, 15);
		}
		
		FontMetrics fm = g.getFontMetrics();
		int filled = getTileCount(Tile.FILL);
		int tryFill = getTileCount(Tile.TRY_FILL);
		int crossed = getTileCount(Tile.CROSS);
		int tryCross = getTileCount(Tile.TRY_CROSS);
		
		g.setColor(Color.BLACK);
		String line = " Filled: " + filled;
		g.drawString(line, 0, 30);
		if(testMode){
			g.setColor(TEST_MODE_COLOR);
			g.drawString(" (+" + tryFill + ")", fm.stringWidth(line), 30);
		}
		
		g.setColor(Color.BLACK);
		line = " Crossed: " + crossed;
		g.drawString(line, 0, 45);
		if(testMode){
			g.setColor(TEST_MODE_COLOR);
			g.drawString(" (+" + tryCross + ")", fm.stringWidth(line), 45);
		}
		
		g.setColor(Color.BLACK);
		line = String.format(" Done: %1$.2f%%", (100.0D * (filled + crossed)) / getTileCount());
		g.drawString(line, 0, 60);
		if(testMode){
			g.setColor(TEST_MODE_COLOR);
			g.drawString(String.format(" (+%1$.2f%%)", (100.0D * (tryFill + tryCross)) / getTileCount()), fm.stringWidth(line), 60);
		}
		
		g.setColor(Color.BLACK);
		line = " Moves: " + undoStack.size();
		g.drawString(line, 0, 75);
		
		//origin at the top left corner of the grid
		double ox = (this.getWidth() - width * SIZE) / 2.0D + dx;
		double oy = (this.getHeight() - height * SIZE) / 2.0D + dy;
		g.translate(ox, oy);
		g.scale(zoom, zoom);
		ox /= zoom;
		oy /= zoom;
		
		//grid
		g.setColor(Color.GRAY);
		for(int x = 0; x <= width; x++){
			g.fillRect(x * SIZE - 1, -1, 2, height * SIZE + 2);
		}
		for(int y = 0; y <= height; y++){
			g.fillRect(-1, y * SIZE - 1, width * SIZE + 2, 2);
		}
		
		g.setColor(Color.BLACK);
		for(int x = 0; x <= width; x += 5){
			g.fillRect(x * SIZE - 1, -1, 2, height * SIZE + 2);
		}
		for(int y = 0; y <= height; y += 5){
			g.fillRect(-1, y * SIZE - 1, width * SIZE + 2, 2);
		}
		
		//cell status
		for(int x = 0; x < width; x++){
			for(int y = 0; y < height; y++){
				Tile currentState = getState(x, y);
				Composite comp = g.getComposite();
				if(currentState == Tile.SEL_EMPTY){
					g.setComposite(FADE_COMPOSITE);
					currentState = state[x][y];
				}
				
				switch(currentState){
				case SEL_FILL:
					g.setComposite(FADE_COMPOSITE);
					//$FALL-THROUGH$
				case FILL:
					g.setColor(Color.BLACK);
					g.fillRect(x * SIZE + 5, y * SIZE + 5, SIZE - 10, SIZE - 10);
					break;
				case SEL_CROSS:
					g.setComposite(FADE_COMPOSITE);
					//$FALL-THROUGH$
				case CROSS:
					g.setColor(Color.BLACK);
					g.drawLine(x * SIZE + 5, y * SIZE + 5, x * SIZE + SIZE - 5, y * SIZE + SIZE - 5);
					g.drawLine(x * SIZE + SIZE - 5, y * SIZE + 5, x * SIZE + 5, y * SIZE + SIZE - 5);
					break;
				case SEL_TRY_FILL:
					g.setComposite(FADE_COMPOSITE);
					//$FALL-THROUGH$
				case TRY_FILL:
					g.setColor(Color.BLUE);
					g.fillRect(x * SIZE + 5, y * SIZE + 5, SIZE - 10, SIZE - 10);
					break;
				case SEL_TRY_CROSS:
					g.setComposite(FADE_COMPOSITE);
					//$FALL-THROUGH$
				case TRY_CROSS:
					g.setColor(Color.BLUE);
					g.drawLine(x * SIZE + 5, y * SIZE + 5, x * SIZE + SIZE - 5, y * SIZE + SIZE - 5);
					g.drawLine(x * SIZE + SIZE - 5, y * SIZE + 5, x * SIZE + 5, y * SIZE + SIZE - 5);
					break;
				default:
					break;
				}
				
				g.setComposite(comp);
				
				if(reveal && solution[x][y]){
					g.setColor(Color.RED);
					g.fillRect(x * SIZE + 15, y * SIZE + 15, 20, 20);
				}
			}
		}
		
		if(hx != 0 || hy != 0){
			g.setColor(Color.BLACK);
			g.drawString(
				(Math.abs(hx) + 1) + " x " + (Math.abs(hy) + 1),
				Math.min(lastPress.x, lastPress.x + hx) * SIZE + 5,
				Math.min(lastPress.y, lastPress.y + hy) * SIZE + g.getFontMetrics().getHeight()
			);
		}
		
		if(x != -1){
			g.setColor(Color.RED);
			g.drawRect(x * SIZE + 1, y * SIZE + 1, SIZE - 3, SIZE - 3);
		}
		
		g.setFont(NUMBERS);
		g.setColor(Color.BLACK);
		
		//row numbers
		for(int y = 0; y < height; y++){
			Boolean[] found = rowJudgement[y];
			int offset = -10;
			int s = -20 * rowHints[y].length;
			if(ox < -s){
				offset -= ox + s;
				g.setColor(BACKDROP);
				g.fillRect(offset + s, y * SIZE + 15, -s + 10, SIZE - 30);
			}
			for(int i = rowHints[y].length - 1; i >= 0; i--){
				g.setColor(solved ? SOLVED_COLOR : (found[i] == null ? MISTAKE_COLOR : (found[i] ? Color.GRAY : Color.BLACK)));
				String str = String.valueOf(rowHints[y][i]);
				g.drawString(str, offset - (g.getFontMetrics().stringWidth(str) / 2), y * SIZE + (SIZE + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2);
				offset -= 20;
			}
		}
		
		//column numbers
		for(int x = 0; x < width; x++){
			Boolean[] found = colJudgement[x];
			int offset = -5;
			int s = -20 * colHints[x].length;
			if(oy < -s){
				offset -= oy + s;
				g.setColor(BACKDROP);
				g.fillRect(x * SIZE + 15, offset + s, SIZE - 30, -s + 5);
			}
			for(int i = colHints[x].length - 1; i >= 0; i--){
				g.setColor(solved ? SOLVED_COLOR : (found[i] == null ? MISTAKE_COLOR : (found[i] ? Color.GRAY : Color.BLACK)));
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
		last = e.getPoint();
		if(!e.isControlDown()){
			lastPress = new Point(toGridX(last.x), toGridY(last.y));
			if(isWithinGridBounds(lastPress)){
				baseType = state[lastPress.x][lastPress.y];
				switch(e.getButton()){
				case MouseEvent.BUTTON1:
					nextType = nextTileState(lastPress.x, lastPress.y, Tile.FILL);
					break;
				case MouseEvent.BUTTON3:
					nextType = nextTileState(lastPress.x, lastPress.y, Tile.CROSS);
					break;
				default:
					baseType = null;
					lastPress = null;
					break;
				}
			}else{
				lastPress = null;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e){
		if(lastPress != null){
			int mx = Math.min(lastPress.x, lastPress.x + hx);
			int my = Math.min(lastPress.y, lastPress.y + hy);
			List<StateChange> changes = new ArrayList<StateChange>();
			for(int x = mx; x <= mx + Math.abs(hx); x++){
				for(int y = my; y <= my + Math.abs(hy); y++){
					if(state[x][y].canOverride(nextType, testMode, baseType)){
						changes.add(applyStateChange(x, y, testMode ? nextType.toTest() : nextType));
					}
				}
			}
			if(!changes.isEmpty()){
				undoStack.push(changes);
			}
			nextType = null;
			baseType = null;
		}
		
		this.repaint();
		lastPress = null;
		last = null;
		hx = 0;
		hy = 0;
	}

	@Override
	public void mouseEntered(MouseEvent e){
		this.requestFocusInWindow();
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
		case KeyEvent.VK_SPACE:
			setNextState(x, y, Tile.FILL);
			break;
		case KeyEvent.VK_SHIFT:
			setNextState(x, y, Tile.CROSS);
			break;
		case KeyEvent.VK_W:
			if(x == -1){
				x = 0;
			}else if(y > 0){
				y--;
			}
			this.repaint();
			break;
		case KeyEvent.VK_S:
			if(x == -1){
				x = 0;
			}else if(y < height - 1){
				y++;
			}
			this.repaint();
			break;
		case KeyEvent.VK_D:
			if(x == -1){
				x = 0;
			}else if(x < width - 1){
				x++;
			}
			this.repaint();
			break;
		case KeyEvent.VK_A:
			if(x == -1){
				x = 0;
			}else if(x > 0){
				x--;
			}
			this.repaint();
			break;
		case KeyEvent.VK_T:
			enterTestMode();
			break;
		case KeyEvent.VK_R:
			showSolution(!reveal);
			break;
		case KeyEvent.VK_V:
			leaveTestMode(false);
			break;
		case KeyEvent.VK_C:
			leaveTestMode(true);
			break;
		case KeyEvent.VK_UP:
		case KeyEvent.VK_KP_UP:
			moveViewUp();
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_KP_LEFT:
			moveViewLeft();
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_RIGHT:
			moveViewRight();
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_KP_DOWN:
			moveViewDown();
			break;
		case KeyEvent.VK_Z:
			if(e.isControlDown()){
				undo();
			}
			break;
		case KeyEvent.VK_Y:
			if(e.isControlDown()){
				redo();
			}
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e){		
	}

	@Override
	public void mouseDragged(MouseEvent e){
		Point to = e.getPoint();
		int tx = toGridX(to.x);
		int ty = toGridY(to.y);
		
		if(!isWithinGridBounds(tx, ty) || solved || e.isControlDown()){
			if(last == null || (isWithinBounds(last) && !e.isControlDown() && !solved)){
				return;
			}
			dx += to.x - last.x;
			dy += to.y - last.y;
		}else if(lastPress != null){
			hx = tx - lastPress.x;
			hy = ty - lastPress.y;
		}
		
		last = to;
		this.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e){		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e){
		changeZoom(Math.max(zoom * (e.getWheelRotation() == -1 ? 1.1D : 0.9), 0.1D));
	}
	
	/**
	 * Event describing a state change of a single tile.
	 * @author Roan
	 */
	private final class StateChange{
		/**
		 * The x-coordinate that changed.
		 */
		private final int x;
		/**
		 * The y-coordinate that changed.
		 */
		private final int y;
		/**
		 * The previous state of the tile.
		 */
		private Tile old;
		/**
		 * The new state of the tile.
		 */
		private Tile next;
		/**
		 * True if test mode was enabled during this change.
		 */
		private boolean test;
		
		/**
		 * Constructs a new change event with the
		 * given location and old and new state.
		 * @param x The x-coordinate.
		 * @param y The y-coordinate.
		 * @param old The old tile state.
		 * @param next The new tile state.
		 * @param test Whether test mode was enabled.
		 */
		private StateChange(int x, int y, Tile old, Tile next, boolean test){
			this.x = x;
			this.y = y;
			this.old = old;
			this.next = next;
			this.test = test;
		}
		
		/**
		 * Reverts the change described by this event.
		 */
		private void undo(){
			testMode = test;
			state[x][y] = old;
			computeJudgement(x, y);
		}
		
		/**
		 * Applies the change described by this event.
		 */
		private void apply(){
			testMode = test;
			state[x][y] = next;
			computeJudgement(x, y);
		}
	}
}
