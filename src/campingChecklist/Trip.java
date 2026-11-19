package campingChecklist; //remember to change this if needed

import java.time.LocalDate;
import java.util.ArrayList;
/**
 * A trip that has a destination, Date, name of trip, and the items that will be packed.
 * 
 * @author Jared Saltou
 */
public class Trip{
	private String name;
	private LocalDate date;
	private String destination;
	private ArrayList<PackingItem> packingItems;
	
	/**
	 * Constructs a trip with a date,a destination, and an empty ArrayList of PackingItem.
	 * 
	 * @param date				The date of the trip
	 * @param destination		The destination of the trip
	 */
	public Trip(String destination, LocalDate date) {
		super();
		
		/*
		 * invalid input detection
		 */
		if (destination == null) {
			throw new NullPointerException("destination must have a name!");
		} 
		if (destination.isBlank()) {
			throw new IllegalArgumentException("destination must have a name!");
		}
		if (date == null) {
			throw new NullPointerException("There must be a date!");
		}
		
		this.destination = destination;
		this.date = date;
		this.packingItems = new ArrayList<>();
	}

	/**
	 * Returns the name of the trip.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the trip.
	 * 
	 * @param name the name to set
	 */
	public void setName(String name) {
		/*
		 * Invalid input detection
		 */
		if (name == null) {
			throw new NullPointerException("Must have a name!");
		} 
		if (name.isBlank()) {
			throw new IllegalArgumentException("Must have a name!");
		}
		this.name = name;
	}

	/**
	 * Returns the date of the trip.
	 * 
	 * @return the date
	 */
	public LocalDate getDate() {
		return date;
	}

	/**
	 * Set the date of the trip.
	 * 
	 * @param date the date to set
	 */
	public void setDate(LocalDate date) {
		/*
		 * Invalid input detection
		 */
		if (date == null) {
			throw new NullPointerException("Must have a date!");
		}
		this.date = date;
	}

	/**
	 * Returns the destination of the trip.
	 * 
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * Set the destination of the trip.
	 * 
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		/*
		 * Invalid input detection
		 */
		if (destination == null) {
			throw new NullPointerException("destination must have a name!");
		} 
		if (destination.isBlank()) {
			throw new IllegalArgumentException("destination must have a name!");
		}
		this.destination = destination;
	}

	/**
	 * Returns the items of the checking list for a trip.
	 * 
	 * @return the packingItems
	 */
	public ArrayList<PackingItem> getPackingItems() {
		return packingItems;
	}
	
	/**
	 * Add an item to the check list.
	 * 
	 * @param item		New item added to the checklist
	 */
	public void addPackingItem(PackingItem item) {
		packingItems.add(item);
	}
	
	/**
	 * Remove an item from the check list.
	 * 
	 * @param item		Item that is being removed from the list
	 */
	public void removePackingItem(PackingItem item) {
		packingItems.remove(item);
	}
	
	/**
	 * Returns the percentage packed for the trip.
	 * 
	 * @return the percentage packed
	 */
	public double getPackingProgress() {
		if (packingItems.isEmpty()) {
			return 0.0;
		}
		int packed = 0;
		
		for (PackingItem p : packingItems) {
			if (p.getPacked()) {
				packed++;
			}
		}
		
		return (double) packed / packingItems.size();
	}
	
	@Override
	public String toString() {
	    return name + " (" + destination + ")";
	}
}