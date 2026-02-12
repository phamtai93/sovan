package vn.tafi.process;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import vn.tafi.object.ConfigLoader;

public class ContentDocProcessingHelper {

	public static void processFormatVietChar(JPanel fileListPanel, JTextArea logArea, Map<String, String> filePathMap) {
		Component[] fileLabels = fileListPanel.getComponents();
		if (fileLabels.length == 0) {
			Utils.showMessageWithFont("Hãy chọn file trước khi nhấn Cập nhật.", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Regex cho từ tiếng Việt (có dấu)
		String vietCharRegex = "(?iu)[aăâáàảãạắằẳẵặấầẩẫậbcdđeêéèẻẽẹếềểễệfghiíĩỉịìklmnoôơóòỏõọốồổỗộớờởỡợpqrstuưúùủũụứừửữựvwxyýỳỷỹỵz]+";

		Pattern vietCharPattern = Pattern.compile(vietCharRegex);

		for (Component comp : fileLabels) {
			if (comp instanceof JLabel) {
				JLabel label = (JLabel) comp;
				String fileName = label.getText();
				String filePath = filePathMap.get(fileName);

				// ✅ Mở file gốc
				Path originalFilePath = Paths.get(filePath);

				// ✅ Tạo tên file mới với hậu tố "_formatted"
				Path outputFilePath = originalFilePath.getParent()
						.resolve(originalFilePath.getFileName().toString().replace(".docx", "_formatted.docx"));

				// ✅ Tiến hành mở và chỉnh sửa file (format)
				try (XWPFDocument document = new XWPFDocument(Files.newInputStream(originalFilePath))) {
					for (XWPFParagraph paragraph : document.getParagraphs()) {
						List<XWPFRun> oldRuns = new ArrayList<>(paragraph.getRuns()); // Clone tránh bị detach

						for (int i = 0; i < oldRuns.size(); i++) {
							XWPFRun oldRun = oldRuns.get(i);
							String text = oldRun.getText(0);
							if (text == null)
								continue;

							Matcher matcher = vietCharPattern.matcher(text);
							if (matcher.find()) {
								// ✅ Remove old run
								int runIndex = paragraph.getRuns().indexOf(oldRun);
								paragraph.removeRun(runIndex); // Xóa run cũ

								// ✅ Chèn lại văn bản với format mới
								XWPFRun formattedRun = paragraph.insertNewRun(runIndex);
								formattedRun.setText(matcher.group());
								formattedRun.setFontFamily("Calibri");
								formattedRun.setFontSize(13);
							}
						}
					}

					// ✅ Ghi file mới
					try (FileOutputStream out = new FileOutputStream(outputFilePath.toFile())) {
						document.write(out);
					}

					logArea.append("✅ Đã tạo file mới: " + outputFilePath.getFileName() + "\n");
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Lỗi khi xử lý file: " + e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 * Chọn document
	 * @param fileLabel
	 * @param fileListPanel
	 * @param filePathMap
	 */
	public static void processSelectDocFiles(JLabel fileLabel, JPanel fileListPanel, Map<String, String> filePathMap) {
		JFileChooser fileChooser = new JFileChooser();
		// Set the current directory to the directory containing the JAR file
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fileChooser.setMultiSelectionEnabled(true); // Allow multiple file selection
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File[] selectedFiles = fileChooser.getSelectedFiles();
			addFilesToPanel(selectedFiles, fileLabel, fileListPanel, filePathMap);
		}
	}

	/**
	 * Helper: Add files to the file list panel (used by both JFileChooser and drag-and-drop)
	 */
	static void addFilesToPanel(File[] files, JLabel fileLabel, JPanel fileListPanel, Map<String, String> filePathMap) {
		fileListPanel.removeAll(); // Clear the previous file list
		filePathMap.clear(); // Clear the stored paths
		if (files.length > 0) {
			fileLabel.setText("");
			for (File file : files) {
				// Store file name and path
				filePathMap.put(file.getName(), file.getAbsolutePath());
				JLabel fileNameLabel = new JLabel(file.getName());
				// Add spacing between items
				fileNameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
				fileListPanel.add(fileNameLabel);
			}
		} else {
			fileLabel.setText("Chưa chọn file! (Bạn có thể chọn nhiều file cùng lúc)");
		}
		fileListPanel.revalidate();
		fileListPanel.repaint();
	}

	/**
	 * Xử lý cập nhật năm và tuổi trong các file
	 * @param fileListPanel
	 * @param logArea
	 * @param filePathMap
	 */
	public static void processUpdatingYearAndAge(JPanel fileListPanel, JTextArea logArea,
			Map<String, String> filePathMap) {
		Component[] fileLabels = fileListPanel.getComponents();
		if (fileLabels.length == 0) {
			Utils.showMessageWithFont("Hãy chọn file trước khi nhấn Cập nhật.", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			// ✅ Tạo panel chứa hướng dẫn + input
			JPanel panel = new JPanel(new BorderLayout(10, 10));

			// 🔹 Thêm hướng dẫn
			JLabel messageLabel = new JLabel("<html>Hãy nhập <b>số năm của Sớ</b> vào ô bên dưới,<br>"
					+ "hoặc để trống nếu muốn phần mềm tự động xác định<br>"
					+ "dựa vào phần cuối nội dung sớ.</html>");

			// 🔹 Tạo input field
			JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel inputLabel = new JLabel("Năm: ");
			JTextField yearOfFileField = new JTextField(10);
			Utils.styleTextField(yearOfFileField);
			inputPanel.add(inputLabel);
			inputPanel.add(yearOfFileField);

			// 🔹 Ghép các thành phần lại
			panel.add(messageLabel, BorderLayout.NORTH);
			panel.add(inputPanel, BorderLayout.CENTER);

			// ✅ Hiển thị popup dialog
			int result = JOptionPane.showOptionDialog(null, panel, "Tùy chỉnh tuổi tăng thêm",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					new String[] { "Tiếp tục", "Hủy" }, "Tiếp tục");

			if (result == JOptionPane.YES_OPTION) {
				String inputText = yearOfFileField.getText().trim();
				Integer yearOfFile = null;

				if (!inputText.isEmpty()) {
					try {
						yearOfFile = Integer.parseInt(inputText);
					} catch (NumberFormatException ex) {
						Utils.showMessageWithFont("Vui lòng nhập số nguyên hợp lệ.", "Lỗi",
								JOptionPane.ERROR_MESSAGE);
						return; // Không tiếp tục nếu nhập sai
					}
				}
				// ✅ Gọi xử lý với giá trị ageIncrease
				ageUpdatingMainProcess(logArea, filePathMap, fileLabels, yearOfFile);
			}

			
		} catch (Exception ex) {
			ex.printStackTrace();
			Utils.showMessageWithFont("Lỗi xử lý file: " + ex.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void ageUpdatingMainProcess(JTextArea logArea, Map<String, String> filePathMap,
			Component[] fileLabels, Integer yearOfFile) throws Exception {
		String yearStr = ConfigLoader.getProperty("year");
		String lunaYearStr = ConfigLoader.getProperty("lunaYear");
		if (yearStr == null || yearStr.isEmpty() || lunaYearStr == null || lunaYearStr.isEmpty()) {
			Utils.showMessageWithFont("File cấu hình phải có \"year\" và \"lunaYearStr\"!", "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		int year = Integer.parseInt(yearStr.trim());
		int buddhistYear = year + 544;
		String buddhistYearInChinese = Utils.convertNumberToChinese(buddhistYear);
		String vietnameseNumerals = Utils.convertNumberToVietnamese(buddhistYear);

		StringBuilder logs = new StringBuilder();
		Map<String, String> processedFiles = new HashMap<>(); // Track original -> updated file paths

		for (Component comp : fileLabels) {
			if (comp instanceof JLabel) {
				String fileName = ((JLabel) comp).getText();
				String filePath = filePathMap.get(fileName);
				if (filePath != null) {
					String outputFilePath = Utils.getUniqueFileName(filePath.replace(".docx", "_updated.docx"));
					// Store file pair for later file operations
					processedFiles.put(filePath, outputFilePath);

					// Process age update
					logs.append(processUpdatingAgeIntext(filePath, outputFilePath, buddhistYear, yearOfFile)).append("\n");

					// Process year update
					processUpdatingBuddhistYearInText(outputFilePath, buddhistYear, buddhistYearInChinese,
							vietnameseNumerals);

					// Process lunaYear update
					processUpdatingLunaYearInText(outputFilePath, lunaYearStr);
					logs.append("Năm đã được cập nhật sang: ").append(year).append("-").append(lunaYearStr)
							.append(", Phật lịch:").append(buddhistYear).append("\n");
				}
			}
		}
		logArea.setText(logs.toString());

		// Display file replacement dialog with 3 options
		String[] options = { "Không", "Có, dùng tên mới", "Có, dùng lại tên cũ" };
		int choice = JOptionPane.showOptionDialog(
				null,
				"Bạn có muốn thay thế các file cũ bằng các file mới không?",
				"Xác nhận thay thế file",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]  // Default: "Không"
		);

		// Handle user choice
		if (choice == 0) {
			// "Không" - Do nothing, keep both old and new files
			// Dialog closes automatically
		} else if (choice == 1) {
			// "Có, dùng tên mới" - Delete old files, keep new files with "_updated"
			handleDeleteOldFiles(processedFiles);
		} else if (choice == 2) {
			// "Có, dùng lại tên cũ" - Delete old files, rename new files to original names
			handleRenameToOriginalNames(processedFiles);
		}
		// If user closes dialog (choice == -1), do nothing
	}

	private static String processUpdatingAgeIntext(String filePath, String outputFilePath, int buddhistYear,
			Integer yearOfFile) throws Exception {
		File inputFile = new File(filePath);
		String logFilePath = new File(outputFilePath).getParent() + "/update_log.log";

		// Read and process the file
		XWPFDocument document = new XWPFDocument(Files.newInputStream(inputFile.toPath()));
		StringBuilder logBuilder = new StringBuilder();

		logBuilder.append("\n\n--------------------\n");
		logBuilder.append("###").append(inputFile.getName()).append("\n");
		logBuilder.append("[").append(Utils.getCurrentTimestamp()).append("]\n");

		int offset = 0;
		if (yearOfFile != null) {
			offset = buddhistYear - (yearOfFile + 544);
		} else {
			for (XWPFParagraph paragraph : document.getParagraphs()) {
				Matcher yearMatcher = Pattern.compile("\\((\\d{4})\\)").matcher(paragraph.getText());
				if (yearMatcher.find()) {
					int currentYear = Integer.parseInt(yearMatcher.group(1));
					offset = buddhistYear - currentYear;
					logBuilder.append("Số tuổi cần tăng: " + offset + "\n");
					break;
				}
			}
		}

		int changeCount = 0;
		int logIndex = 1;
		for (XWPFParagraph paragraph : document.getParagraphs()) {
			List<XWPFRun> runs = paragraph.getRuns();
			if (runs != null && !runs.isEmpty()) {
				StringBuilder paragraphText = new StringBuilder();
				for (XWPFRun run : runs) {
					paragraphText.append(run.getText(0));
				}

				String originalText = paragraphText.toString();
				String updatedText = detectAndIncreaseAgeInText(originalText, offset, logBuilder, logIndex);

				if (!originalText.equals(updatedText)) {
					changeCount++;
					Utils.replaceTextWithFormatting(paragraph, updatedText, runs);
					logIndex++;
				}
			}
		}

		// Save updated document
		try (FileOutputStream out = new FileOutputStream(outputFilePath)) {
			document.write(out);
		}

		// Save log
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(logFilePath), StandardOpenOption.CREATE,
				StandardOpenOption.APPEND)) {
			writer.write(logBuilder.toString());
		}

		document.close();

		return "- File đã cập nhật được lưu tại: \n" + outputFilePath + "\n- Có tổng cộng " + changeCount
				+ " vị trí đã cập nhật." + "\n- Chi tiết thay đổi đã được ghi lại: \n" + logFilePath + "\n ";
	}

	private static String detectAndIncreaseAgeInText(String text, int offset, StringBuilder logBuilder, int logIndex) {
		Pattern pattern = Pattern.compile("\\b(\\d+)(?:\\s{1}t|t)\\b");
		Matcher matcher = pattern.matcher(text);

		StringBuffer updatedText = new StringBuffer();
		while (matcher.find()) {
			String original = matcher.group();
			int number = Integer.parseInt(matcher.group(1));
			String updated = String.valueOf(number + offset); // Update only the number
			matcher.appendReplacement(updatedText, Matcher.quoteReplacement(updated + "t"));
			logBuilder.append(logIndex).append(". ").append(original).append(" -> ").append(updated).append("t\n");
			logIndex++;
		}
		matcher.appendTail(updatedText);
		return updatedText.toString();
	}

	/**
	 * Xử lý cập nhật năm Phật lịch.
	 * @param filePath
	 * @param buddhistYear
	 * @param buddhistYearInChinese
	 * @param vietnameseNumerals
	 * @throws Exception
	 */
	private static void processUpdatingBuddhistYearInText(String filePath, int buddhistYear, String buddhistYearInChinese,
			String vietnameseNumerals) throws Exception {
		XWPFDocument document = new XWPFDocument(Files.newInputStream(Paths.get(filePath)));
		List<XWPFParagraph> paragraphs = document.getParagraphs();
		for (int i = 0; i < paragraphs.size() - 18; i++) { // Ensure there are enough lines
			// Check the next 4 lines for the pattern
			String line1 = paragraphs.get(i).getText().trim();
			String line2 = paragraphs.get(i + 1).getText().trim();
			String line3 = paragraphs.get(i + 2).getText().trim();
			String line4 = paragraphs.get(i + 3).getText().trim();

			if (line1.equals("佛") && line2.equals("Phật") && line3.equals("曆") && line4.equals("Lịch:")) {
				// Update the 14 lines following the pattern
				String[] chineseWords = buddhistYearInChinese.split(" ");
				String[] vietnameseWords = vietnameseNumerals.split(" ");
				for (int j = 0; j < 14; j++) {
					XWPFParagraph targetParagraph = paragraphs.get(i + 4 + j);
					String updatedText = (j % 2 == 0) ? chineseWords[j / 2] : vietnameseWords[j / 2];
					if (updatedText.equals(".")) {
						Utils.addNewTextWithSmallSize(targetParagraph, " ", paragraphs.get(i + (j % 2 == 0 ? 2 : 3)));
					} else {
						Utils.copyAndReplaceWithReference(targetParagraph, updatedText,
								paragraphs.get(i + (j % 2 == 0 ? 2 : 3)));
					}
				}
				// Update the 15th line to the Buddhist year
				XWPFParagraph yearParagraph = paragraphs.get(i + 18);
				Utils.copyAndReplaceWithReference(yearParagraph, "(" + buddhistYear + ")", paragraphs.get(i + 3));
				break;
			}
		}
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			document.write(out);
		}
		document.close();
	}

	/**
	 * Xử lý cập nhật năm Âm lịch.
	 * @param filePath
	 * @param lunaYear
	 * @throws Exception
	 */
	private static void processUpdatingLunaYearInText(String filePath, String lunaYear) throws Exception {
		XWPFDocument document = new XWPFDocument(Files.newInputStream(Paths.get(filePath)));
		List<XWPFParagraph> paragraphs = document.getParagraphs();
		for (int i = 0; i < paragraphs.size() - 5; i++) { // Ensure there are enough lines
			// Check the next 4 lines for the pattern
			String line1 = paragraphs.get(i).getText().trim();
			String line2 = paragraphs.get(i + 1).getText().trim();
			String line3 = paragraphs.get(i + 2).getText().trim();
			String line4 = paragraphs.get(i + 3).getText().trim();

			if (line1.equals("歲") && line2.equals("Tuế") && line3.equals("次") && line4.equals("Thứ:")) {
				// Update the 2 lines following the pattern
				String[] lunaYearWords = lunaYear.split(" ");
				XWPFParagraph firstParagraph = paragraphs.get(i + 4);
				firstParagraph.getRuns().forEach(run -> run.setText("", 0));
				XWPFRun newFirstRuns = firstParagraph.createRun();
				newFirstRuns.setText(lunaYearWords[0]);
				Utils.copyRunFormatting(firstParagraph, firstParagraph.getRuns().get(0), newFirstRuns, null);

				XWPFParagraph secondParagraph = paragraphs.get(i + 5);
				secondParagraph.getRuns().forEach(run -> run.setText("", 0));
				XWPFRun newSecondRuns = secondParagraph.createRun();
				newSecondRuns.setText(lunaYearWords[1]);
				Utils.copyRunFormatting(secondParagraph, secondParagraph.getRuns().get(0), newSecondRuns, null);
				break;
			}
		}
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			document.write(out);
		}
		document.close();
	}

	/**
	 * Xóa các file cũ, giữ lại các file mới với hậu tố "_updated"
	 * @param processedFiles Map<originalPath, updatedPath>
	 */
	private static void handleDeleteOldFiles(Map<String, String> processedFiles) {
		try {
			int deletedCount = 0;
			for (String originalPath : processedFiles.keySet()) {
				File oldFile = new File(originalPath);
				if (oldFile.exists()) {
					if (oldFile.delete()) {
						deletedCount++;
					}
				}
			}
			Utils.showMessageWithFont(
					"✅ Đã xóa " + deletedCount + " file cũ. Các file mới được giữ lại với hậu tố \"_updated\".",
					"Thành công",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			Utils.showMessageWithFont(
					"❌ Lỗi khi xóa file cũ: " + e.getMessage(),
					"Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Xóa các file cũ và đổi tên các file mới thành tên cũ (xóa hậu tố "_updated")
	 * @param processedFiles Map<originalPath, updatedPath>
	 */
	private static void handleRenameToOriginalNames(Map<String, String> processedFiles) {
		try {
			int renamedCount = 0;
			int failedCount = 0;

			for (Map.Entry<String, String> entry : processedFiles.entrySet()) {
				String originalPath = entry.getKey();
				String updatedPath = entry.getValue();

				File oldFile = new File(originalPath);
				File newFile = new File(updatedPath);

				try {
					// Delete old file
					if (oldFile.exists()) {
						oldFile.delete();
					}

					// Rename new file to original name
					if (newFile.exists()) {
						if (newFile.renameTo(oldFile)) {
							renamedCount++;
						} else {
							failedCount++;
						}
					}
				} catch (Exception e) {
					failedCount++;
					System.err.println("Lỗi khi xử lý file " + originalPath + ": " + e.getMessage());
				}
			}

			if (failedCount == 0) {
				Utils.showMessageWithFont(
						"✅ Đã cập nhật thành công " + renamedCount + " file. Các file được đổi tên về tên gốc.",
						"Thành công",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				Utils.showMessageWithFont(
						"⚠️  Đã cập nhật " + renamedCount + " file, nhưng " + failedCount + " file bị lỗi.",
						"Cảnh báo",
						JOptionPane.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			Utils.showMessageWithFont(
					"❌ Lỗi khi cập nhật file: " + e.getMessage(),
					"Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

}
