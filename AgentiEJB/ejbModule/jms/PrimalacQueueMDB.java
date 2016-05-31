package jms;

import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

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
	
	Logger log = Logger.getLogger("Primalac MDB");
	
	@EJB
	Database database;
	
	public void onMessage (Message msg) {
		try {
			ObjectMessage omsg = (ObjectMessage) msg;
			try {
				ACLMessage aclMessage = (ACLMessage) omsg.getObject();
				//long time = omsg.getLongProperty("sent");
				// pronadji agenta za koga je poruka
				log.info("Primio sam novu poruku za: " +  aclMessage.getReceivers()[0].getName());
				if (aclMessage.getReceivers()[0] == null) {
					log.info("Nemam kome da posaljem");
					return;
				}
				AgentInterface agent = findAgent(aclMessage.getReceivers()[0]);
				//ispisi poruku koja je stigla u que
				database.getMessages().add(aclMessage);
				if (agent == null) {
					log.info("Ne postoji trazeni agent");
				} else {
					log.info("Nasao sam trazenog agenta " + agent.getAid().getName());
					// reci agentu da obradi poruku
					agent.handleMessage(aclMessage);
				}
				//System.out.println("Received new message from Queue : " + aclMessage.toString() + ", with timestamp: " + time);
			} catch (JMSException e) {
				e.printStackTrace();
			}
	    } catch (Exception e) {
	    	e.printStackTrace ();
	    }
	}
	
	private AgentInterface findAgent(AID reciever) {
		log.info("Trazim agenta na osnovu njegovo AID-a: " + reciever.getName());
		return database.getActiveAgentByAID(reciever);
	}

}