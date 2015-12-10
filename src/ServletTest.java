import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.ServletException;
import org.junit.runners.MethodSorters;

import org.junit.FixMethodOrder;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


import messageUtils.Message;

/**
 * Below are a number of example Java mock tests using the Spring framework.
 * Essentially it creates a mock Servlet and simulates http requests
 * This helped to outline some unlikely, but possible issues with the Servlet
 * Such as invalid long/lat values, and null parameters
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServletTest {
	private Servlet servlet;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private DatabaseAccess database;
	private Random random;
	private String longitude;
	private String latitude;

	@Before
	public void setUp() {
		servlet = new Servlet();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		database = new DatabaseAccess();
		random = new Random();
		longitude = "51.117930736089";
		latitude = "-0.207110183901943";
	}


	@Test
	public void checkNullEntryParameter() throws ServletException, IOException {
		String value = null;
		request.addParameter("MAC", "d3:eb");
		request.addParameter("ENTRY", value);
		System.out.println("Test 1: Running checkNullEntryParameter Mock test");
		servlet.doPost(request, response);

		assertEquals(500, response.getStatus());

	}

	/**
	 * This test failed, therefore, checks for null parameters were included.
	 * As well as, a regex for checking the validity of the long/lat, which is
	 * simply just between -90 and 90, -180 and 180, for longitude and latitude respectively
	 */



	@Test
	public void checkNullLongLatParameters() throws ServletException, IOException {
		String value = null;
		request.addParameter("latitude", value);
		request.addParameter("longitude", value);
		System.out.println("Test 2: Running checkNullLongLatParameter Mock test");
		servlet.doPost(request, response);
		System.out.println(response.getStatus());
		assertEquals(500, response.getStatus());

	}

	/**
	 * This test failed, therefore, checks for null parameters were included.
	 */

	@Test
	public void checkNullMacParameter() throws ServletException, IOException {
		String value = null;
		request.addParameter("MAC", value);
		request.addParameter("ENTRY", "10,20,d0");
		System.out.println("Test 3: Running checkNullMacParameter Mock test");
		servlet.doPost(request, response);
		System.out.println(response.getStatus());
		assertEquals(500, response.getStatus());

	}

	/**
	 * This test failed, therefore, checks for null parameters were included.
	 */



	@Test
	public void checkNullPostParameter() throws ServletException, IOException {
		String value = null;
		request.addParameter("MAC", value);
		request.addParameter("ENTRY", value);
		System.out.println("Test 4: Running checkNullPostParameter Mock test");
		servlet.doPost(request, response);

		assertEquals(500, response.getStatus());

	}

	/**
	 * No issues here, regex works and database only contains valid postcodes now
	 * Therefore, there is no need to call the checkPostcode method during the applications lifecycle
	 */


	@Test
	public void checkPostcodeValidator() {
		String value = new String("BN 19RH");
		ArrayList<String> postcode = new ArrayList<String>(Arrays.asList(value));
		System.out.println("Test 5: Running checkPostcodeValidator Mock test");
		// Should return false, accepted is BN1 9RH or BN19RH
		DatabaseAccess db = new DatabaseAccess();
		assertEquals(0, db.checkPostcode(postcode).size());

	}

	/**
	 * Identified an issue with the averages calculation which resulted in divide by 0.
	 * This is now rectified with the inclusion of a simply if most = least check
	 */


	@Test
	public void checkWeightedFormulaCalculation() {

		double averagePrice = 190000;
		double leastExpensive = 190000;
		double mostExpensive = 190000;
		System.out.println("Test 6: Running checkWeightedFormulaCalculation Mock test");

		DatabaseAccess d = new DatabaseAccess();
		assertEquals(1, d.performWeightCalculation(averagePrice, leastExpensive, mostExpensive), 1);

		// Helped to discover a bug
	}


	@Test
	public void divideByZero() {
		double values = 0;
		System.out.println("Test 7: Running divideByZero Mock test");
		DatabaseAccess d = new DatabaseAccess();
		assertEquals(1, d.performWeightCalculation(values, values, values), 1);
	}

	@Test
	public void testBudgetFeatureWeighted() throws IOException, ServletException{
		//Assume there aren't many houses more than 1 milllion
		int randomBudgetLimit = random.nextInt(1000000);

		request.addParameter("longitude", longitude);
		request.addParameter("latitude", latitude);
		request.addParameter("budget", String.valueOf(randomBudgetLimit));
		servlet.doGet(request, response);
		Message message = null;
		Object container = SerializationUtils.deserialize(response.getContentAsByteArray());
		message = (Message) container; 
		ArrayList<ArrayList<Double>> weights = message.getHouse();
		for(int i = 0; i < weights.size(); i++){
			double priceSold = weights.get(i).get(3);
			assertTrue("sold for " +priceSold,priceSold <= randomBudgetLimit);
		}
	}

	@Test
	public void testMostExpensiveComparisonValue() throws ServletException, IOException{
		//Assume there aren't many houses more than 1 milllion
		int randomBudgetLimit = random.nextInt(1000000);
		request.addParameter("longitude", longitude);
		request.addParameter("latitude", latitude);
		request.addParameter("budget", String.valueOf(randomBudgetLimit));
		servlet.doGet(request, response);
		Message message = null;
		Object container = SerializationUtils.deserialize(response.getContentAsByteArray());
		message = (Message) container; 
		assertTrue(message.getMostExpensive() <= randomBudgetLimit);


	}

	@Test
	public void testRandomYearFeature() throws ServletException, IOException{

		int year = ThreadLocalRandom.current().nextInt(1995, 2015 + 1);

		//Generate long/lats in the UK

		database.setSpecificYear(year);
		database.runQueries(longitude, latitude);

		request.addParameter("longitude", longitude);
		request.addParameter("latitude", latitude);
		request.addParameter("specificYear", String.valueOf(year));
		servlet.doGet(request, response);
		Message message = null;
		Object container = SerializationUtils.deserialize(response.getContentAsByteArray());
		message = (Message) container; 	
		ArrayList<ArrayList<String>> houses = message.getHouses();
		System.out.println(message.getSizeOfWeighted());
		for(int i = 0; i < houses.size(); i++){
			String dateSold = houses.get(i).get(8);

			assertTrue(Integer.parseInt(dateSold.substring(0, 4)) == year);
		}


	}

	@Test
	public void testFlatsOnlyFeature() throws ServletException, IOException{

		request.addParameter("longitude", longitude);
		request.addParameter("latitude", latitude);
		request.addParameter("flatsOnly", "");
		servlet.doGet(request, response);
		Message message = null;
		Object container = SerializationUtils.deserialize(response.getContentAsByteArray());
		message = (Message) container; 
		ArrayList<ArrayList<String>> houses = message.getHouses();
		for(int i = 0; i < houses.size(); i++){

			assertFalse(houses.get(i).get(2).equals(""));
		}

	}

	@Test
	public void testHousesOnlyFeature() throws ServletException, IOException{
		
		request.addParameter("longitude", longitude);
		request.addParameter("latitude", latitude);
		request.addParameter("housesOnly", "");
		servlet.doGet(request, response);
		Message message = null;
        Object container = SerializationUtils.deserialize(response.getContentAsByteArray());
	    message = (Message) container; 
		ArrayList<ArrayList<String>> houses = message.getHouses();
		for(int i = 0; i < houses.size(); i++){

			assertTrue(houses.get(i).get(2).equals(""));
		}
	}

	@Test
	public void testBudgetAndFlatsOnly() throws ServletException, IOException{
		//Budget between 1 and 250000
		int randomBudgetLimit = random.nextInt(250000);
		
		request.addParameter("longitude", longitude);
		request.addParameter("latitude", latitude);
		request.addParameter("budget", String.valueOf(randomBudgetLimit));
		request.addParameter("flatsOnly", "");
		servlet.doGet(request, response);
		Message message = null;
        Object container = SerializationUtils.deserialize(response.getContentAsByteArray());
	    message = (Message) container; 
		ArrayList<ArrayList<String>> houses = message.getHouses();
		for(int i = 0; i < houses.size(); i++){

			assertFalse(houses.get(i).get(2).equals(""));
			assertFalse(Integer.parseInt(houses.get(i).get(7)) > randomBudgetLimit);
		}
	}

	@Test
	public void testBudgetAndHousesOnly() throws ServletException, IOException{
		//Budget between 1 and 500000
		int randomBudgetLimit = random.nextInt(500000);
		
		
		request.addParameter("longitude", longitude);
		request.addParameter("latitude", latitude);
		request.addParameter("budget", String.valueOf(randomBudgetLimit));
		request.addParameter("housesOnly", "");
		servlet.doGet(request, response);
		Message message = null;
        Object container = SerializationUtils.deserialize(response.getContentAsByteArray());
	    message = (Message) container; 
		ArrayList<ArrayList<String>> houses = message.getHouses();
		System.out.println(houses.get(1).get(3));
		System.out.println(houses.get(1).get(7));
		for(int i = 0; i < houses.size(); i++){

			assertTrue(houses.get(i).get(2).equals(""));
			assertTrue(Integer.parseInt(houses.get(i).get(7)) <= randomBudgetLimit);



		}
	}

	@Test
	public void testBudgetAndCurrentYear() throws ServletException, IOException{
		//Budget between 1 and 500000
		int randomBudgetLimit = random.nextInt(500000);
		request.addParameter("longitude", longitude);
		request.addParameter("latitude", latitude);
		request.addParameter("thisYearOnly", String.valueOf(randomBudgetLimit));
		request.addParameter("flatsOnly", "");
		servlet.doGet(request, response);
		Message message = null;
        Object container = SerializationUtils.deserialize(response.getContentAsByteArray());
	    message = (Message) container; 
		ArrayList<ArrayList<String>> houses = message.getHouses();
		for(int i = 0; i < houses.size(); i++){
			String value = houses.get(i).get(7).substring(0, 4);
			assertTrue(houses.get(i).get(7).substring(0, 4).equals("2015"));
			if(!value.equals("2015")){
				System.out.println(value);
			}
			assertFalse(Integer.parseInt(houses.get(i).get(7)) > randomBudgetLimit);
		}

	}

}



