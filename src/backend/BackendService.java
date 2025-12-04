package backend;

import java.util.List;

public class BackendService {

    private static final UserDAO userDAO = new UserDAO();
    private static final QueryDAO queryDAO = new QueryDAO();

    // Authentication
    public static boolean loginMember(String username, String password) {
        return userDAO.validateMember(username, password);
    }

    public static boolean loginAdmin(String username, String password) {
        return userDAO.validateAdmin(username, password);
    }

    // Search Movies/Series
    public static List<Media> searchMedia(String keyword, String filter) { return queryDAO.searchMedia(keyword, filter); }
    public static List<Media> getTrendingMedia(int limit) { return queryDAO.getTrendingMedia(limit); }
    public static List<Media> getTopMediaThisMonth(int limit) { return queryDAO.getTopMediaThisMonth(limit); }
    public static List<Media> getTrendingLast24h(int limit) { return queryDAO.getTrendingLast24h(limit); }

    // Watch history
    public static void logWatch(String username, String mediaTitle) { queryDAO.addWatchHistory(username, mediaTitle); }
    public static java.util.List<Media> getWatchHistory(String username) { return queryDAO.getWatchHistoryByUsername(username); }

    // Account info
    public static java.util.Map<String, String> getMemberAccountInfo(String username) { return userDAO.getMemberAccountInfo(username); }

    // Admin: list members with subscription level and status
    public static java.util.List<java.util.Map<String, String>> listMembersWithSubscription() {
        return userDAO.listMembersWithSubscription();
    }

    // Admin: get full member detail (basic info + subscription fields)
    public static java.util.Map<String, String> getMemberDetail(String username) {
        return userDAO.getMemberDetail(username);
    }

    // Single media lookup (used for details/IMDb)
    public static Media getMediaByTitle(String title) { return queryDAO.getMediaByTitle(title); }
    // Media details (adds directors/cast strings)
    public static Media getMediaDetailsByTitle(String title) { return queryDAO.getMediaDetailsByTitle(title); }

    // Get ID by username
    public static int getMemberIdByUsername(String username) { return queryDAO.getMemberIdByUsername(username); } // -1 if not found
    //add streamed media to watch history
    public static void addMediaToWatchHistory(int memberID, String mediaId) throws Exception { queryDAO.addMediaToWatchHistory(memberID, mediaId); }

    // Admin: Member streaming search (by username or media title)
    public static java.util.List<String[]> searchMemberStreaming(String query) {
        return queryDAO.searchMemberStreaming(query);
    }

    // Admin: watch rows for a username (title, date)
    public static java.util.List<String[]> getWatchHistoryRowsByUsername(String username) {
        return queryDAO.getWatchHistoryRowsByUsername(username);
    }

    // Admin: Activity feed for Admin home
    public static java.util.List<String[]> getRecentActivity(int limit) { return queryDAO.getRecentActivity(limit); }

    // Admin: CRUD Media
    public static String createMedia(String title, String genre, String releaseDate, String type, String imdbLink) throws Exception {
        return queryDAO.createMedia(title, genre, releaseDate, type, imdbLink);
    }
    public static boolean removeMediaByIdOrTitle(String idOrTitle) { return queryDAO.removeMediaByIdOrTitle(idOrTitle); }

    // Admin: CRUD Member
    public static boolean createMember(String username, String password, String name, String email, String address, String phone) {
        return userDAO.createMember(username, password, name, email, address, phone);
    }
    public static boolean removeMemberByUsername(String username) { return userDAO.removeMemberByUsername(username); }

    // Admin: Create member with subscription (tries Person first; attempts to set subscription columns if present)
    public static boolean createMemberFull(String username, String password, String name, String email, String address, String phone,
                                           String subscriptionLevel, boolean active) {
        return userDAO.createMemberWithSubscription(username, password, name, email, address, phone, subscriptionLevel, active);
    }

    // Admin: Person/Member two-step helpers
    public static Integer createPerson(String name, String email, String address, String phone) {
        return userDAO.createPerson(name, email, address, phone);
    }
    public static boolean createMemberUsingPerson(Integer personId,
                                                  String username, String password,
                                                  String name, String email, String address, String phone,
                                                  String subscriptionLevel, boolean active) {
        return userDAO.createMemberUsingPerson(personId, username, password, name, email, address, phone, subscriptionLevel, active);
    }

    // Admin: admin personal info
    public static java.util.Map<String, String> getAdminAccountInfo(String adminUsername) { return userDAO.getAdminAccountInfo(adminUsername); }

    // Session/active helpers
    public static boolean isMemberActive(String username) { return userDAO.isMemberActiveByUsername(username); }
}
