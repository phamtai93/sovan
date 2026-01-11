package vn.tafi.object;

import java.util.List;
import java.util.Set;

public class SaoHanGroup {
	private SaoChieuEnum saoChieu;
	private List<MortalObject> namMortal; // Danh sách MortalObject nam
	private List<MortalObject> nuMortal; // Danh sách MortalObject nữ
	private Set<HanEnum> hanSet; // Tập hợp các hạn
	private Integer countCanMang; // Số lượng căn mạng

	// Constructor
	public SaoHanGroup(SaoChieuEnum saoChieu, List<MortalObject> namMortal, List<MortalObject> nuMortal,
			Set<HanEnum> hanSet, Integer countCanMang) {
		this.saoChieu = saoChieu;
		this.namMortal = namMortal;
		this.nuMortal = nuMortal;
		this.hanSet = hanSet;
		this.countCanMang = countCanMang;
	}

	// Getters và Setters
	public SaoChieuEnum getSaoChieu() {
		return saoChieu;
	}

	public void setSaoChieu(SaoChieuEnum saoChieu) {
		this.saoChieu = saoChieu;
	}

	public List<MortalObject> getNamMortal() {
		return namMortal;
	}

	public void setNamMortal(List<MortalObject> namMortal) {
		this.namMortal = namMortal;
	}

	public List<MortalObject> getNuMortal() {
		return nuMortal;
	}

	public void setNuMortal(List<MortalObject> nuMortal) {
		this.nuMortal = nuMortal;
	}

	public Set<HanEnum> getHanSet() {
		return hanSet;
	}

	public void setHanSet(Set<HanEnum> hanSet) {
		this.hanSet = hanSet;
	}

	public Integer getCountCanMang() {
		return countCanMang;
	}

	public void setCountCanMang(Integer countCanMang) {
		this.countCanMang = countCanMang;
	}

	@Override
	public String toString() {
		return "SaoHanGroup{" + "saoChieu=" + saoChieu + ", namMortal=" + (namMortal != null ? namMortal.size() : 0)
				+ ", nuMortal=" + (nuMortal != null ? nuMortal.size() : 0) + ", hanSet=" + hanSet + ", countCanMang="
				+ countCanMang + '}';
	}
}
