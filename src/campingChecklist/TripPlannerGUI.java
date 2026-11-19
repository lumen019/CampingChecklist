package campingChecklist;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * Provides a GUI for managing trips and their packing items.
 *
 * <p>Users can add/remove trips, as well as add/remove packing items belonging
 * to the selected trip..
 * Trip and packing items information is stored temporarily and is not saved after the program closes.</p>
 */
public class TripPlannerGUI extends JPanel {

    /** Stores temporary trip entries displayed by the GUI. */
    private final DefaultListModel<Trip> tripModel =
            new DefaultListModel<>();

    /** Placeholder model for future packing-item entries. */
    private final DefaultListModel<PackingItem> itemModel =
            new DefaultListModel<>();

    /** Displays temporary trips and tracks the selected entry. */
    private final JList<Trip> tripList =
            new JList<>(tripModel);

    /** Placeholder list for the selected trip's packing items. */
    private final JList<PackingItem> itemList =
            new JList<>(itemModel);

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

        itemList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

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

        panel.add(new JLabel("Trips"), BorderLayout.NORTH);
        panel.add(
                new JScrollPane(tripList),
                BorderLayout.CENTER);

        JPanel buttons =
                new JPanel(new GridLayout(1, 2, 5, 5));

        JButton addTripButton = new JButton("Add Trip");
        addTripButton.addActionListener(event -> addTrip());

        JButton removeTripButton = new JButton("Remove Trip");
        removeTripButton.addActionListener(event -> removeTrip());

        buttons.add(addTripButton);
        buttons.add(removeTripButton);

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the packing-item list and its working action buttons.
     *
     * @return the packing-item panel
     */
    private JPanel createItemPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        panel.add(new JLabel("Trip Items"), BorderLayout.NORTH);
        panel.add(
                new JScrollPane(itemList),
                BorderLayout.CENTER);

        JPanel buttons =
                new JPanel(new GridLayout(1, 2, 5, 5));

        
        JButton addItemButton = new JButton("Add Trip Item");
        addItemButton.addActionListener(event -> addItem());
        
        JButton removeItemButton = new JButton("Remove Trip Item");
        removeItemButton.addActionListener(event -> removeItem());
        
        buttons.add(addItemButton);
        buttons.add(removeItemButton);

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
        form.add(new JLabel("Date (use 4-digit year):"));
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
        itemModel.clear();

        if (!tripModel.isEmpty()) {
            tripList.setSelectedIndex(
                    Math.min(selectedIndex, tripModel.size() - 1));
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
    	PackingItem selectedItem = itemList.getSelectedValue();
    	int selectedIndex = itemList.getSelectedIndex();

        if (selectedTrip == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a trip first.",
                    "No Trip Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if (selectedItem == null) {
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

        selectedTrip.removePackingItem(selectedItem);

        if (!itemModel.isEmpty()) {
            itemList.setSelectedIndex(
                    Math.min(selectedIndex, itemModel.size() - 1));
        }
        
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
    	        "MMMM d, yyyy"
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
    	itemModel.clear();
    	
    	Trip selectedTrip = tripList.getSelectedValue();
    	
    	if(selectedTrip != null) {
    		for (PackingItem packingItem : selectedTrip.getPackingItems()) {
    			itemModel.addElement(packingItem);
    		}
    	}
    }
}