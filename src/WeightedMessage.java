import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class WeightedMessage implements Serializable {

	public HashMap<String, ArrayList<Double>> weightedAverages;

	public WeightedMessage() {
		weightedAverages = new HashMap<String, ArrayList<Double>>();
	}

	public void addPostcodeWeight(String postcode, ArrayList<Double> values) {
		weightedAverages.put(postcode, values);
	}

	public ArrayList<Double> getPostcodeWeight(String postcode) {
		return weightedAverages.get(postcode);
	}

}
