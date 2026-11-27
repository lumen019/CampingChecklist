package campingChecklist;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Saves and loads trips using a versioned binary file format.
 *
 * <p>Each trip record includes its name, destination, optional start and
 * end dates, packing items, and note sections. Each item includes its
 * name, category, quantity, and packed status. Each note section
 * includes its title and content.</p>
 *
 * <p>Files written by the previous version (one optional date and
 * no notes) can still be loaded; their date becomes the start date.</p>
 *
 * @author Colby Almond
 */
public class TripFileManager {

    private static final String FILE_HEADER =
            "CAMPING_CHECKLIST_V2";

    private static final String V1_FILE_HEADER =
            "CAMPING_CHECKLIST_V1";

    private final String fileName;

    /**
     * Creates a file manager for the specified path.
     *
     * @param fileName the storage file path
     * @throws IllegalArgumentException if fileName is null or blank
     */
    public TripFileManager(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "File name is required.");
        }

        this.fileName = fileName;
    }

    /**
     * Writes trips with their items and notes, replacing existing
     * file contents.
     *
     * <p>The parent directory must already exist. This method writes
     * directly to the destination file; it does not perform an atomic
     * replacement or preserve the old file if writing fails.</p>
     *
     * @param trips the trips to save
     * @throws NullPointerException if trips is null
     * @throws UncheckedIOException if writing fails
     */
    public void saveTrips(ArrayList<Trip> trips) {
        Objects.requireNonNull(trips, "Trips cannot be null.");

        Path path = Paths.get(fileName);

        try (DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(
                        Files.newOutputStream(path)))) {

            output.writeUTF(FILE_HEADER);
            output.writeInt(trips.size());

            for (Trip trip : trips) {
                writeTrip(output, trip);
            }

        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to save trips to " + fileName,
                    exception);
        }
    }

    /**
     * Loads all trips with their packing items and notes.
     *
     * @return the restored trips, or an empty list if the file is missing
     * @throws UncheckedIOException if reading fails or the file
     *         contains an unsupported or invalid record
     */
    public ArrayList<Trip> loadTrips() {
        Path path = Paths.get(fileName);

        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(
                        Files.newInputStream(path)))) {

            String header = input.readUTF();
            boolean version1 = V1_FILE_HEADER.equals(header);

            if (!version1 && !FILE_HEADER.equals(header)) {
                throw new IOException(
                        "Unrecognized camping checklist file.");
            }

            int tripCount = readCount(input);
            ArrayList<Trip> trips = new ArrayList<>();

            for (int i = 0; i < tripCount; i++) {
                trips.add(readTrip(input, version1));
            }

            if (input.read() != -1) {
                throw new IOException(
                        "Unexpected data after the trip records.");
            }

            return trips;

        } catch (NoSuchFileException exception) {
            return new ArrayList<>();

        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to load trips from " + fileName,
                    exception);
        }
    }

    /**
     * Writes one trip with all of its packing items and note sections.
     */
    private void writeTrip(
            DataOutputStream output,
            Trip trip) throws IOException {

        output.writeUTF(trip.getName());
        output.writeUTF(trip.getDestination());
        writeDate(output, trip.getStartDate());
        writeDate(output, trip.getEndDate());

        ArrayList<PackingItem> items = trip.getPackingItems();
        output.writeInt(items.size());

        for (PackingItem item : items) {
            output.writeUTF(item.getName());
            output.writeUTF(item.getCategory());
            output.writeInt(item.getQuantity());
            output.writeBoolean(item.isPacked());
        }

        ArrayList<NoteSection> sections = trip.getNoteSections();
        output.writeInt(sections.size());

        for (NoteSection section : sections) {
            output.writeUTF(section.getTitle());
            writeLongText(output, section.getContent());
        }
    }

    /**
     * Reads and validates one trip with its packing items and notes.
     */
    private Trip readTrip(
            DataInputStream input,
            boolean version1) throws IOException {

        String name = input.readUTF();
        String destination = input.readUTF();
        LocalDate startDate = readDate(input);
        LocalDate endDate = version1 ? null : readDate(input);

        int itemCount = readCount(input);

        try {
            Trip trip = new Trip(name, destination, startDate, endDate);

            for (int i = 0; i < itemCount; i++) {
                String itemName = input.readUTF();
                String category = input.readUTF();
                int quantity = input.readInt();
                boolean packed = input.readBoolean();

                PackingItem item = new PackingItem(
                        itemName, category, quantity);

                item.setPacked(packed);
                trip.addPackingItem(item);
            }

            int sectionCount = version1 ? 0 : readCount(input);

            for (int i = 0; i < sectionCount; i++) {
                String title = input.readUTF();
                String content = readLongText(input);

                trip.addNoteSection(new NoteSection(title, content));
            }

            return trip;

        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IOException(
                    "Invalid trip, item, or note data in file.",
                    exception);
        }
    }

    /**
     * Writes an optional date as a presence flag and ISO text.
     */
    private void writeDate(
            DataOutputStream output,
            LocalDate date) throws IOException {

        output.writeBoolean(date != null);

        if (date != null) {
            output.writeUTF(date.toString());
        }
    }

    /**
     * Reads an optional date written by writeDate().
     */
    private LocalDate readDate(
            DataInputStream input) throws IOException {

        if (!input.readBoolean()) {
            return null;
        }

        try {
            return LocalDate.parse(input.readUTF());
        } catch (DateTimeParseException exception) {
            throw new IOException(
                    "Invalid trip date in file.",
                    exception);
        }
    }

    /**
     * Writes text of any length. writeUTF() is limited to 64 KB,
     * which long notes could exceed.
     */
    private void writeLongText(
            DataOutputStream output,
            String text) throws IOException {

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
    }

    /**
     * Reads text written by writeLongText().
     */
    private String readLongText(
            DataInputStream input) throws IOException {

        byte[] bytes = new byte[readCount(input)];
        input.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads a nonnegative count or length.
     */
    private int readCount(
            DataInputStream input) throws IOException {

        int count = input.readInt();

        if (count < 0) {
            throw new IOException(
                    "Record count cannot be negative.");
        }

        return count;
    }
}
