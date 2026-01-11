package vn.tafi.object;

public enum SaoChieuEnum {
	LA_HAU("La Hầu", "Thiên Cung Thần Thủ La Hầu Tinh Quân"),
	THO_TU("Thổ Tú", "Trung Ương Mậu Kỷ Thổ Tú Tinh Quân"),
	THUY_DIEU("Thủy Diệu", "Bắc Phương Nhâm Quý Thủy Đức Tinh Quân"),
	THAI_BACH("Thái Bạch", "Tây Phương Canh Tân Kim Đức Tinh Quân"),
	THAI_DUONG("Thái Dương", "Nhật Cung Thái Dương Thiên Tử Tinh Quân"),
	VAN_HAN("Vân Hán", "Nam Phương Bính Đinh Hỏa Đức Tinh Quân"),
	KE_DO("Kế Đô", "Thiên Cung Phân Vỹ Kế Đô Tinh Quân"),
	THAI_AM("Thái Âm", "Nguyệt Cung Thái Âm Hoàng Hậu Tinh Quân"),
	MOC_DUC("Mộc Đức", "Đông Phương Giáp Ất Mộc Đức Tinh Quân");

	private final String saoName;
	private final String saoFullName;

	SaoChieuEnum(String saoName, String saoFullName) {
		this.saoName = saoName;
		this.saoFullName = saoFullName;
	}

	public String getSaoName() {
		return saoName;
	}

	public String getSaoFullName() {
		return saoFullName;
	}

	// Bảng sao chiếu mệnh cho Nam
	private static final SaoChieuEnum[] SAO_CYCLE_NAM = { LA_HAU, THO_TU, THUY_DIEU, THAI_BACH, THAI_DUONG, VAN_HAN,
			KE_DO, THAI_AM, MOC_DUC };

	// Bảng sao chiếu mệnh cho Nữ
	private static final SaoChieuEnum[] SAO_CYCLE_NU = { KE_DO, VAN_HAN, MOC_DUC, THAI_AM, THO_TU, LA_HAU, THAI_DUONG,
			THAI_BACH, THUY_DIEU };

	public static SaoChieuEnum getSaoChieuMang(int age, boolean isMale) {
		if (age < 11) {
			return null;
		}

		int index = (age - 1) % 9; // Vòng lặp 9 sao
		if (isMale) {
			return SAO_CYCLE_NAM[index];
		} else {
			return SAO_CYCLE_NU[index];
		}
	}
}
