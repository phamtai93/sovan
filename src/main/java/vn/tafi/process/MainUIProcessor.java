package vn.tafi.process;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
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

import vn.tafi.object.ConfigLoader;
import vn.tafi.object.MortalObject;

public class MainUIProcessor {
	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		setUIFont(new javax.swing.plaf.FontUIResource("Arial", Font.PLAIN, 14));
		SwingUtilities.invokeLater(MainUIProcessor::createAndShowGUI);
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
	 * Tạo giao diện chính của app
	 */
	private static void createAndShowGUI() {
		JFrame frame = new JFrame("PHẦN MỀM HỖ TRỢ LÀM SỚ");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 500);
		frame.setLocation(180, 280);

		setApplicationIcon(frame);

		try {
			// 🛑 Load config trước khi hiển thị giao diện
			ConfigLoader.loadProperties();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Lỗi khi tải cấu hình: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1); // Ngưng chương trình nếu không tải được config
		}

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
		JButton selectFileButton = new JButton("Chọn file");
		JButton checkSaoHanButton = new JButton("Kiểm tra Sao Hạn");
		JButton createLabelButton = new JButton("Tạo nhãn");
		JButton writeSoButton = new JButton("Viết Sớ");
		JButton resetButton = new JButton("Bỏ chọn");

		buttonPanel.setOpaque(false);
		buttonPanel.add(selectFileButton);
		buttonPanel.add(checkSaoHanButton);
		buttonPanel.add(createLabelButton);
		buttonPanel.add(writeSoButton);
		buttonPanel.add(resetButton);

		// Label hiển thị tên file đã chọn
		JLabel fileLabel = new JLabel("Chưa chọn file! (chỉ chọn 1 file Excel)", JLabel.CENTER);
		fileLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		// Ghi chú hướng dẫn nhập dòng
		JLabel guideLabel = new JLabel("Hãy xác nhận dòng bắt đầu và dòng kết thúc chứa thông tin cần xử lý!",
				JLabel.CENTER);
		guideLabel.setVisible(false); // Ẩn ban đầu

		// Panel chứa nhập số
		JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		JLabel startLabel = new JLabel("Bắt đầu:");
		JTextField startField = new JTextField(5);
		JLabel endLabel = new JLabel("Kết thúc:");
		JTextField endField = new JTextField(5);
		inputPanel.setOpaque(false);

		inputPanel.add(startLabel);
		inputPanel.add(startField);
		inputPanel.add(endLabel);
		inputPanel.add(endField);
		inputPanel.setVisible(false); // Ẩn ban đầu

		// Log TextArea để hiển thị kết quả kiểm tra
		JTextArea logTextArea = new JTextArea(15, 100);
		logTextArea.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(logTextArea);
		logTextArea.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));

		panel.add(logScrollPane, BorderLayout.SOUTH);

		final List<MortalObject> mortalObjects = new ArrayList<>();
		// Chọn file Excel
		final String[] selectedFilePath = { null }; // Lưu đường dẫn file đã chọn

		// Create print panel wrapped in CollapsiblePanel - declare early for action listeners
		final JPanel printPanelContent = PrintingHelper.createPrintPanel(mortalObjects, logTextArea, selectedFilePath);
		final CollapsiblePanel printPanel = new CollapsiblePanel("In nhãn hình nhân", printPanelContent);
		printPanel.setVisible(false);

		// Add callback to resize frame when panel is toggled
		printPanel.setOnToggleCallback(() -> {
			if (printPanel.isExpanded()) {
				frame.setSize(600, 750);
			} else {
				frame.setSize(600, 500);
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
			MotalListProcessingHelper.processCreateLabelAndNote(logTextArea, mortalObjects);
		});

		writeSoButton.addActionListener(e -> {
			MotalListProcessingHelper.processWritingSo(logTextArea, mortalObjects);
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
	 * Tạo panel cho tab "Cập nhật tuổi"
	 */
	private static JPanel createUpdateAgePanel() {
		JPanel panel = new JPanel(new BorderLayout());

		panel.setOpaque(false);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0));
		JButton selectFileButton = new JButton("Chọn file");
		JButton updateButton = new JButton("Cập nhật tuổi");
		JButton formatVietCharButton = new JButton("Format chữ Việt");
		JButton resetButton = new JButton("Bỏ chọn");
		buttonPanel.setOpaque(false);

		buttonPanel.add(selectFileButton);
		buttonPanel.add(updateButton);
		buttonPanel.add(formatVietCharButton);
		buttonPanel.add(resetButton);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(buttonPanel, BorderLayout.NORTH);
		topPanel.setOpaque(false);

		JLabel fileLabel = new JLabel("Chưa chọn file! (Bạn có thể chọn nhiều file cùng lúc)", JLabel.CENTER);
		fileLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add spacing between buttons and label
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
		logArea.setFont(new Font("Arial Unicode MS", Font.PLAIN, 14));

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
