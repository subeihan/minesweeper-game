import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDatabase {

	/* create the database to store game states and the games with top-5 high scores*/
	public static void main(String[] args) {
		try {
			Connection connection = DriverManager.getConnection("jdbc:sqlite:minesweeper.db");
			if (connection != null) {
				DatabaseMetaData meta = connection.getMetaData();
				System.out.println("The driver name is " + meta.getDriverName());

			}
			
			//String dropTable = "DROP TABLE gameStateStore";
			//Statement stmt1 = connection.createStatement();
			//stmt1.execute(dropTable);
			
			/*create database to store game states*/
			String createTable1 = "CREATE TABLE IF NOT EXISTS gameStateStore (\n"
					+ " name TEXT NOT NULL,\n"
					+ " gameState BLOB NOT NULL\n"
					+ ");";
			Statement stmt2 = connection.createStatement();
			stmt2.execute(createTable1);
			
			//String dropTable = "DROP TABLE topFiveGames";
			//Statement stmt3 = connection.createStatement();
			//stmt1.execute(dropTable);
			
			/*create database to store games with top-five high scores*/
			String createTable2 = "CREATE TABLE IF NOT EXISTS topFiveGames (\n"
					+ " score INTEGER NOT NULL,\n"
					+ " game BLOB NOT NULL\n"
					+ ");";
			Statement stmt4 = connection.createStatement();
			stmt4.execute(createTable2);
			

			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
