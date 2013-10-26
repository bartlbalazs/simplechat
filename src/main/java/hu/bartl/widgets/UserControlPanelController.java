package hu.bartl.widgets;

import hu.bartl.backend.Backend;
import hu.bartl.backend.Backend.UserListChangedEvent;
import hu.bartl.backend.Backend.UserListChangedEventHandler;
import hu.bartl.model.Conversation;
import hu.bartl.model.Conversation.MessageAddedEvent;
import hu.bartl.model.Conversation.MessageAddingListener;
import hu.bartl.model.User;
import hu.bartl.model.User.ConversationListChangedEvent;
import hu.bartl.model.User.ConversationListChangedEventHandler;
import hu.bartl.widgets.UserControlPanel.ConversationSelectionEvent;
import hu.bartl.widgets.UserControlPanel.ConversationSelectionHandler;
import hu.bartl.widgets.UserControlPanel.UserSelectionEvent;
import hu.bartl.widgets.UserControlPanel.UserSelectionHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Component;

public class UserControlPanelController implements Serializable {
	private static final long serialVersionUID = 2620430098939630286L;

	private final List<UserEventHandler> userEventHandlers = new ArrayList<UserEventHandler>();

	private final UserControlPanel userControlPanel;
	private final Backend backend;
	private final User user;

	public static UserControlPanelController create(User user) {
		if (user == null) {
			throw new NullPointerException();
		}
		return new UserControlPanelController(user);
	}

	private UserControlPanelController(User user) {
		this.userControlPanel = UserControlPanel.Create();
		this.backend = Backend.getInstance();
		this.user = user;
		initialize();
	}

	private void initialize() {
		initializeBackend();
		initializeUserPanel();
	}

	private void initializeBackend() {
		backend.addUserListChangedEventHandler(new UserListChangedEventHandler() {
			private static final long serialVersionUID = 1208710333589671848L;

			@Override
			public void handleEvent(UserListChangedEvent event) {
				userControlPanel.setUsers(backend.getUserContainer());
				userControlPanel.markAsDirtyRecursive();
				fireUserEvent(UserEventType.USER_LIST_CHANGED);
			}
		});
	}

	private void initializeUserPanel() {
		userControlPanel.setUserName(user.getName());
		initializeUserList();
		initializeConversationList();
	}

	private void initializeUserList() {
		setUserListDataSource();
		userControlPanel.addUserSelectionHandler(new UserSelectionHandler() {

			@Override
			public void handle(UserSelectionEvent event) {
				User selectedUser = backend.getUser(event.getUserName());
				user.startConversation(selectedUser);

			}
		});
	}

	private void setUserListDataSource() {
		userControlPanel.setUsers(backend.getUserContainer());
	}

	private void initializeConversationList() {
		setConversationListDataSource();
		addConversationSelectionHandling();
		addConversationRefreshHandling();
	}

	private void setConversationListDataSource() {
		userControlPanel.setConversations(user.getConversationContainer());
	}

	private void addConversationSelectionHandling() {
		userControlPanel
				.addConversationSelectionHandler(new ConversationSelectionHandler() {

					@Override
					public void handle(ConversationSelectionEvent event) {

						String selectedConversationName = event
								.getConversationName();

						final Conversation conversation = user
								.getConversationByName(selectedConversationName);

						initializeConversationView(conversation);

						Component newContent = conversation
								.getViewComponent(user);

						fireUserEvent(UserEventType.CONVERSATION_SELECTED,
								newContent);

					}
				});
	}

	private void initializeConversationView(Conversation conversation) {
		if (conversation != null && !conversation.hasViewComponent(user)) {
			conversation.attachViewComponentForUser(user);
			conversation.addMessageAddingListener(new MessageAddingListener() {

				@Override
				public void handleEvent(MessageAddedEvent event) {
					fireUserEvent(UserEventType.MESSAGE_RECEIVED);
				}
			});
		}
	}

	private void addConversationRefreshHandling() {
		user.addConversationListChangedEventHandler(new ConversationListChangedEventHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleEvent(ConversationListChangedEvent event) {
				setConversationListDataSource();
				fireUserEvent(UserEventType.CONVERSATION_LIST_CHANGED);
			}
		});
	}

	public void addUserEventHandler(UserEventHandler handler) {
		if (handler != null) {
			userEventHandlers.add(handler);
		}
	}

	public Component getComponent() {
		return this.userControlPanel;
	}

	public void removeUserEventHandler(UserEventHandler handler) {
		if (userEventHandlers.contains(handler)) {
			userEventHandlers.remove(handler);
		}
	}

	private void fireUserEvent(UserEventType eventType) {
		fireUserEvent(eventType, null);
	}

	private void fireUserEvent(UserEventType eventType, Component newContent) {
		UserEvent event = (newContent == null) ? new UserEvent(eventType)
				: new UserEvent(eventType, newContent);

		for (UserEventHandler handler : userEventHandlers) {
			handler.handle(event);
		}
	}

	public class UserEvent {
		private final UserEventType type;
		private final Component newContent;

		public UserEvent(UserEventType type) {
			this.type = type;
			this.newContent = null;
		}

		public UserEvent(UserEventType type, Component newContent) {
			this.type = type;
			this.newContent = newContent;
		}

		public UserEventType getType() {
			return this.type;
		}

		public Component getNewContent() {
			return this.newContent;
		}
	}

	public enum UserEventType {
		USER_LIST_CHANGED, CONVERSATION_LIST_CHANGED, CONVERSATION_SELECTED, MESSAGE_RECEIVED
	}

	public interface UserEventHandler {
		void handle(UserEvent event);
	}
}
