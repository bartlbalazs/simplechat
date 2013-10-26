package hu.bartl.widgets;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;

//TODO: to thinking about a more appropriate super class
public class UserControlPanel extends VerticalLayout {

	private static final long serialVersionUID = -4970485099486962596L;

	public static final int WIDTH_PX_INT = 200;

	private final List<UserSelectionHandler> userSelectionHandlers = new ArrayList<UserSelectionHandler>();
	private final List<ConversationSelectionHandler> conversationSelectionHandlers = new ArrayList<ConversationSelectionHandler>();

	private final Label userName = new Label();
	private final ListSelect userList = new ListSelect();
	private final ListSelect conversationList = new ListSelect();

	public static UserControlPanel Create() {
		return new UserControlPanel();
	}

	private UserControlPanel() {
		build();
	}

	private void build() {
		buildConversationlist();
		buildUserList();
		buildMainLayout();
	}

	private void buildConversationlist() {
		conversationList.setCaption("Beszélgetések");
		conversationList.setHeight(100, Unit.PERCENTAGE);
		conversationList.setWidth(200, Unit.PIXELS);
		conversationList.setImmediate(true);
		conversationList.setNullSelectionAllowed(true);
		addConversationSelectionListener();
	}

	private void buildUserList() {
		userList.setCaption("Felhasználók");
		userList.setHeight(100, Unit.PERCENTAGE);
		userList.setWidth(200, Unit.PIXELS);
		userList.setNullSelectionAllowed(true);
		userList.setImmediate(true);
		addUserSelectionListener();
	}

	private void buildMainLayout() {
		this.addComponent(userName);
		this.addComponent(conversationList);
		this.addComponent(userList);
		this.setExpandRatio(conversationList, 1);
		this.setExpandRatio(userList, 3);
		this.setWidth(WIDTH_PX_INT, Unit.PIXELS);
		this.setHeight(100, Unit.PERCENTAGE);
	}

	private void addConversationSelectionListener() {
		conversationList.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = -6928542442278479964L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				String selectedConversationName = event.getProperty()
						.getValue().toString();
				ConversationSelectionEvent conversationSelectionEvent = new ConversationSelectionEvent(
						selectedConversationName);
				fireConversationSelectionEvent(conversationSelectionEvent);

			}
		});
	}

	private void addUserSelectionListener() {
		userList.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = -6928542442278479964L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				String selectedUserName = event.getProperty().getValue()
						.toString();
				UserSelectionEvent userEvent = new UserSelectionEvent(
						selectedUserName);
				fireUserSelectionEvent(userEvent);
			}
		});
	}

	public void setUserName(String userName) {
		this.userName.setCaption(userName);
	}

	public void setUsers(Container userContainer) {
		userList.setContainerDataSource(userContainer);
	}

	public void setConversations(Container conversationContainer) {
		conversationList.setContainerDataSource(conversationContainer);
	}

	public void addConversationSelectionHandler(
			ConversationSelectionHandler handler) {
		if (handler != null) {
			conversationSelectionHandlers.add(handler);
		}
	}

	public void removeConversationSelectionHandler(
			ConversationSelectionHandler handler) {
		if (handler != null && conversationSelectionHandlers.contains(handler)) {
			conversationSelectionHandlers.remove(handler);
		}
	}

	public void addUserSelectionHandler(UserSelectionHandler handler) {
		if (handler != null) {
			userSelectionHandlers.add(handler);
		}
	}

	public void removeUserSelectionHandler(UserSelectionHandler handler) {
		if (handler != null && userSelectionHandlers.contains(handler)) {
			userSelectionHandlers.remove(handler);
		}
	}

	private void fireUserSelectionEvent(UserSelectionEvent event) {
		for (UserSelectionHandler handler : userSelectionHandlers) {
			handler.handle(event);
		}
	}

	private void fireConversationSelectionEvent(ConversationSelectionEvent event) {
		for (ConversationSelectionHandler handler : conversationSelectionHandlers) {
			handler.handle(event);
		}
	}

	public class UserSelectionEvent {
		private final String userName;

		public UserSelectionEvent(String userName) {
			this.userName = userName;
		}

		public String getUserName() {
			return this.userName;
		}
	}

	public interface UserSelectionHandler {
		void handle(UserSelectionEvent event);
	}

	public class ConversationSelectionEvent {
		private final String conversation;

		public ConversationSelectionEvent(String conversationName) {
			this.conversation = conversationName;
		}

		public String getConversationName() {
			return this.conversation;
		}
	}

	public interface ConversationSelectionHandler {
		void handle(ConversationSelectionEvent event);
	}
}
