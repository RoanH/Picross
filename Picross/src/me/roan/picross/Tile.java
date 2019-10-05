package me.roan.picross;

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
	 * Indicates a white or 'cross' tile.
	 */
	WHITE,
	/**
	 * Indicates a black or 'filled' tile.
	 */
	BLACK,
	/**
	 * Indicates a test mode white or 'cross' tile.
	 */
	TRY_WHITE,
	/**
	 * Indicates a test mode black or 'filled' tile.
	 */
	TRY_BLACK;
	
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
		case TRY_BLACK:
			return BLACK;
		case TRY_WHITE:
			return WHITE;
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
		case BLACK:
			return TRY_BLACK;
		case WHITE:
			return TRY_WHITE;
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
		return this == TRY_BLACK || this == TRY_WHITE;
	}
}
