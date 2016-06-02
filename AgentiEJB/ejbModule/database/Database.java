package database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.websocket.Session;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.AgentType;
import model.AgentskiCentar;

@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Startup
@Singleton
public class Database {

	Logger log = Logger.getLogger("DATABASE");
	
	//private ArrayList<AgentInterface> activeAgents = new ArrayList<AgentInterface>();
	private HashMap<AID,AgentInterface> activeAgents = new HashMap<AID,AgentInterface>();
	private ArrayList<AgentskiCentar> agentskiCentri = new ArrayList<AgentskiCentar>();
	
	private ArrayList<AgentType> podrzaniTipoviAgenata = new ArrayList<AgentType>();
	private ArrayList<AgentType> sviTipoviAgenata = new ArrayList<AgentType>();
	
	private ArrayList<Session> sessions = new ArrayList<Session>();
	private ArrayList<ACLMessage> messages = new ArrayList<ACLMessage>();
	
	private String masterIP = "192.168.0.10";

	private AgentskiCentar agentskiCentar;
	
	public Boolean isMaster(){
		if (agentskiCentar.getAddress().equals(masterIP)){
			return true;
		} else {
			return false;
		}
	}
	
	@Lock(LockType.WRITE)
	public Boolean addActiveAgent(AgentInterface agent){
		
		System.out.println(agent);
		AID aid = agent.getAid();
		for(AID a : activeAgents.keySet()){
			if (aid.getName().equals(a.getName())){
				return false;
			}
		}
		
		activeAgents.put(aid,agent);
		
		/*
		for (AgentInterface a : activeAgents) {
			if (a.getAid().getName().equals(agent.getAid().getName())){
				System.out.println("Database addActiveAgent -- Postoji agent sa istim id, name:" + a.getAid().getName());
				return false;
			}
			
		}
		
		System.out.println("Dodat novi aktivni agent name:" + agent.getAid().getName());
		
		activeAgents.add(agent);
		*/
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
	
	public Boolean removeAgentskiCentar(AgentskiCentar agentskiCentar){
		for (AgentskiCentar ac : agentskiCentri) {
			if (ac.getAlias().equals(agentskiCentar.getAlias())){
				agentskiCentri.remove(agentskiCentar);
				System.out.println("Obrisan agentski centar sa imenom " + agentskiCentar.getAlias());
				return true;
			}
		}
		return false;
	}
	
	public void removeAllAgentsByAgentskiCentar(AgentskiCentar agentskiCentar){
		String centarAlias = agentskiCentar.getAlias();
		ArrayList<AID> zaBrisanje = new ArrayList<AID>();
		
		for (AID a : activeAgents.keySet()) {
			if (a.getHost().getAlias().equals(centarAlias)){
				zaBrisanje.add(a);
			}
		}
		/*
		for (AgentInterface agentInterface : activeAgents) {
			if(agentInterface.getAid().getHost().getAlias().equals(centarAlias)){
				System.out.println("Brisem agenta " + agentInterface.getAid().getName());
				zaBrisanje.add(agentInterface);
			}
		}
		*/
		for (AID a : zaBrisanje) {
			activeAgents.remove(a);
		}
		
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
	
	@Lock(LockType.READ)
	public AgentInterface getActiveAgentByName(String name){
		
		for (AID aid : activeAgents.keySet()){
			if (aid.getName().equals(name)){
				return activeAgents.get(aid);
			}
		}
		/*
		for (AgentInterface agent : activeAgents) {
			if (agent.getAid().getName().equals(name)){
				return agent;
			}
		}
		*/
		return null;
	}
	
	public List<AgentInterface> getAgentsByTypeName(String typeName){
		ArrayList<AgentInterface> retVal = new ArrayList<AgentInterface>();	
		
		for (AgentInterface agentInterface : activeAgents.values()) {
			if (agentInterface.getAid().getType().getName().equals(typeName)){
				retVal.add(agentInterface);
			}
		}
		
		return retVal;
	}
	
	@Lock(LockType.READ)
	public AgentInterface getActiveAgentByAID(AID aid){
		log.info("Trazim agenta po AID-u: " + aid.getName());
		/*
		for (AID a : activeAgents) {
			AID temp = agent.getAid();
			//System.out.println(temp.getName() + " " + aid.getName());
			//System.out.println(temp.getHost().getAlias() + " " + aid.getHost().getAlias());
			if (temp.getName().equals(aid.getName()) && temp.getHost().getAlias().equals(aid.getHost().getAlias())){
				return agent;
			}
		}
		*/
		return activeAgents.get(aid);
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
		for (AgentInterface a : activeAgents.values()){
			if (a.getAid().getName().equals(agent.getAid().getName())){
				activeAgents.remove(a.getAid());
				return true;
			}
		}
		return false;
	}

	public ArrayList<AgentInterface> getActiveAgents() {
		ArrayList<AgentInterface> ai = new ArrayList<AgentInterface>();
		
		for (AgentInterface agentInterface : activeAgents.values()) {
			ai.add(agentInterface);
		}
		return ai;
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
		
		
		for (AID aid : activeAgents.keySet()) {
			retVal.add(new Agent(aid));
		}
		
		return retVal;
	}

	public void setActiveAgents(HashMap<AID,AgentInterface> activeAgents) {
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

	public void sendMessageToSocket() throws IOException, JSONException {
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(messages);
		System.out.println(jsonInString);
		for (Session s : sessions) {
			JSONObject jsonObj = new JSONObject();	
			jsonObj.put("data", new JSONArray(jsonInString));
			jsonObj.put("type", "messagesPoll");
			s.getBasicRemote().sendText(jsonObj.toString());
		}
	}

	public void sendActiveToSocket() throws JSONException, IOException {
		// napravi listu aid-a umesto remote interfejsa
		List<AID> aids = new ArrayList<>();
		
		for (AID aid : activeAgents.keySet()){
			aids.add(aid);
		}
		
		// prodji kroz sve sesije i posalji listu agenata
		for (Session s : sessions) {
			JSONObject jsonObj = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			for(AID ai : aids){
				jsonArray.put(new JSONObject(ai));
			}		
			jsonObj.put("data", jsonArray);
			jsonObj.put("type", "active");
			s.getBasicRemote().sendText(jsonObj.toString());
		}
	}
	
	public ArrayList<ACLMessage> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<ACLMessage> messages) {
		this.messages = messages;
	}
	
}