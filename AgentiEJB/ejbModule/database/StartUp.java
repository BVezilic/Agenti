package database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
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
	
	String masterIP;

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
		
		masterIP = database.getMasterIP();
		
		if (database.isMaster()){
			
			System.out.println("Startup -- Master");
			database.addAgentskiCentar(agentskiCentar);
			
			AgentType ping = new AgentType("Ping", "AgentiEJB");
			database.addPodrzaniTipAgenta(ping);
			database.addSviTipoviAgenata(ping);
			
			AgentType pong = new AgentType("Pong", "AgentiEJB");
			database.addPodrzaniTipAgenta(pong);
			database.addSviTipoviAgenata(pong);
			
			AgentType map = new AgentType("Map", "AgentiEJB");
			database.addPodrzaniTipAgenta(map);
			database.addSviTipoviAgenata(map);
			
		} else {
			
			AgentType pong = new AgentType("Pong", "AgentiEJB");
			database.addPodrzaniTipAgenta(pong);
			database.addSviTipoviAgenata(pong);
			
			if (!doHandshake()){
				rollback(agentskiCentar);
				if (!doHandshake()){
					rollback(agentskiCentar);
				}
			}
			
			System.out.println("Startup -- Slave");

		}
		
	}
	
	@PreDestroy
	public void preDestroy(){
		
		AgentskiCentar agentskiCentar = new AgentskiCentar();
		try {
			agentskiCentar.setAddress(InetAddress.getLocalHost().getHostAddress());
			agentskiCentar.setAlias(InetAddress.getLocalHost().getHostName());
			//database.setAgentskiCentar(agentskiCentar);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		rollback(agentskiCentar);
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
	
	public void rollback(AgentskiCentar ac){
		
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target("http://" + masterIP +  ":8080/AgentiWeb/rest/agentskiCentar/node/" + ac.getAlias());
		/*Response response =*/ target.request().delete();
			
	}
	
	@Schedules({
		@Schedule(hour = "*", minute = "*", second = "*/10", info = "every tenth"),
		})
	public void timer(){
		
		try{
			AgentskiCentar agentskiCentar = database.getAgentskiCentar();
			for (AgentskiCentar ac : database.getAgentskiCentri()){
				if (!ac.getAddress().equals(agentskiCentar.getAddress())){
					
					boolean flag = checkBeat(ac);
					// prvi pokusaj
					if (flag == false){
						// drugi pokusaj
						flag = checkBeat(ac);
						if (flag == false){
							rollback(ac);
						}
					}
					System.out.println("Agentski centar " + ac.getAlias() + " is alive");
				}
				
			}
		} catch (Exception e){
			
			e.printStackTrace();
			
		}
		
	} 
	
	public boolean checkBeat(AgentskiCentar ac){
		
		try {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target("http://" + ac.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/node/");
			target.request(MediaType.TEXT_PLAIN).get();
			return true;
		} catch (Exception e){
			System.out.println("UHVATION SAM EXCEPTION");
			return false;
		}
		
	}
}
