package test;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import database.Database;
import jms.JMSQueue;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.AgentType;
import model.Performative;
import rest.AgentskiCentarREST;

@Remote(AgentInterface.class)
@Stateful
public class Map extends Agent {

	private static final long serialVersionUID = -8864458269166943526L;
	
	Logger log = Logger.getLogger("MAP AGENT");
	
	@EJB
	AgentskiCentarREST agentskiCentar;

	@EJB
	Database database;
	
	private static int SLAVE_NUMBER;
	private HashMap<String, Integer> recnik = new HashMap<String, Integer>();
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		System.out.println("MAP STOPPED");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(ACLMessage poruka) {
		if(poruka.getPerformative().equals(Performative.REQUEST)) {
			File folder = new File(poruka.getContent());
			File[] files = folder.listFiles();
			SLAVE_NUMBER = files.length;
			//ArrayList<ACLMessage> msgs = new ArrayList<ACLMessage>();
			for(int i=0; i < files.length; i++) {
				log.info("Pravim slave redni broj " + i);
				startSlave("Slave", "slave" + i);
				ACLMessage aclMsg = new ACLMessage();
				aclMsg.setContent(files[i].getPath());
				aclMsg.setSender(this.getAid());
				aclMsg.setReceivers(findSlaveByName("slave" + i));
				aclMsg.setPerformative(Performative.REQUEST);
				new JMSQueue(aclMsg);
			}
//			for (ACLMessage msg : msgs) {
//				log.info("Saljem spisak poruka za slave agente na queue");
//				new JMSQueue(msg);
//			}
		} else if (poruka.getPerformative().equals(Performative.INFORM)) {
			SLAVE_NUMBER--;
			HashMap<String, Integer> words = (HashMap<String, Integer>) poruka.getContentObj();
			log.info("Spajam rezultate od slave agenta: " + words);
			for (Entry<String, Integer> entry : words.entrySet()) {
			    String key = entry.getKey();
			    if (recnik.containsKey(key)) {
					recnik.put(key, recnik.get(key) + words.get(key));
				} else {
					recnik.put(key, words.get(key));
				}
			}
			log.info("Stopiram slave agenta sa imenom: " + poruka.getSender().getName());
			stopSlave(poruka.getSender().getName(), poruka.getSender().getHost().getAlias());
			if (SLAVE_NUMBER == 0) {
				log.info(recnik.toString());
			}
		}
	}
	
	private AID[] findSlaveByName(String name) {
		AID[] receivers = new AID[1];
		log.info("Trazim slave agenta kome treba da posaljem poruku: " + name);
		receivers[0] = database.getActiveAgentByName(name).getAid();
		return receivers;
	}
	
	private void startSlave(String type, String name) {
		Context context;
		try {
			context = new InitialContext();
			AgentInterface agent = (AgentInterface) context.lookup("java:module/" + type);
			agent.init(new AID(name, database.getAgentskiCentar(), new AgentType(type,null)));
			database.addActiveAgent(agent);	
			context.close();
		} catch (NamingException e) {
			log.info("Pukao lookup");
			e.printStackTrace();
		}
		
	}
	
	private void stopSlave(String aid, String hostName) {
		AID agentAID = new AID(aid, database.getAgentskiCentarByName(hostName) , null);
		AgentInterface agent = database.getActiveAgentByAID(agentAID);
		database.removeActiveAgent(agent);
		agent.stop();
	}
}
