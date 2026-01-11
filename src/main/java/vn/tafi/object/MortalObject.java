package vn.tafi.object;

public class MortalObject {

	private Integer hostOrder; // Số thứ tự của hộ gia đình
	private Integer order; // Số thứ tự của Đệ tử
	private String fmName; // Họ
	private String midName; // Tên đệm
	private String name; // Tên
	private String gender; // Giới tính
	private boolean isCanMang; // Là căn mạng hay không
	private boolean isAHost; // Là chủ hộ hay không
	private String address;
	private String thienCan; // Thiên Can
	private String diaChi; // Địa Chi
	private Integer estimatedYearOB; // Tuổi ước tính
	private Integer age; // Tuổi
	private String sao; // Sao
	private String han; // Hạn
	private Integer ageRecalculated; // Tuổi được tính lại
	private SaoChieuEnum saoRecalculated; // Sao được tính lại
	private HanEnum hanRecalculated; // Hạn được tính lại
	private boolean isNotSupported; // Không hỗ trợ

	@Override
	public String toString() {
		return "MortalObject [hostOrder=" + hostOrder + ", order=" + order + ", fmName=" + fmName + ", midName="
				+ midName + ", name=" + name + ", gender=" + gender + ", isCanMang=" + isCanMang + ", isAHost="
				+ isAHost + ", address=" + address + ", thienCan=" + thienCan + ", diaChi=" + diaChi
				+ ", estimatedYearOB=" + estimatedYearOB + ", age=" + age + ", sao=" + sao + ", han=" + han
				+ ", isNotSupported=" + isNotSupported + "]";
	}

	public MortalObject() {
	}

	public MortalObject(Integer hostOrder, Integer order, String fmName, String midName, String name, String gender,
			boolean isCanMang, boolean isAHost, String address, String thienCan, String diaChi, Integer estimatedYearOB,
			Integer age, String sao, String han, boolean isNotSupported) {
		super();
		this.hostOrder = hostOrder;
		this.order = order;
		this.fmName = fmName;
		this.midName = midName;
		this.name = name;
		this.gender = gender;
		this.isCanMang = isCanMang;
		this.isAHost = isAHost;
		this.address = address;
		this.thienCan = thienCan;
		this.diaChi = diaChi;
		this.estimatedYearOB = estimatedYearOB;
		this.age = age;
		this.sao = sao;
		this.han = han;
		this.isNotSupported = isNotSupported;
	}

	public Integer getHostOrder() {
		return hostOrder;
	}

	public void setHostOrder(Integer hostOrder) {
		this.hostOrder = hostOrder;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getFmName() {
		return fmName;
	}

	public void setFmName(String fmName) {
		this.fmName = fmName;
	}

	public String getMidName() {
		return midName;
	}

	public void setMidName(String midName) {
		this.midName = midName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public boolean isCanMang() {
		return isCanMang;
	}

	public void setCanMang(boolean isCanMang) {
		this.isCanMang = isCanMang;
	}

	public boolean isAHost() {
		return isAHost;
	}

	public void setAHost(boolean isAHost) {
		this.isAHost = isAHost;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getThienCan() {
		return thienCan;
	}

	public void setThienCan(String thienCan) {
		this.thienCan = thienCan;
	}

	public String getDiaChi() {
		return diaChi;
	}

	public void setDiaChi(String diaChi) {
		this.diaChi = diaChi;
	}

	public Integer getEstimatedYearOB() {
		return estimatedYearOB;
	}

	public void setEstimatedYearOB(Integer estimatedYearOB) {
		this.estimatedYearOB = estimatedYearOB;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getSao() {
		return sao;
	}

	public void setSao(String sao) {
		this.sao = sao;
	}

	public String getHan() {
		return han;
	}

	public void setHan(String han) {
		this.han = han;
	}

	public Integer getAgeRecalculated() {
		return ageRecalculated;
	}

	public void setAgeRecalculated(Integer ageRecalculated) {
		this.ageRecalculated = ageRecalculated;
	}

	public SaoChieuEnum getSaoRecalculated() {
		return saoRecalculated;
	}

	public void setSaoRecalculated(SaoChieuEnum saoRecalculated) {
		this.saoRecalculated = saoRecalculated;
	}

	public HanEnum getHanRecalculated() {
		return hanRecalculated;
	}

	public void setHanRecalculated(HanEnum hanRecalculated) {
		this.hanRecalculated = hanRecalculated;
	}

	public boolean isNotSupported() {
		return isNotSupported;
	}

	public void setNotSupported(boolean isNotSupported) {
		this.isNotSupported = isNotSupported;
	}

}
