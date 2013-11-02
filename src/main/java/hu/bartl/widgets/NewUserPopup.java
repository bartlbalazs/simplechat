package hu.bartl.widgets;

import hu.bartl.authentication.FacebookButton;
import hu.bartl.authentication.OAuthButton.OAuthListener;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class NewUserPopup extends Window {

	private static final long serialVersionUID = 5099225781613078532L;

	private final VerticalLayout layout = new VerticalLayout();

	private final Button loginButton;

	public NewUserPopup(String apiKey, String apiSecret,
			OAuthListener authListener) {
		loginButton = new FacebookButton(apiKey, apiSecret, authListener);
		initialize();
	}

	private void initialize() {
		initializeWindow();
		initializeLayout();
		initializeEnterButton();
	}

	private void initializeWindow() {
		this.setWidth(300, Unit.PIXELS);
		this.setHeight(200, Unit.PIXELS);
		this.setModal(true);
		this.setClosable(false);
		this.center();
		this.setDraggable(false);
		this.setResizable(false);
		this.setCaption("Bejelentkezés");
		this.setContent(layout);
	}

	private void initializeLayout() {
		layout.setSizeFull();
		layout.setMargin(true);
		layout.addComponent(loginButton);
	}

	private void initializeEnterButton() {
		loginButton.setCaption("Belépés");
		loginButton.setClickShortcut(KeyCode.ENTER);
		loginButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 4660205399289094526L;

			@Override
			public void buttonClick(ClickEvent event) {
				NewUserPopup.this.close();
			}
		});
	}
}
