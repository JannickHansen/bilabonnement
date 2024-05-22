package dk.kea.bilabonnement.controller;

import dk.kea.bilabonnement.model.BilModel;
import dk.kea.bilabonnement.model.Bruger;
import dk.kea.bilabonnement.model.Lejeaftale;
import dk.kea.bilabonnement.repository.BilRepo;
import dk.kea.bilabonnement.repository.LejeaftaleRepo;
import dk.kea.bilabonnement.service.BilService;
import dk.kea.bilabonnement.repository.BrugerRepo;
import dk.kea.bilabonnement.service.BrugerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class bilabonnementController {
    @Autowired
    BrugerRepo brugerRepo;

    @Autowired
    BilRepo bilRepo;

    @Autowired
    LejeaftaleRepo lejeaftaleRepo;
    @Autowired
    HttpServletRequest request;
    @Autowired
    BrugerService brugerService;


    @GetMapping("/")
    public String forside() {

        if (Objects.equals(request.getParameter("logud"), "1")){
            request.getSession().invalidate();
        }
        return "BilabonnementForside";
    }

    @PostMapping("/")
        public String login(@RequestParam String login, @RequestParam String password) {
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
        // Valider adgang start
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        // Valider adgang slut
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
        // Valider adgang start
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        // Valider adgang slut

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
        // Valider adgang start
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        // Valider adgang slut
        return "OpretBilFejl";
    }

    @GetMapping("/Administrator")
    public String admin(){
        if (brugerService.isAdmin(request)){
            return "Admin";
        }
        return "redirect:/";
        }

    @GetMapping("/manageFleet")
    public String manageFleet(Model model) {
        // Valider adgang start
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        // Valider adgang slut
        List<BilModel> fleetList = bilRepo.loadAllCars();
        model.addAttribute("fleetList", fleetList);
        return "manageFleet"; }

    @GetMapping("/FjernBil")
    public String fjernBil(Model model) {
        // Valider adgang start
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        // Valider adgang slut

        List<BilModel> fleetList = bilRepo.loadAllCars();
        model.addAttribute("fleetList", fleetList);
        return "FjernBil"; }

    @PostMapping("/FjernBil")
    public String bilFjernes(@RequestParam("chassisNumber") String chassisNumber, Model model) {
        // Valider adgang start
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        // Valider adgang slut

        bilRepo.deleteChassisNumber(chassisNumber);
        List<BilModel> fleetList = bilRepo.loadAllCars();
        model.addAttribute("fleetList", fleetList);
        return "FjernBil";
    }
  
    @GetMapping("/Forretningsudvikler")
    public String forretningudv(){
        if (!brugerService.isUdvikler(request)){
            return "redirect:/";
        }
        return "Forretningsudvikler";}
    @GetMapping("/registrer")
    public String registrer(){
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        return "register";}
    @GetMapping("/skade")
    public String skade(){
        if (!brugerService.isSkade(request)){
            return "redirect:/";
        }
        return "skade";}

    @GetMapping("/OpretBruger")
    public String opretBruger(){
        if (!brugerService.isAdmin(request)){
            return "redirect:/";
        }
        return "OpretBruger";
    }
    @PostMapping("/OpretBruger")
    public String nyBruger(
            @RequestParam("login") String login,
            @RequestParam("password") String password,
            @RequestParam("role") String role

    ) {
        // Valider adgang start
        if (!brugerService.isAdmin(request)){
            return "redirect:/";
        }
        // Valider adgang slut

        Bruger bruger = new Bruger(login, password, role);
        brugerRepo.create(bruger);
        return "redirect:/Administrator";
    }

    @GetMapping("/NyLejeaftale")
    public String opretLejeaftale(){
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        return "NyLejeaftale";}


    @GetMapping("/vaelglejeaftale")
    public String showVaelglejeaftale(Model model) {
        if (!brugerService.isSkade(request)){
            return "redirect:/";
        }
        List<Lejeaftale> lejeaftaler = lejeaftaleRepo.findAll();
        model.addAttribute("Lejeaftale", lejeaftaler);
        return "vaelglejeaftale";
    }
    @GetMapping("/tilbagelevering/{Lejeaftale_id}")
    public String showTilbagelevering(@PathVariable("Lejeaftale_id") int lejeaftale_id, Model model){
        if (!brugerService.isSkade(request)){
            return "redirect:/";
        }
        return "tilbagelevering";
    }
}

