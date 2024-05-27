package dk.kea.bilabonnement.service;

import dk.kea.bilabonnement.model.BilModel;
import dk.kea.bilabonnement.model.Lejeaftale;
import dk.kea.bilabonnement.repository.BilRepo;
import dk.kea.bilabonnement.repository.LejeaftaleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    @Autowired
    private LejeaftaleRepo lejeaftaleRepo;

    @Autowired
    public ValidationService(LejeaftaleRepo lejeaftaleRepo) {
        this.lejeaftaleRepo = lejeaftaleRepo;
    }

    @Autowired
    BilRepo bilRepo;

    Pattern patternLetter = Pattern.compile("[^a-zA-Z]");
    Pattern patternLetterNumber = Pattern.compile("[^a-zA-Z0-9]");
    Pattern patternLicensePlate = Pattern.compile("^[a-zA-Z]{2}\\d{5}$");

    public boolean validateChassisNumber(String chassisNumber) {

        if (chassisNumber.length() != 17) {
            return false;
        } else {
            return !patternLetterNumber.matcher(chassisNumber).find();
        }
    }

    public boolean validateBrand(String brand) {
        return !patternLetter.matcher(brand).find();
    }

    public boolean validateCarModel(String carModel) {
        return !patternLetterNumber.matcher(carModel).find();
    }

    public boolean validateLicensePlate(String licensePlate) {
        if (licensePlate.length() != 7) {
            return false;
        } else {
            return patternLicensePlate.matcher(licensePlate).find();
        }
    }

    public boolean validateDato(Date dato) {
        Date today = new Date();
        return dato.compareTo(today) >= 1;
    }

    public String findLicensePlate(String LicensePlate) {
        return lejeaftaleRepo.findLicensePlate(LicensePlate);
    }

    public boolean validateDato(String dato) {
        String[] parts = dato.split("-");

        if (Integer.parseInt(parts[0]) > 31) {
            return false;
        } else if (Integer.parseInt(parts[1]) > 12) {
            return false;
        } else if (Integer.parseInt(parts[2]) < LocalDate.now().getYear()) {
            return false;
        }
        return true;
    }

    public List<String> datoFormatteringTilVisning(List<Lejeaftale> input) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        List<String> formateredeDatoer = new ArrayList<>();

        for (Lejeaftale lejeaftale : input) {
            Date dato = lejeaftale.getDato();
            if (dato != null) {
                String formattedDate = dateFormat.format(dato);
                formateredeDatoer.add(formattedDate);
            }
        }
        return formateredeDatoer;
    }

    public boolean checkStatusIsLedig(List<BilModel> templist, String chassisNumber) {

        for (BilModel tjekBil : templist) {
            if (tjekBil.getStatus().equals("Ledig") && chassisNumber.equals(tjekBil.getChassisNumber())) {
                return true;
            }
        }
        return false;
    }

    public Time convertTime(String Afhentningstidspunkt) {
        LocalTime afhentningstidspunkttemp2 = LocalTime.parse(Afhentningstidspunkt, DateTimeFormatter.ofPattern("HH:mm"));
        Time Afhentningstidspunkttemp = Time.valueOf(afhentningstidspunkttemp2);
        return Afhentningstidspunkttemp;
    }

    public String checkErrors(String chassisNumber, Date dato, int udlejningsperiode, String Udlejnings_Type, String errorText) {
        if (errorText == null) {
            if (lejeaftaleRepo.findChassisNumberInDatabase(chassisNumber).isEmpty()) {
                errorText = "Stelnummer findes ikke i databasen.";
            } else if (!validateDato(dato)) {
                errorText = "Ugyldig Dato. Dato må tidligst være 1 dag efter bestilling.";
            } else if (udlejningsperiode != 5 && Udlejnings_Type.equals("Limited")) {
                errorText = "Limited udlejningstype skal være 5 måneder.";
            }
            return errorText;
        }
        return errorText;
    }

    public String checkErrorOpretBil(String chassisNumber, String brand, String carModel, String licensePlate, String errorText) {
        if (errorText == null) {
            if (!validateChassisNumber(chassisNumber)) {
                errorText = "Ugyldigt Stelnummer";
            } else if (!validateBrand(brand)) {
                errorText = "Ugyldigt Mærke";
            } else if (!validateCarModel(carModel)) {
                errorText = "Ugyldig Model";
            } else if (!validateLicensePlate(licensePlate)) {
                errorText = "Ugyldig Nummerplade";
            }

            int carUniquenessCheck = BilRepo.authenticateUniqueCar(chassisNumber, licensePlate);

            if (carUniquenessCheck == 1) {
                errorText = "Stelnummer er allerede oprettet";
            } else if (carUniquenessCheck == 2) {
                errorText = "Nummerplade er allerede oprettet";
            }
            return errorText;
        }
        return errorText;

    }
}
