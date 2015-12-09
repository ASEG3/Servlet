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
		//Assume no houses are more than 10 milllion
		int randomBudgetLimit = random.nextInt(100000);
		ArrayList<Integer> listValues = new ArrayList<Integer>();
		
		
		//Generate long/lats in the UK
		database.setbudget(randomBudgetLimit);
		database.setIsBudgetRequest();
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		ArrayList<ArrayList<String>> weights = message.getHouses();
		System.out.println(message.getSizeOfWeighted());
		for(int i = 0; i < weights.size(); i++){
			String postCodeValue = weights.get(i).get(3);
			System.out.println(postCodeValue);
			if(Integer.parseInt(postCodeValue) > randomBudgetLimit) System.out.println(postCodeValue);
			assertTrue(Integer.parseInt(postCodeValue) <= randomBudgetLimit);
		}
		
	}
	
	
	//Test budget with a comma
	
	@Test
	public void testBudgetFeatureComma(){
		//initially failed this test
		database.setbudget(Double.parseDouble("800,000"));
		database.setIsBudgetRequest();
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		assertTrue(message.getSizeOfNewHouses() > 0);
	}
	
	@Test
	public void testRandomYearFeature(){
		
		int year = ThreadLocalRandom.current().nextInt(1995, 2015 + 1);
		ArrayList<Integer> listValues = new ArrayList<Integer>();
		
		
		//Generate long/lats in the UK
		
		database.setSpecificYear(year);
		database.runQueries(longitude, latitude);
		Message message = database.getMessage();
		ArrayList<ArrayList<String>> weights = message.getHouses();
		System.out.println(message.getSizeOfWeighted());
		for(int i = 0; i < weights.size(); i++){
			String dateSold = weights.get(i).get(8);
		
			assertTrue(Integer.parseInt(dateSold.substring(0, 4)) == year);
		}
		
		
	}
	
	//Check to see if the 
	@Test
	public void testFlatsOnlyFeature(){
		
	}
	
	@Test
	public void testHousesOnlyFeature(){
		
	}
	
	@Test
	public void testBudgetAndFlatsOnly(){
		
	}
	
	@Test
	public void testBudgetAndHousesOnly(){
		
	}
	
	@Test
	public void testBudgetAndCurrentYear(){
		
	}

}
