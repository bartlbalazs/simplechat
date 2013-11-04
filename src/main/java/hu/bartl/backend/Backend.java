package hu.bartl.backend;

import hu.bartl.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class Backend implements Serializable {

	private static final long serialVersionUID = 3433634884328428300L;

	private static Backend instance = null;
	private static Object mutex = new Object();
	private final Logger logger = Logger.getLogger(Backend.class.getName());

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

	public void createUser(String id, String name, String pictureUrl) {
		if (users.containsKey(id)) {
			logger.warning("failed to create user: " + id + " " + name + " "
					+ pictureUrl);
			throw new UserAlreadyExistsException(id);
		}
		addUser(User.create(id, name, pictureUrl));
	}

	private void addUser(User user) {
		users.put(user.getId(), user);
		logger.info(user + " added successfully.");
		fireUserAddedEvent(user);
	}

	public void destroyUser(User user) {
		if (user != null) {
			user.kill();
			removeUser(user);
		}
	}

	private void removeUser(User user) {
		users.remove(user.getId());
		logger.info(user + "destroyed");
		fireUserRemovedEvent(user);
	}

	public User getUser(String userId) {
		return users.get(userId);
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

	private void fireUserAddedEvent(User user) {
		fireUserLisChangedEvent(user, null);
	}

	private void fireUserRemovedEvent(User user) {
		fireUserLisChangedEvent(null, user);
	}

	private void fireUserLisChangedEvent(User addedUser, User removedUser) {
		UserListChangedEvent event = new UserListChangedEvent(addedUser,
				removedUser);

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
		private final User addedUser;
		private final User removedUser;

		public UserListChangedEvent(User addedUser, User removedUser) {
			this.addedUser = addedUser;
			this.removedUser = removedUser;
		}

		public User getAddedUsers() {
			return addedUser;
		}

		public User getRemovedUsers() {
			return this.removedUser;
		}

	}

	public interface UserListChangedEventHandler extends Serializable {
		void handleEvent(UserListChangedEvent event);
	}

	public class UserAlreadyExistsException extends RuntimeException {
		private static final long serialVersionUID = 1977103856457900816L;
		private final String userId;

		public UserAlreadyExistsException(String userId) {
			this.userId = userId;
		}

		public String getUserId() {
			return this.userId;
		}
	}
}
