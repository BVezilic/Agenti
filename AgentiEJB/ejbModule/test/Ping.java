package test;

import javax.ejb.Stateful;

import model.ACLMessage;
import model.Agent;

//@Remote(AgentInterface.class)
@Stateful
public class Ping extends Agent {

	private static final long serialVersionUID = -9209081242711408357L;

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		System.out.println("AGENT STOPPED");
	}

	@Override
	public void handleMessage(ACLMessage poruka) {
		// TODO Auto-generated method stub
		System.out.println("STIGLA PORUKA");
	}

}
