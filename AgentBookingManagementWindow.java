import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AgentBookingManagementWindow extends JFrame {
    private BookingSystem bookingSystem;
    private JTable bookingTable;
    private DefaultTableModel bookingTableModel;

    public AgentBookingManagementWindow(BookingSystem system) {
        this.bookingSystem = system;
        setTitle("Agent - Manage Bookings");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Booking Table
        String[] bookingColumns = {"Booking ID", "User ID", "Flight ID", "Seats", "Date", "Status"};
        bookingTableModel = new DefaultTableModel(bookingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingTable = new JTable(bookingTableModel);
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        add(scrollPane, BorderLayout.CENTER);

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton markAsPaidButton = new JButton("Mark as Paid");
        JButton refreshButton = new JButton("Refresh");

        controlPanel.add(markAsPaidButton);
        controlPanel.add(refreshButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Add action listeners
        markAsPaidButton.addActionListener(e -> markSelectedBookingAsPaid());
        refreshButton.addActionListener(e -> loadBookings());

        // Initial data load
        loadBookings();
    }

    private void loadBookings() {
        bookingTableModel.setRowCount(0);
        List<Booking> bookings = bookingSystem.getAllBookings(); // Need a method to get all bookings in BookingSystem/DatabaseManager
        for (Booking booking : bookings) {
            bookingTableModel.addRow(new Object[]{
                booking.getBookingId(),
                booking.getUserId(),
                booking.getFlightId(),
                booking.getNumSeats(),
                booking.getBookingDate(),
                booking.getStatus()
            });
        }
    }

    private void markSelectedBookingAsPaid() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to mark as paid.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) bookingTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) bookingTableModel.getValueAt(selectedRow, 5);

        if ("Paid".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This booking is already marked as Paid.", "Status Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to mark this booking as Paid?",
            "Confirm Payment",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = bookingSystem.processPayment(bookingId); // Reuse the processPayment method

            if (success) {
                JOptionPane.showMessageDialog(this, "Booking status updated to Paid!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBookings(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update booking status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 