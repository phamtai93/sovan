package vn.tafi.process;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import vn.tafi.object.ConfigLoader;
import vn.tafi.object.MortalObject;

public class MainUIProcessor {
	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");

		// Apply AtlantaFX theme
		try {
			applyAtlantaFXTheme();
		} catch (Exception e) {
			System.err.println("Could not apply AtlantaFX theme: " + e.getMessage());
		}

		setUIFont(new javax.swing.plaf.FontUIResource("Calibri", Font.PLAIN, 14));
		SwingUtilities.invokeLater(MainUIProcessor::createAndShowGUI);
	}

	private static void applyAtlantaFXTheme() {
		try {
			// Load FlatLaf Dark theme - modern and sleek
			UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarculaLaf");
		} catch (Exception e) {
			System.err.println("Could not load FlatLaf theme: " + e.getMessage());
			try {
				// Fallback to system look and feel if FlatLaf fails
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception fallback) {
				System.err.println("Could not load system look and feel: " + fallback.getMessage());
			}
		}
	}

	private static void setUIFont(javax.swing.plaf.FontUIResource fontUIResource) {
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, fontUIResource);
			}
		}
	}

	private static void setApplicationIcon(JFrame frame) {
		try (InputStream iconStream = MainUIProcessor.class.getResourceAsStream("/images/app_icon.png")) {
			if (iconStream == null) {
				throw new IllegalArgumentException("Icon file not found!");
			}
			Image icon = ImageIO.read(iconStream);
			frame.setIconImage(icon);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static JPanel createBackgroundPanel(String imagePath) {
		URL imageUrl = MainUIProcessor.class.getResource(imagePath);

		// Kiểm tra xem ảnh có tồn tại không
		if (imageUrl == null) {
			// Nếu không tìm thấy ảnh, trả về JPanel bình thường (không có background)
			JPanel panel = new JPanel(new BorderLayout());
			panel.setOpaque(false); // Đảm bảo nền trong suốt
			return panel;
		}

		// Nếu ảnh tồn tại, tiếp tục tạo JPanel với background
		ImageIcon backgroundImage = new ImageIcon(imageUrl);

		JPanel backgroundPanel = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = -793401932578961103L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
			}
		};

		backgroundPanel.setOpaque(false);
		return backgroundPanel;
	}

	/**
	 * Cập nhật title của frame với năm và Can Giáp từ config
	 */
	private static void updateFrameTitle(JFrame frame) {
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

	/**
	 * Tạo giao diện chính của app
	 */
	private static void createAndShowGUI() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(750, 650);
		frame.setLocation(180, 150);
		Font boldFont = new Font("Calibri", Font.BOLD, 16);
		UIManager.put("TitlePane.font", boldFont);

		setApplicationIcon(frame);

		try {
			// 🛑 Load config trước khi hiển thị giao diện
			ConfigLoader.loadProperties();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Lỗi khi tải cấu hình: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1); // Ngưng chương trình nếu không tải được config
		}

		// Update frame title with current config values
		updateFrameTitle(frame);

		// Tạo TabbedPane
		JTabbedPane tabbedPane = new JTabbedPane();
		// tabbedPane.setPreferredSize(new Dimension(700, 350));

		// Tạo Tab "Cập nhật tuổi"
		JPanel updateAgeTab = createBackgroundPanel("/images/background_1.png");
		updateAgeTab.add(createUpdateAgePanel());
		tabbedPane.addTab("Cập nhật tuổi", updateAgeTab);

		// Tạo Tab "Hỗ trợ Sao Hạn"
		JPanel saoHanTab = createBackgroundPanel("/images/background_2.png");
		saoHanTab.add(createSaoHanPanel(frame));
		tabbedPane.addTab("Hỗ trợ Sao Hạn", saoHanTab);

		// Tạo Tab "Cài đặt"
		JPanel settingsTab = createBackgroundPanel("/images/background_1.png");
		settingsTab.add(SettingsPanel.createSettingsPanel(frame));
		try {
			FlatSVGIcon settingsIcon = new FlatSVGIcon("icons/config.svg", 16, 16);
			tabbedPane.addTab("Cài đặt", settingsIcon, settingsTab);
		} catch (Exception e) {
			System.err.println("Could not load settings icon: " + e.getMessage());
			tabbedPane.addTab("Cài đặt", settingsTab);
		}

		// Style TabbedPane - làm tab header có màu đậm hơn background
		styleTabbedPane(tabbedPane);

		// Set dark background cho frame ContentPane
		frame.getContentPane().setBackground(new Color(60, 70, 80)); // Màu xám đậm
		frame.getContentPane().add(tabbedPane);
		frame.setVisible(true);

	}

	/**
	 * Tạo panel cho tab "Hỗ trợ Sao Hạn"
	 */
	private static JPanel createSaoHanPanel(JFrame frame) {
		JPanel panel = new JPanel(new BorderLayout());

		panel.setOpaque(false);

		// panel.setPreferredSize(new Dimension(700, 350)); // Đảm bảo chiều cao đủ lớn

		// Panel chứa dãy nút
		JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 5, 0));
		JButton selectFileButton = new JButton("CHỌN FILE");
		JButton checkSaoHanButton = new JButton("KIỂM TRA");
		JButton createLabelButton = new JButton("TẠO NHÃN");
		JButton writeSoButton = new JButton("VIẾT SỚ");
		JButton resetButton = new JButton("BỎ CHỌN");

		// Apply styling với icon
		styleButton(selectFileButton, "primary", "icons/folder.svg");
		styleButton(checkSaoHanButton, "success", "icons/check.svg");
		styleButton(createLabelButton, "info", "icons/tag.svg");
		styleButton(writeSoButton, "primary", "icons/edit.svg");
		styleButton(resetButton, "danger", "icons/x.svg");

		buttonPanel.setOpaque(false);
		buttonPanel.add(selectFileButton);
		buttonPanel.add(checkSaoHanButton);
		buttonPanel.add(createLabelButton);
		buttonPanel.add(writeSoButton);
		buttonPanel.add(resetButton);

		// Label hiển thị tên file đã chọn
		JLabel fileLabel = new JLabel("Chưa chọn file! (chỉ chọn 1 file Excel)", JLabel.CENTER);
		fileLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		styleLabel(fileLabel, "info");

		// Ghi chú hướng dẫn nhập dòng
		JLabel guideLabel = new JLabel("Hãy xác nhận dòng bắt đầu và dòng kết thúc chứa thông tin cần xử lý!",
				JLabel.CENTER);
		guideLabel.setVisible(false); // Ẩn ban đầu
		styleLabel(guideLabel, "warning");

		// Panel chứa nhập số
		JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		JLabel startLabel = new JLabel("Bắt đầu:");
		JTextField startField = new JTextField(5);
		JLabel endLabel = new JLabel("Kết thúc:");
		JTextField endField = new JTextField(5);
		inputPanel.setOpaque(false);

		styleLabel(startLabel, "info");
		styleLabel(endLabel, "info");
		styleTextField(startField);
		styleTextField(endField);

		inputPanel.add(startLabel);
		inputPanel.add(startField);
		inputPanel.add(endLabel);
		inputPanel.add(endField);
		inputPanel.setVisible(false); // Ẩn ban đầu

		// Log TextArea để hiển thị kết quả kiểm tra
		JTextArea logTextArea = new JTextArea(15, 100);
		logTextArea.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(logTextArea);
		styleTextArea(logTextArea);

		panel.add(logScrollPane, BorderLayout.SOUTH);

		final List<MortalObject> mortalObjects = new ArrayList<>();
		// Chọn file Excel
		final String[] selectedFilePath = { null }; // Lưu đường dẫn file đã chọn

		// Create print panel wrapped in CollapsiblePanel - declare early for action listeners
		final JPanel printPanelContent = PrintingHelper.createPrintPanel(mortalObjects, logTextArea, selectedFilePath);
		final CollapsiblePanel printPanel = new CollapsiblePanel("In nhãn hình nhân", printPanelContent);
		printPanel.setVisible(false);
		// Set dark background để trùng với background frame
		printPanel.setBackground(new Color(60, 70, 80)); // Màu xám đậm trùng với frame background
		printPanel.setOpaque(true);

		// Add callback to resize frame when panel is toggled
		printPanel.setOnToggleCallback(() -> {
			if (printPanel.isExpanded()) {
				frame.setSize(750, 860);
			} else {
				frame.setSize(750, 580);
			}
		});

		/* Phần xử lý nút chọn file danh sách đệ tử **/
		selectFileButton.addActionListener(e -> {
			MotalListProcessingHelper.processSelectListMotalFile(
					fileLabel, guideLabel, inputPanel, startField, endField, selectedFilePath);
		});

		/* Phần xử lý nút kiểm tra sao hạn **/
		checkSaoHanButton.addActionListener(e -> {
			MotalListProcessingHelper.processCheckingSaoHan(
					startField, endField, logTextArea, mortalObjects, selectedFilePath);

			// Show printPanel only if no errors
			if (!logTextArea.getText().contains("⚠️")) {
				// Populate person list panel with loaded data
				PrintingHelper.populatePersonListPanel(mortalObjects);

				printPanel.setVisible(true);
				// Auto-expand the panel
				SwingUtilities.invokeLater(() -> {
					printPanel.expand();
				});
			} else {
				printPanel.setVisible(false);
			}
		});

		/* Phần xử lý nút tạo nhãn và file ghi chú **/
		createLabelButton.addActionListener(e -> {
			MotalListProcessingHelper.processCreateLabelAndNote(logTextArea, mortalObjects, selectedFilePath[0]);
		});

		writeSoButton.addActionListener(e -> {
			MotalListProcessingHelper.processWritingSo(logTextArea, mortalObjects, selectedFilePath[0]);
		});

		// Bỏ chọn file
		resetButton.addActionListener(e -> {
			selectedFilePath[0] = null;
			mortalObjects.clear(); // Clear danh sách dữ liệu đã load
			fileLabel.setText("Chưa chọn file! (chỉ chọn 1 file Excel)");
			// Ẩn hướng dẫn và input khi bỏ chọn
			guideLabel.setVisible(false);
			inputPanel.setVisible(false);
			printPanel.setVisible(false); // Hide print panel
			logTextArea.setText("");
		});

		// Panel chứa dãy nút và label
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(buttonPanel, BorderLayout.NORTH);
		topPanel.add(fileLabel, BorderLayout.CENTER);
		topPanel.add(guideLabel, BorderLayout.SOUTH);
		topPanel.setOpaque(false);

		// Create main center container
		JPanel mainCenterPanel = new JPanel();
		mainCenterPanel.setLayout(new BoxLayout(mainCenterPanel, BoxLayout.Y_AXIS));
		mainCenterPanel.setOpaque(false);

		// Add existing inputPanel
		mainCenterPanel.add(inputPanel);

		// Add print panel (already created earlier)
		mainCenterPanel.add(printPanel);

		// Wrap mainCenterPanel in a JScrollPane for better visibility
		JScrollPane centerScrollPane = new JScrollPane(mainCenterPanel);
		centerScrollPane.setOpaque(false);
		centerScrollPane.getViewport().setOpaque(false);
		centerScrollPane.setBorder(null);
		centerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		centerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(centerScrollPane, BorderLayout.CENTER);
		return panel;
	}


	/**
	 * Style button với icon, màu sắc, font, và border tuyệt đẹp
	 */
	private static void styleButton(JButton button, String type, String iconPath) {
		button.setFont(new Font("Calibri", Font.BOLD, 14));
		button.setFocusPainted(false);
		button.setMargin(new Insets(8, 15, 8, 15));
		button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

		// Thêm icon nếu có
		if (iconPath != null && !iconPath.isEmpty()) {
			try {
				FlatSVGIcon icon = new FlatSVGIcon(iconPath, 18, 18);
				button.setIcon(icon);
				button.setIconTextGap(8);
			} catch (Exception e) {
				System.err.println("Could not load icon: " + iconPath + " - " + e.getMessage());
			}
		}

		// Tùy chỉnh màu sắc theo loại button
		if ("primary".equals(type)) {
			// Nút chính - xanh dương
			button.setBackground(new Color(0, 120, 215));
			button.setForeground(Color.WHITE);
		} else if ("success".equals(type)) {
			// Nút thành công - xanh lục
			button.setBackground(new Color(40, 167, 69));
			button.setForeground(Color.WHITE);
		} else if ("danger".equals(type)) {
			// Nút xóa/bỏ chọn - đỏ
			button.setBackground(new Color(220, 53, 69));
			button.setForeground(Color.WHITE);
		} else if ("info".equals(type)) {
			// Nút thông tin - xanh nhạt
			button.setBackground(new Color(23, 162, 184));
			button.setForeground(Color.WHITE);
		} else {
			// Nút mặc định
			button.setBackground(new Color(108, 117, 125));
			button.setForeground(Color.WHITE);
		}

		button.setOpaque(true);
		button.setBorderPainted(true);
		button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
	}

	/**
	 * Style JLabel với font và màu sắc sáng, dễ nhìn
	 */
	private static void styleLabel(JLabel label, String type) {
		if ("header".equals(type)) {
			// Tiêu đề - màu đen sáng, bold
			label.setFont(new Font("Calibri", Font.BOLD, 18));
			label.setForeground(new Color(13, 13, 13)); // Gần như đen
		} else if ("info".equals(type)) {
			// Thông tin - màu sáng (có xu hướng trắng) cho hiển thị trên nền xám
			label.setFont(new Font("Calibri", Font.PLAIN, 16));
			label.setForeground(new Color(200, 205, 210)); // Xám sáng - gần như trắng
		} else if ("warning".equals(type)) {
			// Cảnh báo - vàng đậm
			label.setFont(new Font("Calibri", Font.BOLD, 16));
			label.setForeground(new Color(204, 130, 0)); // Vàng đậm
		} else {
			// Mặc định
			label.setFont(new Font("Calibri", Font.PLAIN, 16));
			label.setForeground(new Color(35, 40, 45));
		}
	}

	/**
	 * Style JTextArea với font, border, và màu sắc đẹp
	 */
	private static void styleTextArea(JTextArea textArea) {
		textArea.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));
		textArea.setForeground(new Color(30, 35, 40)); // Màu text đen sáng
		textArea.setBackground(new Color(255, 255, 255)); // Nền trắng
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setMargin(new Insets(8, 8, 8, 8));
		// Thêm border đẹp
		textArea.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(200, 210, 220), 1),
			BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));
	}

	/**
	 * Style JTextField với border sáng, dễ nhìn
	 */
	private static void styleTextField(JTextField textField) {
		textField.setFont(new Font("Calibri", Font.PLAIN, 14));
		textField.setForeground(new Color(30, 35, 40)); // Màu text đen sáng
		textField.setBackground(Color.WHITE);
		textField.setMargin(new Insets(6, 8, 6, 8));
		// Border sáng hơn, dễ nhìn
		textField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(180, 195, 210), 1),
			BorderFactory.createEmptyBorder(4, 6, 4, 6)
		));
		textField.setCaretColor(new Color(0, 120, 215)); // Cursor xanh dương
	}

	/**
	 * Style JTabbedPane với phần tab header có màu đậm hơn background
	 */
	private static void styleTabbedPane(JTabbedPane tabbedPane) {
		tabbedPane.setFont(new Font("Calibri", Font.BOLD, 14));

		// Thiết lập UIManager để giữ màu tab khi focus lost
		// Tab được chọn - màu sáng hơn
		UIManager.put("TabbedPane.selected", new Color(70, 85, 100)); // Màu tab được chọn - sáng hơn
		UIManager.put("TabbedPane.selectedForeground", new Color(255, 255, 255)); // Chữ trắng khi được chọn

		// Tab không được chọn - màu tối hơn
		UIManager.put("TabbedPane.background", new Color(100, 110, 120)); // Màu xám đậm cho tab header
		UIManager.put("TabbedPane.foreground", new Color(200, 200, 200)); // Chữ nhạt khi không được chọn
		UIManager.put("TabbedPane.unselectedBackground", new Color(100, 110, 120)); // Màu tab không được chọn
		UIManager.put("TabbedPane.unselectedForeground", new Color(200, 200, 200)); // Chữ nhạt hơn khi không được chọn

		// Bỏ focus highlight color
		UIManager.put("TabbedPane.focus", new Color(70, 85, 100)); // Giữ màu được chọn khi focus
		UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
	}

	/**
	 * Tạo panel cho tab "Cập nhật tuổi"
	 */
	private static JPanel createUpdateAgePanel() {
		JPanel panel = new JPanel(new BorderLayout());

		panel.setOpaque(false);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 0));
		JButton selectFileButton = new JButton("CHỌN FILE");
		JButton updateButton = new JButton("CẬP NHẬT TUỔI");
		JButton formatVietCharButton = new JButton("FORMAT CHỮ");
		JButton resetButton = new JButton("BỎ CHỌN");
		buttonPanel.setOpaque(false);

		// Apply styling với icon
		styleButton(selectFileButton, "primary", "icons/folder.svg");
		styleButton(updateButton, "success", "icons/refresh.svg");
		styleButton(formatVietCharButton, "info", "icons/settings.svg");
		styleButton(resetButton, "danger", "icons/x.svg");

		buttonPanel.add(selectFileButton);
		buttonPanel.add(updateButton);
		buttonPanel.add(formatVietCharButton);
		buttonPanel.add(resetButton);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(buttonPanel, BorderLayout.NORTH);
		topPanel.setOpaque(false);

		JLabel fileLabel = new JLabel("Chưa chọn file! (Bạn có thể chọn nhiều file cùng lúc)", JLabel.CENTER);
		fileLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add spacing between buttons and label
		styleLabel(fileLabel, "info");
		topPanel.add(fileLabel, BorderLayout.SOUTH);

		JPanel fileListPanel = new JPanel();
		fileListPanel.setLayout(new BoxLayout(fileListPanel, BoxLayout.Y_AXIS));
		fileListPanel.setOpaque(false);

		JScrollPane fileListScrollPane = new JScrollPane(fileListPanel);
		fileListScrollPane.setOpaque(false);
		fileListScrollPane.getViewport().setOpaque(false);
		// Tăng chiều cao để không bị che khuất
		// fileListScrollPane.setPreferredSize(new Dimension(650, 100));

		JTextArea logArea = new JTextArea(5, 100);
		logArea.setEditable(false);
		styleTextArea(logArea);

		JScrollPane logScrollPane = new JScrollPane(logArea);
		logScrollPane.getViewport().setOpaque(false); // Làm trong suốt
		logScrollPane.setPreferredSize(new Dimension(600, 250)); // Tăng kích thước log

		// To store file names and their full paths
		Map<String, String> filePathMap = new HashMap<>();

		selectFileButton.addActionListener(e -> {
			ContentDocProcessingHelper.processSelectDocFiles(fileLabel, fileListPanel, filePathMap);
		});

		updateButton.addActionListener(e -> {
			ContentDocProcessingHelper.processUpdatingYearAndAge(fileListPanel, logArea, filePathMap);
		});

		formatVietCharButton.addActionListener(e -> {
			ContentDocProcessingHelper.processFormatVietChar(fileListPanel, logArea, filePathMap);
		});

		resetButton.addActionListener(e -> {
			fileListPanel.removeAll();
			fileLabel.setText("Chưa chọn file! (Bạn có thể chọn nhiều file cùng lúc)");
			logArea.setText("");
			filePathMap.clear(); // Clear stored file paths
			fileListPanel.revalidate();
			fileListPanel.repaint();
		});

		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(fileListScrollPane, BorderLayout.CENTER);
		panel.add(logScrollPane, BorderLayout.SOUTH);

		return panel;
	}

}
