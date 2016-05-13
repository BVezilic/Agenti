package model;

public abstract class Agent {

	private AID id;
	
	public abstract void handleMessage(ACLPoruka poruka);

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
