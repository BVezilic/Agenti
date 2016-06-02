package test;

import java.util.ArrayList;
import java.util.HashMap;

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

@Remote(AgentInterface.class)
@Stateful
public class Initiator extends Agent {

	private static final long serialVersionUID = 6509467929501143779L;

	Logger log = Logger.getLogger("INITIATOR AGENT");

	private HashMap<String, HashMap<AID, Integer>> ponudeZaAgente = new HashMap<String, HashMap<AID, Integer>>();
	private long startTime;
	private long waitTime = 5000;
	@EJB
	Database database;

	@Override
	public void stop() {
		log.info("INITIATOR STOPPED");
	}

	@Override
	public void handleMessage(ACLMessage poruka) {
		if (poruka.getPerformative().equals(Performative.REQUEST)) {

			startTime = System.currentTimeMillis();

			log.info("Zapocinjem handle request");
			ACLMessage aclMessage = new ACLMessage();
			aclMessage.setContent("Ko zeli da uradi ovaj posao za mene");
			aclMessage.setSender(this.getAid());
			aclMessage.setReceivers(findParticipants());
			aclMessage.setConversationID(poruka.getConversationID());
			aclMessage.setPerformative(Performative.CFP);
			log.info("Formirao sam poruku za participante");
			ponudeZaAgente.put(poruka.getConversationID(), new HashMap<AID,Integer>());
			new JMSQueue(aclMessage);

			ACLMessage aclTimeUp = new ACLMessage();
			aclTimeUp.setSender(this.id);
			aclTimeUp.setReceivers(new AID[] { this.id });
			aclTimeUp.setPerformative(Performative.TIME_UP);

			new JMSQueue(aclTimeUp, waitTime);

		} else if (poruka.getPerformative().equals(Performative.REFUSE)) {

			log.info("Agent " + poruka.getSender().getName() + " je odbio cfp");

		} else if (poruka.getPerformative().equals(Performative.PROPOSE)) {

			long currentTime = System.currentTimeMillis();
			if (currentTime - startTime > waitTime) {
				log.info("Agent " + poruka.getSender().getName() + " je zakasnio sa odgovorom na cfp");

				ACLMessage aclMessage = new ACLMessage();
				aclMessage.setSender(this.getAid());
				aclMessage.setConversationID(poruka.getConversationID());
				aclMessage.setPerformative(Performative.REJECT);
				aclMessage.setReceivers(new AID[] { poruka.getSender() });
				new JMSQueue(aclMessage);

			} else {

				log.info("Agent " + poruka.getSender().getName() + " je prihvation cfp");
				String content = poruka.getContent().split(":")[1];
				Integer vrednost = Integer.parseInt(content);
				ponudeZaAgente.get(poruka.getConversationID()).put(poruka.getSender(), vrednost);

			}

		} else if (poruka.getPerformative().equals(Performative.TIME_UP)) {

			log.info("Saljem odgovor participantima");
			AID bestAID = findBestOffer(poruka.getConversationID());

			ACLMessage aclMessage = new ACLMessage();
			aclMessage.setSender(this.getAid());
			aclMessage.setConversationID(poruka.getConversationID());

			for (AID aid : ponudeZaAgente.get(poruka.getConversationID()).keySet()) {

				if (aid.equals(bestAID)) {
					aclMessage.setPerformative(Performative.ACCEPT);

				} else {
					aclMessage.setPerformative(Performative.REJECT);
				}

				aclMessage.setReceivers(new AID[] { aid });
				new JMSQueue(aclMessage);

			}
		} else if (poruka.getPerformative().equals(Performative.INFORM_DONE)) {
			log.info("uspesno zavrsen protokol");
			cleanUp(poruka.getConversationID());
		} else if (poruka.getPerformative().equals(Performative.FAILURE)) {
			log.info("neuspesno zavrsen protokol");
			cleanUp(poruka.getConversationID());
		}
	}

	public AID[] findParticipants() {

		log.info("Trazim participante");
		ArrayList<AID> ai =  database.getAIDSByTypeName("Participant");
		AID[] aids = new AID[ai.size()];

		int brojac = 0;
		for (AID a : ai) {
			aids[brojac++] = a;
		}
		return aids;
	}

	public AID findBestOffer(String conversationID) {

		AID bestAID = new AID();
		Integer bestOffer = Integer.MIN_VALUE;
		for (AID aid : ponudeZaAgente.get(conversationID).keySet()) {
			if (ponudeZaAgente.get(conversationID).get(aid) > bestOffer) {
				bestAID = aid;
				bestOffer = ponudeZaAgente.get(conversationID).get(aid);
			}
		}

		return bestAID;
	}

	public void cleanUp(String conversationID) {
		ponudeZaAgente.remove(conversationID);
	}

}
