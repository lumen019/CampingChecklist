package campingChecklist;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests that the GUI displays the planner's trips, items, progress,
 * and notes, and that edits in the GUI reach the trip data.
 *
 * <p>Dialog-based actions (add, edit, remove) are not tested here
 * because they wait for a person to click a button.</p>
 *
 * @author Lia Day
 * @author Colby Almond (co-author)
 */
class TripPlannerGUITest {

    @TempDir
    Path tempDir;

    private TripPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new TripPlanner(
                tempDir.resolve("gui-test-trips.dat").toString());
    }

    @Test
    void emptyPlannerDisplaysEmptyLists() throws Exception {
        onEdt(() -> {
            TripPlannerGUI gui = new TripPlannerGUI(planner);

            assertEquals(0, gui.getTripList().getModel().getSize());
            assertEquals(0, gui.getItemTable().getRowCount());
            assertEquals(0, gui.getNoteList().getModel().getSize());
            assertEquals("0 of 0 packed — 0%", gui.getProgressText());
        });
    }

    @Test
    void displaysTripsAndSelectsFirstTrip() throws Exception {
        Trip first = new Trip("Weekend", "Pine Lake",
                LocalDate.of(2026, 6, 12), LocalDate.of(2026, 6, 14));
        planner.addTrip(first);
        planner.addTrip(new Trip("Later", "Forest", null, null));

        onEdt(() -> {
            TripPlannerGUI gui = new TripPlannerGUI(planner);

            assertEquals(2, gui.getTripList().getModel().getSize());
            assertSame(first, gui.getTripList().getSelectedValue());
            assertEquals(
                    "Dates: 6/12/2026 – 6/14/2026 (3 days)",
                    gui.getDatesText());
        });
    }

    @Test
    void selectingTripDisplaysItsChecklistAndNotes() throws Exception {
        Trip first = new Trip("First", "Lake", null, null);
        Trip second = new Trip("Second", "Forest", null, null);
        PackingItem tent = new PackingItem("Tent", "Shelter", 2);
        tent.setPacked(true);
        NoteSection campsite = new NoteSection("Campsite", "Site 42");

        second.addPackingItem(tent);
        second.addNoteSection(campsite);
        planner.addTrip(first);
        planner.addTrip(second);

        onEdt(() -> {
            TripPlannerGUI gui = new TripPlannerGUI(planner);
            gui.getTripList().setSelectedValue(second, true);

            JTable table = gui.getItemTable();
            assertEquals(1, table.getRowCount());
            assertEquals(Boolean.TRUE, table.getValueAt(0, 0));
            assertEquals("Tent", table.getValueAt(0, 1));
            assertEquals("Shelter", table.getValueAt(0, 2));
            assertEquals(2, table.getValueAt(0, 3));
            assertEquals("Dates: not set", gui.getDatesText());

            assertSame(campsite, gui.getNoteList().getSelectedValue());
            assertEquals("Site 42", gui.getNoteTextArea().getText());

            gui.getTripList().setSelectedValue(first, true);

            assertEquals(0, gui.getItemTable().getRowCount());
            assertEquals(0, gui.getNoteList().getModel().getSize());
            assertEquals("", gui.getNoteTextArea().getText());
        });
    }

    @Test
    void displaysProgressWeightedByQuantity() throws Exception {
        Trip trip = new Trip("Weekend", "Pine Lake", null, null);
        PackingItem tent = new PackingItem("Tent", "Shelter", 1);
        PackingItem water = new PackingItem("Water", "Hydration", 3);

        tent.setPacked(true);
        trip.addPackingItem(tent);
        trip.addPackingItem(water);
        planner.addTrip(trip);

        onEdt(() -> {
            TripPlannerGUI gui = new TripPlannerGUI(planner);

            assertEquals("1 of 4 packed — 25%", gui.getProgressText());
        });
    }

    @Test
    void checkingPackedBoxUpdatesItemAndProgress() throws Exception {
        Trip trip = new Trip("Weekend", "Pine Lake", null, null);
        PackingItem tent = new PackingItem("Tent", "Shelter", 1);
        PackingItem water = new PackingItem("Water", "Hydration", 3);
        trip.addPackingItem(tent);
        trip.addPackingItem(water);
        planner.addTrip(trip);

        onEdt(() -> {
            TripPlannerGUI gui = new TripPlannerGUI(planner);

            // Same call the table makes when the user clicks the checkbox.
            gui.getItemTable().setValueAt(Boolean.TRUE, 1, 0);

            assertTrue(water.isPacked());
            assertFalse(tent.isPacked());
            assertEquals("3 of 4 packed — 75%", gui.getProgressText());

            gui.getItemTable().setValueAt(Boolean.FALSE, 1, 0);

            assertFalse(water.isPacked());
            assertEquals("0 of 4 packed — 0%", gui.getProgressText());
            assertTrue(planner.hasUnsavedChanges());
        });
    }

    @Test
    void closeIsAllowedWithoutPromptWhenNothingChanged() throws Exception {
        Trip trip = new Trip("Weekend", "Pine Lake", null, null);
        trip.addPackingItem(new PackingItem("Tent", "Shelter", 1));
        trip.addNoteSection(new NoteSection("Campsite", "Site 42"));
        planner.addTrip(trip);
        planner.saveTrips();

        onEdt(() -> {
            TripPlannerGUI gui = new TripPlannerGUI(planner);

            // Showing the trip must not count as an edit, or closing
            // would prompt (and fail headless) here.
            assertFalse(planner.hasUnsavedChanges());
            assertTrue(gui.confirmClose());
        });
    }

    @Test
    void typingInNotesUpdatesSelectedSectionOnly() throws Exception {
        Trip trip = new Trip("Weekend", "Pine Lake", null, null);
        NoteSection campsite = new NoteSection("Campsite", "Site 42");
        NoteSection meals = new NoteSection("Meals", "Tacos");
        trip.addNoteSection(campsite);
        trip.addNoteSection(meals);
        planner.addTrip(trip);

        onEdt(() -> {
            TripPlannerGUI gui = new TripPlannerGUI(planner);
            gui.getNoteList().setSelectedValue(meals, true);

            assertEquals("Tacos", gui.getNoteTextArea().getText());

            gui.getNoteTextArea().setText("Tacos and s'mores");

            assertEquals("Tacos and s'mores", meals.getContent());
            assertEquals("Site 42", campsite.getContent());

            gui.getNoteList().setSelectedValue(campsite, true);

            assertEquals("Site 42", gui.getNoteTextArea().getText());
            assertEquals("Tacos and s'mores", meals.getContent());
        });
    }

    @Test
    void displayTripsShowsLoadedTripsAndKeepsSelection() throws Exception {
        Trip first = new Trip("First", "Lake", null, null);
        Trip second = new Trip("Second", "Forest", null, null);
        planner.addTrip(first);
        planner.addTrip(second);

        onEdt(() -> {
            TripPlannerGUI gui = new TripPlannerGUI(planner);
            gui.getTripList().setSelectedValue(second, true);

            planner.addTrip(new Trip("Third", "Desert", null, null));
            gui.displayTrips();

            assertEquals(3, gui.getTripList().getModel().getSize());
            assertSame(second, gui.getTripList().getSelectedValue());

            planner.removeTrip(first);
            planner.removeTrip(second);
            gui.displayTrips();

            assertEquals(1, gui.getTripList().getModel().getSize());
            assertEquals(
                    "Third",
                    gui.getTripList().getSelectedValue().getName());
        });
    }

    private static void onEdt(Runnable action) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });

        Throwable throwable = failure.get();

        if (throwable instanceof Error) {
            throw (Error) throwable;
        }

        if (throwable instanceof Exception) {
            throw (Exception) throwable;
        }

        if (throwable != null) {
            throw new RuntimeException(throwable);
        }
    }
}
