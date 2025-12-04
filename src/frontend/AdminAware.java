package frontend;

// Tiny interface to let NavBar and pages know if the current user is admin.
public interface AdminAware {
    boolean isAdmin();
    void setAdmin(boolean admin);
}
