package dk.kea.bilabonnement.model;

import java.time.LocalDate;
import java.util.Date;

public class Lejeaftale {
    private String licensePlate;
    private int Lejeaftale_id;
    private String chassisNumber;
    private Date dato;
    private String Udlejnings_Type;
    private Date Afhentningstidspunkt;
    private String Afhentningssted;
    private int Medarbejder_id;
    private int Kunde_id;

    public Lejeaftale() {
    }

    public Lejeaftale(int lejeaftale_id, String chassisNumber, Date dato, String udlejnings_Type, Date afhentningstidspunkt, String afhentningssted, int medarbejder_id, int kunde_id) {
        Lejeaftale_id = lejeaftale_id;
        this.chassisNumber = chassisNumber;
        this.dato = dato;
        Udlejnings_Type = udlejnings_Type;
        Afhentningstidspunkt = afhentningstidspunkt;
        Afhentningssted = afhentningssted;
        Medarbejder_id = medarbejder_id;
        Kunde_id = kunde_id;
    }
    public Lejeaftale(int lejeaftale_id, String chassisNumber, Date dato, String udlejnings_Type, Date afhentningstidspunkt, String afhentningssted, int medarbejder_id, int kunde_id, String licensePlate) {
        Lejeaftale_id = lejeaftale_id;
        this.chassisNumber = chassisNumber;
        this.dato = dato;
        Udlejnings_Type = udlejnings_Type;
        Afhentningstidspunkt = afhentningstidspunkt;
        Afhentningssted = afhentningssted;
        Medarbejder_id = medarbejder_id;
        Kunde_id = kunde_id;
        this.licensePlate = licensePlate;
    }

    public int getLejeaftale_id() {
        return Lejeaftale_id;
    }

    public void setLejeaftale_id(int lejeaftale_id) {
        Lejeaftale_id = lejeaftale_id;
    }

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }

    public Date getDato() {
        return dato;
    }

    public void setDato(Date dato) {
        this.dato = dato;
    }

    public String getUdlejnings_Type() {
        return Udlejnings_Type;
    }

    public void setUdlejnings_Type(String udlejnings_Type) {
        Udlejnings_Type = udlejnings_Type;
    }

    public Date getAfhentningstidspunkt() {
        return Afhentningstidspunkt;
    }

    public void setAfhentningstidspunkt(Date afhentningstidspunkt) {
        Afhentningstidspunkt = afhentningstidspunkt;
    }

    public String getAfhentningssted() {
        return Afhentningssted;
    }

    public void setAfhentningssted(String afhentningssted) {
        Afhentningssted = afhentningssted;
    }

    public int getMedarbejder_id() {
        return Medarbejder_id;
    }

    public void setMedarbejder_id(int medarbejder_id) {
        Medarbejder_id = medarbejder_id;
    }

    public int getKunde_id() {
        return Kunde_id;
    }

    public void setKunde_id(int kunde_id) {
        Kunde_id = kunde_id;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getLicensePlate () {
        return this.licensePlate;
    }

}