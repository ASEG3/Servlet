

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter output = response.getWriter();
		String fileName = request.getParameter("MAC");
		Boolean foundFile = findFile(fileName+".txt",new File(getServletContext().getRealPath("/")));
		String date = new Date().toString();
		saveFile(fileName, request.getParameter("ENTRY"), date, foundFile);
		output.close();
		output.flush();
	}
	
	protected void saveFile(String fileName, String entry, String date, Boolean foundFile) throws ServletException, IOException{
		
		File outputFile = new File(getServletContext().getRealPath("/")
	            + fileName + ".txt");
		FileWriter fw = new FileWriter(outputFile, true);
		
		try{
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append(entry);
		sb.append(", ");
		sb.append(date);
		if(!foundFile){
		fw.write("long, lat, date");
		}
		fw.write(sb.toString());
		} finally{
		fw.close();
		}
	}
	
	
	protected Boolean findFile(String name, File file){
		File[] list = file.listFiles();
		if(list!= null){
			
			for(File f: list){
				System.out.println(f.getAbsolutePath());
				if(f.isDirectory()){
					findFile(name, f);
				} else if(name.equalsIgnoreCase(f.getName())){
					System.out.println("Found the file!");
					return true;
				}
			}
			
		} else {
			return false;
		}
		return false;
	}
}
