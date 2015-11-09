import java.io.Serializable;

public class ContainerObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private Message message;
	private WeightedMessage weightedMessage;

	public ContainerObject(Message message, WeightedMessage weightedMessage) {
		this.message = message;
		this.weightedMessage = weightedMessage;
	}

	public Message returnMessage() {
		return message;
	}

	public WeightedMessage returnWeightedMessage() {
		return weightedMessage;
	}

}