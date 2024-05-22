package dk.kea.bilabonnement.controller;

import dk.kea.bilabonnement.model.BilModel;
import dk.kea.bilabonnement.model.Bruger;
import dk.kea.bilabonnement.model.Lejeaftale;
import dk.kea.bilabonnement.model.Skaderapport;
import dk.kea.bilabonnement.repository.BilRepo;
import dk.kea.bilabonnement.repository.LejeaftaleRepo;
import dk.kea.bilabonnement.repository.SkadeRepo;
import dk.kea.bilabonnement.service.BilService;
import dk.kea.bilabonnement.repository.BrugerRepo;
import dk.kea.bilabonnement.service.BrugerService;
import dk.kea.bilabonnement.service.SkadeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class bilabonnementController {
    @Autowired
    BrugerRepo brugerRepo;

    @Autowired
    BilRepo bilRepo;

    @Autowired
    LejeaftaleRepo lejeaftaleRepo;

    @Autowired
    SkadeRepo skadeRepo;


    @GetMapping("/")
    public String forside() {
        return "BilabonnementForside";
    }

    @PostMapping("/")
    public String login(HttpServletRequest request, @RequestParam String login, @RequestParam String password) {
        Bruger bruger = brugerRepo.getBruger(login, password);
        if (bruger == null) {
            return "redirect:/";

        } else {
            BrugerService brugerService = new BrugerService();
            brugerService.gemBruger(request, bruger);
            return switch (bruger.getRole()) {
                case "Administrator" -> "redirect:/Administrator";
                case "Dataregistrer" -> "redirect:/registrer";
                case "SkadeOgUdbedring" -> "redirect:/skade";
                case "Forretningsudvikler" -> "redirect:/Forretningsudvikler";
                default -> "redirect:/";
            };

        }
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
        model.addAttribute("errorText", errorText);
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
    public String admin() {
        return "Admin";
    }

    @GetMapping("/manageFleet")
    public String manageFleet(Model model) {
        List<BilModel> fleetList = bilRepo.loadAllCars();
        model.addAttribute("fleetList", fleetList);
        return "manageFleet";
    }

    @GetMapping("/FjernBil")
    public String fjernBil(Model model) {
        List<BilModel> fleetList = bilRepo.loadAllCars();
        model.addAttribute("fleetList", fleetList);
        return "FjernBil";
    }

    @PostMapping("/FjernBil")
    public String bilFjernes(@RequestParam("chassisNumber") String chassisNumber, Model model) {
        bilRepo.deleteChassisNumber(chassisNumber);
        List<BilModel> fleetList = bilRepo.loadAllCars();
        model.addAttribute("fleetList", fleetList);
        return "FjernBil";
    }

    @GetMapping("/Forretningsudvikler")
    public String forretningudv() {
        return "Forretningsudvikler";
    }

    @GetMapping("/registrer")
    public String registrer() {
        return "register";
    }

    @GetMapping("/skade")
    public String skade() {
        return "skade";
    }

    @GetMapping("/OpretBruger")
    public String opretBruger() {
        return "OpretBruger";
    }

    @PostMapping("/OpretBruger")
    public String nyBruger(
            @RequestParam("login") String login,
            @RequestParam("password") String password,
            @RequestParam("role") String role

    ) {
        Bruger bruger = new Bruger(login, password, role);
        brugerRepo.create(bruger);
        return "redirect:/Administrator";
    }

    @GetMapping("/NyLejeaftale")
    public String opretLejeaftale() {
        return "NyLejeaftale";
    }


    @GetMapping("/vaelglejeaftale")
    public String showVaelglejeaftale(Model model) {
        List<Lejeaftale> lejeaftaler = lejeaftaleRepo.findAll();
        model.addAttribute("Lejeaftale", lejeaftaler);
        return "vaelglejeaftale";
    }


    @Autowired
    private SkadeService skadeService;

    private List<Skaderapport> temporarySkadeList = new ArrayList<>();

    @PostMapping("/tilbagelevering")
    public String showTilbageleveringForm(@RequestParam("chassisNumber") String chassisNumber, Model model) {
        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("skadeList", temporarySkadeList);
        return "tilbagelevering";
    }

    @PostMapping("/addSkade")
    public String addSkade(@ModelAttribute Skaderapport skadeRapport, Model model) {
        temporarySkadeList.add(skadeRapport);
        model.addAttribute("skadeList", temporarySkadeList);
        return "/tilbagelevering";
    }

    @PostMapping("/submitTotalPrice")
    public String submitTotalPrice(@RequestParam("chassisNumber") String chassisNumber, @ModelAttribute List<Skaderapport> skadeList) {
        skadeService.createSkadeRapport(chassisNumber, skadeList);
        // Clear the temporarySkadeList after persisting to the database
        temporarySkadeList.clear();
        return "redirect:/tilbagelevering?chassisNumber=" + chassisNumber;
    }
}
    /*
     @PostMapping("/submitTotalPrice")
    public String submitTotalPrice(@RequestParam("chassisNumber") String chassisNumber, Skaderapport[] skadeList) {
        // Create SkadeRapport and change Bil status to "ledig"
        skadeService.createSkadeRapport(chassisNumber, Arrays.asList(skadeList));
        // Redirect to the same page after processing
        return "redirect:/tilbagelevering?chassisNumber=" + chassisNumber;
    }
}


*/