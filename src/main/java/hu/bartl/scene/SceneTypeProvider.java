package hu.bartl.scene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class SceneTypeProvider {

	private static final String INPUT_FILE_PATH = "./src/main/resources/scenes.yaml";
	private static final String INPUT_ROOT_ELEMENT = "SceneTypes";

	public List<SceneType> getSceneTypes() {
		ObjectMapper mapper = createMapper();
		return parseInput(mapper, INPUT_ROOT_ELEMENT);
	}

	private ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
				true);
		return mapper;
	}

	@SuppressWarnings("unchecked")
	private List<SceneType> parseInput(ObjectMapper mapper,
			String rootElementName) {

		try {
			List<LinkedHashMap<String, ?>> rawSceneTypes;
			rawSceneTypes = (List) ((Map) ((mapper.readValue(getInput(),
					List.class)).get(0))).get(rootElementName);
			List<SceneType> sceneTypes = new ArrayList<>();
			for (LinkedHashMap<String, ?> rawSceneType : rawSceneTypes) {
				sceneTypes.add(mapper.convertValue(rawSceneType,
						SceneType.class));
			}
			return sceneTypes;
		} catch (IOException e) {
			return new ArrayList<SceneType>();
		}
	}

	private File getInput() {
		File input = new File(INPUT_FILE_PATH);
		return input;
	}

}
