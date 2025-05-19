import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PaymentWindow extends JDialog {
    private BookingSystem bookingSystem;
    private int bookingId;

    // Payment form components
    private JTextField cardNumberField;
    private JTextField expiryDateField;
    private JTextField cvvField;
    private JButton payButton;

    public PaymentWindow(JFrame parent, BookingSystem system, int bookingId) {
        super(parent, "Process Payment", true);
        this.bookingSystem = system;
        this.bookingId = bookingId;

        // Initialize payment form layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form labels and input fields
        JLabel cardNumberLabel = new JLabel("Card Number:");
        JLabel expiryDateLabel = new JLabel("Expiry Date (MM/YY):");
        JLabel cvvLabel = new JLabel("CVV:");

        cardNumberField = new JTextField(20);
        expiryDateField = new JTextField(5);
        cvvField = new JTextField(3);
        payButton = new JButton("Pay");

        // Add components to form
        gbc.gridx = 0; gbc.gridy = 0; add(cardNumberLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; add(cardNumberField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(expiryDateLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; add(expiryDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(cvvLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; add(cvvField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(payButton, gbc);

        payButton.addActionListener(e -> processPayment());

        pack();
        setLocationRelativeTo(parent);
    }

    private void processPayment() {
        String cardNumber = cardNumberField.getText().trim();
        String expiryDate = expiryDateField.getText().trim();
        String cvv = cvvField.getText().trim();

        // Validate required fields
        if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all payment details.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate payment details format
        if (!cardNumber.matches("^[0-9]{13,19}$")) {
             JOptionPane.showMessageDialog(this, "Please enter a valid card number.", "Input Error", JOptionPane.WARNING_MESSAGE);
             return;
        }

        if (!expiryDate.matches("^(0[1-9]|1[0-2])/([0-9]{2})$")) {
             JOptionPane.showMessageDialog(this, "Please enter a valid expiry date in MM/YY format.", "Input Error", JOptionPane.WARNING_MESSAGE);
             return;
        }

        if (!cvv.matches("^[0-9]{3,4}$")) {
             JOptionPane.showMessageDialog(this, "Please enter a valid CVV (3 or 4 digits).", "Input Error", JOptionPane.WARNING_MESSAGE);
             return;
        }

        // Process payment and update booking status
        boolean paymentSuccessful = simulatePayment(cardNumber, expiryDate, cvv);

        if (paymentSuccessful) {
            boolean statusUpdated = bookingSystem.processPayment(bookingId);

            if (statusUpdated) {
                JOptionPane.showMessageDialog(this, "Payment successful! Booking status updated to Paid.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Payment successful, but failed to update booking status.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Payment failed. Please check your details.", "Payment Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean simulatePayment(String cardNumber, String expiryDate, String cvv) {
        // Payment gateway integration would be implemented here
        return !cardNumber.isEmpty() && !expiryDate.isEmpty() && !cvv.isEmpty();
    }
} 