import static org.junit.Assert.*;

import java.awt.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Test;

import messageUtils.Message;

public class DatabaseAccessUnitTest {

	private DatabaseAccess database;
	private Random random;
	private String longitude;
	private String latitude;

	/**
	 * Below are a number of pseudo random tests, all values are generated 
	 * from random excluding long/lat (within a range of course)
	 */

	public DatabaseAccessUnitTest(){

	}

	@Before
	public void setup(){
		database = new DatabaseAccess();
		random = new Random();
		longitude = "51.117930736089";
		latitude = "-0.207110183901943";
	}

	@Test
	public void testBudgetFeatureWeighted(){
		//Assume there aren't many houses more than 1 milllion
		int randomBudgetLimit = random.nextInt(1000000);
		database.setbudget(randomBudgetLimit);
		database.setIsBudgetRequest();
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		ArrayList<ArrayList<Double>> weights = message.getHouse();
		System.out.println("weighted is of size: " + message.getSizeOfWeighted());
		System.out.println(weights.get(1).get(3));
		for(int i = 0; i < weights.size(); i++){
			double priceSold = weights.get(i).get(3);
			System.out.println(priceSold);
			assertTrue("sold for " +priceSold,priceSold <= randomBudgetLimit);
		}

	}
	
	@Test
	public void testMostExpensiveComparisonValue(){
		//Assume there aren't many houses more than 1 milllion
		int randomBudgetLimit = random.nextInt(1000000);
		database.setbudget(randomBudgetLimit);
		database.setIsBudgetRequest();
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		
		assertTrue(message.getMostExpensive() <= randomBudgetLimit);
		

	}


	//Test budget with a comma
/*  Handled by the Servlet, therefore this test would always fail
	@Test
	public void testBudgetFeatureComma(){
		//initially failed this test
		database.setbudget(Double.parseDouble("800,000"));
		database.setIsBudgetRequest();
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		assertTrue(message.getSizeOfNewHouses() > 0);
	}*/

	@Test
	public void testRandomYearFeature(){

		int year = ThreadLocalRandom.current().nextInt(1995, 2015 + 1);

		//Generate long/lats in the UK

		database.setSpecificYear(year);
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		ArrayList<ArrayList<String>> houses = message.getHouses();
		System.out.println(message.getSizeOfWeighted());
		for(int i = 0; i < houses.size(); i++){
			String dateSold = houses.get(i).get(8);

			assertTrue(Integer.parseInt(dateSold.substring(0, 4)) == year);
		}


	}


	@Test
	public void testFlatsOnlyFeature(){

		//Generate long/lats in the UK

		database.setIsFlatOnlyRequest();
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		ArrayList<ArrayList<String>> houses = message.getHouses();
		for(int i = 0; i < houses.size(); i++){

			assertFalse(houses.get(i).get(2).equals(""));
		}

	}

	@Test
	public void testHousesOnlyFeature(){

		database.setIsHouseOnlyRequest();
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		ArrayList<ArrayList<String>> houses = message.getHouses();
		for(int i = 0; i < houses.size(); i++){

			assertTrue(houses.get(i).get(2).equals(""));
		}
	}

	@Test
	public void testBudgetAndFlatsOnly(){
		//Budget between 1 and 250000
		int randomBudgetLimit = random.nextInt(250000);
		database.setIsFlatOnlyRequest();
		database.setIsBudgetRequest();
		database.setbudget(randomBudgetLimit);
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		ArrayList<ArrayList<String>> houses = message.getHouses();
		for(int i = 0; i < houses.size(); i++){

			assertFalse(houses.get(i).get(2).equals(""));
			assertFalse(Integer.parseInt(houses.get(i).get(7)) > randomBudgetLimit);
		}
	}

	@Test
	public void testBudgetAndHousesOnly(){
		//Budget between 1 and 500000
		int randomBudgetLimit = random.nextInt(500000);
		database.setIsHouseOnlyRequest();
		database.setIsBudgetRequest();
		database.setbudget(randomBudgetLimit);
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		ArrayList<ArrayList<String>> houses = message.getHouses();
		System.out.println(houses.get(1).get(3));
		System.out.println(houses.get(1).get(7));
		for(int i = 0; i < houses.size(); i++){

			assertTrue(houses.get(i).get(2).equals(""));
			assertTrue(Integer.parseInt(houses.get(i).get(7)) <= randomBudgetLimit);
			
			
			
		}
	}

	@Test
	public void testBudgetAndCurrentYear(){
		//Budget between 1 and 500000
		int randomBudgetLimit = random.nextInt(500000);
		database.setThisYearOnly();
		//database.setIsBudgetRequest();
		//database.setbudget(randomBudgetLimit);
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		ArrayList<ArrayList<String>> houses = message.getHouses();
		for(int i = 0; i < houses.size(); i++){

			assertTrue(houses.get(i).get(8).substring(0, 4).equals("2015"));
			assertFalse(Integer.parseInt(houses.get(i).get(7)) > randomBudgetLimit);
		}

	}

}
