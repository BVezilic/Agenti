package jms;

import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import database.Database;
import model.ACLMessage;
import model.AID;
import model.AgentInterface;
import model.AgentskiCentar;

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
				log.info("Primio sam novu poruku: " +  aclMessage);
				
				// zabelezi novu poruku u bazu
				sendRest(aclMessage);

				
				// proveri da li postoji receiveri
				if (aclMessage.getReceivers().length == 0) {
					log.info("Nemam kome da posaljem");
					return;
				}
				
				// pronadji agente za koga je poruka
				for (int i = 0; i < aclMessage.getReceivers().length; i++) {
					log.info("Proveri da li je receiver na istom agenstkom centru");
					if (database.getAgentskiCentar().getAddress().equals(aclMessage.getReceivers()[i].getHost().getAddress())) {
						log.info("Trazim agenta na osnovu njegovo AID-a: " + aclMessage.getReceivers()[i].getName());
						AgentInterface agent =  database.getActiveAgentByAID(aclMessage.getReceivers()[i]);	
						if (agent == null) {
							log.info("Ne postoji trazeni agent");
						} else {
							log.info("Nasao sam trazenog agenta " + agent.getAid().getName());
							agent.handleMessage(aclMessage);
						}
					} else {
						log.info("Formiraj novu poruku kako bih kontaktirao agentski centar na kome se nalazi taj agenat");
						ACLMessage aclMsg = new ACLMessage();
						aclMsg.setSender(aclMessage.getSender());
						aclMsg.setReceivers(new AID[]{aclMessage.getReceivers()[i]});
						aclMsg.setContent(aclMessage.getContent());
						aclMsg.setContentObj(aclMessage.getContentObj());
						aclMsg.setConversationID(aclMessage.getConversationID());
						aclMsg.setPerformative(aclMessage.getPerformative());
						aclMsg.setProtocol(aclMessage.getProtocol());
						aclMsg.setEncoding(aclMessage.getEncoding());
						aclMsg.setReplyTo(aclMessage.getReplyTo());
						aclMsg.setUserArgs(aclMessage.getUserArgs());
						log.info("Posalji novi poruku na receiverovo agentski centar da obradi");
						ResteasyClient client = new ResteasyClientBuilder().build();
						System.out.println("http://" + aclMessage.getReceivers()[i].getHost().getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/messages");
						ResteasyWebTarget target = client.target("http://" + aclMessage.getReceivers()[i].getHost().getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/messages");
						target.request(MediaType.APPLICATION_JSON).post(Entity.entity(aclMsg, MediaType.APPLICATION_JSON));
						log.info("Kraj rest poziva");
					}
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
	    } catch (Exception e) {
	    	e.printStackTrace ();
	    }
	}
	
	private void sendRest(ACLMessage aclMessage){
		try {
			for (AgentskiCentar ac : database.getAgentskiCentri()) {
				if (database.getAgentskiCentar().getAlias().equals(ac.getAlias())){
					database.getMessages().add(aclMessage);
					database.sendMessageToSocket();
				} else {
					if (aclMessage.getSender() != null){
						AgentInterface ai = database.getActiveAgentByAID(aclMessage.getSender());
						if (!ai.getAid().getHost().getAlias().equals(ac.getAlias())){
							ResteasyClient client = new ResteasyClientBuilder().build();
							ResteasyWebTarget target = client.target("http://" + ac.getAlias() + ":8080/AgentiWeb/rest/agentskiCentar/messages");
							target.request(MediaType.APPLICATION_JSON).put(Entity.entity(aclMessage, MediaType.APPLICATION_JSON));
						}
					} else {
						ResteasyClient client = new ResteasyClientBuilder().build();
						ResteasyWebTarget target = client.target("http://" + ac.getAlias() + ":8080/AgentiWeb/rest/agentskiCentar/messages");
						target.request(MediaType.APPLICATION_JSON).put(Entity.entity(aclMessage, MediaType.APPLICATION_JSON));
					}
					
					
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}