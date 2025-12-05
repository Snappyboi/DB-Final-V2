package frontend;

import frontend.components.GradientBackgroundPanel;

import javax.swing.*;
import java.awt.*;

import backend.BackendService;
import frontend.*;
import frontend.components.GradientBackgroundPanel;

// ACED Streaming UI entry point
public class app implements Navigation, AdminAware {
    private final JFrame frame;
    private final CardLayout cards;
    private final JPanel root;
    private String currentUsername;
    private boolean adminUser = false; // whether current user is admin

    // Page keys
    private static final String LOGIN = "login";
    private static final String MEMBER = "member";
    private static final String ADMIN = "admin";
    private static final String DETAILS = "details";
    private static final String ACCOUNT = "account";
    private static final String WATCH_HISTORY = "watch_history";
    private static final String BROWSE_SEARCH = "browse_search";
    private static final String ADMIN_ANALYTICS = "admin_analytics";
    private static final String ADMIN_MEMBER_STREAM = "admin_member_stream";
    private static final String ADMIN_SUBSCRIPTIONS = "admin_subscriptions";
    private static final String ADMIN_MEMBER_DETAIL = "admin_member_detail";
    private static final String ADMIN_ADD_PERSON = "admin_add_person";
    private static final String ADMIN_ADD_MEDIA = "admin_add_media";
    private static final String ADMIN_REMOVE_MEDIA = "admin_remove_media";
    private static final String ADMIN_ADD_MEMBER = "admin_add_member";
    private static final String ADMIN_REMOVE_MEMBER = "admin_remove_member";

    // Pages
    private LoginFrame loginPage;
    private MemberHomePage memberPage;
    private frontend.admin.AdminHomePage adminPage;
    private frontend.admin.AdminAnalyticsPage adminAnalyticsPanel;
    private frontend.admin.AdminMemberStreaming adminMemberStreamingPanel;
    private frontend.admin.AdminSubscriptionsPage adminSubscriptionsPage;
    private frontend.admin.AdminMemberDetailPage adminMemberDetailPage;
    private frontend.admin.AdminAddPersonPage adminAddPersonPage;
    private frontend.admin.AdminAddMediaPage adminAddMediaPage;
    private frontend.admin.AdminRemoveMediaPage adminRemoveMediaPage;
    private frontend.admin.AdminAddMemberPage adminAddMemberPage;
    private frontend.admin.AdminRemoveMemberPage adminRemoveMemberPage;
    private MediaDetailsPage detailsPage;
    private viewAccountFrame accountPage;
    private WatchHistoryPage watchHistoryPage;
    private searchFrame browseSearchPage;

    public app() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        frame = new JFrame("ACED Streaming");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 700));

        cards = new CardLayout();
        root = new GradientBackgroundPanel(cards);

        // Instantiate pages
        loginPage = new LoginFrame(this);
        memberPage = new MemberHomePage(this);
        adminPage = new frontend.admin.AdminHomePage(this);
        detailsPage = new MediaDetailsPage(this);
        accountPage = new viewAccountFrame(this);
        watchHistoryPage = new WatchHistoryPage(this);
        browseSearchPage = new searchFrame(this);
        adminAnalyticsPanel = new frontend.admin.AdminAnalyticsPage(this);
        adminMemberStreamingPanel = new frontend.admin.AdminMemberStreaming(this);
        adminSubscriptionsPage = new frontend.admin.AdminSubscriptionsPage(this);
        adminMemberDetailPage = new frontend.admin.AdminMemberDetailPage(this);
        adminAddPersonPage = new frontend.admin.AdminAddPersonPage(this);
        adminAddMediaPage = new frontend.admin.AdminAddMediaPage(this);
        adminRemoveMediaPage = new frontend.admin.AdminRemoveMediaPage(this);
        adminAddMemberPage = new frontend.admin.AdminAddMemberPage(this);
        adminRemoveMemberPage = new frontend.admin.AdminRemoveMemberPage(this);

        root.add(loginPage, LOGIN);
        root.add(memberPage, MEMBER);
        root.add(adminPage, ADMIN);
        root.add(detailsPage, DETAILS);
        root.add(accountPage, ACCOUNT);
        root.add(watchHistoryPage, WATCH_HISTORY);
        root.add(browseSearchPage, BROWSE_SEARCH);
        root.add(adminAnalyticsPanel, ADMIN_ANALYTICS);
        root.add(adminMemberStreamingPanel, ADMIN_MEMBER_STREAM);
        root.add(adminSubscriptionsPage, ADMIN_SUBSCRIPTIONS);
        root.add(adminMemberDetailPage, ADMIN_MEMBER_DETAIL);
        root.add(adminAddPersonPage, ADMIN_ADD_PERSON);
        root.add(adminAddMediaPage, ADMIN_ADD_MEDIA);
        root.add(adminRemoveMediaPage, ADMIN_REMOVE_MEDIA);
        root.add(adminAddMemberPage, ADMIN_ADD_MEMBER);
        root.add(adminRemoveMemberPage, ADMIN_REMOVE_MEMBER);

        frame.setContentPane(root);
        frame.setLocationRelativeTo(null);
        showLogin();
    }

    public void show() { frame.setVisible(true); }

    // Navigation implementation
    @Override public void showLogin() { cards.show(root, LOGIN); frame.getRootPane().setDefaultButton(loginPage.getDefaultButton()); }
    @Override public void showMemberHome() { cards.show(root, MEMBER); }
    @Override public void showAdminHome() {
        if (isAdmin()) {
            try { if (adminPage != null) adminPage.reload(); } catch (Exception ignored) {}
            cards.show(root, ADMIN);
        } else {
            cards.show(root, MEMBER);
        }
    }
    @Override public void showMediaDetails(String title) { detailsPage.setMedia(title); cards.show(root, DETAILS); }
    @Override public void showAccount() { try { if (accountPage != null) accountPage.refreshData(); } catch (Exception ignored) {} cards.show(root, ACCOUNT); }
    @Override public void showWatchHistory() { try { if (watchHistoryPage != null) watchHistoryPage.refresh(); } catch (Exception ignored) {} cards.show(root, WATCH_HISTORY); }
    @Override public void showBrowseSearch() { try { if (browseSearchPage != null) browseSearchPage.focusSearch(); } catch (Exception ignored) {} cards.show(root, BROWSE_SEARCH); }
    @Override public void logout(int member_id) {
        BackendService.logout(member_id);
        this.currentUsername = null; this.adminUser = false; showLogin();
    }

    // Track logged-in user
    @Override public void setCurrentUsername(String username) { this.currentUsername = username; }
    @Override public String getCurrentUsername() { return currentUsername; }

    // AdminAware
    @Override public boolean isAdmin() { return adminUser; }
    @Override public void setAdmin(boolean admin) { this.adminUser = admin; }

    // Internal helpers to navigate to admin panels from AdminHomePage
    public void showAdminAnalytics() { if (isAdmin()) { try { if (adminAnalyticsPanel != null) adminAnalyticsPanel.reload(); } catch (Exception ignored) {} cards.show(root, ADMIN_ANALYTICS); } }
    public void showAdminMemberStreaming() { if (isAdmin()) cards.show(root, ADMIN_MEMBER_STREAM); }
    public void showAdminSubscriptions() { if (isAdmin()) { try { if (adminSubscriptionsPage != null) adminSubscriptionsPage.reload(); } catch (Exception ignored) {} cards.show(root, ADMIN_SUBSCRIPTIONS); } }
    public void showAdminMemberDetail(String username) { if (isAdmin()) { try { if (adminMemberDetailPage != null) adminMemberDetailPage.showUser(username); } catch (Exception ignored) {} cards.show(root, ADMIN_MEMBER_DETAIL); } }
    public void showAdminAddPerson() { if (isAdmin()) cards.show(root, ADMIN_ADD_PERSON); }
    public void showAdminAddMedia() { if (isAdmin()) cards.show(root, ADMIN_ADD_MEDIA); }
    public void showAdminRemoveMedia() { if (isAdmin()) cards.show(root, ADMIN_REMOVE_MEDIA); }
    public void showAdminAddMember() { if (isAdmin()) cards.show(root, ADMIN_ADD_MEMBER); }
    public void showAdminRemoveMember() { if (isAdmin()) cards.show(root, ADMIN_REMOVE_MEMBER); }

    // Start step 2 of Add Member with Person data
    public void startAddMemberWithPerson(PersonData pd) {
        if (!isAdmin()) return;
        try {
            if (adminAddMemberPage != null) adminAddMemberPage.setPersonData(pd);
        } catch (Exception ignored) {}
        cards.show(root, ADMIN_ADD_MEMBER);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new app().show());
    }
}
