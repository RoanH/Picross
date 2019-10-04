package me.roan.picross;

public enum Tile{
	EMPTY,
	WHITE,
	BLACK,
	TRY_WHITE,
	TRY_BLACK;
	
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
	
	public boolean isTrial(){
		return this == TRY_BLACK || this == TRY_WHITE;
	}
}
