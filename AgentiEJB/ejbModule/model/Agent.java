package model;

public class Agent implements AgentInterface {


	private static final long serialVersionUID = -6431055503756843639L;
	
	protected AID id;
	
	public Agent(AID id){
		this.id = id;
	}
	
	public Agent(){
		id = null;
	}
	
	public void init(AID aid) {
		this.id = aid;
	}
	
	public AID getId() {
		return id;
	}

	public void setId(AID id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Agent [id=" + id + "]";
	}
	
	@Override
	public void setAid(AID aid){
		this.id = aid;
	}
	
	@Override
	public AID getAid(){
		return id;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		System.out.println("AGENT(onaj sto je abstract) STOP");
	}

	@Override
	public void handleMessage(ACLMessage aclMessage) {
		// TODO Auto-generated method stub
		System.out.println("AGENT(onaj sto je abstract) HADNLE MESSAGE");
	}
	
	
	
}
