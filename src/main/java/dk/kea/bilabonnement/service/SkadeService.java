package dk.kea.bilabonnement.service;

import dk.kea.bilabonnement.model.Skaderapport;
import dk.kea.bilabonnement.repository.SkadeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkadeService {

    @Autowired
    private SkadeRepo skadeRepo;

    public void addSkade(Skaderapport skadeRapport) {
        skadeRepo.addSkade(skadeRapport);
    }

    public void createSkadeRapport(String chassisNumber, List<Skaderapport> skadeList) {
        skadeRepo.createSkadeRapport(chassisNumber, skadeList);
    }
}