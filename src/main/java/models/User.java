package models;

/**
 * User model for authentication and profile management.
 */
public class User {

    private String id;
    private String fullName;
    private String email;
    private String password;      // In production this would be hashed
    private String role;          // "USER" or "ADMIN"
    private String avatarInitials;
    private String joinDate;
    private int    totalBookings;
    private String memberTier;    // "Silver", "Gold", "Platinum"

    public User(String id, String fullName, String email,
                String password, String role) {
        this.id             = id;
        this.fullName       = fullName;
        this.email          = email;
        this.password       = password;
        this.role           = role;
        this.joinDate       = "2024-01-15";
        this.totalBookings  = 0;
        this.memberTier     = "Silver";
        // Generate initials automatically
        String[] parts = fullName.trim().split("\\s+");
        this.avatarInitials = parts.length >= 2
            ? String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0)
            : fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
    }

    // ---- Getters / Setters ----
    public String getId()              { return id; }
    public void   setId(String id)     { this.id = id; }

    public String getFullName()           { return fullName; }
    public void   setFullName(String v)   { this.fullName = v; }

    public String getEmail()              { return email; }
    public void   setEmail(String v)      { this.email = v; }

    public String getPassword()           { return password; }
    public void   setPassword(String v)   { this.password = v; }

    public String getRole()               { return role; }
    public void   setRole(String v)       { this.role = v; }

    public String getAvatarInitials()     { return avatarInitials; }

    public String getJoinDate()           { return joinDate; }
    public void   setJoinDate(String v)   { this.joinDate = v; }

    public int  getTotalBookings()        { return totalBookings; }
    public void setTotalBookings(int v)   { this.totalBookings = v; }

    public String getMemberTier()         { return memberTier; }
    public void   setMemberTier(String v) { this.memberTier = v; }

    public boolean isAdmin()              { return "ADMIN".equalsIgnoreCase(role); }

    @Override
    public String toString() {
        return fullName + " <" + email + ">";
    }
}
