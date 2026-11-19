package campingChecklist;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
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
 */
public class TripFileManager {

    private static final String FILE_HEADER =
            "CAMPING_CHECKLIST_V1";

    private final String fileName;

    public TripFileManager(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "File name is required.");
        }

        this.fileName = fileName;
    }

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

    public ArrayList<Trip> loadTrips() {
        Path path = Paths.get(fileName);

        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(
                        Files.newInputStream(path)))) {

            if (!FILE_HEADER.equals(input.readUTF())) {
                throw new IOException(
                        "Unrecognized camping checklist file.");
            }

            int tripCount = readCount(input);
            ArrayList<Trip> trips = new ArrayList<>();

            for (int i = 0; i < tripCount; i++) {
                trips.add(readTrip(input));
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

    private void writeTrip(
            DataOutputStream output,
            Trip trip) throws IOException {

        output.writeUTF(trip.getName());
        output.writeUTF(trip.getLocation());

        LocalDate date = trip.getDate();
        output.writeBoolean(date != null);

        if (date != null) {
            output.writeUTF(date.toString());
        }

        ArrayList<PackingItem> items = trip.getPackingItems();
        output.writeInt(items.size());

        for (PackingItem item : items) {
            output.writeUTF(item.getName());
            output.writeUTF(item.getCategory());
            output.writeInt(item.getQuantity());
            output.writeBoolean(item.isPacked());
        }
    }

    private Trip readTrip(
            DataInputStream input) throws IOException {

        String name = input.readUTF();
        String location = input.readUTF();

        LocalDate date = null;

        if (input.readBoolean()) {
            try {
                date = LocalDate.parse(input.readUTF());
            } catch (DateTimeParseException exception) {
                throw new IOException(
                        "Invalid trip date in file.",
                        exception);
            }
        }

        int itemCount = readCount(input);

        try {
            Trip trip = new Trip(name, location, date);

            for (int i = 0; i < itemCount; i++) {
                String itemName = input.readUTF();
                String category = input.readUTF();
                int quantity = input.readInt();
                boolean packed = input.readBoolean();

                if (quantity < 1) {
                    throw new IOException(
                            "Item quantity must be positive.");
                }

                PackingItem item = new PackingItem(
                        itemName, category, quantity);

                item.setPacked(packed);
                trip.addPackingItem(item);
            }

            return trip;

        } catch (IllegalArgumentException exception) {
            throw new IOException(
                    "Invalid trip or item data in file.",
                    exception);
        }
    }

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