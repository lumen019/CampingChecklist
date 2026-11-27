package campingChecklist;

import java.util.Objects;

/**
 * A titled section of notes for a trip, such as "Campsite" or "Meal Plan".
 *
 * @author Colby Almond
 */
public class NoteSection {
	private String title;
	private String content;

	/**
	 * Creates a note section.
	 *
	 * @param title   the section title
	 * @param content the note text, which may be empty
	 * @throws NullPointerException if title or content is null
	 * @throws IllegalArgumentException if title is blank
	 */
	public NoteSection(String title, String content) {
		setTitle(title);
		setContent(content);
	}

	/**
	 * Returns the section title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the section title. Surrounding whitespace is removed.
	 *
	 * @param title the new title
	 * @throws NullPointerException if title is null
	 * @throws IllegalArgumentException if title is blank
	 */
	public void setTitle(String title) {
		Objects.requireNonNull(title, "Section must have a title!");
		if (title.isBlank()) {
			throw new IllegalArgumentException("Section must have a title!");
		}
		this.title = title.trim();
	}

	/**
	 * Returns the note text.
	 *
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Sets the note text.
	 *
	 * @param content the new text, which may be empty
	 * @throws NullPointerException if content is null
	 */
	public void setContent(String content) {
		this.content = Objects.requireNonNull(content, "Content cannot be null.");
	}

	@Override
	public String toString() {
		return title;
	}
}
