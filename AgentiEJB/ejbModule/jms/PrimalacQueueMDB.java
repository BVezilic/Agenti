package jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.json.JSONException;
import org.json.JSONObject;

import database.Database;
import model.ACLMessage;
import model.AID;
import model.AgentInterface;

/*
 * Prvo probati sa queue/mojQueue2. Posto u tom slucaju ovaj MDB 
 * ne "hvata" poruke iz JMSQueue aplikacije, queue se prazni preko 
 * nje. Ako i ovaj bean prima iz istog reda (mojQueue), onda
 * sama aplikacija nece ni stici da dobije poruku (MDB) ce je prvi
 * "pojesti"
 * 
 * Osim toga, probati i da JMSQueue ostane upaljena, a da se 
 * startuje jos jedna instanca iste aplikacije (dok MDB ne "hvata"
 * poruke). Videcemo da queue polako raste (sa svakim startovanjem
 * aplikacije).
 * 
 */


@MessageDriven(activationConfig =
{
  @ActivationConfigProperty(propertyName="destinationType",
    propertyValue="javax.jms.Queue"),
  @ActivationConfigProperty(propertyName="destination",
    propertyValue="java:jboss/exported/jms/queue/mojQueue")
})

public class PrimalacQueueMDB implements MessageListener {
	
	@EJB
	Database database;
	
	public void onMessage (Message msg) {
		try {
			ObjectMessage omsg = (ObjectMessage) msg;
			try {
				ACLMessage aclMessage = (ACLMessage) omsg.getObject();
				long time = omsg.getLongProperty("sent");
				// pronadji agenta za koga je poruka
				AgentInterface agent = findAgent(aclMessage.getReceivers()[0]);
				// reci agentu da obradi poruku
				agent.handleMessage(aclMessage);
				//ispisi poruku koja je stigla u que
				database.sendMessage(formConsoleMessage(aclMessage));
				System.out.println("Received new message from Queue : " + aclMessage.getConversationID() + ", with timestamp: " + time);
			} catch (JMSException e) {
				e.printStackTrace();
			}
	    } catch (Exception e) {
	    	e.printStackTrace ();
	    }
	}
	
	private AgentInterface findAgent(AID reciever) {
		return database.getActiveAgentByAID(reciever);
	}
	
	private String formConsoleMessage(ACLMessage aclMessage) throws JSONException {
		JSONObject jsonObj = new JSONObject();
		String msg = "Message from: " + aclMessage.getSender().getName() + " to " + aclMessage.getReceivers()[0].getName();
		jsonObj.put("data", msg);
		jsonObj.put("type", "consoleLog");
		return jsonObj.toString();
	}

}