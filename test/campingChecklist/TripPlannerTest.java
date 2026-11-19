package campingChecklist;

import static org.junit.jupiter.api.Assertions.*;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TripPlannerTest {

    @TempDir
    Path tempDirectory;

    private Path file;
    private TripPlanner planner;

    @BeforeEach
    void setUp() {
        file = tempDirectory.resolve("trips.dat");
        planner = new TripPlanner(file.toString());
    }

    @Test
    void newPlannerStartsEmpty() {
        assertTrue(planner.getTrips().isEmpty());
    }

    @Test
    void addTripStoresTrip() {
        Trip trip = createTrip("Zion Weekend", "Zion");

        planner.addTrip(trip);

        assertEquals(1, planner.getTrips().size());
        assertSame(trip, planner.getTrips().get(0));
    }

    @Test
    void addTripRejectsNull() {
        assertThrows(
                NullPointerException.class,
                () -> planner.addTrip(null));
    }

    @Test
    void addTripRejectsDuplicateObject() {
        Trip trip = createTrip("Zion Weekend", "Zion");
        planner.addTrip(trip);

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.addTrip(trip));
    }

    @Test
    void getTripsReturnsDefensiveListCopy() {
        planner.addTrip(createTrip("Zion Weekend", "Zion"));

        ArrayList<Trip> returnedTrips = planner.getTrips();
        returnedTrips.clear();

        assertEquals(1, planner.getTrips().size());
    }

    @Test
    void deleteTripRemovesOnlySelectedTrip() {
        Trip zion = createTrip("Zion Weekend", "Zion");
        Trip moab = createTrip("Moab Overnight", "Moab");

        planner.addTrip(zion);
        planner.addTrip(moab);
        planner.deleteTrip(zion);

        assertEquals(1, planner.getTrips().size());
        assertSame(moab, planner.getTrips().get(0));
    }

    @Test
    void deletingUnknownTripLeavesListUnchanged() {
        Trip existing = createTrip("Zion Weekend", "Zion");
        planner.addTrip(existing);

        planner.deleteTrip(
                createTrip("Other Trip", "Other"));

        assertEquals(1, planner.getTrips().size());
        assertSame(existing, planner.getTrips().get(0));
    }

    @Test
    void editTripUpdatesDetails() {
        Trip trip = createTrip("Zion Weekend", "Zion");
        planner.addTrip(trip);

        LocalDate newDate = LocalDate.of(2026, 11, 27);

        planner.editTrip(
                trip, "Moab Overnight", "Moab", newDate);

        assertEquals("Moab Overnight", trip.getName());
        assertEquals("Moab", trip.getLocation());
        assertEquals(newDate, trip.getDate());
    }

    @Test
    void editTripRejectsUnmanagedTrip() {
        Trip trip = createTrip("Other Trip", "Other");

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.editTrip(trip));
    }

    @Test
    void originalEditMethodAcceptsManagedTrip() {
        Trip trip = createTrip("Zion Weekend", "Zion");
        planner.addTrip(trip);

        trip.setName("Updated Name");
        planner.editTrip(trip);

        assertEquals(
                "Updated Name",
                planner.getTrips().get(0).getName());
    }

    @Test
    void calculateProgressDelegatesToTrip() {
        Trip trip = createTrip("Zion Weekend", "Zion");

        PackingItem tent =
                new PackingItem("Tent", "Shelter", 1);
        tent.setPacked(true);

        trip.addPackingItem(tent);
        trip.addPackingItem(
                new PackingItem("Sleeping bag", "Bedding", 2));

        planner.addTrip(trip);

        assertEquals(
                trip.getPackingProgress(),
                planner.calculateProgress(trip),
                0.0001);
    }

    @Test
    void saveAndLoadRestoresTrips() {
        planner.addTrip(createTrip("Zion Weekend", "Zion"));
        planner.saveTrips();

        TripPlanner restored =
                new TripPlanner(file.toString());
        restored.loadTrips();

        assertEquals(1, restored.getTrips().size());
        assertEquals(
                "Zion Weekend",
                restored.getTrips().get(0).getName());
    }

    @Test
    void failedLoadPreservesCurrentTrips() throws Exception {
        Trip trip = createTrip("Zion Weekend", "Zion");
        planner.addTrip(trip);

        Files.write(file, new byte[] {1, 2, 3});

        assertThrows(
                UncheckedIOException.class,
                () -> planner.loadTrips());

        assertEquals(1, planner.getTrips().size());
        assertSame(trip, planner.getTrips().get(0));
    }

    private Trip createTrip(String name, String destination) {
        Trip trip = new Trip(
                destination,
                LocalDate.of(2026, 11, 20));

        trip.setName(name);
        return trip;
    }
}