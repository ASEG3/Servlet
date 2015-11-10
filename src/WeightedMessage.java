import java.io.Serializable;
import java.util.ArrayList;

public class WeightedMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private ArrayList<ArrayList<Double>> weightedAverage;
	private double mostExpensive;
	private double leastExpensive;

	public WeightedMessage() {
		weightedAverage = new ArrayList<ArrayList<Double>>();
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

	public void setLeastExpensive(double leastExpensive) {
		this.leastExpensive = leastExpensive;
	}

	public double getLeastExpensive() {
		return leastExpensive;
	}

	public void setMostExpensive(double mostExpensive) {
		this.mostExpensive = mostExpensive;
	}

	public double getMostExpensive() {
		return mostExpensive;
	}

}
