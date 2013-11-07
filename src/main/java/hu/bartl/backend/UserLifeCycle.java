package hu.bartl.backend;

import hu.bartl.authentication.OAuthButton.OAuthListener;
import hu.bartl.authentication.OAuthButton.OAuthUser;
import hu.bartl.backend.Backend.UserAlreadyExistsException;
import hu.bartl.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserLifeCycle implements Serializable {

	private static final long serialVersionUID = -7957794884886185269L;

	private final List<UserLifeCycleEventHandler> eventHandlers = new ArrayList<UserLifeCycleEventHandler>();
	private final Backend backend;
	private User user;

	public static UserLifeCycle create() {
		return new UserLifeCycle();
	}

	private UserLifeCycle() {
		this.backend = Backend.getInstance();
	}

	public User getUser() {
		return this.user;
	}

	public void login() {
		fireLifeCycleEvent(UserLifeCycleEventType.LOGIN_STARTED);
	}

	public OAuthListener getAuthenticationListener() {
		return new OAuthListener() {

			@Override
			public void userAuthenticated(OAuthUser user) {
				try {
					backend.createUser(user.getId(), user.getName(),
							user.getPictureUrl());
					UserLifeCycle.this.user = backend.getUser(user.getId());
					fireLifeCycleEvent(UserLifeCycleEventType.LOGGED_IN);
				} catch (IllegalArgumentException | UserAlreadyExistsException e) {
					fireLifeCycleEvent(UserLifeCycleEventType.LOGIN_FAILED);
				}
			}

			@Override
			public void failed(String reason) {
				fireLifeCycleEvent(UserLifeCycleEventType.LOGIN_FAILED);
			}
		};
	}

	public void logout() {
		backend.destroyUser(user);
		this.user = null;
		fireLifeCycleEvent(UserLifeCycleEventType.LOGGED_OUT);
	}

	public void addLifeCycleEventHandler(UserLifeCycleEventHandler handler) {
		if (handler != null) {
			this.eventHandlers.add(handler);
		}
	}

	public void removeLifeCycleEventHandler(UserLifeCycleEventHandler handler) {
		if (this.eventHandlers.contains(handler)) {
			this.eventHandlers.remove(handler);
		}
	}

	private void fireLifeCycleEvent(UserLifeCycleEventType type) {
		UserLifeCycleEvent event = new UserLifeCycleEvent(type);
		List<UserLifeCycleEventHandler> handlers = new ArrayList<UserLifeCycle.UserLifeCycleEventHandler>(
				eventHandlers);

		for (UserLifeCycleEventHandler handler : handlers) {
			handler.handle(event);
		}
	}

	public class UserLifeCycleEvent {
		private final UserLifeCycleEventType type;

		public UserLifeCycleEvent(UserLifeCycleEventType type) {
			this.type = type;
		}

		public UserLifeCycleEventType getType() {
			return this.type;
		}
	}

	public enum UserLifeCycleEventType {
		LOGIN_STARTED, LOGGED_IN, LOGIN_FAILED, LOGGED_OUT
	}

	public interface UserLifeCycleEventHandler {
		void handle(UserLifeCycleEvent event);
	}
}
