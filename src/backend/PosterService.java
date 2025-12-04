package backend;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * helper to turn an IMDb link into a poster URL via the OMDb API.
 * 
 */
public class PosterService {
    private static final Pattern IMDB_ID_PATTERN = Pattern.compile("(tt\\d+)");
    private static final Pattern POSTER_PATTERN = Pattern.compile("\"Poster\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
    private final String apiKey;

    public PosterService() {
        this.apiKey = System.getenv("OMDB_API_KEY");
    }

    /**
     * @param imdbLink full IMDb URL or ID
     * @return direct poster URL or null if unavailable
     */
    public String resolvePosterUrl(String imdbLink) {
        if (imdbLink == null || imdbLink.isBlank()) {
            return null;
        }
        String imdbId = extractImdbId(imdbLink);
        if (imdbId == null) {
            return null;
        }
        if (apiKey == null || apiKey.isBlank()) {
            // no API key configured; caller will use fallback artwork
            return null;
        }
        try {
            String endpoint = "https://www.omdbapi.com/?i=" + imdbId + "&apikey=" + apiKey + "&r=json";
            HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if (status >= 400) {
                return null;
            }

            try (InputStream in = connection.getInputStream()) {
                String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                return parsePosterFromBody(body);
            }
        } catch (Exception e) {
        
            e.printStackTrace();
            return null;
        }
    }

    private String extractImdbId(String imdbLink) {
        Matcher matcher = IMDB_ID_PATTERN.matcher(imdbLink);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String parsePosterFromBody(String body) {
        Matcher matcher = POSTER_PATTERN.matcher(body);
        if (matcher.find()) {
            String value = matcher.group(1);
            if (value == null || value.isBlank() || "N/A".equalsIgnoreCase(value)) {
                return null;
            }
            // OMDb adds a backslash before the regular slash "\/" so replace with "/"
            return value.replace("\\/", "/");
        }
        return null;
    }
}
