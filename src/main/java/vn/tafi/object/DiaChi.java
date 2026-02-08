package vn.tafi.object;

/**
 * Địa Chi (12 Chi) - Bộ 12 Chi trong hệ thống Can Chi
 * Thứ tự: Tí, Sửu, Dần, Mão, Thìn, Tỵ, Ngọ, Mùi, Thân, Dậu, Tuất, Hợi
 */
public enum DiaChi {
	TI("Tí", 0),
	SUU("Sửu", 1),
	DAN("Dần", 2),
	MAO("Mão", 3),
	THAN("Thìn", 4),
	TY("Tỵ", 5),
	NGO("Ngọ", 6),
	MUI("Mùi", 7),
	THAN_2("Thân", 8),
	DAU("Dậu", 9),
	TUAT("Tuất", 10),
	HOI("Hợi", 11);

	private final String displayName;
	private final int index;

	DiaChi(String displayName, int index) {
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
	 * Lấy Địa Chi từ index (0-11)
	 * @param index chỉ số từ 0 đến 11
	 * @return Địa Chi tương ứng
	 */
	public static DiaChi fromIndex(int index) {
		index = ((index % 12) + 12) % 12; // Đảm bảo index nằm trong [0, 11]
		for (DiaChi chi : DiaChi.values()) {
			if (chi.index == index) {
				return chi;
			}
		}
		throw new IllegalArgumentException("Invalid DiaChi index: " + index);
	}

	@Override
	public String toString() {
		return displayName;
	}
}
