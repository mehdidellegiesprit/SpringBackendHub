package com.supportportalMehdi.demo.AAAA.PARSING.controller;



import com.supportportalMehdi.demo.AAAA.PARSING.controller.api.ReleveBancaireApi;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReleveBancaireDto;
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

//    @Override
//    public ResponseEntity<ReleveBancaireDto> AddReleve(ReleveBancaire data) {
//        System.out.println("Releve Bancaire :---"+data);
//        System.out.println("getNom_societe()-"+data.getNom_societe());
//        Optional<SocieteDto> exist_soc = this.societeService.findSocieteByNomSociete(data.getNom_societe());
//        System.out.println("jiji");
//        //findByNameSociete
//        System.out.println("exist_soc: " + exist_soc); // Add this line
//        if (!exist_soc.isPresent()) {
//            SocieteDto soc = this.societeService.createSociete(new SocieteDto(data.getNom_societe()));
//            data.setId_societe(soc.getId());
//        } else {
//            //data.setId_societe(exist_soc.get().getId());
//            data.setId_societe(exist_soc.get().getId());
//
//        }
//        return ResponseEntity.ok(releveBancaireService.AddReleve(data));
//    }

    //addOrUpdateReleve
//    @Override
//    public ResponseEntity<ReleveBancaireDto> AddReleve(ReleveBancaire data) {
//        Optional<SocieteDto> exist_soc = this.societeService.findSocieteByNomSociete(data.getNom_societe());
//
//        if (!exist_soc.isPresent()) {
//            SocieteDto soc = this.societeService.createSociete(new SocieteDto(data.getNom_societe()));
//            data.setId_societe(soc.getId());
//        } else {
//            data.setId_societe(exist_soc.get().getId());
//        }
//
//        // Check if 'ReleveBancaire' already exists with the same IBAN
//        Optional<ReleveBancaireDto> optReleveDto = releveBancaireService.findByIban(data.getIban());
//
//        if (optReleveDto.isPresent()) {
//            ReleveBancaire existingReleve = ReleveBancaireDto.toEntity(optReleveDto.get());
//
//            // Merge 'extraits' of the new and existing 'ReleveBancaire'
//            for (ExtraitBancaire newExtrait : data.getExtraits()) {
//                boolean extraitExists = false;
//                for (ExtraitBancaire existingExtrait : existingReleve.getExtraits()) {
//                    if (newExtrait.getDateExtrait().equals(existingExtrait.getDateExtrait())) {
//                        extraitExists = true;
//
//                        // Merge 'donneeExtraits'
//                        HashSet<DonneeExtrait> mergedDonneeExtraits = new HashSet<>(existingExtrait.getDonneeExtraits());
//                        mergedDonneeExtraits.addAll(newExtrait.getDonneeExtraits());
//                        existingExtrait.setDonneeExtraits(new ArrayList<>(mergedDonneeExtraits));
//
//                        // Sort 'donneeExtraits' by 'dateDonneeExtrait' and 'dateValeurDonneeExtrait'
//                        existingExtrait.getDonneeExtraits().sort(Comparator.comparing(DonneeExtrait::getDateDonneeExtrait)
//                                .thenComparing(DonneeExtrait::getDateValeurDonneeExtrait));
//
//                        // Replace totals, don't add to them
//                        existingExtrait.setTotalMouvementsDebit(newExtrait.getTotalMouvementsDebit());
//                        existingExtrait.setTotalMouvementsCredit(newExtrait.getTotalMouvementsCredit());
//                    }
//                }
//                // If the 'extrait' does not exist in the existing statement, add it
//                if (!extraitExists) {
//                    existingReleve.getExtraits().add(newExtrait);
//                }
//            }
//            // Sort 'extraits' by 'dateExtrait'
//            existingReleve.getExtraits().sort(Comparator.comparing(ExtraitBancaire::getDateExtrait));
//
//            releveBancaireService.updateReleveBancaire(ReleveBancaireDto.fromEntity(existingReleve)); // Update existing 'ReleveBancaire'
//        } else {
//            releveBancaireService.createReleveBancaire(ReleveBancaireDto.fromEntity(data)); // Save new 'ReleveBancaire'
//        }
//
//        // Return the updated or created 'ReleveBancaire'
//        Optional<ReleveBancaireDto> releveBancaireDto = releveBancaireService.findByIban(data.getIban());
//        if (releveBancaireDto.isPresent()) {
//            return ResponseEntity.ok(releveBancaireDto.get());
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//    }

    @Override
    public ResponseEntity<ReleveBancaireDto> AddReleve(ReleveBancaire data) {
        Optional<SocieteDto> exist_soc = this.societeService.findSocieteByNomSociete(data.getNom_societe());

        if (!exist_soc.isPresent()) {
            SocieteDto soc = this.societeService.createSociete(new SocieteDto(data.getNom_societe()));
            data.setId_societe(soc.getId());
        } else {
            data.setId_societe(exist_soc.get().getId());
        }

        // Check if 'ReleveBancaire' already exists with the same IBAN
        Optional<ReleveBancaireDto> optReleveDto = releveBancaireService.findByIban(data.getIban());

        if (optReleveDto.isPresent()) {
            ReleveBancaire existingReleve = ReleveBancaireDto.toEntity(optReleveDto.get());

            // Merge 'extraits' of the new and existing 'ReleveBancaire'
            for (ExtraitBancaire newExtrait : data.getExtraits()) {
                boolean extraitExists = false;
                for (ExtraitBancaire existingExtrait : existingReleve.getExtraits()) {
                    if (newExtrait.getDateExtrait().equals(existingExtrait.getDateExtrait())) {
                        extraitExists = true;

                        // Merge 'donneeExtraits'
                        for (DonneeExtrait newDonnee : newExtrait.getDonneeExtraits()) {
                            boolean donneeExists = false;
                            for (DonneeExtrait existingDonnee : existingExtrait.getDonneeExtraits()) {
                                if (newDonnee.equals(existingDonnee)) {
                                    donneeExists = true;
                                    break;
                                }
                            }
                            if (!donneeExists) {
                                existingExtrait.getDonneeExtraits().add(newDonnee);
                            }
                        }

                        // Sort 'donneeExtraits' by 'dateDonneeExtrait' and 'dateValeurDonneeExtrait'
                        existingExtrait.getDonneeExtraits().sort(Comparator.comparing(DonneeExtrait::getDateDonneeExtrait)
                                .thenComparing(DonneeExtrait::getDateValeurDonneeExtrait));

                        // Replace totals, don't add to them
                        existingExtrait.setTotalMouvementsDebit(newExtrait.getTotalMouvementsDebit());
                        existingExtrait.setTotalMouvementsCredit(newExtrait.getTotalMouvementsCredit());
                        break;
                    }
                }
                // If the 'extrait' does not exist in the existing statement, add it
                if (!extraitExists) {
                    existingReleve.getExtraits().add(newExtrait);
                }
            }
            // Sort 'extraits' by 'dateExtrait'
            existingReleve.getExtraits().sort(Comparator.comparing(ExtraitBancaire::getDateExtrait));

            releveBancaireService.updateReleveBancaire(ReleveBancaireDto.fromEntity(existingReleve)); // Update existing 'ReleveBancaire'
        } else {
            releveBancaireService.createReleveBancaire(ReleveBancaireDto.fromEntity(data)); // Save new 'ReleveBancaire'
        }

        // Return the updated or created 'ReleveBancaire'
        Optional<ReleveBancaireDto> releveBancaireDto = releveBancaireService.findByIban(data.getIban());
        if (releveBancaireDto.isPresent()) {
            return ResponseEntity.ok(releveBancaireDto.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
