package zad1;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.jms.JMSException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class GUI {
	private JMSChat jmsChat;
	private String idChat;

	private JFrame mainFrame;
	private JTextArea chatBox;// all chat messages, not editable
	private JTextField messageBox;// write message box, editable
	private JButton btnSend;// btn to send messages

	public GUI(JMSChat jmsChat) {

		this.jmsChat = jmsChat;
		jmsChat.addListener(new ChatListener() {
			@Override
			public void newMessageReceived(String id, String message) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SwingUtilities.invokeLater(() -> {
							chatBox.append("<" + id + ">:  " + message + "\n");
							messageBox.setText("");
						});
					}
				});
			}
		});
		displayGUI();
	}

	public void displayGUI() {
		mainFrame = new JFrame(jmsChat.getChatId());
		chatBox = new JTextArea();
		chatBox.setEditable(false);
		JPanel panel = new JPanel();
		panel.add(chatBox);

		messageBox = new JTextField(30);
		btnSend = new JButton("Send Message");
		btnSend.addActionListener(new sendMessageButtonListener());
		mainFrame.add(panel, BorderLayout.NORTH);
		mainFrame.add(btnSend, BorderLayout.SOUTH);
		mainFrame.add(messageBox, BorderLayout.CENTER);
		// chatBox.setLineWrap(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(300, 300);
		mainFrame.setVisible(true);
	}

	class sendMessageButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String msg  = messageBox.getText().trim();
			if (msg.length() < 1) {
				// do nothing
			} else if (msg.equals(".clear")) {
				chatBox.setText("Cleared all messages\n");
				messageBox.setText("");
			} else {
				try {
					jmsChat.sendMessage(msg);
					//chatBox.append("<" + jmsChat.getChatId() + ">:  " + messageBox.getText() + "\n");
					messageBox.setText("");
				}catch (JMSException e){
					e.printStackTrace();
				}
			}
		}
	}

}