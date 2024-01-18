package com.supportportalMehdi.demo.AAAA.PARSING.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data       // Génère les getters, setters, toString, equals et hashCode
@NoArgsConstructor  // Génère un constructeur sans argument
@AllArgsConstructor // Génère un constructeur avec tous les arguments
public class ReportDataDto {
    private int totalTransactions;
    private double totalCreditedAmount;
    private double totalDebitedAmount;
    private String societeName; // Nom de la société, si applicable
    private String bankName;    // Nom de la banque, si applicable
}
