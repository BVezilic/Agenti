package test;

import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.logging.Logger;

import database.Database;
import jms.JMSQueue;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.Performative;

@Stateful
@Remote(AgentInterface.class)
public class Participant extends Agent {

	private static final long serialVersionUID = 2895407059340456494L;

	Logger log = Logger.getLogger("PARTICIPANT AGENT");
	
	@EJB
	Database database;
	
	@Override 
	public void stop(){
		log.info("INITIATOR STOPPED");
	}
	
	@Override
	public void handleMessage(ACLMessage poruka){
		if (poruka.getPerformative().equals(Performative.CFP)){
			log.info("Zapocinjem handle cfp");
			ACLMessage aclMessage = new ACLMessage();
			aclMessage.setSender(this.getAid());
			aclMessage.setReceivers(new AID[]{poruka.getSender()});
			aclMessage.setConversationID(poruka.getConversationID());
			if (doYouWantToWork()){
				aclMessage.setPerformative(Performative.PROPOSE);
				int cena = (new Random()).nextInt(100);
				aclMessage.setContent("Hocu da radim, za:" + cena );
			} else {
				aclMessage.setPerformative(Performative.REFUSE);
				aclMessage.setContent("Necu da radim");
			}
			log.info("Formirao sam poruku za initiatora:" + aclMessage.getPerformative().toString());
			
			new JMSQueue(aclMessage);
			
		} else if (poruka.getPerformative().equals(Performative.REJECT)){
			log.info( this.id.getName() + ": Odbili su moju ponudu");
		} else if (poruka.getPerformative().equals(Performative.ACCEPT)){
			
			log.info( this.id.getName() + ": Prihvatili su moju ponudu");
			ACLMessage aclMessage = new ACLMessage();
			aclMessage.setSender(this.getAid());
			aclMessage.setReceivers(new AID[]{poruka.getSender()});
			aclMessage.setConversationID(poruka.getConversationID());
			
			if (doWork()){
				aclMessage.setPerformative(Performative.INFORM_DONE);
				aclMessage.setContent("zavrsio sam");
			} else {
				aclMessage.setPerformative(Performative.FAILURE);
				aclMessage.setContent("failovao sam");
			}
			
			new JMSQueue(aclMessage);
			
		}
	}
	
	public boolean doYouWantToWork(){
		return (new Random()).nextBoolean();
	}
	
	public boolean doWork(){
		Random r = new Random();
		return (r.nextInt(100) < 95); 
	}
	
}
