import java.sql.*;
import java.util.Random;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.ArrayList;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:flight_system.db?enable_load_extension=false&busy_timeout=5000";
    private Connection connection;
    private Random random = new Random();
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    // Add getConnection method with retry logic
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            // Enable foreign keys
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            // Set busy timeout
            connection.createStatement().execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    // Add retry logic for database operations
    private <T> T executeWithRetry(DatabaseOperation<T> operation) {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                return operation.execute(getConnection());
            } catch (SQLException e) {
                if (e.getMessage().contains("database is locked") && retries < MAX_RETRIES - 1) {
                    retries++;
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    e.printStackTrace();
                    break;
                }
            }
        }
        return null;
    }

    // Interface for database operations
    private interface DatabaseOperation<T> {
        T execute(Connection conn) throws SQLException;
    }

    // Arrays of cities for random flight generation
    public final String[] cities = {
        "New York", "London", "Paris", "Tokyo", "Berlin", "Rome", "Madrid", "Dubai",
        "Singapore", "Sydney", "Toronto", "Moscow", "Beijing", "Mumbai", "Cairo"
    };

    // Getter method for cities array
    public String[] getCities() {
        return cities;
    }

    // City coordinates (latitude, longitude)
    private final double[][] cityCoordinates = {
        {40.7128, -74.0060},  // New York
        {51.5074, -0.1278},   // London
        {48.8566, 2.3522},    // Paris
        {35.6762, 139.6503},  // Tokyo
        {52.5200, 13.4050},   // Berlin
        {41.9028, 12.4964},   // Rome
        {40.4168, -3.7038},   // Madrid
        {25.2048, 55.2708},   // Dubai
        {1.3521, 103.8198},   // Singapore
        {-33.8688, 151.2093}, // Sydney
        {43.6532, -79.3832},  // Toronto
        {55.7558, 37.6173},   // Moscow
        {39.9042, 116.4074},  // Beijing
        {19.0760, 72.8777},   // Mumbai
        {30.0444, 31.2357}    // Cairo
    };

    // Calculate distance between two cities using Haversine formula
    private double calculateDistance(int city1Index, int city2Index) {
        double lat1 = Math.toRadians(cityCoordinates[city1Index][0]);
        double lon1 = Math.toRadians(cityCoordinates[city1Index][1]);
        double lat2 = Math.toRadians(cityCoordinates[city2Index][0]);
        double lon2 = Math.toRadians(cityCoordinates[city2Index][1]);

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;

        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double r = 6371; // Earth's radius in kilometers

        return c * r;
    }

    // Calculate price based on distance and add some randomness
    public double calculatePrice(int originIndex, int destinationIndex) {
        double distance = calculateDistance(originIndex, destinationIndex);
        // Base price: $0.15 per kilometer
        double basePrice = distance * 0.15;
        // Add 20% random variation
        double variation = basePrice * 0.2;
        return basePrice + (random.nextDouble() * variation * 2 - variation);
    }

    public DatabaseManager() {
        try {
            // Print the current working directory
            System.out.println("Current working directory: " + System.getProperty("user.dir"));
            
            // Try to load the SQLite JDBC driver
            try {
                Class.forName("org.sqlite.JDBC");
                System.out.println("SQLite JDBC driver loaded successfully");
            } catch (ClassNotFoundException e) {
                String error = "SQLite JDBC driver not found. Please make sure sqlite-jdbc.jar is in your classpath.\n" +
                             "Error: " + e.getMessage();
                System.err.println(error);
                JOptionPane.showMessageDialog(null, error, "Driver Error", JOptionPane.ERROR_MESSAGE);
                connection = null; // Ensure connection is null on failure
                return;
            }

            // Try to create connection
            try {
                connection = DriverManager.getConnection(DB_URL);
                if (connection != null) {
                    System.out.println("Database connection established successfully");
                } else {
                    System.err.println("DriverManager.getConnection returned null.");
                    JOptionPane.showMessageDialog(null, "Database connection could not be established.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (SQLException e) {
                String error = "Failed to connect to database.\n" +
                             "URL: " + DB_URL + "\n" +
                             "Error: " + e.getMessage();
                System.err.println(error);
                JOptionPane.showMessageDialog(null, error, "Connection Error", JOptionPane.ERROR_MESSAGE);
                connection = null; // Ensure connection is null on failure
                return;
            }

            // Check if connection is valid before proceeding
            if (connection == null || connection.isClosed()) {
                 System.err.println("Database connection is not valid after establishment attempt.");
                 JOptionPane.showMessageDialog(null, "Database connection is invalid.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            createTables();
            if (isDatabaseEmpty()) {
                addRandomFlights();
            }
            // Add default admin and agent users if they don't exist
            addDefaultUsers();

        } catch (Exception e) {
            String error = "Unexpected error in DatabaseManager constructor: " + e.getMessage() + "\n" +
                          "Stack trace: " + e.toString();
            System.err.println(error);
            JOptionPane.showMessageDialog(null, error, "Unexpected Error", JOptionPane.ERROR_MESSAGE);
             connection = null; // Ensure connection is null on unexpected error
        }
         // Log the final state of the connection
         if (connection != null) {
             System.out.println("DatabaseManager constructor finished. Connection is open.");
         } else {
             System.out.println("DatabaseManager constructor finished. Connection is null or closed.");
         }
    }

    private boolean isDatabaseEmpty() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM flights");
            int count = rs.getInt(1);
            rs.close();
            stmt.close();
            return count == 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    private void addRandomFlights() {
        try {
            for (int i = 0; i < 10; i++) {
                String flightNumber = String.format("FL%03d", i + 1);
                int originIndex = random.nextInt(cities.length);
                int destinationIndex;
                do {
                    destinationIndex = random.nextInt(cities.length);
                } while (destinationIndex == originIndex);

                String origin = cities[originIndex];
                String destination = cities[destinationIndex];
                int capacity = random.nextInt(151) + 50; // Random capacity between 50 and 200
                double price = calculatePrice(originIndex, destinationIndex);

                Flight flight = new Flight(flightNumber, origin, destination, capacity, price);
                saveFlight(flight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "email TEXT NOT NULL," +
                    "phone_number TEXT NOT NULL," +
                    "user_id TEXT UNIQUE NOT NULL," +
                    "role TEXT NOT NULL DEFAULT 'Customer')");

            // Create flights table
            stmt.execute("CREATE TABLE IF NOT EXISTS flights (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "flight_number TEXT UNIQUE NOT NULL," +
                    "origin TEXT NOT NULL," +
                    "destination TEXT NOT NULL," +
                    "capacity INTEGER NOT NULL," +
                    "booked_seats INTEGER DEFAULT 0," +
                    "price REAL DEFAULT 100.0)");

            // Create bookings table
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "flight_id INTEGER NOT NULL," +
                    "num_seats INTEGER NOT NULL," +
                    "booking_date TEXT NOT NULL," +
                    "status TEXT NOT NULL DEFAULT 'Pending'," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (flight_id) REFERENCES flights(id))");

            // Create passengers table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS passengers (
                    passenger_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    passport_number TEXT NOT NULL UNIQUE,
                    date_of_birth TEXT NOT NULL,
                    special_requests TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create booking_passengers table (junction table for many-to-many relationship)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS booking_passengers (
                    booking_id INTEGER,
                    passenger_id INTEGER,
                    PRIMARY KEY (booking_id, passenger_id),
                    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
                    FOREIGN KEY (passenger_id) REFERENCES passengers(passenger_id) ON DELETE CASCADE
                )
            """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveFlight(Flight flight) {
        try {
             System.out.println("Saving flight: " + flight.getFlightNumber());
            // First check if the flight exists
            String checkSql = "SELECT id FROM flights WHERE flight_number = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setString(1, flight.getFlightNumber());
            ResultSet rs = checkStmt.executeQuery();
            
            boolean flightExists = rs.next();
            rs.close();
            checkStmt.close();

            String sql;
            PreparedStatement pstmt;
            
            if (flightExists) {
                // Flight exists, use UPDATE
                sql = "UPDATE flights SET origin = ?, destination = ?, capacity = ?, booked_seats = ?, price = ? WHERE flight_number = ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, flight.getDeparture());
                pstmt.setString(2, flight.getDestination());
                pstmt.setInt(3, flight.getCapacity());
                pstmt.setInt(4, flight.getBookedSeats());
                pstmt.setDouble(5, flight.getPrice());
                pstmt.setString(6, flight.getFlightNumber());
            } else {
                // Flight doesn't exist, use INSERT
                sql = "INSERT INTO flights (flight_number, origin, destination, capacity, booked_seats, price) VALUES (?, ?, ?, ?, ?, ?)";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, flight.getFlightNumber());
                pstmt.setString(2, flight.getDeparture());
                pstmt.setString(3, flight.getDestination());
                pstmt.setInt(4, flight.getCapacity());
                pstmt.setInt(5, flight.getBookedSeats());
                pstmt.setDouble(6, flight.getPrice());
            }
            
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            if (affectedRows > 0) {
                System.out.println("Flight " + (flightExists ? "updated" : "saved") + ": " + flight.getFlightNumber());
            } else {
                System.out.println("No changes made to flight: " + flight.getFlightNumber());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadFlights(BookingSystem system) {
        try {
            System.out.println("Loading flights from database...");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM flights");
            
            while (rs.next()) {
                Flight flight = new Flight(
                    rs.getString("flight_number"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getInt("capacity"),
                    rs.getDouble("price")
                );
                flight.setBookedSeats(rs.getInt("booked_seats"));
                system.addFlightLoaded(flight);
                System.out.println("Loaded flight: " + flight.getFlightNumber());
            }
            
            rs.close();
            stmt.close();
            System.out.println("Flights loaded from database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFlightSeats(String flightNumber, int bookedSeats) {
        try {
             System.out.println("Updating seats for flight: " + flightNumber);
            String sql = "UPDATE flights SET booked_seats = ? WHERE flight_number = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, bookedSeats);
            pstmt.setString(2, flightNumber);
            pstmt.executeUpdate();
            pstmt.close();
             System.out.println("Seats updated for flight: " + flightNumber);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long registerUser(String username, String password, String email, String phoneNumber, String userId, String role) {
        Long result = executeWithRetry(conn -> {
            String sql = "INSERT INTO users (username, password, email, phone_number, user_id, role) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, email);
                pstmt.setString(4, phoneNumber);
                pstmt.setString(5, userId);
                pstmt.setString(6, role);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            System.out.println("User registered with DB ID: " + generatedKeys.getLong(1) + ", User ID: " + userId);
                            return generatedKeys.getLong(1);
                        }
                    }
                }
                return -3L; // Indicate other registration failure
            } catch (SQLException e) {
                if (e.getMessage().contains("UNIQUE constraint failed: users.username")) {
                    System.err.println("Registration failed: Username already exists.");
                    return -1L;
                } else if (e.getMessage().contains("UNIQUE constraint failed: users.user_id")) {
                    System.err.println("Registration failed: User ID already exists.");
                    return -2L;
                }
                throw e;
            }
        });
        return result != null ? result : -3L;
    }

    public User loginUser(String username, String password) {
        try {
            String sql = "SELECT id, username, email, phone_number, user_id, role FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phone_number");
                String userId = rs.getString("user_id");
                String role = rs.getString("role");

                User loggedInUser;

                // Create the appropriate User subclass based on role
                switch (role) {
                    case "Administrator":
                        loggedInUser = new Administrator(id, username, password, email, phoneNumber, userId);
                        break;
                    case "Agent":
                        loggedInUser = new Agent(id, username, password, email, phoneNumber, userId);
                        break;
                    case "Customer":
                    default:
                        loggedInUser = new Customer(id, username, password, email, phoneNumber, userId);
                        break;
                }

                System.out.println("User logged in: " + loggedInUser.getUsername() + " (User ID: " + loggedInUser.getUserId() + ", Role: " + loggedInUser.getRole() + ")");
                rs.close();
                pstmt.close();
                return loggedInUser;
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Login failed for username: " + username);
        return null;
    }

    public boolean updateUser(int dbId, String email, String phoneNumber, String newPassword) {
        try {
            String sql;
            PreparedStatement pstmt;

            if (newPassword != null && !newPassword.isEmpty()) {
                // Update email, phone number, and password
                sql = "UPDATE users SET email = ?, phone_number = ?, password = ? WHERE db_id = ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, email);
                pstmt.setString(2, phoneNumber);
                pstmt.setString(3, newPassword);
                pstmt.setInt(4, dbId);
            } else {
                // Update email and phone number only
                sql = "UPDATE users SET email = ?, phone_number = ? WHERE db_id = ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, email);
                pstmt.setString(2, phoneNumber);
                pstmt.setInt(3, dbId);
            }

            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveBooking(Booking booking) {
        String sql = "INSERT INTO bookings (user_id, flight_id, num_seats, booking_date, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            if (conn == null || conn.isClosed()) {
                 System.err.println("Error in saveBooking: Database connection is null or closed.");
                 return false;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, booking.getUserId());
            pstmt.setInt(2, booking.getFlightId());
            pstmt.setInt(3, booking.getNumSeats());
            pstmt.setString(4, booking.getBookingDate());
            pstmt.setString(5, booking.getStatus());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Booking> getUserBookings(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            if (conn == null || conn.isClosed()) {
                 System.err.println("Error in getUserBookings: Database connection is null or closed.");
                 return bookings;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("flight_id"),
                    rs.getInt("num_seats"),
                    rs.getString("booking_date"),
                    rs.getString("status")
                );
                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return bookings;
    }

    // Modified cancelBooking to handle partial cancellation and return flightId and seats cancelled
    public int[] cancelBooking(int bookingId, int userId, int numSeatsToCancel) {
        String selectSql = "SELECT flight_id, num_seats FROM bookings WHERE id = ? AND user_id = ?";
        String updateSql = "UPDATE bookings SET num_seats = ? WHERE id = ?";
        String deleteSql = "DELETE FROM bookings WHERE id = ?";

        int flightId = -1;
        int actualSeatsCancelled = 0;

        try (PreparedStatement selectPstmt = connection.prepareStatement(selectSql)) {
            selectPstmt.setInt(1, bookingId);
            selectPstmt.setInt(2, userId);
            ResultSet rs = selectPstmt.executeQuery();

            if (rs.next()) {
                flightId = rs.getInt("flight_id");
                int currentNumSeats = rs.getInt("num_seats");
                rs.close();

                // Validate numSeatsToCancel
                if (numSeatsToCancel <= 0 || numSeatsToCancel > currentNumSeats) {
                    System.err.println("Invalid number of seats to cancel: " + numSeatsToCancel + " for booking ID: " + bookingId);
                    return null; // Indicate failure due to invalid seats
                }

                if (numSeatsToCancel < currentNumSeats) {
                    // Partial cancellation
                    int remainingSeats = currentNumSeats - numSeatsToCancel;
                    try (PreparedStatement updatePstmt = connection.prepareStatement(updateSql)) {
                        updatePstmt.setInt(1, remainingSeats);
                        updatePstmt.setInt(2, bookingId);
                        int affectedRows = updatePstmt.executeUpdate();
                        if (affectedRows > 0) {
                            actualSeatsCancelled = numSeatsToCancel;
                            System.out.println("Partially cancelled " + actualSeatsCancelled + " seats for booking ID " + bookingId);
                            return new int[]{flightId, actualSeatsCancelled}; // Success
                        }
                    }
                } else {
                    // Full cancellation (numSeatsToCancel == currentNumSeats)
                    try (PreparedStatement deletePstmt = connection.prepareStatement(deleteSql)) {
                        deletePstmt.setInt(1, bookingId);
                        int affectedRows = deletePstmt.executeUpdate();
                         if (affectedRows > 0) {
                            actualSeatsCancelled = currentNumSeats; // Cancelled all seats
                             System.out.println("Fully cancelled booking ID " + bookingId);
                             return new int[]{flightId, actualSeatsCancelled}; // Success
                         }
                    }
                }
            } else {
                // Booking not found or does not belong to the user
                System.out.println("Attempted to cancel booking ID " + bookingId + " for user ID " + userId + ", but verification failed.");
                return null; // Indicate failure
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Indicate general failure
    }

    public void close() {
        try {
             System.out.println("Attempting to close database connection...");
            if (connection != null && !connection.isClosed()) {
                connection.close();
                 System.out.println("Database connection closed.");
            } else {
                 System.out.println("Database connection is already null or closed. No action needed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Flight getFlightById(int flightId) {
        try {
            String sql = "SELECT * FROM flights WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, flightId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Flight flight = new Flight(
                    rs.getString("flight_number"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getInt("capacity"),
                    rs.getDouble("price")
                );
                flight.setBookedSeats(rs.getInt("booked_seats"));
                return flight;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getFlightId(String flightNumber) {
        String sql = "SELECT id FROM flights WHERE flight_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, flightNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if flight not found
    }

    // New method to get all flights from the database
    public ArrayList<Flight> getAllFlightsFromDB() {
        ArrayList<Flight> flights = new ArrayList<>();
        try {
            String sql = "SELECT * FROM flights";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Flight flight = new Flight(
                    rs.getString("flight_number"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getInt("capacity"),
                    rs.getDouble("price")
                );
                flight.setBookedSeats(rs.getInt("booked_seats"));
                flights.add(flight);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flights;
    }

    private boolean userExists(String username) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            int count = rs.getInt(1);
            rs.close();
            pstmt.close();
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Assume user doesn't exist in case of error
        }
    }

    private void addDefaultUsers() {
        System.out.println("Checking for default users...");
        // Add Admin user if they don't exist
        if (!userExists("admin")) {
            System.out.println("Adding default Admin user...");
            registerUser("admin", "adminpass", "admin@example.com", "111-111-1111", "ADMIN001", "Administrator");
        } else {
             System.out.println("Admin user already exists.");
        }

        // Add Agent user if they don't exist
        if (!userExists("agent")) {
            System.out.println("Adding default Agent user...");
            registerUser("agent", "agentpass", "agent@example.com", "222-222-2222", "AGENT001", "Agent");
        } else {
             System.out.println("Agent user already exists.");
        }
         System.out.println("Default user check finished.");
    }

    // Method to delete a flight by flight number
    public boolean deleteFlight(String flightNumber) {
        String sql = "DELETE FROM flights WHERE flight_number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, flightNumber);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to get a User by username
    public User getUserByUsername(String username) {
        String sql = "SELECT id, username, password, email, phone_number, user_id, role FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phone_number");
                String userId = rs.getString("user_id");
                String role = rs.getString("role");

                // Create the appropriate User subclass based on role
                switch (role) {
                    case "Administrator":
                        return new Administrator(id, username, password, email, phoneNumber, userId);
                    case "Agent":
                        return new Agent(id, username, password, email, phoneNumber, userId);
                    case "Customer":
                    default:
                        return new Customer(id, username, password, email, phoneNumber, userId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to get all bookings
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            if (conn == null || conn.isClosed()) {
                 System.err.println("Error in getAllBookings: Database connection is null or closed.");
                 return bookings;
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                 Booking booking = new Booking(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("flight_id"),
                    rs.getInt("num_seats"),
                    rs.getString("booking_date"),
                    rs.getString("status")
                );
                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
             try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return bookings;
    }

    // Method for Agent to cancel a booking by ID
    public int[] agentCancelBooking(int bookingId) {
        String selectSql = "SELECT flight_id, num_seats FROM bookings WHERE id = ?";
        String deleteSql = "DELETE FROM bookings WHERE id = ?";

        int flightId = -1;
        int actualSeatsCancelled = 0;

        try (PreparedStatement selectPstmt = connection.prepareStatement(selectSql)) {
            selectPstmt.setInt(1, bookingId);
            ResultSet rs = selectPstmt.executeQuery();

            if (rs.next()) {
                flightId = rs.getInt("flight_id");
                int currentNumSeats = rs.getInt("num_seats");
                rs.close();

                try (PreparedStatement deletePstmt = connection.prepareStatement(deleteSql)) {
                    deletePstmt.setInt(1, bookingId);
                    int affectedRows = deletePstmt.executeUpdate();
                     if (affectedRows > 0) {
                        actualSeatsCancelled = currentNumSeats; // Cancelled all seats for this booking
                         System.out.println("Agent cancelled booking ID " + bookingId);
                         return new int[]{flightId, actualSeatsCancelled}; // Success
                     }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Indicate failure
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT id, username, password, email, phone_number, user_id, role FROM users";
        try {
            conn = getConnection();
            if (conn == null || conn.isClosed()) {
                 System.err.println("Error in getAllUsers: Database connection is null or closed.");
                 return users;
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("user_id"),
                    rs.getString("role")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // Do NOT close the connection here (conn.close())!
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    // New method to get only users with the 'Customer' role
    public List<User> getAllCustomerUsers() {
        List<User> users = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT id, username, password, email, phone_number, user_id, role FROM users WHERE role = 'Customer'";
        try {
            conn = getConnection();
             if (conn == null || conn.isClosed()) {
                 System.err.println("Error in getAllCustomerUsers: Database connection is null or closed.");
                 return users;
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("user_id"),
                    rs.getString("role")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
             try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // Do NOT close the connection here (conn.close())!
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    public User getUserById(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT id, username, password, email, phone_number, user_id, role FROM users WHERE id = ?";
        try {
            conn = getConnection();
            if (conn == null || conn.isClosed()) {
                 System.err.println("Error in getUserById: Database connection is null or closed.");
                 return null;
            }
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("user_id"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                // Do NOT close the connection here (conn.close())!
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Booking getBookingById(int bookingId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try {
            conn = getConnection();
             if (conn == null || conn.isClosed()) {
                 System.err.println("Error in getBookingById: Database connection is null or closed.");
                 return null;
            }
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, bookingId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Booking(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("flight_id"),
                    rs.getInt("num_seats"),
                    rs.getString("booking_date"),
                    rs.getString("status")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
             try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean updateBooking(int bookingId, int newNumSeats) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE bookings SET num_seats = ? WHERE id = ?";
        try {
            conn = getConnection();
             if (conn == null || conn.isClosed()) {
                 System.err.println("Error in updateBooking: Database connection is null or closed.");
                 return false;
            }
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, newNumSeats);
            pstmt.setInt(2, bookingId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                 // Do NOT close the connection here (conn.close())!
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // New method for Administrator to update a user's role
    public boolean updateUserRole(int userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            if (conn == null || conn.isClosed()) {
                 System.err.println("Error in updateUserRole: Database connection is null or closed.");
                 return false;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newRole);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // New method to update booking status
    public boolean updateBookingStatus(int bookingId, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            if (conn == null || conn.isClosed()) {
                 System.err.println("Error in updateBookingStatus: Database connection is null or closed.");
                 return false;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, bookingId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Add method to delete a user
    public boolean deleteUser(int userId) {
        return executeWithRetry(conn -> {
            // First check if user has any bookings
            String checkBookingsSql = "SELECT COUNT(*) FROM bookings WHERE user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkBookingsSql)) {
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // User has bookings, cannot delete
                    return false;
                }
            }

            // If no bookings, proceed with deletion
            String deleteSql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, userId);
                int affectedRows = deleteStmt.executeUpdate();
                return affectedRows > 0;
            }
        });
    }

    /**
     * Saves a new passenger to the database
     * @param name Passenger name
     * @param passportNumber Passport number
     * @param dateOfBirth Date of birth
     * @param specialRequests Special requests
     * @return The generated passenger ID, or -1 if failed
     */
    public int savePassenger(String name, String passportNumber, String dateOfBirth, String specialRequests) {
        String sql = "INSERT INTO passengers (name, passport_number, date_of_birth, special_requests) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, passportNumber);
            pstmt.setString(3, dateOfBirth);
            pstmt.setString(4, specialRequests);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Links a passenger to a booking
     * @param bookingId The booking ID
     * @param passengerId The passenger ID
     * @return true if successful
     */
    public boolean linkPassengerToBooking(int bookingId, int passengerId) {
        String sql = "INSERT INTO booking_passengers (booking_id, passenger_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookingId);
            pstmt.setInt(2, passengerId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all passengers for a specific booking
     * @param bookingId The booking ID
     * @return List of passengers
     */
    public List<Passenger> getPassengersForBooking(int bookingId) {
        List<Passenger> passengers = new ArrayList<>();
        String sql = "SELECT p.* FROM passengers p " +
                    "JOIN booking_passengers bp ON p.passenger_id = bp.passenger_id " +
                    "WHERE bp.booking_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Passenger passenger = new Passenger(
                    rs.getInt("passenger_id"),
                    rs.getString("name"),
                    rs.getString("passport_number"),
                    rs.getString("date_of_birth")
                );
                passenger.setSpecialRequests(rs.getString("special_requests"));
                passengers.add(passenger);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return passengers;
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
    public boolean updatePassenger(int passengerId, String name, String passportNumber, 
                                 String dateOfBirth, String specialRequests) {
        String sql = "UPDATE passengers SET name = ?, passport_number = ?, " +
                    "date_of_birth = ?, special_requests = ? WHERE passenger_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, passportNumber);
            pstmt.setString(3, dateOfBirth);
            pstmt.setString(4, specialRequests);
            pstmt.setInt(5, passengerId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 