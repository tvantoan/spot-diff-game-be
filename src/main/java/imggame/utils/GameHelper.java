package imggame.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import imggame.game.DiffBox;
import imggame.game.ImageSet;

public class GameHelper {
	public static ImageSet getRandomImageSet() {
		try {
			InputStream inputStream = GameHelper.class.getResourceAsStream("/dataset/image_sets.json");
			if (inputStream == null) {
				System.err.println("Cannot find image_sets.json in resources");
				return new ImageSet();
			}

			String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			JSONArray imageSetsArray = new JSONArray(jsonContent);

			if (imageSetsArray.isEmpty()) {
				System.err.println("image_sets.json is empty");
				return new ImageSet();
			}

			Random random = new Random();
			int randomIndex = random.nextInt(imageSetsArray.length());
			JSONObject selectedImageSet = imageSetsArray.getJSONObject(randomIndex);

			String diffImagePath = "dataset/" + selectedImageSet.getString("diff_image");
			String orgImagePath = "dataset/" + selectedImageSet.getString("org_image");

			JSONArray diffBoxesArray = selectedImageSet.getJSONArray("diff_boxes");
			DiffBox[] diffBoxes = new DiffBox[diffBoxesArray.length()];

			for (int i = 0; i < diffBoxesArray.length(); i++) {
				JSONObject boxJson = diffBoxesArray.getJSONObject(i);
				diffBoxes[i] = new DiffBox(
						boxJson.getInt("x"),
						boxJson.getInt("y"),
						boxJson.getInt("width"),
						boxJson.getInt("height"));
			}

			int difficulty = Math.min(5, Math.max(1, diffBoxes.length / 2));

			System.out.println("Loaded image set: " + diffImagePath + " with " + diffBoxes.length + " differences");
			return new ImageSet(orgImagePath, diffImagePath, diffBoxes, difficulty);

		} catch (Exception e) {
			System.err.println("Error loading random image set: " + e.getMessage());
			e.printStackTrace();
			return new ImageSet();
		}
	}
}
