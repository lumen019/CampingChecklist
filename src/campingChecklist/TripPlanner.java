package campingChecklist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Manages trips and packing items for the current demo.
 *
 * <p>Supports trip name/date editing, packing-item addition and removal,
 * packed-status updates, and packing-progress calculation.</p>
 *
 * <p>Item-detail editing is unfinished. Changes remain in memory until
 * saveTrips() is called.</p>
 */
public class TripPlanner {

    private ArrayList<Trip> trips;
    private final TripFileManager fileManager;

    /**
     * Creates an empty planner using trips.dat for storage.
     */
    public TripPlanner() {
        this("trips.dat");
    }

    /**
     * Creates an empty planner using the specified storage file.
     *
     * <p>Saved trips are not loaded automatically.</p>
     *
     * @param fileName the storage file path
     * @throws IllegalArgumentException if fileName is null or blank
     */
    public TripPlanner(String fileName) {
        trips = new ArrayList<>();
        fileManager = new TripFileManager(fileName);
    }

    /**
     * Returns a copy of the list containing the current trip objects.
     *
     * <p>Changing the returned list does not change this planner's list.
     * The trip objects themselves remain editable.</p>
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
    }

    /**
     * Verifies that a trip belongs to this planner.
     *
     * <p>This overload does not modify the trip. Callers may change
     * the managed trip through its available setters.</p>
     *
     * @param trip the trip to verify
     * @throws NullPointerException if trip is null
     * @throws IllegalArgumentException if the trip is not managed
     */
    public void editTrip(Trip trip) {
        requireManagedTrip(trip);
    }

    /**
     * Updates a managed trip's name and date.
     *
     * <p>The location parameter is retained for compatibility with
     * existing callers but is not validated or stored in this demo.</p>
     *
     * @param trip the managed trip to edit
     * @param name the new nonblank trip name
     * @param location unused in the demo version
     * @param date the new trip date
     * @throws NullPointerException if trip is null
     * @throws IllegalArgumentException if the trip is not managed
     *         or the new values are rejected by the Trip constructor
     */
    public void editTrip(
            Trip trip,
            String name,
            String location,
            LocalDate date) {

        requireManagedTrip(trip);

        Trip validated = new Trip(name, date);

        trip.setName(validated.getName());
        trip.setDate(validated.getDate());
    }

    /**
     * Removes a trip if it belongs to this planner.
     *
     * <p>An unknown trip leaves the list unchanged.</p>
     *
     * @param trip the trip to remove
     * @throws NullPointerException if trip is null
     */
    public void deleteTrip(Trip trip) {
        Objects.requireNonNull(trip, "Trip cannot be null.");
        trips.remove(trip);
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
     * <p>The same item object cannot be added to the selected trip
     * twice. Separate objects with identical details are allowed.</p>
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
    }

    /**
     * Validates proposed item details without applying them.
     *
     * <p>This is an unfinished demo operation. Valid input leaves the
     * existing item unchanged. GUI callers should not present it as
     * a completed edit operation.</p>
     *
     * @param trip the managed trip containing the item
     * @param item the item whose proposed changes are being checked
     * @param name the proposed nonblank item name
     * @param category the proposed nonblank category
     * @param quantity the proposed quantity, at least 1
     * @throws NullPointerException if trip or item is null
     * @throws IllegalArgumentException if membership validation fails
     *         or the proposed item values are invalid
     */
    public void editPackingItem(
            Trip trip,
            PackingItem item,
            String name,
            String category,
            int quantity) {

        requireManagedItem(trip, item);

        // Validate input while item-detail editing is unfinished.
        new PackingItem(name, category, quantity);

        // TODO: Apply validated details when item editing is implemented.
    }

    /**
     * Marks an item as packed or unpacked in memory.
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
    }

    /**
     * Returns packing progress calculated by the managed trip.
     *
     * @param trip the managed trip to inspect
     * @return packing progress as a percentage
     * @throws NullPointerException if trip is null
     * @throws IllegalArgumentException if the trip is not managed
     */
    public double calculateProgress(Trip trip) {
        requireManagedTrip(trip);
        return trip.getPackingProgress();
    }

    /**
     * Saves current trips through the configured file manager.
     *
     * <p>The fields preserved depend on the current file-manager format.</p>
     *
     * @throws java.io.UncheckedIOException if saving fails
     */
    public void saveTrips() {
        fileManager.saveTrips(trips);
    }

    /**
     * Replaces current trips after loading succeeds.
     *
     * <p>A missing file produces an empty list. If loading throws an
     * exception, the existing trips remain unchanged.</p>
     *
     * @throws java.io.UncheckedIOException if loading fails
     */
    public void loadTrips() {
        ArrayList<Trip> loadedTrips = fileManager.loadTrips();
        trips = loadedTrips;
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
}