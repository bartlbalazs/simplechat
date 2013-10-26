package hu.bartl.model;

import hu.bartl.MyVaadinUI.UiCloseEvent;
import hu.bartl.MyVaadinUI.UiCloseListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class User implements UiCloseListener {
	private final String name;

	private final Map<User, Conversation> conversations = new LinkedHashMap<User, Conversation>();

	private final List<ConversationListChangedEventHandler> conversationListChangedEventHandlers = new ArrayList<ConversationListChangedEventHandler>();

	private User(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static User create(String name) {
		if (!Strings.isNullOrEmpty(name)) {
			return new User(name);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void startConversation(User user) {
		if (user != this) {
			Conversation newConversation = Conversation.create(this, user);
			conversations.put(user, newConversation);
		}

	}

	public void endConversation(User user) {
		if (conversations.containsKey(user)) {
			conversations.remove(user);
		}
	}

	public void notifyConversationStarted(Conversation conversation) {
		if (this.equals(conversation.getUser1())) {
			if (!conversations.containsKey(conversation.getUser2())) {
				conversations.put(conversation.getUser2(), conversation);
				List<Conversation> startedConversations = new ArrayList<Conversation>();
				startedConversations.add(conversation);
				fireConversationLisChangedEvent(startedConversations,
						new ArrayList<Conversation>());
			}
		}

		if (this.equals(conversation.getUser2())) {
			if (!conversations.containsKey(conversation.getUser1())) {
				conversations.put(conversation.getUser1(), conversation);
				List<Conversation> endedConversations = new ArrayList<Conversation>();
				endedConversations.add(conversation);
				fireConversationLisChangedEvent(new ArrayList<Conversation>(),
						endedConversations);
			}
		}
	}

	public void notifyConversationEnded(Conversation conversation) {
		if (this.equals(conversation.getUser1())) {
			if (conversations.containsKey(conversation.getUser2())) {
				conversations.remove(conversation.getUser2());
			}
		}

		if (this.equals(conversation.getUser2())) {
			if (conversations.containsKey(conversation.getUser1())) {
				conversations.remove(conversation.getUser1());
			}
		}
	}

	public void addConversationListChangedEventHandler(
			ConversationListChangedEventHandler handler) {
		if (handler != null) {
			this.conversationListChangedEventHandlers.add(handler);
		}
	}

	public void removeConversationListChangedEventHandler(
			ConversationListChangedEventHandler handler) {
		if (handler != null
				&& this.conversationListChangedEventHandlers.contains(handler)) {
			this.conversationListChangedEventHandlers.remove(handler);
		}
	}

	private void clearReferences() {
		for (Entry<User, Conversation> conversationEntry : conversations
				.entrySet()) {
			Conversation conversation = conversationEntry.getValue();
			if (conversation != null) {
				conversation.end();
			}
		}

		conversationListChangedEventHandlers.clear();
	}

	private void fireConversationLisChangedEvent(
			List<Conversation> startedConversations,
			List<Conversation> endedConversations) {
		ConversationListChangedEvent event = new ConversationListChangedEvent(
				startedConversations, endedConversations);

		for (ConversationListChangedEventHandler handler : this.conversationListChangedEventHandlers) {
			handler.handleEvent(event);
		}
	}

	public class ConversationListChangedEvent implements Serializable {
		private static final long serialVersionUID = -3777134377567604478L;
		private final List<Conversation> startedConversation;
		private final List<Conversation> endedConversation;

		public ConversationListChangedEvent(
				List<Conversation> startedConversation,
				List<Conversation> endedConversation) {
			this.startedConversation = startedConversation;
			this.endedConversation = endedConversation;
		}

		public List<Conversation> getStartedConversation() {
			return startedConversation;
		}

		public List<Conversation> getEndedConversation() {
			return endedConversation;
		}
	}

	public interface ConversationListChangedEventHandler extends Serializable {
		void handleEvent(ConversationListChangedEvent event);
	}

	@SuppressWarnings("unchecked")
	public Container getConversationContainer() {
		IndexedContainer conversationContainer = new IndexedContainer();
		conversationContainer.addContainerProperty("conversation",
				Conversation.class, null);

		for (Entry<User, Conversation> conversationEntry : conversations
				.entrySet()) {
			Item newItem = conversationContainer.addItem(conversationEntry
					.getKey().getName());
			newItem.getItemProperty("conversation").setValue(
					conversationEntry.getValue());
		}
		return conversationContainer;
	}

	@Override
	public void onUiClose(UiCloseEvent event) {
		clearReferences();
	}
}
