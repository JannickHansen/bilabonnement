package dk.kea.bilabonnement.controller;

import dk.kea.bilabonnement.model.BilModel;
import dk.kea.bilabonnement.model.Bruger;
import dk.kea.bilabonnement.model.Lejeaftale;
import dk.kea.bilabonnement.model.Skaderapport;
import dk.kea.bilabonnement.repository.BilRepo;
import dk.kea.bilabonnement.repository.LejeaftaleRepo;
import dk.kea.bilabonnement.repository.SkadeRepo;
import dk.kea.bilabonnement.service.KPIService;
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
import java.sql.Time;
import java.time.LocalDate;
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
    KPIService kpiService;
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
        if (!brugerService.isData(request)) {
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
        if (!brugerService.isData(request)) {
            return "redirect:/";
        }
        // Valider adgang slut

        ValidationService validation = new ValidationService(lejeaftaleRepo);

        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("brand", brand);
        model.addAttribute("carModel", carModel);
        model.addAttribute("type", type);
        model.addAttribute("licensePlate", licensePlate);
        model.addAttribute("fuel", fuel);

        String errorText = null;
        errorText = validation.checkErrorOpretBil(chassisNumber, brand, carModel, licensePlate, errorText);

        /* Hvis en fejl findes, indsættes teksten i modellen, og dette sendes til opretbilfejl siden. */
        model.addAttribute("errorText", errorText);
        if (errorText != null) {
            return "OpretBilFejl";
        }
        int km = 0;
        BilModel bil = new BilModel(chassisNumber, brand, carModel, type, licensePlate, fuel, km);
        bil.setAvailable();
        bilRepo.create(bil);

        return "redirect:/manageFleet";
    }

    @GetMapping("/OpretBilFejl")
    public String opretBilFejl() {
        // Valider adgang start
        if (!brugerService.isData(request)) {
            return "redirect:/";
        }
        // Valider adgang slut
        return "OpretBilFejl";
    }

    @GetMapping("/Administrator")
    public String admin(Model model) {
        boolean isAdmin = brugerService.isAdmin(request);
        model.addAttribute("isAdmin", isAdmin);

        if (brugerService.isAdmin(request)) {
            return "Admin";
        }
        return "redirect:/";
    }


    @GetMapping("/manageFleet")
    public String manageFleet(Model model) {
        // Valider adgang start
        if (!brugerService.isData(request)) {
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
        if (!brugerService.isData(request)) {
            return "redirect:/";
        }
        // Valider adgang slut

        List<BilModel> fleetList = bilRepo.showAvailableCars();
        model.addAttribute("fleetList", fleetList);
        return "FjernBil";
    }

    @PostMapping("/FjernBil")
    public String bilFjernes(@RequestParam("chassisNumber") String chassisNumber, Model model) {
        // Valider adgang start
        if (!brugerService.isData(request)) {
            return "redirect:/";
        }
        // Valider adgang slut

        bilRepo.deleteChassisNumber(chassisNumber);
        List<BilModel> fleetList = bilRepo.showAvailableCars();
        model.addAttribute("fleetList", fleetList);
        return "FjernBil";
    }

    @GetMapping("/help")
    public String helper() {
        return "help";
    }

    @GetMapping("/Cookies")
    public String cookie() {
        return "Cookies";
    }

    @GetMapping("/Forretningsudvikler")
    public String forretningudv(Model model) {
        boolean isAdmin = brugerService.isAdmin(request);
        model.addAttribute("isAdmin", isAdmin);

        if (!brugerService.isUdvikler(request)) {
            return "redirect:/";
        }
        return "Forretningsudvikler";}

    @GetMapping("/KPIpage")
    public String KPI() {
        if (!brugerService.isUdvikler(request)){
            return "redirect:/";
        }
        return "KPIpage";}

    @GetMapping("/KPIpageTables")
    public String KPItables() {
        if (!brugerService.isUdvikler(request)){
            return "redirect:/";
        }
        return "/KPIpage";
    }

    @PostMapping("/KPIpageTables")
    public String KPIpageTable(
            @RequestParam("bilstatus") String bilstatus,
            @RequestParam("brand") String brand,
            @RequestParam("carModel") String carModel,
            @RequestParam("type") String type,
            @RequestParam("fuel") String fuel,
            @RequestParam(value = "gnslejeperiode", required = false, defaultValue = "false") boolean gnslejeperiode,
            @RequestParam(value = "gnsskadepris", required = false, defaultValue = "false") boolean gnsskadepris,
            @RequestParam(value = "gnsudlejepris", required = false, defaultValue = "false") boolean gnsudlejepris,
            @RequestParam(value = "totalpris", required = false, defaultValue = "false") boolean totalpris,
            Model model
    ) {

        if (!brugerService.isUdvikler(request)){
            return "redirect:/";
        }

        List<BilModel> bilList = kpiService.kpiManageCriteria(bilstatus, brand, carModel, type, fuel);
        model.addAttribute("bilList",bilList);

        if (gnslejeperiode) {
            int lejeperiodefinal = kpiService.gnslejeperiodehent(bilList);

            model.addAttribute("gnslejeperiode", gnslejeperiode);
            model.addAttribute("lejeperiodefinal", lejeperiodefinal);
        }

        if (gnsskadepris) {
            int skadeprisfinal = kpiService.gnsskadeprishent(bilList);

            model.addAttribute("gnsskadepris", gnsskadepris);
            model.addAttribute("skadeprisfinal", skadeprisfinal);
        }

        if (gnsudlejepris) {
            double udlejeprisfinal = kpiService.gnsudlejepris(bilList);

            model.addAttribute("gnsudlejepris", gnsudlejepris);
            model.addAttribute("udlejeprisfinal", udlejeprisfinal);
        }

        if (totalpris) {
            List<Integer> totalprislist = kpiService.totalpris();
            int totalprisantal = totalprislist.get(0);
            int totalprisfinal = totalprislist.get(1);

            model.addAttribute("totalpris", totalpris);
            model.addAttribute("totalprisantal", totalprisantal);
            model.addAttribute("totalprisfinal", totalprisfinal);
        }


        return "/KPIpageTables";
    }
    @GetMapping("/registrer")
    public String registrer(Model model) {
        boolean isAdmin = brugerService.isAdmin(request);
        model.addAttribute("isAdmin", isAdmin);

        if (!brugerService.isData(request)) {
            return "redirect:/";
        }
        return "register";
    }

    @GetMapping("/skade")
    public String skade(Model model) {
        boolean isAdmin = brugerService.isAdmin(request);
        model.addAttribute("isAdmin", isAdmin);

        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        return "skade";
    }

    @GetMapping("/OpretBruger")
    public String opretBruger() {
        if (!brugerService.isAdmin(request)) {
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
        if (!brugerService.isAdmin(request)) {
            return "redirect:/";
        }
        // Valider adgang slut

        Bruger bruger = new Bruger(login, password, role);
        brugerRepo.create(bruger);
        return "redirect:/Administrator";
    }


    @GetMapping("/LejeAftale")
    public String lejeaftale(Model model) {
        // Valider adgang start
        if (!brugerService.isData(request)) {
            return "redirect:/";
        }
        // Valider adgang slut
        List<Lejeaftale> lejeaftaleList = lejeaftaleRepo.findAllAfventende();
        model.addAttribute("datoliste", validationService.datoFormatteringTilVisning(lejeaftaleList));
        model.addAttribute("lejeaftaleList", lejeaftaleList);
        return "/LejeAftale";
    }

    @PostMapping("/OpretLejeaftaleFejl")
    public String opretLejeaftale(@RequestParam("chassisNumber") String chassisNumber,
                                  @RequestParam("dato")
                                  @DateTimeFormat(pattern = "dd-MM-yyyy") String datotemp,
                                  @RequestParam("Udlejnings_Type") String Udlejnings_Type,
                                  @RequestParam("Udlejningsperiode") Integer udlejningsperiode,
                                  @RequestParam("Afhentningstidspunkt") String Afhentningstidspunkt,
                                  @RequestParam("Afhentningssted") String Afhentningssted,
                                  @RequestParam("Kunde_Navn") String Kunde_Navn,
                                  @RequestParam("Telefon_nummer") int Telefon_nummer,
                                  @RequestParam("Email") String Email,
                                  @RequestParam("Adresse") String Adresse,
                                  Model model) {
        if (!brugerService.isData(request)) {
            return "redirect:/";
        }

        ValidationService validation = new ValidationService(lejeaftaleRepo);
        String errorText = null;

        Time Afhentningstidspunkttemp = validation.convertTime(Afhentningstidspunkt);

        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("Udlejnings_Type", Udlejnings_Type);
        model.addAttribute("Udlejningsperiode", udlejningsperiode);
        model.addAttribute("Afhentningssted", Afhentningssted);
        model.addAttribute("Kunde_Navn", Kunde_Navn);
        model.addAttribute("Telefon_nummer", Telefon_nummer);
        model.addAttribute("Email", Email);
        model.addAttribute("Adresse", Adresse);
        model.addAttribute("Afhentningstidspunkt", Afhentningstidspunkt);
        model.addAttribute("datotemp", datotemp);

        if (!validation.validateDato(datotemp)) {
            errorText = "Ugyldig Dato. Dato må tidligst være i dag.";
            model.addAttribute("errorText", errorText);
            return "OpretLejeaftaleFejl";
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate datoLocalDate = LocalDate.parse(datotemp, dateFormatter);
        Date dato = Date.valueOf(datoLocalDate);
        model.addAttribute("dato", dato);

        int Kunde_id = lejeaftaleRepo.customerCheck(Kunde_Navn, Telefon_nummer, Email, Adresse);
        model.addAttribute("Kunde_id", Kunde_id);

        if (udlejningsperiode == null) {
            udlejningsperiode = 0;
        }

        errorText = validation.checkErrors(chassisNumber, dato, udlejningsperiode, Udlejnings_Type, errorText);
        if (!validation.checkStatusIsLedig(bilRepo.loadAllCars(), chassisNumber)) {
            errorText = "Denne bil er allerede Udlejet.";
        }

        model.addAttribute("errorText", errorText);
        if (errorText != null) {
            return "OpretLejeaftaleFejl";
        }

        String licensePlate = lejeaftaleRepo.findLicensePlate(chassisNumber);
        model.addAttribute(licensePlate);

        model.addAttribute("datotemp", datotemp);

        String status = "Afventende";
        bilRepo.changeStatusOnCar(chassisNumber, "Udlejet");

        Lejeaftale nyLejeaftale = new Lejeaftale(chassisNumber, dato, Udlejnings_Type, Afhentningstidspunkttemp, Afhentningssted, Kunde_id, licensePlate, status, udlejningsperiode);
        lejeaftaleRepo.create(nyLejeaftale);
        return "redirect:/LejeAftale";
    }

    @GetMapping("/OpretLejeaftale")
    public String opretLejeaftaleFejl() {
        // Valider adgang start
        if (!brugerService.isData(request)) {
            return "redirect:/";
        }
        // Valider adgang slut

        return "OpretLejeaftale";

    }

    @GetMapping("/vaelglejeaftale")
    public String showVaelglejeaftale(Model model) {
        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        List<Lejeaftale> lejeaftaler = lejeaftaleRepo.findRentedCars();
        model.addAttribute("Lejeaftale", lejeaftaler);
        return "vaelglejeaftale";
    }

    @Autowired
    private SkadeService skadeService;

    private List<Skaderapport> temporarySkadeList = new ArrayList<>();

    @PostMapping("/tilbagelevering")
    public String showTilbagelevering(@RequestParam("kunde") int kunde, @RequestParam("chassisNumber") String chassisNumber, @RequestParam("lejeaftale") String lejeaftale, @RequestParam("brand") String brand, @RequestParam("carmodel") String carmodel, @RequestParam("licenseplate") String licenseplate, Model model) {
        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        // Valider adgang slut

        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("lejeaftale", lejeaftale);
        model.addAttribute("brand", brand);
        model.addAttribute("carmodel", carmodel);
        model.addAttribute("licenseplate", licenseplate);
        model.addAttribute("kunde", kunde);
        model.addAttribute("skadeList", temporarySkadeList);

        // Totalpris udregnes gennem metoden i SkadeService klassen
        double totalPris = skadeService.calculateTotalPris(temporarySkadeList);
        model.addAttribute("totalPris", totalPris);
        return "tilbagelevering";
    }

    @PostMapping("/addSkade")
    public String addSkade(@ModelAttribute Skaderapport skadeRapport, @RequestParam("skade") String skade, @RequestParam("chassisNumber") String chassisNumber, @RequestParam("lejeaftale") String lejeaftale, @RequestParam("brand") String brand, @RequestParam("carmodel") String carmodel, @RequestParam("licenseplate") String licenseplate, Model model) {
        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        // Valider adgang slut
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

    // back button på tilbagelevering.html linker hertil GetMapping clearer temporarySkadeList og redirecter tilbage til listen af biler til tilbagelevering
    @GetMapping("/clearTemporarySkadeList")
    public String clearTemporarySkadeList() {
        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        // Valider adgang slut

        temporarySkadeList.clear();
        return "redirect:/vaelglejeaftale";
    }

    // afslut knappen gør at man clearer temporarySkadeList og ændrer bilstatus til "Ledig", og ændrer lejeaftale status til "Afventerbetaling"
    @PostMapping("/tilbageleveringAfslut")
    public String tilbageleveringAfslut(@RequestParam("kmpris") int kmpris, @RequestParam("nykmantal") int nykmantal, @RequestParam("chassisNumber") String chassisNumber, @RequestParam("lejeaftale") int lejeaftale) {
        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        // Valider adgang slut

        // Opret skaderapport i databasen.
        for (Skaderapport skaderapport : temporarySkadeList) {
            skaderapport.setLejeaftaleId(lejeaftale);
            skaderapport.setKundeId(skaderapport.getKundeId());
            skadeService.opretSkade(skaderapport.getLejeaftaleId(), skaderapport.getSkade(), skaderapport.getSkadePris(), skaderapport.getKundeId());
        }

        // Opret Pris for overkørte km pris i databasen.
        // JANNICKKK HELP MEH
        // skadeService.opretSkade(skaderapport.getLejeaftaleId(), "overkørte KM udgift, kmpris, skaderapport.getKundeId());

        // bil status ændres til Ledig og lejeaftale status til Afventerbetaling, km opdateres på bil og clearer temporarySkadeList
        bilRepo.changeStatusOnCar(chassisNumber, "Ledig");
        lejeaftaleRepo.statusUpdate("Afventerbetaling", lejeaftale);
        bilRepo.changeKmOnCar(chassisNumber,nykmantal);
        temporarySkadeList.clear();
        return "redirect:/skade";
    }

    @PostMapping("/createSkaderapport")
    public String createSkaderapport(@RequestParam("kmantal") int kmantal, @RequestParam("totalPris") double totalPris, @RequestParam("chassisNumber") String chassisNumber, @RequestParam("lejeaftale") int lejeaftale, @RequestParam("brand") String brand, @RequestParam("carmodel") String carmodel, @RequestParam("licenseplate") String licenseplate, @RequestParam("kunde") int kunde, Model model) {
        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        // Valider adgang slut
        model.addAttribute("lejeaftale", lejeaftale);
        model.addAttribute("kunde", kunde);
        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("brand", brand);
        model.addAttribute("carmodel", carmodel);
        model.addAttribute("licenseplate", licenseplate);
        model.addAttribute("skadeList", temporarySkadeList);
        model.addAttribute("totalPris", totalPris);
        model.addAttribute("kmantal", kmantal);

        double kmpris = 0;
        int udlejningsperiode = lejeaftaleRepo.findUdlejningsPeriodeByChassisNumber(chassisNumber).getFirst();
        int tilladtkmkoert = 1000 * udlejningsperiode;
        int startkm = bilRepo.getKmByChassisNumber(chassisNumber);
        int overkoertekm = 0;
        int drivenduringrental = kmantal - startkm;
        if (drivenduringrental > tilladtkmkoert) {
            overkoertekm = drivenduringrental - tilladtkmkoert;
            kmpris = overkoertekm * 0.75;
        } else {
            overkoertekm = 0;
            kmpris = 0; }

        double skaderapporttotal = totalPris+kmpris;
        int nykmantal = bilRepo.getKmByChassisNumber(chassisNumber)+kmantal;
        model.addAttribute("kmpris", kmpris);
        model.addAttribute("nykmantal", nykmantal);
        model.addAttribute("overkoertekm", overkoertekm);
        model.addAttribute("skaderaporttotal", skaderapporttotal);

        return "/skaderapport";
    }

    @GetMapping("/skaderapport")
    public String skaderapport(@RequestParam("totalPris") String totalPris, @RequestParam("chassisNumber") String chassisNumber, @RequestParam("lejeaftale") String lejeaftale, @RequestParam("brand") String brand, @RequestParam("carmodel") String carmodel, @RequestParam("licenseplate") String licenseplate, @RequestParam("kunde") int kunde, @RequestParam("skadeList") List<Skaderapport> skadeList, Model model) {
        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        // Valider adgang slut
        model.addAttribute("lejeaftale", lejeaftale);
        model.addAttribute("kunde", kunde);
        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("brand", brand);
        model.addAttribute("carmodel", carmodel);
        model.addAttribute("licenseplate", licenseplate);
        model.addAttribute("skadeList", temporarySkadeList);
        model.addAttribute("totalPris", totalPris);

        return "skaderapport";
    }

    @GetMapping("/udskrivskaderapport")
    public String udskrivskaderapport(){
        return "udskrivskaderapport";
    }
    @PostMapping("/udskrivskaderapport")
    public String udskrivskaderapport(@RequestParam("overkoertekm") int overkoertekm, @RequestParam("skaderaporttotal") double skaderaporttotal, @RequestParam("kmpris") double kmpris, @RequestParam("kmantal") int kmantal, @RequestParam("totalPris") String totalPris, @RequestParam("chassisNumber") String chassisNumber, @RequestParam("lejeaftale") String lejeaftale, @RequestParam("brand") String brand, @RequestParam("carmodel") String carmodel, @RequestParam("licenseplate") String licenseplate, @RequestParam("kunde") int kunde, Model model) {
        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        // Valider adgang slut
        model.addAttribute("lejeaftale", lejeaftale);
        model.addAttribute("kunde", kunde);
        model.addAttribute("chassisNumber", chassisNumber);
        model.addAttribute("brand", brand);
        model.addAttribute("carmodel", carmodel);
        model.addAttribute("licenseplate", licenseplate);
        model.addAttribute("kmantal", kmantal);
        model.addAttribute("skadeList", temporarySkadeList);
        model.addAttribute("totalPris", totalPris);
        model.addAttribute("overkoertekm", overkoertekm);
        model.addAttribute("skaderaporttotal", skaderaporttotal);
        model.addAttribute("kmpris", kmpris);

        return "udskrivskaderapport";
    }
    @GetMapping("/bekraeftlejeaftale")
    public String bekraeftlejeaftale(Model model) {
        // Valider adgang start
        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        // Valider adgang slut

        List<Lejeaftale> afventendeBiler = lejeaftaleRepo.findWaitingCars();
        model.addAttribute("afventendeBiler", afventendeBiler);

        return "bekraeftlejeaftale";
    }
    // afslut knappen gør at man gør bilstatus til udlejet.
    @PostMapping("/bekraeftlejeaftaleAfslut")
    public String bekraeftlejeaftaleAfslut(@RequestParam("lejeaftale") int Lejeaftale_id) {
        if (!brugerService.isSkade(request)) {
            return "redirect:/";
        }
        // Valider adgang slut

        // bil status ændres til udlejet
        lejeaftaleRepo.statusUpdate("Udlejet", Lejeaftale_id);
        return "redirect:/skade";
    }
}
