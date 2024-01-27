package com.supportportalMehdi.demo.AAAA.PARSING.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data       // Génère les getters, setters, toString, equals et hashCode
@NoArgsConstructor  // Génère un constructeur sans argument
@AllArgsConstructor // Génère un constructeur avec tous les arguments
public class ReportDataDto {
    private int year; // Ajout de l'attribut year
    private int month; // Mois de l'extrait
    private int totalTransactions;
    private double totalCreditedAmount;
    private double totalDebitedAmount;
    private String societeName; // Nom de la société, si applicable
    private String bankName;    // Nom de la banque, si applicable
    // Constructeur personnalisé sans le mois
    private int week; // Ajoutez ce champ pour la semaine

}
