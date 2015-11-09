import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseAccess {

	final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	final String DB_URL = "jdbc:mysql://52.33.206.187/ase";
	final String USER = "admin";
	final String PASS = "g3mjhmts";
	Message message;
	WeightedMessage weightedMessage;

	public DatabaseAccess() {
		message = new Message();
		weightedMessage = new WeightedMessage();

	}

	public void runQueries(String longitude, String latitude) {
		PreparedStatement cs = null;
		Connection conn = null;
		ResultSet rs = null;

		try {
			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// Open a connection
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			// cs.setEscapeProcessing(true);
			// cs.setQueryTimeout(120);
			String SQL = "CALL getSurroundingProperties(" + longitude + ", " + latitude + ", 1)";
			cs = conn.prepareStatement(SQL);

			rs = cs.executeQuery();
			// System.out.println(rs.toString());
			while (rs.next()) {

				ArrayList<String> houseInformation = new ArrayList<String>();
				houseInformation.add(rs.getString(1));
				houseInformation.add(rs.getString(2));
				houseInformation.add(rs.getString(3));
				houseInformation.add(rs.getString(4));
				houseInformation.add(rs.getString(5));
				houseInformation.add(rs.getString(6));
				houseInformation.add(rs.getString(7));
				houseInformation.add(rs.getString(8));
				houseInformation.add(rs.getString(9));
				houseInformation.add(rs.getString(10));
				message.addHouseEntryNew(houseInformation);

			}

			rs.close();
			cs.close();
			SQL = "CALL getWeightedLatLong(" + longitude + ", " + latitude + ", 1)";
			cs = conn.prepareStatement(SQL);

			rs = cs.executeQuery();
			// System.out.println(rs.toString());

			double mostExpensive = 0;
			double leastExpensive = 0;
			// System.out.println("Entering the loop");

			if (rs.last()) {
				int rows = rs.getRow();
				// System.out.println(rows);
				leastExpensive = rs.getDouble(4);
				rs.beforeFirst();

			}

			while (rs.next()) {

				// System.out.println("Inside the loop");
				if (rs.getRow() == 1) {
					mostExpensive = rs.getDouble(4);
				}

				String houseID = rs.getString(1);
				ArrayList<Double> houseValues = new ArrayList<Double>();
				houseValues.add(rs.getDouble(2));
				houseValues.add(rs.getDouble(3));
				double average = rs.getDouble(4);
				double weightedAverage = performWeightCalculation(average, leastExpensive, mostExpensive);
				houseValues.add(weightedAverage);
				weightedMessage.addWeight(houseValues);

			}

			weightedMessage.setLeastExpensive(leastExpensive);
			weightedMessage.setMostExpensive(mostExpensive);

			rs.close();
			cs.close();
			conn.close();

			cs.close();
			conn.close();
		} catch (SQLException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			try {
				if (cs != null)
					cs.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try

	}

	public Message getMessage() {
		return message;
	}

	public WeightedMessage getWeightedMessage() {
		return weightedMessage;
	}

	public double performWeightCalculation(double ap, double le, double me) {
		return (ap - le) / (me - le);
	}

	public ArrayList<String> checkPostcode(ArrayList<String> postcodes) {
		ArrayList<String> verifiedPostcodes = new ArrayList<String>();
		String regex = "^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][ABD-HJLNP-UW-Z]{2}$";

		Pattern pattern = Pattern.compile(regex);

		for (String postcode : postcodes) {
			Matcher matcher = pattern.matcher(postcode);
			if (matcher.matches()) {
				verifiedPostcodes.add(postcode);
			}
		}

		return verifiedPostcodes;
	}

}
