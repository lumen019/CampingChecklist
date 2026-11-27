package campingChecklist;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
/**
 * Tests the constructor, getters, and setters of class PackingItem.
 * 
 * @author Jared Saltou
 * @author Colby Almond (co-author)
 */
class PackingItemTest {
	
	private PackingItem sockPair;
	
	@Test
	void negativeQuantity_Error() {
		assertThrows(IllegalArgumentException.class, () -> new PackingItem("Pair of Socks", "Clothing", -1));
	}
	
	@Test
	void zeroQuantity_Error() {
		assertThrows(IllegalArgumentException.class, () -> new PackingItem("Shirt", "Clothing", 0));
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
		assertThrows(IllegalArgumentException.class, () -> new PackingItem("Pair of Socks", "", 1));
	}
	
	@Test
	void categoryWhiteSpace_Error() {
		assertThrows(IllegalArgumentException.class, () -> new PackingItem("Pair of Socks", "   ", 1));
	}
	
	@Test
	void nameBlank_Error() {
		assertThrows(IllegalArgumentException.class, () -> new PackingItem("", "Clothing", 1));
	}
	
	@Test
	void nameWhiteSpace_Error() {
		assertThrows(IllegalArgumentException.class, () -> new PackingItem("   ", "Clothing", 1));
	}
	
	@Test
	void nameNull_Error() {
		assertThrows(NullPointerException.class, () -> new PackingItem(null, "Clothing", 1));
	}
	
	@Test
	void categoryNull_Error() {
		assertThrows(NullPointerException.class, () -> new PackingItem("Propane", null, 1));
	}
	
	@Test
	void constructorTrimsNameAndCategory() {
		sockPair = new PackingItem("  Pair of Socks ", "  clothing  ", 2);
		assertEquals("Pair of Socks", sockPair.getName());
		assertEquals("Clothing", sockPair.getCategory());
		assertEquals(2, sockPair.getQuantity());
	}
	
	@Test
	void newItemIsUnpacked() {
		sockPair = new PackingItem("Pair of Socks", "Clothing", 1);
		assertFalse(sockPair.isPacked());
	}
	
	@Test
	void packedGetterAndSetter_Test() {
		sockPair = new PackingItem("Pair of Socks", "Clothing", 1);
		sockPair.setPacked(true);
		assertTrue(sockPair.isPacked());
		sockPair.setPacked(false);
		assertFalse(sockPair.isPacked());
	}
	
	@Test
	void nameSetter_Test() {
		sockPair = new PackingItem("Pair of Socks", "Clothing", 1);
		sockPair.setName(" Wool Socks ");
		assertEquals("Wool Socks", sockPair.getName());
		assertThrows(IllegalArgumentException.class, () -> sockPair.setName(" "));
		assertThrows(NullPointerException.class, () -> sockPair.setName(null));
		assertEquals("Wool Socks", sockPair.getName());
	}
	
	@Test
	void categorySetter_Test() {
		sockPair = new PackingItem("Pair of Socks", "Clothing", 1);
		sockPair.setCategory("fOOTWEAR");
		assertEquals("Footwear", sockPair.getCategory());
		assertThrows(IllegalArgumentException.class, () -> sockPair.setCategory(""));
		assertEquals("Footwear", sockPair.getCategory());
	}
	
	@Test
	void quantitySetter_Test() {
		sockPair = new PackingItem("Pair of Socks", "Clothing", 1);
		sockPair.setQuantity(4);
		assertEquals(4, sockPair.getQuantity());
		assertThrows(IllegalArgumentException.class, () -> sockPair.setQuantity(0));
		assertEquals(4, sockPair.getQuantity());
	}
}
