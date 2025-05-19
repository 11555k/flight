import javax.swing.*;
import java.awt.*;

public class UpdateProfileWindow extends JDialog {
    private final UserService userService;
    private final User currentUser;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public UpdateProfileWindow(UserService userService, User currentUser) {
        super((Frame) null, "Update Profile", true);
        this.userService = userService;
        this.currentUser = currentUser;
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(currentUser.getEmail(), 20);
        add(emailField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(currentUser.getPhoneNumber(), 20);
        add(phoneField, gbc);

        // Current Password
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        currentPasswordField = new JPasswordField(20);
        add(currentPasswordField, gbc);

        // New Password
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        newPasswordField = new JPasswordField(20);
        add(newPasswordField, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        add(confirmPasswordField, gbc);

        // Update button
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton updateButton = new JButton("Update Profile");
        updateButton.addActionListener(e -> handleUpdate());
        add(updateButton, gbc);

        pack();
        setLocationRelativeTo(null);
    }

    private void handleUpdate() {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validate current password
        if (!currentPassword.isEmpty()) {
            User authenticatedUser = userService.authenticateUser(currentUser.getUsername(), currentPassword);
            if (authenticatedUser == null) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Update Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Validate new password if provided
        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Update Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.", "Update Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Update user information
        boolean success = userService.updateUserInfo(currentUser.getId(), email, phone);
        if (success) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile. Please try again.", "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }
} 