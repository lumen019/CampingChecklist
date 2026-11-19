package campingChecklist; //remember to change this if needed

import java.util.Objects;

/**
 * A item added to the checklist.
 * 
 * @author Jared Saltou
 */
public class PackingItem{
	private String name;
	private String category;
	private int quantity;
	private boolean packed;
	
	/**
	 * Constructs a new item that has a name, a category, the amount being packed, and if the item is already packed.
	 * 
	 * @param name			Name of the item i.e Pants, Shirt, Flashlight
	 * @param category		Category it falls under i.e Tent, Clothing, Hygiene
	 * @param quantity		Amount of the items.
	 * @param packed		is the item packed(checked off)
	 */
	public PackingItem(String name, String category, int quantity) {
		super();
		Objects.requireNonNullElse(name, "").isBlank();
		
		/*
		 * Invalid input detection
		 */
		if (quantity <= 0) {
			throw new IllegalArgumentException("Value must be positive!");
		}
		if (category.equals(null) || name.equals(null)) {
			throw new NullPointerException("Category and Name must have a name!");
		}
		if (category.isBlank() || name.isBlank()) {
			throw new IllegalArgumentException("Category and Name must have a name!");
		} 
		
		category = Character.toUpperCase(category.charAt(0)) + category.substring(1).toLowerCase();
				
		this.name = name;
		this.category = category;
		this.quantity = quantity;
	}
	
	/**
	 * Returns the name of the item.
	 * 
	 * @return the name of the item
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the category of the item.
	 * 
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}
	
	/**
	 * Returns the quantity of the item.
	 * 
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}
	
	/**
	 * Sets the boolean packed to true or false.
	 * 
	 * @param packed the packed to set
	 */
	public void setPacked(boolean packed) {
		this.packed = packed;
	}
	
	/**
	 * Returns if the item is packed or not.
	 * 
	 * @return the packed condition
	 */
	public boolean getPacked() {
		return packed;
	}

	@Override
	public String toString() {
		return name + " (" + category + ") x" + quantity;
	}
	
	
}