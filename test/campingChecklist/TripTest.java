package campingTripChecklist;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
/**
 * Tests the constructor and the public methods of class Trip.
 * 
 * @author Jared Saltou
 */
class TripTest {

	double delta = 0.000001;
	private Trip trip;
	
	@Test
	void emptyList_Test() {
		trip = new Trip("A", LocalDate.of(2026, 11, 15));
		double expected = 0.0;
		double actual = trip.getPackingProgress();
		assertEquals(expected, actual, delta);
	}
	
	@Test
	void completePacking_Test() {
		trip = new Trip("A", LocalDate.of(2026, 11, 15));
		PackingItem shirt = new PackingItem("Shirt", "Clothing", 1);
		shirt.setPacked(true);
		trip.addPackingItem(shirt);
		double expected = 1.0;
		double actual = trip.getPackingProgress();
		assertEquals(expected, actual, delta);
	}
	
	@Test
	void halfCompletePacking_Test() {
		trip = new Trip("A", LocalDate.of(2026, 11, 15));
		PackingItem shirt = new PackingItem("Shirt", "Clothing", 1);
		PackingItem pants = new PackingItem("Pants", "Clothing", 99);
		shirt.setPacked(true);
		trip.addPackingItem(shirt);
		trip.addPackingItem(pants);
		double expected = 0.5;
		double actual = trip.getPackingProgress();
		assertEquals(expected, actual, delta);
	}

	@Test
	void thirdCompletePacking_Test() {
		trip = new Trip("A", LocalDate.of(2026, 11, 15));
		PackingItem shirt = new PackingItem("Shirt", "Clothing", 1);
		PackingItem pants = new PackingItem("Pants", "Clothing", 99);
		PackingItem underwear = new PackingItem("Underwear", "Clothing", 1);
		shirt.setPacked(true);
		trip.addPackingItem(shirt);
		trip.addPackingItem(pants);
		trip.addPackingItem(underwear);
		double expected = 0.33;
		double actual = trip.getPackingProgress();
		assertEquals(expected, actual, 0.01); //Used 0.01 delta for the repeating 3 from 1/3
	}
	
	@Test
	void blankDestination_Test() {
		assertThrows(IllegalArgumentException.class, () -> { trip = new Trip("", LocalDate.of(2026, 11, 15)); });
	}
	
	@Test
	void whiteSpaceDestination_Test() {
		assertThrows(IllegalArgumentException.class, () -> { trip = new Trip("   ", LocalDate.of(2026, 11, 15)); });
	}
	
	@Test
	void destinationNull_Error() {
		assertThrows(NullPointerException.class, () -> { trip = new Trip(null, LocalDate.of(2026, 11, 15)); });
	}
	
	@Test
	void dateNull_Error() {
		assertThrows(NullPointerException.class, () -> { trip = new Trip("Cali", null); });
	}
	
	@Test
	void destinationGetterAndSetter_Test() {
		trip = new Trip("A", LocalDate.of(2026, 11, 15));
		assertEquals("A", trip.getDestination());
		trip.setDestination("B");
		assertEquals("B", trip.getDestination());
	}
	@Test
	void nameGetterAndSetter_Test() {
		trip = new Trip("A", LocalDate.of(2026, 11, 15));
		trip.setName("Hike and Ski");
		assertEquals("Hike and Ski", trip.getName());
		trip.setName("Hike and Surf");
		assertEquals("Hike and Surf", trip.getName());
	}
	
	@Test
	void dateGetterAndSetter_Test() {
		trip = new Trip("A", LocalDate.of(2026, 11, 15));
		assertEquals(LocalDate.of(2026, 11, 15), trip.getDate());
		trip.setDate(LocalDate.of(2036, 11, 15));
		assertEquals(LocalDate.of(2036, 11, 15), trip.getDate());
	}
	
	@Test
	void arrayListEdit_Test() {
		trip = new Trip("A", LocalDate.of(2026, 11, 15));
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
}
