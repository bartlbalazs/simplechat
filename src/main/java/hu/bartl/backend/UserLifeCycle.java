package hu.bartl.backend;

import hu.bartl.model.User;
import hu.bartl.widgets.NewUserPopup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class UserLifeCycle implements Serializable {

	private static final long serialVersionUID = -7957794884886185269L;

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
			NewUserPopup popup = new NewUserPopup();
			final Property<String> userNameProperty = popup.getDataSource();
			popup.addCloseListener(new Window.CloseListener() {
				private static final long serialVersionUID = -8803555429247162254L;

				@Override
				public void windowClose(CloseEvent e) {

					String userName = userNameProperty.getValue();
					User newUser = User.create(userName);

					if (backend.addUser(newUser)) {
						UserLifeCycle.this.user = newUser;
						fireLifeCycleEvent(UserLifeCycleEventType.LOGGED_IN);
					} else {
						fireLifeCycleEvent(UserLifeCycleEventType.LOGIN_FAILED);
					}
				}
			});
			ui.addWindow(popup);
		}
	}

	public void logout() {
		backend.removeUser(this.user);
		user.kill();
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
		for (UserLifeCycleEventHandler handler : eventHandlers) {
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
