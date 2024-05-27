package dk.kea.bilabonnement.model;

public class BilModel {
    //lavet af Jannick
    String chassisNumber;
    String brand;
    String carModel;
    String type;
    String licensePlate;
    String fuel;
    String status;
    int km;

    public BilModel(String chassisNumber, String brand, String carModel, String type, String licensePlate, String fuel, int km) {
        this.chassisNumber = chassisNumber;
        this.brand = brand;
        this.carModel = carModel;
        this.type = type;
        this.licensePlate = licensePlate;
        this.fuel = fuel;
        this.km = km;
    }

    public BilModel() {}

    // Get methoder
    public String getChassisNumber() {
        return this.chassisNumber;
    }
    public String getBrand() {
        return this.brand;
    }
    public String getCarModel() {
        return this.carModel;
    }
    public String getType() {
        return this.type;
    }
    public String getLicensePlate() {
        return this.licensePlate;
    }
    public String getFuel() {
        return this.fuel;
    }
    public String getStatus() {
        return this.status;
    }
    public int getKm() {
        return this.km;
    }

    // Set methoder
    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    public void setFuel(String fuel) {
        this.fuel = fuel;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setKm(int km) {
        this.km = km;
    }

    // Metoder til at ændre unik status på bil objekter
    public void setAvailable() {
        this.status = "Ledig";
    }


}
