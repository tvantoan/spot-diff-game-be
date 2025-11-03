package imggame.game;

public class DiffBox {
	public int x;
	public int y;
	public int width;
	public int height;
	public boolean found = false;

	public DiffBox(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean isPointInside(int px, int py) {
		return px >= x && px <= x + width && py >= y && py <= y + height;
	}
}
