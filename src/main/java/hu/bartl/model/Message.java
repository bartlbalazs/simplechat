package hu.bartl.model;

import com.google.common.base.Strings;

public class Message {
	private final User sender;
	private final String content;

	private Message(User sender, String content) {
		super();
		this.sender = sender;
		this.content = content;
	}

	public static Message Create(User sender, String content) {
		if (sender != null && !Strings.isNullOrEmpty(content)) {
			return new Message(sender, content);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public User getSender() {
		return sender;
	}

	public String getContent() {
		return content;
	}
}
