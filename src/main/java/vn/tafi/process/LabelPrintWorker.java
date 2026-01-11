package vn.tafi.process;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import vn.tafi.object.MortalObject;

/**
 * SwingWorker for background printing with real-time UI updates
 */
public class LabelPrintWorker extends SwingWorker<Void, String> {

	private final List<MortalObject> printQueue;
	private final PrintService printService;
	private final PrintRequestAttributeSet attributes;
	private final JTextArea logTextArea;
	private final String logFilePath;
	private final List<String> fileLogEntries;

	public LabelPrintWorker(List<MortalObject> printQueue, PrintService printService,
			PrintRequestAttributeSet attributes, JTextArea logTextArea, String logFilePath) {
		this.printQueue = printQueue;
		this.printService = printService;
		this.attributes = attributes;
		this.logTextArea = logTextArea;
		this.logFilePath = logFilePath;
		this.fileLogEntries = new ArrayList<>();
	}

	@Override
	protected Void doInBackground() throws Exception {
		for (MortalObject person : printQueue) {
			String fullName = getPersonFullName(person);

			try {
				// Print twice (2 pages per person) - separate log for each
				for (int copy = 1; copy <= 2; copy++) {
					// Publish progress with marker: "PROGRESS: [person name]"
					publish("PROGRESS:" + fullName);

					printLabelPage(person);
					Thread.sleep(100); // Small delay between pages

					// Publish completion with marker: "COMPLETE: [person name]"
					publish("COMPLETE:" + fullName);
				}

				// Add to file log (once per person)
				fileLogEntries.add(formatLogEntry(person));

			} catch (Exception e) {
				publish("ERROR:" + fullName + " - Lỗi: " + e.getMessage());
				fileLogEntries.add("❌ " + fullName + " - Lỗi: " + e.getMessage());
			}
		}
		return null;
	}

	@Override
	protected void process(List<String> chunks) {
		// Update UI in real-time on EDT
		for (String message : chunks) {
			if (message.startsWith("PROGRESS:")) {
				String name = message.substring("PROGRESS:".length());
				logTextArea.append("... " + name + "\n");
			} else if (message.startsWith("COMPLETE:")) {
				String name = message.substring("COMPLETE:".length());
				// Replace last line: remove last line and add completed line
				String text = logTextArea.getText();
				int lastNewline = text.lastIndexOf('\n', text.length() - 2);
				if (lastNewline >= 0) {
					logTextArea.setText(text.substring(0, lastNewline + 1));
				}
				logTextArea.append("✓ " + name + "\n");
			} else if (message.startsWith("ERROR:")) {
				String errorMsg = message.substring("ERROR:".length());
				// Replace last line with error
				String text = logTextArea.getText();
				int lastNewline = text.lastIndexOf('\n', text.length() - 2);
				if (lastNewline >= 0) {
					logTextArea.setText(text.substring(0, lastNewline + 1));
				}
				logTextArea.append("❌ " + errorMsg + "\n");
			}

			// Auto-scroll to bottom
			logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
		}
	}

	@Override
	protected void done() {
		try {
			get(); // Check for exceptions
			logTextArea.append("\n✅ Hoàn thành in " + printQueue.size() + " nhãn.\n");

			// Write to print_log.log
			PrintingHelper.writePrintLog(logFilePath, fileLogEntries);

		} catch (Exception e) {
			logTextArea.append("\n❌ Lỗi in: " + e.getMessage() + "\n");
			e.printStackTrace();
		}
	}

	/**
	 * Print a single label page
	 */
	private void printLabelPage(MortalObject person) throws Exception {
		// Get label content
		String labelText = PrintingHelper.formatLabelContent(person);

		// Create print job
		DocPrintJob printJob = printService.createPrintJob();

		// Create document with formatted text
		Doc doc = new SimpleDoc(createPrintable(labelText), DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);

		// Print (throws PrintException)
		printJob.print(doc, attributes);
	}

	/**
	 * Create Printable object for rendering text
	 */
	private Printable createPrintable(String text) {
		return new Printable() {
			@Override
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
				if (pageIndex > 0) {
					return Printable.NO_SUCH_PAGE;
				}

				Graphics2D g2d = (Graphics2D) graphics;

				// Set font: Calibri 14pt Bold
				Font font = new Font("Calibri", Font.BOLD, 14);

				// Fallback if Calibri not available
				if (!font.getFamily().equals("Calibri")) {
					font = new Font("Arial", Font.BOLD, 14);
				}

				g2d.setFont(font);

				// Apply margins from pageFormat
				g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

				// Draw text
				g2d.drawString(text, 0, font.getSize());

				return Printable.PAGE_EXISTS;
			}
		};
	}

	/**
	 * Get person full name
	 */
	private String getPersonFullName(MortalObject person) {
		return String.join(" ", person.getFmName(), person.getMidName(), person.getName()).trim();
	}

	/**
	 * Format log entry for file
	 */
	private String formatLogEntry(MortalObject person) {
		return String.format("✓ %s - %s %s (%dt) - %s/%s", getPersonFullName(person), person.getThienCan(),
				person.getDiaChi(), person.getAgeRecalculated(), person.getSaoRecalculated().getSaoName(),
				person.getHanRecalculated().getHanName());
	}
}
