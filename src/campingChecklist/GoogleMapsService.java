package campingChecklist;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.imageio.ImageIO;

/**
 * Looks up trip locations with Google Maps.
 *
 * <p>Opening a location in Google Maps needs no API key. Downloading a
 * map image uses the Google Maps Static API and needs a key from the
 * GOOGLE_MAPS_API_KEY environment variable.</p>
 *
 * @author Colby Almond
 */
public class GoogleMapsService {

    /** Environment variable that holds the Google Maps API key. */
    public static final String API_KEY_VARIABLE = "GOOGLE_MAPS_API_KEY";

    private static final String SEARCH_URL =
            "https://www.google.com/maps/search/?api=1&query=";

    private static final String STATIC_MAP_URL =
            "https://maps.googleapis.com/maps/api/staticmap";

    private final String apiKey;

    /**
     * Creates a service using the key in GOOGLE_MAPS_API_KEY, if any.
     */
    public GoogleMapsService() {
        this(System.getenv(API_KEY_VARIABLE));
    }

    /**
     * Creates a service using the specified key.
     *
     * @param apiKey the Google Maps API key, or null or blank for none
     */
	public GoogleMapsService(String apiKey) {
        this.apiKey = apiKey == null || apiKey.isBlank()
                ? null
                : apiKey.trim();
    }

    /**
     * Returns whether map images can be downloaded.
     *
     * @return true if an API key is configured
     */
    public boolean hasApiKey() {
        return apiKey != null;
    }

    /**
     * Returns the Google Maps page that searches for a location.
     *
     * @param location the place to search for
     * @return the Google Maps search URI
     * @throws NullPointerException if location is null
     */
    public URI getSearchUri(String location) {
        return URI.create(SEARCH_URL + encode(location));
    }

    /**
     * Returns the Static API request for a map image centered on a
     * location with a marker on it.
     *
     * @param location the place to show
     * @return the Static API URI, including the API key
     * @throws NullPointerException if location is null
     * @throws IllegalStateException if no API key is configured
     */
    public URI getStaticMapUri(String location) {
        if (apiKey == null) {
            throw new IllegalStateException(
                    "Set " + API_KEY_VARIABLE + " to download map images.");
        }

        String place = encode(location);

        return URI.create(STATIC_MAP_URL
                + "?center=" + place
                + "&zoom=10&size=600x400"
                + "&markers=color:red%7C" + place
                + "&key=" + encode(apiKey));
    }

    /**
     * Downloads a map image of a location from the Static API.
     *
     * <p>This makes a network request, so GUI callers should run it
     * off the Swing event thread.</p>
     *
     * @param location the place to show
     * @return the map image
     * @throws IllegalStateException if no API key is configured
     * @throws IOException if the request fails or Google returns an error
     */
    public BufferedImage fetchStaticMap(String location) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)
                getStaticMapUri(location).toURL().openConnection();

        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);

        try {
            int status = connection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
                // The error body explains the problem; the URL is left
                // out of the message so the API key is not displayed.
                throw new IOException("Google Maps returned HTTP "
                        + status + ": " + readError(connection));
            }

            try (InputStream input = connection.getInputStream()) {
                BufferedImage image = ImageIO.read(input);

                if (image == null) {
                    throw new IOException(
                            "Google Maps did not return an image.");
                }

                return image;
            }
        } finally {
            connection.disconnect();
        }
    }

    private static String readError(HttpURLConnection connection)
            throws IOException {

        try (InputStream error = connection.getErrorStream()) {
            if (error == null) {
                return "no details";
            }

            return new String(error.readAllBytes(),
                    StandardCharsets.UTF_8).trim();
        }
    }

    private static String encode(String value) {
        Objects.requireNonNull(value, "Location cannot be null.");
        return URLEncoder.encode(value.trim(), StandardCharsets.UTF_8);
    }
}
