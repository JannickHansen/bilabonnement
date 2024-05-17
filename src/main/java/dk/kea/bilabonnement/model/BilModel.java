package dk.kea.bilabonnement.model;

public class BilModel {
    String chassisNumber;
    String brand;
    String carModel;
    String type;
    String licensePlate;
    String fuel;
    String status;
    int employeeID;

    public BilModel(String chassisNumber, String brand, String carModel, String type, String licensePlate, String fuel) {
        this.chassisNumber = chassisNumber;
        this.brand = brand;
        this.carModel = carModel;
        this.type = type;
        this.licensePlate = licensePlate;
        this.fuel = fuel;
    }

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
    public int getEmployeeID() {
        return this.employeeID;
    }
    public void setAvailable() {
        this.status = "Ledig";
    }
}
