import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SerializationUtils;

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
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		OutputStream outputStream = response.getOutputStream();
		// Enumeration<String> parameterNames = request.getParameterNames();
		// ArrayList<String> postcodes = checkPostcode(new ArrayList<String>());
		String longitude = request.getParameter("longitude");
		String latitude = request.getParameter("latitude");
		Message m = createMessage(longitude, latitude);
		WeightedMessage wm = createWeightedMessage(longitude, latitude);
		outputStream.write(fromJavaToByteArray(m));
		outputStream.write(fromJavaToByteArray(wm));
		outputStream.close();
		outputStream.flush();

	}

	public Message createMessage(String longitude, String latitude) {
		final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		final String DB_URL = "jdbc:mysql://localhost/ase";
		// Need to change to the proper amazon DB URL ^

		final String USER = "admin@localhost";
		final String PASS = "";

		// postcodes = checkPostcode(postcodes);
		PreparedStatement cs = null;
		Connection conn = null;
		ResultSet rs = null;
		Message m = new Message();

		try {
			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// Open a connection
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			cs = conn.prepareStatement("exec getSurroundingProperties");
			cs.setEscapeProcessing(true);
			cs.setQueryTimeout(120);

			rs = cs.executeQuery();

			while (rs.next()) {

				String houseID = rs.getString(1);
				ArrayList<String> houseInformation = new ArrayList<String>();
				houseInformation.add(rs.getString(2));
				houseInformation.add(rs.getString(3));
				houseInformation.add(rs.getString(4));
				houseInformation.add(rs.getString(5));
				houseInformation.add(rs.getString(6));
				houseInformation.add(rs.getString(7));
				houseInformation.add(rs.getString(8));
				houseInformation.add(rs.getString(9));
				houseInformation.add(rs.getString(10));
				m.addHouseEntry(houseID, houseInformation);

			}

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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try

		return m;

	}

	public WeightedMessage createWeightedMessage(String longitude, String latitude) {

		final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		final String DB_URL = "jdbc:mysql://localhost/ase";
		// Need to change to the proper amazon DB URL ^

		final String USER = "admin@localhost";
		final String PASS = "g3mjhmts";

		// postcodes = checkPostcode(postcodes);
		PreparedStatement cs = null;
		Connection conn = null;
		ResultSet rs = null;
		WeightedMessage wm = new WeightedMessage();

		try {
			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// Open a connection
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			cs = conn.prepareStatement("exec getWeightedLatLong ?,?,?");
			cs.setEscapeProcessing(true);
			cs.setQueryTimeout(120);

			rs = cs.executeQuery();
			rs.first();
			double mostExpensive = rs.getDouble(4);
			rs.last();
			double leastExpensive = rs.getDouble(4);

			while (rs.next()) {

				String houseID = rs.getString(1);
				ArrayList<Double> houseValues = new ArrayList<Double>();
				houseValues.add(rs.getDouble(2));
				houseValues.add(rs.getDouble(3));
				double average = rs.getDouble(4);
				double weightedAverage = performWeightCalculation(average, leastExpensive, mostExpensive);
				houseValues.add(weightedAverage);
				wm.addPostcodeWeight(houseID, houseValues);

			}

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
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try

		return wm;

	}

	public static byte[] fromJavaToByteArray(Serializable object) {
		return SerializationUtils.serialize(object);
	}

	public double performWeightCalculation(double ap, double le, double me) {
		return (ap - le) / (me - le);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		hasConnected();
		PrintWriter output = response.getWriter();
		System.out.println(request.getParameter("MAC"));
		String fileName = request.getParameter("MAC");
		Boolean foundFile = findFile(fileName + ".txt", new File(getServletContext().getRealPath("/")));
		saveFile(fileName, request.getParameter("ENTRY"), new Date().toString(), foundFile);
		output.write("Message received!");
		output.close();
		output.flush();
	}

	protected void saveFile(String fileName, String entry, String date, Boolean foundFile)
			throws ServletException, IOException {

		File outputFile = new File(getServletContext().getRealPath("/") + fileName + ".txt");
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

		File outputFile = new File(getServletContext().getRealPath("/") + "connections.txt");
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
