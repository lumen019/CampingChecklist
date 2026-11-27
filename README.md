# CampingChecklist

### Camping Trip Checklist

A Java Swing desktop application for planning camping trips, organizing packing lists, and tracking packing progress.

## Features

- Create, edit, and remove trips with a name, destination, and optional start and end dates.
- Add, edit, and remove packing items with a name, category (chosen from a list), and quantity.
- Check items off as packed; packing progress is weighted by quantity (a packed tent ×1 and unpacked water bottles ×3 is
  25%).
- Keep notes for each trip in titled sections (e.g. Campsite, Meal Plan).
- View a trip's destination on Google Maps.
- Save and load trips, packing lists, and notes. Trips load automatically at startup.
- Closing the app with unsaved changes asks whether to save first.
- Nimbus theme, so the app looks the same on macOS and Windows.

## Requirements

- Java 21 or newer (the team uses Eclipse's bundled Java 25 and OpenJDK 26 in IntelliJ).
- Eclipse or IntelliJ IDEA. JUnit 5 is used for tests and is included with both IDEs.

## Running the app

- **Eclipse:** open the project, then run `src/campingChecklist/Main.java` (Run As → Java Application).
- **IntelliJ:** open the project folder, then run `Main`.

Trips are saved to `trips.dat` in the project folder when you click **Save**.

## Running the tests

- **Eclipse:** right-click the `test` folder → Run As → JUnit Test.
- **IntelliJ:** right-click the `test` folder → Run 'All Tests'.

If Eclipse reports errors after files were changed outside it (for example by `git pull`), refresh the project (F5) and
run Project → Clean.

## Project structure

| Class               | Responsibility                                                             |
|---------------------|----------------------------------------------------------------------------|
| `Main`              | Loads saved trips and opens the window                                     |
| `TripPlannerGUI`    | Swing interface: trip list, trip details, Packing List and Notes tabs      |
| `TripPlanner`       | Manages all trips and their items and notes; tracks unsaved changes        |
| `TripFileManager`   | Saves and loads `trips.dat` (files from the previous format still load)    |
| `Trip`              | One trip: name, destination, dates, packing items, note sections, progress |
| `PackingItem`       | One checklist item: name, category, quantity, packed status                |
| `NoteSection`       | A titled section of trip notes                                             |
| `GoogleMapsService` | Builds Google Maps links for a destination                                 |

Source code is in `src/`, and JUnit tests are in `test/`.

## Google Maps

"View on Google Maps" opens the destination in your browser with no setup.
The app can also show a map image inside the app using a Google Maps Static API
key set in the `GOOGLE_MAPS_API_KEY` environment variable. The team has not set
up a key yet; setup instructions will be added if we do.

  - **macOS:** `export GOOGLE_MAPS_API_KEY=your-key` in a terminal only affects programs started from that terminal.
  - **Windows PowerShell:** `setx GOOGLE_MAPS_API_KEY "your-key"`, then restart the IDE.

## Team

- Jared Saltou
- Lia Day
- Colby Almond

All team members contribute to project design and review.

## Status

The core features are implemented and working in Eclipse and IntelliJ, with 112 passing JUnit tests.

Next, the team is polishing the GUI and adding enhancements that don't require network calls or save file changes.
Candidates include a calendar date picker, a category manager, sorting the packing list by column, check all / uncheck
all, printing the checklist, a trip countdown, and progress per category.
