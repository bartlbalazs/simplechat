package hu.bartl;

import hu.bartl.backend.UserLifeCycle;
import hu.bartl.backend.UserLifeCycle.UserLifeCycleEvent;
import hu.bartl.backend.UserLifeCycle.UserLifeCycleEventHandler;
import hu.bartl.model.User;
import hu.bartl.widgets.UserControlPanel;
import hu.bartl.widgets.UserControlPanelController;
import hu.bartl.widgets.UserControlPanelController.UserEvent;
import hu.bartl.widgets.UserControlPanelController.UserEventHandler;
import hu.bartl.widgets.UserControlPanelController.UserEventType;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.UIDetachedException;

@Theme("chattheme")
@SuppressWarnings("serial")
@Push(PushMode.MANUAL)
@PreserveOnRefresh
public class MyVaadinUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "hu.bartl.AppWidgetSet", heartbeatInterval = 5, closeIdleSessions = true)
	public static class Servlet extends VaadinServlet {
	}

	private Component userComponent = new Panel();

	private Component displayedComponent = new Panel();

	private final HorizontalLayout layout = new HorizontalLayout();

	@Override
	protected void init(VaadinRequest request) {
		startLifeCycle();
	}

	private void startLifeCycle() {
		UserLifeCycle lifeCycle = UserLifeCycle.create(this);
		addLifeCycleHandler(lifeCycle);
		lifeCycle.login();
	}

	private void addLifeCycleHandler(final UserLifeCycle lifeCycle) {
		lifeCycle.addLifeCycleEventHandler(new UserLifeCycleEventHandler() {

			@Override
			public void handle(UserLifeCycleEvent event) {
				switch (event.getType()) {

				case LOGGED_IN:
					User newUser = lifeCycle.getUser();
					initializeLayoutWithUser(newUser);
					break;

				case LOGIN_FAILED:
					lifeCycle.login();
					break;

				case LOGGED_OUT:
					lifeCycle.removeLifeCycleEventHandler(this);
					break;

				default:
					break;
				}

			}
		});
	}

	private void initializeLayoutWithUser(User user) {
		UserControlPanelController userControlPanelController = UserControlPanelController
				.create(user);

		userComponent = userControlPanelController.getComponent();

		userControlPanelController.addUserEventHandler(new UserEventHandler() {

			@Override
			public void handle(UserEvent event) {
				if (event.getType().equals(UserEventType.CONVERSATION_SELECTED)) {
					showComponent(event.getNewContent());
				}
				MyVaadinUI.this.pushSafely();
			}
		});

		buildLayout();
		pushSafely();
	}

	private void buildLayout() {

		userComponent.setWidth(UserControlPanel.WIDTH_PX_INT, Unit.PIXELS);
		displayedComponent.setSizeFull();

		layout.removeAllComponents();
		layout.addComponent(userComponent);
		layout.addComponent(displayedComponent);
		layout.setExpandRatio(displayedComponent, 1);
		layout.setMargin(true);
		layout.setSizeFull();

		setContent(layout);
	}

	private void showComponent(Component component) {
		this.layout.replaceComponent(displayedComponent, component);
		displayedComponent = component;
		layout.setExpandRatio(displayedComponent, 1);
	}

	public void pushSafely() {
		try {
			if (this.isAttached()) {
				this.access(new Runnable() {
					@Override
					public void run() {
						try {
							if (MyVaadinUI.this.isAttached()) {
								MyVaadinUI.this.push();
							}
						} catch (UIDetachedException e) {
							System.out.println(e);
						}
					}
				});
			}
		} catch (UIDetachedException e) {
			System.out.println(e);
		}
	}
}
