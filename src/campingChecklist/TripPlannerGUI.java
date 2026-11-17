package campingChecklist;

import java.awt.BorderLayout;
import java.awt.GridLayout;

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
 * Provides a starter GUI with working add and remove trip controls.
 *
 * <p>Trips are temporarily stored as display strings in this panel.
 * Backend integration, packing-item controls, and persistence remain
 * to be implemented.</p>
 */
public class TripPlannerGUI extends JPanel {

    /** Stores temporary trip entries displayed by the GUI. */
    private final DefaultListModel<String> tripModel =
            new DefaultListModel<>();

    /** Placeholder model for future packing-item entries. */
    private final DefaultListModel<String> itemModel =
            new DefaultListModel<>();

    /** Displays temporary trips and tracks the selected entry. */
    private final JList<String> tripList =
            new JList<>(tripModel);

    /** Placeholder list for the selected trip's packing items. */
    private final JList<String> itemList =
            new JList<>(itemModel);

    /**
     * Creates the layout and connects the add/remove trip controls.
     */
    public TripPlannerGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(
                10, 10, 10, 10));

        tripList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

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
     * Creates the packing-item area with disabled placeholder buttons.
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
        JButton removeItemButton = new JButton("Remove Trip Item");

        // TODO: Implement item controls after backend integration.
        addItemButton.setEnabled(false);
        removeItemButton.setEnabled(false);

        buttons.add(addItemButton);
        buttons.add(removeItemButton);

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Prompts for a trip name and location, then adds a display entry.
     *
     * <p>Both fields are required. Invalid input keeps the form open
     * for correction. Canceling leaves the list unchanged.</p>
     */
    private void addTrip() {
        JTextField nameField = new JTextField(20);
        JTextField locationField = new JTextField(20);

        JPanel form =
                new JPanel(new GridLayout(0, 1, 5, 5));

        form.add(new JLabel("Trip name:"));
        form.add(nameField);
        form.add(new JLabel("Location:"));
        form.add(locationField);

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

            if (name.isEmpty() || location.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter both a trip name and a location.",
                        "Missing Trip Information",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            // TODO: Replace temporary strings with Trip objects
            // and delegate storage to TripPlanner.
            tripModel.addElement(name + " - " + location);
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

        if (!tripModel.isEmpty()) {
            tripList.setSelectedIndex(
                    Math.min(selectedIndex, tripModel.size() - 1));
        }
    }
}