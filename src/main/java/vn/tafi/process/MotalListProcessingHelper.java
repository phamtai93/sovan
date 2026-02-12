package vn.tafi.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import vn.tafi.object.ConfigLoader;
import vn.tafi.object.HanEnum;
import vn.tafi.object.MortalDataResultDTO;
import vn.tafi.object.MortalObject;
import vn.tafi.object.SaoChieuEnum;
import vn.tafi.object.SaoHanGroup;

public class MotalListProcessingHelper {

	public static void processSelectListMotalFile(JLabel fileLabel, JLabel guideLabel, JPanel inputPanel,
			JTextField startField, JTextField endField, final String[] selectedFilePath) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnValue = fileChooser.showOpenDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			if (selectedFile.getName().endsWith(".xls") || selectedFile.getName().endsWith(".xlsx")) {
				applySelectedFile(selectedFile, fileLabel, guideLabel, inputPanel, startField, endField, selectedFilePath);
			} else {
				JOptionPane.showMessageDialog(null, "Chỉ được chọn file Excel (.xls, .xlsx)!", "Lỗi",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Helper: Apply selected file to UI (used by both JFileChooser and drag-and-drop)
	 */
	static void applySelectedFile(File file, JLabel fileLabel, JLabel guideLabel,
			JPanel inputPanel, JTextField startField, JTextField endField, String[] selectedFilePath) {
		selectedFilePath[0] = file.getAbsolutePath();
		fileLabel.setText("File đã chọn: " + file.getName());

		// Tự động xác định `endField`
		int autoEndField = detectEndField(file.getAbsolutePath(), 4); // Tìm endField từ hàng 4

		// Cập nhật UI
		startField.setText("4");
		endField.setText(String.valueOf(autoEndField));
		// Hiển thị hướng dẫn và input khi chọn file thành công
		guideLabel.setVisible(true);
		inputPanel.setVisible(true);
	}

	public static void processCheckingSaoHan(JTextField startField, JTextField endField, JTextArea logTextArea,
			final List<MortalObject> mortalObjects, final String[] selectedFilePath) {
		if (selectedFilePath[0] == null) {
			JOptionPane.showMessageDialog(null, "Hãy chọn một file Excel trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int currentYear;
		String lunarYear;
		try {
			currentYear = Integer.parseInt(ConfigLoader.getProperty("year"));
			lunarYear = ConfigLoader.getProperty("lunaYear");
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(null, "Lỗi: Giá trị 'year' trong file cấu hình không hợp lệ!", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String startText = startField.getText().trim();
		String endText = endField.getText().trim();
		if (startText.isEmpty() || endText.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Vui lòng nhập dòng bắt đầu và dòng kết thúc!", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			int startRow = Integer.parseInt(startText);
			int endRow = Integer.parseInt(endText);

			// Hiển thị kết quả kiểm tra vào logTextArea
			logTextArea.setText(""); // Xóa nội dung cũ

			if (startRow > endRow || startRow < 1) {
				JOptionPane.showMessageDialog(null, "Giá trị dòng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return;
			}

			File excelFile = new File(selectedFilePath[0]);
			mortalObjects.addAll(readMortalObjectsFromExcel(excelFile, startRow, endRow));

			if (mortalObjects.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Không tìm thấy dữ liệu hợp lệ trong khoảng dòng đã chọn!",
						"Thông báo", JOptionPane.INFORMATION_MESSAGE);
			} else {
				MortalDataResultDTO dataResultDTO = processMortalObjectsFromExcel(mortalObjects, currentYear);

				// Kiểm tra xem có Mortal object nào không có hostOrder
				List<String> allErrors = new ArrayList<>(dataResultDTO.getErrorMessages());
				List<MortalObject> nullHostOrderObjects = new ArrayList<>();
				for (MortalObject obj : mortalObjects) {
					if (obj.getHostOrder() == null) {
						nullHostOrderObjects.add(obj);
						String errorMsg = String.format("STT: %s | Họ tên: %s %s %s | Lỗi: Thiếu thông tin 'Hộ thứ'",
								obj.getOrder(), obj.getFmName(), obj.getMidName(), obj.getName());
						allErrors.add(errorMsg);
					}
				}

				// Hiển thị kết quả vào textArea
				logTextArea.setText("   Xử lý dữ liệu cho năm: " + currentYear + " - " + lunarYear + "\n");

				if (!allErrors.isEmpty()) {
					// Có lỗi: hiển thị danh sách lỗi
					logTextArea.append("❌ Kiểm tra THẤT BẠI: Phát hiện " + allErrors.size() + " lỗi\n\n");
					logTextArea.append("⚠️ Danh sách lỗi phát hiện:\n");
					logTextArea.append(new String(new char[80]).replace('\0', '=') + "\n");
					for (String error : allErrors) {
						logTextArea.append(error + "\n");
					}
					logTextArea.append(new String(new char[80]).replace('\0', '=') + "\n");
					logTextArea.append("\n💡 Vui lòng kiểm tra lại file Excel và sửa các lỗi phát hiện.\n");
				} else {
					// Kiểm tra thành công
					logTextArea.append("✅ Kiểm tra hoàn tất: " + mortalObjects.size()
							+ " dòng dữ liệu đã được nạp từ file Excel và không có lỗi. \n");

					// Debug: In thông tin chi tiết từng người nếu config cho phép
					String debugPrintMortalInfo = ConfigLoader.getProperty("debug-printMortalInfo");
					if ("true".equalsIgnoreCase(debugPrintMortalInfo)) {
						String separator = new String(new char[80]).replace('\0', '=');
						logTextArea.append("\n" + separator + "\n");
						logTextArea.append("📋 THÔNG TIN CHI TIẾT TỪNG NGƯỜI (Debug Mode)\n");
						logTextArea.append(separator + "\n\n");

						for (MortalObject obj : mortalObjects) {
							logTextArea.append(String.format("STT: %s | Họ tên: %s %s %s\n",
									obj.getOrder(), obj.getFmName(), obj.getMidName(), obj.getName()));
							logTextArea.append(String.format("  Giới tính: %s | Tuổi gốc: %s | Tuổi tính lại: %s\n",
									obj.getGender(), obj.getAge(), obj.getAgeRecalculated()));
							logTextArea.append(String.format("  Thiên Căn: %s | Địa Chi: %s | Địa chỉ: %s\n",
									obj.getThienCan(), obj.getDiaChi(), obj.getAddress()));
							logTextArea.append(String.format("  Năm sinh (ước): %s | Chủ hộ: %s | Hộ thứ: %s\n",
									obj.getEstimatedYearOB(), obj.isAHost() ? "Có" : "Không", obj.getHostOrder()));

							if (obj.getSaoRecalculated() != null) {
								logTextArea.append(String.format("  Sao chiếu mệnh: %s\n", obj.getSaoRecalculated().getSaoName()));
							}
							if (obj.getHanRecalculated() != null) {
								logTextArea.append(String.format("  Hạn: %s\n", obj.getHanRecalculated().getHanName()));
							}
							if (obj.isNotSupported()) {
								logTextArea.append("  ⚠️ Không được hỗ trợ\n");
							}
							logTextArea.append("\n");
						}
						logTextArea.append(separator + "\n");
					}
				}
			}
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(null, "Dòng nhập vào phải là số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void processCreateLabelAndNote(JTextArea logTextArea, final List<MortalObject> mortalObjects, String excelFilePath) {
		if (mortalObjects == null || mortalObjects.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Không có dữ liệu để tạo nhãn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			// Lấy thư mục chứa file Excel (ưu tiên) hoặc working directory
			String excelFileDirectory = (excelFilePath != null && !excelFilePath.isEmpty())
				? new File(excelFilePath).getParent()
				: System.getProperty("user.dir");

			String saoHanTemplatePath = Utils.findTemplateFile(Utils.TemplateFile.LABEL_SAO_HAN, excelFileDirectory).getAbsolutePath();
			String coverTemplatePath = Utils.findTemplateFile(Utils.TemplateFile.COVER, excelFileDirectory).getAbsolutePath();
			String noteTemplatePath = Utils.findTemplateFile(Utils.TemplateFile.NOTEBOOK, excelFileDirectory).getAbsolutePath();
			String outputDir = excelFileDirectory;

			generateLabelWordFiles(saoHanTemplatePath, coverTemplatePath, noteTemplatePath, outputDir, mortalObjects,
					logTextArea);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Lỗi khi tạo file nhãn: " + ex.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void generateLabelWordFiles(String saoHanTemplatePath, String coverTemplatePath,
			String noteTemplatePath, String outputDir, List<MortalObject> mortalObjects, JTextArea logTextArea)
			throws Exception {
		File saoHanTemplateFile = new File(saoHanTemplatePath);
		File coverTemplateFile = new File(coverTemplatePath);
		File noteTemplateFile = new File(noteTemplatePath);
		if (!saoHanTemplateFile.exists()) {
			throw new FileNotFoundException("Không tìm thấy file template: " + saoHanTemplatePath);
		}

		// Tạo danh sách Nam và Nữ
		List<MortalObject> maleList = new ArrayList<>();
		List<MortalObject> femaleList = new ArrayList<>();

		// Tạo Map để nhóm các đối tượng theo hộ gia đình (hostOrder)
		LinkedHashMap<Integer, List<MortalObject>> groupedByHostOrder = new LinkedHashMap<>();
		LinkedHashMap<Integer, List<MortalObject>> notSupportedObjs = new LinkedHashMap<>();

		for (MortalObject obj : mortalObjects) {
			try {
				if (obj.getAgeRecalculated() == null || obj.getAgeRecalculated() < 11) {
					continue; // Bỏ qua nếu dưới 11 tuổi
				}

				if (obj.isNotSupported()) {
					notSupportedObjs.computeIfAbsent(obj.getHostOrder(), k -> new ArrayList<>()).add(obj);
					continue; // Dừng xử lý nếu không được hỗ trợ
				}

				// Nhóm các đối tượng vào LinkedHashMap theo hostOrder
				// Nếu chưa có hostOrder, tạo danh sách mới
				groupedByHostOrder.computeIfAbsent(obj.getHostOrder(), k -> new ArrayList<>()).add(obj);

				// Phân loại theo giới tính
				if ("Nam".equalsIgnoreCase(obj.getGender())) {
					maleList.add(obj);
				} else if ("Nữ".equalsIgnoreCase(obj.getGender())) {
					femaleList.add(obj);
				}
			} catch (Exception e) {
				System.out.println("Lỗi ở order: " + obj.getOrder());
			}
		}

		// Định nghĩa file output (Nam)
		String maleOutputFile = Utils.getUniqueFileName(outputDir + "/printLabelSaoHanGenerated_NAM.docx");
		// Định nghĩa file output (Nữ)
		String femaleOutputFile = Utils.getUniqueFileName(outputDir + "/printLabelSaoHanGenerated_NU.docx");
		// File in nhãn chủ hộ
		String hostOutputFile = Utils.getUniqueFileName(outputDir + "/printCoverGenerated.docx");
		// File in ghi chú từng hộ
		String noteOutputFile = Utils.getUniqueFileName(outputDir + "/printNotebookGenerated.docx");

		// Ghi log số lượng đối tượng
		StringBuilder logBuilder = new StringBuilder();
		if (!maleList.isEmpty()) {
			writeLabelsToFile(saoHanTemplateFile, maleOutputFile, maleList);
			logBuilder.append("✅ Đã tạo file: ").append(maleOutputFile).append("\n");
			logBuilder.append("   là File nhãn in Hình nhân cho Nam").append("\n");
			logBuilder.append("📌 Tổng số Nam được xử lý: ").append(maleList.size()).append("\n\n");
		}

		if (!femaleList.isEmpty()) {
			writeLabelsToFile(saoHanTemplateFile, femaleOutputFile, femaleList);
			logBuilder.append("✅ Đã tạo file: ").append(femaleOutputFile).append("\n");
			logBuilder.append("   là File nhãn in Hình nhân cho Nữ").append("\n");
			logBuilder.append("📌 Tổng số Nữ được xử lý: ").append(femaleList.size()).append("\n\n");
		}

		if (maleList.isEmpty() && femaleList.isEmpty()) {
			logBuilder.append("⚠ Không có đối tượng nào đủ điều kiện để tạo nhãn.\n");
		}

		if (!groupedByHostOrder.isEmpty()) {
			writeMortalObjectsToFile(coverTemplateFile, hostOutputFile, groupedByHostOrder);
			logBuilder.append("✅ Đã tạo file: ").append(hostOutputFile).append("\n");
			logBuilder.append("   là File nhãn in Tập hình nhân theo từng hộ").append("\n");
			logBuilder.append("📌 Tổng số Hộ được xử lý: ").append(groupedByHostOrder.size()).append("\n\n");

			LinkedHashMap<Integer, List<MortalObject>> mergedMap = new LinkedHashMap<>();
			mergedMap.putAll(groupedByHostOrder);
			mergedMap.putAll(notSupportedObjs);

			writeNotebookEntries(noteTemplateFile, noteOutputFile, mergedMap);
			logBuilder.append("✅ Đã tạo file: ").append(noteOutputFile).append("\n");
			logBuilder.append("   là File in Sổ ghi chép theo từng hộ").append("\n");
			logBuilder.append("📌 Tổng số Hộ được xử lý: ").append(mergedMap.size()).append("\n\n");
		}

		// Cập nhật log trên UI
		logTextArea.setText(logBuilder.toString());
	}

	private static void writeLabelsToFile(File templateFile, String outputFilePath, List<MortalObject> mortalObjects)
			throws Exception {
		try (XWPFDocument templateDocument = new XWPFDocument(Files.newInputStream(templateFile.toPath()));
				XWPFDocument document = new XWPFDocument()) {

			// Lấy Section Properties từ template
			CTSectPr sectPr = templateDocument.getDocument().getBody().isSetSectPr()
					? templateDocument.getDocument().getBody().getSectPr()
					: null;

			// Nếu template có section properties, sao chép vào tài liệu mới
			if (sectPr != null) {
				document.getDocument().getBody().addNewSectPr().set(sectPr);
			}

		// Lấy số nhãn cho mỗi mortal object từ config
		int labelsPerMortal = 2; // Default value
		try {
			String labelsPerMortalStr = ConfigLoader.getProperty("labelsPerMortalObject");
			if (labelsPerMortalStr != null && !labelsPerMortalStr.isEmpty()) {
				labelsPerMortal = Integer.parseInt(labelsPerMortalStr);
			}
		} catch (NumberFormatException e) {
			System.err.println("Invalid labelsPerMortalObject config: " + e.getMessage());
		}

		int pageCounter = 0;
		for (int i = 0; i < mortalObjects.size(); i++) {
				MortalObject obj = mortalObjects.get(i);
				String labelContent = String.format(ConfigLoader.getProperty("labelSaoHanTemplate"),
						String.join(" ", obj.getFmName(), obj.getMidName(), obj.getName()).trim(),
						String.join(" ", obj.getThienCan(), obj.getDiaChi()).trim(), obj.getAgeRecalculated(),
						obj.getSaoRecalculated().getSaoName(), obj.getHanRecalculated().getHanName());

				// Tạo 2 trang cho mỗi mortal object
				for (int pageNum = 0; pageNum < labelsPerMortal; pageNum++) {
					// Tạo đoạn văn bản ở đầu trang
					XWPFParagraph paragraph = document.createParagraph();

					// Nếu không phải trang đầu tiên, thêm page break
					if (pageCounter > 0) {
						paragraph.setPageBreak(true);
					}

					paragraph.setAlignment(ParagraphAlignment.CENTER);

					XWPFRun run = paragraph.createRun();
					run.setText(labelContent);
					run.setFontFamily("Calibri");
					run.setFontSize(13);
					run.setBold(true);

					pageCounter++;
				}
			}

			// Ghi ra file mới
			try (FileOutputStream out = new FileOutputStream(outputFilePath)) {
				document.write(out);
			}
		}
	}

	public static void writeMortalObjectsToFile(File templateFile, String outputFilePath,
			Map<Integer, List<MortalObject>> groupedByHostOrder) throws Exception {
		try (XWPFDocument templateDocument = new XWPFDocument(Files.newInputStream(templateFile.toPath()));
				XWPFDocument document = new XWPFDocument(Files.newInputStream(templateFile.toPath()))) {

			// ✅ Lấy bảng đầu tiên từ template
			List<XWPFTable> tables = document.getTables();
			if (tables.isEmpty()) {
				throw new IllegalStateException("Không tìm thấy bảng trong template!");
			}

			XWPFTable table = tables.get(0);

			// ✅ Lấy hàng mẫu để sao chép định dạng
			XWPFTableRow templateRow = table.getRow(0);
			if (templateRow == null) {
				throw new IllegalStateException("Hàng mẫu không tồn tại trong bảng template!");
			}

			// ✅ Đảm bảo bảng có đủ hàng để ghi
			int currentRowIndex = 0;

			for (Map.Entry<Integer, List<MortalObject>> entry : groupedByHostOrder.entrySet()) {
				int hostOrder = entry.getKey();
				List<MortalObject> familyMembers = entry.getValue();

				// 🔎 Tìm chủ hộ trong danh sách
				MortalObject houseOwner = familyMembers.stream().filter(MortalObject::isAHost).findFirst().orElse(null);

				if (houseOwner == null)
					continue; // Không có chủ hộ, bỏ qua

				int familySize = familyMembers.size();
				long maleCount = familyMembers.stream().filter(m -> "Nam".equalsIgnoreCase(m.getGender())).count();
				long femaleCount = familySize - maleCount;

				// 📌 Format chuỗi theo yêu cầu
				String formattedText = String.format(ConfigLoader.getProperty("labelHostTemplate"), hostOrder,
						String.join(" ", houseOwner.getFmName(), houseOwner.getMidName(), houseOwner.getName()).trim(),
						String.join(" ", houseOwner.getThienCan(), houseOwner.getDiaChi()).trim(),
						houseOwner.getAgeRecalculated(), familySize, maleCount, femaleCount, houseOwner.getAddress());

				// Nếu không có nam hoặc không có nữ, thì không cần hiển thị số lượng
				formattedText = formattedText.replace("0 nam,", "");
				formattedText = formattedText.replace("0 nữ,", "");

				// Nếu chuỗi quá dài thì viết tắt chữ "tuổi" thành "t"
				// "Gồm có" thì cắt ngắn còn lại "Gồm"
				int checkedIndex = formattedText.indexOf("Gồm có");
				if (checkedIndex > 42) {
					formattedText = formattedText.replace("tuổi", "t");
				}
				if (formattedText.length() - checkedIndex > 41) {
					formattedText = formattedText.replace("Gồm có", "Gồm:");
				}

				XWPFTableRow targetRow;
				if (currentRowIndex == 0) {
					// ✅ Ghi nội dung vào hàng đầu tiên (hàng mẫu)
					targetRow = templateRow;
				} else {
					// ✅ Tạo hàng mới từ hàng mẫu theo cách thủ công
					targetRow = table.createRow();
					Utils.copyRowStyle(templateRow, targetRow);
				}

				// ✅ Ghi nội dung vào cột 2 (Fix lỗi không hiển thị nội dung)
				XWPFTableCell cell = targetRow.getCell(1);
				if (cell == null) {
					cell = targetRow.addNewTableCell();
				}

				// 🛑 FIX: Xóa đoạn văn bản trống mặc định trước khi thêm nội dung mới
				while (cell.getParagraphs().size() > 0) {
					cell.removeParagraph(0);
				}

				XWPFParagraph paragraph = cell.addParagraph();
				XWPFRun run = paragraph.createRun();
				run.setFontFamily("Calibri");
				run.setFontSize(19);
				run.setBold(true);

				// 🔥 Tách nội dung theo dấu xuống dòng `\n` và ghi vào Word
				String[] lines = formattedText.split("\n");
				for (int i = 0; i < lines.length; i++) {
					run.setText(lines[i]);
					if (i < lines.length - 1) {
						run.addBreak(); // ✅ Xuống dòng đúng chuẩn Word
					}
				}

				currentRowIndex++;
			}

			// ✅ Xuất file kết quả
			try (FileOutputStream out = new FileOutputStream(outputFilePath)) {
				document.write(out);
			}
		}
	}

	private static int detectEndField(String filePath, int startRow) {
		int endField = startRow; // Giá trị mặc định

		try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheetAt(0); // Lấy sheet đầu tiên
			int lastRowNum = sheet.getLastRowNum();

			for (int rowIndex = startRow - 1; rowIndex <= lastRowNum; rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row == null)
					break; // Gặp dòng trống -> Dừng

				Cell cell = row.getCell(3); // Cột D (index 3)
				if (cell == null || cell.getCellType() == CellType.BLANK
						|| (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty())) {
					break; // Dòng trống -> Kết thúc
				}

				endField = rowIndex + 1; // Lưu lại hàng cuối có dữ liệu
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Lỗi đọc file Excel: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		return endField;
	}

	private static List<MortalObject> readMortalObjectsFromExcel(File file, int startRow, int endRow) {
		List<MortalObject> mortalObjects = new ArrayList<>();

		try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheetAt(0); // Giả sử đọc từ sheet đầu tiên
			// Công cụ tính toán công thức
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			for (int rowIndex = startRow - 1; rowIndex <= endRow - 1; rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row == null)
					continue;

				MortalObject obj = new MortalObject();

				obj.setHostOrder(Utils.getIntegerValue(row, 2, evaluator)); // Cột C
				obj.setOrder(Utils.getIntegerValue(row, 3, evaluator)); // Cột D
				obj.setFmName(Utils.getStringValue(row, 4, evaluator)); // Cột E
				obj.setMidName(Utils.getStringValue(row, 5, evaluator)); // Cột F
				obj.setName(Utils.getStringValue(row, 6, evaluator)); // Cột G
				obj.setGender(Utils.getStringValue(row, 7, evaluator)); // Cột H
				obj.setCanMang(Utils.getBooleanValue(row, 8, evaluator)); // Cột I (x = true)
				obj.setAHost(Utils.getBooleanValue(row, 9, evaluator)); // Cột J (x = true)
				obj.setThienCan(Utils.getStringValue(row, 10, evaluator)); // Cột K
				obj.setDiaChi(Utils.getStringValue(row, 11, evaluator)); // Cột L
				obj.setEstimatedYearOB(Utils.getIntegerValue(row, 12, evaluator)); // Cột M
				obj.setAge(Utils.getIntegerValue(row, 13, evaluator)); // Cột N
				obj.setSao(Utils.getStringValue(row, 14, evaluator)); // Cột O
				obj.setHan(Utils.getStringValue(row, 15, evaluator)); // Cột P
				obj.setNotSupported(Utils.getBooleanValue(row, 16, evaluator)); // Cột Q (x = true)
				obj.setAddress(Utils.getStringValue(row, 17, evaluator)); // Cột R

				mortalObjects.add(obj);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Lỗi khi đọc file Excel: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

		return mortalObjects;
	}

	private static MortalDataResultDTO processMortalObjectsFromExcel(List<MortalObject> mortalObjects,
			int currentYear) {
		MortalDataResultDTO resultDTO = new MortalDataResultDTO();

		// Danh sách chứa thông tin lỗi
		List<MortalObject> errorObjects = new ArrayList<>();
		List<String> errorMessages = new ArrayList<>();

		for (MortalObject obj : mortalObjects) {

			// Có thể check if (obj.isNotSupported()) và bỏ qua phần xử lý
			// (vì người này không được hổ trợ kiểm tra Sao Hạn)

			// 🎯 Tính lại tuổi: ageRecalculated = year - estimatedYearOB + 1
			if (obj.getEstimatedYearOB() != null) {
				obj.setAgeRecalculated(currentYear - obj.getEstimatedYearOB() + 1);
			}

			// 🎯 Tra cứu Sao chiếu mệnh và Hạn
			if (obj.getAgeRecalculated() != null && obj.getGender() != null) {
				boolean isMale = "Nam".equalsIgnoreCase(obj.getGender());
				obj.setSaoRecalculated(SaoChieuEnum.getSaoChieuMang(obj.getAgeRecalculated(), isMale));
				obj.setHanRecalculated(HanEnum.getHan(obj.getAgeRecalculated(), isMale));
				// Kiểm tra sự khác biệt giữa giá trị từ file Excel và giá trị tính toán lại
				boolean isAgeMismatch = !Objects.equals(obj.getAge(), obj.getAgeRecalculated());
				boolean isSaoMismatch = !obj.isNotSupported() && obj.getSaoRecalculated() != null
						&& !Utils.isEqualIgnoreNull(obj.getSao(), obj.getSaoRecalculated().getSaoName());
				boolean isHanMismatch = !obj.isNotSupported() && obj.getHanRecalculated() != null
						&& !Utils.isEqualIgnoreNull(obj.getHan(), obj.getHanRecalculated().getHanName());

				// Nếu có lỗi, thêm vào danh sách lỗi
				if (isAgeMismatch || isSaoMismatch || isHanMismatch) {
					StringBuilder errorMessage = new StringBuilder();
					errorMessage.append("STT: ").append(obj.getOrder()).append(" - Họ tên: ").append(obj.getFmName())
							.append(" ").append(obj.getMidName()).append(" ").append(obj.getName());

					if (isAgeMismatch) {
						errorMessage.append(" - Tuổi sai, đúng phải là: ").append(obj.getAgeRecalculated());
					}
					if (isSaoMismatch) {
						errorMessage.append(" - Sao sai, đúng phải là: ").append(obj.getSaoRecalculated().getSaoName());
					}
					if (isHanMismatch) {
						errorMessage.append(" - Hạn sai, đúng phải là: ").append(obj.getHanRecalculated().getHanName());
					}

					errorMessages.add(errorMessage.toString());
					errorObjects.add(obj);
				}
			}
		}

		// Gán danh sách lỗi vào DTO
		resultDTO.setErrorObjects(errorObjects);
		resultDTO.setErrorMessages(errorMessages);

		return resultDTO;
	}

	public static void writeNotebookEntries(File templateFile, String outputFilePath,
			Map<Integer, List<MortalObject>> groupedByHostOrder) throws Exception {
		try (XWPFDocument templateDocument = new XWPFDocument(Files.newInputStream(templateFile.toPath()));
				XWPFDocument document = new XWPFDocument(Files.newInputStream(templateFile.toPath()))) {

			// 📌 Lấy bảng đầu tiên từ file template
			List<XWPFTable> tables = document.getTables();
			if (tables.isEmpty()) {
				throw new IllegalStateException("Không tìm thấy bảng trong template!");
			}

			XWPFTable table = tables.get(0);
			if (table.getNumberOfRows() < 1) {
				throw new IllegalStateException("Template phải có ít nhất 1 hàng mẫu!");
			}

			// 📌 Lấy hàng mẫu đầu tiên nhưng **KHÔNG** ghi dữ liệu vào nó
			XWPFTableRow templateRow = table.getRow(0);

			// 📌 Lấy ô thứ 3 của hàng mẫu
			XWPFTableCell templateCell = (templateRow.getTableCells().size() > 2) ? templateRow.getCell(2) : null;

			List<XWPFTableRow> newRows = new ArrayList<>(); // Danh sách các hàng mới
			for (Map.Entry<Integer, List<MortalObject>> entry : groupedByHostOrder.entrySet()) {
				int hostOrder = entry.getKey();
				List<MortalObject> familyMembers = entry.getValue();

				// 📌 Tìm chủ hộ
				MortalObject houseOwner = familyMembers.stream().filter(MortalObject::isAHost).findFirst().orElse(null);

				if (houseOwner == null) {
					continue;
				}

				// 📌 Tạo nội dung ghi vào bảng
				String formattedText = String.format(ConfigLoader.getProperty("labelHostTemplateForNotebook"),
						String.join(" ", houseOwner.getFmName(), houseOwner.getMidName(), houseOwner.getName()).trim(),
						String.join(" ", houseOwner.getThienCan(), houseOwner.getDiaChi()).trim(),
						houseOwner.getAgeRecalculated(), familyMembers.size(),
						familyMembers.stream().filter(m -> "Nam".equalsIgnoreCase(m.getGender())).count(),
						familyMembers.stream().filter(m -> "Nữ".equalsIgnoreCase(m.getGender())).count(),
						houseOwner.getAddress());

				// Nếu không có nam hoặc không có nữ, thì không cần hiển thị số lượng
				formattedText = formattedText.replace("0 nam,", "");
				formattedText = formattedText.replace("0 nữ,", "");

				// 📌 Không ghi vào hàng mẫu -> Tạo hàng mới và copy style từ templateRow
				XWPFTableRow newRow = table.createRow();
				Utils.copyRowStyle(templateRow, newRow);
				newRows.add(newRow); // ✅ Lưu hàng mới vào danh sách

				// 📌 Ghi số thứ tự hộ vào cột 1
				Utils.copyFormattedTextToCell(newRow.getCell(0), String.valueOf(hostOrder));
				newRow.getCell(0).getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);

				// 📌 Ghi thông tin chủ hộ vào cột 2
				Utils.copyFormattedTextToCell(newRow.getCell(1), formattedText);

				// 📌 Copy nội dung của ô thứ 3 từ hàng mẫu
				if (templateCell != null) {
					Utils.copyCellContent(templateCell, Utils.ensureCellExists(newRow, 2));
				}
			}

			// 🛑 Sau khi đã ghi hết dữ liệu, **xóa hàng mẫu**
			table.removeRow(0);

			// ✅ Sau khi ghi xong dữ liệu, thêm hàng header vào đầu bảng
			Utils.addTableHeader(table, "STT", "Nội Gia", "Nội dung cần ghi chép");

			// 📌 Xuất file kết quả
			try (FileOutputStream out = new FileOutputStream(outputFilePath)) {
				document.write(out);
			}
		}
	}

	public static void processWritingSo(JTextArea logTextArea, final List<MortalObject> mortalObjects, String excelFilePath) {
	    // 📌 🛑 Kiểm tra nếu chưa chọn file
	    if (mortalObjects == null || mortalObjects.isEmpty()) {
	    	JOptionPane.showMessageDialog(null, "Không có dữ liệu để tạo Sớ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    try {
	        // Lấy thư mục chứa file Excel (ưu tiên) hoặc working directory
	        String excelFileDirectory = (excelFilePath != null && !excelFilePath.isEmpty())
	        	? new File(excelFilePath).getParent()
	        	: System.getProperty("user.dir");

	        // 📌 🛠 Gọi hàm tạo danh sách nhóm SaoHạn
	        List<SaoHanGroup> saoHanGroups = createSaoHanGroups(mortalObjects);

	        // ✅ File template được tìm từ các vị trí ưu tiên
	        File templateFile = Utils.findTemplateFile(Utils.TemplateFile.SO_SAO_HAN, excelFileDirectory);

	        // ✅ Tạo file đích trong thư mục Excel
	        String outputFilePath = Utils.getUniqueFileName(excelFileDirectory + "/printSoSaoHanGenerated.docx");

	        // 📌 📝 Ghi file Word từ dữ liệu SaoHạn
	        writeSoDocument(templateFile, outputFilePath, saoHanGroups);

	        // ✅ Cập nhật log UI
	        logTextArea.append("✅ Tạo file Sớ thành công: " + outputFilePath + "\n");
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        JOptionPane.showMessageDialog(null, "Lỗi khi tạo file Sớ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
	    }
	}

	public static List<SaoHanGroup> createSaoHanGroups(List<MortalObject> mortalObjects) {
        Map<SaoChieuEnum, SaoHanGroup> saoHanMap = new LinkedHashMap<>();

        for (MortalObject obj : mortalObjects) {
            SaoChieuEnum sao = obj.getSaoRecalculated();
            HanEnum han = obj.getHanRecalculated();

            if (sao == null || han == null) {
                continue; // Bỏ qua nếu không có Sao hoặc Hạn
            }

            // Tạo SaoHanGroup nếu chưa có
            saoHanMap.putIfAbsent(sao, new SaoHanGroup(sao, new ArrayList<>(), new ArrayList<>(), new HashSet<>(), 0));

            SaoHanGroup group = saoHanMap.get(sao);

            // Thêm MortalObject vào nhóm Nam hoặc Nữ
            if ("Nam".equalsIgnoreCase(obj.getGender())) {
                group.getNamMortal().add(obj);
            } else if ("Nữ".equalsIgnoreCase(obj.getGender())) {
                group.getNuMortal().add(obj);
            }

            // Thêm Hạn vào Set
            group.getHanSet().add(han);

            // Tăng số lượng căn mạng nếu có
            if (obj.isCanMang()) {
                group.setCountCanMang(group.getCountCanMang() + 1);
            }
        }

        return new ArrayList<>(saoHanMap.values());
    }

	private static void writeSoDocument(File templateFile, String outputFilePath, List<SaoHanGroup> saoHanGroups) throws Exception {
	    try (XWPFDocument templateDocument = new XWPFDocument(Files.newInputStream(templateFile.toPath()));
	         XWPFDocument document = new XWPFDocument(Files.newInputStream(templateFile.toPath()))) {

	        // ✅ Đọc paragraph đầu tiên làm mẫu định dạng
	        XWPFParagraph referenceParagraph = !document.getParagraphs().isEmpty() ? document.getParagraphs().get(0) : null;
	        XWPFRun referenceRun = (referenceParagraph != null && !referenceParagraph.getRuns().isEmpty())
	                ? referenceParagraph.getRuns().get(0) : null;

	        // 🔥 Sao chép format paragraph trước khi xóa
	        ParagraphAlignment savedAlignment = (referenceParagraph != null) ? referenceParagraph.getAlignment() : ParagraphAlignment.LEFT;
	        double savedSpacingBetween = (referenceParagraph != null) ? referenceParagraph.getSpacingBetween() : 1.0;
	        LineSpacingRule savedSpacingRule = (referenceParagraph != null) ? referenceParagraph.getSpacingLineRule() : LineSpacingRule.EXACT;
	        int savedSpacingBefore = (referenceParagraph != null) ? referenceParagraph.getSpacingBefore() : 0;
	        int savedSpacingAfter = (referenceParagraph != null) ? referenceParagraph.getSpacingAfter() : 0;

	        // ✅ Giữ nguyên Line Spacing tính theo pt (Fix lỗi spacing 180pt)
	        int savedSpacingBeforeLines = (referenceParagraph != null) ? referenceParagraph.getSpacingBeforeLines() : 0;
	        int savedSpacingAfterLines = (referenceParagraph != null) ? referenceParagraph.getSpacingAfterLines() : 0;

	        // 🔥 Sao chép format Run trước khi xóa
	        String savedFontFamily = (referenceRun != null) ? referenceRun.getFontFamily() : "Calibri";
	        double savedFontSize = (referenceRun != null) ? referenceRun.getFontSizeAsDouble() : 12.5;
	        boolean savedBold = (referenceRun != null) && referenceRun.isBold();
	        boolean savedItalic = (referenceRun != null) && referenceRun.isItalic();
	        UnderlinePatterns savedUnderline = (referenceRun != null) ? referenceRun.getUnderline() : UnderlinePatterns.NONE;
	        String savedColor = (referenceRun != null) ? referenceRun.getColor() : "000000";

	        // ✅ Xóa paragraph mẫu sau khi sao chép format
	        if (referenceParagraph != null) {
	            document.removeBodyElement(document.getPosOfParagraph(referenceParagraph));
	        }

	        // ✅ Duyệt qua danh sách SaoHanGroup
	        for (SaoHanGroup group : saoHanGroups) {
	            List<MortalObject> allMortalObjects = new ArrayList<>();
	            allMortalObjects.addAll(group.getNamMortal()); // Nam trước
	            allMortalObjects.addAll(group.getNuMortal());  // Nữ sau

	            // ✅ Duyệt từng MortalObject
	            for (MortalObject obj : allMortalObjects) {
	                String fullName = Stream.of("-" + obj.getFmName(), obj.getMidName(), obj.getName())
	                        .filter(s -> s != null && !s.isEmpty()) // 🛑 Loại bỏ null hoặc chuỗi rỗng
	                        .collect(Collectors.joining(" "))
	                        .trim(); // 🛑 Cắt khoảng trắng dư thừa

	                String saoHanText = String.format("tuổi %s %s %dt", obj.getThienCan(), obj.getDiaChi(), obj.getAgeRecalculated());

	                // 📌 Tạo paragraph mới cho từng MortalObject, sử dụng format từ mẫu
	                XWPFParagraph paragraph = document.createParagraph();
	                paragraph.setAlignment(savedAlignment);
	                paragraph.setSpacingBetween(savedSpacingBetween, savedSpacingRule);
	                paragraph.setSpacingBefore(savedSpacingBefore);
	                paragraph.setSpacingAfter(savedSpacingAfter);

	                // ✅ Áp dụng spacingBeforeLines & spacingAfterLines (Fix lỗi spacing)
	                paragraph.setSpacingBeforeLines(savedSpacingBeforeLines);
	                paragraph.setSpacingAfterLines(savedSpacingAfterLines);

	                XWPFRun run = paragraph.createRun();
	                run.setFontFamily(savedFontFamily);
	                run.setFontSize(savedFontSize);
	                run.setBold(savedBold);
	                run.setItalic(savedItalic);
	                run.setUnderline(savedUnderline);
	                run.setColor(savedColor);

	                // 🔥 Ghi từng từ vào paragraph, đảm bảo không vỡ cấu trúc "tuổi <<thienCan>> <<diaChi>> <<ageRecalculated>>t"
	                String[] words = (fullName + " " + saoHanText).split(" ");
	                for (int i = 0; i < words.length; i++) {
	                    run.setText(words[i]);
	                    if (i < words.length - 1) {
	                        run.addBreak(); // 🛑 Chỉ xuống dòng nếu chưa phải từ cuối cùng
	                    }
	                }
	            }

	            // 📌 Thêm đoạn mô tả sau mỗi nhóm Sao Hạn
	            int totalInGroup = allMortalObjects.size();
	            int canMangCount = group.getCountCanMang();
	            String saoFullName = group.getSaoChieu().getSaoFullName();

	            // Tạo danh sách Hạn không trùng lặp
	            String hanList = group.getHanSet().stream()
	                    .map(HanEnum::getHanName)
	                    .collect(Collectors.joining(", "));

	            // Format chuỗi mô tả (đổi thứ tự: totalInGroup/canMangCount)
	            String summaryText = String.format(
	                ConfigLoader.getProperty("summaryTitleForGroupSao"),
	                totalInGroup, canMangCount, saoFullName, hanList
	            );

	            // Ghi đoạn mô tả vào sớ, mỗi từ một dòng
	            // Riêng *Có, %d/%d coi như một từ
	            summaryText = summaryText.replace("*Có", "*Có ")
	                                     .replace(totalInGroup + "/" + canMangCount, totalInGroup + "/" + canMangCount + " ");

	            String[] summaryWords = summaryText.split(" ");
	            for (String word : summaryWords) {
	                if (word.trim().isEmpty()) continue;

	                XWPFParagraph summaryParagraph = document.createParagraph();
	                summaryParagraph.setAlignment(savedAlignment);
	                summaryParagraph.setSpacingBetween(savedSpacingBetween, savedSpacingRule);
	                summaryParagraph.setSpacingBefore(savedSpacingBefore);
	                summaryParagraph.setSpacingAfter(savedSpacingAfter);
	                summaryParagraph.setSpacingBeforeLines(savedSpacingBeforeLines);
	                summaryParagraph.setSpacingAfterLines(savedSpacingAfterLines);

	                XWPFRun summaryRun = summaryParagraph.createRun();
	                summaryRun.setFontFamily(savedFontFamily);
	                summaryRun.setFontSize(savedFontSize);
	                summaryRun.setBold(savedBold);
	                summaryRun.setItalic(savedItalic);
	                summaryRun.setUnderline(savedUnderline);
	                summaryRun.setColor(savedColor);
	                summaryRun.setText(word.trim());
	            }

	            // ✅ Khi hết 1 SaoHanGroup, tạo trang mới (chỉ page break, không cần thêm empty lines)
	            document.createParagraph().setPageBreak(true);
	        }

	        // ✅ Ghi file ra output
	        try (FileOutputStream out = new FileOutputStream(outputFilePath)) {
	            document.write(out);
	        }
	    }
	}


	/**
	 * 📌 **Tính toán Exactly Spacing cho Paragraph**
	 */
	@SuppressWarnings("unused")
	private static double calculateExactSpacing(XWPFDocument document, int minLines, int maxLines) {
	    // Lấy thông tin chiều dài trang từ template
	    double pageHeight = 35.57; // cm (mặc định từ template)
	    double topMargin = 1.0;    // cm
	    double bottomMargin = 0.8; // cm

	    // Tính khoảng trống thực tế
	    double usableHeight = pageHeight - (topMargin + bottomMargin);

	    // Tính khoảng cách dòng dựa trên số dòng tối thiểu và tối đa
	    return usableHeight / minLines;
	}

}
