package mybank.controller;

import com.mybank.controller.ClientProfileController;
import com.mybank.controller.FolderController;
import com.mybank.controller.SimulationController;
import com.mybank.model.ClientProfile;
import com.mybank.model.DemandeCredit;
import com.mybank.model.Folder;
import com.mybank.service.ClientProfileService;
import com.mybank.service.DemandeCreditService;
import com.mybank.service.LoanApprovalPredictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SimulationControllerTest {

    private SimulationController simulationController;

    @Mock private FolderController folderController;
    @Mock private DemandeCreditService demandeCreditService;
    @Mock private ClientProfileService clientProfileService;
    @Mock private LoanApprovalPredictionService predictionService;
    @Mock private ClientProfileController clientProfileController;

    private Folder folder;
    private ClientProfile profile;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        simulationController = new SimulationController();
        simulationController.setFolderBean(folderController);
        simulationController.setDemandeCreditService(demandeCreditService);
        simulationController.setClientProfileService(clientProfileService);
        simulationController.setLoanApprovalPredictionService(predictionService);
        simulationController.setClientProfileController(clientProfileController);

        folder = new Folder();
        folder.setMontantDemande(10000.0);
        folder.setDureeMois(12);
        folder.setTauxAnnuel(5.0);
        folder.setRevenuMensuel(2000.0);

        profile = new ClientProfile();
        profile.setPaiementsMensuelsDette(300.0);

        when(folderController.getSelectedFolder()).thenReturn(folder);
        when(clientProfileService.findByFolder(folder)).thenReturn(profile);
    }

    @Test
    public void testSimulate_CorrectCalculations() {
        simulationController.simulate();

        DemandeCredit result = simulationController.getSimulatedResult();
        assertNotNull(result);
        assertEquals(15.0, result.getTauxEndettementAvant(), 0.01);
        assertEquals(856.07, result.getMensualiteEstimee(), 1.0);   // Approx value
        assertEquals(57.8, result.getTauxEndettement(), 1.0);       // Approx value
    }
}
