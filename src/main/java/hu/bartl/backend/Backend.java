package hu.bartl.backend;

import hu.bartl.MyVaadinUI.UiCloseEvent;
import hu.bartl.MyVaadinUI.UiCloseListener;
import hu.bartl.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class Backend implements UiCloseListener, Serializable {

	private static final long serialVersionUID = 3433634884328428300L;

	private static Backend instance = null;
	private static Object mutex = new Object();

	private Backend() {
	}

	public static Backend getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null)
					instance = new Backend();
			}
		}
		return instance;
	}

	private final List<UserListChangedEventHandler> userListChangedEventHandlers = new ArrayList<UserListChangedEventHandler>();

	private final Map<String, User> users = new LinkedHashMap<String, User>();

	public void addUser(User user) {
		if (user != null) {
			users.put(user.getName(), user);
			List<User> addedUsers = new ArrayList<User>();
			addedUsers.add(user);
			fireUserLisChangedEvent(addedUsers, new ArrayList<User>());
		}
	}

	public void removeUser(User user) {
		if (users.containsKey(user.getName())) {
			users.remove(user);
			List<User> removedUsers = new ArrayList<User>();
			removedUsers.add(user);
			fireUserLisChangedEvent(new ArrayList<User>(), removedUsers);
		}
	}

	public User getUser(String userName) {
		return users.get(userName);
	}

	@SuppressWarnings("unchecked")
	public Container getUserContainer() {
		IndexedContainer userContainer = new IndexedContainer();
		userContainer.addContainerProperty("user", User.class, null);

		for (Entry<String, User> userEntry : users.entrySet()) {
			Item newItem = userContainer.addItem(userEntry.getKey());
			newItem.getItemProperty("user").setValue(userEntry.getValue());
		}
		return userContainer;
	}

	private void fireUserLisChangedEvent(List<User> addedUsers,
			List<User> removedUsers) {
		UserListChangedEvent event = new UserListChangedEvent(addedUsers,
				removedUsers);

		for (UserListChangedEventHandler handler : this.userListChangedEventHandlers) {
			handler.handleEvent(event);
		}
	}

	public void addUserListChangedEventHandler(
			UserListChangedEventHandler handler) {
		if (handler != null) {
			this.userListChangedEventHandlers.add(handler);
		}
	}

	public void removeUserListChangedEventHandler(
			UserListChangedEventHandler handler) {
		if (handler != null
				&& this.userListChangedEventHandlers.contains(handler)) {
			this.userListChangedEventHandlers.remove(handler);
		}
	}

	public class UserListChangedEvent implements Serializable {
		private static final long serialVersionUID = -3777134377567604478L;
		private final List<User> addedUsers;
		private final List<User> removedUsers;

		public UserListChangedEvent(List<User> addedUsers,
				List<User> removedUsers) {
			this.addedUsers = addedUsers;
			this.removedUsers = removedUsers;
		}

		public List<User> getAddedUsers() {
			return addedUsers;
		}

		public List<User> getRemovedUsers() {
			return this.removedUsers;
		}

	}

	public interface UserListChangedEventHandler extends Serializable {
		void handleEvent(UserListChangedEvent event);
	}

	@Override
	public void onUiClose(UiCloseEvent event) {
		removeUser(event.getAttachedUser());
	}
}
