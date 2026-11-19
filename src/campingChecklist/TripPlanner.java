package campingChecklist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Manages camping trips and coordinates file storage.
 */
public class TripPlanner {

    private ArrayList<Trip> trips;
    private final TripFileManager fileManager;

    public TripPlanner() {
        this("trips.dat");
    }

    public TripPlanner(String fileName) {
        trips = new ArrayList<>();
        fileManager = new TripFileManager(fileName);
    }

    /**
     * Returns a copy of the list containing the current Trip objects.
     */
    public ArrayList<Trip> getTrips() {
        return new ArrayList<>(trips);
    }

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
     * The caller can edit that trip through its setters.
     */
    public void editTrip(Trip trip) {
        requireManagedTrip(trip);
    }

    /**
     * Updates a managed trip after validating all new values.
     */
    public void editTrip(
            Trip trip,
            String name,
            String location,
            LocalDate date) {

        requireManagedTrip(trip);

        Trip validated = new Trip(name, location, date);

        trip.setName(validated.getName());
        trip.setLocation(validated.getLocation());
        trip.setDate(validated.getDate());
    }

    public void deleteTrip(Trip trip) {
        Objects.requireNonNull(trip, "Trip cannot be null.");
        trips.remove(trip);
    }

    /**
     * Alternate name used by the GUI and GUI tests.
     */
    public void removeTrip(Trip trip) {
        deleteTrip(trip);
    }

    /**
     * Returns the trip's packing percentage.
     */
    public double calculateProgress(Trip trip) {
        requireManagedTrip(trip);
        return trip.getPackingProgress();
    }

    public void saveTrips() {
        fileManager.saveTrips(trips);
    }

    /**
     * Replaces current trips only after loading succeeds.
     */
    public void loadTrips() {
        ArrayList<Trip> loadedTrips = fileManager.loadTrips();
        trips = loadedTrips;
    }

    private void requireManagedTrip(Trip trip) {
        Objects.requireNonNull(trip, "Trip cannot be null.");

        if (!trips.contains(trip)) {
            throw new IllegalArgumentException(
                    "Trip does not belong to this planner.");
        }
    }
}