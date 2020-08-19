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
	 * @return True if this tile is not a tset mode tile.
	 */
	public boolean isReal(){
		return this == FILL || this == CROSS;
	}
	
	/**
	 * Check to see if the given tile can replace this tile.
	 * @param replacement The replacement tile.
	 * @param testMode Whether test mode is enabled or not.
	 * @return True if the given replacement tile can override this tile.
	 */
	public boolean canOverride(Tile replacement, boolean testMode){
		if((testMode && this.isReal()) || this == replacement){
			return false;
		}else{
			if(replacement == EMPTY){
				return (!testMode && this.isReal()) || this.isTest();
			}else{
				return this.equalMode(replacement) || this == EMPTY;
			}
		}
	}
	
	/**
	 * Checks to see of this tile and the given tile
	 * are for the same mode (test mode or not).
	 * @param other The other tile.
	 * @return True if this tile and the given tile are
	 *         for the same mode.
	 */
	public boolean equalMode(Tile other){
		return (this.isTest() && other.isTest()) || (this.isReal() && other.isReal());
	}
}
