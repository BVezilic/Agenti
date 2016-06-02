package test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import jms.JMSQueue;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.Performative;

@Remote(AgentInterface.class)
@Stateful
public class Slave extends Agent {

	private static final long serialVersionUID = -814335692392089461L;

	Logger log = Logger.getLogger("SLAVE AGENT");
	
	private HashMap<String, Integer> recnik = new HashMap<String, Integer>();
	
	@Override
	public void stop() {
		log.info("SLAVE STOPPED");
	}

	@Override
	public void handleMessage(ACLMessage poruka) {
		if(poruka.getPerformative().equals(Performative.REQUEST)) {
			Path path = FileSystems.getDefault().getPath(poruka.getContent());
			log.info("Trazim fajl na putanji " + path.toString());
			try {
				for (String line : Files.readAllLines(path)) {
					for (String word : line.split(" ")) {
						if (recnik.containsKey(word)) {
							recnik.put(word, recnik.get(word) + 1);
						} else {
							recnik.put(word, 1);
						}
					}
				}
			} catch (IOException e) {
				System.out.println("Nisam nasao fajl");
				e.printStackTrace();
			}
			ACLMessage aclMsg = new ACLMessage();
			aclMsg.setSender(this.getAid());
			aclMsg.setReceivers(new AID[]{poruka.getSender()});
			aclMsg.setPerformative(Performative.INFORM);
			aclMsg.setContentObj(recnik);
			log.info("Formirao sam poruku za Map agenta: " + aclMsg.toString());
			new JMSQueue(aclMsg);
		}
	}
}
