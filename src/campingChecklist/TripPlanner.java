package campingChecklist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Manages camping trips, their packing items and note sections,
 * and coordinates file storage.
 *
 * <p>Changes remain in memory until saveTrips() is called;
 * hasUnsavedChanges() reports whether any are pending.</p>
 *
 * @author Colby Almond
 */
public class TripPlanner {

    private ArrayList<Trip> trips;
    private final TripFileManager fileManager;
    private boolean unsavedChanges;

    /**
     * Creates an empty planner using trips.dat for storage.
     */
    public TripPlanner() {
        this("trips.dat");
    }

    /**
     * Creates an empty planner using the specified storage file.
     *
     * @param fileName the storage file path
     * @throws IllegalArgumentException if the file name is null or blank
     */
    public TripPlanner(String fileName) {
        trips = new ArrayList<>();
        fileManager = new TripFileManager(fileName);
    }

    /**
     * Returns a copy of the trip list containing the current Trip objects.
     *
     * <p>Changing the returned list does not change the planner's list.
     * The Trip objects themselves remain editable.</p>
     *
     * @return a copy of the trip list
     */
    public ArrayList<Trip> getTrips() {
        return new ArrayList<>(trips);
    }

    /**
     * Adds a trip to this planner.
     *
     * @param trip the trip to add
     * @throws NullPointerException if trip is null
     * @throws IllegalArgumentException if the trip is already managed
     */
    public void addTrip(Trip trip) {
        Objects.requireNonNull(trip, "Trip cannot be null.");

        if (trips.contains(trip)) {
            throw new IllegalArgumentException(
                    "This trip is already in the planner.");
        }

        trips.add(trip);
        unsavedChanges = true;
    }

    /**
     * Verifies that a trip belongs to this planner.
     *
     * <p>This overload does not change trip details. Callers may update
     * the managed trip through its setters, then call this method so the
     * change counts as unsaved.</p>
     *
     * @param trip the trip to verify
     * @throws NullPointerException if trip is null
     * @throws IllegalArgumentException if the trip is not managed
     */
    public void editTrip(Trip trip) {
        requireManagedTrip(trip);
        unsavedChanges = true;
    }

    /**
     * Updates a managed trip after validating all new values.
     *
     * <p>Packing items and note sections are preserved.</p>
     *
     * @param trip the managed trip to edit
     * @param name the new nonblank name
     * @param destination the new nonblank destination
     * @param startDate the new start date, or null
     * @param endDate the new end date, or null
     * @throws NullPointerException if trip, name, or destination is null
     * @throws IllegalArgumentException if the trip is not managed,
     *         the new name or destination is blank, or endDate is
     *         before startDate
     */
    public void editTrip(
            Trip trip,
            String name,
            String destination,
            LocalDate startDate,
            LocalDate endDate) {

        requireManagedTrip(trip);

        Trip validated = new Trip(name, destination, startDate, endDate);

        trip.setName(validated.getName());
        trip.setDestination(validated.getDestination());
        trip.setDates(validated.getStartDate(), validated.getEndDate());
        unsavedChanges = true;
    }

    /**
     * Removes a trip if it belongs to this planner.
     *
     * <p>An unknown trip leaves the planner unchanged.</p>
     *
     * @param trip the trip to remove
     * @throws NullPointerException if trip is null
     */
    public void deleteTrip(Trip trip) {
        Objects.requireNonNull(trip, "Trip cannot be null.");
        if (trips.remove(trip)) {
            unsavedChanges = true;
        }
    }

    /**
     * Alternate name for deleteTrip(), used by GUI callers.
     *
     * @param trip the trip to remove
     * @throws NullPointerException if trip is null
     */
    public void removeTrip(Trip trip) {
        deleteTrip(trip);
    }

    /**
     * Adds a packing item to a managed trip.
     *
     * <p>The same item object cannot be added to the selected trip twice.
     * Separate item objects with identical details are allowed.</p>
     *
     * @param trip the managed trip receiving the item
     * @param item the item to add
     * @throws NullPointerException if trip or item is null
     * @throws IllegalArgumentException if the trip is not managed
     *         or the item already belongs to the trip
     */
    public void addPackingItem(Trip trip, PackingItem item) {
        requireManagedTrip(trip);
        Objects.requireNonNull(item, "Packing item cannot be null.");

        if (trip.getPackingItems().contains(item)) {
            throw new IllegalArgumentException(
                    "This packing item already belongs to the trip.");
        }

        trip.addPackingItem(item);
        unsavedChanges = true;
    }

    /**
     * Removes an item from a managed trip.
     *
     * @param trip the managed trip containing the item
     * @param item the item to remove
     * @throws NullPointerException if trip or item is null
     * @throws IllegalArgumentException if the trip is not managed
     *         or the item does not belong to the trip
     */
    public void removePackingItem(Trip trip, PackingItem item) {
        requireManagedItem(trip, item);
        trip.removePackingItem(item);
        unsavedChanges = true;
    }

    /**
     * Updates an item's details after validating all new values.
     *
     * <p>The item's packed status is preserved.</p>
     *
     * @param trip the managed trip containing the item
     * @param item the item to edit
     * @param name the new nonblank name
     * @param category the new nonblank category
     * @param quantity the new quantity, at least 1
     * @throws NullPointerException if trip, item, name, or category is null
     * @throws IllegalArgumentException if membership validation fails
     *         or any new item value is invalid
     */
    public void editPackingItem(
            Trip trip,
            PackingItem item,
            String name,
            String category,
            int quantity) {

        requireManagedItem(trip, item);

        PackingItem validated =
                new PackingItem(name, category, quantity);

        item.setName(validated.getName());
        item.setCategory(validated.getCategory());
        item.setQuantity(validated.getQuantity());
        unsavedChanges = true;
    }

    /**
     * Marks an item as packed or unpacked.
     *
     * @param trip the managed trip containing the item
     * @param item the item to update
     * @param packed true for packed, false for unpacked
     * @throws NullPointerException if trip or item is null
     * @throws IllegalArgumentException if the trip is not managed
     *         or the item does not belong to the trip
     */
    public void setItemPacked(
            Trip trip,
            PackingItem item,
            boolean packed) {

        requireManagedItem(trip, item);
        item.setPacked(packed);
        unsavedChanges = true;
    }

    /**
     * Returns the percentage of the trip's total item quantity
     * that is packed.
     *
     * @param trip the managed trip to inspect
     * @return packing progress from 0.0 to 100.0
     * @throws NullPointerException if trip is null
     * @throws IllegalArgumentException if the trip is not managed
     */
    public double calculateProgress(Trip trip) {
        requireManagedTrip(trip);
        return trip.calculateProgress();
    }

    /**
     * Adds a note section to a managed trip.
     *
     * @param trip the managed trip receiving the section
     * @param section the section to add
     * @throws NullPointerException if trip or section is null
     * @throws IllegalArgumentException if the trip is not managed
     *         or the section already belongs to the trip
     */
    public void addNoteSection(Trip trip, NoteSection section) {
        requireManagedTrip(trip);
        Objects.requireNonNull(section, "Note section cannot be null.");

        if (trip.getNoteSections().contains(section)) {
            throw new IllegalArgumentException(
                    "This note section already belongs to the trip.");
        }

        trip.addNoteSection(section);
        unsavedChanges = true;
    }

    /**
     * Updates a note section after validating the new values.
     *
     * @param trip the managed trip containing the section
     * @param section the section to edit
     * @param title the new nonblank title
     * @param content the new text, which may be empty
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if membership validation fails
     *         or the title is blank
     */
    public void editNoteSection(
            Trip trip,
            NoteSection section,
            String title,
            String content) {

        requireManagedSection(trip, section);

        NoteSection validated = new NoteSection(title, content);

        section.setTitle(validated.getTitle());
        section.setContent(validated.getContent());
        unsavedChanges = true;
    }

    /**
     * Removes a note section from a managed trip.
     *
     * @param trip the managed trip containing the section
     * @param section the section to remove
     * @throws NullPointerException if trip or section is null
     * @throws IllegalArgumentException if the trip is not managed
     *         or the section does not belong to the trip
     */
    public void removeNoteSection(Trip trip, NoteSection section) {
        requireManagedSection(trip, section);
        trip.removeNoteSection(section);
        unsavedChanges = true;
    }

    /**
     * Saves all current trips with their items and notes.
     *
     * @throws java.io.UncheckedIOException if saving fails
     */
    public void saveTrips() {
        fileManager.saveTrips(trips);
        unsavedChanges = false;
    }

    /**
     * Replaces current trips with the saved trips.
     *
     * <p>A missing file produces an empty list. If loading throws an
     * exception, the current trips remain unchanged.</p>
     *
     * @throws java.io.UncheckedIOException if loading fails
     */
    public void loadTrips() {
        trips = fileManager.loadTrips();
        unsavedChanges = false;
    }

    /**
     * Returns whether trips changed since they were last saved or loaded.
     *
     * <p>Only changes made through this planner's methods are tracked.</p>
     *
     * @return true if there are unsaved changes
     */
    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    /**
     * Verifies that a trip belongs to this planner.
     */
    private void requireManagedTrip(Trip trip) {
        Objects.requireNonNull(trip, "Trip cannot be null.");

        if (!trips.contains(trip)) {
            throw new IllegalArgumentException(
                    "Trip does not belong to this planner.");
        }
    }

    /**
     * Verifies that an item belongs to the selected managed trip.
     */
    private void requireManagedItem(Trip trip, PackingItem item) {
        requireManagedTrip(trip);
        Objects.requireNonNull(item, "Packing item cannot be null.");

        if (!trip.getPackingItems().contains(item)) {
            throw new IllegalArgumentException(
                    "Packing item does not belong to this trip.");
        }
    }

    /**
     * Verifies that a note section belongs to the selected managed trip.
     */
    private void requireManagedSection(Trip trip, NoteSection section) {
        requireManagedTrip(trip);
        Objects.requireNonNull(section, "Note section cannot be null.");

        if (!trip.getNoteSections().contains(section)) {
            throw new IllegalArgumentException(
                    "Note section does not belong to this trip.");
        }
    }
}
