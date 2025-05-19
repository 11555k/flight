import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList; // To safely manage a list of windows
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FlightSystemGUI extends JFrame {
    // Main application components
    private BookingSystem system;
    private User loggedInUser;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // Authentication panel components
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    // Flight booking panel components
    private JList<String> flightList;
    private DefaultListModel<String> flightListModel;
    private JTextField bookingFlightIdField;
    private JTextField bookingNumSeatsField;
    private JButton displayButton;
    private JButton cancelButton;
    private JButton logoutButton;
    private JButton updateProfileButton;
    private JButton bookButton;

    // Flight management panel components
    private JTextField agentFlightNumberField;
    private JTextField agentOriginField;
    private JTextField agentDestinationField;
    private JTextField agentCapacityField;
    private JTable agentFlightTable;
    private DefaultTableModel agentFlightTableModel;

    // Admin panel components
    private JPanel adminPanel;
    private JLabel adminLabel;
    private JTextField manageUserIdField;

    // Agent panel components
    private JPanel agentPanel;
    private JLabel agentLabel;

    // Window management
    private List<Window> openUserWindows;

    public FlightSystemGUI() {
        system = new BookingSystem();
        loggedInUser = null;
        openUserWindows = new CopyOnWriteArrayList<>();

        // Initialize main window
        setTitle("Flight System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Handle database cleanup on application exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                system.getDbManager().close();
            }
        });

        // Initialize panel management
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Create and configure application panels
        JPanel loginPanel = createLoginPanel();
        JPanel flightPanel = createFlightPanel();
        adminPanel = createAdminPanel();
        agentPanel = createAgentPanel();

        // Add panels to card layout
        cardPanel.add(loginPanel, "Login");
        cardPanel.add(flightPanel, "Flight");
        cardPanel.add(adminPanel, "Admin");
        cardPanel.add(agentPanel, "Agent");

        add(cardPanel);
        cardLayout.show(cardPanel, "Login");
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        userField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        gbc.gridx = 0; gbc.gridy = 0; panel.add(userLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(userField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; panel.add(loginButton, gbc);
        gbc.gridy = 3; panel.add(registerButton, gbc);

        loginButton.addActionListener(e -> performLogin());
        registerButton.addActionListener(e -> openRegistrationWindow());

        return panel;
    }

    private void openRegistrationWindow() {
        RegistrationWindow registrationWindow = new RegistrationWindow(system.getUserService());
        registrationWindow.setVisible(true);
    }

    private JPanel createFlightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcFlight = new GridBagConstraints();
        gbcFlight.insets = new Insets(5, 5, 5, 5);
        gbcFlight.fill = GridBagConstraints.HORIZONTAL;

        // Configure flight list display
        gbcFlight.gridx = 0;
        gbcFlight.gridy = 0;
        gbcFlight.gridwidth = 2;
        gbcFlight.weightx = 1.0;
        gbcFlight.weighty = 1.0;
        gbcFlight.fill = GridBagConstraints.BOTH;
        flightListModel = new DefaultListModel<>();
        flightList = new JList<>(flightListModel);
        JScrollPane scrollPane = new JScrollPane(flightList);
        panel.add(scrollPane, gbcFlight);

        // Handle flight selection
        flightList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedFlightInfo = flightList.getSelectedValue();
                if (selectedFlightInfo != null) {
                    try {
                        String idPart = selectedFlightInfo.split(" - ")[0];
                        int flightId = Integer.parseInt(idPart.replace("ID: ", ""));
                        bookingFlightIdField.setText(String.valueOf(flightId));
                        bookingNumSeatsField.setText("");
                    } catch (NumberFormatException ex) {
                        bookingFlightIdField.setText("");
                    }
                }
            }
        });

        JLabel bookingFlightIdLabel = new JLabel("Flight ID:");
        gbcFlight.gridx = 0;
        gbcFlight.gridy = 1;
        gbcFlight.gridwidth = 1;
        gbcFlight.weightx = 0;
        gbcFlight.weighty = 0;
        gbcFlight.fill = GridBagConstraints.NONE; // Don't fill
        gbcFlight.anchor = GridBagConstraints.EAST; // Align to right
        panel.add(bookingFlightIdLabel, gbcFlight);

        bookingFlightIdField = new JTextField(10);
        bookingFlightIdField.setEditable(false); // Make read-only as it's set by list selection
        gbcFlight.gridx = 1;
        gbcFlight.gridy = 1;
        gbcFlight.weightx = 1.0; // Take remaining horizontal space
        gbcFlight.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally
        gbcFlight.anchor = GridBagConstraints.WEST; // Align to left
        panel.add(bookingFlightIdField, gbcFlight);

        JLabel bookingNumSeatsLabel = new JLabel("Number of Seats:");
        gbcFlight.gridx = 0;
        gbcFlight.gridy = 2;
        gbcFlight.weightx = 0;
        gbcFlight.fill = GridBagConstraints.NONE;
        gbcFlight.anchor = GridBagConstraints.EAST;
        panel.add(bookingNumSeatsLabel, gbcFlight);

        bookingNumSeatsField = new JTextField(10);
        gbcFlight.gridx = 1;
        gbcFlight.gridy = 2;
        gbcFlight.weightx = 1.0;
        gbcFlight.fill = GridBagConstraints.HORIZONTAL;
        gbcFlight.anchor = GridBagConstraints.WEST;
        panel.add(bookingNumSeatsField, gbcFlight);

        bookButton = new JButton("Book Flight");
        gbcFlight.gridx = 0;
        gbcFlight.gridy = 3;
        gbcFlight.gridwidth = 2;
        gbcFlight.anchor = GridBagConstraints.CENTER;
        gbcFlight.fill = GridBagConstraints.NONE;
        bookButton.addActionListener(e -> handleBookFlight());
        panel.add(bookButton, gbcFlight);

        // Panel for control buttons (Logout, Update Profile)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutButton = new JButton("Logout");
        updateProfileButton = new JButton("Update Profile");
        // Add My Bookings button
        JButton myBookingsButton = new JButton("My Bookings");
        controlPanel.add(myBookingsButton);
        controlPanel.add(updateProfileButton);
        controlPanel.add(logoutButton);

        // Add control panel using GridBagConstraints instead of BorderLayout
        gbcFlight.gridx = 0;
        gbcFlight.gridy = 6;
        gbcFlight.gridwidth = 2;
        gbcFlight.weightx = 1.0;
        gbcFlight.weighty = 0;
        gbcFlight.fill = GridBagConstraints.HORIZONTAL;
        gbcFlight.anchor = GridBagConstraints.EAST;
        panel.add(controlPanel, gbcFlight);

        displayButton = new JButton("Refresh Flights");

        gbcFlight.gridx = 0; 
        gbcFlight.gridy = 4; 
        gbcFlight.gridwidth = 2; 
        gbcFlight.anchor = GridBagConstraints.CENTER; 
        panel.add(displayButton, gbcFlight);

        displayButton.addActionListener(e -> displayFlights());
        logoutButton.addActionListener(e -> performLogout());
        updateProfileButton.addActionListener(e -> openUpdateProfileWindow());

        // Add action listener for My Bookings button
        myBookingsButton.addActionListener(e -> openMyBookingsWindow());

        return panel;
    }

    private void performLogin() {
        String username = userField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loggedInUser = system.getUserService().authenticateUser(username, password);

        if (loggedInUser != null) {
            JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            // Switch panel based on user role
            switch (loggedInUser.getRole()) {
                case "Administrator":
                    cardLayout.show(cardPanel, "Admin");
                    break;
                case "Agent":
                    cardLayout.show(cardPanel, "Agent");
                    break;
                case "Customer":
                default:
                    cardLayout.show(cardPanel, "Flight");
                    displayFlights();
                    break;
            }
            userField.setText("");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performLogout() {
        // Close all open user-specific windows
        for (Window userWindow : openUserWindows) {
            userWindow.dispose();
        }
        openUserWindows.clear(); // Clear the list

        loggedInUser = null;
        cardLayout.show(cardPanel, "Login");
        JOptionPane.showMessageDialog(this, "Logged out successfully.", "Logout", JOptionPane.INFORMATION_MESSAGE);
        flightListModel.clear();
        bookingFlightIdField.setText("");
        bookingNumSeatsField.setText("");
    }

    private void openUpdateProfileWindow() {
        if (loggedInUser != null) {
            UpdateProfileWindow updateWindow = new UpdateProfileWindow(system.getUserService(), loggedInUser);
            updateWindow.setVisible(true);
            openUserWindows.add(updateWindow);
        } else {
            JOptionPane.showMessageDialog(this, "Please login to update your profile.", "Update Profile Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // New method to open the My Bookings window
    private void openMyBookingsWindow() {
        if (loggedInUser != null) {
            // Pass the BookingSystem instance instead of DatabaseManager
            MyBookingsWindow bookingsWindow = new MyBookingsWindow(system, loggedInUser, this);
            bookingsWindow.setVisible(true);
            // Add the bookings window to the list of open user windows
            openUserWindows.add(bookingsWindow);
        } else {
            JOptionPane.showMessageDialog(this, "Please login to view your bookings.", "My Bookings Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void displayFlights() {
        flightListModel.clear();

        ArrayList<Flight> flights = system.getAllFlights();
        if (flights.isEmpty()) {
            // You might want to add a message to the list or status bar instead
            System.out.println("No flights available.");
        } else {
            for (Flight flight : flights) {
                // Format the flight string to include ID at the start
                String flightInfo = String.format("ID: %d - %s", 
                    system.getDbManager().getFlightId(flight.getFlightNumber()),
                    flight.toString());
                flightListModel.addElement(flightInfo);
            }
        }
    }

    private void handleBookFlight() {
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this, "Please log in first.");
            return;
        }

        String flightIdStr = bookingFlightIdField.getText().trim();
        String numSeatsStr = bookingNumSeatsField.getText().trim();

        if (flightIdStr.isEmpty() || numSeatsStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both flight ID and number of seats.");
            return;
        }

        try {
            int flightId = Integer.parseInt(flightIdStr);
            int numSeats = Integer.parseInt(numSeatsStr);

            // Show passenger information dialog
            PassengerInfoDialog passengerDialog = new PassengerInfoDialog(this, system, numSeats);
            passengerDialog.setVisible(true);

            if (passengerDialog.isConfirmed()) {
                // Create the booking
                boolean success = system.createBooking(loggedInUser, flightId, numSeats);
                if (success) {
                    List<Booking> userBookings = system.getUserBookings(loggedInUser);
                    Booking latestBooking = null;
                    if (!userBookings.isEmpty()) {
                        latestBooking = userBookings.get(userBookings.size() - 1);
                    }

                    if (latestBooking != null) {
                        // Link passengers to the booking using PassengerService
                        List<Passenger> passengers = passengerDialog.getPassengers();
                        for (Passenger passenger : passengers) {
                            system.getPassengerService().linkPassengerToBooking(latestBooking.getBookingId(), passenger.getPassengerId());
                        }

                        // Show payment window
                        PaymentWindow paymentWindow = new PaymentWindow(this, system, latestBooking.getBookingId());
                        paymentWindow.setVisible(true);

                        // Refresh the display
                        displayFlights();
                        bookingFlightIdField.setText("");
                        bookingNumSeatsField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "Booking created, but could not retrieve booking details for payment.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create booking. Please try again.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for flight ID and seats.");
        }
    }

    // Add a getter for DatabaseManager to use in GUI
    public DatabaseManager getDbManager() {
        return system.getDbManager();
    }

    // Method to update the flight list display (Changed from updateFlightArea)
    private void updateFlightList(List<Flight> flights) {
        flightListModel.clear(); // Clear existing items
        if (flights != null) {
            for (Flight flight : flights) {
                flightListModel.addElement(flight.toString()); // Add flight string to model
            }
        }
    }

    // Placeholder Admin Panel
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout()); // Main panel for Admin tab

        // Create an outer panel to hold the user management section
        JPanel contentPanel = new JPanel(new BorderLayout());

        // --- User Table Section ---
        JPanel userTablePanel = new JPanel(new BorderLayout());
        String[] userColumns = {"ID", "Username", "Email", "Phone Number", "User ID", "Role"};
        DefaultTableModel adminUserTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable adminUserTable = new JTable(adminUserTableModel);
        JScrollPane adminUserScrollPane = new JScrollPane(adminUserTable);
        userTablePanel.add(adminUserScrollPane, BorderLayout.CENTER);
        userTablePanel.setBorder(BorderFactory.createTitledBorder("All Users"));

        // Add a ListSelectionListener to populate fields when a user is selected
        adminUserTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting() && adminUserTable.getSelectedRow() != -1) {
                    int selectedRow = adminUserTable.getSelectedRow();
                    int selectedUserId = (int) adminUserTableModel.getValueAt(selectedRow, 0);
                    manageUserIdField.setText(String.valueOf(selectedUserId));
                    System.out.println("Admin: User selected with ID: " + selectedUserId);
                }
            }
        });

        // Add Delete User button
        JButton deleteUserButton = new JButton("Delete Selected User");
        deleteUserButton.addActionListener(e -> {
            int selectedRow = adminUserTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a user to delete.", "Delete Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int userId = (int) adminUserTableModel.getValueAt(selectedRow, 0);
            String username = (String) adminUserTableModel.getValueAt(selectedRow, 1);
            String role = (String) adminUserTableModel.getValueAt(selectedRow, 5);

            // Prevent deletion of admin users
            if ("Administrator".equals(role)) {
                JOptionPane.showMessageDialog(panel, "Cannot delete administrator accounts.", "Delete Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel,
                "Are you sure you want to delete user '" + username + "'?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = system.getDbManager().deleteUser(userId);
                if (success) {
                    JOptionPane.showMessageDialog(panel, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // Refresh the user table
                    populateAdminUserTable(adminUserTableModel);
                } else {
                    JOptionPane.showMessageDialog(panel, 
                        "Failed to delete user. The user may have existing bookings or the account is protected.",
                        "Delete Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add the delete button to the user table panel
        JPanel userTableButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userTableButtonPanel.add(deleteUserButton);
        userTablePanel.add(userTableButtonPanel, BorderLayout.SOUTH);

        // --- User Creation Section ---
        JPanel createUserPanel = new JPanel(new GridBagLayout());
        createUserPanel.setBorder(BorderFactory.createTitledBorder("Create New User"));
        GridBagConstraints gbcCreate = new GridBagConstraints();
        gbcCreate.insets = new Insets(5, 5, 5, 5);
        gbcCreate.fill = GridBagConstraints.HORIZONTAL;

        // Input Fields
        JLabel createUsernameLabel = new JLabel("Username:");
        JTextField createUsernameField = new JTextField(15);
        JLabel createPasswordLabel = new JLabel("Password:");
        JPasswordField createPasswordField = new JPasswordField(15);
        JLabel createEmailLabel = new JLabel("Email:");
        JTextField createEmailField = new JTextField(15);
        JLabel createPhoneLabel = new JLabel("Phone:");
        JTextField createPhoneField = new JTextField(15);
        JLabel createUserIdLabel = new JLabel("User ID:");
        JTextField createUserIdField = new JTextField(15);
        JLabel createRoleLabel = new JLabel("Role:");
        JComboBox<String> createRoleCombo = new JComboBox<>(new String[]{"Customer", "Agent", "Administrator"});

        // Add components to panel
        gbcCreate.gridx = 0; gbcCreate.gridy = 0; createUserPanel.add(createUsernameLabel, gbcCreate);
        gbcCreate.gridx = 1; gbcCreate.gridy = 0; createUserPanel.add(createUsernameField, gbcCreate);

        gbcCreate.gridx = 0; gbcCreate.gridy = 1; createUserPanel.add(createPasswordLabel, gbcCreate);
        gbcCreate.gridx = 1; gbcCreate.gridy = 1; createUserPanel.add(createPasswordField, gbcCreate);

        gbcCreate.gridx = 0; gbcCreate.gridy = 2; createUserPanel.add(createEmailLabel, gbcCreate);
        gbcCreate.gridx = 1; gbcCreate.gridy = 2; createUserPanel.add(createEmailField, gbcCreate);

        gbcCreate.gridx = 2; gbcCreate.gridy = 0; createUserPanel.add(createPhoneLabel, gbcCreate);
        gbcCreate.gridx = 3; gbcCreate.gridy = 0; createUserPanel.add(createPhoneField, gbcCreate);

        gbcCreate.gridx = 2; gbcCreate.gridy = 1; createUserPanel.add(createUserIdLabel, gbcCreate);
        gbcCreate.gridx = 3; gbcCreate.gridy = 1; createUserPanel.add(createUserIdField, gbcCreate);

        gbcCreate.gridx = 2; gbcCreate.gridy = 2; createUserPanel.add(createRoleLabel, gbcCreate);
        gbcCreate.gridx = 3; gbcCreate.gridy = 2; createUserPanel.add(createRoleCombo, gbcCreate);

        // Button
        JButton createUserButton = new JButton("Create User");
        gbcCreate.gridx = 0; gbcCreate.gridy = 3; gbcCreate.gridwidth = 4; gbcCreate.anchor = GridBagConstraints.CENTER; gbcCreate.fill = GridBagConstraints.NONE;
        createUserPanel.add(createUserButton, gbcCreate);

        // Add action listener for Create User button
        createUserButton.addActionListener(e -> {
            String username = createUsernameField.getText().trim();
            String password = new String(createPasswordField.getPassword()).trim();
            String email = createEmailField.getText().trim();
            String phone = createPhoneField.getText().trim();
            String userId = createUserIdField.getText().trim();
            String role = (String) createRoleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty() || userId.isEmpty() || role == null) {
                JOptionPane.showMessageDialog(panel, "Please fill in all fields to create a user.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Call DatabaseManager directly
            long result = system.getDbManager().registerUser(username, password, email, phone, userId, role);

            if (result > 0) {
                JOptionPane.showMessageDialog(panel, "User created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Clear fields
                createUsernameField.setText("");
                createPasswordField.setText("");
                createEmailField.setText("");
                createPhoneField.setText("");
                createUserIdField.setText("");
                createRoleCombo.setSelectedIndex(0); // Reset to Customer
                // Refresh the user table
                populateAdminUserTable(adminUserTableModel);
            } else {
                String errorMessage = "Failed to create user.";
                if (result == -1) errorMessage = "Registration failed: Username already exists.";
                if (result == -2) errorMessage = "Registration failed: User ID already exists.";
                JOptionPane.showMessageDialog(panel, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Manage User Access Section ---
        JPanel manageUserAccessPanel = new JPanel(new GridBagLayout());
        manageUserAccessPanel.setBorder(BorderFactory.createTitledBorder("Manage User Access"));
        GridBagConstraints gbcManage = new GridBagConstraints();
        gbcManage.insets = new Insets(5, 5, 5, 5);
        gbcManage.fill = GridBagConstraints.HORIZONTAL;

        // Input Fields
        JLabel selectUserLabel = new JLabel("Select User (by ID or select from table): ");
        manageUserIdField = new JTextField(10);  // Use the class field instead of creating a local variable
        JLabel newUserRoleLabel = new JLabel("New Role:");
        JComboBox<String> newUserRoleCombo = new JComboBox<>(new String[]{"Customer", "Agent", "Administrator"});

        // Buttons
        JButton updateUserRoleButton = new JButton("Update Role");
        JButton refreshUsersButton = new JButton("Refresh Users"); // Button to refresh the user table

        // Add components
        gbcManage.gridx = 0; gbcManage.gridy = 0; manageUserAccessPanel.add(selectUserLabel, gbcManage);
        gbcManage.gridx = 1; gbcManage.gridy = 0; manageUserAccessPanel.add(manageUserIdField, gbcManage);

        gbcManage.gridx = 0; gbcManage.gridy = 1; manageUserAccessPanel.add(newUserRoleLabel, gbcManage);
        gbcManage.gridx = 1; gbcManage.gridy = 1; manageUserAccessPanel.add(newUserRoleCombo, gbcManage);

        JPanel manageButtonPanel = new JPanel(new FlowLayout());
        manageButtonPanel.add(updateUserRoleButton);
        manageButtonPanel.add(refreshUsersButton);

        gbcManage.gridx = 0; gbcManage.gridy = 2; gbcManage.gridwidth = 2; gbcManage.anchor = GridBagConstraints.CENTER; gbcManage.fill = GridBagConstraints.NONE;
        manageUserAccessPanel.add(manageButtonPanel, gbcManage);

        // Add action listener for Update Role button
        updateUserRoleButton.addActionListener(e -> {
            String userIdText = manageUserIdField.getText().trim();
            String newRole = (String) newUserRoleCombo.getSelectedItem();

            if (userIdText.isEmpty() || newRole == null) {
                JOptionPane.showMessageDialog(panel, "Please enter User ID and select a new role.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int userId = Integer.parseInt(userIdText);

                // Call DatabaseManager directly
                boolean success = system.getDbManager().updateUserRole(userId, newRole);

                if (success) {
                    JOptionPane.showMessageDialog(panel, "User role updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // Clear fields and refresh table
                    manageUserIdField.setText("");
                    newUserRoleCombo.setSelectedIndex(0); // Reset to Customer
                    populateAdminUserTable(adminUserTableModel);
                } else {
                    JOptionPane.showMessageDialog(panel, "Failed to update user role. User ID might not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid User ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Add action listener for Refresh Users button
        refreshUsersButton.addActionListener(e -> populateAdminUserTable(adminUserTableModel));

        // Layout for the content panel: User Table at the top, creation and manage sections below
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5)); // Use FlowLayout for the two bottom panels
        bottomPanel.add(createUserPanel);
        bottomPanel.add(manageUserAccessPanel);

        contentPanel.add(userTablePanel, BorderLayout.CENTER); // User table in the center of contentPanel
        contentPanel.add(bottomPanel, BorderLayout.SOUTH); // Creation and manage panels at the bottom

        // Add the main content panel to the main Admin panel
        panel.add(contentPanel, BorderLayout.CENTER);

        // Initial population of the user table
        populateAdminUserTable(adminUserTableModel);

        // Add a logout button to the admin panel (keep at the bottom) - This will be in the SOUTH of the main 'panel'
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton adminLogoutButton = new JButton("Logout");
        adminLogoutButton.addActionListener(e -> performLogout());
        controlPanel.add(adminLogoutButton);
        panel.add(controlPanel, BorderLayout.SOUTH); // Add control panel to the SOUTH of the main 'panel'

        return panel;
    }

    // Helper method to populate the admin user table
    private void populateAdminUserTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        List<User> users = system.getUserService().getAllUsers();
        for (User user : users) {
            tableModel.addRow(new Object[]{
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getUserId(),
                user.getRole()
            });
        }
    }

    // Placeholder Agent Panel
    private JPanel createAgentPanel() {
        JPanel panel = new JPanel(new BorderLayout()); // Main panel for Agent tab

        // Create an outer panel to hold the input/buttons and the table
        JPanel contentPanel = new JPanel(new BorderLayout());

        // --- Flight Management Section (Input Fields and Buttons) ---
        JPanel flightManagementInputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Input Fields
        JLabel flightNumberLabel = new JLabel("Flight Number:");
        agentFlightNumberField = new JTextField(10);
        JLabel originLabel = new JLabel("Origin:");
        agentOriginField = new JTextField(10);
        JLabel destinationLabel = new JLabel("Destination:");
        agentDestinationField = new JTextField(10);
        JLabel capacityLabel = new JLabel("Capacity:");
        agentCapacityField = new JTextField(5);

        gbc.gridx = 0; gbc.gridy = 0; flightManagementInputPanel.add(flightNumberLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; flightManagementInputPanel.add(agentFlightNumberField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; flightManagementInputPanel.add(originLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; flightManagementInputPanel.add(agentOriginField, gbc);

        gbc.gridx = 2; gbc.gridy = 0; flightManagementInputPanel.add(destinationLabel, gbc);
        gbc.gridx = 3; gbc.gridy = 0; flightManagementInputPanel.add(agentDestinationField, gbc);

        gbc.gridx = 2; gbc.gridy = 1; flightManagementInputPanel.add(capacityLabel, gbc);
        gbc.gridx = 3; gbc.gridy = 1; flightManagementInputPanel.add(agentCapacityField, gbc);

        // Buttons
        JButton addFlightButton = new JButton("Add Flight");
        JButton updateFlightButton = new JButton("Update Flight");
        JButton deleteFlightButton = new JButton("Delete Flight");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addFlightButton);
        buttonPanel.add(updateFlightButton);
        buttonPanel.add(deleteFlightButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        flightManagementInputPanel.add(buttonPanel, gbc);

        // --- Flight List Table ---
        String[] columnNames = {"ID", "Flight Number", "Origin", "Destination", "Capacity", "Booked Seats", "Available Seats"};
        agentFlightTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        agentFlightTable = new JTable(agentFlightTableModel);
        JScrollPane tableScrollPane = new JScrollPane(agentFlightTable);

        // Add a ListSelectionListener to the table to populate fields when a row is selected
        agentFlightTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting() && agentFlightTable.getSelectedRow() != -1) {
                    int selectedRow = agentFlightTable.getSelectedRow();
                    agentFlightNumberField.setText(agentFlightTableModel.getValueAt(selectedRow, 1).toString());
                    agentOriginField.setText(agentFlightTableModel.getValueAt(selectedRow, 2).toString());
                    agentDestinationField.setText(agentFlightTableModel.getValueAt(selectedRow, 3).toString());
                    agentCapacityField.setText(agentFlightTableModel.getValueAt(selectedRow, 4).toString());
                }
            }
        });

        // Add action listeners to buttons
        addFlightButton.addActionListener(e -> {
            String flightNum = agentFlightNumberField.getText().trim();
            String origin = agentOriginField.getText().trim();
            String destination = agentDestinationField.getText().trim();
            String capacityText = agentCapacityField.getText().trim();

            if (flightNum.isEmpty() || origin.isEmpty() || destination.isEmpty() || capacityText.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all flight details.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int capacity = Integer.parseInt(capacityText);
                if (capacity <= 0) {
                    JOptionPane.showMessageDialog(panel, "Capacity must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
                JTextField flightNumField = new JTextField(10);
                JTextField originField = new JTextField(10);
                JTextField destinationField = new JTextField(10);
                JTextField capacityField = new JTextField(10);
                JTextField priceField = new JTextField(10);
                priceField.setEditable(false); // Make price field read-only

                inputPanel.add(new JLabel("Flight Number:"));
                inputPanel.add(flightNumField);
                inputPanel.add(new JLabel("Origin:"));
                inputPanel.add(originField);
                inputPanel.add(new JLabel("Destination:"));
                inputPanel.add(destinationField);
                inputPanel.add(new JLabel("Capacity:"));
                inputPanel.add(capacityField);
                inputPanel.add(new JLabel("Price ($):"));
                inputPanel.add(priceField);

                // Add document listeners to calculate price when origin or destination changes
                DocumentListener priceCalculator = new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) { calculatePrice(); }
                    public void removeUpdate(DocumentEvent e) { calculatePrice(); }
                    public void insertUpdate(DocumentEvent e) { calculatePrice(); }

                    private void calculatePrice() {
                        String origin = originField.getText().trim();
                        String destination = destinationField.getText().trim();
                        if (!origin.isEmpty() && !destination.isEmpty()) {
                            // Find city indices
                            int originIndex = -1;
                            int destIndex = -1;
                            for (int i = 0; i < system.getDbManager().getCities().length; i++) {
                                if (system.getDbManager().getCities()[i].equals(origin)) {
                                    originIndex = i;
                                }
                                if (system.getDbManager().getCities()[i].equals(destination)) {
                                    destIndex = i;
                                }
                            }
                            if (originIndex != -1 && destIndex != -1) {
                                double price = system.getDbManager().calculatePrice(originIndex, destIndex);
                                priceField.setText(String.format("%.2f", price));
                            }
                        }
                    }
                };

                originField.getDocument().addDocumentListener(priceCalculator);
                destinationField.getDocument().addDocumentListener(priceCalculator);

                int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add New Flight", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String newFlightNum = flightNumField.getText();
                        String newOrigin = originField.getText();
                        String newDestination = destinationField.getText();
                        int newCapacity = Integer.parseInt(capacityField.getText());
                        double price = Double.parseDouble(priceField.getText());

                        Flight newFlight = new Flight(newFlightNum, newOrigin, newDestination, newCapacity, price);
                        system.addFlight(newFlight);
                        populateFlightTable(agentFlightTableModel);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Please enter valid numbers for capacity and price.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid capacity number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        updateFlightButton.addActionListener(e -> {
            String flightNum = agentFlightNumberField.getText().trim();
            String origin = agentOriginField.getText().trim();
            String destination = agentDestinationField.getText().trim();
            String capacityText = agentCapacityField.getText().trim();

            if (flightNum.isEmpty() || origin.isEmpty() || destination.isEmpty() || capacityText.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all flight details for update.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int capacity = Integer.parseInt(capacityText);
                if (capacity <= 0) {
                    JOptionPane.showMessageDialog(panel, "Capacity must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Flight existingFlight = system.findFlight(flightNum);

                if (existingFlight != null) {
                    if (capacity < existingFlight.getBookedSeats()) {
                        JOptionPane.showMessageDialog(panel, String.format("Cannot reduce capacity below booked seats (%d).", existingFlight.getBookedSeats()), "Update Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
                    JTextField originField = new JTextField(existingFlight.getDeparture(), 10);
                    JTextField destinationField = new JTextField(existingFlight.getDestination(), 10);
                    JTextField capacityField = new JTextField(String.valueOf(existingFlight.getCapacity()), 10);
                    JTextField priceField = new JTextField(String.valueOf(existingFlight.getPrice()), 10);

                    inputPanel.add(new JLabel("Origin:"));
                    inputPanel.add(originField);
                    inputPanel.add(new JLabel("Destination:"));
                    inputPanel.add(destinationField);
                    inputPanel.add(new JLabel("Capacity:"));
                    inputPanel.add(capacityField);
                    inputPanel.add(new JLabel("Price ($):"));
                    inputPanel.add(priceField);

                    int result = JOptionPane.showConfirmDialog(this, inputPanel, "Update Flight", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        try {
                            String newOrigin = originField.getText();
                            String newDestination = destinationField.getText();
                            int newCapacity = Integer.parseInt(capacityField.getText());
                            double newPrice = Double.parseDouble(priceField.getText());

                            Flight updatedFlight = new Flight(existingFlight.getFlightNumber(), newOrigin, newDestination, newCapacity, newPrice);
                            updatedFlight.setBookedSeats(existingFlight.getBookedSeats());
                            system.updateFlight(updatedFlight);
                            populateFlightTable(agentFlightTableModel);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Please enter valid numbers for capacity and price.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "Flight not found for update.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid capacity number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteFlightButton.addActionListener(e -> {
            String flightNum = agentFlightNumberField.getText().trim();

            if (flightNum.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter the flight number to delete or select from the table.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel,
                "Are you sure you want to delete flight " + flightNum + "?", "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = system.deleteFlight(flightNum);

                if (success) {
                    JOptionPane.showMessageDialog(panel, "Flight deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFlightFields();
                    populateFlightTable(agentFlightTableModel);
                } else {
                    JOptionPane.showMessageDialog(panel, "Failed to delete flight. It might have existing bookings or not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add the input fields and buttons panel to the NORTH of the content panel
        contentPanel.add(flightManagementInputPanel, BorderLayout.NORTH);

        // Add the table to the CENTER of the content panel
        contentPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add the content panel to the CENTER of the main Agent panel
        panel.add(contentPanel, BorderLayout.CENTER);

        // Initial population of the table
        populateFlightTable(agentFlightTableModel);

        // Add a logout button to the agent panel (keep at the bottom)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton manageUsersButton = new JButton("Manage Users");
        JButton agentLogoutButton = new JButton("Logout");
        // Add button to manage bookings
        JButton manageBookingsButton = new JButton("Manage Bookings");

        manageUsersButton.addActionListener(e -> {
            UserManagementWindow userManagementWindow = new UserManagementWindow(system);
            userManagementWindow.setVisible(true);
        });
        agentLogoutButton.addActionListener(e -> performLogout());

        // Add action listener for Manage Bookings button
        manageBookingsButton.addActionListener(e -> {
            AgentBookingManagementWindow bookingManagementWindow = new AgentBookingManagementWindow(system);
            bookingManagementWindow.setVisible(true);
        });

        controlPanel.add(manageUsersButton);
        controlPanel.add(manageBookingsButton); // Add the new button
        controlPanel.add(agentLogoutButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Helper method to populate the flight table
    private void populateFlightTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        ArrayList<Flight> flights = system.getAllFlights();
        for (Flight flight : flights) {
            int flightId = system.getDbManager().getFlightId(flight.getFlightNumber());
            tableModel.addRow(new Object[]{
                flightId,
                flight.getFlightNumber(),
                flight.getDeparture(),
                flight.getDestination(),
                flight.getCapacity(),
                flight.getBookedSeats(),
                flight.getAvailableSeats()
            });
        }
    }

    // Helper method to clear flight management input fields
    private void clearFlightFields() {
        agentFlightNumberField.setText("");
        agentOriginField.setText("");
        agentDestinationField.setText("");
        agentCapacityField.setText("");
    }
}