import java.util.ArrayList;
import java.util.List;

public class BookingSystem {
    private ArrayList<Flight> flights;
    private DatabaseManager dbManager;
    private PassengerService passengerService;
    private UserService userService;

    public BookingSystem() {
        flights = new ArrayList<>();
        dbManager = new DatabaseManager();
        passengerService = new PassengerService(dbManager);
        userService = new UserService(dbManager);
        dbManager.loadFlights(this);
    }

    // This method is for adding a loaded flight during initialization
    public void addFlightLoaded(Flight flight) {
        flights.add(flight);
    }

    // This method is for adding a *new* flight created by the user/system
    public void addNewFlight(Flight flight) {
        flights.add(flight);
        dbManager.saveFlight(flight);
    }

    public Flight findFlight(String flightNumber) {
        for (Flight flight : flights) {
            if (flight.getFlightNumber().equals(flightNumber)) {
                return flight;
            }
        }
        return null;
    }

    public ArrayList<Flight> getAllFlights() {
        // Fetch flights directly from the database
        return dbManager.getAllFlightsFromDB();
    }

    public void displayAllFlights() {
        if (flights.isEmpty()) {
            System.out.println("No flights available.");
            return;
        }
        for (Flight flight : flights) {
            System.out.println(flight);
        }
    }

    public void updateFlightInDatabase(Flight flight) {
        dbManager.updateFlightSeats(flight.getFlightNumber(), flight.getBookedSeats());
    }

    public Flight findFlightById(int flightId) {
        return dbManager.getFlightById(flightId);
    }

    // Getter for the DatabaseManager instance
    public DatabaseManager getDbManager() {
        return dbManager;
    }

    // Method to handle creating a booking for a user
    public boolean createBooking(User user, int flightId, int numSeats) {
        if (user == null) {
            System.err.println("Cannot create booking: User is null.");
            return false;
        }
         // Get the flight to validate availability before creating the booking record
         Flight flight = dbManager.getFlightById(flightId);
         if (flight == null) {
             System.err.println("Cannot create booking: Flight not found with ID " + flightId);
             return false;
         }

         if (flight.getAvailableSeats() < numSeats) {
             System.err.println("Cannot create booking: Not enough seats available on flight " + flight.getFlightNumber());
             return false;
         }

        // Create the booking object with default "Pending" status
        Booking booking = new Booking(user.getId(), flightId, numSeats);
        // Save the booking to the database
        boolean success = dbManager.saveBooking(booking);

        if (success) {
            // For now, flight seats are updated immediately upon booking creation (even if pending)
            // In a more complex system, this update might happen upon payment confirmation.
            // Let's keep it simple and update seats now.
            flight.setBookedSeats(flight.getBookedSeats() + numSeats);
            dbManager.updateFlightSeats(flight.getFlightNumber(), flight.getBookedSeats());
        }

        return success;
    }

    // New method to update booking status (called after successful payment)
    public boolean processPayment(int bookingId) {
        // In a real system, this would involve payment gateway integration.
        // Here, we'll just update the booking status to "Paid".
        System.out.println("Processing payment for booking ID: " + bookingId);
        boolean success = dbManager.updateBookingStatus(bookingId, "Paid");
        if (success) {
             System.out.println("Payment successful for booking ID: " + bookingId + ". Status updated to Paid.");
        } else {
             System.out.println("Payment processing failed for booking ID: " + bookingId + ". Status not updated.");
        }
        return success;
    }

    // Method to get bookings for a specific user
    public List<Booking> getUserBookings(User user) {
         if (user == null) {
             System.err.println("Cannot get user bookings: User is null.");
             return new ArrayList<>();
         }
        return dbManager.getUserBookings(user.getId());
    }

    // Method to handle cancelling seats from a booking for a user
    public int[] cancelBookingSeats(User user, int bookingId, int numSeatsToCancel) {
         if (user == null) {
             System.err.println("Cannot cancel booking seats: User is null.");
             return null;
         }
        // The dbManager.cancelBooking method already verifies user ownership
        return dbManager.cancelBooking(bookingId, user.getId(), numSeatsToCancel);
    }

    // --- Agent Functionalities ---

    // Method for Agent to add a new flight
    public boolean addFlight(Flight flight) {
        System.out.println("BookingSystem: addFlight called");
        // Check if a flight with the same number already exists (optional, but good practice)
        if (findFlight(flight.getFlightNumber()) != null) {
            System.out.println("Flight with number " + flight.getFlightNumber() + " already exists.");
            return false;
        }
        // Save the new flight to the database
        dbManager.saveFlight(flight);
        // Note: We might need to decide if we update the in-memory list here or rely on reloading flights
        // For now, let's just save to DB and rely on refreshing the GUI which calls getAllFlights (from DB).
        System.out.println("Flight " + flight.getFlightNumber() + " added.");
        return true;
    }

    // Method for Agent to update an existing flight
    public boolean updateFlight(Flight flight) {
         System.out.println("BookingSystem: updateFlight called");
        // Check if the flight exists before updating
         if (dbManager.getFlightId(flight.getFlightNumber()) == -1) { // Use getFlightId to check existence by number
             System.out.println("Flight with number " + flight.getFlightNumber() + " not found for update.");
             return false;
         }
        // Save (update) the flight in the database
        dbManager.saveFlight(flight); // saveFlight handles both insert and replace (update)
         System.out.println("Flight " + flight.getFlightNumber() + " updated.");
        return true;
    }

    // Method for Agent to delete a flight
    public boolean deleteFlight(String flightNumber) {
         System.out.println("BookingSystem: deleteFlight called");
        // Check if the flight exists before deleting
         int flightId = dbManager.getFlightId(flightNumber);
         if (flightId == -1) {
             System.out.println("Flight with number " + flightNumber + " not found for deletion.");
             return false;
         }
        // TODO: Add logic to check for and handle existing bookings for this flight before deleting.
        // For simplicity now, we'll just delete the flight.

        // Delete the flight from the database
        boolean success = dbManager.deleteFlight(flightNumber); // Need a deleteFlight method in DatabaseManager
         if (success) {
             System.out.println("Flight " + flightNumber + " deleted.");
         }
        return success;
    }

    // Method for Agent to create a booking for a specific customer
    public boolean createBookingForCustomer(String customerUsername, int flightId, int numSeats) {
        System.out.println("BookingSystem: createBookingForCustomer called");
        // Find the customer user first
        User customer = dbManager.getUserByUsername(customerUsername); // Need a getUserByUsername method in DatabaseManager
        if (customer == null || !"Customer".equals(customer.getRole())) {
            System.out.println("Customer user '" + customerUsername + "' not found or is not a customer.");
            return false;
        }

        // Then use the existing createBooking logic with the customer user
        return createBooking(customer, flightId, numSeats);
    }

    // Method for Agent to view all bookings (or potentially filter)
    public List<Booking> getAllBookings() {
        System.out.println("BookingSystem: getAllBookings called");
        // Need a method in DatabaseManager to get all bookings
        return dbManager.getAllBookings(); // Need getAllBookings method in DatabaseManager
    }

    // Method for Agent to cancel any booking by booking ID
    public boolean cancelBooking(int bookingId) {
        System.out.println("BookingSystem: cancelBooking (by Agent) called for booking ID " + bookingId);
         // Need a method in DatabaseManager to cancel booking by ID without user check (for Agent)
        // Or, we can reuse the existing cancelBooking method in DatabaseManager
        // which checks user ID, and pass the booking's actual user ID.
        // Let's add a specific method in DatabaseManager for Agent cancellation.

        // Alternative: Modify DatabaseManager.cancelBooking to accept an 'agent' flag or similar.
        // Let's add a new method in DatabaseManager: agentCancelBooking(int bookingId).

         // Need to implement agentCancelBooking in DatabaseManager
        int[] cancellationResult = dbManager.agentCancelBooking(bookingId);

         if (cancellationResult != null && cancellationResult.length == 2) {
             int flightIdToUpdate = cancellationResult[0];
             int actualSeatsCancelled = cancellationResult[1];

             // Update the flight's booked seats count in the database
             Flight flight = dbManager.getFlightById(flightIdToUpdate);
             if (flight != null) {
                 flight.setBookedSeats(flight.getBookedSeats() - actualSeatsCancelled);
                 dbManager.updateFlightSeats(flight.getFlightNumber(), flight.getBookedSeats());
             }
             return true; // Success
         }
        return false; // Failure
    }

    // --- Administrator Functionalities (Placeholder Methods) ---
    // Moved to Administrator.java

    public boolean modifyBooking(int bookingId, int newNumSeats) {
        try {
            // Get the current booking
            Booking booking = dbManager.getBookingById(bookingId);
            if (booking == null) {
                return false;
            }

            // Get the flight
            Flight flight = dbManager.getFlightById(booking.getFlightId());
            if (flight == null) {
                return false;
            }

            // Calculate the difference in seats
            int seatDifference = newNumSeats - booking.getNumSeats();
            
            // Check if we can accommodate the new number of seats
            if (flight.getAvailableSeats() < seatDifference) {
                return false;
            }

            // Update the booking in the database
            return dbManager.updateBooking(bookingId, newNumSeats);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Add getter for PassengerService
    public PassengerService getPassengerService() {
        return passengerService;
    }

    // Add getter for UserService
    public UserService getUserService() {
        return userService;
    }
} 