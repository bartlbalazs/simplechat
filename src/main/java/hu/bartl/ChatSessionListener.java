package hu.bartl;

import hu.bartl.backend.UserLifeCycle;
import hu.bartl.widgets.UserLayout;

import com.google.inject.Singleton;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

@Singleton
public class ChatSessionListener implements SessionInitListener {

	private static final long serialVersionUID = 4607487962406603146L;

	private VaadinSession session;
	private UIProvider defaultProvider;

	@Override
	public void sessionInit(SessionInitEvent event) throws ServiceException {
		initListener();
		replaceDefaultUiProvider();
	}

	private void initListener() {
		session = VaadinSession.getCurrent();
		defaultProvider = session.getUIProviders().get(0);
	}

	private void replaceDefaultUiProvider() {
		session.removeUIProvider(defaultProvider);
		session.addUIProvider(new UIProvider() {
			private static final long serialVersionUID = 5505987187147439619L;

			@Override
			public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
				return ChatUI.class;
			}

			@Override
			public UI createInstance(UICreateEvent event) {
				UI instance = super.createInstance(event);
				createMediator(instance);
				return instance;
			}

			private void createMediator(UI instance) {
				UserLayout layout = ((ChatUI) instance).getLayout();
				UserLifeCycle lifeCycle = UserLifeCycle.create();
				UiLifeCycleMediator.create(instance, lifeCycle, layout);
			}
		});
	}

}
