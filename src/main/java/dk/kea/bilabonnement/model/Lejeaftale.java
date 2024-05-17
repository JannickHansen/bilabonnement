package dk.kea.bilabonnement.model;

import java.time.LocalDate;

public class Lejeaftale {
    LocalDate pickupdate;
    LocalDate pickuptime;
    String renttype;
    String pickuplocation;
    BilModel bil;

    public Lejeaftale(LocalDate pickupdate, LocalDate pickuptime, String renttype, String pickuplocation, BilModel bil) {
        this.pickupdate = pickupdate;
        this.pickuptime = pickuptime;
        this.renttype = renttype;
        this.pickuplocation = pickuplocation;
        this.bil = bil;
    }

    public LocalDate getPickupdate() {
        return pickupdate;
    }

    public LocalDate getPickuptime() {
        return pickuptime;
    }

    public String getRenttype() {
        return renttype;
    }

    public String getPickuplocation() {
        return pickuplocation;
    }

    public BilModel getBil() {
        return bil;
    }

    public void setPickupdate(LocalDate pickupdate) {
        this.pickupdate = pickupdate;
    }

    public void setPickuptime(LocalDate pickuptime) {
        this.pickuptime = pickuptime;
    }

    public void setRenttype(String renttype) {
        this.renttype = renttype;
    }

    public void setPickuplocation(String pickuplocation) {
        this.pickuplocation = pickuplocation;
    }

    public void setBil(BilModel bil) {
        this.bil = bil;
    }
}
