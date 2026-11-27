package campingChecklist;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

/**
 * Provides a GUI for managing trips, their packing items, and their notes.
 *
 * <p>Users can add/remove/edit trips and the packing items and note
 * sections belonging to the selected trip, check items off, view a trip's
 * destination on Google Maps, and save or load trips. All changes go through
 * the {@link TripPlanner} and stay in memory until Save is clicked.</p>
 *
 * @author Lia Day
 * @author Colby Almond (co-author)
 */
public class TripPlannerGUI extends JPanel {

	private static final long serialVersionUID = 1L;

	/** Format used to show dates to the user. */
	private static final DateTimeFormatter DISPLAY_DATE =
			DateTimeFormatter.ofPattern("M/d/yyyy");

	/** Formats accepted when the user types a date. */
	private static final String[] INPUT_DATE_FORMATS = {
			"M/d/yyyy",
			"M-d-yyyy",
			"yyyy-MM-dd",
			"MMMM d, yyyy",
			"M/d/yy"
	};

	/** Categories the user can pick from in the item form. */
	private static final String[] DEFAULT_CATEGORIES = {
			"Shelter", "Bedding", "Clothing", "Cooking", "Food",
			"Hydration", "Hygiene", "Lighting", "Safety", "Tools"
	};

	private final TripPlanner planner;
	private final GoogleMapsService mapsService = new GoogleMapsService();

	/** Mirrors the planner's trips for display. */
	private final DefaultListModel<Trip> tripModel =
			new DefaultListModel<>();

	/** Displays trips and tracks the selected entry. */
	private final JList<Trip> tripList = new JList<>(tripModel);

	/** Stores packing item data displayed by the GUI.
	 * Packed column is editable, while rest is read only.
	 * Column 0 contain Boolean data type, while column 3
	 * contains Integer, every other column is data type String.
	 */
	private final DefaultTableModel itemModel =
			new DefaultTableModel(new Object[]{"Packed", "Item", "Category", "Qty"}, 0) {

		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 0; // Only the Packed column is editable
		}

		@Override
		public Class<?> getColumnClass(int column) {
			if (column == 0) {
				return Boolean.class;
			} else if (column == 3) {
				return Integer.class;
			}
			return String.class;
		}
	};

	/** Displays packing item data stored in itemModel. */
	private final JTable itemTable = new JTable(itemModel);

	private final JLabel tripNameLabel = new JLabel();
	private final JLabel destinationLabel = new JLabel();
	private final JLabel datesLabel = new JLabel();
	private final JButton mapButton = new JButton("View on Google Maps");

	private final JLabel progressLabel = new JLabel();
	private final JProgressBar progressBar = new JProgressBar(0, 100);

	/** Mirrors the selected trip's note sections for display. */
	private final DefaultListModel<NoteSection> noteModel =
			new DefaultListModel<>();
	private final JList<NoteSection> noteList = new JList<>(noteModel);
	private final JTextArea noteText = new JTextArea();

	private final JLabel statusLabel = new JLabel("Ready.");

	/**
	 * True while the GUI fills the item table or note text itself, so the
	 * listeners do not treat those updates as user edits.
	 */
	private boolean updatingView;

	/**
	 * Creates the GUI layout, connects the controls, and displays the
	 * planner's current trips.
	 *
	 * @param planner the planner that owns the trips
	 */
	public TripPlannerGUI(TripPlanner planner) {
		this.planner = planner;

		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		tripList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tripList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				selectTrip();
			}
		});

		JSplitPane splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				createTripPanel(),
				createDetailsPanel());

		splitPane.setResizeWeight(0.35);

		add(splitPane, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);

		displayTrips();
	}

	/**
	 * Creates the trip list with its save, load, and trip buttons.
	 *
	 * @return the trip-management panel
	 */
	private JPanel createTripPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(event -> saveTrips());

		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(event -> loadTrips());

		JPanel headerButtons = new JPanel(new GridLayout(1, 2, 5, 5));
		headerButtons.add(saveButton);
		headerButtons.add(loadButton);

		JPanel header = new JPanel(new BorderLayout());
		header.add(new JLabel("Trips"), BorderLayout.WEST);
		header.add(headerButtons, BorderLayout.EAST);

		JButton addTripButton = new JButton("Add Trip");
		addTripButton.addActionListener(event -> addTrip());

		JButton editTripButton = new JButton("Edit Trip");
		editTripButton.addActionListener(event -> editTrip());

		JButton removeTripButton = new JButton("Remove Trip");
		removeTripButton.addActionListener(event -> removeTrip());

		JPanel bottomButtons = new JPanel(new GridLayout(1, 3, 5, 5));
		bottomButtons.add(addTripButton);
		bottomButtons.add(editTripButton);
		bottomButtons.add(removeTripButton);

		panel.add(header, BorderLayout.NORTH);
		panel.add(new JScrollPane(tripList), BorderLayout.CENTER);
		panel.add(bottomButtons, BorderLayout.SOUTH);
		panel.setPreferredSize(new Dimension(320, 0));

		return panel;
	}

	/**
	 * Creates the selected trip's details with packing list and notes tabs.
	 *
	 * @return the trip details panel
	 */
	private JPanel createDetailsPanel() {
		tripNameLabel.setFont(
				tripNameLabel.getFont().deriveFont(Font.BOLD, 16f));

		mapButton.addActionListener(event -> showMap());

		JPanel labels = new JPanel(new GridLayout(3, 1));
		labels.add(tripNameLabel);
		labels.add(destinationLabel);
		labels.add(datesLabel);

		JPanel mapButtonPanel = new JPanel(new BorderLayout());
		mapButtonPanel.add(mapButton, BorderLayout.NORTH);

		JPanel details = new JPanel(new BorderLayout(10, 0));
		details.add(labels, BorderLayout.CENTER);
		details.add(mapButtonPanel, BorderLayout.EAST);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Packing List", createItemPanel());
		tabs.addTab("Notes", createNotesPanel());

		JPanel panel = new JPanel(new BorderLayout(5, 10));
		panel.add(details, BorderLayout.NORTH);
		panel.add(tabs, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Creates the packing-item table, packing progress, and item buttons.
	 *
	 * @return the packing-item panel
	 */
	private JPanel createItemPanel() {
		progressBar.setStringPainted(true);

		JPanel progress = new JPanel(new GridLayout(2, 1, 0, 3));
		progress.add(progressLabel);
		progress.add(progressBar);

		itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemTable.getColumnModel().getColumn(0).setMaxWidth(60);
		itemTable.getColumnModel().getColumn(3).setMaxWidth(60);

		// Checking the Packed box updates the item and the progress.
		itemModel.addTableModelListener(e -> {
			if (!updatingView
					&& e.getType() == TableModelEvent.UPDATE
					&& e.getColumn() == 0) {
				setRowPacked(e.getFirstRow());
			}
		});

		JButton addItemButton = new JButton("Add Item");
		addItemButton.addActionListener(event -> addItem());

		JButton editItemButton = new JButton("Edit Item");
		editItemButton.addActionListener(event -> editItem());

		JButton removeItemButton = new JButton("Remove Item");
		removeItemButton.addActionListener(event -> removeItem());

		JPanel buttons = new JPanel(new GridLayout(1, 3, 5, 5));
		buttons.add(addItemButton);
		buttons.add(editItemButton);
		buttons.add(removeItemButton);

		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(progress, BorderLayout.NORTH);
		panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
		panel.add(buttons, BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * Creates the note section list, the note editor, and section buttons.
	 *
	 * @return the notes panel
	 */
	private JPanel createNotesPanel() {
		noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		noteList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				showSelectedNote();
			}
		});

		noteText.setLineWrap(true);
		noteText.setWrapStyleWord(true);

		// Typing in the editor saves the text into the selected section.
		noteText.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				saveNoteText();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				saveNoteText();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				saveNoteText();
			}
		});

		JButton addSectionButton = new JButton("Add");
		addSectionButton.addActionListener(event -> addNoteSection());

		JButton renameSectionButton = new JButton("Rename");
		renameSectionButton.addActionListener(event -> renameNoteSection());

		JButton removeSectionButton = new JButton("Remove");
		removeSectionButton.addActionListener(event -> removeNoteSection());

		JPanel buttons = new JPanel(new GridLayout(1, 3, 5, 5));
		buttons.add(addSectionButton);
		buttons.add(renameSectionButton);
		buttons.add(removeSectionButton);

		JPanel sections = new JPanel(new BorderLayout(5, 5));
		sections.add(new JLabel("Sections"), BorderLayout.NORTH);
		sections.add(new JScrollPane(noteList), BorderLayout.CENTER);
		sections.add(buttons, BorderLayout.SOUTH);
		sections.setPreferredSize(new Dimension(240, 0));

		JSplitPane splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				sections,
				new JScrollPane(noteText));

		splitPane.setResizeWeight(0.0); // extra width goes to the note text

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(splitPane, BorderLayout.CENTER);

		return panel;
	}

	//Display methods

	/**
	 * Redisplays the planner's trips, keeping the selected trip selected
	 * when it still exists, or selecting the first trip otherwise.
	 */
	void displayTrips() {
		Trip selected = tripList.getSelectedValue();

		tripModel.clear();

		for (Trip trip : planner.getTrips()) {
			tripModel.addElement(trip);
		}

		if (selected != null && tripModel.contains(selected)) {
			tripList.setSelectedValue(selected, true);
		} else if (!tripModel.isEmpty()) {
			tripList.setSelectedIndex(0);
		}

		selectTrip();
	}

	/**
	 * Displays the selected trip's details, packing items, and notes.
	 */
	private void selectTrip() {
		Trip trip = tripList.getSelectedValue();

		if (trip == null) {
			tripNameLabel.setText("No trip selected");
			destinationLabel.setText(" ");
			datesLabel.setText(" ");
		} else {
			tripNameLabel.setText(trip.getName());
			destinationLabel.setText("Destination: " + trip.getDestination());
			datesLabel.setText("Dates: " + formatDates(trip));
		}

		mapButton.setEnabled(trip != null);
		tripList.repaint();

		displayChecklist();
		updateNotes();
	}

	/**
	 * Updates the packing list belonging to the selected trip.
	 */
	private void displayChecklist() {
		Trip trip = tripList.getSelectedValue();

		updatingView = true;
		itemModel.setRowCount(0);

		if (trip != null) {
			for (PackingItem packingItem : trip.getPackingItems()) {
				itemModel.addRow(new Object[] {
						packingItem.isPacked(),
						packingItem.getName(),
						packingItem.getCategory(),
						packingItem.getQuantity()
				});
			}
		}

		updatingView = false;
		updatePackingProgress();
	}

	/**
	 * Shows the selected trip's packing progress, weighted by quantity.
	 */
	private void updatePackingProgress() {
		Trip trip = tripList.getSelectedValue();

		int total = 0;
		int packed = 0;
		double percent = 0.0;

		if (trip != null) {
			for (PackingItem item : trip.getPackingItems()) {
				total += item.getQuantity();
				if (item.isPacked()) {
					packed += item.getQuantity();
				}
			}

			percent = planner.calculateProgress(trip);
		}

		progressLabel.setText(String.format(
				"%d of %d packed — %.0f%%", packed, total, percent));
		progressBar.setValue((int) Math.round(percent));
		progressBar.setString(trip != null && trip.isAllPacked()
				? "All packed!"
				: String.format("%.0f%%", percent));
	}

	/**
	 * Updates the note section list for the selected trip, keeping the
	 * selected section selected when it still exists.
	 */
	private void updateNotes() {
		Trip trip = tripList.getSelectedValue();
		NoteSection selected = noteList.getSelectedValue();

		noteModel.clear();

		if (trip != null) {
			for (NoteSection section : trip.getNoteSections()) {
				noteModel.addElement(section);
			}
		}

		if (selected != null && noteModel.contains(selected)) {
			noteList.setSelectedValue(selected, true);
		} else if (!noteModel.isEmpty()) {
			noteList.setSelectedIndex(0);
		}

		showSelectedNote();
	}

	/**
	 * Shows the selected note section's text in the editor.
	 */
	private void showSelectedNote() {
		NoteSection section = noteList.getSelectedValue();

		updatingView = true;
		noteText.setText(section == null ? "" : section.getContent());
		noteText.setCaretPosition(0);
		updatingView = false;

		noteText.setEnabled(section != null);
	}

	//Packing list actions

	/**
	 * Saves the Packed checkbox in a table row to its item.
	 *
	 * @param row the edited table row
	 */
	private void setRowPacked(int row) {
		Trip trip = tripList.getSelectedValue();
		PackingItem item = trip.getPackingItems().get(row);
		boolean packed = (Boolean) itemModel.getValueAt(row, 0);

		planner.setItemPacked(trip, item, packed);
		updatePackingProgress();
	}

	/**
	 * Prompts for a packing item name, category and quantity then adds
	 * it to the selected trip.
	 *
	 * <p>Displays a message if no trip is selected.</p>
	 */
	private void addItem() {
		Trip trip = requireSelectedTrip();

		if (trip == null) {
			return;
		}

		PackingItem item = showItemDialog("Add Packing Item", null);

		if (item != null) {
			planner.addPackingItem(trip, item);
			displayChecklist();

			int row = itemModel.getRowCount() - 1;
			itemTable.setRowSelectionInterval(row, row);
		}
	}

	/**
	 * Prompts for new details for the selected packing item.
	 * Its packed status is kept.
	 *
	 * <p>Displays a message if no packing item or trip is selected.</p>
	 */
	private void editItem() {
		Trip trip = requireSelectedTrip();
		PackingItem selectedItem = requireSelectedItem(trip);

		if (selectedItem == null) {
			return;
		}

		int row = itemTable.getSelectedRow();
		PackingItem edited = showItemDialog("Edit Packing Item", selectedItem);

		if (edited != null) {
			planner.editPackingItem(
					trip,
					selectedItem,
					edited.getName(),
					edited.getCategory(),
					edited.getQuantity());

			displayChecklist();
			itemTable.setRowSelectionInterval(row, row);
		}
	}

	/**
	 * Removes the selected packing item from the selected trip after confirmation.
	 *
	 * <p>Displays a message if no packing item or trip is selected.</p>
	 */
	private void removeItem() {
		Trip trip = requireSelectedTrip();
		PackingItem selectedItem = requireSelectedItem(trip);

		if (selectedItem == null) {
			return;
		}

		int result = JOptionPane.showConfirmDialog(
				this,
				"Remove the selected item?",
				"Remove Item",
				JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.YES_OPTION) {
			planner.removePackingItem(trip, selectedItem);
			displayChecklist();
		}
	}

	/**
	 * Shows the packing item form until the user enters valid values
	 * or cancels.
	 *
	 * @param title the dialog title
	 * @param existing the item whose values fill the form, or null
	 * @return a new item holding the entered values, or null if canceled
	 */
	private PackingItem showItemDialog(String title, PackingItem existing) {
		JTextField nameField = new JTextField(20);
		// Read-only pick list: the standard categories plus any others
		// already on this trip (e.g. from an older save file), so editing
		// an item never loses its category.
		Set<String> categories = new LinkedHashSet<>(List.of(DEFAULT_CATEGORIES));
		for (PackingItem item : tripList.getSelectedValue().getPackingItems()) {
			categories.add(item.getCategory());
		}
		JComboBox<String> categoryField =
				new JComboBox<>(categories.toArray(new String[0]));
		JTextField quantityField = new JTextField("1", 20);

		if (existing != null) {
			nameField.setText(existing.getName());
			categoryField.setSelectedItem(existing.getCategory());
			quantityField.setText(String.valueOf(existing.getQuantity()));
		}

		JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));

		form.add(new JLabel("Item name:"));
		form.add(nameField);
		form.add(new JLabel("Category:"));
		form.add(categoryField);
		form.add(new JLabel("Quantity:"));
		form.add(quantityField);

		while (true) {
			int result = JOptionPane.showConfirmDialog(
					this,
					form,
					title,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			if (result != JOptionPane.OK_OPTION) {
				return null;
			}

			String name = nameField.getText().trim();
			String category = (String) categoryField.getSelectedItem();

			if (name.isEmpty()) {
				showError("Missing Item Information", "Enter an item name.");
				continue;
			}

			int quantity;

			try {
				quantity = Integer.parseInt(quantityField.getText().trim());
			} catch (NumberFormatException e) {
				showError("Invalid Quantity",
						"Quantity must be a whole number.");
				continue;
			}

			if (quantity < 1) {
				showError("Invalid Quantity", "Quantity must be at least 1.");
				continue;
			}

			return new PackingItem(name, category, quantity);
		}
	}

	//Trip actions

	/**
	 * Prompts for a trip name, destination and dates then adds the trip.
	 *
	 * <p>Canceling leaves the list unchanged.</p>
	 */
	private void addTrip() {
		Trip trip = showTripDialog("Add Trip", null);

		if (trip != null) {
			planner.addTrip(trip);
			displayTrips();
			tripList.setSelectedValue(trip, true);
		}
	}

	/**
	 * Prompts for new details for the selected trip. Its packing items
	 * and notes are kept.
	 *
	 * <p>Displays a message if no trip is selected.</p>
	 */
	private void editTrip() {
		Trip selectedTrip = requireSelectedTrip();

		if (selectedTrip == null) {
			return;
		}

		Trip edited = showTripDialog("Edit Trip", selectedTrip);

		if (edited != null) {
			planner.editTrip(
					selectedTrip,
					edited.getName(),
					edited.getDestination(),
					edited.getStartDate(),
					edited.getEndDate());

			selectTrip();
		}
	}

	/**
	 * Removes the selected trip after confirmation.
	 *
	 * <p>Displays a message if no trip is selected.</p>
	 */
	private void removeTrip() {
		Trip selectedTrip = requireSelectedTrip();

		if (selectedTrip == null) {
			return;
		}

		int selectedIndex = tripList.getSelectedIndex();

		int result = JOptionPane.showConfirmDialog(
				this,
				"Remove the selected trip?",
				"Remove Trip",
				JOptionPane.YES_NO_OPTION);

		if (result != JOptionPane.YES_OPTION) {
			return;
		}

		planner.removeTrip(selectedTrip);
		displayTrips();

		if (!tripModel.isEmpty()) {
			tripList.setSelectedIndex(
					Math.min(selectedIndex, tripModel.size() - 1));
		}
	}

	/**
	 * Shows the trip form until the user enters valid values or cancels.
	 *
	 * <p>Name and destination are required. Dates are optional, but the end
	 * date cannot be before the start date.</p>
	 *
	 * @param title the dialog title
	 * @param existing the trip whose values fill the form, or null
	 * @return a new trip holding the entered values, or null if canceled
	 */
	private Trip showTripDialog(String title, Trip existing) {
		JTextField nameField = new JTextField(20);
		JTextField destinationField = new JTextField(20);
		JTextField startField = new JTextField(20);
		JTextField endField = new JTextField(20);

		if (existing != null) {
			nameField.setText(existing.getName());
			destinationField.setText(existing.getDestination());
			startField.setText(formatDate(existing.getStartDate()));
			endField.setText(formatDate(existing.getEndDate()));
		}

		JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));

		form.add(new JLabel("Trip name:"));
		form.add(nameField);
		form.add(new JLabel("Destination (city, park, or address):"));
		form.add(destinationField);
		form.add(new JLabel("Start date (e.g. 6/14/2026, optional):"));
		form.add(startField);
		form.add(new JLabel("End date (optional):"));
		form.add(endField);

		while (true) {
			int result = JOptionPane.showConfirmDialog(
					this,
					form,
					title,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			if (result != JOptionPane.OK_OPTION) {
				return null;
			}

			String name = nameField.getText().trim();
			String destination = destinationField.getText().trim();

			if (name.isEmpty() || destination.isEmpty()) {
				showError("Missing Trip Information",
						"Enter both a trip name and a destination.");
				continue;
			}

			try {
				LocalDate startDate = parseDate(startField.getText());
				LocalDate endDate = parseDate(endField.getText());

				return new Trip(name, destination, startDate, endDate);
			} catch (IllegalArgumentException e) {
				showError("Invalid Trip Dates", e.getMessage());
			}
		}
	}

	//Note actions

	/**
	 * Saves the editor text into the selected note section.
	 */
	private void saveNoteText() {
		NoteSection section = noteList.getSelectedValue();

		if (!updatingView && section != null) {
			planner.editNoteSection(
					tripList.getSelectedValue(),
					section,
					section.getTitle(),
					noteText.getText());
		}
	}

	/**
	 * Prompts for a section title and adds an empty section to the
	 * selected trip.
	 */
	private void addNoteSection() {
		Trip trip = requireSelectedTrip();

		if (trip == null) {
			return;
		}

		String title = promptSectionTitle("Add Note Section", "");

		if (title != null) {
			NoteSection section = new NoteSection(title, "");
			planner.addNoteSection(trip, section);
			updateNotes();
			noteList.setSelectedValue(section, true);
			noteText.requestFocusInWindow();
		}
	}

	/**
	 * Prompts for a new title for the selected note section.
	 */
	private void renameNoteSection() {
		Trip trip = requireSelectedTrip();
		NoteSection section = requireSelectedSection(trip);

		if (section == null) {
			return;
		}

		String title = promptSectionTitle(
				"Rename Note Section", section.getTitle());

		if (title != null) {
			planner.editNoteSection(trip, section, title, section.getContent());
			noteList.repaint();
		}
	}

	/**
	 * Removes the selected note section after confirmation.
	 */
	private void removeNoteSection() {
		Trip trip = requireSelectedTrip();
		NoteSection section = requireSelectedSection(trip);

		if (section == null) {
			return;
		}

		int result = JOptionPane.showConfirmDialog(
				this,
				"Remove the \"" + section.getTitle() + "\" section and its notes?",
				"Remove Note Section",
				JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.YES_OPTION) {
			planner.removeNoteSection(trip, section);
			updateNotes();
		}
	}

	/**
	 * Asks for a nonblank section title.
	 *
	 * @param title the dialog title
	 * @param initial the starting text
	 * @return the trimmed title, or null if canceled
	 */
	private String promptSectionTitle(String title, String initial) {
		while (true) {
			String input = (String) JOptionPane.showInputDialog(
					this,
					"Section title (e.g. Campsite, Meal Plan):",
					title,
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					initial);

			if (input == null) {
				return null;
			}

			if (!input.isBlank()) {
				return input.trim();
			}

			showError("Missing Title", "Enter a section title.");
		}
	}

	//File actions

	/**
	 * Saves all trips to the planner's file.
	 */
	private void saveTrips() {
		try {
			planner.saveTrips();
			statusLabel.setText("Trips saved successfully (" + countTrips() + ").");
		} catch (UncheckedIOException e) {
			showError("Save Failed", e.getMessage());
		}
	}

	/**
	 * Asks whether to save unsaved changes before the window closes.
	 *
	 * <p>Returns true without asking when there is nothing to save.
	 * Choosing Save closes only if saving succeeds.</p>
	 *
	 * @return true if the window may close, false to keep it open
	 */
	public boolean confirmClose() {
		if (!planner.hasUnsavedChanges()) {
			return true;
		}

		Object[] options = {"Save", "Don't Save", "Cancel"};

		int choice = JOptionPane.showOptionDialog(
				this,
				"Save changes to your trips before closing?",
				"Unsaved Changes",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				options,
				options[0]);

		if (choice == 0) {
			saveTrips();
			return !planner.hasUnsavedChanges();
		}

		return choice == 1;
	}

	/**
	 * Replaces the current trips with the saved trips, after confirmation
	 * if there are unsaved changes.
	 */
	private void loadTrips() {
		if (planner.hasUnsavedChanges()) {
			int result = JOptionPane.showConfirmDialog(
					this,
					"Replace the current trips with the saved trips?\n"
							+ "Unsaved changes will be lost.",
					"Load Trips",
					JOptionPane.YES_NO_OPTION);

			if (result != JOptionPane.YES_OPTION) {
				return;
			}
		}

		try {
			planner.loadTrips();
			displayTrips();
			statusLabel.setText("Trips loaded successfully (" + countTrips() + ").");
		} catch (UncheckedIOException e) {
			showError("Load Failed", e.getMessage());
		}
	}

	//Google Maps

	/**
	 * Shows the selected trip's destination on a Google Maps image when an
	 * API key is configured, or opens it in the browser otherwise.
	 */
	private void showMap() {
		Trip trip = requireSelectedTrip();

		if (trip == null) {
			return;
		}

		String destination = trip.getDestination();

		if (!mapsService.hasApiKey()) {
			openInBrowser(mapsService.getSearchUri(destination));
			return;
		}

		mapButton.setEnabled(false);
		mapButton.setText("Loading map...");

		// Download off the Swing thread so the window stays responsive.
		new SwingWorker<BufferedImage, Void>() {
			@Override
			protected BufferedImage doInBackground() throws IOException {
				return mapsService.fetchStaticMap(destination);
			}

			@Override
			protected void done() {
				mapButton.setText("View on Google Maps");
				mapButton.setEnabled(tripList.getSelectedValue() != null);

				try {
					showMapDialog(destination, get());
				} catch (ExecutionException e) {
					showMapError(destination, e.getCause().getMessage());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}.execute();
	}

	private void showMapDialog(String destination, BufferedImage image) {
		Object[] options = {"Open in Google Maps", "Close"};

		int choice = JOptionPane.showOptionDialog(
				this,
				new JLabel(new ImageIcon(image)),
				destination,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				options,
				options[1]);

		if (choice == 0) {
			openInBrowser(mapsService.getSearchUri(destination));
		}
	}

	private void showMapError(String destination, String message) {
		int choice = JOptionPane.showConfirmDialog(
				this,
				"Could not load the map image.\n" + message
						+ "\n\nOpen " + destination + " in Google Maps instead?",
				"Map Unavailable",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (choice == JOptionPane.YES_OPTION) {
			openInBrowser(mapsService.getSearchUri(destination));
		}
	}

	private void openInBrowser(URI uri) {
		if (Desktop.isDesktopSupported()
				&& Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(uri);
				return;
			} catch (IOException e) {
				// Fall through and show the link instead.
			}
		}

		JTextField link = new JTextField(uri.toString());
		link.setEditable(false);

		JOptionPane.showMessageDialog(
				this,
				new Object[] {"Open this link in your browser:", link},
				"Google Maps",
				JOptionPane.INFORMATION_MESSAGE);
	}

	//Helper methods

	/**
	 * Returns the selected trip, or shows a message and returns null.
	 */
	private Trip requireSelectedTrip() {
		Trip trip = tripList.getSelectedValue();

		if (trip == null) {
			JOptionPane.showMessageDialog(
					this,
					"Select a trip first.",
					"No Trip Selected",
					JOptionPane.INFORMATION_MESSAGE);
		}

		return trip;
	}

	/**
	 * Returns the selected item of a trip, or shows a message and
	 * returns null. Returns null without a message if trip is null.
	 */
	private PackingItem requireSelectedItem(Trip trip) {
		if (trip == null) {
			return null;
		}

		int row = itemTable.getSelectedRow();

		if (row == -1) {
			JOptionPane.showMessageDialog(
					this,
					"Select an item first.",
					"No Item Selected",
					JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		return trip.getPackingItems().get(row);
	}

	/**
	 * Returns the selected note section, or shows a message and returns
	 * null. Returns null without a message if trip is null.
	 */
	private NoteSection requireSelectedSection(Trip trip) {
		if (trip == null) {
			return null;
		}

		NoteSection section = noteList.getSelectedValue();

		if (section == null) {
			JOptionPane.showMessageDialog(
					this,
					"Select a note section first.",
					"No Section Selected",
					JOptionPane.INFORMATION_MESSAGE);
		}

		return section;
	}

	private void showError(String title, String message) {
		JOptionPane.showMessageDialog(
				this, message, title, JOptionPane.ERROR_MESSAGE);
	}

	private String countTrips() {
		int count = tripModel.size();
		return count + (count == 1 ? " trip" : " trips");
	}

	/**
	 * Describes a trip's dates, such as "6/14/2026 – 6/16/2026 (3 days)".
	 */
	private static String formatDates(Trip trip) {
		LocalDate start = trip.getStartDate();
		LocalDate end = trip.getEndDate();

		if (start == null && end == null) {
			return "not set";
		}

		if (end == null) {
			return "starts " + formatDate(start);
		}

		if (start == null) {
			return "ends " + formatDate(end);
		}

		long days = ChronoUnit.DAYS.between(start, end) + 1;

		return formatDate(start) + " – " + formatDate(end)
				+ " (" + days + (days == 1 ? " day)" : " days)");
	}

	private static String formatDate(LocalDate date) {
		return date == null ? "" : date.format(DISPLAY_DATE);
	}

	/**
	 * Converts a date typed by the user into a LocalDate.
	 *
	 * @param dateString date entered by user
	 * @return the date, or null if dateString is blank
	 * @throws IllegalArgumentException if the date is not in a known format
	 */
	private static LocalDate parseDate(String dateString) {
		String text = dateString.trim();

		if (text.isEmpty()) {
			return null;
		}

		for (String format : INPUT_DATE_FORMATS) {
			try {
				return LocalDate.parse(text, DateTimeFormatter.ofPattern(format));
			} catch (DateTimeException e) {
				//empty to try the next format
			}
		}

		throw new IllegalArgumentException(
				"\"" + text + "\" is not a valid date. Enter dates like 6/14/2026.");
	}

	//Accessors used by TripPlannerGUITest

	JList<Trip> getTripList() {
		return tripList;
	}

	JTable getItemTable() {
		return itemTable;
	}

	JList<NoteSection> getNoteList() {
		return noteList;
	}

	JTextArea getNoteTextArea() {
		return noteText;
	}

	String getProgressText() {
		return progressLabel.getText();
	}

	String getDatesText() {
		return datesLabel.getText();
	}
}
