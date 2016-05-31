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
public class Pong extends Agent {

	private static final long serialVersionUID = -9209081242711408357L;

	Logger log = Logger.getLogger("PING AGENT");
	
	@EJB
	Database database;
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		System.out.println("PONG STOPPED");
	}

	@Override
	public void handleMessage(ACLMessage poruka) {
		if(poruka.getPerformative().equals(Performative.REQUEST)) {
			log.info("Formiram poruku za Ping-a");
			//formiraj poruku za pinga
			ACLMessage aclMessage = new ACLMessage();
			aclMessage.setSender(this.getAid());
			aclMessage.setReceivers(new AID[]{poruka.getSender()});
			aclMessage.setConversationID(poruka.getConversationID());
			aclMessage.setPerformative(Performative.INFORM);
			log.info("Formirana poruka za pinga, pokusavam da je posaljem: " + aclMessage);
			// posalji poruku
			sendToPing(poruka.getSender().getHost().getAddress(), aclMessage);
			log.info("Zavrseno slanje poruke ka Pingu");
		}
	}
	
	private void sendToPing(String address, ACLMessage aclMessage) {
		if (this.getAid().getHost().getAddress().equals(aclMessage.getReceivers()[0].getHost().getAddress())) {
			log.info("Saljem poruku na JMSQueue zato sto su na istom cvoru " + aclMessage.getReceivers()[0].getHost().getAddress());
			new JMSQueue(aclMessage);
		} else {
			log.info("Saljem poruke preko REST zahteva: " + aclMessage.toString());
			ResteasyClient client = new ResteasyClientBuilder().build();
			System.out.println("http://" + address + ":8080/AgentiWeb/rest/agentskiCentar/messages");
			ResteasyWebTarget target = client.target("http://" + address + ":8080/AgentiWeb/rest/agentskiCentar/messages");
			target.request(MediaType.APPLICATION_JSON).post(Entity.entity(aclMessage, MediaType.APPLICATION_JSON));
		}
	}
}
