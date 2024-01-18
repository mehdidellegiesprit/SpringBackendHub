package com.supportportalMehdi.demo.AAAA.PARSING.controller;



import com.supportportalMehdi.demo.AAAA.PARSING.controller.api.ReleveBancaireApi;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReleveBancaireDto;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReportDataDto;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.SocieteDto;
import com.supportportalMehdi.demo.AAAA.PARSING.model.DonneeExtrait;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ExtraitBancaire;
import com.supportportalMehdi.demo.AAAA.PARSING.model.FactureData;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ReleveBancaire;
import com.supportportalMehdi.demo.AAAA.PARSING.services.ReleveBancaireService;
import com.supportportalMehdi.demo.AAAA.PARSING.services.SocieteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@RestController
public class ReleveBancaireController implements ReleveBancaireApi {

    private SocieteService societeService ;
    private ReleveBancaireService releveBancaireService ;

    @Autowired
    public ReleveBancaireController(ReleveBancaireService releveBancaireService,SocieteService societeService) {
          this.releveBancaireService = releveBancaireService;
          this.societeService = societeService;
    }

    @Override
    public ResponseEntity<ReleveBancaireDto> getUploadedFile(MultipartFile file) throws IOException, ParseException {
        System.out.println("**************************************** getUploadedFile ****************************************");
        return ResponseEntity.ok(releveBancaireService.parseAndExtract((MultipartFile) file));
    }

    @Override
    public ResponseEntity<List<ReleveBancaireDto>> getAllRelevesBancaires() {
        return ResponseEntity.ok(releveBancaireService.getAllReleveBancaires());
    }

    @Override
    public ResponseEntity<ReleveBancaireDto> AddFactureComment(DonneeExtrait data) {
        return ResponseEntity.ok(releveBancaireService.AddFactureToDonneeExtrait(data));
    }

    @Override
    public ResponseEntity<ReleveBancaireDto> updateCommentaireFacture(DonneeExtrait data) {
        return ResponseEntity.ok(releveBancaireService.updateCommentaireFactureToDonneeExtrait(data));
    }

    @Override
    public ResponseEntity<ReleveBancaireDto> deleteFacture(FactureData factureData) {
        return ResponseEntity.ok(releveBancaireService.deleteFacture(factureData.getFacture(), factureData.getData()));
    }


    @Override
    public ResponseEntity<ReleveBancaireDto> AddReleve(ReleveBancaire data) {
        Optional<SocieteDto> exist_soc = this.societeService.findSocieteByNomSociete(data.getNom_societe());

        if (!exist_soc.isPresent()) {
            SocieteDto soc = this.societeService.createSociete(new SocieteDto(data.getNom_societe()));
            data.setId_societe(soc.getId());
        } else {
            data.setId_societe(exist_soc.get().getId());
        }

        Optional<ReleveBancaireDto> optReleveDto = releveBancaireService.findByIban(data.getIban());

        if (optReleveDto.isPresent()) {
            ReleveBancaire existingReleve = ReleveBancaireDto.toEntity(optReleveDto.get());

            for (ExtraitBancaire newExtrait : data.getExtraits()) {
                boolean extraitExists = false;

                for (ExtraitBancaire existingExtrait : existingReleve.getExtraits()) {
                    if (sameMonthAndYear(newExtrait.getDateExtrait(), existingExtrait.getDateExtrait())) {
                        extraitExists = true;

                        for (DonneeExtrait newDonnee : newExtrait.getDonneeExtraits()) {
                            boolean donneeExists = existingExtrait.getDonneeExtraits().stream().anyMatch(existingDonnee -> existingDonnee.equals(newDonnee));
                            if (!donneeExists) {
                                existingExtrait.getDonneeExtraits().add(newDonnee);
                            }
                        }

                        existingExtrait.getDonneeExtraits().sort(Comparator.comparing(DonneeExtrait::getDateDonneeExtrait)
                                .thenComparing(DonneeExtrait::getDateValeurDonneeExtrait));

                        if (getDay(newExtrait.getDateExtrait()) >= getDay(existingExtrait.getDateExtrait())) {
                            existingExtrait.setDateExtrait(newExtrait.getDateExtrait());
                            existingExtrait.setDateDuSoldeCrediteurDebutMois(newExtrait.getDateDuSoldeCrediteurDebutMois());
                            existingExtrait.setCreditDuSoldeCrediteurDebutMois(newExtrait.getCreditDuSoldeCrediteurDebutMois());
                            existingExtrait.setDateDuSoldeCrediteurFinMois(newExtrait.getDateDuSoldeCrediteurFinMois());
                            existingExtrait.setCreditDuSoldeCrediteurFinMois(newExtrait.getCreditDuSoldeCrediteurFinMois());
                            existingExtrait.setTotalMouvementsDebit(newExtrait.getTotalMouvementsDebit());
                            existingExtrait.setTotalMouvementsCredit(newExtrait.getTotalMouvementsCredit());
                        }
                        break;
                    }
                }

                if (!extraitExists) {
                    existingReleve.getExtraits().add(newExtrait);
                }
            }

            existingReleve.getExtraits().sort(Comparator.comparing(ExtraitBancaire::getDateExtrait));

            releveBancaireService.updateReleveBancaire(ReleveBancaireDto.fromEntity(existingReleve));
        } else {
            releveBancaireService.createReleveBancaire(ReleveBancaireDto.fromEntity(data));
        }

        Optional<ReleveBancaireDto> releveBancaireDto = releveBancaireService.findByIban(data.getIban());
        if (releveBancaireDto.isPresent()) {
            return ResponseEntity.ok(releveBancaireDto.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Override
    public ResponseEntity<ReportDataDto> getYearlyReport(int year) {
        // Étape 1 : Récupérer les relevés de l'année

        List<ReleveBancaire> releves = releveBancaireService.findByYear(year); // Méthode fictive

        // Étape 2 : Calculer les statistiques
        int totalTransactions = 0;
        double totalCreditedAmount = 0;
        double totalDebitedAmount = 0;

        for (ReleveBancaire releve : releves) {
            for (ExtraitBancaire extrait : releve.getExtraits()) {
                for (DonneeExtrait donnee : extrait.getDonneeExtraits()) {
                    totalTransactions++;
                    totalCreditedAmount += donnee.getCredit() != null ? donnee.getCredit() : 0;
                    totalDebitedAmount += donnee.getDebit() != null ? donnee.getDebit() : 0;
                }
            }
        }

        // Étape 3 : Création de ReportDataDto
        ReportDataDto reportData = new ReportDataDto(totalTransactions, totalCreditedAmount, totalDebitedAmount, null, null);

        // Étape 4 : Retour de la réponse
        return ResponseEntity.ok(reportData);
    }


    private boolean sameMonthAndYear(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    private int getDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

}
