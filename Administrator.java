public class Administrator extends User {

    public Administrator(int id, String username, String password, String email, String phoneNumber, String userId) {
        // Call the superclass (User) constructor, explicitly setting the role
        super(id, username, password, email, phoneNumber, userId, "Administrator");
    }
} 