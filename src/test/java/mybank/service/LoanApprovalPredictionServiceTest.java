package mybank.service;

import com.mybank.service.LoanApprovalPredictionService;
import com.mybank.service.LoanApprovalPredictionService.PredictionResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoanApprovalPredictionServiceTest {

    @Test
    public void testPredictLoanApproval_LiveCall() {
        LoanApprovalPredictionService service = new LoanApprovalPredictionService();

        PredictionResult result = service.predictLoanApproval(
                30, 700, "Employé", 100000.0, 24, "Marié", "Locataire",
                3000.0, 0.2, 0, 1, 0, 450.0, 250.0, 10000.0, 0.05
        );

        assertNotNull(result);
        System.out.println("Approved: " + result.isLoanApproved());
        System.out.println("Probability: " + result.getApprovalProbability());
        System.out.println("Risk Score: " + result.getRiskScore());
    }
}
