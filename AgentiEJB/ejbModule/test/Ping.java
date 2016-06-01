package test;

import java.util.ArrayList;
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
			aclMessage.setReceivers(findPong());
			aclMessage.setConversationID(poruka.getConversationID());
			aclMessage.setPerformative(Performative.REQUEST);
			log.info("Formirao sam poruku za Pong-a: " + aclMessage.toString());
			// posalji poruku
			new JMSQueue(aclMessage);
			log.info("Zavrsio sam slanje ka Pongu");
		} else if (poruka.getPerformative().equals(Performative.INFORM)){
			log.info("Stigao mi je odgovor od Pong-a. Kraj razgovora.");
		}
	}
	
	private AID[] findPong() {
		log.info("Trazim ponga u svom agentskom centru");
		ArrayList<AgentInterface> ai = (ArrayList<AgentInterface>)database.getAgentsByTypeName("Pong");
		if (ai.isEmpty())
			return new AID[]{};
		else
			return new AID[]{ai.get(0).getAid()};
	}
}
