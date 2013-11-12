package hu.bartl.scene;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;

public class SceneTypeProviderTest {

	SceneTypeProvider provider = new SceneTypeProvider();
	private File inputFile;
	PrintWriter writer;

	@Before
	public void setup() {
		try {
			inputFile = File.createTempFile("test", ".yaml");
			writer = new PrintWriter(inputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadingSceneTypes() {
		// @formatter:off
		String testInput = Joiner.on("\n").join(
				"SceneTypes:",
				"    - title: Függetlenség napja",
				"      description: Jönnek az ufók!",
				"      maxActorCount: 6", "",
				"    - title: Zombi apokalipszis",
				"      description: Jönnek a zombik!",
				"      maxActorCount: 4");
		// @formatter:on

		writeToInputFile(testInput);

		List<SceneType> expectedOutput = new ArrayList<>();

		SceneType scene1 = new SceneType();
		scene1.setTitle("Függetlenség napja");
		scene1.setDescription("Jönnek az ufók!");
		scene1.setMaxActorCount(6);
		expectedOutput.add(scene1);

		SceneType scene2 = new SceneType();
		scene2.setTitle("Zombi apokalipszis");
		scene2.setDescription("Jönnek a zombik!");
		scene2.setMaxActorCount(4);
		expectedOutput.add(scene2);

		assertEquals(expectedOutput, provider.getSceneTypes(inputFile));
	}

	@Test
	public void testUnparsableInput() {
		writeToInputFile("random silly nonsense");
		assertEquals(new ArrayList<SceneType>(),
				provider.getSceneTypes(inputFile));
	}

	@Test
	public void testNullInput() {
		assertEquals(new ArrayList<SceneType>(),
				provider.getSceneTypes(inputFile));
	}

	private void writeToInputFile(String text) {
		writer.println(text);
		writer.close();
	}

	@After
	public void cleanup() {
		inputFile.delete();
	}
}
