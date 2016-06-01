package test;

import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

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
			new JMSQueue(aclMessage);
			log.info("Zavrseno slanje poruke ka Pingu");
		}
	}

}
