package jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/*
 * Prvo probati sa queue/mojQueue2. Posto u tom slucaju ovaj MDB 
 * ne "hvata" poruke iz JMSQueue aplikacije, queue se prazni preko 
 * nje. Ako i ovaj bean prima iz istog reda (mojQueue), onda
 * sama aplikacija nece ni stici da dobije poruku (MDB) ce je prvi
 * "pojesti"
 * 
 * Osim toga, probati i da JMSQueue ostane upaljena, a da se 
 * startuje jos jedna instanca iste aplikacije (dok MDB ne "hvata"
 * poruke). Videcemo da queue polako raste (sa svakim startovanjem
 * aplikacije).
 * 
 */


@MessageDriven(activationConfig =
{
  @ActivationConfigProperty(propertyName="destinationType",
    propertyValue="javax.jms.Queue"),
  @ActivationConfigProperty(propertyName="destination",
    propertyValue="java:jboss/exported/jms/queue/mojQueue")
})

public class PrimalacQueueMDB implements MessageListener {
	
	public void onMessage (Message msg) {
		try {
			TextMessage tmsg = (TextMessage) msg;
			try {
				String text = tmsg.getText();
				long time = tmsg.getLongProperty("sent");
				String type = tmsg.getStringProperty("type");
	  
				switch(type) {
					case "login": {
						
					}
				}	  
				System.out.println("Received new message from Queue : " + text + ", with timestamp: " + time);
			} catch (JMSException e) {
				e.printStackTrace();
			}
	    } catch (Exception e) {
	    	e.printStackTrace ();
	    }
	}

}