import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerInfoDialog extends JDialog {
    private final BookingSystem bookingSystem;
    private final int numSeats;
    private List<Passenger> passengers = new ArrayList<>();
    private final List<JTextField> nameFields;
    private final List<JTextField> passportFields;
    private final List<JTextField> dobFields;
    private final List<JTextField> requestFields;
    private boolean confirmed = false;

    public PassengerInfoDialog(Frame parent, BookingSystem bookingSystem, int numSeats) {
        super(parent, "Passenger Information", true);
        this.bookingSystem = bookingSystem;
        this.numSeats = numSeats;
        this.nameFields = new ArrayList<>();
        this.passportFields = new ArrayList<>();
        this.dobFields = new ArrayList<>();
        this.requestFields = new ArrayList<>();

        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(parent);

        // Create main panel with scroll pane
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Create input fields for each passenger
        for (int i = 0; i < numSeats; i++) {
            JPanel passengerPanel = createPassengerPanel(i + 1);
            mainPanel.add(passengerPanel);
            mainPanel.add(Box.createVerticalStrut(10));
        }

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");

        confirmButton.addActionListener(e -> {
            if (validateAndSavePassengers()) {
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createPassengerPanel(int passengerNumber) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Passenger " + passengerNumber));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        nameFields.add(nameField);
        panel.add(nameField, gbc);

        // Passport field
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Passport Number:"), gbc);
        gbc.gridx = 1;
        JTextField passportField = new JTextField(20);
        passportFields.add(passportField);
        panel.add(passportField, gbc);

        // Date of Birth field
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Date of Birth (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        JTextField dobField = new JTextField(20);
        dobFields.add(dobField);
        panel.add(dobField, gbc);

        // Special Requests field
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Special Requests:"), gbc);
        gbc.gridx = 1;
        JTextField requestsField = new JTextField(20);
        requestFields.add(requestsField);
        panel.add(requestsField, gbc);

        return panel;
    }

    private boolean validateAndSavePassengers() {
        // Clear previous passengers
        passengers.clear();

        // Validate all fields
        for (int i = 0; i < numSeats; i++) {
            String name = nameFields.get(i).getText().trim();
            String passport = passportFields.get(i).getText().trim();
            String dob = dobFields.get(i).getText().trim();
            String requests = requestFields.get(i).getText().trim();

            // Validate required fields
            if (name.isEmpty() || passport.isEmpty() || dob.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields for Passenger " + (i + 1),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate date format
            if (!dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this,
                    "Invalid date format for Passenger " + (i + 1) + ". Use YYYY-MM-DD",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Create passenger using PassengerService
            Passenger passenger = bookingSystem.getPassengerService().createPassenger(name, passport, dob, requests);
            if (passenger == null) {
                JOptionPane.showMessageDialog(this,
                    "Failed to create passenger record for passenger " + (i + 1),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            passengers.add(passenger);
        }
        return true;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
} 