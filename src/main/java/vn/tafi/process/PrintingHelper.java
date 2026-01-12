package vn.tafi.process;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import vn.tafi.object.ConfigLoader;
import vn.tafi.object.MortalObject;

public class PrintingHelper {

	// Store references for later updates
	private static JRadioButton maleRadioRef = null;
	private static FilteredListPanel personListPanelRef = null;
	private static FilteredListPanel endPositionPanelRef = null;

	/**
	 * Create print panel UI with all components
	 */
	public static JPanel createPrintPanel(List<MortalObject> mortalObjects, JTextArea logTextArea,
			String[] selectedFilePath) {

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createDashedBorder(null));
		panel.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;

		// Row 0: Title
		JLabel titleLabel = new JLabel("In nhãn hình nhân", JLabel.CENTER);
		titleLabel.setFont(new Font("Arial Unicode MS", Font.BOLD, 16));
		titleLabel.setForeground(new Color(200, 205, 210)); // Light text matching parent panel
		gbc.gridwidth = 2;
		panel.add(titleLabel, gbc);

		// Row 1: Printer selection
		gbc.gridy++;
		gbc.gridwidth = 1;
		JLabel printerLabel = new JLabel("Chọn máy in:");
		printerLabel.setForeground(new Color(200, 205, 210)); // Light text matching parent panel
		panel.add(printerLabel, gbc);
		panel.setFont(new Font("Calibri", Font.PLAIN, 14));

		gbc.gridx = 1;
		JComboBox<String> printerComboBox = initializePrinterComboBox();
		panel.add(printerComboBox, gbc);

		// Row 2: Instruction label
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		JLabel instructionLabel = new JLabel(
				"<html>Hãy đảm bảo máy in đang sẵn sàng hoạt động.<br>Bạn muốn in nhãn cho nam hay nữ?</html>");
		instructionLabel.setFont(new Font("Calibri", Font.PLAIN, 14));
		instructionLabel.setForeground(new Color(200, 205, 210)); // Light text matching parent panel
		panel.add(instructionLabel, gbc);

		// Row 3: Gender radio buttons
		gbc.gridy++;
		ButtonGroup genderGroup = new ButtonGroup();
		JRadioButton maleRadio = new JRadioButton("In cho nam");
		JRadioButton femaleRadio = new JRadioButton("In cho nữ");
		maleRadio.setSelected(true); // Default select male

		JPanel genderPanel = createGenderRadioPanel(genderGroup, maleRadio, femaleRadio);
		panel.add(genderPanel, gbc);

		// Row 4: Person selection
		gbc.gridy++;
		gbc.gridwidth = 1;
		JLabel positionLabel = new JLabel("Bắt đầu ở vị trí:");
		positionLabel.setForeground(new Color(200, 205, 210)); // Light text matching parent panel
		panel.add(positionLabel, gbc);
		panel.setFont(new Font("Calibri", Font.PLAIN, 14));

		gbc.gridx = 1;
		gbc.gridwidth = 1;
		FilteredListPanel personListPanel = new FilteredListPanel();
		panel.add(personListPanel, gbc);

		// Row 5: End position selection
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		JLabel endPositionLabel = new JLabel("Kết thúc ở vị trí:");
		endPositionLabel.setForeground(new Color(200, 205, 210)); // Light text matching parent panel
		panel.add(endPositionLabel, gbc);

		gbc.gridx = 1;
		gbc.gridwidth = 1;
		FilteredListPanel endPositionPanel = new FilteredListPanel();
		panel.add(endPositionPanel, gbc);

		// Store references for later updates
		maleRadioRef = maleRadio;
		personListPanelRef = personListPanel;
		endPositionPanelRef = endPositionPanel;

		// Don't populate initially - will be populated after data is loaded

		// Radio button listeners - switch gender updates person list
		maleRadio.addActionListener(e -> {
			personListPanel.setSelectedIndex(-1);
			endPositionPanel.setSelectedIndex(-1);
			updatePersonListPanel(personListPanel, mortalObjects, true);
			updatePersonListPanel(endPositionPanel, mortalObjects, true);
		});

		femaleRadio.addActionListener(e -> {
			personListPanel.setSelectedIndex(-1);
			endPositionPanel.setSelectedIndex(-1);
			updatePersonListPanel(personListPanel, mortalObjects, false);
			updatePersonListPanel(endPositionPanel, mortalObjects, false);
		});

		// Row 6: Print button
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		JButton printButton = new JButton("In nhãn");
		printButton.setBackground(new Color(83, 109, 254));
		printButton.setForeground(new Color(248, 249, 250));
		printButton.setFont(new Font("Arial Unicode MS", Font.BOLD, 14));
		// Add printer icon
		try {
			FlatSVGIcon printerIcon = new FlatSVGIcon("icons/printer.svg", 18, 18);
			printButton.setIcon(printerIcon);
		} catch (Exception e) {
			System.err.println("Could not load printer icon: " + e.getMessage());
		}
		panel.add(printButton, gbc);

		// Print button action
		printButton.addActionListener(e -> {
			boolean isMale = maleRadio.isSelected();
			processPrinting(printerComboBox, personListPanel, endPositionPanel, mortalObjects, isMale, logTextArea, selectedFilePath);
		});

		return panel;
	}

	/**
	 * Initialize printer ComboBox with available printers
	 */
	private static JComboBox<String> initializePrinterComboBox() {
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

		String[] printerNames = new String[printServices.length];
		for (int i = 0; i < printServices.length; i++) {
			printerNames[i] = printServices[i].getName();
		}

		JComboBox<String> comboBox = new JComboBox<>(printerNames);
		if (printerNames.length > 0) {
			comboBox.setSelectedIndex(0); // Select first printer as default
		}

		return comboBox;
	}

	/**
	 * Create gender radio button panel
	 */
	private static JPanel createGenderRadioPanel(ButtonGroup buttonGroup, JRadioButton maleRadio,
			JRadioButton femaleRadio) {

		buttonGroup.add(maleRadio);
		buttonGroup.add(femaleRadio);

		maleRadio.setForeground(new Color(200, 205, 210)); // Light text matching parent panel
		maleRadio.setOpaque(false);
		femaleRadio.setForeground(new Color(200, 205, 210)); // Light text matching parent panel
		femaleRadio.setOpaque(false);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		panel.setOpaque(false);
		panel.add(maleRadio);
		panel.add(femaleRadio);

		return panel;
	}

	/**
	 * Update FilteredListPanel with person items based on gender
	 */
	private static void updatePersonListPanel(FilteredListPanel listPanel, List<MortalObject> mortalObjects,
			boolean isMale) {

		// Build new items list and object map
		List<String> newItems = new ArrayList<>();
		Map<String, Object> objectMap = new HashMap<>();

		// Add empty option for "print from beginning"
		newItems.add("");
		objectMap.put("", null);

		// Filter by gender and add to list
		String targetGender = isMale ? "Nam" : "Nữ";

		for (MortalObject obj : mortalObjects) {
			if (targetGender.equalsIgnoreCase(obj.getGender()) && obj.getAgeRecalculated() != null
					&& obj.getAgeRecalculated() >= 11 && !obj.isNotSupported()) {

				String displayName = String.format("%d. %s %s %s", obj.getOrder(),
						obj.getFmName(), obj.getMidName(), obj.getName()).trim();
				newItems.add(displayName);
				objectMap.put(displayName, obj); // Store object mapping
			}
		}

		listPanel.setItemsWithObjects(newItems, objectMap);
		// Show dropdown list if text field is focused (when switching radio buttons)
		listPanel.showListIfTextFieldFocused();
	}


	/**
	 * Process printing - validation, confirmation, launch worker
	 */
	public static void processPrinting(JComboBox<String> printerComboBox, FilteredListPanel personListPanel,
			FilteredListPanel endPositionPanel, List<MortalObject> mortalObjects, boolean isMale, JTextArea logTextArea, String[] selectedFilePath) {

		// 1. Check if printer is selected
		String selectedPrinter = (String) printerComboBox.getSelectedItem();
		if (selectedPrinter == null || selectedPrinter.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Hãy chọn máy in!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 2. Verify printer is still available
		PrintService service = getPrintServiceByName(selectedPrinter);
		if (service == null) {
			JOptionPane.showMessageDialog(null, "Máy in không khả dụng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 3. Build print queue based on start and end position selection
		// Use getSelectedObject() to get the MortalObject instead of text
		List<MortalObject> printQueue = buildPrintQueue(mortalObjects, isMale, personListPanel.getSelectedObject(), endPositionPanel.getSelectedObject());

		if (printQueue.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Không có người nào để in!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 4. Show confirmation
		String genderText = isMale ? "nam" : "nữ";
		String startPosition = printQueue.size() > 0
			? printQueue.get(0).getFmName() + " " + printQueue.get(0).getMidName() + " " + printQueue.get(0).getName()
			: "";
		String endPosition = printQueue.size() > 0
			? printQueue.get(printQueue.size() - 1).getFmName() + " " + printQueue.get(printQueue.size() - 1).getMidName() + " " + printQueue.get(printQueue.size() - 1).getName()
			: "";
		int confirm = JOptionPane.showConfirmDialog(null,
				String.format(
						"Bạn có chắc muốn in %d nhãn %s (x2 bản = %d trang)?\n\nMáy in: %s\nBắt đầu từ: %s\nKết thúc ở: %s",
						printQueue.size(), genderText, printQueue.size() * 2, selectedPrinter,
						startPosition, endPosition),
				"Xác nhận in", JOptionPane.YES_NO_OPTION);

		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		// 5. Get log file path
		String excelFilePath = selectedFilePath[0];
		File excelFile = new File(excelFilePath);
		String logFilePath = excelFile.getParent() + "/print_log.log";

		// 6. Launch SwingWorker
		PrintRequestAttributeSet attributes = createPrintAttributes();
		LabelPrintWorker worker = new LabelPrintWorker(new ArrayList<>(printQueue), service, attributes, logTextArea,
				logFilePath);
		worker.execute();

		logTextArea.append("\n📝 Bắt đầu in " + printQueue.size() + " nhãn " + genderText + "...\n");
	}

	/**
	 * Get PrintService by printer name
	 */
	private static PrintService getPrintServiceByName(String printerName) {
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

		for (PrintService service : printServices) {
			if (service.getName().equals(printerName)) {
				return service;
			}
		}

		return null;
	}

	/**
	 * Create PrintRequestAttributeSet for print job configuration
	 */
	private static PrintRequestAttributeSet createPrintAttributes() {
		PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

		// Paper size: Legal (8.5" x 14")
		attributes.add(MediaSizeName.NA_LEGAL);

		// Orientation: Portrait
		attributes.add(OrientationRequested.PORTRAIT);

		// Margins: Top 3.5mm, Left 10mm (converted to inches)
		float topMarginInch = 3.5f / 25.4f; // 0.138 inches
		float leftMarginInch = 10f / 25.4f; // 0.394 inches
		float widthInch = 8.5f - leftMarginInch;
		float heightInch = 14f - topMarginInch;

		attributes.add(new MediaPrintableArea(leftMarginInch, topMarginInch, widthInch, heightInch,
				MediaPrintableArea.INCH));

		return attributes;
	}

	/**
	 * Build print queue based on gender and start/end position selection
	 */
	private static List<MortalObject> buildPrintQueue(List<MortalObject> allObjects, boolean isMale,
			Object startPerson, Object endPerson) {

		String targetGender = isMale ? "Nam" : "Nữ";

		List<MortalObject> filtered = allObjects.stream().filter(obj -> targetGender.equalsIgnoreCase(obj.getGender()))
				.filter(obj -> obj.getAgeRecalculated() != null && obj.getAgeRecalculated() >= 11)
				.filter(obj -> !obj.isNotSupported()).collect(Collectors.toList());

		// If no selection, return all
		if (startPerson == null && endPerson == null) {
			return filtered;
		}

		// Find start index
		int startIndex = 0;
		if (startPerson != null) {
			for (int i = 0; i < filtered.size(); i++) {
				if (filtered.get(i) == startPerson) {
					startIndex = i;
					break;
				}
			}
		}

		// Find end index
		int endIndex = filtered.size();
		if (endPerson != null) {
			for (int i = 0; i < filtered.size(); i++) {
				if (filtered.get(i) == endPerson) {
					endIndex = i + 1; // +1 to include the end person
					break;
				}
			}
		}

		// Return sublist from startIndex to endIndex
		if (startIndex <= endIndex) {
			return filtered.subList(startIndex, endIndex);
		}
		return filtered; // Fallback: print all if indices are invalid
	}

	/**
	 * Format label content from config template
	 */
	public static String formatLabelContent(MortalObject obj) {
		return String.format(ConfigLoader.getProperty("labelSaoHanTemplate"),
				String.join(" ", obj.getFmName(), obj.getMidName(), obj.getName()).trim(),
				String.join(" ", obj.getThienCan(), obj.getDiaChi()).trim(), obj.getAgeRecalculated(),
				obj.getSaoRecalculated().getSaoName(), obj.getHanRecalculated().getHanName());
	}

	/**
	 * Populate person list panels after data is loaded
	 */
	public static void populatePersonListPanel(List<MortalObject> mortalObjects) {
		if (personListPanelRef != null && endPositionPanelRef != null && maleRadioRef != null) {
			boolean isMale = maleRadioRef.isSelected();
			updatePersonListPanel(personListPanelRef, mortalObjects, isMale);
			updatePersonListPanel(endPositionPanelRef, mortalObjects, isMale);
		}
	}

	/**
	 * Write to print_log.log
	 */
	public static void writePrintLog(String logFilePath, List<String> logEntries) throws IOException {

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(logFilePath), StandardOpenOption.CREATE,
				StandardOpenOption.APPEND)) {

			writer.write("\n\n--------------------\n");
			writer.write("### In nhãn hình nhân\n");
			writer.write("[" + Utils.getCurrentTimestamp() + "]\n");

			for (String entry : logEntries) {
				writer.write(entry + "\n");
			}
		}
	}
}
