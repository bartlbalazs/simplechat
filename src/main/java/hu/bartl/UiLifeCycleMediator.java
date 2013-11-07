package hu.bartl;

import hu.bartl.backend.UserLifeCycle;
import hu.bartl.backend.UserLifeCycle.UserLifeCycleEvent;
import hu.bartl.backend.UserLifeCycle.UserLifeCycleEventHandler;
import hu.bartl.model.User;
import hu.bartl.widgets.NewUserPopup;
import hu.bartl.widgets.UserLayout;

import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.ui.UI;

public class UiLifeCycleMediator {
	private static final String fbApiKey = "195337333982818";
	private static final String fbApiSecret = "3379a6ba4a00349f1787483f1dcacfe6";

	private final UI ui;
	private final UserLifeCycle lifeCycle;
	private final UserLayout layout;

	public static UiLifeCycleMediator create(UI ui, UserLifeCycle lifeCycle,
			UserLayout layout) {
		return new UiLifeCycleMediator(ui, lifeCycle, layout);
	}

	private UiLifeCycleMediator(UI ui, UserLifeCycle lifeCycle,
			UserLayout layout) {
		this.ui = ui;
		this.lifeCycle = lifeCycle;
		this.layout = layout;
		initialize();
	}

	private void initialize() {
		addDetachHandler();
		addLifeCycleHandler();
		lifeCycle.login();
	}

	private void addDetachHandler() {
		this.ui.addDetachListener(new DetachListener() {
			private static final long serialVersionUID = -4320072406018854769L;

			@Override
			public void detach(DetachEvent event) {
				lifeCycle.logout();
			}
		});
	}

	private void addLifeCycleHandler() {
		lifeCycle.addLifeCycleEventHandler(new UserLifeCycleEventHandler() {

			@Override
			public void handle(UserLifeCycleEvent event) {
				switch (event.getType()) {
				case LOGIN_STARTED:
					showLoginWindow();
					break;
				case LOGIN_FAILED:
					break;
				case LOGGED_IN:
					User newUser = lifeCycle.getUser();
					layout.setUser(newUser);
					break;
				case LOGGED_OUT:
					ui.close();
					break;
				default:
					break;

				}
			}
		});
	}

	private void showLoginWindow() {
		NewUserPopup popup = new NewUserPopup(fbApiKey, fbApiSecret,
				lifeCycle.getAuthenticationListener());
		ui.addWindow(popup);
	}
}
