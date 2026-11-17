package campingTripChecklist;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
/**
 * Tests the constructor and getters of class PackingItem.
 * 
 * @author Jared Saltou
 */
class PackingItemTest {
	
	private PackingItem shirts;
	private PackingItem sockPair;
	
	@Test
	void negativeQuantity_Error() {
		assertThrows(IllegalArgumentException.class, () -> { sockPair = new PackingItem("Pair of Socks", "Clothing", -1); });
	}
	
	@Test
	void zeroQuantity_Error() {
		assertThrows(IllegalArgumentException.class, () -> { shirts = new PackingItem("Shirt", "Clothing", 0); });
	}
	
	@Test
	void packingTest() {
		sockPair = new PackingItem("Pair of Socks", "Clothing", 1);
		assertFalse(sockPair.getPacked());
		sockPair.setPacked(true);
		assertTrue(sockPair.getPacked());
	}
	
	@Test
	void category_WeirdCap() {
		sockPair = new PackingItem("Pair of Socks", "CloThinG", 1);
		assertEquals("Clothing", sockPair.getCategory());
	}
	
	@Test
	void category_CapLock() {
		sockPair = new PackingItem("Pair of Socks", "cLOTHING", 1);
		assertEquals("Clothing", sockPair.getCategory());
	}
	
	@Test
	void categoryBlank_Error() {
		assertThrows(IllegalArgumentException.class, () -> { sockPair = new PackingItem("Pair of Socks", "", 1); });
	}
	
	@Test
	void categoryWhiteSpace_Error() {
		assertThrows(IllegalArgumentException.class, () -> { sockPair = new PackingItem("Pair of Socks", "   ", 1); });
	}
	
	@Test
	void nameBlank_Error() {
		assertThrows(IllegalArgumentException.class, () -> { sockPair = new PackingItem("", "Clothing", 1); });
	}
	
	@Test
	void nameWhiteSpce_Error() {
		assertThrows(IllegalArgumentException.class, () -> { sockPair = new PackingItem("   ", "Clothing", 1); });
	}
	
	@Test
	void nameNull_Error() {
		assertThrows(NullPointerException.class, () -> { sockPair = new PackingItem(null, "Clothing", 1); });
	}
	
	void catergoryNull_Error() {
		assertThrows(NullPointerException.class, () -> { sockPair = new PackingItem("Propane", null, 1); });
	}
	
	@Test
	void nameGetter_Test() {
		sockPair = new PackingItem("Pair of Socks", "Clothing", 1);
		assertEquals("Pair of Socks", sockPair.getName());
	}
	
	@Test
	void categoryGetter_Test() {
		sockPair = new PackingItem("Pair of Socks", "Clothing", 1);
		assertEquals("Clothing", sockPair.getCategory());
	}
	
	@Test
	void packedGetterAndSetter_Test() {
		sockPair = new PackingItem("Pair of Socks", "Clothing", 1);
		assertFalse(sockPair.getPacked());
		sockPair.setPacked(true);
		assertTrue(sockPair.getPacked());
		sockPair.setPacked(false);
		assertFalse(sockPair.getPacked());
	}
}
