package websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import model.AgentInterface;
import model.AgentType;
import model.Performative;
import rest.AgentskiCentarREST;

@ServerEndpoint("/websocket")
@Stateful
public class WSManager {
	Logger log = Logger.getLogger("Websockets endpoint");
	
	List<Session> sessions = new ArrayList<Session>();
	
	@EJB
	AgentskiCentarREST agentskiCentar;
	
	public WSManager() {
		super();
	}
	
	@OnOpen
	public void onOpen(Session session) {
		if (!sessions.contains(session)) {
			sessions.add(session);
			log.info("Dodao sesiju: " + session.getId() + " u endpoint-u: " + this.hashCode() + ", ukupno sesija: " + sessions.size());
		}
	}
	
	@OnMessage
	public void echoTextMessage(Session session, String msg, boolean last) {
		try {
			if (session.isOpen()) {
				log.info("Websocket endpoint: " + this.hashCode() + " primio: " + msg + " u sesiji: " + session.getId());
				JSONObject jsonMsg = new JSONObject(msg);
				for (Session s : sessions) {
					if (s.getId().equals(session.getId())) {
						switch ((String)jsonMsg.get("type")) {
							case "getPerformative": {
								List<Performative> performative = agentskiCentar.getPerformatives();
								JSONObject jsonObj = new JSONObject();
								JSONArray jsonArray = new JSONArray();
								for (Performative p : performative) {
									jsonArray.put(p);
								}
								jsonObj.put("data", jsonArray);
								jsonObj.put("type", "performative");
								s.getBasicRemote().sendText(jsonObj.toString());
								break;
							}
							case "getTypes": {
								List<AgentType> tipovi = agentskiCentar.getAgentTypes();
								JSONObject jsonObj = new JSONObject();
								JSONArray jsonArray = new JSONArray();
								for(AgentType at : tipovi){
									jsonArray.put(new JSONObject(at));
								}
								jsonObj.put("data", jsonArray);
								jsonObj.put("type", "types");
								s.getBasicRemote().sendText(jsonObj.toString());
								break;
							}
							case "getActive": {
								List<AgentInterface> aktivni = agentskiCentar.getActiveAgents();
								JSONObject jsonObj = new JSONObject();
								JSONArray jsonArray = new JSONArray();
								for(AgentInterface ai : aktivni){
									jsonArray.put(new JSONObject(ai));
								}		
								jsonObj.put("data", jsonArray);
								jsonObj.put("type", "active");
								s.getBasicRemote().sendText(jsonObj.toString());
								break;
							}
							case "activateAgent": {
								String type = jsonMsg.getString("agentType");
								String name = jsonMsg.getString("name");
								agentskiCentar.startAgentByName(type, name);								
								break;
							}
							case "deactivateAgent": {
								String aid = jsonMsg.getString("name");
								String hostName = jsonMsg.getString("alias");
								agentskiCentar.stopAgentByAID(aid, hostName);
								break;
							}
						}
//						ResteasyClient client = new ResteasyClientBuilder().build();
//				        ResteasyWebTarget target = client.target("http://"+database.getMasterIP()+":8080/AgentiWeb/rest/something");
//				        Response response = target.request().get();
//				        String ret = response.readEntity(String.class);	
//				        System.out.println(ret);
//						s.getBasicRemote().sendText("login;"+ret, last);
//						log.info("Sending '" + ret + "' to : " + s.getId());
						
					}
				}
			}
		} catch (IOException e) {
			try {
				session.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (JSONException e) {
			System.out.println("Doslo je do greske prilikom parsiranja JSON stringa");
			e.printStackTrace();
		}
	}

	@OnClose
	public void close(Session session) {
		sessions.remove(session);
		log.info("Zatvorio: " + session.getId() + " u endpoint-u: " + this.hashCode());
	}
	
	@OnError
	public void error(Session session, Throwable t) {
		sessions.remove(session);
		log.log(Level.SEVERE, "Greška u sessiji: " + session.getId() + " u endpoint-u: " + this.hashCode() + ", tekst: " + t.getMessage());
		t.printStackTrace();
	}
}
