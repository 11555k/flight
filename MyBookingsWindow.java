import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class MyBookingsWindow extends JFrame {
    private BookingSystem bookingSystem; // Renamed from dbManager
    private User loggedInUser;
    private FlightSystemGUI mainGUI; // Reference to the main GUI

    private JList<Booking> bookingList; // Store Booking objects
    private DefaultListModel<Booking> bookingListModel;
    private JButton cancelButton;
    private JTextField seatsToCancelField; // New field for seats to cancel
    private JButton payPendingButton; // New button for paying pending flights

    public MyBookingsWindow(BookingSystem bookingSystem, User loggedInUser, FlightSystemGUI mainGUI) { // Updated parameter type
        this.bookingSystem = bookingSystem;
        this.loggedInUser = loggedInUser;
        this.mainGUI = mainGUI;

        setTitle("My Bookings - " + loggedInUser.getUsername());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this window
        setLocationRelativeTo(null); // Center the window

        setLayout(new BorderLayout());

        // Booking List
        bookingListModel = new DefaultListModel<>();
        bookingList = new JList<>(bookingListModel);
        JScrollPane scrollPane = new JScrollPane(bookingList);
        add(scrollPane, BorderLayout.CENTER);

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        cancelButton = new JButton("Cancel Selected Booking");
        payPendingButton = new JButton("Pay Pending Flights"); // New button

        // Add input for seats to cancel
        JLabel seatsToCancelLabel = new JLabel("Seats to Cancel:");
        seatsToCancelField = new JTextField(5);

        controlPanel.add(seatsToCancelLabel);
        controlPanel.add(seatsToCancelField);
        controlPanel.add(cancelButton);
        controlPanel.add(payPendingButton); // Add the new button
        add(controlPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> cancelSelectedBooking());
        payPendingButton.addActionListener(e -> handlePendingPayments()); // Add action listener

        // Load and display bookings
        loadBookings();

        // Update the cell renderer to display Booking details
        bookingList.setCellRenderer(new BookingListCellRenderer());
    }

    private void loadBookings() {
        bookingListModel.clear();
        if (loggedInUser != null) {
            List<Booking> bookings = bookingSystem.getUserBookings(loggedInUser); // Use BookingSystem method
            if (bookings.isEmpty()) {
                // Add a placeholder or leave empty if preferred
            } else {
                for (Booking booking : bookings) {
                    bookingListModel.addElement(booking); // Add Booking object directly
                }
            }
        }
    }

    private void cancelSelectedBooking() {
        Booking selectedBooking = bookingList.getSelectedValue();
        if (selectedBooking == null) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Cancellation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String seatsToCancelText = seatsToCancelField.getText().trim();
        if (seatsToCancelText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the number of seats to cancel.", "Cancellation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int numSeatsToCancel;
        try {
            numSeatsToCancel = Integer.parseInt(seatsToCancelText);
            if (numSeatsToCancel <= 0) {
                 JOptionPane.showMessageDialog(this, "Number of seats to cancel must be positive.", "Cancellation Error", JOptionPane.WARNING_MESSAGE);
                 return;
            }
             if (numSeatsToCancel > selectedBooking.getNumSeats()) {
                JOptionPane.showMessageDialog(this, String.format("Cannot cancel %d seats. You only booked %d seats for this flight.", numSeatsToCancel, selectedBooking.getNumSeats()), "Cancellation Error", JOptionPane.WARNING_MESSAGE);
                return;
             }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number of seats.", "Cancellation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm cancellation
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Are you sure you want to cancel %d seat(s) from this booking?", numSeatsToCancel), "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (loggedInUser != null) {
                // Call the cancelBookingSeats method in BookingSystem
                int[] cancellationResult = bookingSystem.cancelBookingSeats(loggedInUser, selectedBooking.getBookingId(), numSeatsToCancel); // Use BookingSystem method

                if (cancellationResult != null && cancellationResult.length == 2) {
                    int flightIdToUpdate = cancellationResult[0];
                    int actualSeatsCancelled = cancellationResult[1];

                    JOptionPane.showMessageDialog(this, "Booking cancelled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Update the flight's booked seats count in the database via BookingSystem
                     // Need to get the Flight object via BookingSystem as well
                    Flight flight = bookingSystem.findFlightById(flightIdToUpdate);
                    if (flight != null) {
                        flight.setBookedSeats(flight.getBookedSeats() - actualSeatsCancelled);
                        bookingSystem.updateFlightInDatabase(flight); // Use BookingSystem method
                    }

                    // Refresh the list of bookings in this window
                    loadBookings();

                    // Refresh the flight list in the main GUI
                    if (mainGUI != null) {
                        mainGUI.displayFlights();
                    }

                     seatsToCancelField.setText(""); // Clear the input field

                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel booking. Booking not found, does not belong to you, or invalid seats.", "Cancellation Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Add new method to handle pending payments
    private void handlePendingPayments() {
        List<Booking> pendingBookings = new ArrayList<>();
        
        // Collect all pending bookings
        for (int i = 0; i < bookingListModel.size(); i++) {
            Booking booking = bookingListModel.getElementAt(i);
            if ("Pending".equals(booking.getStatus())) {
                pendingBookings.add(booking);
            }
        }

        if (pendingBookings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending bookings found.", "Payment Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show confirmation dialog with total amount
        double totalAmount = 0;
        StringBuilder bookingDetails = new StringBuilder("Pending Bookings:\n\n");
        for (Booking booking : pendingBookings) {
            Flight flight = bookingSystem.getDbManager().getFlightById(booking.getFlightId());
            if (flight != null) {
                double bookingAmount = booking.getNumSeats() * flight.getPrice();
                totalAmount += bookingAmount;
                bookingDetails.append(String.format("Flight %s: %d seats - $%.2f\n", 
                    flight.getFlightNumber(), booking.getNumSeats(), bookingAmount));
            }
        }
        bookingDetails.append(String.format("\nTotal Amount: $%.2f", totalAmount));

        int confirm = JOptionPane.showConfirmDialog(this,
            bookingDetails.toString() + "\n\nDo you want to proceed with payment?",
            "Confirm Payment",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Open payment window for each pending booking
            for (Booking booking : pendingBookings) {
                PaymentWindow paymentWindow = new PaymentWindow(mainGUI, bookingSystem, booking.getBookingId());
                paymentWindow.setVisible(true);
            }
            // Refresh the bookings list after payment attempts
            loadBookings();
        }
    }

    // Update the cell renderer to display Booking details
    private class BookingListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Booking) {
                Booking booking = (Booking) value;
                // Fetch flight details to display meaningful info via BookingSystem and DatabaseManager
                Flight flight = bookingSystem.getDbManager().getFlightById(booking.getFlightId());
                if (flight != null) {
                    // Include booking status in the display
                    setText(String.format("Booking ID: %d - Flight %s (%s to %s) - Seats: %d - Status: %s",
                        booking.getBookingId(), flight.getFlightNumber(), flight.getDeparture(), flight.getDestination(), booking.getNumSeats(), booking.getStatus()));
                } else {
                    // Include booking status in the display even if flight is unknown
                    setText(String.format("Booking ID: %d - Flight (Unknown - ID: %d) - Seats: %d - Status: %s",
                        booking.getBookingId(), booking.getFlightId(), booking.getNumSeats(), booking.getStatus()));
                }
            }
            return this;
        }
    }
} 