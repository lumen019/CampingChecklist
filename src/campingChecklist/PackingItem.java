package campingChecklist; //remember to change this if needed

import java.util.Objects;

/**
 * A item added to the checklist.
 * 
 * @author Jared Saltou
 * @author Colby Almond (co-author)
 */
public class PackingItem{
	private String name;
	private String category;
	private int quantity;
	private boolean packed;
	
	/**
	 * Constructs a new unpacked item that has a name, a category, and the amount being packed.
	 * 
	 * @param name			Name of the item i.e Pants, Shirt, Flashlight
	 * @param category		Category it falls under i.e Tent, Clothing, Hygiene
	 * @param quantity		Amount of the items.
	 * @throws NullPointerException if name or category is null
	 * @throws IllegalArgumentException if name or category is blank or quantity is not positive
	 */
	public PackingItem(String name, String category, int quantity) {
		setName(name);
		setCategory(category);
		setQuantity(quantity);
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
	 * Sets the name of the item. Surrounding whitespace is removed.
	 * 
	 * @param name the new name
	 * @throws NullPointerException if name is null
	 * @throws IllegalArgumentException if name is blank
	 */
	public void setName(String name) {
		Objects.requireNonNull(name, "Name must have a name!");
		if (name.isBlank()) {
			throw new IllegalArgumentException("Name must have a name!");
		}
		this.name = name.trim();
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
	 * Sets the category of the item, capitalized like "Clothing".
	 * 
	 * @param category the new category
	 * @throws NullPointerException if category is null
	 * @throws IllegalArgumentException if category is blank
	 */
	public void setCategory(String category) {
		Objects.requireNonNull(category, "Category must have a name!");
		if (category.isBlank()) {
			throw new IllegalArgumentException("Category must have a name!");
		}
		category = category.trim();
		this.category = Character.toUpperCase(category.charAt(0)) + category.substring(1).toLowerCase();
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
	 * Sets the quantity of the item.
	 * 
	 * @param quantity the new quantity, at least 1
	 * @throws IllegalArgumentException if quantity is not positive
	 */
	public void setQuantity(int quantity) {
		if (quantity <= 0) {
			throw new IllegalArgumentException("Value must be positive!");
		}
		this.quantity = quantity;
	}
	
	/**
	 * Sets whether the item is packed (checked off).
	 * 
	 * @param packed true if the item is packed
	 */
	public void setPacked(boolean packed) {
		this.packed = packed;
	}
	
	/**
	 * Returns if the item is packed or not.
	 * 
	 * @return the packed condition
	 */
	public boolean isPacked() {
		return packed;
	}

	@Override
	public String toString() {
		return name + " (" + category + ") x" + quantity;
	}
}
