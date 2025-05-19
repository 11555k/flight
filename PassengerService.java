import java.util.List;
import java.util.ArrayList;

public class PassengerService {
    private DatabaseManager dbManager;

    public PassengerService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Validates passenger information before booking
     * @param name Passenger name
     * @param passportNumber Passport number
     * @param dateOfBirth Date of birth
     * @return true if all information is valid
     */
    public boolean validatePassengerInfo(String name, String passportNumber, String dateOfBirth) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (passportNumber == null || passportNumber.trim().isEmpty()) {
            return false;
        }
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            return false;
        }
        // Add more validation as needed
        return true;
    }

    /**
     * Creates a new passenger record
     * @param name Passenger name
     * @param passportNumber Passport number
     * @param dateOfBirth Date of birth
     * @param specialRequests Special requests (optional)
     * @return The created Passenger object or null if creation failed
     */
    public Passenger createPassenger(String name, String passportNumber, String dateOfBirth, String specialRequests) {
        if (!validatePassengerInfo(name, passportNumber, dateOfBirth)) {
            return null;
        }
        
        // Save passenger to database and get the generated ID
        int passengerId = dbManager.savePassenger(name, passportNumber, dateOfBirth, specialRequests);
        if (passengerId == -1) {
            return null;
        }
        
        return new Passenger(passengerId, name, passportNumber, dateOfBirth);
    }

    /**
     * Links a passenger to a booking
     * @param bookingId The booking ID
     * @param passengerId The passenger ID
     * @return true if successful
     */
    public boolean linkPassengerToBooking(int bookingId, int passengerId) {
        return dbManager.linkPassengerToBooking(bookingId, passengerId);
    }

    /**
     * Gets all passengers for a specific booking
     * @param bookingId The booking ID
     * @return List of passengers
     */
    public List<Passenger> getPassengersForBooking(int bookingId) {
        return dbManager.getPassengersForBooking(bookingId);
    }

    /**
     * Updates passenger information
     * @param passengerId The passenger ID
     * @param name New name
     * @param passportNumber New passport number
     * @param dateOfBirth New date of birth
     * @param specialRequests New special requests
     * @return true if successful
     */
    public boolean updatePassengerInfo(int passengerId, String name, String passportNumber, 
                                     String dateOfBirth, String specialRequests) {
        if (!validatePassengerInfo(name, passportNumber, dateOfBirth)) {
            return false;
        }
        return dbManager.updatePassenger(passengerId, name, passportNumber, dateOfBirth, specialRequests);
    }

    /**
     * Gets passenger details in a formatted string
     * @param passenger The passenger object
     * @return Formatted string with passenger details
     */
    public String getPassengerDetails(Passenger passenger) {
        return String.format("Passenger: %s\nPassport: %s\nDate of Birth: %s\nSpecial Requests: %s",
            passenger.getName(),
            passenger.getPassportNumber(),
            passenger.getDateOfBirth(),
            passenger.getSpecialRequests().isEmpty() ? "None" : passenger.getSpecialRequests());
    }
} 