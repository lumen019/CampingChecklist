package campingChecklist; //remember to change this if needed

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
/**
 * A trip that has a name, a destination, optional start and end dates,
 * the items that will be packed, and sections of notes.
 * 
 * @author Jared Saltou
 * @author Colby Almond (co-author)
 */
public class Trip{
	private String name;
	private String destination;
	private LocalDate startDate;
	private LocalDate endDate;
	private ArrayList<PackingItem> packingItems;
	private ArrayList<NoteSection> noteSections;
	
	/**
	 * Constructs a trip with no packing items and no notes.
	 * 
	 * @param name				The name of the trip
	 * @param destination			The destination of the trip, also used for Google Maps
	 * @param startDate			The first day of the trip, or null if not planned yet
	 * @param endDate			The last day of the trip, or null if not planned yet
	 * @throws NullPointerException if name or destination is null
	 * @throws IllegalArgumentException if name or destination is blank,
	 *         or endDate is before startDate
	 */
	public Trip(String name, String destination, LocalDate startDate, LocalDate endDate) {
		setName(name);
		setDestination(destination);
		setDates(startDate, endDate);
		this.packingItems = new ArrayList<>();
		this.noteSections = new ArrayList<>();
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
	 * Set the name of the trip. Surrounding whitespace is removed.
	 * 
	 * @param name the name to set
	 * @throws NullPointerException if name is null
	 * @throws IllegalArgumentException if name is blank
	 */
	public void setName(String name) {
		Objects.requireNonNull(name, "Must have a name!");
		if (name.isBlank()) {
			throw new IllegalArgumentException("Must have a name!");
		}
		this.name = name.trim();
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
	 * Set the destination of the trip. Surrounding whitespace is removed.
	 * 
	 * @param destination the destination to set
	 * @throws NullPointerException if destination is null
	 * @throws IllegalArgumentException if destination is blank
	 */
	public void setDestination(String destination) {
		Objects.requireNonNull(destination, "destination must have a name!");
		if (destination.isBlank()) {
			throw new IllegalArgumentException("destination must have a name!");
		}
		this.destination = destination.trim();
	}

	/**
	 * Returns the first day of the trip.
	 * 
	 * @return the start date, or null if not set
	 */
	public LocalDate getStartDate() {
		return startDate;
	}

	/**
	 * Returns the last day of the trip.
	 * 
	 * @return the end date, or null if not set
	 */
	public LocalDate getEndDate() {
		return endDate;
	}

	/**
	 * Sets both trip dates together so they can be checked against each other.
	 * Either date may be null when it has not been planned yet.
	 * 
	 * @param startDate the first day of the trip, or null
	 * @param endDate the last day of the trip, or null
	 * @throws IllegalArgumentException if endDate is before startDate
	 */
	public void setDates(LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
			throw new IllegalArgumentException("End date cannot be before the start date!");
		}
		this.startDate = startDate;
		this.endDate = endDate;
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
	 * Returns the note sections of the trip.
	 * 
	 * @return the note sections
	 */
	public ArrayList<NoteSection> getNoteSections() {
		return noteSections;
	}

	/**
	 * Add a note section to the trip.
	 * 
	 * @param section	New note section
	 */
	public void addNoteSection(NoteSection section) {
		noteSections.add(section);
	}

	/**
	 * Remove a note section from the trip.
	 * 
	 * @param section	Note section being removed
	 */
	public void removeNoteSection(NoteSection section) {
		noteSections.remove(section);
	}
	
	/**
	 * Returns the percentage packed for the trip, weighted by quantity.
	 * 
	 * <p>For example, a packed tent (x1) and unpacked water bottles (x3)
	 * is 1 of 4 total items, or 25%.</p>
	 * 
	 * @return the percentage packed, from 0.0 to 100.0
	 */
	public double calculateProgress() {
		long total = 0;
		long packed = 0;
		
		for (PackingItem p : packingItems) {
			total += p.getQuantity();
			if (p.isPacked()) {
				packed += p.getQuantity();
			}
		}
		
		if (total == 0) {
			return 0.0;
		}
		return 100.0 * packed / total;
	}
	
	/**
	 * Returns whether every item on the checklist is packed.
	 * 
	 * @return true if the trip has items and all of them are packed;
	 *         false for an empty checklist
	 */
	public boolean isAllPacked() {
		if (packingItems.isEmpty()) {
			return false;
		}
		for (PackingItem p : packingItems) {
			if (!p.isPacked()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
	    return name + " (" + destination + ")";
	}
}
