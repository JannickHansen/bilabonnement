package dk.kea.bilabonnement.controller;

import dk.kea.bilabonnement.model.BilModel;
import dk.kea.bilabonnement.model.Bruger;
import dk.kea.bilabonnement.model.Lejeaftale;
import dk.kea.bilabonnement.model.Skaderapport;
import dk.kea.bilabonnement.repository.BilRepo;
import dk.kea.bilabonnement.repository.LejeaftaleRepo;
import dk.kea.bilabonnement.repository.SkadeRepo;
import dk.kea.bilabonnement.service.BilService;
import dk.kea.bilabonnement.service.ValidationService;
import dk.kea.bilabonnement.repository.BrugerRepo;
import dk.kea.bilabonnement.service.BrugerService;
import dk.kea.bilabonnement.service.SkadeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.sql.Date;
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
    @Autowired
    ValidationService validationService;

    @Autowired
    SkadeRepo skadeRepo;


    @GetMapping("/")
    public String forside() {

        if (Objects.equals(request.getParameter("logud"), "1")){
            request.getSession().invalidate();
        }
        return "BilabonnementForside";
    }

    @PostMapping("/")

    public String login(HttpServletRequest request, @RequestParam String login, @RequestParam String password) {
        Bruger bruger = brugerRepo.getBruger(login, password);

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

        ValidationService validation = new ValidationService();

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
        // Valider adgang start
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        // Valider adgang slut
        return "OpretBilFejl";
    }

    @GetMapping("/Administrator")

    public String admin() {
        return "Admin";
    }

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
        return "manageFleet";
    }

    @GetMapping("/FjernBil")
    public String fjernBil(Model model) {
        // Valider adgang start
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        // Valider adgang slut

        List<BilModel> fleetList = bilRepo.loadAllCars();
        model.addAttribute("fleetList", fleetList);
        return "FjernBil";
    }

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


    @GetMapping("/help")
    public String helper(){return "help";}

    @GetMapping("/Cookies")
    public String cookie(){return "Cookies";}
  
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
    public String opretLejeaftale() {
        return "NyLejeaftale";

    @GetMapping("/LejeAftale")
    public String lejeaftale(Model model) {
        // Valider adgang start
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        // Valider adgang slut
        List<Lejeaftale> lejeaftaleList = lejeaftaleRepo.findAllAfventende();
        model.addAttribute("datoliste",validationService.datoFormatteringTilVisning(lejeaftaleList));
        model.addAttribute("lejeaftaleList", lejeaftaleList);
        return "/LejeAftale";
    }

    @PostMapping("/OpretLejeaftaleFejl")
    public String opretLejeaftale(@RequestParam("chassisNumber") String chassisNumber,
                                  @RequestParam("dato")
                                    @DateTimeFormat(pattern = "dd-MM-yyyy") String datotemp,
                                  @RequestParam("Udlejnings_Type") String Udlejnings_Type,
                                  @RequestParam("Afhentningstidspunkt") String Afhentningstidspunkt,
                                  @RequestParam("Afhentningssted") String Afhentningssted,
                                  @RequestParam("Medarbejder_id") int Medarbejder_id,
                                  @RequestParam("Kunde_Navn") String Kunde_Navn,
                                  @RequestParam("Telefon_nummer") int Telefon_nummer,
                                  @RequestParam("Email") String Email,
                                  @RequestParam("Adresse") String Adresse,
                                  Model model){
        if (!brugerService.isData(request)){
            return "redirect:/";
        }

        ValidationService validation = new ValidationService();
        String errorText = null;

        LocalTime afhentningstidspunkttemp2 = LocalTime.parse(Afhentningstidspunkt, DateTimeFormatter.ofPattern("HH:mm"));
        Time Afhentningstidspunkttemp = Time.valueOf(afhentningstidspunkttemp2);

        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("Udlejnings_Type", Udlejnings_Type);
        model.addAttribute("Afhentningssted", Afhentningssted);
        model.addAttribute("Medarbejder_id", Medarbejder_id);
        model.addAttribute("Kunde_Navn", Kunde_Navn);
        model.addAttribute("Telefon_nummer", Telefon_nummer);
        model.addAttribute("Email", Email);
        model.addAttribute("Adresse", Adresse);
        model.addAttribute("Afhentningstidspunkt", Afhentningstidspunkt);
        model.addAttribute("datotemp", datotemp);

        if (!validation.validateDato(datotemp)) {
            errorText = "Ugyldig Dato. Dato må tidligst være i dag.";
            model.addAttribute("errorText",errorText);
            return "OpretLejeaftaleFejl";
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate datoLocalDate = LocalDate.parse(datotemp, dateFormatter);
        Date dato = Date.valueOf(datoLocalDate);
        model.addAttribute("dato", dato);

        int Kunde_id = lejeaftaleRepo.customerCheck(Kunde_Navn, Telefon_nummer, Email, Adresse);
        model.addAttribute("Kunde_id", Kunde_id);

        if (lejeaftaleRepo.findChassisNumberInDatabase(chassisNumber).isEmpty()) {
            errorText = "Stelnummer findes ikke i databasen.";
        } else if (!validation.validateDato(dato)) {
            errorText = "Ugyldig Dato. Dato må tidligst være i dag.";
            model.addAttribute("datotemp", datotemp);
        } else if (!validation.validateTime(Afhentningstidspunkttemp)) {
            errorText = "Vælg venligst et tidspunkt i fremtiden.";
        }

        model.addAttribute("errorText",errorText);
        if (errorText != null) {
            return "OpretLejeaftaleFejl";
        }

        //to make it work - find out how to do cookies // check what employee is logged in /w kevin
        Medarbejder_id = 2;

        String licensePlate = lejeaftaleRepo.findLicensePlate(chassisNumber);
        model.addAttribute(licensePlate);
        String status = "Afventende";
        Lejeaftale nyLejeaftale = new Lejeaftale(chassisNumber, dato, Udlejnings_Type, Afhentningstidspunkttemp, Afhentningssted, Medarbejder_id, Kunde_id, licensePlate, status);
        lejeaftaleRepo.create(nyLejeaftale);
        return "/LejeAftale";
    }

    @GetMapping("/OpretLejeaftale")
    public String opretLejeaftaleFejl(Model model) {
        // Valider adgang start
        if (!brugerService.isData(request)){
            return "redirect:/";
        }
        // Valider adgang slut

        return "OpretLejeaftale";

    }


    @GetMapping("/vaelglejeaftale")
    public String showVaelglejeaftale(Model model) {
        if (!brugerService.isSkade(request)){
            return "redirect:/";
        }
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

    @GetMapping("/tilbagelevering/{Lejeaftale_id}")
    public String showTilbagelevering(@PathVariable("Lejeaftale_id") int lejeaftale_id, Model model){
        if (!brugerService.isSkade(request)){
            return "redirect:/";
        }

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
