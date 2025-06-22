package com.mybank.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Named
@ApplicationScoped
public class LoanApprovalPredictionService {

    public PredictionResult predictLoanApproval(
            int age,
            int scoreCredit,
            String statutEmploi,
            double montantDemande,
            int dureeMois,
            String statutMatrimonial,
            String statutPropriete,
            double revenuMensuel,
            double tauxEndettement,
            int precedentsDefautsPret,
            int historiquePaiement,
            int historiqueFaillite,
            double mensualiteEstimee,
            double paiementsMensuelsDette,
            double valeurNette,
            double tauxAnnuel
    ) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://localhost:5000/predict");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // JSON body string
            String jsonInputString = String.format(
                    "{" +
                    "\"Age\": %d," +
                    "\"ScoreCredit\": %d," +
                    "\"StatutEmploi\": \"%s\"," +
                    "\"MontantDemande\": %.2f," +
                    "\"DureeMois\": %d," +
                    "\"StatutMatrimonial\": \"%s\"," +
                    "\"StatutPropriete\": \"%s\"," +
                    "\"RevenuMensuel\": %.2f," +
                    "\"TauxEndettement\": %.4f," +
                    "\"PrecedentsDefautsPret\": %d," +
                    "\"HistoriquePaiement\": %d," +
                    "\"HistoriqueFaillite\": %d," +
                    "\"MensualiteEstimee\": %.2f," +
                    "\"PaiementsMensuelsDette\": %.2f," +
                    "\"ValeurNette\": %.2f," +
                    "\"TauxAnnuel\": %.4f" +
                    "}",
                    age, scoreCredit, statutEmploi, montantDemande, dureeMois,
                    statutMatrimonial, statutPropriete, revenuMensuel, tauxEndettement,
                    precedentsDefautsPret, historiquePaiement, historiqueFaillite,
                    mensualiteEstimee, paiementsMensuelsDette, valeurNette, tauxAnnuel
            );

            // Send POST request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Parse JSON response
            try (InputStream is = conn.getInputStream();
                 JsonReader reader = Json.createReader(is)) {
                JsonObject response = reader.readObject();

                boolean approved = response.getBoolean("loanApproved");
                double probability = response.getJsonNumber("approvalProbability").doubleValue();
                double riskScore = response.getJsonNumber("predictedRiskScore").doubleValue();

                return new PredictionResult(approved, probability, riskScore);
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (conn != null) {
                try (InputStream errorStream = conn.getErrorStream()) {
                    if (errorStream != null) {
                        String errorResponse = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                        System.err.println("Error response from Flask: " + errorResponse);
                    }
                } catch (Exception inner) {
                    System.err.println("Failed to read error stream: " + inner.getMessage());
                }
            }

            return new PredictionResult(false, 0.0, -1.0);
        }
    }

    // Helper class to encapsulate results
    public static class PredictionResult {
        private final boolean loanApproved;
        private final double approvalProbability;
        private final double riskScore;

        public PredictionResult(boolean loanApproved, double approvalProbability, double riskScore) {
            this.loanApproved = loanApproved;
            this.approvalProbability = approvalProbability;
            this.riskScore = riskScore;
        }

        public boolean isLoanApproved() {
            return loanApproved;
        }

        public double getApprovalProbability() {
            return approvalProbability;
        }

        public double getRiskScore() {
            return riskScore;
        }
    }
}
