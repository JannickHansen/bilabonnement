package dk.kea.bilabonnement.model;

public class Skaderapport {
    //lavet af Thomas
    private int skadeId;
    private int lejeaftaleId;
    private String skade;
    private double skadePris;

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

    public Skaderapport(String skade, double skadePris, int kundeId) {
        this.skade = skade;
        this.skadePris = skadePris;
        this.kundeId = kundeId;
    }

    public Skaderapport(int skadeId, String skade, double skadePris, int kundeId, String chassisNumber) {
        this.skadeId = skadeId;
        this.skade = skade;
        this.skadePris = skadePris;
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

    public int getKundeId() {
        return kundeId;
    }

    public void setKundeId(int kundeId) {
        this.kundeId = kundeId;
    }

    public int getLejeaftaleId() {
        return lejeaftaleId;
    }

    public void setLejeaftaleId(int lejeaftaleId) {
        this.lejeaftaleId = lejeaftaleId;
    }
}
