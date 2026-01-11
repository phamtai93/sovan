package vn.tafi.object;

public enum HanEnum {

    HUYNH_TUYEN("Huỳnh Tuyền"),
    TAM_KHEO("Tam Kheo"),
    NGU_MO("Ngũ Mộ"),
    THIEN_TINH("Thiên Tinh"),
    TOAN_TAN("Toán Tận"),
    THIEN_LA("Thiên La"),
    DIA_VONG("Địa Võng"),
    DIEM_VUONG("Diêm Vương");

    private final String hanName;

    HanEnum(String hanName) {
        this.hanName = hanName;
    }

    public String getHanName() {
        return hanName;
    }

    public static final HanEnum[] HAN_CYCLE_NAM = {
        HUYNH_TUYEN, TAM_KHEO, NGU_MO, THIEN_TINH, TOAN_TAN, THIEN_LA, DIA_VONG, DIEM_VUONG
    };

    public static final HanEnum[] HAN_CYCLE_NU = {
        TOAN_TAN, THIEN_TINH, NGU_MO, TAM_KHEO, HUYNH_TUYEN, DIEM_VUONG, DIA_VONG, THIEN_LA
    };

    public static HanEnum getHan(int age, boolean isMale) {
        if (age < 11) {
            return null;
        }

        HanEnum[] hanCycle = isMale ? HanEnum.HAN_CYCLE_NAM : HanEnum.HAN_CYCLE_NU;

        // Công thức tính chỉ số Hạn trong chu kỳ 8
        int cycleIndex = (age - 10 - ((age / 10) - 1)) % 8;

        return hanCycle[cycleIndex];
    }
}
