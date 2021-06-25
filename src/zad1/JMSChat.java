package zad1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JMSChat {

	private static String idChat = "Anonim";

	private static TopicConnectionFactory connectionFactory;
	private static TopicConnection connection;
	private static TopicSession session;
	private static TopicPublisher publisher;
	private static Topic topic;
	private static BufferedReader bf;

	public JMSChat(String idChat) {
		this.idChat = idChat;
		System.out.println("Chat" + idChat + "\n");
		try {
			runJMSChat(init());
		} catch (NamingException | JMSException | IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private static void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
			if (bf != null) {
				try {
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void runJMSChat(TopicSubscriber subscriber) throws JMSException, IOException, InterruptedException {
		String msg = "";
		connection.start();

		do {
			if (!msg.isEmpty()) {
				sendMessage(publisher, session, idChat, msg);
			}
			Thread.sleep(100);
			getMessage(subscriber, idChat);
		} while (!"bye".equals(msg = bf.readLine()));
		System.out.println("Koniec chatu");
		connection.close();
		System.exit(0);
	}

	private static TopicSubscriber init() throws NamingException, JMSException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
		properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");

		Context ctx = new InitialContext(properties);
		connectionFactory = (TopicConnectionFactory) ctx.lookup("ConnectionFactory");
		connection = connectionFactory.createTopicConnection();
		session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		topic = (Topic) ctx.lookup("topic1");
		publisher = session.createPublisher(topic);
		TopicSubscriber subscriber = session.createSubscriber(topic);
		bf = new BufferedReader(new InputStreamReader(System.in));
		return subscriber;
	}

	private static void getMessage(TopicSubscriber subscriber, String idChat) throws JMSException {
		subscriber.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message arg0) {

				TextMessage msg = (TextMessage) arg0;

				System.out.println(idChat + " odbiera " + msg);

			}
		});
	}

	private static void sendMessage(TopicPublisher publisher, TopicSession session, String id, String msg2)
			throws IOException, JMSException {
		publisher.publish(session.createTextMessage("\n" + id + " pisze: " + msg2));

	}

	public String getChatId() {
		return idChat;
	}

	private List<ChatListener> listeners = new ArrayList<ChatListener>();

	public void addListener(ChatListener toAdd) {
		listeners.add(toAdd);
	}

	public void onNewMessage(String message) {
		for (ChatListener l : listeners)
			l.newMessageReceived(message);
	}

}