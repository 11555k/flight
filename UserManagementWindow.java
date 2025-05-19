/**
 * UserManagementWindow.java
 * This class creates a window for managing users and their bookings in the flight system.
 * It allows agents to view customer information and manage their bookings.
 * 
 * @author Student
 * @version 1.0
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UserManagementWindow extends JFrame {
    // Instance variables to store system data and UI components
    private BookingSystem system;  // Reference to the main booking system
    private JTable userTable;      // Table to display user information
    private JTable bookingTable;   // Table to display booking information
    private DefaultTableModel userTableModel;    // Model for user table
    private DefaultTableModel bookingTableModel; // Model for booking table
    private User selectedUser;     // Currently selected user

    /**
     * Constructor for UserManagementWindow
     * Sets up the window layout and initializes all components
     * 
     * @param system The booking system instance to use
     */
    public UserManagementWindow(BookingSystem system) {
        // Initialize the system reference
        this.system = system;
        
        // Set up the window properties
        setTitle("User Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);  // Center the window on screen

        // Create the main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create a split pane to separate user and booking tables
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);  // Set initial divider position

        // Set up the user table panel
        JPanel userPanel = new JPanel(new BorderLayout());
        String[] userColumns = {"ID", "Username", "Email", "Role"};
        // Create a non-editable table model
        userTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Make table read-only
            }
        };
        userTable = new JTable(userTableModel);
        JScrollPane userScrollPane = new JScrollPane(userTable);
        userPanel.add(userScrollPane, BorderLayout.CENTER);
        userPanel.setBorder(BorderFactory.createTitledBorder("Users"));

        // Set up the booking table panel
        JPanel bookingPanel = new JPanel(new BorderLayout());
        String[] bookingColumns = {"Booking ID", "Flight Number", "Seats", "Status"};
        // Create a non-editable table model for bookings
        bookingTableModel = new DefaultTableModel(bookingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Make table read-only
            }
        };
        bookingTable = new JTable(bookingTableModel);
        JScrollPane bookingScrollPane = new JScrollPane(bookingTable);
        bookingPanel.add(bookingScrollPane, BorderLayout.CENTER);
        bookingPanel.setBorder(BorderFactory.createTitledBorder("User's Bookings"));

        // Add panels to split pane
        splitPane.setTopComponent(userPanel);
        splitPane.setBottomComponent(bookingPanel);

        // Create button panel with action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton newBookingButton = new JButton("New Booking");
        JButton modifyBookingButton = new JButton("Modify Booking");
        JButton cancelBookingButton = new JButton("Cancel Booking");
        JButton refreshButton = new JButton("Refresh");

        // Add buttons to panel
        buttonPanel.add(newBookingButton);
        buttonPanel.add(modifyBookingButton);
        buttonPanel.add(cancelBookingButton);
        buttonPanel.add(refreshButton);

        // Add components to main panel
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Add listener for user selection
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Get selected user's ID and load their data
                    int userId = (int) userTableModel.getValueAt(selectedRow, 0);
                    selectedUser = system.getDbManager().getUserById(userId);
                    updateBookingTable();  // Update booking table with user's bookings
                }
            }
        });

        // Add action listeners for buttons
        newBookingButton.addActionListener(e -> openNewBookingDialog());
        modifyBookingButton.addActionListener(e -> openModifyBookingDialog());
        cancelBookingButton.addActionListener(e -> cancelSelectedBooking());
        refreshButton.addActionListener(e -> refreshData());

        // Load initial data
        refreshData();
    }

    /**
     * Refreshes the user table with current data
     * Only shows customer users (agents can't manage other agents or admins)
     */
    private void refreshData() {
        // Clear existing data
        userTableModel.setRowCount(0);
        List<User> users = system.getDbManager().getAllUsers();
        
        // Add only customer users to the table
        for (User user : users) {
            if (user.getRole().equals("Customer")) {
                userTableModel.addRow(new Object[]{
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
                });
            }
        }

        // Clear booking table
        bookingTableModel.setRowCount(0);
    }

    /**
     * Updates the booking table with the selected user's bookings
     */
    private void updateBookingTable() {
        if (selectedUser != null) {
            bookingTableModel.setRowCount(0);
            List<Booking> bookings = system.getDbManager().getUserBookings(selectedUser.getId());
            
            // Add each booking to the table
            for (Booking booking : bookings) {
                Flight flight = system.getDbManager().getFlightById(booking.getFlightId());
                if (flight != null) {
                    bookingTableModel.addRow(new Object[]{
                        booking.getBookingId(),
                        flight.getFlightNumber(),
                        booking.getNumSeats(),
                        "Active"
                    });
                }
            }
        }
    }

    /**
     * Opens a dialog to create a new booking for the selected user
     * Prevents creating bookings for agents and administrators
     */
    private void openNewBookingDialog() {
        // Check if a user is selected
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Please select a user first.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if trying to book for an agent or admin
        if (selectedUser.getRole().equals("Agent") || selectedUser.getRole().equals("Administrator")) {
            JOptionPane.showMessageDialog(this, "Cannot create bookings for agents or administrators.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create and set up the booking dialog
        JDialog dialog = new JDialog(this, "New Booking", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create flight selection dropdown
        JComboBox<String> flightCombo = new JComboBox<>();
        ArrayList<Flight> flights = system.getAllFlights();
        for (Flight flight : flights) {
            flightCombo.addItem(flight.getFlightNumber() + " - " + flight.getDeparture() + " to " + flight.getDestination());
        }

        // Create seats input field
        JTextField seatsField = new JTextField(5);

        // Add components to dialog
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Flight:"), gbc);
        gbc.gridx = 1;
        dialog.add(flightCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Number of Seats:"), gbc);
        gbc.gridx = 1;
        dialog.add(seatsField, gbc);

        // Add buttons to dialog
        JPanel buttonPanel = new JPanel();
        JButton bookButton = new JButton("Book");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(bookButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        // Handle booking submission
        bookButton.addActionListener(e -> {
            try {
                // Get selected flight and seats
                String selectedFlight = (String) flightCombo.getSelectedItem();
                String flightNumber = selectedFlight.split(" - ")[0];
                int numSeats = Integer.parseInt(seatsField.getText());
                int flightId = system.getDbManager().getFlightId(flightNumber);

                // Attempt to create the booking
                if (system.createBooking(selectedUser, flightId, numSeats)) {
                    JOptionPane.showMessageDialog(dialog, "Booking created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    updateBookingTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create booking. Check available seats.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number of seats.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Handle dialog cancellation
        cancelButton.addActionListener(e -> dialog.dispose());

        // Show the dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Opens a dialog to modify an existing booking
     * Prevents modifying bookings for agents and administrators
     */
    private void openModifyBookingDialog() {
        // Check if a booking is selected
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to modify.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if trying to modify an agent or admin's booking
        if (selectedUser.getRole().equals("Agent") || selectedUser.getRole().equals("Administrator")) {
            JOptionPane.showMessageDialog(this, "Cannot modify bookings for agents or administrators.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get current booking details
        int bookingId = (int) bookingTableModel.getValueAt(selectedRow, 0);
        int currentSeats = (int) bookingTableModel.getValueAt(selectedRow, 2);

        // Create and set up the modification dialog
        JDialog dialog = new JDialog(this, "Modify Booking", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create seats input field with current value
        JTextField seatsField = new JTextField(String.valueOf(currentSeats), 5);

        // Add components to dialog
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("New Number of Seats:"), gbc);
        gbc.gridx = 1;
        dialog.add(seatsField, gbc);

        // Add buttons to dialog
        JPanel buttonPanel = new JPanel();
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        // Handle update submission
        updateButton.addActionListener(e -> {
            try {
                int newSeats = Integer.parseInt(seatsField.getText());
                if (newSeats <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Number of seats must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Attempt to modify the booking
                if (system.modifyBooking(bookingId, newSeats)) {
                    JOptionPane.showMessageDialog(dialog, "Booking modified successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    updateBookingTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to modify booking. Check available seats.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number of seats.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Handle dialog cancellation
        cancelButton.addActionListener(e -> dialog.dispose());

        // Show the dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Cancels the selected booking
     * Prevents canceling bookings for agents and administrators
     */
    private void cancelSelectedBooking() {
        // Check if a booking is selected
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if trying to cancel an agent or admin's booking
        if (selectedUser.getRole().equals("Agent") || selectedUser.getRole().equals("Administrator")) {
            JOptionPane.showMessageDialog(this, "Cannot cancel bookings for agents or administrators.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get booking ID and confirm cancellation
        int bookingId = (int) bookingTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel this booking?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION);

        // Process cancellation if confirmed
        if (confirm == JOptionPane.YES_OPTION) {
            if (system.cancelBooking(bookingId)) {
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                updateBookingTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 