package database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.websocket.Session;

import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.AgentType;
import model.AgentskiCentar;

@Startup
@Singleton
public class Database {

	private ArrayList<AgentInterface> activeAgents = new ArrayList<AgentInterface>();
	private ArrayList<AgentskiCentar> agentskiCentri = new ArrayList<AgentskiCentar>();
	
	private ArrayList<AgentType> podrzaniTipoviAgenata = new ArrayList<AgentType>();
	private ArrayList<AgentType> sviTipoviAgenata = new ArrayList<AgentType>();
	
	private ArrayList<Session> sessions = new ArrayList<Session>();
	private ArrayList<ACLMessage> messages = new ArrayList<ACLMessage>();
	
	private String masterIP = "192.168.1.6";
	private AgentskiCentar agentskiCentar;
	
	public Boolean isMaster(){
		if (agentskiCentar.getAddress().equals(masterIP)){
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean addActiveAgent(AgentInterface agent){
		
		System.out.println(agent);
		
		
		for (AgentInterface a : activeAgents) {
			if (a.getAid().getName().equals(agent.getAid().getName())){
				System.out.println("Database addActiveAgent -- Postoji agent sa istim id, name:" + a.getAid().getName());
				return false;
			}
			
		}
		
		System.out.println("Dodat novi aktivni agent name:" + agent.getAid().getName());
		
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
			if (agent.getAid().getName().equals(name)){
				return agent;
			}
		}
		return null;
	}
	
	public AgentInterface getActiveAgentByAID(AID aid){
		for (AgentInterface agent : activeAgents) {
			AID temp = agent.getAid();
			System.out.println(temp.getName() + " " + aid.getName());
			System.out.println(temp.getHost());
			if (temp.getName().equals(aid.getName()) && temp.getHost().getAlias().equals(aid.getHost().getAlias())){
				return agent;
			}
		}
		return null;
	}
	
	public AgentskiCentar getAgentskiCentarByName(String name){
		for (AgentskiCentar ac : agentskiCentri){
			if (ac.getAlias().equals(name)){
				return ac;
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
	public ArrayList<AgentInterface> getAgentInterfaceFromClasses(ArrayList<Agent> agents){
		ArrayList<AgentInterface> retVal = new ArrayList<AgentInterface>();
		for (Agent a : agents){
			retVal.add(a);
		}
		return retVal;
	}
	
	public ArrayList<Agent> getActiveAgentsClasses(){
		ArrayList<Agent> retVal = new ArrayList<Agent>();
		for (AgentInterface agentInterface : activeAgents) {
			retVal.add(new Agent(agentInterface.getAid()));
		}
		return retVal;
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

	public ArrayList<Session> getSessions() {
		return sessions;
	}

	public void setSessions(ArrayList<Session> sessions) {
		this.sessions = sessions;
	}

	public void sendMessage(String msg) throws IOException {
		for (Session s : sessions) {
			s.getBasicRemote().sendText(msg);
		}
	}

	public ArrayList<ACLMessage> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<ACLMessage> messages) {
		this.messages = messages;
	}
	
}