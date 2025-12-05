package frontend;

public interface Navigation {
    void showLogin();
    void showMemberHome();
    void showAdminHome();
    void showMediaDetails(String title);
    void showAccount();
    void showWatchHistory();
    void showBrowseSearch();
    void logout(int member_id);

    // Track current logged-in username
    void setCurrentUsername(String username);
    String getCurrentUsername();
}
