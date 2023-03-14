import java.io.Serializable;

public class GameState implements Serializable{

	/*GameState class to hold the information for the state of a game*/
	private static final long serialVersionUID = -1428330160420418941L;
	private String name;
	private int remainingTime;
	private int numOfHiddenMines;
	private TilePanel[][] tilesPanelArr;
	private boolean gameWon;
	private boolean gameLost;
	
	public GameState(String name, int remainingTime, int numOfHiddenMines, TilePanel[][] tilesPanelArr, 
			boolean gameWon, boolean gameLost){
		this.name = name;
		this.remainingTime = remainingTime;
		this.numOfHiddenMines = numOfHiddenMines;
		this.tilesPanelArr = tilesPanelArr;
		this.gameWon = gameWon;
		this.gameLost = gameLost;
	}


	public int getRemainingTime() {
		return remainingTime;
	}


	public void setRemainingTime(int remainingTime) {
		this.remainingTime = remainingTime;
	}

	

	public int getNumOfHiddenMines() {
		return numOfHiddenMines;
	}


	public void setNumOfHiddenMines(int numOfHiddenMines) {
		this.numOfHiddenMines = numOfHiddenMines;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public TilePanel[][] getTilesPanelArr() {
		return tilesPanelArr;
	}


	public void setTilesPanelArr(TilePanel[][] tilesPanelArr) {
		this.tilesPanelArr = tilesPanelArr;
	}


	public boolean isGameWon() {
		return gameWon;
	}


	public void setGameWon(boolean gameWon) {
		this.gameWon = gameWon;
	}


	public boolean isGameLost() {
		return gameLost;
	}


	public void setGameLost(boolean gameLost) {
		this.gameLost = gameLost;
	}
	
}
