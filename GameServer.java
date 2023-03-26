import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GameServer implements Runnable {
	/*server for the game, accept connections from Minesweeper application and
	 * store a game state or a game with top-five high score */
	
	private int clientNum = 0;
	private static ExecutorService threadPool = new ThreadPoolExecutor(3, 10, 1, TimeUnit.MINUTES, 
			new ArrayBlockingQueue<>(10), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
	
	public GameServer() {
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		try {
			// Create a server socket
	        ServerSocket serverSocket = new ServerSocket(8000);
	        
	        while (true) {
	        	Socket socket = serverSocket.accept();
	        	clientNum++;

	        	// Create and start a new thread for the connection
	        	threadPool.execute(new HandleAClient(socket, clientNum));
	        }
	       
		}
		catch(IOException e) {
			System.err.println(e);
		}
		

	}
	
	class HandleAClient implements Runnable {
	    private Socket socket;
	    private int clientNum;
	    ObjectOutputStream outputToDatabase;
	    DataOutputStream messageToClient;
	    
	    
	    // Construct a thread
	    public HandleAClient(Socket socket, int clientNum) {
	      this.socket = socket;
	      this.clientNum = clientNum;
	    }
	    
	    // Run a thread
	    public void run() {
	    	try {
	    		// Continuously serve the client
	    		while (!socket.isClosed()) {
    				ObjectInputStream objectFromClient = new ObjectInputStream(socket.getInputStream());
	    			Object object = objectFromClient.readObject();
	    			
	    			if ((object instanceof String) && object.equals("Show stored games")){
	    				// if client send a request for stored games, query the database
		    			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
		    			ArrayList<GameState> storedGame = queryGameStore();
		    			if (storedGame != null) {
		    				toClient.writeObject(storedGame);
		    				toClient.flush();
		    			}
	    			}
	    			else if(object instanceof GameState){
	    				// if client send a GameState object, insert it into the gameStateStore table
		    			GameState gameState = (GameState) object;
		    			writeToGameStore(gameState);
		    			messageToClient = new DataOutputStream(socket.getOutputStream());
		    			messageToClient.writeUTF("Game saved");		    			
	    			}
	    			else if ((object instanceof String) && object.equals("Show top five games")) {
	    				// if client send a request for stored top-five high score games, query the database
	    				ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
		    			ArrayList<GameWonScore> topFiveGame = queryTopFive();
		    			if (topFiveGame != null) {
		    				toClient.writeObject(topFiveGame);
		    				toClient.flush();
		    			}
	    			}
	    			else if(object instanceof GameWonScore){
	    				// if client send a GameWOnScore object, insert it into topFiveGames table
	    				GameWonScore game = (GameWonScore) object;
		    			retainTopFive(game);	    			
	    			}
	    			objectFromClient.close();
	    		}
	    	}
	    	catch (IOException | ClassNotFoundException e) {
	      		e.printStackTrace();
	    	}
	    }
	    
	    public void writeToGameStore(GameState gameState) {
	    	PreparedStatement preparedStatement;
			Connection connection = null;
			if (gameState != null) {
				try {
					connection = DriverManager.getConnection("jdbc:sqlite:minesweeper.db");
					
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					outputToDatabase = new ObjectOutputStream(bos);   
					outputToDatabase.writeObject(gameState);
					outputToDatabase.flush();
					byte[] objectBytes = bos.toByteArray();
					String insertString = "INSERT INTO gameStateStore VALUES (?, ?)";
						preparedStatement = connection.prepareStatement(insertString);
						preparedStatement.setString(1, gameState.getName());
						preparedStatement.setBytes(2, objectBytes);
					preparedStatement.execute();
				
				} 
				catch (IOException | SQLException e) {
					try {
						messageToClient = new DataOutputStream(socket.getOutputStream());
						messageToClient.writeChars("Error - Game not saved");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
			
	    }
	    
	    public ArrayList<GameState> queryGameStore() {
	    	ArrayList<GameState> storedGame = new ArrayList<>();
	    	PreparedStatement preparedStatement;
			Connection connection = null;
	    	
	    	try {
				connection = DriverManager.getConnection("jdbc:sqlite:minesweeper.db");
				String queryString = "SELECT * FROM gameStateStore";
				preparedStatement = connection.prepareStatement(queryString);
				ResultSet rs = preparedStatement.executeQuery();
				
				while (rs.next()) {
					byte[] buf = rs.getBytes(2);
					ObjectInputStream objectIn = null;
					if (buf != null)
						objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
					
					Object object = objectIn.readObject();
					GameState gameState = (GameState) object;
					storedGame.add(gameState);
				}
				rs.close();
			} 
	    	catch (ClassNotFoundException | SQLException | IOException e) {
				e.printStackTrace();
			}	
			return storedGame;
	    }
	    
	    public void retainTopFive(GameWonScore game) {
	    	PreparedStatement preparedStatement;
			Connection connection = null;
			if (game != null) {
				int score = game.getScore();
				try {
					connection = DriverManager.getConnection("jdbc:sqlite:minesweeper.db");
					ArrayList<GameWonScore> topFiveGameList = queryTopFive();
					
					// check if the list has less than five games or the score of the current game is higher than the lowest top-five score
					int lowestTopFiveIndex = lowestTopFive(topFiveGameList);
					if ((lowestTopFiveIndex == - 1) || (topFiveGameList.size() < 5)) {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						outputToDatabase = new ObjectOutputStream(bos);   
						outputToDatabase.writeObject(game);
						outputToDatabase.flush();
						byte[] objectBytes = bos.toByteArray();
						String insertString = "INSERT INTO topFiveGames VALUES (?, ?)";
							preparedStatement = connection.prepareStatement(insertString);
							preparedStatement.setInt(1, game.getScore());
							preparedStatement.setBytes(2, objectBytes);
						preparedStatement.execute();
					}
					else if (score > topFiveGameList.get(lowestTopFiveIndex).getScore()) {
						// delete the game with lowest score
						int lowestScore = topFiveGameList.get(lowestTopFiveIndex).getScore();
						String deleteString = "delete from topFiveGames where score = ?";
						PreparedStatement deleteStatement = connection.prepareStatement(deleteString);
						deleteStatement.setInt(1, lowestScore);
						deleteStatement.execute();
						
						// insert the new game
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						outputToDatabase = new ObjectOutputStream(bos);   
						outputToDatabase.writeObject(game);
						outputToDatabase.flush();
						byte[] objectBytes = bos.toByteArray();
						String insertString = "INSERT INTO topFiveGames VALUES (?, ?)";
							preparedStatement = connection.prepareStatement(insertString);
							preparedStatement.setInt(1, game.getScore());
							preparedStatement.setBytes(2, objectBytes);
						preparedStatement.execute();
					}

				} 
				catch (IOException | SQLException e) {
					e.printStackTrace();
				}
			}
			
	    }	    
	    
	    public ArrayList<GameWonScore> queryTopFive() {
	    	ArrayList<GameWonScore> topFiveGame = new ArrayList<>();
	    	PreparedStatement preparedStatement;
			Connection connection = null;
	    	
	    	try {
				connection = DriverManager.getConnection("jdbc:sqlite:minesweeper.db");
				String queryString = "SELECT * FROM topFiveGames";
				preparedStatement = connection.prepareStatement(queryString);
				ResultSet rs = preparedStatement.executeQuery();
				
				while (rs.next()) {
					byte[] buf = rs.getBytes(2);
					ObjectInputStream objectIn = null;
					if (buf != null)
						objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
					
					Object object = objectIn.readObject();
					GameWonScore game = (GameWonScore) object;
					topFiveGame.add(game);
				}
				rs.close();
			} 
	    	catch (ClassNotFoundException | SQLException | IOException e) {
				e.printStackTrace();
			}	
			return topFiveGame;
	    }
	    
		private int lowestTopFive(ArrayList<GameWonScore> topFiveGameList) {
			int minScore = 0;
			int minIndex = - 1;
			if (topFiveGameList.size() > 0) {
				minScore = topFiveGameList.get(0).getScore();
				minIndex = 0;
				int currentScore = 0;
				for (int i = 1; i < topFiveGameList.size(); i++) {	
					currentScore = topFiveGameList.get(i).getScore();
					if (currentScore < minScore) {
						minScore = currentScore;
						minIndex = i;
					}
				}
			}
			return minIndex;
		}	    

	}

	
	public static void main(String[] args) {
		GameServer gameServer = new GameServer();
	}
	
	
	

}
