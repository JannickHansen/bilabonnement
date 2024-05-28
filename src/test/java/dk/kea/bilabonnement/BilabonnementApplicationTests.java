package dk.kea.bilabonnement;

import dk.kea.bilabonnement.model.BilModel;
import dk.kea.bilabonnement.model.Lejeaftale;
import dk.kea.bilabonnement.model.Skaderapport;
import dk.kea.bilabonnement.repository.BilRepo;
import dk.kea.bilabonnement.repository.LejeaftaleRepo;
import dk.kea.bilabonnement.service.SkadeService;
import dk.kea.bilabonnement.service.ValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;


import java.sql.Date;
import java.sql.Time;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


// Lavet af Thomas
// TestPropertySource med hjælp af chatGPT
@TestPropertySource(locations = "classpath:test.properties")

@SpringBootTest
class BilabonnementApplicationTests {


    @Autowired
    private SkadeService skadeService;
    @Autowired
    private LejeaftaleRepo lejeaftaleRepo;
    @Autowired
    private BilRepo bilRepo;
    @Autowired
    ValidationService validationService;


    // Metoden calculateTotalPris testes, sammen med Skaderapport.
    @Test
    void testCalculateTotalPrisForSkade() {
        // Arrange
        List<Skaderapport> skaderapportTestList = Arrays.asList(
                new Skaderapport("Fælg skade", 100.0, 1),
                new Skaderapport("Rids på front", 200.0, 1),
                new Skaderapport("Forrude itu", 1500, 1)
        );

        // Act
        double result = skadeService.calculateTotalPris(skaderapportTestList);

        // Assert
        assertEquals(1800.0, result, "Totalprisen for skaderapporten skal give 1800.0 kr");
    }


    // Create metoden fra bilRepo testes
    @Test
    void testCreateBil() {
        // Arrange
        BilModel bil = new BilModel("1FZFB21114B1451234", "Ford", "F-150", "Pickup", "LM89123", "Diesel", 45000);

        // Act
        bilRepo.create(bil);

        // Assert
        assertEquals("1FZFB21114B1451234", bil.getChassisNumber());
    }

    // Når man opretter en lejeaftale uden at en bil er oprettet, sker der en fejl.
    // Da Chassisnumber, som er Primary Key, SKAL være oprettet i bil tabellen, og dens chassisnumber er en foreign key for lejeaftale,
    // skal den eksistere for at metoden virker. Hvis det ikke er tilfældet, skal den throw en DataIntegrityViolationException.
    @Test
    void testCreateLejeaftaleUdenBilErOprettet() {
        // Arrange
        Lejeaftale lejeaftale = new Lejeaftale();
        lejeaftale.setChassisNumber("1FTSW21R08EB123453");
        lejeaftale.setDato(Date.valueOf("2024-05-15"));
        lejeaftale.setUdlejnings_Type("Unlimited");
        lejeaftale.setAfhentningstidspunkt(Time.valueOf("10:00:00"));
        lejeaftale.setAfhentningssted("Aalborg");
        lejeaftale.setKunde_id(1);
        lejeaftale.setStatus("Afventende");
        lejeaftale.setUdlejningsperiode(22);

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            lejeaftaleRepo.create(lejeaftale);
        });
    }

    // Forskellige tests af inputvalidering, for checkStatusIsLedig metoden i ValidationService linie 96.
    @Test
    void testCheckStatusIsLedigMedInputValidering() {
        // Test 1: Test med liste med et objekt med status Ledig, og matchende chassisnumber
        BilModel bilModel = new BilModel("1FT241524B1451234", "Ford", "F-150", "Pickup", "LM89123", "Diesel", 45000);
        bilModel.setStatus("Ledig");
        List<BilModel> bilListe = Arrays.asList(bilModel);
        assertTrue(validationService.checkStatusIsLedig(bilListe, "1FT241524B1451234"));

        // Test 2: Test med liste med status Ledig, men uden gyldigt chassisnumber.
        assertFalse(validationService.checkStatusIsLedig(bilListe, "UgyldigtStelNummer"));

        // Test 3: Test med liste med anden status end Ledig, men gyldigt chassisnumber.
        BilModel bilModelUdenLedig = new BilModel("1FT115524B1451234", "Ford", "F-150", "Pickup", "LM89123", "Diesel", 45000);
        bilModelUdenLedig.setStatus("Udlejet");
        List<BilModel> bilListeUdenLedigStatus = Arrays.asList(bilModel);
        assertFalse(validationService.checkStatusIsLedig(bilListeUdenLedigStatus, "1FT115524B1451234"));
    }
}

