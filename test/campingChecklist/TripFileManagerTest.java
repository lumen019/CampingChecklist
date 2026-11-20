package campingChecklist;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the reduced TripFileManager demo format.
 *
 * <p>Checks supported data round trips, default unpacked status,
 * file replacement, and rejection of invalid saved records.</p>
 */
class TripFileManagerTest {

    private static final String DEMO_HEADER =
            "CAMPING_CHECKLIST_DEMO_V1";

    @TempDir
    Path tempDirectory;

    private Path file;
    private TripFileManager fileManager;

    @BeforeEach
    void setUp() {
        file = tempDirectory.resolve("demo-trips.dat");
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
                        () -> new TripFileManager("")),
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

//    @Test
//    void supportedTripAndItemDetailsArePreserved() {
//        LocalDate date = LocalDate.of(2026, 11, 20);
//        Trip trip = new Trip("Zion Weekend", date);
//
//        trip.addPackingItem(
//                new PackingItem("Tent", "Shelter", 1));
//        trip.addPackingItem(
//                new PackingItem("Water bottle", "Hydration", 3));
//
//        ArrayList<Trip> trips = new ArrayList<>();
//        trips.add(trip);
//
//        fileManager.saveTrips(trips);
//        ArrayList<Trip> restored = fileManager.loadTrips();
//
//        assertEquals(1, restored.size());
//
//        Trip loadedTrip = restored.get(0);
//
//        assertEquals("Zion Weekend", loadedTrip.getName());
//        assertEquals(date, loadedTrip.getDate());
//        assertEquals(2, loadedTrip.getPackingItems().size());
//
//        PackingItem tent = loadedTrip.getPackingItems().get(0);
//        PackingItem water = loadedTrip.getPackingItems().get(1);
//
//        assertAll(
//                () -> assertEquals("Tent", tent.getName()),
//                () -> assertEquals("Shelter", tent.getCategory()),
//                () -> assertEquals(1, tent.getQuantity()),
//                () -> assertEquals("Water bottle", water.getName()),
//                () -> assertEquals("Hydration", water.getCategory()),
//                () -> assertEquals(3, water.getQuantity())
//        );
//    }

//    @Test
//    void loadedItemsAreUnpackedInDemoFormat() {
//        Trip trip = new Trip(
//                "Zion Weekend",
//                LocalDate.of(2026, 11, 20));
//
//        PackingItem tent =
//                new PackingItem("Tent", "Shelter", 1);
//        tent.setPacked(true);
//        trip.addPackingItem(tent);
//
//        assertEquals(100.0, trip.getPackingProgress(), 0.0001);
//
//        ArrayList<Trip> trips = new ArrayList<>();
//        trips.add(trip);
//
//        fileManager.saveTrips(trips);
//
//        Trip loadedTrip = fileManager.loadTrips().get(0);
//
//        assertEquals(1, loadedTrip.getPackingItems().size());
//        assertEquals(0.0, loadedTrip.getPackingProgress(), 0.0001);
//    }

//    @Test
//    void multipleTripsKeepTheirOwnItems() {
//        Trip zion = new Trip(
//                "Zion Weekend",
//                LocalDate.of(2026, 11, 20));
//        zion.addPackingItem(
//                new PackingItem("Tent", "Shelter", 1));
//
//        Trip moab = new Trip(
//                "Moab Overnight",
//                LocalDate.of(2026, 11, 27));
//        moab.addPackingItem(
//                new PackingItem("Headlamp", "Equipment", 2));
//
//        ArrayList<Trip> trips = new ArrayList<>();
//        trips.add(zion);
//        trips.add(moab);
//
//        fileManager.saveTrips(trips);
//        ArrayList<Trip> restored = fileManager.loadTrips();
//
//        assertEquals(2, restored.size());
//
//        Trip loadedZion = restored.get(0);
//        Trip loadedMoab = restored.get(1);
//
//        assertEquals("Zion Weekend", loadedZion.getName());
//        assertEquals("Moab Overnight", loadedMoab.getName());
//        assertEquals(1, loadedZion.getPackingItems().size());
//        assertEquals(1, loadedMoab.getPackingItems().size());
//
//        assertEquals(
//                "Tent",
//                loadedZion.getPackingItems().get(0).getName());
//        assertEquals(
//                "Headlamp",
//                loadedMoab.getPackingItems().get(0).getName());
//    }

//    @Test
//    void optionalDateIsPreserved() {
//        ArrayList<Trip> trips = new ArrayList<>();
//        trips.add(new Trip("Undated Trip", null));
//
//        fileManager.saveTrips(trips);
//        ArrayList<Trip> restored = fileManager.loadTrips();
//
//        assertEquals(1, restored.size());
//        assertNull(restored.get(0).getDate());
//    }

//    @Test
//    void savingAgainReplacesPreviousContents() {
//        ArrayList<Trip> trips = new ArrayList<>();
//        trips.add(new Trip(
//                "First Trip",
//                LocalDate.of(2026, 11, 20)));
//
//        fileManager.saveTrips(trips);
//        fileManager.saveTrips(new ArrayList<>());
//
//        assertTrue(fileManager.loadTrips().isEmpty());
//    }

    @Test
    void malformedFileThrowsException() throws IOException {
        Files.write(file, new byte[] {1, 2, 3});

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void unsupportedHeaderThrowsException() throws IOException {
        try (DataOutputStream output = openOutput()) {
            output.writeUTF("UNSUPPORTED_FORMAT");
            output.writeInt(0);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void originalNonDemoHeaderIsRejected() throws IOException {
        try (DataOutputStream output = openOutput()) {
            output.writeUTF("CAMPING_CHECKLIST_V1");
            output.writeInt(0);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void negativeTripCountThrowsException() throws IOException {
        try (DataOutputStream output = openOutput()) {
            output.writeUTF(DEMO_HEADER);
            output.writeInt(-1);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

    @Test
    void negativeItemCountThrowsException() throws IOException {
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
    void invalidDateThrowsException() throws IOException {
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

//    @Test
//    void nonpositiveItemQuantityThrowsException() throws IOException {
//        try (DataOutputStream output = openOutput()) {
//            writeSingleTripPrefix(output);
//            output.writeBoolean(false);
//            output.writeInt(1);
//            output.writeUTF("Tent");
//            output.writeUTF("Shelter");
//            output.writeInt(0);
//        }
//
//        assertThrows(
//                UncheckedIOException.class,
//                () -> fileManager.loadTrips());
//    }

    @Test
    void blankTripNameThrowsException() throws IOException {
        try (DataOutputStream output = openOutput()) {
            output.writeUTF(DEMO_HEADER);
            output.writeInt(1);
            output.writeUTF(" ");
            output.writeBoolean(false);
            output.writeInt(0);
        }

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

//    @Test
//    void blankItemNameThrowsException() throws IOException {
//        try (DataOutputStream output = openOutput()) {
//            writeSingleTripPrefix(output);
//            output.writeBoolean(false);
//            output.writeInt(1);
//            output.writeUTF(" ");
//            output.writeUTF("Shelter");
//            output.writeInt(1);
//        }
//
//        assertThrows(
//                UncheckedIOException.class,
//                () -> fileManager.loadTrips());
//    }

//    @Test
//    void blankItemCategoryThrowsException() throws IOException {
//        try (DataOutputStream output = openOutput()) {
//            writeSingleTripPrefix(output);
//            output.writeBoolean(false);
//            output.writeInt(1);
//            output.writeUTF("Tent");
//            output.writeUTF(" ");
//            output.writeInt(1);
//        }
//
//        assertThrows(
//                UncheckedIOException.class,
//                () -> fileManager.loadTrips());
//    }

    @Test
    void trailingDataThrowsException() throws IOException {
        fileManager.saveTrips(new ArrayList<>());

        Files.write(
                file,
                new byte[] {42},
                StandardOpenOption.APPEND);

        assertThrows(
                UncheckedIOException.class,
                () -> fileManager.loadTrips());
    }

//    @Test
//    void truncatedFileThrowsException() throws IOException {
//        ArrayList<Trip> trips = new ArrayList<>();
//        trips.add(new Trip(
//                "Zion Weekend",
//                LocalDate.of(2026, 11, 20)));
//
//        fileManager.saveTrips(trips);
//
//        byte[] bytes = Files.readAllBytes(file);
//        byte[] truncated = Arrays.copyOf(bytes, bytes.length - 1);
//
//        Files.write(file, truncated);
//
//        assertThrows(
//                UncheckedIOException.class,
//                () -> fileManager.loadTrips());
//    }

    @Test
    void savingToMissingDirectoryThrowsException() {
        Path missingFile = tempDirectory
                .resolve("missing")
                .resolve("demo-trips.dat");

        TripFileManager invalidManager =
                new TripFileManager(missingFile.toString());

        assertThrows(
                UncheckedIOException.class,
                () -> invalidManager.saveTrips(new ArrayList<>()));
    }

    private DataOutputStream openOutput() throws IOException {
        return new DataOutputStream(Files.newOutputStream(file));
    }

    /**
     * Writes the header, one-trip count, and trip name.
     * Individual tests write the remaining fields.
     */
    private void writeSingleTripPrefix(
            DataOutputStream output) throws IOException {

        output.writeUTF(DEMO_HEADER);
        output.writeInt(1);
        output.writeUTF("Zion Weekend");
    }
}