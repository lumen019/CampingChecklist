package campingChecklist;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
/**
 * Tests the constructor and the public methods of class Trip.
 * 
 * @author Jared Saltou
 * @author Colby Almond (co-author)
 */
class TripTest {

	private static final double DELTA = 0.000001;
	private static final LocalDate START = LocalDate.of(2026, 11, 15);
	private static final LocalDate END = LocalDate.of(2026, 11, 17);

	private Trip trip = new Trip("Weekend", "Zion", START, END);
	
	@Test
	void emptyList_Test() {
		assertEquals(0.0, trip.calculateProgress(), DELTA);
	}
	
	@Test
	void completePacking_Test() {
		PackingItem shirt = new PackingItem("Shirt", "Clothing", 3);
		shirt.setPacked(true);
		trip.addPackingItem(shirt);
		assertEquals(100.0, trip.calculateProgress(), DELTA);
	}
	
	@Test
	void progressIsWeightedByQuantity() {
		PackingItem tent = new PackingItem("Tent", "Shelter", 1);
		PackingItem water = new PackingItem("Water", "Hydration", 3);
		tent.setPacked(true);
		trip.addPackingItem(tent);
		trip.addPackingItem(water);
		// 1 of 4 total items packed, not 1 of 2 entries
		assertEquals(25.0, trip.calculateProgress(), DELTA);
		
		water.setPacked(true);
		tent.setPacked(false);
		assertEquals(75.0, trip.calculateProgress(), DELTA);
	}

	@Test
	void isAllPacked_Test() {
		assertFalse(trip.isAllPacked(), "an empty checklist is not packed");
		PackingItem tent = new PackingItem("Tent", "Shelter", 1);
		PackingItem water = new PackingItem("Water", "Hydration", 3);
		trip.addPackingItem(tent);
		trip.addPackingItem(water);
		tent.setPacked(true);
		assertFalse(trip.isAllPacked());
		water.setPacked(true);
		assertTrue(trip.isAllPacked());
	}

	@Test
	void thirdCompletePacking_Test() {
		PackingItem shirt = new PackingItem("Shirt", "Clothing", 2);
		PackingItem pants = new PackingItem("Pants", "Clothing", 2);
		PackingItem underwear = new PackingItem("Underwear", "Clothing", 2);
		shirt.setPacked(true);
		trip.addPackingItem(shirt);
		trip.addPackingItem(pants);
		trip.addPackingItem(underwear);
		assertEquals(100.0 / 3, trip.calculateProgress(), DELTA);
	}
	
	@Test
	void blankDestination_Test() {
		assertThrows(IllegalArgumentException.class, () -> new Trip("Weekend", "", START, END));
	}
	
	@Test
	void whiteSpaceDestination_Test() {
		assertThrows(IllegalArgumentException.class, () -> new Trip("Weekend", "   ", START, END));
	}
	
	@Test
	void destinationNull_Error() {
		assertThrows(NullPointerException.class, () -> new Trip("Weekend", null, START, END));
	}
	
	@Test
	void blankName_Error() {
		assertThrows(IllegalArgumentException.class, () -> new Trip(" ", "Zion", START, END));
	}
	
	@Test
	void nameNull_Error() {
		assertThrows(NullPointerException.class, () -> new Trip(null, "Zion", START, END));
	}
	
	@Test
	void datesAreOptional() {
		Trip undated = new Trip("Someday", "Moab", null, null);
		assertNull(undated.getStartDate());
		assertNull(undated.getEndDate());
	}
	
	@Test
	void oneDayTripAllowsSameStartAndEnd() {
		Trip dayTrip = new Trip("Day Hike", "Moab", START, START);
		assertEquals(START, dayTrip.getEndDate());
	}
	
	@Test
	void endBeforeStart_Error() {
		assertThrows(IllegalArgumentException.class, () -> new Trip("Weekend", "Zion", END, START));
	}
	
	@Test
	void setDatesRejectsEndBeforeStartWithoutChangingDates() {
		assertThrows(IllegalArgumentException.class, () -> trip.setDates(END, START));
		assertEquals(START, trip.getStartDate());
		assertEquals(END, trip.getEndDate());
	}
	
	@Test
	void setDatesCanMoveTripLater() {
		LocalDate laterStart = END.plusDays(10);
		LocalDate laterEnd = END.plusDays(12);
		trip.setDates(laterStart, laterEnd);
		assertEquals(laterStart, trip.getStartDate());
		assertEquals(laterEnd, trip.getEndDate());
	}
	
	@Test
	void destinationGetterAndSetter_Test() {
		assertEquals("Zion", trip.getDestination());
		trip.setDestination("  Bryce Canyon ");
		assertEquals("Bryce Canyon", trip.getDestination());
	}

	@Test
	void nameGetterAndSetter_Test() {
		assertEquals("Weekend", trip.getName());
		trip.setName("Hike and Ski");
		assertEquals("Hike and Ski", trip.getName());
	}
	
	@Test
	void arrayListEdit_Test() {
		PackingItem shirt = new PackingItem("Shirt", "Clothing", 1);
		PackingItem pants = new PackingItem("Pants", "Clothing", 99);
		PackingItem underwear = new PackingItem("Underwear", "Clothing", 1);
		trip.addPackingItem(shirt);
		trip.addPackingItem(pants);
		trip.addPackingItem(underwear);
		assertEquals(3, trip.getPackingItems().size());
		trip.removePackingItem(underwear);
		assertEquals(2, trip.getPackingItems().size());
	}
	
	@Test
	void noteSectionsCanBeAddedAndRemoved() {
		NoteSection campsite = new NoteSection("Campsite", "Site 42");
		NoteSection meals = new NoteSection("Meal Plan", "");
		trip.addNoteSection(campsite);
		trip.addNoteSection(meals);
		assertEquals(2, trip.getNoteSections().size());
		trip.removeNoteSection(campsite);
		assertSame(meals, trip.getNoteSections().get(0));
	}
}
