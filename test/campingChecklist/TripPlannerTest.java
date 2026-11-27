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
 *
 * @author Colby Almond
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
        PackingItem tent = createItem();
        NoteSection notes = new NoteSection("Campsite", "Site 42");
        planner.addPackingItem(trip, tent);
        planner.addNoteSection(trip, notes);

        LocalDate newStart = LocalDate.of(2026, 12, 4);
        LocalDate newEnd = LocalDate.of(2026, 12, 6);

        planner.editTrip(
                trip, "Moab Overnight", "Moab", newStart, newEnd);

        assertEquals("Moab Overnight", trip.getName());
        assertEquals("Moab", trip.getDestination());
        assertEquals(newStart, trip.getStartDate());
        assertEquals(newEnd, trip.getEndDate());
        assertSame(tent, trip.getPackingItems().get(0));
        assertSame(notes, trip.getNoteSections().get(0));
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
                                trip, "Updated", "Moab", null, null))
        );
    }

    @Test
    void editTripRejectsInvalidValuesWithoutChangingTrip() {
        Trip trip = addManagedTrip();
        LocalDate originalStart = trip.getStartDate();
        LocalDate originalEnd = trip.getEndDate();

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editTrip(
                                trip, " ", "Moab", null, null)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editTrip(
                                trip, "Updated", " ", null, null)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editTrip(
                                trip, "Updated", "Moab",
                                LocalDate.of(2026, 12, 6),
                                LocalDate.of(2026, 12, 4)))
        );

        assertEquals("Zion Weekend", trip.getName());
        assertEquals("Zion", trip.getDestination());
        assertEquals(originalStart, trip.getStartDate());
        assertEquals(originalEnd, trip.getEndDate());
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

        // Tent x1 packed, water x3 unpacked: 1 of 4 items.
        assertTrue(tent.isPacked());
        assertEquals(
                25.0, planner.calculateProgress(trip), 0.0001);

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
                trip.calculateProgress(),
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

    @Test
    void noteSectionsCanBeAddedEditedAndRemoved() {
        Trip trip = addManagedTrip();
        NoteSection campsite = new NoteSection("Campsite", "");
        NoteSection meals = new NoteSection("Meals", "Tacos");

        planner.addNoteSection(trip, campsite);
        planner.addNoteSection(trip, meals);
        planner.editNoteSection(
                trip, campsite, "  Campsite 42  ", "Near the river");

        assertEquals("Campsite 42", campsite.getTitle());
        assertEquals("Near the river", campsite.getContent());

        planner.removeNoteSection(trip, meals);

        assertEquals(1, trip.getNoteSections().size());
        assertSame(campsite, trip.getNoteSections().get(0));
    }

    @Test
    void addNoteSectionRejectsDuplicateObject() {
        Trip trip = addManagedTrip();
        NoteSection section = new NoteSection("Campsite", "");
        planner.addNoteSection(trip, section);

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.addNoteSection(trip, section));

        assertEquals(1, trip.getNoteSections().size());
    }

    @Test
    void editNoteSectionRejectsBlankTitleWithoutChangingSection() {
        Trip trip = addManagedTrip();
        NoteSection section = new NoteSection("Campsite", "Site 42");
        planner.addNoteSection(trip, section);

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.editNoteSection(trip, section, " ", "New"));

        assertEquals("Campsite", section.getTitle());
        assertEquals("Site 42", section.getContent());
    }

    @Test
    void noteChangesRejectSectionFromAnotherTripOrUnmanagedTrip() {
        Trip zion = addManagedTrip();
        Trip moab = createTrip("Moab Overnight", "Moab");
        planner.addTrip(moab);
        Trip unmanaged = createTrip("Other Trip", "Other");

        NoteSection section = new NoteSection("Campsite", "Site 42");
        planner.addNoteSection(zion, section);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.editNoteSection(
                                moab, section, "Moved", "")),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.removeNoteSection(moab, section)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> planner.addNoteSection(
                                unmanaged, new NoteSection("New", "")))
        );

        assertEquals(1, zion.getNoteSections().size());
        assertEquals("Campsite", section.getTitle());
    }

    @Test
    void saveAndLoadPreservesDatesAndNotes() {
        Trip trip = addManagedTrip();
        planner.addNoteSection(
                trip, new NoteSection("Campsite", "Site 42"));
        planner.saveTrips();

        TripPlanner restored = new TripPlanner(file.toString());
        restored.loadTrips();

        Trip loadedTrip = restored.getTrips().get(0);

        assertEquals(LocalDate.of(2026, 11, 20), loadedTrip.getStartDate());
        assertEquals(LocalDate.of(2026, 11, 22), loadedTrip.getEndDate());
        assertEquals(
                "Site 42",
                loadedTrip.getNoteSections().get(0).getContent());
    }

    @Test
    void newPlannerHasNoUnsavedChanges() {
        assertFalse(planner.hasUnsavedChanges());
    }

    @Test
    void everyKindOfEditMarksUnsavedAndSaveClearsIt() {
        Trip trip = addManagedTrip();
        PackingItem item = createItem();
        NoteSection section = new NoteSection("Campsite", "");

        Runnable[] edits = {
                () -> planner.editTrip(trip, "New", "Moab", null, null),
                () -> planner.editTrip(trip),
                () -> planner.addPackingItem(trip, item),
                () -> planner.editPackingItem(trip, item, "Tarp", "Gear", 2),
                () -> planner.setItemPacked(trip, item, true),
                () -> planner.removePackingItem(trip, item),
                () -> planner.addNoteSection(trip, section),
                () -> planner.editNoteSection(trip, section, "Site", "42"),
                () -> planner.removeNoteSection(trip, section),
                () -> planner.removeTrip(trip)
        };

        for (int i = 0; i < edits.length; i++) {
            planner.saveTrips();
            assertFalse(planner.hasUnsavedChanges());

            edits[i].run();

            assertTrue(planner.hasUnsavedChanges(), "edit #" + i);
        }
    }

    @Test
    void rejectedOrNoOpEditsDoNotMarkUnsaved() {
        Trip trip = addManagedTrip();
        planner.saveTrips();

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.editTrip(trip, " ", "Moab", null, null));
        planner.deleteTrip(createTrip("Other Trip", "Other"));

        assertFalse(planner.hasUnsavedChanges());
    }

    @Test
    void loadClearsUnsavedChangesButFailedLoadKeepsThem() throws Exception {
        addManagedTrip();
        planner.saveTrips();
        planner.addTrip(createTrip("Moab Overnight", "Moab"));

        planner.loadTrips();
        assertFalse(planner.hasUnsavedChanges());

        planner.addTrip(createTrip("Moab Overnight", "Moab"));
        Files.write(file, new byte[] {1, 2, 3});

        assertThrows(
                UncheckedIOException.class,
                () -> planner.loadTrips());
        assertTrue(planner.hasUnsavedChanges());
    }

    @Test
    void failedSaveKeepsUnsavedChanges() {
        TripPlanner badPlanner = new TripPlanner(
                tempDirectory.resolve("missing").resolve("trips.dat").toString());
        badPlanner.addTrip(createTrip("Zion Weekend", "Zion"));

        assertThrows(
                UncheckedIOException.class,
                () -> badPlanner.saveTrips());
        assertTrue(badPlanner.hasUnsavedChanges());
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
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 22));
    }

    private PackingItem createItem() {
        return new PackingItem("Tent", "Shelter", 1);
    }
}