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
	SEL_EMPTY,
	SEL_CROSS,
	SEL_FILL,
	SEL_TRY_CROSS,
	SEL_TRY_FILL;
	
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
	
	public boolean isReal(){
		return this == FILL || this == CROSS;
	}
	
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
	
	public boolean equalMode(Tile other){
		return (this.isTest() && other.isTest()) || (this.isReal() && other.isReal());
	}
}
