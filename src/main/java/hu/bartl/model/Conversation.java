package hu.bartl.model;

import hu.bartl.widgets.ConversationPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;

public class Conversation {
	private User user1;
	private User user2;
	private final List<Message> messages = new ArrayList<Message>();
	private final Map<User, ConversationPanel> conversationPanels = new HashMap<User, ConversationPanel>();
	private final List<MessageAddingListener> listeners = new ArrayList<Conversation.MessageAddingListener>();

	public static Conversation create(User user1, User user2) {
		if (user1 != null && user2 != null && user1 != user2) {
			return new Conversation(user1, user2);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private Conversation(User user1, User user2) {
		this.user1 = user1;
		this.user2 = user2;
		start();
	}

	public void start() {
		user1.notifyConversationStarted(this);
		user2.notifyConversationStarted(this);
	}

	public void end() {
		user1.notifyConversationEnded(this);
		user2.notifyConversationEnded(this);
		clearReferences();
	}

	public User getUser1() {
		return user1;
	}

	public User getUser2() {
		return user2;
	}

	public List<Message> getMessages() {
		return messages;
	}

	private void clearReferences() {
		user1 = null;
		user2 = null;

		listeners.clear();
	}

	public void addMessage(Message message) {
		if (message != null) {
			this.messages.add(message);
			for (Entry<User, ConversationPanel> panelEntry : conversationPanels
					.entrySet()) {
				ConversationPanel panel = panelEntry.getValue();
				panel.addMessage(message.getSender().getName(),
						message.getContent());
			}
			fireMessageAddedEvent(new MessageAddedEvent(message));
		}
	}

	public void attachViewComponentForUser(User user) {
		if (!hasViewComponent(user)) {
			this.conversationPanels.put(user, buildPanelForUser(user));
		}
	}

	private ConversationPanel buildPanelForUser(final User user) {
		ConversationPanel newPanel = ConversationPanel.create();
		for (Message message : messages) {
			newPanel.addMessage(message.getSender().getName(),
					message.getContent());
		}
		newPanel.addInputListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				String messageContent = event.getProperty().getValue()
						.toString();
				if (!messageContent.equals("")) {
					Conversation.this.addMessage(Message.Create(user,
							messageContent));
				}
			}
		});
		return newPanel;
	}

	public boolean hasViewComponent(User user) {
		return conversationPanels.containsKey(user);
	}

	public ConversationPanel getViewComponent(User user) {
		return conversationPanels.get(user);
	}

	public void addMessageAddingListener(MessageAddingListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public interface MessageAddingListener {
		void handleEvent(MessageAddedEvent event);
	}

	private void fireMessageAddedEvent(MessageAddedEvent event) {
		for (MessageAddingListener listener : listeners) {
			listener.handleEvent(event);
		}
	}

	public class MessageAddedEvent {
		private final Message message;

		public MessageAddedEvent(Message message) {
			this.message = message;
		}

		public Message getMessage() {
			return this.message;
		}
	};
}
