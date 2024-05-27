package dk.kea.bilabonnement.service;

import dk.kea.bilabonnement.model.Bruger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class BrugerService {
    // lavet af Kevin
    // metode til at validere bruger, fra cookies.
    // vi vil gerne gemme valideret bruger i cookie.
    // lavet med hjælp af jarls link: https://jart.gitbook.io/2-semester-programmering/sessions
    public void gemBruger(HttpServletRequest request, Bruger bruger){
        HttpSession session = request.getSession();
        session.setAttribute("bruger", bruger);

    }

    // Dette er en metode til at sørge for adgang til de forskellige afdelinger. Den laves i "private" da den kun bruges i denne klasse.
    private boolean validerRole(HttpServletRequest request, String role){
        HttpSession session = request.getSession();
        Bruger bruger = (Bruger) session.getAttribute("bruger");
        if (bruger == null){
            return false;
        }
        if (bruger.getRole().equals("Administrator")){
            return true;
        }
        return bruger.getRole().equals(role);
    }


    // disse 4 metoder, bruger metoden ovenfor og bliver implementeret i vores controller for at checke, at bruger har den rigtige adgang.B
    public boolean isAdmin(HttpServletRequest request){
        return validerRole(request, "Administrator");
    }
    public boolean isData(HttpServletRequest request){
        return validerRole(request, "Dataregistrer");
    }
    public boolean isSkade(HttpServletRequest request){
        return validerRole(request, "SkadeOgUdbedring");
    }
    public boolean isUdvikler(HttpServletRequest request){
        return validerRole(request, "Forretningsudvikler");
    }

}
