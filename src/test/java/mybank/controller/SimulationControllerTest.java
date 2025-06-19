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

    @Mock private FolderController mockFolderController;
    @Mock private DemandeCreditService mockDemandeCreditService;
    @Mock private ClientProfileService mockClientProfileService;
    @Mock private LoanApprovalPredictionService mockPredictionService;
    @Mock private ClientProfileController mockClientProfileController;

    private Folder sampleFolder;
    private ClientProfile sampleProfile;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Create instance and inject mocks via setters
        simulationController = new SimulationController();
        simulationController.setFolderBean(mockFolderController);
        simulationController.setDemandeCreditService(mockDemandeCreditService);
        simulationController.setClientProfileService(mockClientProfileService);
        simulationController.setLoanApprovalPredictionService(mockPredictionService);
        simulationController.setClientProfileController(mockClientProfileController);

        // Sample folder setup
        sampleFolder = new Folder();
        sampleFolder.setMontantDemande(10000.0);
        sampleFolder.setDureeMois(12);
        sampleFolder.setTauxAnnuel(5.0);
        sampleFolder.setRevenuMensuel(2000.0);

        // Sample profile setup
        sampleProfile = new ClientProfile();
        sampleProfile.setPaiementsMensuelsDette(300.0);

        // Mocking behavior
        when(mockFolderController.getSelectedFolder()).thenReturn(sampleFolder);
        when(mockClientProfileService.findByFolder(sampleFolder)).thenReturn(sampleProfile);
    }

    @Test
    public void testSimulate_CalculatesMensualiteAndEndettement() {
        simulationController.simulate();
        DemandeCredit result = simulationController.getSimulatedResult();

        assertNotNull(result);
        assertEquals(15.0, result.getTauxEndettementAvant(), 0.01); // 300 / 2000 * 100
        assertEquals(856.07, result.getMensualiteEstimee(), 1.0);   // Approx monthly payment
        assertEquals(57.8, result.getTauxEndettement(), 1.0);       // (300 + mensualité) / 2000 * 100
    }
}
