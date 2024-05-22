package dk.kea.bilabonnement.service;

import dk.kea.bilabonnement.model.Bruger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class BrugerService {
    // metode til at validere bruger, fra cookies.
    // vi vil gerne gemme valideret bruger i cookie.
    // lavet med hjælp af jarls link: https://jart.gitbook.io/2-semester-programmering/sessions
    public void gemBruger(HttpServletRequest request, Bruger bruger){
        HttpSession session = request.getSession();
        session.setAttribute("bruger", bruger);

    }

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
