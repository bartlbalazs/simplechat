package hu.bartl;

import hu.bartl.backend.Backend;
import hu.bartl.backend.Backend.UserListChangedEvent;
import hu.bartl.backend.Backend.UserListChangedEventHandler;
import hu.bartl.model.Conversation;
import hu.bartl.model.User;
import hu.bartl.model.Conversation.MessageAddedEvent;
import hu.bartl.model.Conversation.MessageAddingListener;
import hu.bartl.model.User.ConversationListChangedEvent;
import hu.bartl.model.User.ConversationListChangedEventHandler;
import hu.bartl.widgets.NewUserPopup;
import hu.bartl.widgets.UserControlPanel;
import hu.bartl.widgets.UserControlPanel.ConversationSelectionEvent;
import hu.bartl.widgets.UserControlPanel.ConversationSelectionHandler;
import hu.bartl.widgets.UserControlPanel.UserSelectionEvent;
import hu.bartl.widgets.UserControlPanel.UserSelectionHandler;

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

	private final UserControlPanel userControlPanel = new UserControlPanel();

	private Component displayedComponent = new Panel();

	private final HorizontalLayout layout = new HorizontalLayout();

	User user;

	@Override
	protected void init(VaadinRequest request) {

		backend.addUserListChangedEventHandler(new UserListChangedEventHandler() {
			@Override
			public void handleEvent(UserListChangedEvent event) {
				userControlPanel.setUsers(backend.getUserContainer());
				// userList.setContainerDataSource(backend.getUserContainer());
				// userList.markAsDirty();
				userControlPanel.markAsDirtyRecursive();
				MyVaadinUI.this.pushSafely();
			}
		});

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

		userControlPanel.addUserSelectionHandler(new UserSelectionHandler() {

			@Override
			public void handle(UserSelectionEvent event) {
				User selectedUser = backend.getUser(event.getUserName());
				user.startConversation(selectedUser);

			}
		});

		userControlPanel.setWidth(UserControlPanel.WIDTH_PX_INT, Unit.PIXELS);
		displayedComponent.setSizeFull();

		layout.addComponent(userControlPanel);
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
					MyVaadinUI.this.user = newUser;
					MyVaadinUI.this.userControlPanel.setUserName(userName);
					MyVaadinUI.this.initializeConversationList();
				}
			});
			this.addWindow(popup);
		}
	}

	private void initializeConversationList() {
		if (user != null) {
			setConverzationListDataSource();
			addConversationSelectionHandling();
			addConversationRefreshHandling();
		}
	}

	private void setConverzationListDataSource() {
		userControlPanel.setConversations(user.getConversationContainer());
	}

	private void addConversationSelectionHandling() {
		userControlPanel
				.addConversationSelectionHandler(new ConversationSelectionHandler() {

					@Override
					public void handle(ConversationSelectionEvent event) {
						String selectedConversationName = event
								.getConversationName();
						final Conversation conversation = (Conversation) user
								.getConversationContainer()
								.getItem(selectedConversationName)
								.getItemProperty("conversation").getValue();

						if (!conversation.hasViewComponent(user)) {
							conversation.attachViewComponentForUser(user);
							conversation
									.addMessageAddingListener(new MessageAddingListener() {

										@Override
										public void handleEvent(
												MessageAddedEvent event) {
											MyVaadinUI.this.pushSafely();
										}
									});
						}

						MyVaadinUI.this.showComponent(conversation
								.getViewComponent(user));

					}
				});

	}

	private void addConversationRefreshHandling() {
		user.addConversationListChangedEventHandler(new ConversationListChangedEventHandler() {

			@Override
			public void handleEvent(ConversationListChangedEvent event) {
				setConverzationListDataSource();
				MyVaadinUI.this.pushSafely();
			}
		});
	}

	private void showComponent(Component component) {
		this.layout.replaceComponent(displayedComponent, component);
		displayedComponent = component;
		layout.setExpandRatio(displayedComponent, 1);
		MyVaadinUI.this.pushSafely();
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
