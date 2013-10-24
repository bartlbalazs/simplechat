package hu.bartl.widgets;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ConversationPanel extends VerticalLayout {

	private static final long serialVersionUID = 7912609064753661825L;

	private final Panel messagePanel = new Panel();
	private final VerticalLayout messageLayout = new VerticalLayout();

	private final HorizontalLayout inputLayout = new HorizontalLayout();

	private final Button sendButton = new Button("Küldés");

	private final TextField inputField = new TextField();

	private ConversationPanel() {
		setMargin(true);
		this.addComponent(messagePanel);
		messagePanel.setWidth(600, Unit.PIXELS);

		messagePanel.setContent(messageLayout);
		messageLayout.setSizeFull();
		this.setExpandRatio(messagePanel, 1);

		inputLayout.setWidth(600, Unit.PIXELS);

		inputField.setHeight(25, Unit.PIXELS);
		inputField.setWidth(100, Unit.PERCENTAGE);
		inputField.setBuffered(true);
		inputLayout.addComponent(inputField);

		sendButton.setWidth(75, Unit.PIXELS);
		sendButton.setHeight(25, Unit.PIXELS);
		inputLayout.addComponent(sendButton);
		inputLayout.setExpandRatio(inputField, 1);

		this.addComponent(inputLayout);
		this.setSizeFull();

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
			this.addComponent(new Label(content));
		}
	}
}
