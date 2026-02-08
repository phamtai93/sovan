package vn.tafi.object;

/**
 * Thiên Can (10 Can) - Bộ 10 Can trong hệ thống Can Chi
 * Thứ tự: Giáp, Ất, Bính, Đinh, Mậu, Kỷ, Canh, Tân, Nhâm, Quý
 */
public enum ThienCan {
	GIAP("Giáp", 0),
	AT("Ất", 1),
	BINH("Bính", 2),
	DINH("Đinh", 3),
	MAU("Mậu", 4),
	KY("Kỷ", 5),
	CANH("Canh", 6),
	TAN("Tân", 7),
	NHAM("Nhâm", 8),
	QUY("Quý", 9);

	private final String displayName;
	private final int index;

	ThienCan(String displayName, int index) {
		this.displayName = displayName;
		this.index = index;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getIndex() {
		return index;
	}

	/**
	 * Lấy Thiên Can từ index (0-9)
	 * @param index chỉ số từ 0 đến 9
	 * @return Thiên Can tương ứng
	 */
	public static ThienCan fromIndex(int index) {
		index = ((index % 10) + 10) % 10; // Đảm bảo index nằm trong [0, 9]
		for (ThienCan can : ThienCan.values()) {
			if (can.index == index) {
				return can;
			}
		}
		throw new IllegalArgumentException("Invalid TienCan index: " + index);
	}

	@Override
	public String toString() {
		return displayName;
	}
}
