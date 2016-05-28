package test.PingPong;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.Performative;

@Remote(AgentInterface.class)
@Stateful
public class Pong extends Agent {

	private static final long serialVersionUID = -9209081242711408357L;

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		System.out.println("PONG STOPPED");
	}

	@Override
	public void handleMessage(ACLMessage poruka) {
		if(poruka.getPerformative().equals(Performative.REQUEST)) {
			//formiraj poruku za pinga
			ACLMessage aclMessage = new ACLMessage();
			aclMessage.setSender(this.getAid());
			aclMessage.setReceivers(new AID[]{poruka.getSender()});
			aclMessage.setConversationID(poruka.getConversationID());
			aclMessage.setPerformative(Performative.REQUEST);
			// posalji poruku
			sendToPing(poruka.getReceivers()[0].getHost().getAddress(), poruka);
		}
		System.out.println("PONG - STIGLA PORUKA");
	}
	
	private void sendToPing(String address, ACLMessage aclMessage) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		System.out.println("http://" + address + ":8080/AgentiWeb/rest/agentskiCentar/messages");
		ResteasyWebTarget target = client.target("http://" + address + ":8080/AgentiWeb/rest/agentskiCentar/messages");
		target.request(MediaType.APPLICATION_JSON).post(Entity.entity(aclMessage, MediaType.APPLICATION_JSON));
	}
}
