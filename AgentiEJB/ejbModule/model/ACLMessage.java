package model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

public class ACLMessage implements Serializable {

	private static final long serialVersionUID = 3490319159379449813L;
	
	private Performative performative;
	private AID sender;
	private AID[] recieves;
	private AID replyTo;
	private String content;
	private Object contentObj;
	private HashMap<String,Object> userArgs;
	private String language;
	private String encoding;
	private String ontology;
	private String protocol;
	private String conversationID;
	private String replyWith;
	private Long replyBy;
	
	public ACLMessage() {
		super();
	}

	public Performative getPerformative() {
		return performative;
	}

	public void setPerformative(Performative performative) {
		this.performative = performative;
	}

	public AID getSender() {
		return sender;
	}

	public void setSender(AID sender) {
		this.sender = sender;
	}

	public AID[] getRecieves() {
		return recieves;
	}

	public void setRecieves(AID[] recieves) {
		this.recieves = recieves;
	}

	public AID getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(AID replyTo) {
		this.replyTo = replyTo;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Object getContentObj() {
		return contentObj;
	}

	public void setContentObj(Object contentObj) {
		this.contentObj = contentObj;
	}

	public HashMap<String, Object> getUserArgs() {
		return userArgs;
	}

	public void setUserArgs(HashMap<String, Object> userArgs) {
		this.userArgs = userArgs;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getConversationID() {
		return conversationID;
	}

	public void setConversationID(String conversationID) {
		this.conversationID = conversationID;
	}

	public String getReplyWith() {
		return replyWith;
	}

	public void setReplyWith(String replyWith) {
		this.replyWith = replyWith;
	}

	public Long getReplyBy() {
		return replyBy;
	}

	public void setReplyBy(Long replyBy) {
		this.replyBy = replyBy;
	}

	@Override
	public String toString() {
		return "ACLPoruka [performative=" + performative + ", sender=" + sender + ", recieves="
				+ Arrays.toString(recieves) + ", replyTo=" + replyTo + ", content=" + content + ", contentObj="
				+ contentObj + ", userArgs=" + userArgs + ", language=" + language + ", encoding=" + encoding
				+ ", ontology=" + ontology + ", protocol=" + protocol + ", conversationID=" + conversationID
				+ ", replyWith=" + replyWith + ", replyBy=" + replyBy + "]";
	}
	
	
	
	
}
