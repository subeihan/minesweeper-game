import java.io.Serializable;

public class GameWonScore implements Serializable {

	/*GameWonScore class to hold the information for games won*/
	private static final long serialVersionUID = 5520491328019582158L;
	private int score;
	private TilePanel[][] tilesPanelArr;
	
	public GameWonScore(int score, TilePanel[][] tilesPanelArr){
		this.score = score;
		this.tilesPanelArr = tilesPanelArr;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public TilePanel[][] getTilesPanelArr() {
		return tilesPanelArr;
	}

	public void setTilesPanelArr(TilePanel[][] tilesPanelArr) {
		this.tilesPanelArr = tilesPanelArr;
	}

}
