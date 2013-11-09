package hu.bartl.scene;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class SceneTypeProviderTest {

	@Test
	public void testLoadingSceneTypes() {
		SceneTypeProvider provider = new SceneTypeProvider();
		List<SceneType> sceneTypes = provider.getSceneTypes();
		assertTrue(sceneTypes != null);
	}
}
