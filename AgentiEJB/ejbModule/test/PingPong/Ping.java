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
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.Performative;

@Remote(AgentInterface.class)
@Stateful
public class Ping extends Agent {

	private static final long serialVersionUID = -9209081242711408357L;

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
			//formiraj poruku za ponga
			ACLMessage aclMessage = new ACLMessage();
			aclMessage.setSender(this.getAid());
			aclMessage.setReceivers(findPongByAddress(poruka.getContent()));
			aclMessage.setConversationID(poruka.getConversationID());
			aclMessage.setPerformative(Performative.REQUEST);
			// posalji poruku
			sendToPong(poruka.getContent(), aclMessage);
		} else if (poruka.getPerformative().equals(Performative.INFORM)){
			
		}
		System.out.println("PING - STIGLA PORUKA");
	}
	
	private void sendToPong(String address, ACLMessage aclMessage) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		System.out.println("http://" + address + ":8080/AgentiWeb/rest/agentskiCentar/messages");
		ResteasyWebTarget target = client.target("http://" + address + ":8080/AgentiWeb/rest/agentskiCentar/messages");
		target.request(MediaType.APPLICATION_JSON).post(Entity.entity(aclMessage, MediaType.APPLICATION_JSON));
	}
	
	private AID[] findPongByAddress(String address) {
		AID[] receivers = new AID[1];
		for (AgentInterface ai : database.getActiveAgents()) {
			if (ai.getAid().getType().getName().equals("Pong") && ai.getAid().getHost().getAddress().equals(address)) {
				receivers[0] = ai.getAid();
				break;
			}
		}
		return receivers;
	}
}
