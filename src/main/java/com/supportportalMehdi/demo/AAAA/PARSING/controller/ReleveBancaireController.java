package com.supportportalMehdi.demo.AAAA.PARSING.controller;



import com.supportportalMehdi.demo.AAAA.PARSING.controller.api.ReleveBancaireApi;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReleveBancaireDto;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReportDataDto;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.SocieteDto;
import com.supportportalMehdi.demo.AAAA.PARSING.model.*;
import com.supportportalMehdi.demo.AAAA.PARSING.repository.ReleveBancaireRepository;
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
    private ReleveBancaireRepository releveBancaireRepository ;

    @Autowired
    public ReleveBancaireController(ReleveBancaireService releveBancaireService, SocieteService societeService, ReleveBancaireRepository releveBancaireRepository) {
          this.releveBancaireService = releveBancaireService;
          this.societeService = societeService;
          this.releveBancaireRepository = releveBancaireRepository;
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

    public ResponseEntity<ReportDataDto> getYearlyReport(int year) {
        List<ReleveBancaire> releves = releveBancaireService.findByYear(year);
        int totalTransactions = 0;
        double totalCreditedAmount = 0;
        double totalDebitedAmount = 0;
        Set<String> banks = new HashSet<>();
        Set<String> societes = new HashSet<>();

        for (ReleveBancaire releve : releves) {
            banks.add(releve.getNomBank());
            if (releve.getId_societe() != null) {
                Optional<SocieteDto> societeDto = societeService.findById(releve.getId_societe());
                societeDto.ifPresent(s -> societes.add(s.getNomSociete()));
            }

            for (ExtraitBancaire extrait : releve.getExtraits()) {
                for (DonneeExtrait donnee : extrait.getDonneeExtraits()) {
                    totalTransactions++;
                    totalCreditedAmount += Optional.ofNullable(donnee.getCredit()).orElse(0.0);
                    totalDebitedAmount += Optional.ofNullable(donnee.getDebit()).orElse(0.0);
                }
            }
        }

        String bankNames = String.join(", ", banks);
        String societeNames = String.join(", ", societes);
        //ReportDataDto reportData = new ReportDataDto(totalTransactions, totalCreditedAmount, totalDebitedAmount, societeNames, bankNames);
        return ResponseEntity.ok(new ReportDataDto());
    }

    @Override
    public ResponseEntity<List<ReportDataDto>> getAllYearsReport() {
        List<ReportDataDto> yearlyReports = releveBancaireService.aggregateDataForAllYears();
        return ResponseEntity.ok(yearlyReports);
    }

    @Override
    public ResponseEntity<List<ReportDataDto>> getAllMonthsReport() {
        List<ReportDataDto> monthlyReports = releveBancaireService.aggregateDataForAllMonths();
        return ResponseEntity.ok(monthlyReports);
    }

    @Override
    public ResponseEntity<List<ReportDataDto>> getAllWeeksReport() {
        List<ReportDataDto> weeklyReports = releveBancaireService.aggregateDataForAllWeeks();
        return ResponseEntity.ok(weeklyReports);
    }

    @Override
    public ResponseEntity<List<Integer>> getAvailableYears() {
        List<Integer> years = releveBancaireRepository.findDistinctYears();
        return ResponseEntity.ok(years);
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
