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

/**
 * Enum of different Tile states.
 * @author Roan
 */
public enum Tile{
	/**
	 * Indicates an empty tile.
	 */
	EMPTY,
	/**
	 * Indicates a 'cross' tile.
	 */
	CROSS,
	/**
	 * Indicates a 'filled' tile.
	 */
	FILL,
	/**
	 * Indicates a test mode 'cross' tile.
	 */
	TRY_CROSS,
	/**
	 * Indicates a test mode 'filled' tile.
	 */
	TRY_FILL,
	/**
	 * Indicates an area selection tile that will transition
	 * towards the {@link #EMPTY} state.
	 */
	SEL_EMPTY,
	/**
	 * Indicates an area selection tile that will transition
	 * towards the {@link #CROSS} state.
	 */
	SEL_CROSS,
	/**
	 * Indicates an area selection tile that will transition
	 * towards the {@link #FILL} state.
	 */
	SEL_FILL,
	/**
	 * Indicates an area selection tile that will transition
	 * towards the {@link #TRY_CROSS} state.
	 */
	SEL_TRY_CROSS,
	/**
	 * Indicates an area selection tile that will transition
	 * towards the {@link #TRY_FILL} state.
	 */
	SEL_TRY_FILL;
	
	/**
	 * Gets the area selection variant of this tile.
	 * @return The area selection variant of this tile.
	 */
	public Tile toSelection(){
		switch(this){
		case FILL:
			return SEL_FILL;
		case EMPTY:
			return SEL_EMPTY;
		case TRY_FILL:
			return SEL_TRY_FILL;
		case TRY_CROSS:
			return SEL_TRY_CROSS;
		case CROSS:
			return SEL_CROSS;
		default:
			return this;
		}
	}
	
	/**
	 * Converts this tile to it's non-test
	 * mode equivalent tile. Returns this
	 * tile itself if this tile is not a
	 * test mode tile.
	 * @return The non-test mode version
	 *         of this tile.
	 */
	public Tile toReal(){
		switch(this){
		case TRY_FILL:
			return FILL;
		case TRY_CROSS:
			return CROSS;
		default:
			return this;
		}
	}
	
	/**
	 * Converts this tile to it's test
	 * mode equivalent tile. Returns this
	 * tile itself if this tile is already a
	 * test mode tile.
	 * @return The non-test mode version
	 *         of this tile.
	 */
	public Tile toTest(){
		switch(this){
		case FILL:
			return TRY_FILL;
		case CROSS:
			return TRY_CROSS;
		default:
			return this;
		}
	}
	
	/**
	 * Returns if this tile is a test mode tile or not.
	 * @return True if this tile is a test mode tile,
	 *         false if it is not.
	 */
	public boolean isTest(){
		return this == TRY_FILL || this == TRY_CROSS;
	}
	
	/**
	 * Returns if this tile is a non test mode tile.
	 * @return True if this tile is not a test mode tile.
	 */
	public boolean isReal(){
		return this == FILL || this == CROSS;
	}
	
	/**
	 * Check to see if the given tile can replace this tile.
	 * @param replacement The replacement tile.
	 * @param testMode Whether test mode is enabled or not.
	 * @param base The type of the tile was initially clicked.
	 * @return True if the given replacement tile can override this tile.
	 */
	public boolean canOverride(Tile replacement, boolean testMode, Tile base){
		if((testMode && this.isReal()) || this == replacement){
			return false;
		}else{
			if(replacement == EMPTY){
				return (!testMode && this.isReal()) || this.isTest();
			}else{
				return this == base || this == EMPTY;
			}
		}
	}
}
