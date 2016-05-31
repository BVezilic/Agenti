package test.PingPong;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import database.Database;
import jms.JMSQueue;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.Performative;

@Remote(AgentInterface.class)
@Stateful
public class Ping extends Agent {

	private static final long serialVersionUID = -9209081242711408357L;

	Logger log = Logger.getLogger("PING AGENT");
	
	@EJB
	Database database;
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		System.out.println("PING STOPPED");
	}

	@Override
	public void handleMessage(ACLMessage poruka) {
		if(poruka.getPerformative().equals(Performative.REQUEST)) {
			log.info("Zapocinjem handle request");
			//formiraj poruku za ponga
			ACLMessage aclMessage = new ACLMessage();
			aclMessage.setSender(this.getAid());
			aclMessage.setReceivers(findPongByAddress(poruka.getContent()));
			aclMessage.setConversationID(poruka.getConversationID());
			aclMessage.setPerformative(Performative.REQUEST);
			log.info("Formirao sam poruku za Pong-a: " + aclMessage.toString());
			// posalji poruku
			sendToPong(poruka.getContent(), aclMessage);
			log.info("Zavrsio sam slanje ka Pongu");
		} else if (poruka.getPerformative().equals(Performative.INFORM)){
			log.info("Stigao mi je odgovor od Pong-a. Kraj razgovora.");
		}
	}
	
	private void sendToPong(String address, ACLMessage aclMessage) {
		if(this.getAid().getHost().getAddress().equals(aclMessage.getReceivers()[0].getHost().getAddress())) {
			log.info("Saljem poruku za Ponga preko queue: " + aclMessage.toString() );
			new JMSQueue(aclMessage);
		} else {
			log.info("Saljem poruku za Ponga preko resta na adresu: " + address);
			ResteasyClient client = new ResteasyClientBuilder().build();
			System.out.println("http://" + address + ":8080/AgentiWeb/rest/agentskiCentar/messages");
			ResteasyWebTarget target = client.target("http://" + address + ":8080/AgentiWeb/rest/agentskiCentar/messages");
			target.request(MediaType.APPLICATION_JSON).post(Entity.entity(aclMessage, MediaType.APPLICATION_JSON));
			log.info("Kraj rest poziva");
		}
	}
	
	private AID[] findPongByAddress(String address) {
		log.info("Trazim ponga preko adrese na kojoj se nalazi: " + address);
		AID[] receivers = new AID[1];
		for (AgentInterface ai : database.getActiveAgents()) {
			if (ai.getAid().getType().getName().equals("Pong") && ai.getAid().getHost().getAddress().equals(address)) {
				receivers[0] = ai.getAid();
				break;
			}
		}
		log.info("Nasao sam agenta: " + receivers[0].toString());
		return receivers;
	}
}
