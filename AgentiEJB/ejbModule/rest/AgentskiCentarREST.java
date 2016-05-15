package rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import database.Database;
import model.ACLMessage;
import model.Agent;
import model.AgentType;
import model.AgentskiCentar;
import model.Performative;

@LocalBean
@Path("/agentskiCentar")
@Stateless
public class AgentskiCentarREST implements AgentskiCentarRESTRemote {

	@EJB
	Database database;
	
// KLIJENT - AGENTSKI CENTAR

	/**
	 * dobavi listu svih tipova agenata na sistemu;
	 */
	@GET
	@Path("/agents/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AgentType> getAgentTypes(){ 
	
		return null;
	}
	
	/**
	 * dobavi sve pokrenute agente sa sistema;
	 */
	@GET
	@Path("/agents/running")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Agent> getActiveAgents(){
	
		return null;
	}
	
	/**
	 * pokreni agenta određenog tipa sa zadatim imenom;
	 */
	@PUT
	@Path("/agents/running/{type}/{name}")
	public void startAgentByName(@PathParam("type") String type,@PathParam("name") String name){ 
	
	}
	
	/**
	 * zaustavi određenog agenta
	 */
	@DELETE
	@Path("/agents/running/{aid}")
	public void stopAgentByAID(@PathParam("aid")String aid){
	
	}
	
	/**
	 * pošalji ACL poruku
	 */
	@POST
	@Path("/messages")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendACLMessage(ACLMessage aclMessage){
	
	}
	
	/**
	 * dobavi listu performativa.
	 */
	@GET
	@Path("/message")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Performative> getPerformatives(){ 
	
		return null;
	}
	
// AGENTSKI CENTAR - AGENTSKI CENTAR
	
	/**
	 * Nov ne-master čvor kontaktira master čvor koji ga registruje;
	 * Master čvor javlja ostalim ne-master čvorovima da je nov ne-master čvor ušao u mrežu;
	 * Master čvor dostavlja spisak ostalih ne-master čvorova novom ne-master čvoru;
	 */
	@POST
	@Path("/node")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<AgentskiCentar> register(AgentskiCentar agentskiCentar){
			
		if (database.isMaster()){
			System.out.println("Master cvor primio register od " + agentskiCentar.getAlias());
			System.out.println("Master cvor salje zahtev za spisak podrzavanjih agenata");
			
			// Zahtev za spisak podrzavanih agenata od novog cvora (@GET /agents/classes)
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target("http://" + agentskiCentar.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/agents/classes");
			Response response = target.request(MediaType.APPLICATION_JSON).get();
			ArrayList<AgentType> podrzavaniAgenti = (ArrayList<AgentType>) response.readEntity(new GenericType<List<AgentType>>(){});
			database.addSviTipoviAgenata(podrzavaniAgenti);
			
			// Salje se zahtev za register svim cvorovima osim master cvoru i novom cvoru (@POST /node)
			for (AgentskiCentar ac : database.getAgentskiCentri()) {
				if (!ac.getAddress().equals(database.getMasterIP()) && !ac.getAddress().equals(agentskiCentar.getAddress())){
					target = client.target("http://" + ac.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/node");
					response = target.request().post(Entity.entity(agentskiCentar, MediaType.APPLICATION_JSON));
				}
			}
			
			// Salje se spisak novih tipova agenata svim cvorovima osim masteru (@POST /agents/classes)
			for (AgentskiCentar ac : database.getAgentskiCentri()) {
				if (!ac.getAddress().equals(database.getMasterIP())){
					target = client.target("http://" + ac.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/agents/classes");
					response = target.request().post(Entity.entity(database.getSviTipoviAgenata(), MediaType.APPLICATION_JSON));
				}
			}
			
			return database.getAgentskiCentri();
			
		} else {
			
			System.out.println("Slave cvor primio register za cvor " + agentskiCentar.getAlias());
			database.addAgentskiCentar(agentskiCentar);
			
			return null;
		}
		
	}
	
	/**
	 * Master čvor traži spisak tipova agenata koje podržava nov ne-master čvor;
	 */
	@GET
	@Path("/agents/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AgentType> getSupportedAgentTypes(){ 
		return database.getPodrzaniTipoviAgenata();
	}
	
	/**
	 * Master čvor dostavlja spisak novih tipova agenata (ukoliko ih ima) ostalim nemaster čvorovima;
	 * Master čvor dostavlja spisak tipova agenata novom ne-master čvoru koje podržava on ili neki od ostalih ne-master čvorova;
	 */
	@POST
	@Path("/agents/classes")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendAgentTypes(List<AgentType> tipoviAgenata){ 
		database.addSviTipoviAgenata((ArrayList<AgentType>)tipoviAgenata);
	}
	
	/**
	 * Master čvor dostavlja spisak pokrenutih agenata novom ne-master čvoru koji se nalaze kod njega ili nekog od preostalih ne-master čvorova;
	 */
	@POST
	@Path("/agents/running")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendStartedAgents(List<Agent> agents){
		database.addAllActiveAgents(agents);
	}
	
	/**
	 * Master čvor javlja ostalim ne-master čvorovima da obrišu čvor koji nije uspeo da izvrši handshake, 
	 * kao i sve tipove agenata koji su potencijalno dostavljeni ostalim čvorovima. 
	 * Ova operacije se takođe treba eksplicitno pokrenuti kada se neki čvor priprema za gašenje i želi da se odjavi iz klastera. 
	 * Prilikom gašenja čvora treba pogasiti i sve agente koji trče na datom čvoru;
	 */
	@DELETE
	@Path("/node/{alias}")
	public void rollback(@PathParam("alias") String alias){ 
		
	}
	
	/**
	 * Ukoliko se desi da čvor ne odgovori zahtev se izvršava još jednom i ukoliko čvor ni tada ne odgovori smatra se da je ugašen 
	 * i javlja se ostalim čvorovima da izbace zapis o ugašenom čvoru.
	 */
	@GET
	@Path("/node")
	@Produces(MediaType.TEXT_PLAIN)
	public Boolean heartBeat(){ 
		return null;
	}
}
