package model;

import java.io.Serializable;

public class AID implements Serializable {

	private static final long serialVersionUID = -6147672430150261641L;
	
	private String name;
	private AgentskiCentar host;
	private AgentType type;
	
	
	public AID() {
		super();
	}

	public AID(String name, AgentskiCentar host, AgentType type) {
		super();
		this.name = name;
		this.host = host;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AgentskiCentar getHost() {
		return host;
	}

	public void setHost(AgentskiCentar host) {
		this.host = host;
	}

	public AgentType getType() {
		return type;
	}

	public void setType(AgentType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "AID [name=" + name + ", host=" + host + ", type=" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AID other = (AID) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
}
