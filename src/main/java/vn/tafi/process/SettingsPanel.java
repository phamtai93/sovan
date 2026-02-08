package vn.tafi.process;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vn.tafi.object.ConfigLoader;

public class SettingsPanel {

	/**
	 * Tạo panel cài đặt với các tùy chọn cấu hình
	 */
	public static JPanel createSettingsPanel(javax.swing.JFrame parentFrame) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		// Create main settings container
		JPanel settingsContainer = new JPanel(new GridBagLayout());
		settingsContainer.setOpaque(false);
		settingsContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Question Label
		gbc.gridy++;
		gbc.gridwidth = 2;
		JLabel questionLabel = new JLabel("Bạn muốn cập nhật sớ cho năm nào?");
		questionLabel.setFont(new Font("Calibri", Font.BOLD, 16));
		questionLabel.setForeground(new Color(200, 205, 210));
		settingsContainer.add(questionLabel, gbc);

		// Year (Năm lịch Tây)
		gbc.gridy++;
		gbc.gridwidth = 1;
		JLabel yearLabel = new JLabel("Năm lịch Tây:");
		yearLabel.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));
		yearLabel.setForeground(new Color(200, 205, 210));
		settingsContainer.add(yearLabel, gbc);

		gbc.gridx = 1;
		int currentYear = getCurrentYearFromConfig();
		// Đảm bảo năm hiện tại nằm trong khoảng cho phép (1945-2100)
		if (currentYear < 1945) currentYear = 1945;
		if (currentYear > 2100) currentYear = 2100;

		JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(currentYear, 1945, 2100, 1));
		yearSpinner.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));

		// Format spinner to show numbers without commas
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(yearSpinner, "0");
		yearSpinner.setEditor(editor);

		Utils.styleSpinner(yearSpinner);
		settingsContainer.add(yearSpinner, gbc);

		// Luna Year (Can Giáp)
		gbc.gridx = 0;
		gbc.gridy++;
		JLabel lunaLabel = new JLabel("Can Giáp:");
		lunaLabel.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));
		lunaLabel.setForeground(new Color(200, 205, 210));
		settingsContainer.add(lunaLabel, gbc);

		gbc.gridx = 1;
		// Tính Can Giáp từ năm hiện tại
		String calculatedCanChi = Utils.calculateCanChiYear(currentYear);
		JTextField lunaTextField = new JTextField(calculatedCanChi, 20);
		lunaTextField.setEditable(false); // Không cho phép nhập, chỉ đọc
		Utils.styleTextField(lunaTextField);
		settingsContainer.add(lunaTextField, gbc);

		// Thêm listener để tự động cập nhật Can Giáp khi thay đổi năm
		yearSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int year = (Integer) yearSpinner.getValue();
				String canChi = Utils.calculateCanChiYear(year);
				lunaTextField.setText(canChi);
			}
		});

		// Save Button
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(20, 10, 10, 10);
		JButton saveButton = new JButton("💾   Lưu cài đặt");
		Utils.styleButton(saveButton);
		saveButton.addActionListener(e -> {
			handleSaveSettings(yearSpinner, lunaTextField, parentFrame);
		});
		settingsContainer.add(saveButton, gbc);

		// Reset Button
		gbc.gridx = 1;
		JButton resetButton = new JButton("⟲   Hủy và tải lại");
		Utils.styleButton(resetButton);
		resetButton.setBackground(new Color(108, 117, 125)); // Gray
		resetButton.addActionListener(e -> {
			handleResetSettings(yearSpinner, lunaTextField);
		});
		settingsContainer.add(resetButton, gbc);

		// Add to panel
		panel.add(settingsContainer, BorderLayout.NORTH);
		return panel;
	}

	/**
	 * Lấy giá trị năm từ file cấu hình
	 */
	private static int getCurrentYearFromConfig() {
		try {
			String yearStr = ConfigLoader.getProperty("year");
			return Integer.parseInt(yearStr);
		} catch (Exception e) {
			System.err.println("Error reading year from config: " + e.getMessage());
			return 1900; // Default value
		}
	}

	/**
	 * Lấy giá trị Can Giáp từ file cấu hình
	 */
	@SuppressWarnings("unused")
	private static String getCurrentLunaYearFromConfig() {
		try {
			return ConfigLoader.getProperty("lunaYear");
		} catch (Exception e) {
			System.err.println("Error reading lunaYear from config: " + e.getMessage());
			return " "; // Default value
		}
	}

	/**
	 * Xử lý lưu cấu hình
	 */
	private static void handleSaveSettings(JSpinner yearSpinner, JTextField lunaTextField, javax.swing.JFrame parentFrame) {
		try {
			int year = (Integer) yearSpinner.getValue();
			String lunaYear = lunaTextField.getText().trim();

			// Can Giáp được tính tự động, không cần validation
			if (lunaYear.isEmpty()) {
				Utils.showMessageWithFont("Lỗi: Không thể tính Can Giáp cho năm này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Save to config file
			saveConfigToFile(year, lunaYear);

			// Update in-memory config
			ConfigLoader.setProperty("year", String.valueOf(year));
			ConfigLoader.setProperty("lunaYear", lunaYear);

			// Update frame title
			updateFrameTitle(parentFrame);

			Utils.showMessageWithFont("✅ Cấu hình đã được lưu thành công!", "Thành công",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			Utils.showMessageWithFont("❌ Lỗi khi lưu cấu hình: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Xử lý hủy thay đổi và tải lại dữ liệu từ config
	 */
	private static void handleResetSettings(JSpinner yearSpinner, JTextField lunaTextField) {
		try {
			int currentYear = getCurrentYearFromConfig();
			// Đảm bảo năm nằm trong khoảng cho phép
			if (currentYear < 1945) currentYear = 1945;
			if (currentYear > 2100) currentYear = 2100;

			// Reset yearSpinner
			yearSpinner.setValue(currentYear);

			// Tính và cập nhật Can Giáp
			String canChi = Utils.calculateCanChiYear(currentYear);
			lunaTextField.setText(canChi);

			Utils.showMessageWithFont("✅ Đã tải lại cấu hình gốc!", "Thành công",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			Utils.showMessageWithFont("❌ Lỗi khi tải lại cấu hình: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Lưu cấu hình vào file App.config
	 * Chỉ cập nhật các giá trị được chỉnh sửa, giữ nguyên phần còn lại
	 */
	private static void saveConfigToFile(int year, String lunaYear) throws Exception {
		String configPath = findConfigFilePath();
		File configFile = new File(configPath);

		// Đọc nội dung file hiện tại
		List<String> lines = new ArrayList<>();
		if (configFile.exists()) {
			lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
		}

		// Cập nhật chỉ 2 giá trị: year và lunaYear
		boolean yearFound = false;
		boolean lunaYearFound = false;

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("year=")) {
				lines.set(i, "year=" + year);
				yearFound = true;
			} else if (line.startsWith("lunaYear=")) {
				lines.set(i, "lunaYear=" + lunaYear);
				lunaYearFound = true;
			}
		}

		// Nếu chưa có key, thêm vào đầu file (sau comment)
		if (!yearFound || !lunaYearFound) {
			int insertIndex = 0;
			for (int i = 0; i < lines.size(); i++) {
				if (lines.get(i).startsWith("#")) {
					insertIndex = i + 1;
				} else if (!lines.get(i).isEmpty()) {
					break;
				}
			}
			if (!yearFound) {
				lines.add(insertIndex, "year=" + year);
			}
			if (!lunaYearFound) {
				lines.add(insertIndex + (yearFound ? 0 : 1), "lunaYear=" + lunaYear);
			}
		}

		// Ghi lại file
		Files.write(configFile.toPath(), lines, StandardCharsets.UTF_8);
		System.out.println("✅ Config saved to: " + configPath);
	}

	/**
	 * Tìm đường dẫn file App.config
	 */
	private static String findConfigFilePath() {
		// 1. Thử thư mục hiện tại (working directory)
		File workingDirConfig = new File("App.config");
		if (workingDirConfig.exists()) {
			return workingDirConfig.getAbsolutePath();
		}

		// 2. Thư mục JAR installation
		try {
			String jarPath = ConfigLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			File appDir = new File(jarPath).getParentFile();
			File appDirConfig = new File(appDir, "App.config");
			if (appDirConfig.exists()) {
				return appDirConfig.getAbsolutePath();
			}
		} catch (Exception e) {
			System.err.println("Error finding app directory: " + e.getMessage());
		}

		// 3. Default: src/main/resources/App.config trong project
		String resourcesPath = new File("src/main/resources/App.config").getAbsolutePath();
		if (new File(resourcesPath).exists()) {
			return resourcesPath;
		}

		// 4. Fallback: user home directory
		return new File(System.getProperty("user.home"), "App.config").getAbsolutePath();
	}

	/**
	 * Cập nhật title của frame với năm và Can Giáp từ config
	 */
	private static void updateFrameTitle(javax.swing.JFrame frame) {
		try {
			String year = ConfigLoader.getProperty("year");
			String lunaYear = ConfigLoader.getProperty("lunaYear");
			String title = String.format("PHẦN MỀM HỖ TRỢ LÀM SỚ (%s - %s)", year, lunaYear);
			frame.setTitle(title);
		} catch (Exception e) {
			System.err.println("Error updating frame title: " + e.getMessage());
			frame.setTitle("PHẦN MỀM HỖ TRỢ LÀM SỚ");
		}
	}
}
