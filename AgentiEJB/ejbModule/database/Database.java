package database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import model.Agent;
import model.AgentskiCentar;

@Startup
@Singleton
public class Database {

	private ArrayList<Agent> activeAgents = new ArrayList<Agent>();
	private ArrayList<AgentskiCentar> agentskiCentri = new ArrayList<AgentskiCentar>();
	
	private String masterIP = "192.168.0.10";
	private AgentskiCentar agentskiCentar;
	
	@PostConstruct
	public void onStartup(){
		
		 
		try {
			agentskiCentar = new AgentskiCentar();
			agentskiCentar.setAddress(InetAddress.getLocalHost().getHostAddress());
			agentskiCentar.setAlias(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		if (!isMaster()){
			System.out.println("Nisam master");
			if (!doHandshake()){
				rollback();
			}
		} else {
			System.out.println("Ja sam master");
		}
	}
	
	public Boolean doHandshake(){
		try {
			System.out.println("doHandshake -- dataBase");
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target("http://" + masterIP + ":8080/AgentiWeb/rest/agentskiCentar/node/" + agentskiCentar.getAlias());
			Response response = target.request().post(Entity.entity(agentskiCentar, MediaType.APPLICATION_JSON));
		
		} catch (Exception e){
			System.out.println("Desion se exception doHandshake");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void rollback(){
		
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + masterIP + ":8080/AgentiWeb/rest/agentskiCentar/node/" + agentskiCentar.getAlias());
		Response response = target.request().delete();
			
	}
	
	public Boolean isMaster(){
		if (agentskiCentar.getAddress().equals(masterIP)){
			return true;
		} else {
			return false;
		}
	}

	public ArrayList<Agent> getActiveAgents() {
		return activeAgents;
	}

	public void setActiveAgents(ArrayList<Agent> activeAgents) {
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

	
	
}
