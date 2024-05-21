package dk.kea.bilabonnement.service;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class BilService {

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



}
