package model;

public abstract class Agent implements AgentInterface {


	private static final long serialVersionUID = -6431055503756843639L;
	
	protected AID id;
	
	public void init(AID aid) {
		// TODO Auto-generated method stub
		System.out.println("INIT AGENT");
		this.id = aid;
		System.out.println("AGENT INITIALISED");
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
	
	
	
}
