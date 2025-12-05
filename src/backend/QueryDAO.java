package backend;
import java.sql.*;
import java.util.*;
import java.util.ArrayList;

// Runs SQL queries for search, trending, and watch history.
public class QueryDAO {

    // Search Media by keyword
    public List<Media> searchMedia(String keyword, String filter) {
        List<Media> results = new ArrayList<>();

        String normFilter = (filter == null) ? "All" : filter;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        String like = hasKeyword ? ("%" + keyword.trim() + "%") : null;

        String sql;
        int paramCount = 0;

        // If no keyword, some filters just list everything
        if (!hasKeyword) {
            switch (normFilter) {
                case "Movies":
                    sql = "SELECT DISTINCT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                            "FROM Media m JOIN Movie mv ON m.media_ID = mv.media_ID " +
                            "ORDER BY m.release_date DESC LIMIT 100";
                    break;
                case "Series":
                    sql = "SELECT DISTINCT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                            "FROM Media m JOIN Series s ON m.media_ID = s.media_ID " +
                            "ORDER BY m.release_date DESC LIMIT 100";
                    break;
                case "Actor":
                case "Director":
                case "Genre":
                case "Sequel":
                    // These need a keyword (name/genre), so return empty
                    return results;
                case "All":
                default:
                    sql = "SELECT DISTINCT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link FROM Media m " +
                            "ORDER BY m.release_date DESC LIMIT 100";
                    break;
            }
        } else {
            switch (normFilter) {
                case "Actor":
                    sql = "SELECT DISTINCT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                            "FROM Media m " +
                            "JOIN Acts a ON m.media_ID = a.media_ID " +
                            "JOIN Actor_actress act ON a.ID = act.ID " +
                            "WHERE act.actor_name LIKE ?";
                    paramCount = 1;
                    break;
                case "Director":
                    sql = "SELECT DISTINCT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                            "FROM Media m " +
                            "JOIN Directs d ON m.media_ID = d.media_ID " +
                            "JOIN Director dir ON d.ID = dir.ID " +
                            "WHERE dir.director_name LIKE ?";
                    paramCount = 1;
                    break;
                case "Genre":
                    sql = "SELECT DISTINCT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link FROM Media m WHERE m.genre LIKE ?";
                    paramCount = 1;
                    break;
                case "Sequel":
                    sql = "SELECT DISTINCT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                            "FROM Media m " +
                            "JOIN Movie mv ON m.media_ID = mv.media_ID " +
                            "JOIN Sequel s ON mv.media_ID = s.movie1_ID " +
                            "WHERE m.title LIKE ?";
                    paramCount = 1;
                    break;
                case "Movies":
                    sql = "SELECT DISTINCT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                            "FROM Media m JOIN Movie mv ON m.media_ID = mv.media_ID " +
                            "WHERE m.title LIKE ?";
                    paramCount = 1;
                    break;
                case "Series":
                    sql = "SELECT DISTINCT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                            "FROM Media m JOIN Series s ON m.media_ID = s.media_ID " +
                            "WHERE m.title LIKE ?";
                    paramCount = 1;
                    break;
                case "All":
                default:
                    // Default: title search across all media
                    sql = "SELECT DISTINCT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link FROM Media m WHERE m.title LIKE ?";
                    paramCount = 1;
                    break;
            }
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (paramCount == 1) {
                ps.setString(1, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapMedia(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }


    // Admin: search member streaming by username or media title.
    public List<String[]> searchMemberStreaming(String query) {
        List<String[]> rows = new ArrayList<>();
        boolean showAll = (query == null || query.trim().isEmpty());
        String q = showAll ? null : ("%" + query.trim() + "%");
        String base = "SELECT mem.username, m.title, MAX(wh.watch_date) AS last_watch " +
                "FROM Watch_History wh " +
                "JOIN Member mem ON mem.ID = wh.member_id " +
                "JOIN Media m ON m.media_ID = wh.media_id ";
        String where = showAll ? "" : "WHERE mem.username LIKE ? OR m.title LIKE ? ";
        String sql = base + where +
                "GROUP BY mem.username, m.title " +
                "ORDER BY last_watch DESC LIMIT 500";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!showAll) {
                ps.setString(1, q);
                ps.setString(2, q);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String user = rs.getString(1);
                    String title = rs.getString(2);
                    String when = null;
                    try { when = rs.getString(3); } catch (SQLException ignored) {}
                    rows.add(new String[]{user, title, when});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    // Admin: detailed watch rows for one username
    public List<String[]> getWatchHistoryRowsByUsername(String username) {
        List<String[]> rows = new ArrayList<>();
        if (username == null || username.isEmpty()) return rows;
        String memberIdSql = "SELECT ID AS member_id FROM Member WHERE username = ?";
        String historySql = "SELECT m.title, wh.watch_date, m.IMBD_link FROM Watch_History wh " +
                "JOIN Media m ON m.media_ID = wh.media_id WHERE wh.member_id = ? ORDER BY wh.watch_date DESC LIMIT 500";
        try (Connection conn = DBConnection.getConnection()) {
            Integer memberId = null;
            try (PreparedStatement ps = conn.prepareStatement(memberIdSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) memberId = rs.getInt("member_id");
                }
            }
            if (memberId == null) return rows;
            try (PreparedStatement ps = conn.prepareStatement(historySql)) {
                ps.setInt(1, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3)});
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    // Find one media row by exact title
    public Media getMediaByTitle(String title) {
        if (title == null || title.isEmpty()) return null;
        String sql = "SELECT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link FROM Media m WHERE m.title = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapMedia(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Details for one media- adds cast and director
    public Media getMediaDetailsByTitle(String title) {
        Media base = getMediaByTitle(title);
        if (base == null) return null;

        try (Connection conn = DBConnection.getConnection()) {
            // Directors
            String dirSql = "SELECT dir.director_name FROM Directs d JOIN Director dir ON d.ID = dir.ID WHERE d.media_ID = ?";
            java.util.List<String> directors = new java.util.ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(dirSql)) {
                ps.setString(1, base.getMediaIdRaw() != null ? base.getMediaIdRaw() : String.format("M%03d", base.getMediaID()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) directors.add(rs.getString(1));
                }
            }
            base.setDirectors(String.join(", ", directors));

            // Cast (actors)
            String castSql = "SELECT act.actor_name FROM Acts a JOIN Actor_actress act ON a.ID = act.ID WHERE a.media_ID = ?";
            java.util.List<String> actors = new java.util.ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(castSql)) {
                ps.setString(1, base.getMediaIdRaw() != null ? base.getMediaIdRaw() : String.format("M%03d", base.getMediaID()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) actors.add(rs.getString(1));
                }
            }
            base.setCast(String.join(", ", actors));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return base;
    }

    // Turn a ResultSet row into a Media object
    private Media mapMedia(ResultSet rs) throws SQLException {
        int numericId = 0;
        String idStr = null;
        try {
            idStr = rs.getString("media_ID");
            if (idStr != null) {
                String digits = idStr.replaceAll("\\D+", "");
                if (!digits.isEmpty()) {
                    try { numericId = Integer.parseInt(digits); } catch (NumberFormatException ignored) { numericId = 0; }
                }
            }
        } catch (SQLException ignored) {
        }

        Media m = new Media(
                numericId,
                rs.getString("title"),
                rs.getString("genre"),
                rs.getString("release_date")
        );
        // Save  id string for watch history
        try { m.setMediaIdRaw(idStr); } catch (Exception ignored) {}
        // Map IMDb link if present (column is IMBD_link in DB)
        try {
            String link = rs.getString("IMBD_link");
            if (link != null) m.setImdbLink(link);
        } catch (SQLException ignored) {}
        return m;
    }

    // --- Watch history helpers ---

    // Get ID by username
    public int getMemberIdByUsername(String username) {
        if (username == null || username.isEmpty()) return -1;
        String sql = "SELECT ID FROM Member WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // return -1 if not found
    }

    // Insert a row into Watch_History with NOW()
    public void addMediaToWatchHistory(int memberID, String mediaID) throws SQLException {
        String sql = "INSERT INTO Watch_History(member_id, media_id, watch_date) VALUES (?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberID);
            ps.setString(2, mediaID);
            ps.execute();
        }
    }

    // Trending by watch count; if that fails/empty, use most recent releases
    public List<Media> getTrendingMedia(int limit) {
        List<Media> results = new ArrayList<>();
        if (limit <= 0) limit = 12;

        // Primary: order by number of watches
        String sqlByWatches = "SELECT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                "FROM Media m " +
                "JOIN Watch_History wh ON m.media_ID = wh.media_id " +
                "GROUP BY m.media_ID, m.title, m.genre, m.release_date " +
                "ORDER BY COUNT(*) DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlByWatches)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapMedia(rs));
                }
            }
        } catch (SQLException e) {
            // Likely missing table/permissions; fall back silently to recent
        }

        if (!results.isEmpty()) return results;

        // Fallback: most recent media by release_date
        String sqlRecent = "SELECT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link FROM Media m " +
                "ORDER BY m.release_date DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlRecent)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapMedia(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    // Top media watched in the last 30 days by count
    public List<Media> getTopMediaThisMonth(int limit) {
        List<Media> results = new ArrayList<>();
        if (limit <= 0) limit = 10;
        String sql = "SELECT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                "FROM Watch_History wh JOIN Media m ON m.media_ID = wh.media_id " +
                "WHERE wh.watch_date >= (NOW() - INTERVAL 30 DAY) " +
                "GROUP BY m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                "ORDER BY COUNT(*) DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapMedia(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    // Top media watched in the last 24 hours by count
    public List<Media> getTrendingLast24h(int limit) {
        List<Media> results = new ArrayList<>();
        if (limit <= 0) limit = 10;
        String sql = "SELECT m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                "FROM Watch_History wh JOIN Media m ON m.media_ID = wh.media_id " +
                "WHERE wh.watch_date >= (NOW() - INTERVAL 1 DAY) " +
                "GROUP BY m.media_ID, m.title, m.genre, m.release_date, m.IMBD_link " +
                "ORDER BY COUNT(*) DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapMedia(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    // Recent activity for Admin home (latest watches)
    public List<String[]> getRecentActivity(int limit) {
        List<String[]> rows = new ArrayList<>();
        if (limit <= 0) limit = 50;
        String sql = "SELECT mem.username, m.title, wh.watch_date " +
                "FROM Watch_History wh " +
                "JOIN Member mem ON mem.ID = wh.member_id " +
                "JOIN Media m ON m.media_ID = wh.media_id " +
                "ORDER BY wh.watch_date DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String user = rs.getString(1);
                    String title = rs.getString(2);
                    String when = rs.getString(3);
                    rows.add(new String[]{"Watch", title + " by " + user, "Watched", when});
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return rows;
    }

    // --- Admin CRUD: Media ---
    public String createMedia(String title, String genre, String releaseDate, String type, String imdbLink) throws SQLException {
        String mediaId = nextMediaId(type);
        String insertMedia = "INSERT INTO Media(media_ID, title, genre, release_date, IMBD_link) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertMedia)) {
                ps.setString(1, mediaId);
                ps.setString(2, title);
                ps.setString(3, genre);
                ps.setString(4, releaseDate);
                ps.setString(5, imdbLink);
                ps.executeUpdate();
            }
            String table = "Movie".equalsIgnoreCase(type) ? "Movie" : "Series";
            String insertType = "INSERT INTO " + table + "(media_ID) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(insertType)) {
                ps.setString(1, mediaId);
                ps.executeUpdate();
            }
            conn.commit();
        }
        return mediaId;
    }

    public boolean removeMediaByIdOrTitle(String idOrTitle) {
        if (idOrTitle == null || idOrTitle.isEmpty()) return false;
        String findSql = "SELECT media_ID FROM Media WHERE media_ID = ? OR title = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection()) {
            String mid = null;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setString(1, idOrTitle);
                ps.setString(2, idOrTitle);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) mid = rs.getString(1); }
            }
            if (mid == null) return false;
            conn.setAutoCommit(false);
            // Delete dependent rows in typical child tables if exist
            deleteIfExists(conn, "DELETE FROM Acts WHERE media_ID = ?", mid);
            deleteIfExists(conn, "DELETE FROM Directs WHERE media_ID = ?", mid);
            deleteIfExists(conn, "DELETE FROM Watch_History WHERE media_id = ?", mid);
            deleteIfExists(conn, "DELETE FROM Sequel WHERE movie1_ID = ? OR movie2_ID = ?", mid, mid);
            deleteIfExists(conn, "DELETE FROM Movie WHERE media_ID = ?", mid);
            deleteIfExists(conn, "DELETE FROM Series WHERE media_ID = ?", mid);
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Media WHERE media_ID = ?")) {
                ps.setString(1, mid);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addMediaToSession(int member_id, String media_id){
        String sql = """
                INSERT INTO Stream_session(member_id, media_id) VALUES (?, ?)
                """;
        try(Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1,member_id);
            ps.setString(2,media_id);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void logout(int member_id){
        String sql = """
            UPDATE Stream_session SET active_flag = active_flag-1
            WHERE member_id = ?;
        """;
        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1,member_id);
            ps.executeUpdate();

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void deleteIfExists(Connection conn, String sql, String... args) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setString(i + 1, args[i]);
            ps.executeUpdate();
        }
    }

    private String nextMediaId(String type) throws SQLException {
        boolean movie = "Movie".equalsIgnoreCase(type);
        try (Connection conn = DBConnection.getConnection()) {
            if (movie) {
                // Movies use numeric-only media_ID. Find the max numeric ID and increment.
                String sql = "SELECT MAX(CAST(media_ID AS UNSIGNED)) FROM Media WHERE media_ID REGEXP '^[0-9]+$'";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    int next = 1;
                    if (rs.next()) {
                        int max = rs.getInt(1);
                        if (!rs.wasNull()) next = max + 1; else next = 1;
                    }
                    return String.valueOf(next);
                }
            } else {
                // Series use S### format.
                String sql = "SELECT MAX(CAST(SUBSTRING(media_ID, 2) AS UNSIGNED)) FROM Media WHERE media_ID LIKE 'S%'";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    int next = 1;
                    if (rs.next()) {
                        int max = rs.getInt(1);
                        if (!rs.wasNull()) next = max + 1; else next = 1;
                    }
                    return String.format("S%03d", next);
                }
            }
        }
    }

    
}
