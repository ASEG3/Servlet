import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {

	public static final long serialVersionUID = 1L;
	public HashMap<String, ArrayList<String>> houses;

	public Message() {
		houses = new HashMap<String, ArrayList<String>>();
	}

	public void addHouseEntry(String houseID, ArrayList<String> houseInformation) {
		houses.put(houseID, houseInformation);
	}

	public ArrayList<String> getHouseInformation(String houseID) {
		return houses.get(houseID);

	}

}
