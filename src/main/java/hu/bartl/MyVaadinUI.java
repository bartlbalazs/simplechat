package hu.bartl;

import hu.bartl.backend.Backend;
import hu.bartl.backend.Backend.UserListChangedEvent;
import hu.bartl.backend.Backend.UserListChangedEventHandler;
import hu.bartl.backend.Conversation;
import hu.bartl.backend.Conversation.MessageAddedEvent;
import hu.bartl.backend.Conversation.MessageAddingListener;
import hu.bartl.backend.User;
import hu.bartl.backend.User.ConversationListChangedEvent;
import hu.bartl.backend.User.ConversationListChangedEventHandler;
import hu.bartl.widgets.NewUserPopup;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
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

	private Component displayedComponent = new Panel();

	private final Label userName = new Label();

	private final ListSelect userList = new ListSelect();

	private final ListSelect conversationList = new ListSelect();

	private final HorizontalLayout layout = new HorizontalLayout();

	User user;

	@Override
	protected void init(VaadinRequest request) {

		backend.addUserListChangedEventHandler(new UserListChangedEventHandler() {
			@Override
			public void handleEvent(UserListChangedEvent event) {
				userList.setContainerDataSource(backend.getUserContainer());
				userList.markAsDirty();
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

		VerticalLayout listLayout = new VerticalLayout();
		listLayout.addComponent(userName);
		listLayout.addComponent(conversationList);
		listLayout.addComponent(userList);
		listLayout.setHeight(100, Unit.PERCENTAGE);
		listLayout.setWidth(200, Unit.PIXELS);

		conversationList.setCaption("Beszélgetések");
		conversationList.setHeight(100, Unit.PERCENTAGE);
		conversationList.setWidth(200, Unit.PIXELS);
		conversationList.setImmediate(true);
		conversationList.setNullSelectionAllowed(true);

		userList.setCaption("Felhasználók");
		userList.setHeight(100, Unit.PERCENTAGE);
		userList.setWidth(200, Unit.PIXELS);
		userList.setContainerDataSource(backend.getUserContainer());
		userList.setNullSelectionAllowed(true);
		userList.setImmediate(true);
		userList.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {

				if (MyVaadinUI.this.user != null
						&& event.getProperty().getValue() != null) {
					System.out.println(event);
					System.out.println(event.getProperty());
					System.out.println(event.getProperty().getValue());
					String selectedUserName = event.getProperty().getValue() // TODO:
																				// getValue
																				// sometimes
																				// gives
																				// null
							.toString();
					user.startConversation(backend.getUser(selectedUserName));
				}
			}
		});

		listLayout.setExpandRatio(conversationList, 1);
		listLayout.setExpandRatio(userList, 3);

		layout.addComponent(listLayout);

		displayedComponent.setSizeFull();
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
					User newUser = User.create(userNameProperty.getValue());
					backend.addUser(newUser);
					MyVaadinUI.this.user = newUser;
					MyVaadinUI.this.userName.setValue(newUser.getName());
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
		conversationList
				.setContainerDataSource(user.getConversationContainer());
	}

	private void addConversationSelectionHandling() {
		conversationList.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {

				if (event.getProperty().getValue() != null) {
					String selectedUserName = event.getProperty().getValue()
							.toString();
					final Conversation conversation = (Conversation) user
							.getConversationContainer()
							.getItem(selectedUserName)
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

			}
		});
	}

	private void addConversationRefreshHandling() {
		user.addConversationListChangedEventHandler(new ConversationListChangedEventHandler() {

			@Override
			public void handleEvent(ConversationListChangedEvent event) {
				conversationList.setContainerDataSource(user
						.getConversationContainer());
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
