import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.Serializable;

import javax.swing.ImageIcon;

public class TilePanel extends JPanel implements Serializable{
	/*TilePanel class to hold information and perform tasks for each tile in
	 * the grid of a Minesweeper game*/
	private transient Image img;
	private String imgStr;
	private int tileNumber;
	private int rowPos;
	private int columnPos;
	private boolean isRevealed;
	private boolean isFlagged;
	
	public TilePanel(String img) {
		this(new ImageIcon(img).getImage());
		imgStr = img;
		tileNumber = 0;
		rowPos = 0;
		columnPos = 0;
		isRevealed = false;
		isFlagged = false;
	}
	
	
	public TilePanel(Image img) {
		this.img = img;
        Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
        setPreferredSize(size);
        /*setMinimumSize(size);
        setMaximumSize(size);*/
        setSize(size);
        setLayout(null);
	}
	
	public String getImageString() {
		return imgStr;
	}


	public void setImageString(String imgStr) {
		this.imgStr = imgStr;
	}


	public int getTileNumber() {
		return tileNumber;
	}
	
	public void setTileNumber(int tileNumber) {
		this.tileNumber = tileNumber;
	}
	
	public int getColumnPos() {
		return columnPos;
	}
	
	public void setColumnPos(int columnPos) {
		this.columnPos = columnPos;
	}
	
	public int getRowPos() {
		return rowPos;
	}
	
	public void setRowPos(int rowPos) {
		this.rowPos = rowPos;
	}

	public boolean getIsRevealed(){
		return isRevealed;
	}
	
	public void setIsRevealed(boolean isRevealed) {
		this.isRevealed = isRevealed;
	}
	
	public boolean getIsFlagged(){
		return isFlagged;
	}
	
	public void setIsFlagged(boolean isFlagged) {
		this.isFlagged = isFlagged;
	}
	
	public Image getImage() {
		return img;
	}


	public void setImage(Image img) {
		this.img = img;
	}


	public void setImage(String img) {
		this.img = new ImageIcon(img).getImage();
	}
	
    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }
    

}
