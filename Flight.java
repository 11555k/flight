public class Flight {
    private String flightNumber;
    private String departure;
    private String destination;
    private int capacity;
    private int bookedSeats;
    private double price;

    public Flight(String flightNumber, String departure, String destination, int capacity, double price) {
        this.flightNumber = flightNumber;
        this.departure = departure;
        this.destination = destination;
        this.capacity = capacity;
        this.price = price;
    }

    public int getAvailableSeats() {
        return capacity - bookedSeats;
    }

    // Getters
    public String getFlightNumber() {
        return flightNumber;
    }

    public String getDeparture() {
        return departure;
    }

    public String getDestination() {
        return destination;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public double getPrice() {
        return price;
    }

    // Setters
    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("Flight %s: %s to %s (Capacity: %d, Booked: %d, Available: %d, Price: $%.2f)",
            flightNumber, departure, destination, capacity, bookedSeats, getAvailableSeats(), price);
    }
} 