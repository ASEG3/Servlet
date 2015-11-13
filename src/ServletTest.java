import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
	public void checkNullLongLatParameters() throws ServletException, IOException {
		String value = null;
		request.addParameter("latitude", value);
		request.addParameter("longitude", value);

		// servlet.doPost(request, response);

		// assertEquals(500, response.getStatus());

	}

	@Test
	public void checkNullMacParameter() throws ServletException, IOException {
		String value = null;
		request.addParameter("MAC", value);
		request.addParameter("ENTRY", "10,20,d0");

		servlet.doPost(request, response);

		assertEquals(500, response.getStatus());

	}

	@Test
	public void checkNullEntryParameter() throws ServletException, IOException {
		String value = null;
		request.addParameter("MAC", "d3:eb");
		request.addParameter("ENTRY", value);

		servlet.doPost(request, response);

		assertEquals(500, response.getStatus());

	}

	@Test
	public void checkNullPostParameter() throws ServletException, IOException {
		String value = null;
		request.addParameter("MAC", value);
		request.addParameter("ENTRY", value);

		servlet.doPost(request, response);

		assertEquals(500, response.getStatus());

	}

}