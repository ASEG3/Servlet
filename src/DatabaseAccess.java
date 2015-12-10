import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import messageUtils.Message;

public class DatabaseAccess {

	private final String DB_URL = "jdbc:mysql://52.35.222.167/ase";
	private final String USER = "admin";
	private final String PASS = "g3mjhmts"; // Not good practice, I know
	private Message message;
	private double budget;
	private boolean isBudgetRequest;
	private boolean isHouseOnlyRequest;
	private boolean isFlatOnlyRequest;
	private HashMap<String, ArrayList<Integer>> placesToRemove = new HashMap<String, ArrayList<Integer>>();
	private boolean thisYearOnly;
	private int specificYear;
	Calendar calendar = new GregorianCalendar();
	
	public DatabaseAccess() {
		message = new Message();
		System.out.println("Database initialized");

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
	
	private void incrementFilterValue(String postcode){
		ArrayList<Integer> values = placesToRemove.get(postcode);
		values.add(1);
	}
	
	private void incrementPostcodesValue(String postcode){
		ArrayList<Integer> values = placesToRemove.get(postcode);
		if(values !=null){
		Integer i = values.remove(0);
		i++;
		values.add(0, i);
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
			cs.setFetchSize(100);
			rs = cs.executeQuery();
			System.out.println("Executing query");
			
			while (rs.next()) {
				
				
				String postcode = rs.getString(7);
				
				if(placesToRemove.containsKey(postcode)){
					incrementPostcodesValue(postcode);
				} else {
					ArrayList<Integer> values = new ArrayList<Integer>();
					values.add(1);
					placesToRemove.put(postcode, values);			
				}
				
				if(isBudgetRequest && rs.getDouble(8) > budget){
					continue;
				} else if(isHouseOnlyRequest && !rs.getString(3).equals("")) {
					incrementFilterValue(postcode);
					continue;
				} else if(isFlatOnlyRequest && rs.getString(3).equals("")){
					incrementFilterValue(postcode);
					continue;
				} else if(thisYearOnly){
					int dateSold = Integer.parseInt(rs.getString(9).substring(0,4));
					int currentYear = calendar.get(Calendar.YEAR);
					if(dateSold != currentYear){
						System.out.println("adding it");
						incrementFilterValue(postcode);
						continue;
					} 
				} else if(specificYear != 0){
					int dateSold = Integer.parseInt(rs.getString(9).substring(0,4));
					if(dateSold != specificYear){
						incrementFilterValue(postcode);
						continue;
					}
				}


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

			System.out.println(message.getSizeOfNewHouses());

			rs.close();
			cs.close();

			SQL = "CALL getWeightedLatLong(" + longitude + ", " + latitude + ", 3)";
			cs = conn.prepareStatement(SQL);
			cs.setFetchSize(100);
			rs = cs.executeQuery();

			double mostExpensive = 0;
			double leastExpensive = 0;

			if (rs.last()) {
				leastExpensive = rs.getDouble(4);
				rs.beforeFirst();

			}

			while (rs.next()) {

				if (rs.getRow() == 1 && !isBudgetRequest) {
					mostExpensive = rs.getDouble(4);
				} else {
					if(mostExpensive == 0 && rs.getDouble(4) <= budget){
						mostExpensive = rs.getDouble(4);
					}
				}
				
				String postcode = rs.getString(1);
				
				if(isBudgetRequest && rs.getDouble(4) > budget){
					continue;
				}else if (placesToRemove.containsKey(postcode)){
					Integer i = placesToRemove.get(postcode).remove(0)/2;
					if(placesToRemove.get(postcode).size()-1 >  i){
					continue;
					}
				}
				
				

				ArrayList<Double> houseValues = new ArrayList<Double>();
				houseValues.add(rs.getDouble(2));
				houseValues.add(rs.getDouble(3));
				double average = rs.getDouble(4);
				double weightedAverage = performWeightCalculation(average, leastExpensive, mostExpensive);
				houseValues.add(weightedAverage);
				houseValues.add(average);
				message.addWeight(houseValues);
							

			}
			
			System.out.println(message.getSizeOfWeighted());

			message.setLeastExpensive(leastExpensive);
			message.setMostExpensive(mostExpensive);

			rs.close();
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
			String SQL = "CALL getSurroundingProperties(" + longitude + ", " + latitude + ", 3)";
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
					System.out.println("here");
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
