package imggame.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ImageSet implements Serializable {
	private static final long serialVersionUID = 1L;

	private String originImagePath;
	private String diffImagePath;
	private DiffBox[] diffBoxes;
	private int difficulty; // 1-5

	public ImageSet() {
		// Default image set
		this.originImagePath = "images/original.jpg";
		this.diffImagePath = "images/differences.jpg";
		this.difficulty = 3;
		this.diffBoxes = new DiffBox[] {
				new DiffBox(50, 50, 30, 30),
				new DiffBox(150, 80, 25, 25),
				new DiffBox(200, 200, 20, 20),
				new DiffBox(300, 150, 40, 40),
				new DiffBox(400, 300, 35, 35)
		};
	}

	public ImageSet(String originImagePath, String diffImagePath, DiffBox[] diffBoxes) {
		this.originImagePath = originImagePath;
		this.diffImagePath = diffImagePath;
		this.diffBoxes = diffBoxes;
		this.difficulty = 3;
	}

	public ImageSet(String originImagePath, String diffImagePath, DiffBox[] diffBoxes, int difficulty) {
		this.originImagePath = originImagePath;
		this.diffImagePath = diffImagePath;
		this.diffBoxes = diffBoxes;
		this.difficulty = difficulty;
	}

	public String getOriginImagePath() {
		return originImagePath;
	}

	public String getDiffImagePath() {
		return diffImagePath;
	}

	public DiffBox[] getDiffBoxes() {
		return diffBoxes;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public int getTotalDifferences() {
		return diffBoxes.length;
	}

	public int getFoundDifferences() {
		int count = 0;
		for (DiffBox box : diffBoxes) {
			if (box.found) {
				count++;
			}
		}
		return count;
	}

	public int getRemainingDifferences() {
		return getTotalDifferences() - getFoundDifferences();
	}

	public DiffBox getDiffBoxCollisionTo(int x, int y) {
		for (DiffBox box : diffBoxes) {
			if (!box.found && box.isPointInside(x, y)) {
				box.found = true;
				return box;
			}
		}
		return null;
	}

	public boolean isAllFound() {
		return getFoundDifferences() == getTotalDifferences();
	}

	public void reset() {
		for (DiffBox box : diffBoxes) {
			box.found = false;
		}
	}

	public List<int[]> getFoundDifferencePositions() {
		List<int[]> positions = new ArrayList<>();
		for (DiffBox box : diffBoxes) {
			if (box.found) {
				positions.add(new int[] { box.x, box.y, box.width, box.height });
			}
		}
		return positions;
	}
}
