package campingChecklist;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Provides a GUI for managing trips and their packing items.
 *
 * <p>Users can add/remove/edit trips, as well as add/remove/edit packing items belonging
 * to the selected trip..
 * Trip and packing items information is stored temporarily and is not saved after the program closes.</p>
 */
public class TripPlannerGUI extends JPanel {

	private static final long serialVersionUID = 1L;

	/** Stores temporary trip entries displayed by the GUI. */
    private final DefaultListModel<Trip> tripModel =
            new DefaultListModel<>();
 
    /** Displays temporary trips and tracks the selected entry. */
    private final JList<Trip> tripList =
            new JList<>(tripModel);

    /** Stores temporary packing item data displayed by the GUI
     * Packed column is editable, while rest is read only.
     * Column 0 contain Boolean data type, while column 3 
     * contains Integer, every other column is data type String.
     */
    private DefaultTableModel itemModel =
    		new DefaultTableModel(new Object[]{"Packed", "Item", "Category", "Qty"}, 0) {
    	
    	@Override
        public boolean isCellEditable(int row, int column) {
            return column == 0; // Only the first column (index 0) is editable
        }
    	
    	@Override
    	public Class<?> getColumnClass(int column){
    		if (column == 0) {
                return Boolean.class;
            } else if (column ==3) {
            	return Integer.class;
            }
            return String.class;
    	}
    };

    /** Displays packing item data stored in itemModel. */
    private JTable itemTable = new JTable(itemModel);

    /**
     * Creates the GUI layout and connects the trip and packing list controls.
     */
    public TripPlannerGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(
                10, 10, 10, 10));

        tripList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        
        tripList.addListSelectionListener((e -> {
        	if(!e.getValueIsAdjusting()) {
        		 updateItemList();
        	}
        }));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createTripPanel(),
                createItemPanel());

        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);
        add(
                new JLabel(
                        "Temporary trip list — changes are not saved."),
                BorderLayout.SOUTH);
    }

    /**
     * Creates the trip list and its working action buttons.
     *
     * @return the trip-management panel
     */
    private JPanel createTripPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel header = new JPanel(new BorderLayout());

        JPanel headerButtons = new JPanel(new BorderLayout());
       
        panel.add(header, BorderLayout.NORTH);
        panel.add(
                new JScrollPane(tripList),
                BorderLayout.CENTER);
        
        header.add(new JLabel("Trip"), BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);
        
        JButton saveButton = new JButton("Save");
//        saveButton.addActionListener(event -> saveTrip()); //TODO
        
        JButton loadButton = new JButton("Load");
//        loadButton.addActionListener(event -> loadTrip()); //TODO
        
        headerButtons.add(saveButton, BorderLayout.WEST);
        headerButtons.add(loadButton, BorderLayout.EAST);

        JPanel bottomButtons =
                new JPanel(new GridLayout(1, 5, 5, 5));

        JButton addTripButton = new JButton("Add Trip");
        addTripButton.addActionListener(event -> addTrip());

        JButton removeTripButton = new JButton("Remove Trip");
        removeTripButton.addActionListener(event -> removeTrip());
        
        JButton editTripButton = new JButton("Edit Trip"); 
        editTripButton.addActionListener(event -> editTrip());
        
        bottomButtons.add(addTripButton);
        bottomButtons.add(removeTripButton);
        bottomButtons.add(editTripButton);
        
        panel.add(bottomButtons, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the packing-item list and its working action buttons.
     *
     * @return the packing-item panel
     */
    private JPanel createItemPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JPanel header = new JPanel(new GridLayout(2, 1));

        header.add(new JLabel("Trip Items"));
        header.add(new JLabel ("Packing Progress:  0.0%"));
        
        panel.add(header, BorderLayout.NORTH);
        
        panel.add(
                new JScrollPane(itemTable),
                BorderLayout.CENTER);

        JPanel buttons =
                new JPanel(new GridLayout(1, 3, 5, 5));

        
        JButton addItemButton = new JButton("Add Item");
        addItemButton.addActionListener(event -> addItem());
        
        JButton removeItemButton = new JButton("Remove Item");
        removeItemButton.addActionListener(event -> removeItem());
        
        JButton editItemButton = new JButton("Edit Item");
//        editItemButton.addActionListener(event -> editItem()); //TODO
        
        buttons.add(addItemButton);
        buttons.add(removeItemButton);
        buttons.add(editItemButton);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }
    
    //Button Methods

    /**
     * Prompts for a trip name, location and date then adds a display entry.
     *
     * <p>Both fields are required. Invalid input keeps the form open
     * for correction. Canceling leaves the list unchanged.</p>
     */
    private void addTrip() {
        JTextField nameField = new JTextField(20);
        JTextField locationField = new JTextField(20);
        JTextField dateField = new JTextField(20);

        JPanel form =
                new JPanel(new GridLayout(0, 1, 5, 5));

        form.add(new JLabel("Trip name:"));
        form.add(nameField);
        form.add(new JLabel("Location:"));
        form.add(locationField);
        form.add(new JLabel("Date:"));
        form.add(dateField);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    "Add Trip",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String name = nameField.getText().trim();
            String location = locationField.getText().trim();
            String dateString = dateField.getText().trim();
            LocalDate date = parseDate(dateString);

            if (name.isEmpty() || location.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter both a trip name and a location.",
                        "Missing Trip Information",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }
      
            Trip trip = new Trip(location, date);
            trip.setName(name);
            tripModel.addElement(trip);

            tripList.setSelectedIndex(tripModel.size() - 1);
            return;
        }
    }

    /**
     * Removes the selected temporary trip after confirmation.
     *
     * <p>Displays a message if no trip is selected.</p>
     */
    private void removeTrip() {
        int selectedIndex = tripList.getSelectedIndex();

        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a trip first.",
                    "No Trip Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "Remove the selected trip?",
                "Remove Trip",
                JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        tripModel.remove(selectedIndex);
        updateItemList();

        if (!tripModel.isEmpty()) {
            tripList.setSelectedIndex(
                    Math.min(selectedIndex, tripModel.size() - 1));
        }
    }
    
    private void editTrip() {
    	Trip selectedTrip = tripList.getSelectedValue();
    	int selectedIndex = tripList.getSelectedIndex();

        if (selectedTrip == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a trip first.",
                    "No Trip Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }
    
    /**
     * Prompts for a packing item name, category and quantity then adds a display entry.
     * 
     * <p>Displays a message if no trip is selected.</p>
     */
    private void addItem() {
    	Trip selectedTrip = tripList.getSelectedValue();

        if (selectedTrip == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a trip first.",
                    "No Trip Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    	
        JTextField nameField = new JTextField(20);
        JTextField categoryField = new JTextField(20);
        JTextField quantityField = new JTextField(20);

        JPanel form =
                new JPanel(new GridLayout(0, 1, 5, 5));

        form.add(new JLabel("Item name:"));
        form.add(nameField);
        form.add(new JLabel("Category:"));
        form.add(categoryField);
        form.add(new JLabel("Quanity:"));
        form.add(quantityField);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    "Add Packing Item",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            int quantity = Integer.parseInt(quantityField.getText().trim());
           
            if (name.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter a item name, category and quantity",
                        "Missing Item category",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }
     
            PackingItem item = new PackingItem(name, category, quantity);
            selectedTrip.addPackingItem(item);
            
            updateItemList();
            return;
        }
    }
    
    /**
     * Removes the selected packing item from the selected trip after confirmation.
     *
     * <p>Displays a message if no packing item or trip is selected.</p>
     */
    private void removeItem () {
    	Trip selectedTrip = tripList.getSelectedValue();
    	int selectedRow = itemTable.getSelectedRow();

        if (selectedTrip == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a trip first.",
                    "No Trip Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select an item first.",
                    "No Item Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        

        int result = JOptionPane.showConfirmDialog(
                this,
                "Remove the selected item?",
                "Remove Item",
                JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        PackingItem selectedItem = selectedTrip.getPackingItems().get(selectedRow);

        selectedTrip.removePackingItem(selectedItem);

        updateItemList();
    }
    
    //Helper methods 
    
    /**
     * Converts a date string into LocalDate.
     * 
     * @param dateString date entered by user
     * @return date in LocalDate
     */
    private LocalDate parseDate(String dateString) {
    	String[] formats = {
    	        "M/d/yyyy",
    	        "M-d-yyyy",
    	        "yyyy-MM-dd",
    	        "MMMM d, yyyy",
    	        "M/d/yy"
    	    };
    	
    	for (String format : formats) {
    		try {
    			return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(format));
    		} catch (DateTimeException e) {
    			//empty to try the next format
    		}
    	}
    	
    	throw new IllegalArgumentException("Enter a valid date format.");
    }
    
    /**
     * Updates the packing list belonging to the selected trip.
     */
    private void updateItemList() {
    	itemModel.setRowCount(0);
    	
    	Trip selectedTrip = tripList.getSelectedValue();
    	
    	if(selectedTrip != null) {
    		for (PackingItem packingItem : selectedTrip.getPackingItems()) {
    			itemModel.addRow(new Object[] {
    					packingItem.getPacked(),
    					packingItem.getName(),
    					packingItem.getCategory(),
    					packingItem.getQuantity()
    			});
    		}
    		
    		
    	}
    }
}