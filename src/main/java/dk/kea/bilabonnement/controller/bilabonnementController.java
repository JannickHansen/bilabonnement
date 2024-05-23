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
    public String showTilbagelevering(@RequestParam("chassisNumber") String chassisNumber, @RequestParam("lejeaftale") String lejeaftale, @RequestParam("brand") String brand, @RequestParam("carmodel") String carmodel, @RequestParam("licenseplate") String licenseplate, Model model) {
        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("lejeaftale", lejeaftale);
        model.addAttribute("brand", brand);
        model.addAttribute("carmodel", carmodel);
        model.addAttribute("licenseplate", licenseplate);
        model.addAttribute("skadeList", temporarySkadeList);

        // Totalpris udregnes gennem metoden i SkadeService klassen
        double totalPris = skadeService.calculateTotalPris(temporarySkadeList);
        model.addAttribute("totalPris", totalPris);
        return "tilbagelevering";
    }

    @PostMapping("/addSkade")
    public String addSkade(@ModelAttribute Skaderapport skadeRapport, @RequestParam("skade") String skade, @RequestParam ("chassisNumber") String chassisNumber,@RequestParam("lejeaftale") String lejeaftale, @RequestParam("brand") String brand, @RequestParam("carmodel") String carmodel, @RequestParam("licenseplate") String licenseplate, Model model) {
        switch (skade) {
            case "Ridset alufælg":
                skadeRapport.setSkade(skade);
                skadeRapport.setSkadePris(400); // Fast pris for givne skade
                break;
            case "Ny forrude":
                skadeRapport.setSkade(skade);
                skadeRapport.setSkadePris(3000);
                break;
            case "Lakfelt":
                skadeRapport.setSkade(skade);
                skadeRapport.setSkadePris(1500);
                break;
            default:
                break;
        }
        temporarySkadeList.add(skadeRapport);
        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("lejeaftale", lejeaftale);
        model.addAttribute("brand", brand);
        model.addAttribute("carmodel", carmodel);
        model.addAttribute("licenseplate", licenseplate);
        model.addAttribute("skadeList", temporarySkadeList);

        double totalPris = skadeService.calculateTotalPris(temporarySkadeList);
        model.addAttribute("totalPris", totalPris);
        return "/tilbagelevering";
    }
    // back button på tilbagelevering.html linker hertil. GetMapping clearer temporarySkadeList og redirecter tilbage til listen af biler til tilbagelevering
    @GetMapping("/clearTemporarySkadeList")
    public String clearTemporarySkadeList() {
        temporarySkadeList.clear();
        return "redirect:/vaelglejeaftale";
    }

    @PostMapping("/createSkaderapport")
    public String createSkaderapport(@RequestParam("totalPris") String totalPris, @RequestParam ("chassisNumber") String chassisNumber,@RequestParam("lejeaftale") int lejeaftale, @RequestParam("brand") String brand, @RequestParam("carmodel") String carmodel, @RequestParam("licenseplate") String licenseplate,@RequestParam("medarbejder") int medarbejder, @RequestParam("kunde") int kunde, Model model) {
        for (Skaderapport skaderapport : temporarySkadeList){
            skaderapport.setLejeaftaleId(lejeaftale);
            skaderapport.setMedarbejderId(medarbejder);
            skaderapport.setKundeId(kunde);
            skadeService.opretSkade(skaderapport.getSkade(), skaderapport.getLejeaftaleId(), skaderapport.getSkadePris(), skaderapport.getMedarbejderId(), skaderapport.getKundeId());
        }
        model.addAttribute("lejeaftale", lejeaftale);
        model.addAttribute("medarbejder", medarbejder);
        model.addAttribute("kunde", kunde);
        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("brand", brand);
        model.addAttribute("carmodel", carmodel);
        model.addAttribute("licenseplate", licenseplate);
        model.addAttribute("skadeList", temporarySkadeList);
        model.addAttribute("totalPris", totalPris);


        //temporarySkadeList.clear();
        return "/skaderapport";
    }

    @GetMapping("/skaderapport")
        public String skaderapport(@RequestParam("totalPris") String totalPris, @RequestParam ("chassisNumber") String chassisNumber,@RequestParam("lejeaftale") String lejeaftale, @RequestParam("brand") String brand, @RequestParam("carmodel") String carmodel, @RequestParam("licenseplate") String licenseplate,@RequestParam("medarbejder") int medarbejder, @RequestParam("kunde") int kunde, @RequestParam("skadeliste") List<Skaderapport> skadeList, Model model){
        model.addAttribute("lejeaftale", lejeaftale);
        model.addAttribute("medarbejder", medarbejder);
        model.addAttribute("kunde", kunde);
        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("brand", brand);
        model.addAttribute("carmodel", carmodel);
        model.addAttribute("licenseplate", licenseplate);
        model.addAttribute("skadeList", skadeList);
        model.addAttribute("totalPris", totalPris);

        temporarySkadeList.clear();
            return "skaderapport";
        }
    }
//husk bilstatus til ledig
