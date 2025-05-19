import javax.swing.*;
import java.awt.*;

public class RegistrationWindow extends JDialog {
    private final UserService userService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField userIdField;

    public RegistrationWindow(UserService userService) {
        super((Frame) null, "Register New User", true);
        this.userService = userService;
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        add(passwordField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        add(emailField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(20);
        add(phoneField, gbc);

        // User ID
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        userIdField = new JTextField(20);
        add(userIdField, gbc);

        // Register button
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> handleRegistration());
        add(registerButton, gbc);

        pack();
        setLocationRelativeTo(null);
    }

    private void handleRegistration() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String userId = userIdField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty() || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Registration Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User newUser = userService.createUser(username, password, email, phone, userId, "Customer");
        if (newUser != null) {
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Username or User ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
} 