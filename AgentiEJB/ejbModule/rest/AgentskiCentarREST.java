package rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import jms.JMSQueue;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentInterface;
import model.AgentType;
import model.AgentskiCentar;
import model.Performative;

@LocalBean
@Path("/agentskiCentar")
@Stateless
public class AgentskiCentarREST implements AgentskiCentarRESTRemote {

	@EJB
	Database database;
	
// TEST 

	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test(){
		String retVal = "";
		retVal += "Trenutni cvor: " + database.getAgentskiCentar().getAlias() + " " + database.getAgentskiCentar().getAddress() + "\n\n";
		retVal += "\nAgentski centri: \n";
		for (AgentskiCentar ac : database.getAgentskiCentri()) {
			retVal += ac.toString() + "\n";
		}
		
		retVal += "\nAktivni agenti: \n";
		for (AgentInterface ag : database.getActiveAgents()){
			retVal += ag.toString() + "\n";
		}
		
		retVal += "\nPodrzani tipovi agenata: \n";
		for (AgentType at : database.getPodrzaniTipoviAgenata()){
			retVal += at.toString() + "\n";
		}
		
		retVal += "\nSvi tipovi agenata: \n";
		for (AgentType at : database.getSviTipoviAgenata()){
			retVal += at.toString() + "\n";
		}
		
		retVal += "\nSve poruke: \n";
		for (ACLMessage at : database.getMessages()){
			retVal += at.toString() + "\n";
		}
		
		return retVal;
	}
	
	@GET
	@Path("/proba/{alias}")
	public String proba(@PathParam("alias") String alias){
		
		rollback(alias);
		return "done";
	}
// KLIJENT - AGENTSKI CENTAR

	/**
	 * dobavi listu svih tipova agenata na sistemu;
	 */
	
	@GET
	@Path("/agents/types")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AgentType> getAgentTypes(){ 
		return database.getSviTipoviAgenata();
	}
	
	/**
	 * dobavi sve pokrenute agente sa sistema;
	 */
	@GET
	@Path("/agents/running")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AID> getActiveAgents(){
		List<AID> aids = new ArrayList<>();
		for (AgentInterface ai : database.getActiveAgents()) {
			aids.add(ai.getAid());
		}
		return aids;
	}
	
	/**
	 * pokreni agenta određenog tipa sa zadatim imenom;
	 */
	@PUT
	@Path("/agents/running/{type}/{name}")
	public void startAgentByName(@PathParam("type") String type,@PathParam("name") String name){ 
		try {
			Context context = new InitialContext();
			AgentInterface agent = (AgentInterface) context.lookup("java:module/" + type);
			agent.init(new AID(name, database.getAgentskiCentar(), new AgentType(type,null)));
			database.addActiveAgent(agent);	
			context.close();
			
			// salji dodatog svim cvorovima
			for (AgentskiCentar ac : database.getAgentskiCentri()) {
				ResteasyClient client = new ResteasyClientBuilder().build();
				if (!ac.getAddress().equals(database.getAgentskiCentar().getAddress())){
					ResteasyWebTarget target = client.target("http://" + ac.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/agents/running");
					ArrayList<AgentInterface> ai = new ArrayList<AgentInterface>();
					ai.add(agent);
					target.request().post(Entity.entity(ai, MediaType.APPLICATION_JSON));
				}
			}
			
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * zaustavi određenog agenta
	 */
	@DELETE
	@Path("/agents/running/{aid}/{hostName}")
	public void stopAgentByAID(@PathParam("aid") String aid, @PathParam("hostName") String hostName){
		
		for (AgentskiCentar ac : database.getAgentskiCentri()) {
			if (ac.getAddress().equals(database.getAgentskiCentar().getAddress())){
				stopAgentHelper(aid, hostName);
			} else {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target("http://" + ac.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/agents/running/remove/" + aid +  "/" + hostName);
				target.request().delete();
			}
		}
		
	}
	
	@DELETE
	@Path("/agents/running/remove/{aid}/{hostName}")
	public void stopAgentHelper(@PathParam("aid") String aid, @PathParam("hostName") String hostName){
		
		AID agentAID = new AID(aid, database.getAgentskiCentarByName(hostName) , null);
		AgentInterface agent = database.getActiveAgentByAID(agentAID);
		database.removeActiveAgent(agent);
		agent.stop();
	}
	
	/**
	 * pošalji ACL poruku
	 */
	@POST
	@Path("/messages")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendACLMessage(ACLMessage aclMessage){
		// prosledi message na QUEUE
		new JMSQueue(aclMessage);
	}
	
	/**
	 * dobavi sve ACL poruke
	 */
	@GET
	@Path("/messages")
	@Produces(MediaType.TEXT_PLAIN)
	public String getACLMessages(){
		String retVal = "";
		for (ACLMessage aclMessage : database.getMessages()) {
			retVal += "Message from: " + aclMessage.getSender().getName() + " to " 
		+ aclMessage.getReceivers()[0].getName() 
		+ "with performative" + aclMessage.getPerformative() + "/n";
		}
		return retVal;
	}
	
	/**
	 * dobavi listu performativa.
	 */
	@GET
	@Path("/message")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Performative> getPerformatives(){ 
		ArrayList<Performative> retVal = new ArrayList<Performative>();
		for (Performative p : Performative.values()) {
			retVal.add(p);
		}
		return retVal;
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
		
		try {	
			if (database.isMaster()){
				System.out.println("Master cvor primio register od " + agentskiCentar.getAlias());
				System.out.println("Master cvor salje zahtev za spisak podrzavanjih agenata");
				
				// Zahtev za spisak podrzavanih agenata od novog cvora (@GET /agents/classes)
				ResteasyClient client = new ResteasyClientBuilder().build();
				System.out.println("http://" + agentskiCentar.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/agents/classes");
				ResteasyWebTarget target = client.target("http://" + agentskiCentar.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/agents/classes");
				Response response = target.request(MediaType.APPLICATION_JSON).get();
				ArrayList<AgentType> podrzavaniAgenti = (ArrayList<AgentType>) response.readEntity(new GenericType<List<AgentType>>(){});
				database.addSviTipoviAgenata(podrzavaniAgenti);
				
				// Salje se zahtev za register svim cvorovima osim master cvoru i novom cvoru (@POST /node)
				for (AgentskiCentar ac : database.getAgentskiCentri()) {
					
					if (!ac.getAddress().equals(database.getMasterIP()) /*&& !ac.getAddress().equals(agentskiCentar.getAddress()*/){
						target = client.target("http://" + ac.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/node");
						response = target.request().post(Entity.entity(agentskiCentar, MediaType.APPLICATION_JSON));
					}
				}
				database.addAgentskiCentar(agentskiCentar);
				
				// Salje se spisak novih tipova agenata svim cvorovima osim masteru (@POST /agents/classes)
				for (AgentskiCentar ac : database.getAgentskiCentri()) {
					if (!ac.getAddress().equals(database.getMasterIP())){
						target = client.target("http://" + ac.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/agents/classes");
						response = target.request().post(Entity.entity(database.getSviTipoviAgenata(), MediaType.APPLICATION_JSON));
					}
				}
				
				// Salje se spisak aktivnih agenata novom cvoru
				System.out.println("Saljem spisak svih agenata novom cvoru");
				target = client.target("http://" + agentskiCentar.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/agents/running");
				response = target.request().post(Entity.entity(database.getActiveAgents(), MediaType.APPLICATION_JSON));
				
				// Salje se spisak agentskih centara
				System.out.println("Vracam spisak agentskih centara");
				return database.getAgentskiCentri();
				
			} else {
				System.out.println("Slave cvor primio register za cvor " + agentskiCentar.getAlias());
				database.addAgentskiCentar(agentskiCentar);
				return null;
			}
			
		} catch (Exception e){
			System.out.println("Dogodio se exception za register");
			e.printStackTrace();
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
		System.out.println(database.getPodrzaniTipoviAgenata());
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
	@Path("/agents/running")
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendStartedAgents(List<Agent> agents){
		
		System.out.println(agents.toString());
		ArrayList<AgentInterface> ai = database.getAgentInterfaceFromClasses((ArrayList)agents);
		database.addAllActiveAgents(ai);
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
		
		AgentskiCentar ac = database.getAgentskiCentarByName(alias);
		database.removeAllAgentsByAgentskiCentar(ac);
		database.removeAgentskiCentar(ac);
		
		if (database.isMaster()){
			for(AgentskiCentar agentskiCentar : database.getAgentskiCentri()){
				if (!agentskiCentar.getAlias().equals(database.getAgentskiCentar().getAlias())){
					ResteasyClient client = new ResteasyClientBuilder().build();
					ResteasyWebTarget target = client.target("http://" + agentskiCentar.getAddress() + ":8080/AgentiWeb/rest/agentskiCentar/node/" + alias);
					target.request().delete();
				}
			}
		}
	}
	
	/**
	 * Ukoliko se desi da čvor ne odgovori zahtev se izvršava još jednom i ukoliko čvor ni tada ne odgovori smatra se da je ugašen 
	 * i javlja se ostalim čvorovima da izbace zapis o ugašenom čvoru.
	 */
	@GET
	@Path("/node")
	@Produces(MediaType.TEXT_PLAIN)
	public Boolean heartBeat(){ 
		return true;
	}
}
