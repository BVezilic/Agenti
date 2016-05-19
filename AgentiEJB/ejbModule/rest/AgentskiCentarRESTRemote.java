package rest;

import java.util.List;

import javax.ejb.Remote;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import model.ACLMessage;
import model.AID;
import model.AgentInterface;
import model.AgentType;
import model.AgentskiCentar;
import model.Performative;

@Remote
public interface AgentskiCentarRESTRemote {

// KLIJENT - AGENTSKI CENTAR

		/**
		 * dobavi listu svih tipova agenata na sistemu;
		 */
	
		@GET
		@Path("/agents/types")
		@Produces(MediaType.APPLICATION_JSON)
		public List<AgentType> getAgentTypes();
	
		/**
		 * dobavi sve pokrenute agente sa sistema;
		 */
		@GET
		@Path("/agents/running")
		@Produces(MediaType.APPLICATION_JSON)
		public List<AgentInterface> getActiveAgents();
		
		/**
		 * pokreni agenta određenog tipa sa zadatim imenom;
		 */
		@PUT
		@Path("/agents/running/{type}/{name}")
		public void startAgentByName(@PathParam("type") String type,@PathParam("name") String name);
		
		/**
		 * zaustavi određenog agenta
		 */
		@DELETE
		@Path("/agents/running/{aid}/{hostName}")
		public void stopAgentByAID(@PathParam("aid") String aid, @PathParam("hostName") String hostName);
		
		/**
		 * pošalji ACL poruku
		 */
		@POST
		@Path("/messages")
		@Consumes(MediaType.APPLICATION_JSON)
		public void sendACLMessage(ACLMessage aclMessage);
		
		/**
		 * dobavi listu performativa.
		 */
		@GET
		@Path("/message")
		@Produces(MediaType.APPLICATION_JSON)
		public List<Performative> getPerformatives();
		
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
		public List<AgentskiCentar> register(AgentskiCentar agentskiCentar);
		
		/**
		 * Master čvor traži spisak tipova agenata koje podržava nov ne-master čvor;
		 */
		@GET
		@Path("/agents/classes")
		@Produces(MediaType.APPLICATION_JSON)
		public List<AgentType> getSupportedAgentTypes();
		
		/**
		 * Master čvor dostavlja spisak novih tipova agenata (ukoliko ih ima) ostalim nemaster čvorovima;
		 * Master čvor dostavlja spisak tipova agenata novom ne-master čvoru koje podržava on ili neki od ostalih ne-master čvorova;
		 */
		@POST
		@Path("/agents/classes")
		@Consumes(MediaType.APPLICATION_JSON)
		public void sendAgentTypes(List<AgentType> tipoviAgenata);
		
		/**
		 * Master čvor dostavlja spisak pokrenutih agenata novom ne-master čvoru koji se nalaze kod njega ili nekog od preostalih ne-master čvorova;
		 */
		@POST
		@Path("/agents/running")
		@Consumes(MediaType.APPLICATION_JSON)
		public void sendStartedAgents(List<AgentInterface> agents);
		
		/**
		 * Master čvor javlja ostalim ne-master čvorovima da obrišu čvor koji nije uspeo da izvrši handshake, 
		 * kao i sve tipove agenata koji su potencijalno dostavljeni ostalim čvorovima. 
		 * Ova operacije se takođe treba eksplicitno pokrenuti kada se neki čvor priprema za gašenje i želi da se odjavi iz klastera. 
		 * Prilikom gašenja čvora treba pogasiti i sve agente koji trče na datom čvoru;
		 */
		@DELETE
		@Path("/node/{alias}")
		public void rollback(@PathParam("alias") String alias);
		
		/**
		 * Ukoliko se desi da čvor ne odgovori zahtev se izvršava još jednom i ukoliko čvor ni tada ne odgovori smatra se da je ugašen 
		 * i javlja se ostalim čvorovima da izbace zapis o ugašenom čvoru.
		 */
		@GET
		@Path("/node")
		@Produces(MediaType.TEXT_PLAIN)
		public Boolean heartBeat();
}