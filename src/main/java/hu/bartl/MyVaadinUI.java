package hu.bartl;

import hu.bartl.backend.Backend;
import hu.bartl.model.User;
import hu.bartl.widgets.NewUserPopup;
import hu.bartl.widgets.UserControlPanel;
import hu.bartl.widgets.UserControlPanelController;
import hu.bartl.widgets.UserControlPanelController.UserEvent;
import hu.bartl.widgets.UserControlPanelController.UserEventHandler;
import hu.bartl.widgets.UserControlPanelController.UserEventType;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

@Theme("chattheme")
@SuppressWarnings("serial")
@Push(PushMode.MANUAL)
public class MyVaadinUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "hu.bartl.AppWidgetSet")
	public static class Servlet extends VaadinServlet {
	}

	private final Backend backend = Backend.getInstance();

	private final List<UiCloseListener> closeListeners = new ArrayList<UiCloseListener>();

	private final NewUserPopup popup = new NewUserPopup();

	private Component userComponent = new Panel();

	private Component displayedComponent = new Panel();

	private final HorizontalLayout layout = new HorizontalLayout();

	User user;

	@Override
	protected void init(VaadinRequest request) {

		this.addDetachListener(new DetachListener() {

			@Override
			public void detach(DetachEvent event) {
				System.out.println("detach: " + this);
				UiCloseEvent e = new UiCloseEvent(user);
				for (UiCloseListener listener : closeListeners) {
					listener.onUiClose(e);
				}
			}
		});

		buildLayout();
		startLogin();
	}

	public void pushSafely() {
		this.access(new Runnable() {
			@Override
			public void run() {
				if (MyVaadinUI.this.isAttached()) {
					MyVaadinUI.this.push();
				}
			}
		});
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

	private void startLogin() {

		if (user == null) {
			final Property<String> userNameProperty = popup.getDataSource();
			popup.addCloseListener(new Window.CloseListener() {
				private static final long serialVersionUID = -8803555429247162254L;

				@Override
				public void windowClose(CloseEvent e) {

					String userName = userNameProperty.getValue();
					User newUser = User.create(userName);
					backend.addUser(newUser);

					UserControlPanelController userControlPanelController = UserControlPanelController
							.create(newUser);

					userComponent = userControlPanelController.getComponent();

					userControlPanelController
							.addUserEventHandler(new UserEventHandler() {

								@Override
								public void handle(UserEvent event) {
									if (event
											.getType()
											.equals(UserEventType.CONVERSATION_SELECTED)) {
										showComponent(event.getNewContent());
									}
									MyVaadinUI.this.pushSafely();
								}
							});

					buildLayout();
					pushSafely();
				}
			});
			this.addWindow(popup);
		}
	}

	private void showComponent(Component component) {
		this.layout.replaceComponent(displayedComponent, component);
		displayedComponent = component;
		layout.setExpandRatio(displayedComponent, 1);
	}

	public void addCloseListener(UiCloseListener listener) {
		if (listener != null) {
			closeListeners.add(listener);
		}
	}

	public interface UiCloseListener {
		void onUiClose(UiCloseEvent event);
	}

	public class UiCloseEvent {
		private final User attachedUser;

		public UiCloseEvent(User attachedUser) {
			this.attachedUser = attachedUser;
		}

		public User getAttachedUser() {
			return this.attachedUser;
		}
	}
}
