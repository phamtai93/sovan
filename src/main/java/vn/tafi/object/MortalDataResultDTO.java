package vn.tafi.object;

import java.util.ArrayList;
import java.util.List;

public class MortalDataResultDTO {

	private List<MortalObject> mortalObjects; // Danh sách toàn bộ dữ liệu từ Excel
	private List<MortalObject> errorObjects; // Danh sách các đối tượng có lỗi
	private List<String> errorMessages; // Danh sách các lỗi phát hiện

	public MortalDataResultDTO() {
		this.mortalObjects = new ArrayList<>();
		this.errorObjects = new ArrayList<>();
		this.errorMessages = new ArrayList<>();
	}

	public List<MortalObject> getMortalObjects() {
		return mortalObjects;
	}

	public void setMortalObjects(List<MortalObject> mortalObjects) {
		this.mortalObjects = mortalObjects;
	}

	public List<MortalObject> getErrorObjects() {
		return errorObjects;
	}

	public void setErrorObjects(List<MortalObject> errorObjects) {
		this.errorObjects = errorObjects;
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(List<String> errorMessages) {
		this.errorMessages = errorMessages;
	}
}
