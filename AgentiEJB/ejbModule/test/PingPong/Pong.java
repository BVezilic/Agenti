package test.PingPong;

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

	@EJB
	Database database;
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		System.out.println("PONG STOPPED");
	}

	@Override
	public void handleMessage(ACLMessage poruka) {
		System.out.println("PONG HANDLE");
		if(poruka.getPerformative().equals(Performative.REQUEST)) {
			System.out.println("PONG REQUEST");
			//formiraj poruku za pinga
			ACLMessage aclMessage = new ACLMessage();
			aclMessage.setSender(this.getAid());
			aclMessage.setReceivers(new AID[]{poruka.getSender()});
			aclMessage.setConversationID(poruka.getConversationID());
			aclMessage.setPerformative(Performative.INFORM);
			// posalji poruku
			sendToPing(poruka.getSender().getHost().getAddress(), aclMessage);
		}
		System.out.println("PONG - STIGLA PORUKA");
	}
	
	private void sendToPing(String address, ACLMessage aclMessage) {
		if (database.isMaster()) {
			new JMSQueue(aclMessage);
		} else {
			ResteasyClient client = new ResteasyClientBuilder().build();
			System.out.println("http://" + address + ":8080/AgentiWeb/rest/agentskiCentar/messages");
			ResteasyWebTarget target = client.target("http://" + address + ":8080/AgentiWeb/rest/agentskiCentar/messages");
			target.request(MediaType.APPLICATION_JSON).post(Entity.entity(aclMessage, MediaType.APPLICATION_JSON));
		}
	}
}
