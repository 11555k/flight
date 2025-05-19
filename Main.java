import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        System.out.println("Main method started.");
        try {
            System.out.println("Calling SwingUtilities.invokeLater...");
            SwingUtilities.invokeLater(() -> {
                System.out.println("Inside SwingUtilities.invokeLater lambda.");
                FlightSystemGUI gui = new FlightSystemGUI();
                System.out.println("FlightSystemGUI object created.");
                gui.setVisible(true);
                System.out.println("Called gui.setVisible(true).");
            });
             System.out.println("SwingUtilities.invokeLater called. Main method finishing.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error launching GUI: " + e.getMessage(), 
                                        "GUI Launch Error", JOptionPane.ERROR_MESSAGE);
        }
    }
} 