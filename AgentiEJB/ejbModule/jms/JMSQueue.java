package jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import model.ACLMessage;

/**
 * 
 * @author minja
 *
 */

public class JMSQueue {
	public JMSQueue(ACLMessage aclMessage) {
		try {
			System.out.println("JMSQUEUE");
			Context context = new InitialContext();
			
			ConnectionFactory cf = (ConnectionFactory) context.lookup("java:jboss/exported/jms/RemoteConnectionFactory");
		    final Queue queue = (Queue) context.lookup("java:jboss/exported/jms/queue/mojQueue");
		    context.close();
				   
			Connection connection = cf.createConnection("guest", "guestguest");
			final Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			connection.start();

		    ObjectMessage msg = session.createObjectMessage(aclMessage);
		    // The sent timestamp acts as the message's ID
		    long sent = System.currentTimeMillis();
		    msg.setLongProperty("sent", sent);
		    //msg.setStringProperty("type", type);
		    
			MessageProducer producer = session.createProducer(queue);
			//producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			//Thread.sleep(1000);
			producer.send(msg);
			
			System.out.println("Message published. Please check application server's console to see the response from MDB.");

			producer.close();
			session.close();
			connection.close();
		    
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}









