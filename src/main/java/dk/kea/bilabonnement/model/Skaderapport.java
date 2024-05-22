package dk.kea.bilabonnement.model;

public class Skaderapport {
    private int skadeId;
    private String skade;
    private double skadePris;
    private int medarbejderId;
    private int kundeId;

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }

    private String chassisNumber;

    public Skaderapport() {
    }

    public Skaderapport(String skade, double skadePris, int medarbejderId, int kundeId) {
        this.skade = skade;
        this.skadePris = skadePris;
        this.medarbejderId = medarbejderId;
        this.kundeId = kundeId;
    }

    public Skaderapport(int skadeId, String skade, double skadePris, int medarbejderId, int kundeId, String chassisNumber) {
        this.skadeId = skadeId;
        this.skade = skade;
        this.skadePris = skadePris;
        this.medarbejderId = medarbejderId;
        this.kundeId = kundeId;
        this.chassisNumber = chassisNumber;
    }

    public int getSkadeId() {
        return skadeId;
    }

    public void setSkadeId(int skadeId) {
        this.skadeId = skadeId;
    }

    public String getSkade() {
        return skade;
    }

    public void setSkade(String skade) {
        this.skade = skade;
    }

    public double getSkadePris() {
        return skadePris;
    }

    public void setSkadePris(double skadePris) {
        this.skadePris = skadePris;
    }

    public int getMedarbejderId() {
        return medarbejderId;
    }

    public void setMedarbejderId(int medarbejderId) {
        this.medarbejderId = medarbejderId;
    }

    public int getKundeId() {
        return kundeId;
    }

    public void setKundeId(int kundeId) {
        this.kundeId = kundeId;
    }
}
