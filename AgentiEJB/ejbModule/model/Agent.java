package model;

public abstract class Agent implements AgentInterface {


	private static final long serialVersionUID = -6431055503756843639L;
	
	private AID id;

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
	
	
	
}
