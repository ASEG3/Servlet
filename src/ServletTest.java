import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import org.junit.runners.MethodSorters;

import org.junit.FixMethodOrder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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

	@Before
	public void setUp() {
		servlet = new Servlet();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
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



}