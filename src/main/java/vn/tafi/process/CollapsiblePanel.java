package vn.tafi.process;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A collapsible panel with toggle button (arrow icon)
 */
public class CollapsiblePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel;
	private final JLabel toggleButton;
	private boolean expanded = false;
	private Runnable onToggleCallback;

	private static final String ARROW_DOWN = "▼";  // Arrow pointing down (collapsed)
	private static final String ARROW_UP = "▲";    // Arrow pointing up (expanded)

	public CollapsiblePanel(String title, JPanel content) {
		this.contentPanel = content;

		setLayout(new BorderLayout());
		setOpaque(false);

		// Create toggle button panel
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(new Color(60, 70, 80)); // Dark gray matching frame background
		headerPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(70, 85, 100), 1),
			BorderFactory.createEmptyBorder(8, 12, 8, 12)
		));

		// Toggle button with arrow
		toggleButton = new JLabel(ARROW_DOWN + " " + title, SwingConstants.LEFT);
		toggleButton.setFont(new Font("Arial Unicode MS", Font.BOLD, 14));
		toggleButton.setForeground(new Color(255, 255, 255)); // White text for contrast
		toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		toggleButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		// Add hover effect
		toggleButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				toggleButton.setForeground(new Color(200, 200, 200)); // Light gray on hover
			}

			@Override
			public void mouseExited(MouseEvent e) {
				toggleButton.setForeground(new Color(255, 255, 255)); // White text
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				toggleExpansion();
			}
		});

		headerPanel.add(toggleButton, BorderLayout.CENTER);

		// Initially hide content
		contentPanel.setVisible(false);

		add(headerPanel, BorderLayout.NORTH);
		add(contentPanel, BorderLayout.CENTER);
	}

	/**
	 * Toggle expansion state
	 */
	private void toggleExpansion() {
		expanded = !expanded;
		contentPanel.setVisible(expanded);

		// Update arrow icon
		String currentText = toggleButton.getText();
		String titleOnly = currentText.substring(2); // Remove arrow and space

		if (expanded) {
			toggleButton.setText(ARROW_UP + " " + titleOnly);
		} else {
			toggleButton.setText(ARROW_DOWN + " " + titleOnly);
		}

		// Revalidate and repaint
		revalidate();
		repaint();

		// Notify callback
		if (onToggleCallback != null) {
			onToggleCallback.run();
		}
	}

	/**
	 * Programmatically expand the panel
	 */
	public void expand() {
		if (!expanded) {
			toggleExpansion();
		}
	}

	/**
	 * Programmatically collapse the panel
	 */
	public void collapse() {
		if (expanded) {
			toggleExpansion();
		}
	}

	/**
	 * Check if panel is expanded
	 */
	public boolean isExpanded() {
		return expanded;
	}

	/**
	 * Set expansion state
	 */
	public void setExpanded(boolean expanded) {
		if (this.expanded != expanded) {
			toggleExpansion();
		}
	}

	/**
	 * Set callback to be called when panel is toggled
	 */
	public void setOnToggleCallback(Runnable callback) {
		this.onToggleCallback = callback;
	}
}
