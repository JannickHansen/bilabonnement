package dk.kea.bilabonnement.model;

public class Bil {
    String stelnummer;
    String brand;
    String model;
    String type;
    String nummerplade;
    String fuel;
    String status;
    int medarbejderID;

    public Bil(String stelnummer, String brand, String model, String type, String nummerplade, String fuel) {
        this.stelnummer = stelnummer;
        this.brand = brand;
        this.model = model;
        this.type = type;
        this.nummerplade = nummerplade;
        this.fuel = fuel;
    }
    public Bil(String stelnummer, String brand, String model, String type, String nummerplade, String fuel, String fejlbesked) {
        this.stelnummer = stelnummer;
        this.brand = brand;
        this.model = model;
        this.type = type;
        this.nummerplade = nummerplade;
        this.fuel = fuel;
    }
}
