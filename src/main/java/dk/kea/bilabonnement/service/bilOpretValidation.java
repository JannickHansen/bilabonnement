package dk.kea.bilabonnement.service;

import java.util.regex.Pattern;

public class bilOpretValidation {

    Pattern patternLetter = Pattern.compile("[^a-zA]");
    Pattern patternLetterNumber = Pattern.compile("[^a-zA-Z0-9]");
    Pattern patternNummerplade = Pattern.compile("^[a-zA-Z]{2}");
    public boolean validateStelnummer(String stelnummer) {

        if (stelnummer.length()!=17) {
            return false;
        } else {
            return !patternLetterNumber.matcher(stelnummer).find();
        }
    }
    public boolean validateBrand(String brand) {
        return !patternLetter.matcher(brand).find();
    }
    public boolean validateModel(String bilmodel) {
        return !patternLetterNumber.matcher(bilmodel).find();
    }
    public boolean validateNummerplade(String nummerplade) {
        if (nummerplade.length()!=7) {
            return false;
        } else if (!patternLetterNumber.matcher(nummerplade).find()) {
            return false;
        } else {
            return !patternNummerplade.matcher(nummerplade).find();
        }
    }
}
