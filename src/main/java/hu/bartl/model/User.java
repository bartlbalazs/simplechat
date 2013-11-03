package hu.bartl.model;

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

public class User implements Serializable {

	private static final long serialVersionUID = 2543621284115003770L;

	private final Map<User, Conversation> conversations = new LinkedHashMap<User, Conversation>();
	private final List<ConversationListChangedEventHandler> conversationListChangedEventHandlers = new ArrayList<ConversationListChangedEventHandler>();
	private final String id;
	private final String name;
	private final String pictureUrl;

	private User(String id, String name, String pictureUrl) {
		this.id = id;
		this.name = name;
		this.pictureUrl = pictureUrl;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getPictureUrl() {
		return this.pictureUrl;
	}

	public static User create(String id, String name, String pictureUrl) {
		if (!Strings.isNullOrEmpty(id) && !Strings.isNullOrEmpty(name)
				&& !Strings.isNullOrEmpty(pictureUrl)) {
			return new User(id, name, pictureUrl);
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
		if (conversation.isInvolved(this)) {
			User otherUser = conversation.getOtherUser(this);
			if (!conversations.containsKey(otherUser)) {
				conversations.put(otherUser, conversation);
				List<Conversation> startedConversations = buildEmptyList();
				startedConversations.add(conversation);
				fireConversationLisChangedEvent(startedConversations,
						buildEmptyList());
			}
		}
	}

	public void notifyConversationEnded(Conversation conversation) {
		User otherUser = conversation.getOtherUser(this);
		if (conversations.containsKey(otherUser)) {
			List<Conversation> startedConversations = buildEmptyList();
			List<Conversation> endedConversations = buildEmptyList();
			endedConversations.add(conversation);
			conversations.remove(otherUser);
			fireConversationLisChangedEvent(startedConversations,
					endedConversations);
		}

	}

	private List<Conversation> buildEmptyList() {
		return new ArrayList<Conversation>();
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

		conversations.clear();
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

	public Conversation getConversationByName(String conversationName) {
		if (Strings.isNullOrEmpty(conversationName)) {
			return null;
		}

		Item conversationItem = this.getConversationContainer().getItem(
				conversationName);

		if (conversationItem == null) {
			return null;
		}

		return (Conversation) conversationItem.getItemProperty("conversation")
				.getValue();
	}

	// TODO: maybe we should be more polite
	public void kill() {
		clearReferences();
	}

	@Override
	public String toString() {
		return "User [name=" + getName() + "]";
	}
}
