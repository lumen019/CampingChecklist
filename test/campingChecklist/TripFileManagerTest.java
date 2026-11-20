package campingChecklist;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests file persistence and rejection of invalid saved data.
 */
class TripFileManagerTest {

    @TempDir
    Path tempDirectory;

    private Path file;
    private TripFileManager fileManager;

    @BeforeEach
    void setUp() {
        file = tempDirectory.resolve("trips.dat");
        fileManager = new TripFileManager(file.toString());
    }

    @Test
    void constructorRejectsNullOrBlankFileName() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new TripFileManager(null)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new TripFileManager("   "))
        );
    }

    @Test
    void saveTripsRejectsNullList() {
        assertThrows(
                NullPointerException.class,
                () -> fileManager.saveTrips(null));
    }

    @Test
    void missingFileReturnsEmptyList() {
        assertTrue(fileManager.loadTrips().isEmpty());
    }

    @Test
    void emptyListCanBeSavedAndLoaded() {
        fileManager.saveTrips(new ArrayList<>());

        assertTrue(Files.exists(file));
        assertTrue(fileManager.loadTrips().isEmpty());
    }

    @Test
    void tripAndItemDetailsArePreserved() {
        Trip trip = createTrip(
                "Zion Weekend",
                "Zion National Park",
                LocalDate.of(2026, 11, 20));

        PackingItem tent =
                new PackingItem("Tent", "Shelter", 1);
        tent.setPacked(true);

        PackingItem water =
                new PackingItem("Water bottle", "Hydration", 3);

        trip.addPackingItem(tent);
        trip.addPackingItem(water);

        ArrayList<Trip> trips = new ArrayList<>();
        trips.add(trip);

        fileManager.saveTrips(trips);
        ArrayList<Trip> restored = fileManager.loadTrips();

        assertEquals(1, restored.size());

        Trip loadedTrip = restored.get(0);

        assertEquals("Zion Weekend", loadedTrip.getName());
        assertEquals(
                "Zion National Park", loadedTrip.getLocation());
        assertEquals(
                LocalDate.of(2026, 11, 20), loadedTrip.getDate());

        assertEquals(2, loadedTrip.getPackingItems().size());

        PackingItem loadedTent =
                loadedTrip.getPackingItems().get(0);

        assertEquals("Tent", loadedTent.getName());
        assertEquals("Shelter", loadedTent.getCategory());
        assertEquals(1, loadedTent.getQuantity());
        assertTrue(loadedTent.isPacked());

        PackingItem loadedWater =
                loadedTrip.getPackingItems().get(1);

        assertEquals("Water bottle", loadedWater.getName());
        assertEquals("Hydration", loadedWater.getCategory());
        assertEquals(3, loadedWater.getQuantity());
        assertFalse(loadedWater.isPacked());
    }

    @Test
    void multipleTripsKeepTheirOwnItems() {
        Trip zion = createTrip("Zion Weekend", "Zion", null);
        zion.addPackingItem(
                new PackingItem("Tent", "Shelter", 1));

        Trip moab = createTrip("Moab Overnight", "Moab", null);
        moab.addPackingItem(
                new PackingItem("Headlamp", "Equipment", 2));

        ArrayList<Trip> trips = new ArrayList<>();
        trips.add(zion);
        trips.add(moab);

        fileManager.saveTrips(trips);
        ArrayList<Trip> restored = fileManager.loadTrips();

        assertEquals(2, restored.size());
        assertEquals("Zion Weekend", restored.get(0).getName());
        assertEquals("Moab Overnight", restored.get(1).getName());

        assertEquals(
                "Tent",
                restored.get(0).getPackingItems().get(0).getName());
        assertEquals(
                "Headlamp",
                restored.get(1).getPackingItems().get(0).getName());
    }

    @Test
    void optionalDateIsPreserved() {
        ArrayList<Trip> trips = new ArrayList<>();
        trips.add(createTrip("Undated Trip", "Moab", null));

        fileManager.saveTrips(trips);

        assertNull(fileManager.loadTrips().get(0).getDate());
    }

    @Test
    void savingAgainReplacesPreviousContents() {
        ArrayList<Trip> trips = new ArrayList<>();
        trips.add(createTrip("First Trip", "Zion", null));

        fileManager.saveTrips(trips);
        fileManager.saveTrips(new ArrayList<>());

        assertTrue(fileManager.loadTrips().isEmpty());
    }

    @Test
    void malformedFileThrowsException() throws Exception {
        Files.write(file, new byte[] {1, 2, 3});

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void unsupportedHeaderThrowsException() throws Exception {
        try (DataOutputStream output = openOutput()) {
            output.writeUTF("UNSUPPORTED_FORMAT");
            output.writeInt(0);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void negativeTripCountThrowsException() throws Exception {
        try (DataOutputStream output = openOutput()) {
            output.writeUTF("CAMPING_CHECKLIST_V1");
            output.writeInt(-1);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void negativeItemCountThrowsException() throws Exception {
        try (DataOutputStream output = openOutput()) {
            writeSingleTripPrefix(output);
            output.writeBoolean(false);
            output.writeInt(-1);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void invalidDateThrowsException() throws Exception {
        try (DataOutputStream output = openOutput()) {
            writeSingleTripPrefix(output);
            output.writeBoolean(true);
            output.writeUTF("not-a-date");
            output.writeInt(0);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void nonpositiveItemQuantityThrowsException() throws Exception {
        try (DataOutputStream output = openOutput()) {
            writeSingleTripPrefix(output);
            output.writeBoolean(false);
            output.writeInt(1);
            output.writeUTF("Tent");
            output.writeUTF("Shelter");
            output.writeInt(0);
            output.writeBoolean(false);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void blankTripNameThrowsException() throws Exception {
        try (DataOutputStream output = openOutput()) {
            output.writeUTF("CAMPING_CHECKLIST_V1");
            output.writeInt(1);
            output.writeUTF(" ");
            output.writeUTF("Zion");
            output.writeBoolean(false);
            output.writeInt(0);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void blankItemCategoryThrowsException() throws Exception {
        try (DataOutputStream output = openOutput()) {
            writeSingleTripPrefix(output);
            output.writeBoolean(false);
            output.writeInt(1);
            output.writeUTF("Tent");
            output.writeUTF(" ");
            output.writeInt(1);
            output.writeBoolean(false);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void trailingDataThrowsException() throws Exception {
        fileManager.saveTrips(new ArrayList<>());

        Files.write(
                file,
                new byte[] {42},
                StandardOpenOption.APPEND);

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void truncatedFileThrowsException() throws Exception {
        ArrayList<Trip> trips = new ArrayList<>();
        trips.add(createTrip("Zion Weekend", "Zion", null));
        fileManager.saveTrips(trips);

        byte[] bytes = Files.readAllBytes(file);
        byte[] truncated = java.util.Arrays.copyOf(
                bytes, bytes.length - 1);

        Files.write(file, truncated);

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void savingToMissingDirectoryThrowsException() {
        TripFileManager invalidManager =
                new TripFileManager(
                        tempDirectory.resolve("missing")
                                .resolve("trips.dat")
                                .toString());

        assertThrows(
                UncheckedIOException.class,
                () -> invalidManager.saveTrips(new ArrayList<>()));
    }

    private Trip createTrip(
            String name,
            String destination,
            LocalDate date) {

        return new Trip(name, destination, date);
    }

    private DataOutputStream openOutput() throws Exception {
        return new DataOutputStream(Files.newOutputStream(file));
    }

    /**
     * Writes the header, trip count, name, and location.
     * Each test writes the remaining fields needed for its scenario.
     */
    private void writeSingleTripPrefix(
            DataOutputStream output) throws Exception {

        output.writeUTF("CAMPING_CHECKLIST_V1");
        output.writeInt(1);
        output.writeUTF("Zion Weekend");
        output.writeUTF("Zion");
    }
}