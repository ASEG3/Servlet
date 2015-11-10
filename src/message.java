import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	private ArrayList<ArrayList<String>> newHouses;

	public Message() {
		newHouses = new ArrayList<ArrayList<String>>();
	}

	public void addHouseEntryNew(ArrayList<String> houseEntry) {
		newHouses.add(houseEntry);
	}

	public ArrayList<String> getHouseInfo(int i) {
		return newHouses.get(i);
	}

	public int getSize() {
		return newHouses.size();
	}

	public ArrayList<ArrayList<String>> getHouses() {
		return newHouses;
	}

}
