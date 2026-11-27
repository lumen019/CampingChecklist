package campingChecklist;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UncheckedIOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Launches the Camping Trip Checklist application window.
 *
 * <p>Trips saved in trips.dat are loaded when the window opens. Closing
 * the window with unsaved changes asks whether to save them first.
 * The window uses Swing's built-in Nimbus theme.</p>
 *
 * @author Lia Day
 * @author Colby Almond (co-author)
 */
public class Main {

	/**
	 * Loads saved trips and displays the application window.
	 *
	 * @param args command-line arguments; not used
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			useNimbusTheme();

			TripPlanner planner = new TripPlanner();
			String loadError = null;

			try {
				planner.loadTrips();
			} catch (UncheckedIOException e) {
				loadError = e.getMessage();
			}

			JFrame frame = new JFrame("Camping Trip Checklist");
			TripPlannerGUI gui = new TripPlannerGUI(planner);

			// Ask to save unsaved changes before the window closes.
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (gui.confirmClose()) {
						frame.dispose();
						System.exit(0);
					}
				}
			});

			frame.setContentPane(gui);
			frame.setSize(1000, 600);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			if (loadError != null) {
				JOptionPane.showMessageDialog(
						frame,
						loadError + "\nStarting with an empty trip list.",
						"Could Not Load Trips",
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	/**
	 * Switches Swing to its built-in Nimbus look so the app looks the
	 * same on macOS and Windows. Must run before any window is created.
	 * If Nimbus is unavailable, the default look is kept.
	 */
	static void useNimbusTheme() {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					return;
				}
			}
		} catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
			// Keep the default look; the app works the same either way.
		}
	}
}
