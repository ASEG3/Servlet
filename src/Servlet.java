
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SerializationUtils;
import org.json.*;

/**
 * Servlet implementation class Servlet
 */
@WebServlet("/Servlet")
public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public Servlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		OutputStream outputStream = response.getOutputStream();
		Enumeration<String> parameterNames = request.getParameterNames();
		ArrayList<String> postcodes = checkPostcode(new ArrayList<String>());
		final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		final String DB_URL = "jdbc:mysql://localhost/location";
		// Need to change to the proper amazon DB URL ^

		final String USER = "root";
		final String PASS = "password";

		while (parameterNames.hasMoreElements()) {
			String paramName = parameterNames.nextElement();
			postcodes.add(paramName);
		}
		postcodes = checkPostcode(postcodes);

		try {
			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// Open a connection
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// Execute SQL query
			Statement stmt = conn.createStatement();
			String sql;
			sql = "SELECT id, first, last, age FROM Employees";
			ResultSet rs = stmt.executeQuery(sql);

			// Extract data from result set
			while (rs.next()) {

			}

			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		} // end try

		subMessage m = new subMessage(5, new String[5][5]);
		message message = new message();
		message.addSubMessage("bn19rh", m);

		outputStream.write(fromJavaToByteArray(message));
		outputStream.close();
		outputStream.flush();

	}

	public static byte[] fromJavaToByteArray(Serializable object) {
		return SerializationUtils.serialize(object);
	}

	public int performWeightCalculation(int x) {
		return 1 - (1 / x);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		hasConnected();
		PrintWriter output = response.getWriter();
		System.out.println(request.getParameter("MAC"));
		String fileName = request.getParameter("MAC");
		Boolean foundFile = findFile(fileName + ".txt", new File(
				getServletContext().getRealPath("/")));
		saveFile(fileName, request.getParameter("ENTRY"),
				new Date().toString(), foundFile);
		output.write("Message received!");
		output.close();
		output.flush();
	}

	protected void saveFile(String fileName, String entry, String date,
			Boolean foundFile) throws ServletException, IOException {

		File outputFile = new File(getServletContext().getRealPath("/")
				+ fileName + ".txt");
		FileWriter fw = new FileWriter(outputFile, true);
		System.out.println(entry);
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("\n");
			sb.append(entry);
			System.out.println(entry);
			sb.append(", ");
			sb.append(date);
			if (!foundFile) {
				fw.write("long, lat, date");
			}
			fw.write(sb.toString());
		} finally {
			fw.close();
		}
	}

	protected Boolean findFile(String name, File file) {
		File[] list = file.listFiles();
		if (list != null) {

			for (File f : list) {
				System.out.println(f.getAbsolutePath());
				if (f.isDirectory()) {
					findFile(name, f);
				} else if (name.equalsIgnoreCase(f.getName())) {
					System.out.println("Found the file!");
					return true;
				}
			}

		} else {
			return false;
		}
		return false;
	}

	protected void hasConnected() throws IOException {

		File outputFile = new File(getServletContext().getRealPath("/")
				+ "connections.txt");
		FileWriter fw = new FileWriter(outputFile, true);

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("Someone has connected at: ");
			sb.append(new Date().toString());
			fw.write(sb.toString());
		} finally {
			fw.close();
		}

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
