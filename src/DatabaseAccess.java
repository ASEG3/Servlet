import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import messageUtils.Message;

public class DatabaseAccess {

	private final String DB_URL = "jdbc:mysql://52.33.174.180/ase";
	private final String USER = "admin";
	private final String PASS = "g3mjhmts"; // Not good practice, I know
	private Message message;
	private double budget;
	private boolean isBudgetRequest;
	private boolean isHouseOnlyRequest;
	private boolean isFlatOnlyRequest;
	private HashMap<String, ArrayList<Double>> placesToRemove = new HashMap<String, ArrayList<Double>>();
	private boolean thisYearOnly;
	private int specificYear;
	
	public DatabaseAccess() {
		message = new Message();

	}
	
	public void setbudget(double budget){
		this.budget = budget;
	}
	
	public void setIsBudgetRequest(){
		isBudgetRequest = true;
	}
	
	public void setIsHouseOnlyRequest(){
		isHouseOnlyRequest = true;
	}
	
	public void setIsFlatOnlyRequest(){
		isFlatOnlyRequest = true;
	}
	
	public void setThisYearOnly(){
		thisYearOnly = true;
	}
	
	public void setSpecificYear(final int year){
		specificYear = year;
	}
	
	private void addToArrayList(String postcode, double distance){
		ArrayList<Double> values = placesToRemove.get(postcode);
		if(values !=null){
		values.add(distance);
		} else{
			values = new ArrayList<Double>();
			values.add(distance);
		}
		placesToRemove.put(postcode, values);
	}
	
	

	public void runQueries(String longitude, String latitude) {
		PreparedStatement cs = null;
		Connection conn = null;
		ResultSet rs = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			// cs.setEscapeProcessing(true);
			// cs.setQueryTimeout(120);
			String SQL = "CALL getSurroundingProperties(" + longitude + ", " + latitude + ", 3)";
			cs = conn.prepareStatement(SQL);
			rs = cs.executeQuery();
			while (rs.next()) {
				
				String postcode = rs.getString(7);
				double distance = rs.getDouble(10);
				
				if(isBudgetRequest && rs.getDouble(8) > budget){
					continue;
				} else if(isHouseOnlyRequest && !rs.getString(3).equals("")) {
					addToArrayList(postcode, distance);
					continue;
				} else if(isFlatOnlyRequest && rs.getString(3).equals("")){
					addToArrayList(postcode, distance);
					continue;
				} else if(thisYearOnly){
					int dateSold = Integer.parseInt(rs.getString(9).substring(0,3));
					int currentYear = Year.now().getValue();
					if(dateSold != currentYear){
						addToArrayList(postcode, distance);
					}					
				} else if(specificYear != 0){
					int dateSold = Integer.parseInt(rs.getString(9).substring(0,3));
					if(dateSold != specificYear){
						addToArrayList(postcode, distance);
					}
				}
				else{
					
					

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

			}

			rs.close();
			cs.close();

			SQL = "CALL getWeightedLatLong(" + longitude + ", " + latitude + ", 3)";
			cs = conn.prepareStatement(SQL);
			rs = cs.executeQuery();

			double mostExpensive = 0;
			double leastExpensive = 0;

			if (rs.last()) {
				leastExpensive = rs.getDouble(4);
				rs.beforeFirst();

			}

			while (rs.next()) {

				if (rs.getRow() == 1) {
					mostExpensive = rs.getDouble(4);
				}
				
				if(isBudgetRequest && rs.getDouble(4) > budget){
					continue;
				} else if(isHouseOnlyRequest || isFlatOnlyRequest || thisYearOnly || specificYear != 0){
					ArrayList<Double> values = placesToRemove.get(rs.getString(1));
					if(values.contains(rs.getString(5))) continue;
				} 
				else{
				
				ArrayList<Double> houseValues = new ArrayList<Double>();
				houseValues.add(rs.getDouble(2));
				houseValues.add(rs.getDouble(3));
				double average = rs.getDouble(4);
				double weightedAverage = performWeightCalculation(average, leastExpensive, mostExpensive);
				houseValues.add(weightedAverage);
				houseValues.add(average);
				message.addWeight(houseValues);
				}			

			}

			message.setLeastExpensive(leastExpensive);
			message.setMostExpensive(mostExpensive);

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
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}

	}

	public Message getMessage() {
		return message;
	}

	protected double performWeightCalculation(double ap, double le, double me) {
		if (le == me) {
			// If we have just one entry, or multiple entries with equal price
			return 1;
		} else {
			return (ap - le) / (me - le);
		}
	}
	
	public void getMostAndLeastExpensivePostCodes(String longitude, String latitude){
		PreparedStatement cs = null;
		Connection conn = null;
		ResultSet rs = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			// cs.setEscapeProcessing(true);
			// cs.setQueryTimeout(120);
			String SQL = "CALL getSurroundingProperties(" + longitude + ", " + latitude+")";
			cs = conn.prepareStatement(SQL);
			rs = cs.executeQuery();
			while (rs.next()) {
				int size = message.getSizeOfNewHouses();
				
				
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
				
				if(size == 10){
					rs.last();
				} else if(size > 10 & size < 20){
					rs.previous();
				} else {
					break;
				}

			//Add code
			}

			rs.close();
			cs.close();
		} catch (SQLException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			try {
				if (cs != null)
					cs.close();
			} catch (SQLException se2) {
			}
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

	protected ArrayList<String> checkPostcode(ArrayList<String> postcodes) {
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
