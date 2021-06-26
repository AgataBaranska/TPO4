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

public class JMSChat implements MessageListener {

	private  String idChat = "Anonim";

	private  TopicConnectionFactory connectionFactory;
	private  TopicConnection connection;
	private  TopicSession session;
	private  TopicPublisher publisher;
	private TopicSubscriber subscriber;
	private  Topic topic;
	private  BufferedReader bf;
	private List<ChatListener> listeners = new ArrayList<ChatListener>();
	
	public JMSChat(String idChat) {
		this.idChat = idChat;
		System.out.println("Chat " + idChat + "\n");
		try {
			init();
			start();
		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void addListener(ChatListener toAdd) {
		listeners.add(toAdd);
	}

	protected void onNewMessage(String id, String message) {
		for (ChatListener l : listeners)
			l.newMessageReceived(id, message);
	}
	private void close() {
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

	private void start() throws JMSException {
		connection.start();
	}

	private void init() throws NamingException, JMSException {
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

		subscriber.setMessageListener(this);
		this.subscriber = subscriber;
	}

	public void sendMessage(String msg) throws JMSException {
		TextMessage message = session.createTextMessage(msg);
		message.setStringProperty("userId", getChatId());
		publisher.publish(message);

	}

	public String getChatId() {
		return idChat;
	}


	@Override
	public void onMessage(Message message) {
		try {
			TextMessage msg = (TextMessage) message;
			System.out.println(idChat + " odbiera " + msg);
			String userId = message.getStringProperty("userId");
			onNewMessage(userId, msg.getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}