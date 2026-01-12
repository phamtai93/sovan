package vn.tafi.process;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A filterable text field with dropdown list popup
 */
public class FilteredListPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTextField textField;
	private JPopupMenu popupMenu;
	private JList<String> list;
	private DefaultListModel<String> listModel;
	private List<String> allItems;
	private Map<String, Object> itemObjectMap; // Map from display text to actual object
	private Object selectedObject; // Store the selected object (not just text)
	private Runnable onSelectionChanged;
	private JScrollPane scrollPane;
	private boolean isSettingText = false; // Flag to prevent duplicate popup when setting text
	private boolean isUserInitiated = false; // Flag to track if user clicked/typed (not programmatic)

	public FilteredListPanel() {
		setLayout(new BorderLayout());
		setOpaque(false);

		// Text field
		textField = new JTextField();
		textField.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
		textField.setForeground(new Color(30, 35, 40)); // Màu text đen sáng
		textField.setBackground(Color.WHITE);
		textField.setMargin(new Insets(6, 8, 6, 8));
		textField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(180, 195, 210), 1),
			BorderFactory.createEmptyBorder(4, 6, 4, 6)
		));
		textField.setCaretColor(new Color(0, 120, 215)); // Cursor xanh dương
		add(textField, BorderLayout.CENTER);

		// All items storage
		allItems = new ArrayList<>();
		itemObjectMap = new HashMap<>();

		// List for popup
		listModel = new DefaultListModel<>();
		list = new JList<>(listModel);
		list.setFont(new Font("Arial Unicode MS", Font.PLAIN, 12));
		list.setForeground(new Color(30, 35, 40)); // Màu text đen sáng
		list.setBackground(Color.WHITE);
		list.setSelectionBackground(new Color(0, 120, 215));
		list.setSelectionForeground(Color.WHITE);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Popup menu and ScrollPane will be created/recreated as needed
		popupMenu = null;
		scrollPane = null;

		// Make list not take focus away from text field
		list.setFocusable(false);

		// Mouse listener to handle clicks on text field
		textField.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// User clicked on text field - show popup
				isUserInitiated = true;
				java.awt.EventQueue.invokeLater(() -> {
					showPopup();
				});
			}
		});

		// Focus listener
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// Hide popup when focus lost (with small delay to allow selection)
				java.awt.EventQueue.invokeLater(() -> {
					// Only hide if focus is not on the text field itself
					if (!textField.isFocusOwner()) {
						popupMenu.setVisible(false);
						isUserInitiated = false;
					}
				});
			}
		});

		// Mouse listener to handle clicks outside the text field
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// If user clicks outside the text field, hide popup
				if (e.getSource() != textField && popupMenu != null) {
					popupMenu.setVisible(false);
					isUserInitiated = false;
				}
			}
		});

		// Document listener for filtering
		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				// Skip filter if we're just setting text programmatically
				if (!isSettingText) {
					applyFilter();
				}
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// Skip filter if we're just setting text programmatically
				if (!isSettingText) {
					applyFilter();
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

		// List selection listener (with mouse click detection)
		list.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && list.getSelectedValue() != null) {
				String selectedText = list.getSelectedValue();
				isSettingText = true; // Set flag to prevent applyFilter from running
				textField.setText(selectedText);
				isSettingText = false; // Reset flag
				// Store the object associated with selected text
				selectedObject = itemObjectMap.get(selectedText);
				popupMenu.setVisible(false);
				isUserInitiated = false; // Reset flag so dropdown doesn't auto-show when switching radio buttons
				list.clearSelection();
				// Don't need to requestFocus since selection event will naturally keep focus on text field
				if (onSelectionChanged != null) {
					onSelectionChanged.run();
				}
			}
		});
	}

	private void ensurePopupCreated() {
		// Recreate popup if needed (handles the case where it was hidden before)
		if (popupMenu == null || !popupMenu.isDisplayable()) {
			popupMenu = new JPopupMenu();
			popupMenu.setOpaque(true);
			popupMenu.setBackground(Color.WHITE);
			popupMenu.setBorder(BorderFactory.createLineBorder(new Color(180, 195, 210), 1)); // Border sáng hơn
			popupMenu.setFocusable(false);

			scrollPane = new JScrollPane(list);
			scrollPane.setPreferredSize(new java.awt.Dimension(250, 200));
			scrollPane.setFocusable(false);
			scrollPane.setBackground(Color.WHITE);
			scrollPane.getViewport().setBackground(Color.WHITE);
			popupMenu.add(scrollPane);
		}
	}

	private void showPopup() {
		// Ensure popup is created
		ensurePopupCreated();

		// Populate all items when showing popup
		listModel.clear();
		for (String item : allItems) {
			listModel.addElement(item);
		}

		// Show popup (use invokeLater to avoid timing issues)
		if (listModel.size() > 0) {
			java.awt.EventQueue.invokeLater(() -> {
				// Always show popup when asked
				popupMenu.show(FilteredListPanel.this, 0, getHeight());
			});
		}
	}

	private void applyFilter() {
		String input = textField.getText();
		String inputLower = input.toLowerCase();

		listModel.clear();

		// Filter items
		for (String item : allItems) {
			if (item.toLowerCase().contains(inputLower)) {
				listModel.addElement(item);
			}
		}

		// Show popup if there are matches (use invokeLater to avoid timing issues)
		java.awt.EventQueue.invokeLater(() -> {
			if (listModel.size() > 0) {
				// Ensure popup is created and show it
				ensurePopupCreated();
				popupMenu.show(FilteredListPanel.this, 0, getHeight());
			} else if (popupMenu != null) {
				popupMenu.setVisible(false);
			}
		});
	}

	public void setItems(List<String> items) {
		isUserInitiated = false; // Reset flag when items are set programmatically
		isSettingText = true; // Prevent document listener from firing
		this.allItems = new ArrayList<>(items);
		this.itemObjectMap = new HashMap<>(); // Clear the object map
		textField.setText("");
		isSettingText = false;
		selectedObject = null; // Clear selected object
		list.clearSelection();
		if (popupMenu != null) {
			popupMenu.setVisible(false);
		}
	}

	public void setItemsWithObjects(List<String> items, Map<String, Object> objectMap) {
		isUserInitiated = false; // Reset flag when items are set programmatically
		isSettingText = true; // Prevent document listener from firing
		this.allItems = new ArrayList<>(items);
		this.itemObjectMap = new HashMap<>(objectMap); // Store object mapping
		textField.setText("");
		isSettingText = false;
		selectedObject = null; // Clear selected object
		list.clearSelection();
		if (popupMenu != null) {
			popupMenu.setVisible(false);
		}
	}

	public Object getSelectedItem() {
		return textField.getText().isEmpty() ? null : textField.getText();
	}

	public Object getSelectedObject() {
		return selectedObject;
	}

	public void setSelectedObject(Object obj) {
		this.selectedObject = obj;
	}

	public void setSelectedIndex(int index) {
		textField.setText("");
	}

	public JTextField getTextField() {
		return textField;
	}

	public void setOnSelectionChanged(Runnable callback) {
		this.onSelectionChanged = callback;
	}

	public void showListIfTextFieldFocused() {
		// Only show dropdown if user initiated (clicked on input field)
		// Don't show when setItems is called programmatically (e.g., switching radio buttons)
		if (isUserInitiated && textField.isFocusOwner()) {
			showPopup();
		}
	}
}
