package hu.bartl.scene;

public class SceneType {
	String title;
	String description;
	int maxActorCount;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getMaxActorCount() {
		return maxActorCount;
	}

	public void setMaxActorCount(int maxActorCount) {
		this.maxActorCount = maxActorCount;
	}

	@Override
	public String toString() {
		return "SceneType [title=" + title + ", description=" + description
				+ ", maxActorCount=" + maxActorCount + "]";
	}
}
