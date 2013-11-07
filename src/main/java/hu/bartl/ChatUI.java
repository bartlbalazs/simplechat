package hu.bartl;

import hu.bartl.widgets.UserLayout;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.UI;
import com.vaadin.ui.UIDetachedException;

@Theme("chattheme")
@SuppressWarnings("serial")
@Push(PushMode.MANUAL)
@PreserveOnRefresh
public class ChatUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = ChatUI.class, widgetset = "hu.bartl.AppWidgetSet", heartbeatInterval = 10, closeIdleSessions = true)
	public static class Servlet extends VaadinServlet {

		private final Injector injector = Guice
				.createInjector(new MainModule());

		@Override
		protected void servletInitialized() throws ServletException {
			super.servletInitialized();
			getService().addSessionInitListener(
					injector.getInstance(SessionInitListener.class));
		}
	}

	private final UserLayout layout = new UserLayout();

	@Override
	protected void init(VaadinRequest request) {
		setContent(layout);
		addLayoutListener();
	}

	private void addLayoutListener() {
		layout.addListener(new Listener() {
			@Override
			public void componentEvent(Event event) {
				ChatUI.this.pushSafely();
			}
		});
	}

	public UserLayout getLayout() {
		return layout;
	}

	public void pushSafely() {
		try {
			if (this.isAttached()) {
				this.access(new Runnable() {
					@Override
					public void run() {
						try {
							if (ChatUI.this.isAttached()) {
								ChatUI.this.push();
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
