package dk.kea.bilabonnement.service;
import dk.kea.bilabonnement.model.Lejeaftale;
import dk.kea.bilabonnement.repository.LejeaftaleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    @Autowired
    private LejeaftaleRepo lejeaftaleRepo;

    Pattern patternLetter = Pattern.compile("[^a-zA-Z]");
    Pattern patternLetterNumber = Pattern.compile("[^a-zA-Z0-9]");
    Pattern patternLicensePlate = Pattern.compile("^[a-zA-Z]{2}\\d{5}$");
    public boolean validateChassisNumber(String chassisNumber) {

        if (chassisNumber.length()!=17) {
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
    return dato.compareTo(today) >= 0;
    }

    public boolean validateTime(Time tidspunkt) {
        LocalTime now = LocalTime.now();
        LocalTime localTidspunkt = tidspunkt.toLocalTime();
        return !localTidspunkt.isBefore(now);
    }

    public String findLicensePlate(String LicensePlate){
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

}
