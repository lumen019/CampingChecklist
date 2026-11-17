package campingChecklist;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Launches the Camping Trip Checklist application window.
 *
 * <p>This starter version creates the GUI without connecting
 * file loading or saving.</p>
 */
public class Main {

	/**
	 * Creates and displays the application window.
	 *
	 * @param args command-line arguments; not used
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Camping Trip Checklist");

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(new TripPlannerGUI());
			frame.setSize(900, 550);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}