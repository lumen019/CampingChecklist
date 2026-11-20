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

/**
 * Tests trip management, item management, and planner persistence.
 */
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
        Trip trip = addManagedTrip();

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.addTrip(trip));

        assertEquals(1, planner.getTrips().size());
    }

    @Test
    void getTripsReturnsDefensiveListCopy() {
        addManagedTrip();

        ArrayList<Trip> returnedTrips = planner.getTrips();
        returnedTrips.clear();

        assertEquals(1, planner.getTrips().size());
    }

    @Test
    void deleteTripRemovesOnlySelectedTrip() {
        Trip zion = addManagedTrip();
        Trip moab = createTrip("Moab Overnight", "Moab");
        planner.addTrip(moab);

        planner.deleteTrip(zion);

        assertEquals(1, planner.getTrips().size());
        assertSame(moab, planner.getTrips().get(0));
    }

    @Test
    void deletingUnknownTripLeavesListUnchanged() {
        Trip existing = addManagedTrip();

        planner.deleteTrip(createTrip("Other Trip", "Other"));

        assertEquals(1, planner.getTrips().size());
        assertSame(existing, planner.getTrips().get(0));
    }

    @Test
    void removeTripAliasRemovesTrip() {
        Trip trip = addManagedTrip();

        planner.removeTrip(trip);

        assertTrue(planner.getTrips().isEmpty());
    }

    @Test
    void deleteTripRejectsNull() {
        assertThrows(
                NullPointerException.class,
                () -> planner.deleteTrip(null));
    }

    @Test
    void editTripUpdatesDetails() {
        Trip trip = addManagedTrip();
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

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editTrip(trip)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editTrip(
                                trip, "Updated", "Moab", null))
        );
    }

    @Test
    void editTripRejectsInvalidValuesWithoutChangingTrip() {
        Trip trip = addManagedTrip();
        LocalDate originalDate = trip.getDate();

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editTrip(
                                trip, " ", "Moab", null)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editTrip(
                                trip, "Updated", " ", null))
        );

        assertEquals("Zion Weekend", trip.getName());
        assertEquals("Zion", trip.getLocation());
        assertEquals(originalDate, trip.getDate());
    }

    @Test
    void originalEditMethodAcceptsManagedTrip() {
        Trip trip = addManagedTrip();

        trip.setName("Updated Name");
        planner.editTrip(trip);

        assertEquals(
                "Updated Name",
                planner.getTrips().get(0).getName());
    }

    @Test
    void addPackingItemStoresItemInSelectedTrip() {
        Trip trip = addManagedTrip();
        PackingItem tent = createItem();

        planner.addPackingItem(trip, tent);

        assertEquals(1, trip.getPackingItems().size());
        assertSame(tent, trip.getPackingItems().get(0));
    }

    @Test
    void addPackingItemRejectsNullTripOrItem() {
        Trip trip = addManagedTrip();
        PackingItem tent = createItem();

        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> planner.addPackingItem(null, tent)),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> planner.addPackingItem(trip, null))
        );

        assertTrue(trip.getPackingItems().isEmpty());
    }

    @Test
    void addPackingItemRejectsUnmanagedTrip() {
        Trip trip = createTrip("Other Trip", "Other");
        PackingItem tent = createItem();

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.addPackingItem(trip, tent));

        assertTrue(trip.getPackingItems().isEmpty());
    }

    @Test
    void addPackingItemRejectsDuplicateObject() {
        Trip trip = addManagedTrip();
        PackingItem tent = createItem();
        planner.addPackingItem(trip, tent);

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.addPackingItem(trip, tent));

        assertEquals(1, trip.getPackingItems().size());
    }

    @Test
    void addPackingItemAllowsSeparateObjectsWithSameDetails() {
        Trip trip = addManagedTrip();

        planner.addPackingItem(trip, createItem());
        planner.addPackingItem(trip, createItem());

        assertEquals(2, trip.getPackingItems().size());
    }

    @Test
    void removePackingItemRemovesOnlySelectedItem() {
        Trip trip = addManagedTrip();
        PackingItem tent = createItem();
        PackingItem water =
                new PackingItem("Water", "Hydration", 3);

        planner.addPackingItem(trip, tent);
        planner.addPackingItem(trip, water);

        planner.removePackingItem(trip, tent);

        assertEquals(1, trip.getPackingItems().size());
        assertSame(water, trip.getPackingItems().get(0));
    }

    @Test
    void removePackingItemRejectsItemFromAnotherTrip() {
        Trip zion = addManagedTrip();
        Trip moab = createTrip("Moab Overnight", "Moab");
        planner.addTrip(moab);

        PackingItem tent = createItem();
        planner.addPackingItem(zion, tent);

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.removePackingItem(moab, tent));

        assertEquals(1, zion.getPackingItems().size());
        assertTrue(moab.getPackingItems().isEmpty());
    }

    @Test
    void editPackingItemUpdatesDetailsAndPreservesPackedStatus() {
        Trip trip = addManagedTrip();
        PackingItem item = createItem();
        planner.addPackingItem(trip, item);
        planner.setItemPacked(trip, item, true);

        planner.editPackingItem(
                trip, item, "  Tarp  ", "  Equipment  ", 2);

        assertSame(item, trip.getPackingItems().get(0));
        assertEquals("Tarp", item.getName());
        assertEquals("Equipment", item.getCategory());
        assertEquals(2, item.getQuantity());
        assertTrue(item.isPacked());
    }

    @Test
    void editPackingItemRejectsInvalidValuesWithoutChangingItem() {
        Trip trip = addManagedTrip();
        PackingItem item = createItem();
        planner.addPackingItem(trip, item);
        planner.setItemPacked(trip, item, true);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editPackingItem(
                                trip, item, " ", "Equipment", 2)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editPackingItem(
                                trip, item, "Tarp", " ", 2)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editPackingItem(
                                trip, item, "Tarp", "Equipment", 0))
        );

        assertEquals("Tent", item.getName());
        assertEquals("Shelter", item.getCategory());
        assertEquals(1, item.getQuantity());
        assertTrue(item.isPacked());
    }

    @Test
    void itemChangesRejectItemNotInSelectedTrip() {
        Trip trip = addManagedTrip();
        PackingItem unknown = createItem();

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.removePackingItem(trip, unknown)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editPackingItem(
                                trip, unknown, "Tarp", "Equipment", 2)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.setItemPacked(
                                trip, unknown, true))
        );

        assertTrue(trip.getPackingItems().isEmpty());
        assertEquals("Tent", unknown.getName());
        assertFalse(unknown.isPacked());
    }

    @Test
    void itemChangesRejectUnmanagedTrip() {
        Trip trip = createTrip("Other Trip", "Other");
        PackingItem item = createItem();
        trip.addPackingItem(item);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.removePackingItem(trip, item)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editPackingItem(
                                trip, item, "Tarp", "Equipment", 2)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.setItemPacked(trip, item, true))
        );

        assertEquals(1, trip.getPackingItems().size());
        assertEquals("Tent", item.getName());
        assertFalse(item.isPacked());
    }

    @Test
    void itemChangesRejectNullItem() {
        Trip trip = addManagedTrip();

        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> planner.removePackingItem(trip, null)),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> planner.editPackingItem(
                                trip, null, "Tarp", "Equipment", 2)),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> planner.setItemPacked(trip, null, true))
        );
    }

    @Test
    void itemChangesRejectNullTrip() {
        PackingItem item = createItem();

        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> planner.removePackingItem(null, item)),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> planner.editPackingItem(
                                null, item, "Tarp", "Equipment", 2)),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> planner.setItemPacked(null, item, true))
        );
    }

    @Test
    void setItemPackedUpdatesStatusAndProgress() {
        Trip trip = addManagedTrip();
        PackingItem tent = createItem();
        PackingItem water =
                new PackingItem("Water", "Hydration", 3);

        planner.addPackingItem(trip, tent);
        planner.addPackingItem(trip, water);

        planner.setItemPacked(trip, tent, true);

        assertTrue(tent.isPacked());
        assertEquals(
                50.0, planner.calculateProgress(trip), 0.0001);

        planner.setItemPacked(trip, tent, false);

        assertFalse(tent.isPacked());
        assertEquals(
                0.0, planner.calculateProgress(trip), 0.0001);
    }

    @Test
    void calculateProgressDelegatesToTrip() {
        Trip trip = addManagedTrip();
        PackingItem tent = createItem();
        tent.setPacked(true);

        trip.addPackingItem(tent);
        trip.addPackingItem(
                new PackingItem("Sleeping bag", "Bedding", 2));

        assertEquals(
                trip.getPackingProgress(),
                planner.calculateProgress(trip),
                0.0001);
    }

    @Test
    void calculateProgressRejectsUnmanagedTrip() {
        Trip trip = createTrip("Other Trip", "Other");

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.calculateProgress(trip));
    }

    @Test
    void saveAndLoadRestoresTrips() {
        addManagedTrip();
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
    void saveAndLoadPreservesPlannerItemChanges() {
        Trip trip = addManagedTrip();
        PackingItem item = createItem();
        PackingItem removed =
                new PackingItem("Water", "Hydration", 3);

        planner.addPackingItem(trip, item);
        planner.addPackingItem(trip, removed);
        planner.editPackingItem(
                trip, item, "Tarp", "Equipment", 2);
        planner.setItemPacked(trip, item, true);
        planner.removePackingItem(trip, removed);
        planner.saveTrips();

        TripPlanner restored =
                new TripPlanner(file.toString());
        restored.loadTrips();

        assertEquals(1, restored.getTrips().size());

        Trip loadedTrip = restored.getTrips().get(0);
        assertEquals(1, loadedTrip.getPackingItems().size());

        PackingItem loadedItem =
                loadedTrip.getPackingItems().get(0);

        assertEquals("Tarp", loadedItem.getName());
        assertEquals("Equipment", loadedItem.getCategory());
        assertEquals(2, loadedItem.getQuantity());
        assertTrue(loadedItem.isPacked());
    }

    @Test
    void successfulLoadReplacesCurrentTrips() {
        addManagedTrip();
        planner.saveTrips();

        planner.addTrip(createTrip("Moab Overnight", "Moab"));
        planner.loadTrips();

        assertEquals(1, planner.getTrips().size());
        assertEquals(
                "Zion Weekend", planner.getTrips().get(0).getName());
    }

    @Test
    void failedLoadPreservesCurrentTrips() throws Exception {
        Trip trip = addManagedTrip();
        Files.write(file, new byte[] {1, 2, 3});

        assertThrows(
                UncheckedIOException.class,
                () -> planner.loadTrips());

        assertEquals(1, planner.getTrips().size());
        assertSame(trip, planner.getTrips().get(0));
    }

    private Trip addManagedTrip() {
        Trip trip = createTrip("Zion Weekend", "Zion");
        planner.addTrip(trip);
        return trip;
    }

    private Trip createTrip(String name, String destination) {
        return new Trip(
                name,
                destination,
                LocalDate.of(2026, 11, 20));
    }

    private PackingItem createItem() {
        return new PackingItem("Tent", "Shelter", 1);
    }
}