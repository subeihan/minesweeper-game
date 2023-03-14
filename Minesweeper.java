import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;


public class Minesweeper extends JFrame {
	/*Minesweeper application*/
	private JPanel tilesPanel;
	private JPanel timerPanel;
	private JPanel labelPanel;
	private JLabel timerLabel;
	private JLabel bottomLabel;
	private TilePanel[][] tilesPanelArr;
	private int numOfHiddenMines;
	private int remainingTime;
	private boolean gameLost;
	private boolean gameWon;
	private Timer timer;
	
	private static final int NUM_OF_MINES = 40;
	private static final int NUM_OF_ROWS = 16;
	private static final int NUM_OF_COLUMNS = 16;
	private static final int NUM_OF_SECONDS = 1000;
	
	public Minesweeper() {
		
		createMenus();
		JPanel masterPanel = new JPanel();
		
		remainingTime = NUM_OF_SECONDS;
		timerPanel = new JPanel();
		timerLabel = new JLabel("Time Remaining: " + remainingTime);
		timerPanel.add(timerLabel);
		
		numOfHiddenMines = NUM_OF_MINES;
		labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottomLabel = new JLabel(String.valueOf(numOfHiddenMines));
		labelPanel.add(bottomLabel);
		
		tilesPanel = new JPanel();
		tilesPanel.setLayout(new GridLayout(NUM_OF_ROWS, NUM_OF_COLUMNS));
		tilesPanelArr = new TilePanel[NUM_OF_ROWS][NUM_OF_COLUMNS];
		for (int i = 0; i < NUM_OF_ROWS; i++) {
			for (int j = 0; j < NUM_OF_COLUMNS; j++) {
				tilesPanelArr[i][j] = new TilePanel("minesweepertiles/10.png");
				tilesPanelArr[i][j].setRowPos(i);
				tilesPanelArr[i][j].setColumnPos(j);
				tilesPanelArr[i][j].addMouseListener(new TileListener());
				tilesPanel.add(tilesPanelArr[i][j]);
			}
		}
		TimerListener timerListener = new TimerListener();
		timer = new Timer(1000, timerListener);
		createNewGame();

		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
		masterPanel.add(timerPanel);
		masterPanel.add(tilesPanel);
		masterPanel.add(labelPanel);
		masterPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		this.add(masterPanel);
		setSize(275,370);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void createMenus() {
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu menu = new JMenu("File"); 
		menuBar.add(menu);
		
		//create and add File-New item
		JMenuItem newItem = new JMenuItem("New");
		class NewActionListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				createNewGame();
			}
		}
		NewActionListener newListener = new NewActionListener();
		newItem.addActionListener(newListener);
		menu.add(newItem);
		
		
		//create and add File-Open item
		JMenuItem openItem = new JMenuItem("Open");
		class OpenActionListener implements ActionListener {
			ArrayList<GameState> storedGameList = null;
			JComboBox storedGameChoice;
			
			public void actionPerformed(ActionEvent event) {
				// create and show UI
				createOpenUI();

				// connect to the server to get stored games
				Socket socket = null;
				try {
					socket = new Socket("localhost", 8000);
					
					ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
					toServer.writeObject("Show stored games");
					toServer.flush();
					
					ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
					Object object = fromServer.readObject();
					storedGameList = (ArrayList<GameState>) object;
					
	
					if (storedGameList != null) {
						for (int i = 0; i < storedGameList.size(); i++) {	
							storedGameChoice.addItem((i + 1) + " - " + storedGameList.get(i).getName());	
						}
					}
					
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			private void createOpenUI() {
				// pop-up window for user to view the current stored games
				JFrame selectGame;
				JPanel backPanel;
				JLabel instructionLb;
				JPanel labelPanel;
				JPanel choicePanel;
				JButton loadGameButton;
				JPanel buttonPanel;
				
				selectGame = new JFrame("Stored games");
				backPanel = new JPanel();
				backPanel.setLayout(new GridLayout(3, 1));
				
				instructionLb = new JLabel("Please select a game to open");
				labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				labelPanel.add(instructionLb);
				
				storedGameChoice = new JComboBox();
				storedGameChoice.setSize(300, 20);
				storedGameChoice.setEditable(false);
				choicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				choicePanel.add(storedGameChoice);
				
				loadGameButton = new JButton("Load selected game");
				
				// create listener for the loadGameButton
				class ButtonListener implements ActionListener{
					@Override
					public void actionPerformed(ActionEvent e) {
						if (storedGameChoice.getSelectedItem() == null) {
							JOptionPane.showMessageDialog(selectGame, "Must select a game");
						}
						else {
							GameState selectedGame;
							int selectedItem = storedGameChoice.getSelectedIndex();
							selectedGame = storedGameList.get(selectedItem);
							remainingTime = selectedGame.getRemainingTime();
							timerLabel.setText("Time Remaining: " + remainingTime);
							numOfHiddenMines = selectedGame.getNumOfHiddenMines();
							TilePanel[][] selectedGameGrid = selectedGame.getTilesPanelArr();
							for (int i = 0; i < NUM_OF_ROWS; i++) {
								for (int j = 0; j < NUM_OF_COLUMNS; j++) {
									tilesPanelArr[i][j].setImageString(selectedGameGrid[i][j].getImageString());
									tilesPanelArr[i][j].setImage(selectedGameGrid[i][j].getImageString());
									tilesPanelArr[i][j].setTileNumber(selectedGameGrid[i][j].getTileNumber());
									tilesPanelArr[i][j].setIsRevealed(selectedGameGrid[i][j].getIsRevealed());
									tilesPanelArr[i][j].setIsFlagged(selectedGameGrid[i][j].getIsFlagged());
								}
							}

							gameWon = selectedGame.isGameWon();
							gameLost = selectedGame.isGameLost();
							if (gameWon) 
								bottomLabel.setText("Game won");
							else if (gameLost)
								bottomLabel.setText("Game lost");
							else {
								bottomLabel.setText(String.valueOf(numOfHiddenMines));
								timer.restart();
							}
							Minesweeper.this.repaint();
						}
					}
					
				}
				loadGameButton.addActionListener(new ButtonListener());
				buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
				buttonPanel.add(loadGameButton);

				backPanel.add(labelPanel);
				backPanel.add(choicePanel);
				backPanel.add(buttonPanel);
				selectGame.add(backPanel);
				selectGame.setSize(300, 150);
				selectGame.setResizable(false);
				repaint();
				selectGame.setVisible(true);
				selectGame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			}
		}
		OpenActionListener openListener = new OpenActionListener();
		openItem.addActionListener(openListener);
		menu.add(openItem);
		
		//create and add File-save item
		JMenuItem saveItem = new JMenuItem("Save");
		class SaveActionListener implements ActionListener {
			
			public void actionPerformed(ActionEvent e) {
				// stop counting down time
				timer.stop();
				
				// create pop-up window for user to enter a name for the gameState to be saved
				createSaveUI();
			}
			
			private void createSaveUI() {
				JFrame enterName;
				JPanel backPanel;
				JPanel nameTextPanel;
				JPanel buttonPanel;
				JLabel nameTextLabel;
				JTextField nameTextField;
				JButton saveButton;
				
				enterName = new JFrame("Save as name");
				backPanel = new JPanel(new GridLayout(2, 1));
				nameTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
				buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
				nameTextLabel = new JLabel("Please enter a name for the game: ");
				nameTextField = new JTextField(12);
				nameTextPanel.add(nameTextLabel);
				nameTextPanel.add(nameTextField);
				saveButton = new JButton("Save");
				buttonPanel.add(saveButton);
				enterName.setResizable(false);
				
				// create ActionListener for saveButton
				class SaveButtonListener implements ActionListener{
					@Override
					public void actionPerformed(ActionEvent event) {
						String name = nameTextField.getText();
						if (name.equals("")) {
							JOptionPane.showMessageDialog(enterName, "Must enter a name");
						}
						else {
							GameState gameState = new GameState(name, remainingTime, numOfHiddenMines, 
									tilesPanelArr, gameWon, gameLost);
					        Socket socket = null;
							try {
								socket = new Socket("localhost", 8000);
								ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
								toServer.writeObject(gameState);
								toServer.flush();
								DataInputStream fromServer = new DataInputStream(socket.getInputStream());
								String msgFromServer = fromServer.readUTF();
								JOptionPane.showMessageDialog(enterName, msgFromServer);
							} 
							catch (IOException e) {
								e.printStackTrace();
							}
							finally {
								try {
									socket.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				saveButton.addActionListener(new SaveButtonListener());
				backPanel.add(nameTextPanel);
				backPanel.add(buttonPanel);
				
				enterName.setSize(400, 100);
				enterName.add(backPanel);
				enterName.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				
				enterName.setVisible(true);
			}	
		}
		SaveActionListener saveListener = new SaveActionListener();
		saveItem.addActionListener(saveListener);
		menu.add(saveItem);
		
		//create and add File-TopFive item
		JMenuItem TopFiveItem = new JMenuItem("Top Five");
		class TopFiveActionListener implements ActionListener {
			ArrayList<GameWonScore> topFiveGameList = null;
			JComboBox topFiveGameChoice;
			
			public void actionPerformed(ActionEvent event) {
				// create and show UI
				createTopFiveUI();

				// connect to the server to get stored top-five high score games
				Socket socket = null;
				try {
					socket = new Socket("localhost", 8000);
					
					ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
					toServer.writeObject("Show top five games");
					toServer.flush();
					
					ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
					Object object = fromServer.readObject();
					topFiveGameList = (ArrayList<GameWonScore>) object;

					if (topFiveGameList != null) {
						for (int i = 0; i < topFiveGameList.size(); i++) {	
							topFiveGameChoice.addItem("Score" + " - " + topFiveGameList.get(i).getScore());	
						}
					}
					
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			private void createTopFiveUI() {
				// pop-up window for user to view the top five high score games
				JFrame topFiveGame;
				JPanel backPanel;
				JLabel instructionLb;
				JPanel labelPanel;
				JPanel choicePanel;
				JButton loadGameButton;
				JPanel buttonPanel;
				
				topFiveGame = new JFrame("Top-five games");
				backPanel = new JPanel();
				backPanel.setLayout(new GridLayout(3, 1));
				
				instructionLb = new JLabel("Please select a game to view");
				labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				labelPanel.add(instructionLb);
				
				topFiveGameChoice = new JComboBox();
				topFiveGameChoice.setSize(300, 20);
				topFiveGameChoice.setEditable(false);
				choicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				choicePanel.add(topFiveGameChoice);
				
				loadGameButton = new JButton("Load selected game");
				
				// create listener for the loadGameButton
				class ButtonListener implements ActionListener{
					@Override
					public void actionPerformed(ActionEvent e) {
						if (topFiveGameChoice.getSelectedItem() == null) {
							JOptionPane.showMessageDialog(topFiveGame, "Must select a game");
						}
						else {
							GameWonScore selectedGame;
							int selectedItem = topFiveGameChoice.getSelectedIndex();
							selectedGame = topFiveGameList.get(selectedItem);
							remainingTime = selectedGame.getScore();
							numOfHiddenMines = 0;
							gameWon = true;
							gameLost = false;
							TilePanel[][] selectedGameGrid = selectedGame.getTilesPanelArr();
							for (int i = 0; i < NUM_OF_ROWS; i++) {
								for (int j = 0; j < NUM_OF_COLUMNS; j++) {
									tilesPanelArr[i][j].setImageString(selectedGameGrid[i][j].getImageString());
									tilesPanelArr[i][j].setImage(selectedGameGrid[i][j].getImageString());
									tilesPanelArr[i][j].setTileNumber(selectedGameGrid[i][j].getTileNumber());
									tilesPanelArr[i][j].setIsRevealed(selectedGameGrid[i][j].getIsRevealed());
									tilesPanelArr[i][j].setIsFlagged(selectedGameGrid[i][j].getIsFlagged());
								}
							}

							bottomLabel.setText("Game won");

							Minesweeper.this.repaint();
						}
					}
					
				}
				loadGameButton.addActionListener(new ButtonListener());
				buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
				buttonPanel.add(loadGameButton);

				backPanel.add(labelPanel);
				backPanel.add(choicePanel);
				backPanel.add(buttonPanel);
				topFiveGame.add(backPanel);
				topFiveGame.setSize(300, 150);
				topFiveGame.setResizable(false);
				repaint();
				topFiveGame.setVisible(true);
				topFiveGame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			}
		}
		TopFiveActionListener topFiveListener = new TopFiveActionListener();
		TopFiveItem.addActionListener(topFiveListener);
		menu.add(TopFiveItem);		
		
		//create and add File-Exit item.
		JMenuItem exitItem = new JMenuItem("Exit");
		class ExitListener implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		}
		ExitListener exitListener = new ExitListener();
		exitItem.addActionListener(exitListener);
		menu.add(exitItem);
	}
	
	private void createNewGame() {
		//initialization or clear previous game
		gameLost = false;
		gameWon = false;
		remainingTime = NUM_OF_SECONDS;
		timerLabel.setText("Time Remaining: " + remainingTime);
		numOfHiddenMines = NUM_OF_MINES;
		bottomLabel.setText(String.valueOf(numOfHiddenMines));

		for (int i = 0; i < NUM_OF_ROWS; i++) {
			for (int j = 0; j < NUM_OF_COLUMNS; j++) {
				tilesPanelArr[i][j].setImageString("minesweepertiles/10.png");
				tilesPanelArr[i][j].setImage("minesweepertiles/10.png");
				tilesPanelArr[i][j].setTileNumber(0);
				tilesPanelArr[i][j].setIsRevealed(false);
				tilesPanelArr[i][j].setIsFlagged(false);
			}
		}
		Minesweeper.this.repaint();

		//randomly set positions of mines
		int selected = 0;
		int row = 0;
		int column = 0;
		for (int i = 0; i < 40; i++) {
			selected = (int) (1 + Math.random() * (NUM_OF_ROWS * NUM_OF_COLUMNS));
			if (selected % NUM_OF_COLUMNS == 0) {
				row = selected / NUM_OF_COLUMNS - 1;
				column = NUM_OF_COLUMNS - 1;
			}
			else {
				row = selected / NUM_OF_COLUMNS;
				column = selected % NUM_OF_COLUMNS - 1;
			}
					
			while (tilesPanelArr[row][column].getTileNumber() == 9) {
				selected = (int) (1 + Math.random() * (NUM_OF_ROWS * NUM_OF_COLUMNS));
				if (selected % NUM_OF_COLUMNS == 0) {
					row = selected / NUM_OF_COLUMNS - 1;
					column = NUM_OF_COLUMNS - 1;
				}
				else {
					row = selected / NUM_OF_COLUMNS;
					column = selected % NUM_OF_COLUMNS - 1;
				}
			}
			tilesPanelArr[row][column].setTileNumber(9);
			
		}
		
		//calculate number of surrounding tiles
		for (int i = 0; i < NUM_OF_ROWS; i++) {
			for (int j = 0; j< NUM_OF_COLUMNS; j++) {
				int numOfMines = 0;
				if (tilesPanelArr[i][j].getTileNumber() != 9) {
					for (int k = i - 1; k <= i + 1; k++) {
						for (int l = j - 1; l <= j + 1; l++) {
							if ((k >= 0) && (k < NUM_OF_ROWS) && (l >= 0) && (l < NUM_OF_COLUMNS)) {
								if (tilesPanelArr[k][l].getTileNumber() == 9)
									numOfMines += 1;
							}
						}
					}
					tilesPanelArr[i][j].setTileNumber(numOfMines);
				}	
			}
		}
		
		timer.restart();

	}
	
	class TileListener extends MouseAdapter{
		@Override
		public void mouseClicked(MouseEvent e) {
			if (gameWon || gameLost)
				return;
			if (e.getButton() == MouseEvent.BUTTON1) {//if left click
				TilePanel clickedTile = (TilePanel) e.getSource();
				
				if ((!clickedTile.getIsRevealed()) && (!clickedTile.getIsFlagged())){
					int tileNumber = clickedTile.getTileNumber();
					clickedTile.setImageString("minesweepertiles/" + tileNumber + ".png");
					clickedTile.setImage("minesweepertiles/" + tileNumber + ".png");
					clickedTile.setIsRevealed(true);
					if (tileNumber == 9) {// if left click on a mine
						for (int i = 0; i < NUM_OF_ROWS; i++) {
							for (int j = 0; j< NUM_OF_COLUMNS; j++) {
								if ((tilesPanelArr[i][j].getTileNumber() == 9) && (!tilesPanelArr[i][j].getIsFlagged())) {
									tilesPanelArr[i][j].setImageString("minesweepertiles/9.png");
									tilesPanelArr[i][j].setImage("minesweepertiles/9.png");
									tilesPanelArr[i][j].setIsRevealed(true);
								}
								else if ((tilesPanelArr[i][j].getTileNumber() != 9) && (tilesPanelArr[i][j].getIsFlagged())) {
									tilesPanelArr[i][j].setImageString("minesweepertiles/12.png");
									tilesPanelArr[i][j].setImage("minesweepertiles/12.png");
								}
							}
						}
						gameLost = true;
						bottomLabel.setText("Game lost");
					}
					else{
						if (tileNumber == 0) {//expand on tiles with no adjacent mines
							LinkedList<TilePanel> zeroTileQueue = new LinkedList<>();
							zeroTileQueue.offer(clickedTile);
							
							TilePanel currentTile = zeroTileQueue.poll();
							while (currentTile != null) {
								int row = currentTile.getRowPos();
								int col = currentTile.getColumnPos();
								for (int i = row - 1; i <= row + 1; i++) {
									for (int j = col - 1; j <= col + 1; j++) {
										if ((i >= 0) && (i < NUM_OF_ROWS) && (j >= 0) && (j < NUM_OF_COLUMNS)
												&& (!tilesPanelArr[i][j].getIsRevealed())) {//if valid and unrevealed tile
											if (tilesPanelArr[i][j].getTileNumber() == 0) {//if the tile has no adjacent mines
												tilesPanelArr[i][j].setImageString("minesweepertiles/0.png");
												tilesPanelArr[i][j].setImage("minesweepertiles/0.png");
												tilesPanelArr[i][j].setIsRevealed(true);
												zeroTileQueue.offer(tilesPanelArr[i][j]);
											}
											else if ((tilesPanelArr[i][j].getTileNumber() >= 1) && 
													(tilesPanelArr[i][j].getTileNumber() <= 8)) {//if the tile has at least one adjacent mine
												tilesPanelArr[i][j].setImageString("minesweepertiles/" + tilesPanelArr[i][j].getTileNumber() + ".png");
												tilesPanelArr[i][j].setImage("minesweepertiles/" + tilesPanelArr[i][j].getTileNumber() + ".png");
												tilesPanelArr[i][j].setIsRevealed(true);
											}
										}
									}
								}
								currentTile = zeroTileQueue.poll();
							}
		
						}
						
						//check if game is won
						gameWon = true;
						int i = 0;
						while (i < NUM_OF_ROWS) {
							int j = 0;
							while (gameWon && (j < NUM_OF_COLUMNS)) {
								// if any tile that is not a mine is not revealed, the game is not won yet
								if ((tilesPanelArr[i][j].getTileNumber() >= 0) && (tilesPanelArr[i][j].getTileNumber() <= 8)) {
									if (!tilesPanelArr[i][j].getIsRevealed())
										gameWon = false;
								}
								j++;
							}
							i++;
						}
						
						if (gameWon) {
							bottomLabel.setText("Game won");
							// check if the game has top-5 score, and store the game if so
							GameWonScore game = new GameWonScore(remainingTime, tilesPanelArr);
							sendGameToServer(game);
						}
						
					}
					repaint();
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON3) {//if right click
				TilePanel clickedTile = (TilePanel) e.getSource();
				if (!clickedTile.getIsRevealed()) {
					if (clickedTile.getIsFlagged()) {
						clickedTile.setImageString("minesweepertiles/10.png");
						clickedTile.setImage("minesweepertiles/10.png");
						clickedTile.setIsFlagged(false);
						numOfHiddenMines += 1;
						bottomLabel.setText(String.valueOf(numOfHiddenMines));
					}
					else {
						clickedTile.setImageString("minesweepertiles/11.png");
						clickedTile.setImage("minesweepertiles/11.png");
						clickedTile.setIsFlagged(true);
						numOfHiddenMines -= 1;
						bottomLabel.setText(String.valueOf(numOfHiddenMines));
					}
				}
				repaint();	
			}
			else {
				
			}
		}
	}
	
	class TimerListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			if (remainingTime > 0 && (!gameLost) && (!gameWon)) {
				remainingTime -= 1;
				timerLabel.setText("Time Remaining: " + remainingTime);
			}
			if (remainingTime == 0 && (!gameLost) && (!gameWon)) {
				for (int i = 0; i < NUM_OF_ROWS; i++) {
					for (int j = 0; j< NUM_OF_COLUMNS; j++) {
						if ((tilesPanelArr[i][j].getTileNumber() == 9) && (!tilesPanelArr[i][j].getIsFlagged())) {
							tilesPanelArr[i][j].setImageString("minesweepertiles/9.png");
							tilesPanelArr[i][j].setImage("minesweepertiles/9.png");
							tilesPanelArr[i][j].setIsRevealed(true);
						}
						else if ((tilesPanelArr[i][j].getTileNumber() != 9) && (tilesPanelArr[i][j].getIsFlagged())) {
							tilesPanelArr[i][j].setImageString("minesweepertiles/12.png");
							tilesPanelArr[i][j].setImage("minesweepertiles/12.png");
						}
					}
				}
				gameLost = true;
				bottomLabel.setText("Game lost");
			}
		}
	}
	
	private void sendGameToServer(GameWonScore game) {
		Socket socket = null;
		
		try {
			socket = new Socket("localhost", 8000);
			ObjectOutputStream gameToServer = new ObjectOutputStream(socket.getOutputStream());
			gameToServer.writeObject(game);
			gameToServer.flush();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static void main(String[] args) {
		Minesweeper minesweeper = new Minesweeper();

	}

}
