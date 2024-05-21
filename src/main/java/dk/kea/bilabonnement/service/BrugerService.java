package dk.kea.bilabonnement.service;

import dk.kea.bilabonnement.model.Bruger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class BrugerService {
    // metode til at validere bruger, fra cookies.
    // vi vil gerne gemme valideret bruger i cookie.
    // lavet med hjælp af jarls link: https://jart.gitbook.io/2-semester-programmering/sessions
    public void gemBruger(HttpServletRequest request, Bruger bruger){
        HttpSession session = request.getSession();
        session.setAttribute("bruger", bruger);

    }
}
