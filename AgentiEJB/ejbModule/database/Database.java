package database;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import model.AgentInterface;
import model.AgentType;
import model.AgentskiCentar;

@Startup
@Singleton
public class Database {

	private ArrayList<AgentInterface> activeAgents = new ArrayList<AgentInterface>();
	private ArrayList<AgentskiCentar> agentskiCentri = new ArrayList<AgentskiCentar>();
	
	// Spisak agenata koji su podrzani na ovom cvoru
	private ArrayList<AgentType> podrzaniTipoviAgenata = new ArrayList<AgentType>();
	private ArrayList<AgentType> sviTipoviAgenata = new ArrayList<AgentType>();
	private String masterIP = "192.168.0.10";
	private AgentskiCentar agentskiCentar;
	
	
	
	/*@PostConstruct
	public void onStartup(){
		
		if (!isMaster()){
			System.out.println("Nisam master");
			if (!doHandshake()){
				rollback();
			} else {
			}
		} else {
			System.out.println("Ja sam master");
			addAgentskiCentar(agentskiCentar);
			
		}
	}
	
	public Boolean doHandshake(){
		try {
			
			// Saljem register ka masteru
			System.out.println("doHandshake -- DataBase");
			ResteasyClient client = new ResteasyClientBuilder().build();
			System.out.println("http://" + masterIP + ":8080/AgentiWeb/rest/agentskiCentar/node/" + agentskiCentar.getAlias());
			ResteasyWebTarget target = client.target("http://" + masterIP + ":8080/AgentiWeb/rest/agentskiCentar/node/");
			Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(agentskiCentar, MediaType.APPLICATION_JSON));
			List<AgentskiCentar> agentskiCentri = response.readEntity(new GenericType<List<AgentskiCentar>>(){});
			addAllAgentskiCentri(agentskiCentri);
			System.out.println("Odradjen handshake");
					
		} catch (Exception e){
			System.out.println("Desion se exception doHandshake -- Database");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void rollback(){
		
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + masterIP + ":8080/AgentiWeb/rest/agentskiCentar/node/" + agentskiCentar.getAlias());
		/*Response response = target.request().delete();
			
	}
	*/
	
	public Boolean isMaster(){
		if (agentskiCentar.getAddress().equals(masterIP)){
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean addActiveAgent(AgentInterface agent){
		System.out.println("NJegov id");
		for (AgentInterface a : activeAgents) {
			if (a.getAID().getName().equals(agent.getAID().getName())){
				System.out.println("Database addActiveAgent -- Postoji agent sa istim id, name:" + a.getAID().getName());
				return false;
			}
		}
		System.out.println("Dodat novi aktivni agent name:" + agent.getAID().getName());
		activeAgents.add(agent);
		return true;
	}
	
	public void addAllActiveAgents(List<AgentInterface> agents){
		for (AgentInterface agent : agents) {
			addActiveAgent(agent);
		}
	}
	
	public Boolean addAgentskiCentar(AgentskiCentar agentskiCentar){
		for (AgentskiCentar ac : agentskiCentri) {
			if (ac.getAlias().equals(agentskiCentar.getAlias())){
				System.out.println("Database addAgentskiCentar -- Postoji agentski centar sa istim aliasom: " + ac.getAlias());
				return false;
			}
		}
		System.out.println("Dodat novi agentski centar: " + agentskiCentar.getAlias() + " " + agentskiCentar.getAddress());
		agentskiCentri.add(agentskiCentar);
		return true;
	}
	
	public void addAllAgentskiCentri(List<AgentskiCentar> agentskiCentri){
		for (AgentskiCentar agentskiCentar : agentskiCentri) {
			addAgentskiCentar(agentskiCentar);
		}
	}
	
	public Boolean addPodrzaniTipAgenta(AgentType agentType){
		for (AgentType at : podrzaniTipoviAgenata) {
			if (at.getName().equals(agentType.getName()) && at.getModule().equals(agentType.getModule())){
				System.out.println("Postoji tip agenta sa istim name:" + at.getName() + " i module: " + at.getModule());
				return false;
			}
		}
		
		System.out.println("Dodat novi podrzan tip agenta name:" + agentType.getName());
		podrzaniTipoviAgenata.add(agentType);
		return true;
	}
	
	public Boolean addSviTipoviAgenata(AgentType agentType){
		for (AgentType at : sviTipoviAgenata) {
			if (at.getName().equals(agentType.getName()) && at.getModule().equals(agentType.getModule())){
				System.out.println("Postoji tip agenta sa istim name:" + at.getName() + " i module: " + at.getModule());
				return false;
			}
		}
		System.out.println("Dodat novi tip agenta u listu svih tipova agenata: " + agentType.getName());
		sviTipoviAgenata.add(agentType);
		return true;
	}
	
	public void addSviTipoviAgenata(ArrayList<AgentType> listaAgenata){
		for (AgentType at : listaAgenata) {
			addSviTipoviAgenata(at);
		}
	}
	
	public AgentInterface getActiveAgentByName(String name){
		for (AgentInterface agent : activeAgents) {
			if (agent.getAID().getName().equals(name)){
				return agent;
			}
		}
		return null;
	}
	
	public boolean removeActiveAgent(AgentInterface agent){
		for (AgentInterface a : activeAgents){
			if (a.equals(agent)){
				activeAgents.remove(a);
				return true;
			}
		}
		
		return false;
	}

	public ArrayList<AgentInterface> getActiveAgents() {
		return activeAgents;
	}

	public void setActiveAgents(ArrayList<AgentInterface> activeAgents) {
		this.activeAgents = activeAgents;
	}

	public ArrayList<AgentskiCentar> getAgentskiCentri() {
		return agentskiCentri;
	}

	public void setAgentskiCentri(ArrayList<AgentskiCentar> agentskiCentri) {
		this.agentskiCentri = agentskiCentri;
	}

	public String getMasterIP() {
		return masterIP;
	}

	public void setMasterIP(String masterIP) {
		this.masterIP = masterIP;
	}

	public AgentskiCentar getAgentskiCentar() {
		return agentskiCentar;
	}

	public void setAgentskiCentar(AgentskiCentar agentskiCentar) {
		this.agentskiCentar = agentskiCentar;
	}

	public ArrayList<AgentType> getPodrzaniTipoviAgenata() {
		return podrzaniTipoviAgenata;
	}

	public void setPodrzaniTipoviAgenata(ArrayList<AgentType> podrzaniTipoviAgenata) {
		this.podrzaniTipoviAgenata = podrzaniTipoviAgenata;
	}

	public ArrayList<AgentType> getSviTipoviAgenata() {
		return sviTipoviAgenata;
	}

	public void setSviTipoviAgenata(ArrayList<AgentType> sviTipoviAgenata) {
		this.sviTipoviAgenata = sviTipoviAgenata;
	}

	
	
}
