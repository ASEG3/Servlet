import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class WeightedMessage implements Serializable {

	public static final long serialVersionUID = 1L;
	public HashMap<String, ArrayList<Double>> weightedAverages;
	public ArrayList<ArrayList<Double>> weightedAverage;

	public WeightedMessage() {
		weightedAverages = new HashMap<String, ArrayList<Double>>();
		weightedAverage = new ArrayList<ArrayList<Double>>();
	}

	public void addPostcodeWeight(String postcode, ArrayList<Double> values) {
		weightedAverages.put(postcode, values);
	}

	public ArrayList<Double> getPostcodeWeight(String postcode) {
		return weightedAverages.get(postcode);
	}

	public ArrayList<Double> getHouse(int i) {
		return weightedAverage.get(i);
	}

	public void addWeight(ArrayList<Double> houseWeights) {
		weightedAverage.add(houseWeights);
	}

	public int getSize() {
		return weightedAverage.size();
	}

	public ArrayList<ArrayList<Double>> getHouse() {
		return weightedAverage;
	}

}
