package hu.bartl.widgets;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ConversationPanel extends Panel {

	private static final long serialVersionUID = 7912609064753661825L;

	private final int CONTENT_WIDTH_PX = 600;

	private final VerticalLayout mainLayout = new VerticalLayout();
	private final VerticalLayout messageLayout = new VerticalLayout();
	private final HorizontalLayout inputLayout = new HorizontalLayout();

	private final Button sendButton = new Button("Küldés");
	private final TextField inputField = new TextField();

	private ConversationPanel() {
		build();
	}

	private void build() {
		buildMessageLayout();
		buildInputField();
		buildSendButton();
		buildInputLayout();
		buildMainLayout();
	}

	private void buildMainLayout() {
		mainLayout.setMargin(true);
		mainLayout.addComponent(messageLayout);
		mainLayout.setExpandRatio(messageLayout, 1);
		mainLayout.addComponent(inputLayout);
		mainLayout.setSizeFull();

		setContent(mainLayout);
	}

	private void buildInputLayout() {
		inputLayout.setWidth(CONTENT_WIDTH_PX, Unit.PIXELS);
		inputLayout.addComponent(inputField);
		inputLayout.addComponent(buildPlaceHolder(20));
		inputLayout.addComponent(sendButton);
		inputLayout.setExpandRatio(inputField, 1);
	}

	private Component buildPlaceHolder(int placeHolderWidthPx) {
		Label placeHolder = new Label();
		placeHolder.setWidth(placeHolderWidthPx, Unit.PIXELS);
		return placeHolder;
	}

	private void buildSendButton() {
		sendButton.setWidth(75, Unit.PIXELS);
		sendButton.setHeight(25, Unit.PIXELS);
		sendButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 5558384915453382904L;

			@Override
			public void buttonClick(ClickEvent event) {
				inputField.commit();
				inputField.setValue("");
			}
		});
		sendButton.setClickShortcut(KeyCode.ENTER);
	}

	private void buildInputField() {
		inputField.setHeight(25, Unit.PIXELS);
		inputField.setWidth(100, Unit.PERCENTAGE);
		inputField.setBuffered(true);
	}

	private void buildMessageLayout() {
		messageLayout.setWidth(CONTENT_WIDTH_PX, Unit.PIXELS);
	}

	public static ConversationPanel create() {
		return new ConversationPanel();
	}

	public void addInputListener(ValueChangeListener listener) {
		if (listener != null) {
			this.inputField.addValueChangeListener(listener);
		}
	}

	public void removeInputListener(ValueChangeListener listener) {
		if (listener != null) {
			this.inputField.removeValueChangeListener(listener);
		}
	}

	public void addMessage(String sender, String content) {
		messageLayout.addComponent(new MessageComponent(sender, content));
	}

	private class MessageComponent extends HorizontalLayout {
		private static final long serialVersionUID = 6454657057006612044L;

		public MessageComponent(String sender, String content) {
			this.addComponent(new Label(sender));
			this.addComponent(buildPlaceHolder(10));
			this.addComponent(new Label(content));
		}
	}
}
