package hu.bartl.backend;

import hu.bartl.authentication.OAuthButton.OAuthListener;
import hu.bartl.authentication.OAuthButton.OAuthUser;
import hu.bartl.model.User;
import hu.bartl.widgets.NewUserPopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.ui.UI;

public class UserLifeCycle implements Serializable {

	private static final long serialVersionUID = -7957794884886185269L;

	private static final String fbApiKey = "195337333982818";
	private static final String fbApiSecret = "3379a6ba4a00349f1787483f1dcacfe6";

	private final List<UserLifeCycleEventHandler> eventHandlers = new ArrayList<UserLifeCycleEventHandler>();
	private final UI ui;
	private final Backend backend;
	private User user;

	public static UserLifeCycle create(UI ui) {
		return new UserLifeCycle(ui);
	}

	private UserLifeCycle(UI ui) {
		this.ui = ui;
		this.backend = Backend.getInstance();

		initialize();
	}

	public User getUser() {
		return this.user;
	}

	public void initialize() {
		this.ui.addDetachListener(new DetachListener() {
			private static final long serialVersionUID = -4320072406018854769L;

			@Override
			public void detach(DetachEvent event) {
				logout();
			}
		});
	}

	public void login() {
		if (user == null) {
			final NewUserPopup popup = new NewUserPopup(fbApiKey, fbApiSecret,
					new OAuthListener() {

						@Override
						public void userAuthenticated(OAuthUser user) {
							User newUser = User.create(user.getId(),
									user.getName(), user.getPictureUrl());
							if (backend.addUser(newUser)) {
								UserLifeCycle.this.user = newUser;
								fireLifeCycleEvent(UserLifeCycleEventType.LOGGED_IN);
							} else {
								fireLifeCycleEvent(UserLifeCycleEventType.LOGIN_FAILED);
							}
						}

						@Override
						public void failed(String reason) {
							fireLifeCycleEvent(UserLifeCycleEventType.LOGIN_FAILED);
						}
					});

			ui.addWindow(popup);
		}
	}

	public void logout() {
		if (user != null) {
			user.kill();
			backend.removeUser(this.user);
			this.user = null;
		}
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
		LOGGED_IN, LOGIN_FAILED, LOGGED_OUT
	}

	public interface UserLifeCycleEventHandler {
		void handle(UserLifeCycleEvent event);
	}
}
