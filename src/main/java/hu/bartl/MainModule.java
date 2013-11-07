package hu.bartl;

import com.google.inject.AbstractModule;
import com.vaadin.server.SessionInitListener;

public class MainModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SessionInitListener.class).to(ChatSessionListener.class);
	}

}
