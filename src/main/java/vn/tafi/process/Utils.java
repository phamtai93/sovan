package vn.tafi.process;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import vn.tafi.object.DiaChi;
import vn.tafi.object.ThienCan;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

public class Utils {


	/**
	 * Enum for template files with their names
	 */
	public enum TemplateFile {
		COVER("printCoverTemplate.docx"),
		LABEL_SAO_HAN("printLabelSaoHanTemplate.docx"),
		NOTEBOOK("printNotebookTemplate.docx"),
		SO_SAO_HAN("printSoSaoHanTemplate.docx");

		private final String fileName;

		TemplateFile(String fileName) {
			this.fileName = fileName;
		}

		public String getFileName() {
			return fileName;
		}
	}

	/**
	 * Tính toán Can Giáp từ năm Tây lịch
	 * Can Giáp = (Thiên Can) + (Địa Chi) dựa trên vòng 60 năm
	 *
	 * Công thức:
	 * - Mỗi năm trôi qua tăng 1 Thiên Can và 1 Địa Chi
	 * - Tham chiếu: 1945 = Ất Dậu (Can index 1, Chi index 9)
	 *
	 * @param year năm Tây lịch (ví dụ: 2026)
	 * @return Can Giáp dưới dạng String (ví dụ: "Bính Ngọ")
	 */
	public static String calculateCanChiYear(int year) {
		// Sử dụng năm 1945 (Ất Dậu) làm tham chiếu
		int yearsFrom1945 = year - 1945;
		int canIndex = (1 + yearsFrom1945) % 10;
		int chiIndex = (9 + yearsFrom1945) % 12;

		// Đảm bảo index không âm
		if (canIndex < 0) canIndex += 10;
		if (chiIndex < 0) chiIndex += 12;

		ThienCan can = ThienCan.fromIndex(canIndex);
		DiaChi chi = DiaChi.fromIndex(chiIndex);

		return can.getDisplayName() + " " + chi.getDisplayName();
	}

	public static boolean isEqualIgnoreNull(String str1, String str2) {
	    return Objects.equals(str1 == null ? "" : str1, str2 == null ? "" : str2);
	}

	public static String getCurrentTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return sdf.format(new Date());
	}

	public static String getUniqueFileName(String filePath) {
		File file = new File(filePath);
		int counter = 1;
		while (file.exists()) {
			String baseName = filePath.substring(0, filePath.lastIndexOf(".docx"));
			String newName = baseName + "-" + String.format("%02d", counter) + ".docx";
			file = new File(newName);
			counter++;
		}
		return file.getAbsolutePath();
	}

	public static String convertNumberToChinese(int number) {
		String[] chineseNumerals = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
		String[] units = { "", "十", "百", "千" };
		StringBuilder chineseNumber = new StringBuilder();
		String numStr = String.valueOf(number);
		int length = numStr.length();
		for (int i = 0; i < length; i++) {
			int digit = Character.getNumericValue(numStr.charAt(i));
			if (digit != 0 || (i > 0 && numStr.charAt(i - 1) != '0')) {
				chineseNumber.append(chineseNumerals[digit]).append(" ");
				if (digit != 0 && i < length - 1) {
					chineseNumber.append(units[length - i - 1]).append(" ");
				}
			} else if (chineseNumber.length() > 0 && chineseNumber.charAt(chineseNumber.length() - 1) != '零') {
				chineseNumber.append("零").append(" ");
			}
		}
		if (chineseNumber.toString().endsWith("零 ")) {
			chineseNumber.replace(chineseNumber.length() - 2, chineseNumber.length(), ".");
		}
		return chineseNumber.toString();
	}

	public static String convertNumberToVietnamese(int number) {
		String[] vietnameseNumerals = { ".", "Nhất", "Nhị", "Tam", "Tứ", "Ngũ", "Lục", "Thất", "Bát", "Cửu" };
		String[] units = { "", "Thập", "Bách", "Thiên" };
		StringBuilder vietnameseNumber = new StringBuilder();
		String numStr = String.valueOf(number);
		int length = numStr.length();
		for (int i = 0; i < length; i++) {
			int digit = Character.getNumericValue(numStr.charAt(i));
			if (digit != 0 || (i > 0 && numStr.charAt(i - 1) != '0')) {
				vietnameseNumber.append(vietnameseNumerals[digit]).append(" ");
				if (digit != 0 && i < length - 1) {
					vietnameseNumber.append(units[length - i - 1]).append(" ");
				}
			} else if (vietnameseNumber.length() > 0 && vietnameseNumber.charAt(vietnameseNumber.length() - 1) != ' ') {
				vietnameseNumber.append("Không").append(" ");
			}
		}
		if (vietnameseNumber.toString().endsWith("Không ")) {
			vietnameseNumber.replace(vietnameseNumber.length() - 6, vietnameseNumber.length(), ".");
		}
		return vietnameseNumber.toString();
	}

	public static Integer getIntegerValue(Row row, int colIndex, FormulaEvaluator evaluator) {
		String value = getStringValue(row, colIndex, evaluator);
		try {
			return value.isEmpty() ? null : Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return null; // Nếu không thể chuyển đổi, trả về null
		}
	}

	public static boolean getBooleanValue(Row row, int colIndex, FormulaEvaluator evaluator) {
		String value = getStringValue(row, colIndex, evaluator).toLowerCase();
		return value.equals("x"); // Chỉ true nếu cell chứa "x"
	}

	public static String getStringValue(Row row, int colIndex, FormulaEvaluator evaluator) {
		Cell cell = row.getCell(colIndex);
		if (cell == null)
			return "";

		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue().trim();
		case NUMERIC:
			return String.valueOf((int) cell.getNumericCellValue()); // Nếu là số, chuyển thành String
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case FORMULA:
			return getFormulaCellValue(cell, evaluator); // Xử lý công thức
		default:
			return "";
		}
	}

	public static String getFormulaCellValue(Cell cell, FormulaEvaluator evaluator) {
		CellValue cellValue = evaluator.evaluate(cell);

		switch (cellValue.getCellType()) {
		case STRING:
			return cellValue.getStringValue().trim();
		case NUMERIC:
			return String.valueOf((int) cellValue.getNumberValue()); // Chuyển số sang String
		case BOOLEAN:
			return String.valueOf(cellValue.getBooleanValue());
		default:
			return "";
		}
	}

	/**
	 * Right text to cell and keep format
	 * @param cell
	 * @param text
	 */
	public static void copyFormattedTextToCell(XWPFTableCell cell, String text) {
	    // Xóa tất cả đoạn văn bản cũ thay vì chỉ xóa 1 dòng đầu tiên
	    while (cell.getParagraphs().size() > 0) {
	        cell.removeParagraph(0);
	    }

	    // ✅ Tạo đoạn văn bản mới
	    XWPFParagraph paragraph = cell.addParagraph();
	    XWPFRun run = paragraph.createRun();
	    run.setFontFamily("Calibri");
	    run.setFontSize(18);

	    // 🔥 Ghi từng dòng và đảm bảo không có dòng trống đầu
	    String[] lines = text.split("\n");
	    for (int i = 0; i < lines.length; i++) {
	        run.setText(lines[i]);
	        if (i < lines.length - 1) {
	            run.addBreak(); // ✅ Chỉ xuống dòng nếu không phải dòng cuối
	        }
	    }
	}


	/**
	 * 📌 **Sao chép định dạng từ hàng mẫu sang hàng mới**
	 */
	public static void copyRowStyle(XWPFTableRow sourceRow, XWPFTableRow targetRow) {
		targetRow.setCantSplitRow(sourceRow.isCantSplitRow());
		targetRow.setRepeatHeader(sourceRow.isRepeatHeader());

		// ✅ Đảm bảo số cột trong hàng mới đúng với hàng gốc
		while (targetRow.getTableCells().size() < sourceRow.getTableCells().size()) {
			targetRow.addNewTableCell();
		}

		for (int i = 0; i < sourceRow.getTableCells().size(); i++) {
			XWPFTableCell sourceCell = sourceRow.getCell(i);
			XWPFTableCell targetCell = targetRow.getCell(i);

			targetCell.setVerticalAlignment(sourceCell.getVerticalAlignment());
			targetCell.getCTTc().setTcPr(sourceCell.getCTTc().getTcPr());

			for (XWPFParagraph sourceParagraph : sourceCell.getParagraphs()) {
				XWPFParagraph targetParagraph = targetCell.addParagraph();

				// ⭐ Giữ căn chỉnh của đoạn văn bản (Trái/Phải/Giữa)
				targetParagraph.setAlignment(sourceParagraph.getAlignment());

				for (XWPFRun sourceRun : sourceParagraph.getRuns()) {
					XWPFRun targetRun = targetParagraph.createRun();

					targetRun.setFontFamily(sourceRun.getFontFamily());
					targetRun.setFontSize(sourceRun.getFontSizeAsDouble());
					targetRun.setBold(sourceRun.isBold());
					targetRun.setItalic(sourceRun.isItalic());
					targetRun.setUnderline(sourceRun.getUnderline());
					targetRun.setColor(sourceRun.getColor());
					targetRun.setText(sourceRun.text());

					// ✅ Copy thuộc tính spacing và indent nếu có
					targetParagraph.setSpacingAfter(sourceParagraph.getSpacingAfter());
					targetParagraph.setSpacingBefore(sourceParagraph.getSpacingBefore());
					targetParagraph.setSpacingBetween(sourceParagraph.getSpacingBetween(), LineSpacingRule.AUTO);
					targetParagraph.setIndentationLeft(sourceParagraph.getIndentationLeft());
					targetParagraph.setIndentationRight(sourceParagraph.getIndentationRight());
				}
			}
		}

		// ✅ Giữ chiều cao hàng không bị thu nhỏ
		if (sourceRow.getCtRow().isSetTrPr()) {
			targetRow.getCtRow().setTrPr(sourceRow.getCtRow().getTrPr());
		}
	}

	/**
	 * Copy cenn content
	 * @param sourceCell
	 * @param targetCell
	 */
	public static void copyCellContent(XWPFTableCell sourceCell, XWPFTableCell targetCell) {
		if (sourceCell == null || targetCell == null)
			return;

		// ✅ Copy định dạng ô
		targetCell.getCTTc().setTcPr(sourceCell.getCTTc().getTcPr());

		// ✅ Sao chép tất cả đoạn văn từ ô mẫu sang ô mới
		for (XWPFParagraph sourceParagraph : sourceCell.getParagraphs()) {
			XWPFParagraph targetParagraph = targetCell.addParagraph();
			targetParagraph.setAlignment(sourceParagraph.getAlignment());

			for (XWPFRun sourceRun : sourceParagraph.getRuns()) {
				XWPFRun targetRun = targetParagraph.createRun();

				// ✅ Giữ nguyên định dạng
				targetRun.setFontFamily(sourceRun.getFontFamily());
				targetRun.setFontSize(sourceRun.getFontSizeAsDouble());
				targetRun.setBold(sourceRun.isBold());
				targetRun.setItalic(sourceRun.isItalic());
				targetRun.setUnderline(sourceRun.getUnderline());
				targetRun.setColor(sourceRun.getColor());
			}
		}
	}

	/**
	 * Copy format
	 * @param paragraph
	 * @param source
	 * @param target
	 * @param referParagraph
	 */
	public static void copyRunFormatting(XWPFParagraph paragraph, XWPFRun source, XWPFRun target,
			XWPFParagraph referParagraph) {
		target.setFontFamily(source.getFontFamily());
		target.setFontSize(source.getFontSizeAsDouble());
		target.setBold(source.isBold());
		target.setItalic(source.isItalic());
		target.setUnderline(source.getUnderline());
		target.setColor(source.getColor());
		if (referParagraph != null) {
			paragraph.setSpacingLineRule(referParagraph.getSpacingLineRule());
			paragraph.setSpacingBetween(referParagraph.getSpacingBetween() / 20.0);
			paragraph.setSpacingAfter(referParagraph.getSpacingAfter());
			paragraph.setSpacingBefore(referParagraph.getSpacingBefore());
		}
	}

	/**
	 * 📌 **Sao chép đầy đủ định dạng của `sourceRun` sang `targetRun`**
	 */
	public static void copyRunFormatting(XWPFRun sourceRun, XWPFRun targetRun) {
	    if (sourceRun == null || targetRun == null) return;

	    // ✅ Sao chép định dạng văn bản (Font, Cỡ chữ, Màu, Kiểu)
	    targetRun.setFontFamily(sourceRun.getFontFamily());
	    targetRun.setFontSize(sourceRun.getFontSizeAsDouble());
	    targetRun.setBold(sourceRun.isBold());
	    targetRun.setItalic(sourceRun.isItalic());
	    targetRun.setUnderline(sourceRun.getUnderline());
	    targetRun.setColor(sourceRun.getColor());
	    targetRun.setStrikeThrough(sourceRun.isStrikeThrough());
	    targetRun.setCapitalized(sourceRun.isCapitalized());
	    targetRun.setSmallCaps(sourceRun.isSmallCaps());

	    // ✅ Sao chép các thuộc tính đặc biệt (RPr)
	    if (sourceRun.getCTR().isSetRPr()) {
	        targetRun.getCTR().setRPr((CTRPr) sourceRun.getCTR().getRPr().copy());
	    }
	}

	/**
	 * Lưu giá trị vào paragraph và thu nhỏ size của dòng
	 * @param paragraph
	 * @param text
	 * @param referenceParagraph
	 */
	public static void addNewTextWithSmallSize(XWPFParagraph paragraph, String text, XWPFParagraph referenceParagraph) {
		XWPFRun referenceRun = referenceParagraph.getRuns().get(0);
		paragraph.getRuns().forEach(run -> run.setText("", 0));
		XWPFRun newRun = paragraph.createRun();
		Utils.copyRunFormatting(paragraph, referenceRun, newRun, referenceParagraph);
		newRun.setText(text);
		newRun.setFontSize((double) 4); // Set font size to 4
		paragraph.setSpacingLineRule(LineSpacingRule.EXACT);
		paragraph.setSpacingBetween(4 / 20.0); // Set spacing to 4pt
	}

	/**
	 * Cập nhật nội dung và copy format 
	 * @param paragraph
	 * @param newText
	 * @param referenceParagraph
	 */
	public static void copyAndReplaceWithReference(XWPFParagraph paragraph, String newText,
			XWPFParagraph referenceParagraph) {
		XWPFRun referenceRun = referenceParagraph.getRuns().get(0); // Use the first run as a reference
		paragraph.getRuns().forEach(run -> run.setText("", 0));
		XWPFRun newRun = paragraph.createRun();
		Utils.copyRunFormatting(paragraph, referenceRun, newRun, referenceParagraph);
		newRun.setText(newText);
	}

	/**
	 * Cập nhật nội dung và copy format
	 * @param paragraph
	 * @param runs
	 * @param updatedText
	 */
	public static void replaceTextWithFormatting(XWPFParagraph paragraph, String updatedText, List<XWPFRun> runs) {
		int currentIndex = 0;

		for (XWPFRun run : runs) {
			String runText = run.getText(0);
			if (runText == null)
				continue;

			int runLength = runText.length();
			if (currentIndex >= updatedText.length()) {
				run.setText("", 0);
				continue;
			}

			int endIndex = Math.min(currentIndex + runLength, updatedText.length());
			String newText = updatedText.substring(currentIndex, endIndex);

			run.setText(newText, 0);
			currentIndex += newText.length();
		}

		if (currentIndex < updatedText.length()) {
			String remainingText = updatedText.substring(currentIndex);
			XWPFRun newRun = paragraph.createRun();
			XWPFRun referenceRun = runs.get(0); // Copy formatting from the first run in the paragraph
			Utils.copyRunFormatting(paragraph, referenceRun, newRun, null);
			newRun.setText(remainingText);
		}
	}

	/**
	 * 📌 **Thêm header vào đầu bảng**
	 */
	public static void addTableHeader(XWPFTable table, String... headerRows) {
		XWPFTableRow headerRow = table.insertNewTableRow(0); // Thêm hàng mới ở đầu bảng

		// 🔥 Đánh dấu hàng này là header để lặp lại qua trang mới
		headerRow.setRepeatHeader(true);

	    // ✅ Duyệt qua danh sách headers và tạo từng ô header
	    for (int i = 0; i < headerRows.length; i++) {
	        createHeaderCell(headerRow, i, headerRows[i]);
	    }
	}

	/**
	 * 📌 **Tạo ô header với định dạng chuẩn**
	 */
	private static void createHeaderCell(XWPFTableRow row, int cellIndex, String text) {
		XWPFTableCell cell = row.createCell();

		XWPFParagraph paragraph = cell.getParagraphs().get(0);
		paragraph.setAlignment(ParagraphAlignment.CENTER);

		XWPFRun run = paragraph.createRun();
		run.setFontFamily("Calibri");
		run.setFontSize(18);
		run.setBold(true);
		run.setText(text);

		// ✅ Đặt màu nền cho ô (xám nhạt)
		cell.setColor("D9D9D9");
	}


	/**
	 * Check table row does not have enough cell. If not, create some
	 * @param row
	 * @param columnIndex
	 * @return
	 */
	public static XWPFTableCell ensureCellExists(XWPFTableRow row, int columnIndex) {
		while (row.getTableCells().size() <= columnIndex) {
			row.addNewTableCell();
		}
		return row.getCell(columnIndex);
	}

	@Deprecated
	public static void copyFormatAndWrite(XWPFDocument document, String textContent) {
	    List<XWPFParagraph> paragraphs = document.getParagraphs();
	    if (paragraphs.isEmpty()) {
	        throw new IllegalStateException("File mẫu không có nội dung để sao chép định dạng!");
	    }

	    // 📌 Lấy dòng đầu tiên để làm mẫu format
	    XWPFParagraph firstParagraph = paragraphs.get(0);
	    XWPFRun firstRun = firstParagraph.getRuns().isEmpty() ? null : firstParagraph.getRuns().get(0);

	    // 🛑 Xóa dòng mẫu đầu tiên trước khi ghi dữ liệu mới
	    document.removeBodyElement(document.getPosOfParagraph(firstParagraph));

	    // 📌 Tạo một đoạn văn mới để ghi nội dung
	    XWPFParagraph newParagraph = document.createParagraph();
	    XWPFRun newRun = newParagraph.createRun();

	    // ✅ Nếu tìm thấy định dạng từ dòng đầu tiên, sao chép vào dòng mới
	    if (firstRun != null) {
	        newRun.setFontFamily(firstRun.getFontFamily());
	        newRun.setFontSize(firstRun.getFontSizeAsDouble());
	        newRun.setBold(firstRun.isBold());
	        newRun.setItalic(firstRun.isItalic());
	        newRun.setUnderline(firstRun.getUnderline());
	        newRun.setColor(firstRun.getColor());
	    } else {
	        // 🛑 Nếu không tìm thấy, sử dụng định dạng mặc định
	        newRun.setFontFamily("Calibri");
	        newRun.setFontSize(14);
	        newRun.setBold(true);
	    }

	    // 🔥 Ghi nội dung vào tài liệu
	    newRun.setText(textContent);
	}

	/**
	 * Tìm file template theo thứ tự ưu tiên sử dụng enum:
	 * 1. Cùng thư mục với file Excel được chọn
	 * 2. Thư mục cài đặt ứng dụng (nơi JAR/EXE)
	 * 3. Resources trong JAR
	 * 4. Working directory
	 *
	 * @param template TemplateFile enum value
	 * @param excelFileDirectory Thư mục chứa file Excel được chọn (hoặc null)
	 * @return File template được tìm thấy, hoặc null nếu không tìm
	 */
	public static File findTemplateFile(TemplateFile template, String excelFileDirectory) {
		return findTemplateFile(template.getFileName(), excelFileDirectory);
	}

	/**
	 * Tìm file template theo thứ tự ưu tiên:
	 * 1. Cùng thư mục với file Excel được chọn
	 * 2. Thư mục cài đặt ứng dụng (nơi JAR/EXE)
	 * 3. Resources trong JAR
	 * 4. Working directory
	 *
	 * @param templateName Tên file template (ví dụ: "printCoverTemplate.docx")
	 * @param excelFileDirectory Thư mục chứa file Excel được chọn (hoặc null)
	 * @return File template được tìm thấy, hoặc null nếu không tìm
	 */
	public static File findTemplateFile(String templateName, String excelFileDirectory) {
		// 1. Tìm ở cùng thư mục với file Excel (ưu tiên cao nhất)
		if (excelFileDirectory != null && !excelFileDirectory.isEmpty()) {
			File excelDirTemplate = new File(excelFileDirectory, templateName);
			if (excelDirTemplate.exists()) {
				System.out.println("Found template: " + excelDirTemplate.getAbsolutePath());
				return excelDirTemplate;
			}
		}

		// 2. Tìm ở thư mục cài đặt ứng dụng (nơi JAR được cài đặt)
		try {
			String jarPath = Utils.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath();
			File appDir = new File(jarPath).getParentFile();
			File appDirTemplate = new File(appDir, templateName);
			if (appDirTemplate.exists()) {
				System.out.println("Found template: " + appDirTemplate.getAbsolutePath());
				return appDirTemplate;
			}
		} catch (Exception e) {
			System.err.println("Error finding app directory: " + e.getMessage());
		}

		// 3. Tìm trong resources của JAR
		try {
			java.io.InputStream resourceStream = Utils.class.getClassLoader()
					.getResourceAsStream(templateName);
			if (resourceStream != null) {
				// Tạo file tạm thời từ resource stream
				File tempFile = File.createTempFile(templateName.replace(".docx", ""), ".docx");
				tempFile.deleteOnExit();
				try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
					byte[] buffer = new byte[1024];
					int length;
					while ((length = resourceStream.read(buffer)) != -1) {
						fos.write(buffer, 0, length);
					}
				}
				resourceStream.close();
				System.out.println("Found template from resources: " + tempFile.getAbsolutePath());
				return tempFile;
			}
		} catch (Exception e) {
			System.err.println("Error loading template from resources: " + e.getMessage());
		}

		// 4. Tìm ở working directory (fallback)
		File workingDirTemplate = new File(System.getProperty("user.dir"), templateName);
		if (workingDirTemplate.exists()) {
			System.out.println("Found template: " + workingDirTemplate.getAbsolutePath());
			return workingDirTemplate;
		}

		System.err.println("Template not found: " + templateName);
		return null;
	}

	// ============================================================================
	// UI STYLING HELPERS - Centralized styling methods for consistent UI appearance
	// ============================================================================

	/**
	 * Style JSpinner with standard appearance
	 * @param spinner The JSpinner component to style
	 */
	public static void styleSpinner(JSpinner spinner) {
		spinner.setFont(new Font("Calibri", Font.PLAIN, 14));
		spinner.setBackground(java.awt.Color.WHITE);
		spinner.setForeground(new Color(30, 35, 40));
	}

	/**
	 * Style JTextField with standard appearance
	 * @param textField The JTextField component to style
	 */
	public static void styleTextField(JTextField textField) {
		textField.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));
		textField.setForeground(new Color(30, 35, 40));
		textField.setBackground(java.awt.Color.WHITE);
		textField.setMargin(new Insets(6, 8, 6, 8));
		textField.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(180, 195, 210), 1),
				BorderFactory.createEmptyBorder(4, 6, 4, 6)));
	}

	/**
	 * Style JButton with standard appearance
	 * @param button The JButton component to style
	 */
	public static void styleButton(JButton button) {
		button.setFont(new Font("Arial Unicode MS", Font.PLAIN, 16));
		button.setFocusPainted(false);
		button.setMargin(new Insets(8, 15, 8, 15));
		button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
		button.setBackground(new Color(40, 167, 69)); // Green
		button.setForeground(java.awt.Color.WHITE);
		button.setOpaque(true);
		button.setBorderPainted(true);
	}

	/**
	 * Display message dialog with custom styled font
	 * @param message The message text to display
	 * @param title The dialog title
	 * @param messageType The type of message (JOptionPane.ERROR_MESSAGE, INFORMATION_MESSAGE, etc.)
	 */
	public static void showMessageWithFont(String message, String title, int messageType) {
		JLabel label = new JLabel(message);
		label.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));
		label.setForeground(new Color(230, 235, 240)); // Light gray, gần trắng
		JOptionPane.showMessageDialog(null, label, title, messageType);
	}

}
