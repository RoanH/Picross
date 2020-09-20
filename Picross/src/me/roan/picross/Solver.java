package me.roan.picross;

public class Solver{
	private Board board;
	private int rows;
	private int cols;
	
	public Solver(Board board){
		this.board = board;
		Seed seed = board.getSeed();
		rows = seed.height;
		cols = seed.width;
	}
	
	public void solveAll(){
		
	}
	
	public void solveOne(){
		addCrossesFromLeft();
	}
	
	private void addCrossesFromLeft(){
		for(int row = 0; row < rows; row++){
			int[] hints = board.getRowHints(row);
			Boolean[] state = board.getRowJudgement(row);
			
			int idx = 0;
			while(idx < cols && board.getTile(idx, row) != Tile.EMPTY){
				idx++;
			}
			
			int hint = 0;
			while(hint < hints.length && state[hint]){
				hint++;
			}
			
			if(idx != cols && hint != hints.length){
				int run = 0;
				for(int c = idx - 1; c >= 0; c--){
					if(board.getTile(c, row) == Tile.FILL){
						run++;
					}else{
						break;
					}
				}
				
				if(run == hints[hint]){
					board.setNextState(idx, row, Tile.CROSS);
				}
			}
		}
		
		
		
	}
	
	
	
	
	
	
	
	
}
