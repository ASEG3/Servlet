import java.io.Serializable;
import java.util.HashMap;


public class message implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashMap<String, subMessage> houses;
	
	
	
	public message(){
		houses = new HashMap<String, subMessage>();
	}
	
	
	public void addSubMessage(String postcode, subMessage m){
		houses.put(postcode, m);
	}
	
	public subMessage getSubMessage(String postcode){
		return houses.get(postcode);
		
	}
	

}
