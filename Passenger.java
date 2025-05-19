public class Passenger {

    private int passengerId;
    private String name;
    private String passportNumber;
    private String dateOfBirth;
    private String specialRequests;

    public Passenger(int passengerId, String name, String passportNumber, String dateOfBirth) {
        this.passengerId = passengerId;
        this.name = name;
        this.passportNumber = passportNumber;
        this.dateOfBirth = dateOfBirth;
        this.specialRequests = "";
    }

    // Getters
    public int getPassengerId() {
        return passengerId;
    }

    public String getName() {
        return name;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    // Setters
    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }
} 