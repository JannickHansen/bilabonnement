package dk.kea.bilabonnement.controller;

import dk.kea.bilabonnement.model.BilModel;
import dk.kea.bilabonnement.repository.BilRepo;
import dk.kea.bilabonnement.service.BilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class bilabonnementController {

    @Autowired
    BilRepo bilRepo;

    @GetMapping("/")
    public String forside() {
        return "BilabonnementForside";
    }

    @GetMapping("/OpretBil")
    public String opretBil() {
        return "OpretBil";
    }

    /* Postmapping som validere bil informationen ift. format, unikhed osv., og så enten redirecter
       til fejlsiden med fejlbesked, eller tilbage til dataregistrer siden. */
    @PostMapping("/OpretBilFejl")
    public String bilDataTjek(@RequestParam("chassisNumber") String chassisNumber,
                              @RequestParam("brand") String brand,
                              @RequestParam("carModel") String carModel,
                              @RequestParam("type") String type,
                              @RequestParam("licensePlate") String licensePlate,
                              @RequestParam("fuel") String fuel,
                              Model model
    ) {

        BilService validation = new BilService();

        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("brand", brand);
        model.addAttribute("carModel", carModel);
        model.addAttribute("type", type);
        model.addAttribute("licensePlate", licensePlate);
        model.addAttribute("fuel", fuel);

        String errorText = null;

        if (!validation.validateChassisNumber(chassisNumber)) {
            errorText = "Ugyldigt Stelnummer";
        } else if (!validation.validateBrand(brand)) {
            errorText = "Ugyldigt Mærke";
        } else if (!validation.validateCarModel(carModel)) {
            errorText = "Ugyldig Model";
        } else if (!validation.validateLicensePlate(licensePlate)) {
            errorText = "Ugyldig Nummerplade";
        }

        int carUniquenessCheck = BilRepo.authenticateUniqueCar(chassisNumber, licensePlate);

        if (carUniquenessCheck == 1) {
            errorText = "Stelnummer er allerede oprettet";
        } else if (carUniquenessCheck == 2) {
            errorText = "Nummerplade er allerede oprettet";
        }

        /* Hvis en fejl findes, indsættes teksten i modellen, og dette sendes til opretbilfejl siden. */
        model.addAttribute("errorText",errorText);
        if (errorText != null) {
            return "OpretBilFejl";
        }

        BilModel bil = new BilModel(chassisNumber, brand, carModel, type, licensePlate, fuel);
        bil.setAvailable();
        bilRepo.create(bil);

        return "redirect:/manageFleet";
    }

    @GetMapping("/OpretBilFejl")
    public String opretBilFejl() {
        return "OpretBilFejl";
    }

    @GetMapping("/Administrator")
    public String admin(){return "Admin";}

    @GetMapping("/manageFleet")
    public String manageFleet(Model model) {
        List<BilModel> fleetList = bilRepo.loadAllCars();
        model.addAttribute("fleetList", fleetList);
        return "manageFleet"; }

    @GetMapping("/FjernBil")
    public String fjernBil(Model model) {
        List<BilModel> fleetList = bilRepo.loadAllCars();
        model.addAttribute("fleetList", fleetList);
        return "FjernBil"; }

    @PostMapping("/FjernBil")
    public String bilFjernes(@RequestParam("chassisNumber") String chassisNumber, Model model) {
        bilRepo.deleteChassisNumber(chassisNumber);
        List<BilModel> fleetList = bilRepo.loadAllCars();
        model.addAttribute("fleetList", fleetList);
        return "FjernBil";
    }
}
