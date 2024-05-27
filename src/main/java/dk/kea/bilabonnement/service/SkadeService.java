package dk.kea.bilabonnement.service;

import dk.kea.bilabonnement.model.Skaderapport;
import dk.kea.bilabonnement.repository.SkadeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkadeService {
    //lavet af Thomas
    @Autowired
    private SkadeRepo skadeRepo;

    public void opretSkade(String skade, int lejeaftale, double skadePris, int kundeId) {
        Skaderapport skaderapport = new Skaderapport();
        skaderapport.setSkade(skade);
        skaderapport.setLejeaftaleId(lejeaftale);
        skaderapport.setSkadePris(skadePris);
        skaderapport.setKundeId(kundeId);
        skadeRepo.save(skaderapport, lejeaftale);
    }
    public double calculateTotalPris(List<Skaderapport> skadeList) {
        return skadeList.stream()
                .mapToDouble(Skaderapport::getSkadePris)
                .sum();
    }
}