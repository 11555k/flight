import java.util.List;
import java.util.ArrayList;

/**
 * Service class responsible for user-related business logic.
 * Follows Single Responsibility Principle by focusing only on user management.
 */
public class UserService {
    private final DatabaseManager dbManager;

    public UserService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Creates a new user with the specified details.
     * @return The created User object or null if creation failed
     */
    public User createUser(String username, String password, String email, String phoneNumber, String userId, String role) {
        if (!isValidUserData(username, password, email, phoneNumber, userId)) {
            return null;
        }
        
        long generatedId = dbManager.registerUser(username, password, email, phoneNumber, userId, role);
        return generatedId > 0 ? new User((int)generatedId, username, password, email, phoneNumber, userId, role) : null;
    }

    /**
     * Authenticates a user with the given credentials.
     * @return The authenticated User object or null if authentication failed
     */
    public User authenticateUser(String username, String password) {
        return dbManager.loginUser(username, password);
    }

    /**
     * Updates user's contact information.
     * @return true if the update was successful
     */
    public boolean updateUserInfo(int userId, String email, String phoneNumber) {
        return dbManager.updateUser(userId, email, phoneNumber, null);
    }

    /**
     * Updates user's role.
     * @return true if the update was successful
     */
    public boolean updateUserRole(int userId, String newRole) {
        return dbManager.updateUserRole(userId, newRole);
    }

    /**
     * Deletes a user if they have no associated bookings.
     * @return true if the deletion was successful
     */
    public boolean deleteUser(int userId) {
        return dbManager.deleteUser(userId);
    }

    /**
     * Retrieves all users in the system.
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return dbManager.getAllUsers();
    }

    /**
     * Finds a user by their username.
     * @return The User object or null if not found
     */
    public User getUserByUsername(String username) {
        return dbManager.getUserByUsername(username);
    }

    /**
     * Finds a user by their ID.
     * @return The User object or null if not found
     */
    public User getUserById(int userId) {
        return dbManager.getUserById(userId);
    }

    // Private helper methods
    private boolean isValidUserData(String username, String password, String email, String phoneNumber, String userId) {
        return isNotEmpty(username) && 
               isNotEmpty(password) && 
               isNotEmpty(email) && 
               isNotEmpty(phoneNumber) && 
               isNotEmpty(userId);
    }

    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
} 