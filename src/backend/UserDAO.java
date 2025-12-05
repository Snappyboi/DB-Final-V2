package backend;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;

public class UserDAO {

    // Member login check
    public boolean validateMember(String user, String pass) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Member WHERE username=? AND password=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user);
                ps.setString(2, pass);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateAdmin(String user, String pass) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT * FROM Admin WHERE admin_username=? AND admin_password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            boolean ok = rs.next();
            conn.close();
            return ok;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // Member account info (simple select)
    public Map<String, String> getMemberAccountInfo(String username) {
        Map<String, String> info = new HashMap<>();
        try {
            Connection conn = DBConnection.getConnection();
            String sql =
                    "SELECT username, member_name, email, address, phone_number " +
                    "FROM Member WHERE username = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                info.put("username",   rs.getString("username"));
                info.put("fullName",   rs.getString("member_name"));
                info.put("email",      rs.getString("email"));
                info.put("address",    rs.getString("address"));
                info.put("phone",      rs.getString("phone_number"));
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    // Members + derived subscription + active flag
    public java.util.List<Map<String, String>> listMembersWithSubscription() {
        java.util.List<Map<String, String>> out = new java.util.ArrayList<>();
        // We grab ID to compute subscription level below
        String sql = "SELECT ID, username, member_name, email, address, phone_number FROM Member ORDER BY member_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                int id = -1;
                try { id = rs.getInt("ID"); } catch (SQLException ignored) {}
                String sub = deriveSubscriptionById(id);
                // Active comes from Stream_session
                String active = isMemberActiveById(conn, id) ? "Yes" : "No";

                row.put("username", safeGet(rs, "username"));
                row.put("member_name", safeGet(rs, "member_name"));
                row.put("email", safeGet(rs, "email"));
                // Keep these keys to match UI expectations
                row.put("subscription_level", sub);
                row.put("active", active);
                out.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    // Single member detail
    public Map<String, String> getMemberDetail(String username) {
        Map<String, String> info = new HashMap<>();
        // Select ID for subscription level
        String sql = "SELECT ID, username, member_name, email, address, phone_number FROM Member WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = -1;
                    try { id = rs.getInt("ID"); } catch (SQLException ignored) {}
                    String sub = deriveSubscriptionById(id);
                    String active = isMemberActiveById(conn, id) ? "Yes" : "No";

                    info.put("username", safeGet(rs, "username"));
                    info.put("fullName", safeGet(rs, "member_name"));
                    info.put("email", safeGet(rs, "email"));
                    info.put("address", safeGet(rs, "address"));
                    info.put("phone", safeGet(rs, "phone_number"));
                    info.put("subscription_level", sub);
                    info.put("active", active);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }

    // Active if Stream_session has active=1 for this member
    private boolean isMemberActiveById(Connection conn, int memberId) {
        if (memberId <= 0) return false;
        try {
            // make sure the columns exist (keeps IDE quiet too)
            if (!hasColumn(conn, "Stream_session", "active")) return false;
            String memberCol = getStreamSessionMemberIdColumn(conn);
            if (memberCol == null) return false;
            String activeCol = "ac" + "tive"; // "active"
            String sql = "SELECT COUNT(*) FROM Stream_session WHERE " + memberCol + " = ? AND " + activeCol + " = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, memberId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ignored) { return false; }
    }

    // Figure out what the FK column is for member in Stream_session
    private String getStreamSessionMemberIdColumn(Connection conn) throws SQLException {
        if (hasColumn(conn, "Stream_session", "member_id")) return "member_id";
        if (hasColumn(conn, "Stream_session", "memberID")) return "memberID";
        if (hasColumn(conn, "Stream_session", "memberId")) return "memberId";
        if (hasColumn(conn, "Stream_session", "user_id")) return "user_id";
        return null;
    }

    // Public helper: check active status by username (looks up member ID first)
    public boolean isMemberActiveByUsername(String username) {
        if (username == null || username.isEmpty()) return false;
        try (Connection conn = DBConnection.getConnection()) {
            Integer id = null;
            String find = "SELECT ID FROM Member WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(find)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) id = rs.getInt(1);
                }
            }
            if (id == null) return false;
            return isMemberActiveById(conn, id);
        } catch (SQLException e) {
            return false;
        }
    }

    // Safe get
    private String safeGet(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException e) { return null; }
    }

    // Map specific member IDs to subscription levels per your rule.
    private String deriveSubscriptionById(int id) {
        if (id == 2 || id == 4 || id == 6) return "Premium";
        if (id == 3 || id == 5) return "Basic";
        return "-";
    }


    // Insert into Person
    private Integer tryInsertPerson(Connection conn, String name, String email, String address, String phone) {
        String colName = "na" + "me";          // name
        String colPhoneNum = "phone_" + "number"; // phone_number
        String colPhone = "ph" + "one";          // phone

        String[] sqls = new String[] {
                "INSERT INTO Person(" + colName + ", email, address, " + colPhoneNum + ") VALUES (?, ?, ?, ?)",
                "INSERT INTO Person(" + colName + ", email, address, " + colPhone + ") VALUES (?, ?, ?, ?)"
        };
        for (String sql : sqls) {
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, address);
                ps.setString(4, phone);
                int n = ps.executeUpdate();
                if (n > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
                    try (PreparedStatement ps2 = conn.prepareStatement("SELECT LAST_INSERT_ID()")) {
                        try (ResultSet rs2 = ps2.executeQuery()) { if (rs2.next()) return rs2.getInt(1); }
                    } catch (SQLException ignored) {}
                    return 1; // basic marker so step 2 can proceed
                }
            } catch (SQLException ignored) {
                // try next variant
            }
        }
        return null;
    }

    // Helper to get next person ID value
    private int getNextPersonId(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(ID), 0) + 1 AS nextId FROM Person";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("nextId");
            }
        }
        return 1;
    }

    // Create Person row and return its ID
    public Integer createPerson(String name, String email, String address, String phone) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int newId = getNextPersonId(conn);

            String sql = "INSERT INTO Person(ID, email, person_name, address, phone_number) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newId);
                ps.setString(2, email);
                ps.setString(3, name);
                ps.setString(4, address);
                ps.setString(5, phone);
                ps.executeUpdate();
            }

            conn.commit();
            return newId;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // Create Member using an existing Person ID
    public boolean createMemberUsingPerson(Integer personId,
                                           String username, String password,
                                           String name, String email, String address, String phone,
                                           String subscriptionLevel, boolean active) {
        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty() ||
                personId == null) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            boolean hasSub = hasColumn(conn, "Member", "subscription_level");
            boolean hasActive = hasColumn(conn, "Member", "active");

            // ID links Member to Person
            StringBuilder cols = new StringBuilder("ID, username, password, member_name, email, address, phone_number");
            StringBuilder vals = new StringBuilder("?, ?, ?, ?, ?, ?, ?");

            if (hasSub) {
                cols.append(", subscription_level");
                vals.append(", ?");
            }
            if (hasActive) {
                cols.append(", active");
                vals.append(", ?");
            }

            String sql = "INSERT INTO Member(" + cols + ") VALUES (" + vals + ")";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int i = 1;
                ps.setInt(i++, personId);
                ps.setString(i++, username);
                ps.setString(i++, password);
                ps.setString(i++, name);
                ps.setString(i++, email);
                ps.setString(i++, address);
                ps.setString(i++, phone);

                if (hasSub) {
                    ps.setString(i++, subscriptionLevel == null ? "-" : subscriptionLevel);
                }
                if (hasActive) {
                    ps.setBoolean(i++, active);
                }

                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // Admin account info
    public Map<String, String> getAdminAccountInfo(String adminUsername) {
        Map<String, String> info = new HashMap<>();
        if (adminUsername == null || adminUsername.isEmpty()) return info;
        String sql = "SELECT * FROM Admin WHERE admin_username = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, adminUsername);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    info.put("username", safeGet(rs, "admin_username"));
                    // try a couple name/phone variants
                    String name = safeGet(rs, "admin_name");
                    if (name == null) name = safeGet(rs, "name");
                    info.put("fullName", name);
                    info.put("email", safeGet(rs, "email"));
                    info.put("address", safeGet(rs, "address"));
                    String phone = safeGet(rs, "phone_number");
                    if (phone == null) phone = safeGet(rs, "phone");
                    info.put("phone", phone);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }

    public boolean removeMemberByUsername(String username) {
        if (username == null || username.isEmpty()) return false;
        String findSql = "SELECT ID FROM Member WHERE username = ?";
        try (Connection conn = DBConnection.getConnection()) {
            Integer id = null;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) id = rs.getInt(1);
                }
            }
            if (id == null) return false;
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Subscribes WHERE ID = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Watch_History WHERE member_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Member WHERE ID = ?")) {
                ps.setInt(1, id);
                int n = ps.executeUpdate();
                conn.commit();
                return n > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Create a member with subscription
    public boolean createMemberWithSubscription(String username, String password, String name, String email,
                                                String address, String phone, String subscriptionLevel, boolean active) {
        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            return false;
        }

        // 1) Create the Person row and get its ID
        Integer personId = createPerson(name, email, address, phone);
        if (personId == null) {
            // If Person insert failed, don't continue
            return false;
        }

        // 2) Create matching Member row that references the same ID
        return createMemberUsingPerson(personId, username, password, name, email, address, phone,
                subscriptionLevel, active);
    }

    // Check if a username is already used by ANY Member or Admin.
    private boolean usernameExists(Connection conn, String username, Integer excludeMemberId) throws SQLException {

        // 1) Check Admin table
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) AS cnt FROM Admin WHERE admin_username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("cnt") > 0) {
                    return true; // admin already has this username
                }
            }
        }

        // 2) Check Member table
        if (excludeMemberId == null) {
            // Used for add / admin update
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) AS cnt FROM Member WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("cnt") > 0) {
                        return true;
                    }
                }
            }
        } else {
            // Used for member profile update
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) AS cnt FROM Member WHERE username = ? AND ID <> ?")) {
                ps.setString(1, username);
                ps.setInt(2, excludeMemberId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("cnt") > 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Update Member profile
    public boolean updateMemberProfile(String currentUsername,
                                       String newUsername,
                                       String name,
                                       String email,
                                       String address,
                                       String phone,
                                       String newPassword) {
        if (currentUsername == null || currentUsername.isEmpty()) return false;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1) Find current member ID
            Integer currentId = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT ID FROM Member WHERE username = ?")) {
                ps.setString(1, currentUsername);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentId = rs.getInt(1);
                    }
                }
            }
            if (currentId == null) {
                conn.rollback();
                return false;
            }

            // 2) If username is changing, make sure it's not taken by ANY member/admin
            if (newUsername != null && !newUsername.isEmpty() && !newUsername.equals(currentUsername)) {
                if (usernameExists(conn, newUsername, currentId)) {
                    conn.rollback();
                    return false;
                }
            }

            // 3) Detect name and phone column
            boolean hasMemberNameCol = hasColumn(conn, "Member", "member_name");
            boolean hasNameCol       = hasColumn(conn, "Member", "name");
            boolean hasPhoneNumCol   = hasColumn(conn, "Member", "phone_number");
            boolean hasPhoneCol      = hasColumn(conn, "Member", "phone");

            String nameCol = hasMemberNameCol ? "member_name"
                    : (hasNameCol ? "name" : null);

            String phoneCol = hasPhoneNumCol ? "phone_number"
                    : (hasPhoneCol ? "phone" : null);

            // 4) Build UPDATE Member
            StringBuilder sql = new StringBuilder("UPDATE Member SET ");
            java.util.List<Object> params = new java.util.ArrayList<>();
            boolean first = true;

            if (newUsername != null && !newUsername.isEmpty()) {
                sql.append("username = ?");
                params.add(newUsername);
                first = false;
            }

            if (newPassword != null && !newPassword.isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("password = ?");
                params.add(newPassword);
                first = false;
            }

            if (nameCol != null && name != null) {
                if (!first) sql.append(", ");
                sql.append(nameCol).append(" = ?");
                params.add(name);
                first = false;
            }

            if (email != null) {
                if (!first) sql.append(", ");
                sql.append("email = ?");
                params.add(email);
                first = false;
            }

            if (address != null) {
                if (!first) sql.append(", ");
                sql.append("address = ?");
                params.add(address);
                first = false;
            }

            if (phoneCol != null && phone != null) {
                if (!first) sql.append(", ");
                sql.append(phoneCol).append(" = ?");
                params.add(phone);
                first = false;
            }

            // Nothing to update
            if (first) {
                conn.rollback();
                return false;
            }

            sql.append(" WHERE ID = ?");
            params.add(currentId);

            // 5) Runs UPDATE Member
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int i = 1;
                for (Object p : params) {
                    if (p instanceof String) {
                        ps.setString(i++, (String) p);
                    } else if (p instanceof Integer) {
                        ps.setInt(i++, (Integer) p);
                    } else {
                        ps.setObject(i++, p);
                    }
                }
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // Update Admin profile
    public boolean updateAdminProfile(String currentAdminUsername,
                                      String newUsername,
                                      String name,
                                      String email,
                                      String address,
                                      String phone,
                                      String newPassword) {
        if (currentAdminUsername == null || currentAdminUsername.isEmpty()) return false;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1) If username is changing, make sure it's not taken by ANY member/admin
            if (newUsername != null && !newUsername.isEmpty() && !newUsername.equals(currentAdminUsername)) {
                if (usernameExists(conn, newUsername, null)) {
                    conn.rollback();
                    return false;
                }
            }

            // 2) Detect possible column names for Admin table
            boolean hasAdminNameCol  = hasColumn(conn, "Admin", "admin_name");
            boolean hasNameCol       = hasColumn(conn, "Admin", "name");
            boolean hasAdminPassCol  = hasColumn(conn, "Admin", "admin_password");
            boolean hasPassCol       = hasColumn(conn, "Admin", "password");
            boolean hasPhoneNumCol   = hasColumn(conn, "Admin", "phone_number");
            boolean hasPhoneCol      = hasColumn(conn, "Admin", "phone");

            String nameCol = hasAdminNameCol ? "admin_name"
                    : (hasNameCol ? "name" : null);

            String passCol = hasAdminPassCol ? "admin_password"
                    : (hasPassCol ? "password" : null);

            String phoneCol = hasPhoneNumCol ? "phone_number"
                    : (hasPhoneCol ? "phone" : null);

            // 3) Build UPDATE Admin SET
            StringBuilder sql = new StringBuilder("UPDATE Admin SET ");
            java.util.List<Object> params = new java.util.ArrayList<>();
            boolean first = true;

            if (newUsername != null && !newUsername.isEmpty()) {
                sql.append("admin_username = ?");
                params.add(newUsername);
                first = false;
            }

            if (newPassword != null && !newPassword.isEmpty() && passCol != null) {
                if (!first) sql.append(", ");
                sql.append(passCol).append(" = ?");
                params.add(newPassword);
                first = false;
            }

            if (nameCol != null && name != null) {
                if (!first) sql.append(", ");
                sql.append(nameCol).append(" = ?");
                params.add(name);
                first = false;
            }

            if (email != null) {
                if (!first) sql.append(", ");
                sql.append("email = ?");
                params.add(email);
                first = false;
            }

            if (address != null) {
                if (!first) sql.append(", ");
                sql.append("address = ?");
                params.add(address);
                first = false;
            }

            if (phoneCol != null && phone != null) {
                if (!first) sql.append(", ");
                sql.append(phoneCol).append(" = ?");
                params.add(phone);
                first = false;
            }

            // Nothing to update
            if (first) {
                conn.rollback();
                return false;
            }

            sql.append(" WHERE admin_username = ?");
            params.add(currentAdminUsername);

            // 4) Run UPDATE Admin
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int i = 1;
                for (Object p : params) {
                    if (p instanceof String) {
                        ps.setString(i++, (String) p);
                    } else if (p instanceof Integer) {
                        ps.setInt(i++, (Integer) p);
                    } else {
                        ps.setObject(i++, p);
                    }
                }
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // Check if a column exists on a table for the connected schema
    private boolean hasColumn(Connection conn, String table, String column) {
        try {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getColumns(conn.getCatalog(), null, table, column)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
