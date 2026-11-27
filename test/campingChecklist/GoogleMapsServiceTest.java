package campingChecklist;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import org.junit.jupiter.api.Test;

/**
 * Tests the Google Maps links built for trip locations.
 * These tests make no network requests.
 *
 * @author Colby Almond
 */
class GoogleMapsServiceTest {

    @Test
    void searchUriEncodesLocationAndNeedsNoKey() {
        GoogleMapsService maps = new GoogleMapsService(null);

        URI uri = maps.getSearchUri(" Zion National Park, UT & Co ");

        assertEquals(
                "https://www.google.com/maps/search/?api=1"
                        + "&query=Zion+National+Park%2C+UT+%26+Co",
                uri.toString());
    }

    @Test
    void blankKeyCountsAsNoKey() {
        GoogleMapsService maps = new GoogleMapsService("   ");

        assertFalse(maps.hasApiKey());
        assertThrows(
                IllegalStateException.class,
                () -> maps.getStaticMapUri("Moab"));
    }

    @Test
    void staticMapUriCentersAndMarksLocationWithKey() {
        GoogleMapsService maps = new GoogleMapsService("test-key");

        String uri = maps.getStaticMapUri("Moab, UT").toString();

        assertTrue(maps.hasApiKey());
        assertTrue(uri.startsWith(
                "https://maps.googleapis.com/maps/api/staticmap?"));
        assertTrue(uri.contains("center=Moab%2C+UT"));
        assertTrue(uri.contains("markers=color:red%7CMoab%2C+UT"));
        assertTrue(uri.endsWith("&key=test-key"));
    }
}
