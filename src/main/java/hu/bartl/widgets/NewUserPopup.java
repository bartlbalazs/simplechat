package hu.bartl.widgets;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class NewUserPopup extends Window {

	private static final long serialVersionUID = 5099225781613078532L;

	private final VerticalLayout layout = new VerticalLayout();

	private final Label welcomeLabel = new Label("Felhaszálónév: ");

	private final Button enterButton = new Button();

	private final TextField nameTextField = new TextField();

	private final DataSource dataSource = new NewUserPopup.DataSource();

	public NewUserPopup() {
		initialize();
	}

	private void initialize() {
		initializeWindow();
		initializeLayout();
		initializeEnterButton();
		initializeTextField();
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
		layout.addComponent(welcomeLabel);
		layout.addComponent(nameTextField);
		layout.addComponent(enterButton);
	}

	private void initializeEnterButton() {
		enterButton.setCaption("Belépés");
		enterButton.setClickShortcut(KeyCode.ENTER);
		enterButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 4660205399289094526L;

			@Override
			public void buttonClick(ClickEvent event) {
				NewUserPopup.this.close();

			}
		});
	}

	private void initializeTextField() {
		nameTextField.setNullRepresentation("");
		nameTextField.setImmediate(true);
		nameTextField.setBuffered(false);
		nameTextField.setPropertyDataSource(dataSource);
		nameTextField.focus();
	}

	public Property<String> getDataSource() {
		return this.dataSource;
	}

	public class DataSource implements Property<String> {
		private static final long serialVersionUID = -4693668726733374403L;
		private String value;

		@Override
		public String getValue() {
			return this.value;
		}

		@Override
		public void setValue(String newValue) throws ReadOnlyException {
			this.value = newValue;

		}

		@Override
		public Class<? extends String> getType() {
			return String.class;
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public void setReadOnly(boolean newStatus) {
			throw new UnsupportedOperationException();
		}

	}

}
