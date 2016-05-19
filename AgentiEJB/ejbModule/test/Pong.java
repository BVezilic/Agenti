package test;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import model.ACLMessage;
import model.Agent;
import model.AgentInterface;

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
		// TODO Auto-generated method stub
		System.out.println("PONG - STIGLA PORUKA");
	}
	
}
