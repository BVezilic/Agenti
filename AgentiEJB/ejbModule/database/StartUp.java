package database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import model.AgentType;
import model.AgentskiCentar;

@Startup
@Singleton
public class StartUp {

	@EJB
	Database database;
	

	@PostConstruct
	public void startUp(){
		
		AgentskiCentar agentskiCentar = new AgentskiCentar();
		try {
			agentskiCentar.setAddress(InetAddress.getLocalHost().getHostAddress());
			agentskiCentar.setAlias(InetAddress.getLocalHost().getHostName());
			database.setAgentskiCentar(agentskiCentar);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		if (database.isMaster()){
			
			System.out.println("Startup -- Master");
			database.addAgentskiCentar(agentskiCentar);
			
			AgentType ping = new AgentType("Ping", "AgentiEJB");
			database.addPodrzaniTipAgenta(ping);
			database.addSviTipoviAgenata(ping);
			
		} else {
			
			if (!doHandshake()){
				rollback();
			}
			System.out.println("Startup -- Slave");
			AgentType ping = new AgentType("Pong", "AgentiEJB");
			database.addPodrzaniTipAgenta(ping);
			database.addSviTipoviAgenata(ping);
		}
		
	}
	
	public Boolean doHandshake(){
		
		try {
			
			// Saljem register ka masteru
			System.out.println("doHandshake -- DataBase");
			ResteasyClient client = new ResteasyClientBuilder().build();
			System.out.println("http://" + database.getMasterIP() + ":8080/AgentiWeb/rest/agentskiCentar/node/" + database.getAgentskiCentar().getAlias());
			ResteasyWebTarget target = client.target("http://" + database.getMasterIP() + ":8080/AgentiWeb/rest/agentskiCentar/node/");
			Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(database.getAgentskiCentar(), MediaType.APPLICATION_JSON));
			List<AgentskiCentar> agentskiCentri = response.readEntity(new GenericType<List<AgentskiCentar>>(){});
			database.addAllAgentskiCentri(agentskiCentri);
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
		ResteasyWebTarget target = client.target("http://" + database.getMasterIP() + ":8080/AgentiWeb/rest/agentskiCentar/node/" + database.getAgentskiCentar().getAlias());
		/*Response response =*/ target.request().delete();
			
	}
}
