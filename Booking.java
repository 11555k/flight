public class Booking {
    private int bookingId;
    private int userId;
    private int flightId;
    private int numSeats;
    private String bookingDate;
    private String status;

    public Booking(int userId, int flightId, int numSeats) {
        this.userId = userId;
        this.flightId = flightId;
        this.numSeats = numSeats;
        this.bookingDate = java.time.LocalDate.now().toString();
        this.status = "Pending";
    }

    public Booking(int bookingId, int userId, int flightId, int numSeats, String bookingDate, String status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.flightId = flightId;
        this.numSeats = numSeats;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    // Getters
    public int getBookingId() { return bookingId; }
    public int getUserId() { return userId; }
    public int getFlightId() { return flightId; }
    public int getNumSeats() { return numSeats; }
    public String getBookingDate() { return bookingDate; }
    public String getStatus() { return status; }

    // Setters
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setFlightId(int flightId) { this.flightId = flightId; }
    public void setNumSeats(int numSeats) { this.numSeats = numSeats; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Booking ID: %d, Flight ID: %d, Seats: %d, Date: %s, Status: %s",
            bookingId, flightId, numSeats, bookingDate, status);
    }
} 