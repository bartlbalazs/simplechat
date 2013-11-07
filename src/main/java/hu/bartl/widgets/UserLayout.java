package hu.bartl.widgets;

import hu.bartl.model.User;
import hu.bartl.widgets.UserControlPanelController.UserEvent;
import hu.bartl.widgets.UserControlPanelController.UserEventHandler;
import hu.bartl.widgets.UserControlPanelController.UserEventType;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;

public class UserLayout extends HorizontalLayout {
	private static final long serialVersionUID = -7390404677929182114L;

	private Component userComponent = new Panel();
	private Component displayedComponent = new Panel();
	private User user;

	public UserLayout() {
		buildLayout();
	}

	private void buildLayout() {
		userComponent.setWidth(UserControlPanel.WIDTH_PX_INT, Unit.PIXELS);
		displayedComponent.setSizeFull();

		this.removeAllComponents();
		this.addComponent(userComponent);
		this.addComponent(displayedComponent);
		this.setExpandRatio(displayedComponent, 1);
		this.setMargin(true);
		this.setSizeFull();
	}

	public void setUser(User user) {
		this.user = user;
		initializeComponents();
	}

	private void initializeComponents() {
		UserControlPanelController userControlPanelController = UserControlPanelController
				.create(user);

		userComponent = userControlPanelController.getComponent();

		userControlPanelController.addUserEventHandler(new UserEventHandler() {

			@Override
			public void handle(UserEvent event) {
				if (event.getType().equals(UserEventType.CONVERSATION_SELECTED)) {
					showComponent(event.getNewContent());
				}
				UserLayout.this.fireComponentEvent();
			}
		});

		buildLayout();
		this.fireComponentEvent();
	}

	private void showComponent(Component component) {
		this.replaceComponent(displayedComponent, component);
		displayedComponent = component;
		this.setExpandRatio(displayedComponent, 1);
		this.fireComponentEvent();
	}
}
