public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String userId;   
    private String role;     

    public User(int id, String username, String password, String email, String phoneNumber, String userId, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userId = userId;
        this.role = role;
    }

    public User(String username, String password, String email, String phoneNumber, String userId) {
        this(-1, username, password, email, phoneNumber, userId, "Customer");
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return String.format("User: %s (ID: %s, Role: %s)", username, userId, role);
    }
} 