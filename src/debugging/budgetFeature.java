package debugging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class budgetFeature {
	
	public static void main(String[] args){
		//Double.parseDouble("800,000");
		String longitude = "51.117930736089";
		String latitude = "0.207110183901943";
		String regex = "^([-+]?\\d{1,2}([.]\\d+)?),\\s*([-+]?\\d{1,3}([.]\\d+)?)$";
		String longLat = longitude + ", " + latitude;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(longLat);
		System.out.println(matcher.matches());
	}

}
