package com.supportportalMehdi.demo.AAAA.PARSING.controller;



import com.supportportalMehdi.demo.AAAA.PARSING.controller.api.ReleveBancaireApi;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReleveBancaireDto;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.SocieteDto;
import com.supportportalMehdi.demo.AAAA.PARSING.model.DonneeExtrait;
import com.supportportalMehdi.demo.AAAA.PARSING.model.FactureData;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ReleveBancaire;
import com.supportportalMehdi.demo.AAAA.PARSING.services.ReleveBancaireService;
import com.supportportalMehdi.demo.AAAA.PARSING.services.SocieteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

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
        System.out.println("Releve Bancaire :---"+data);
        System.out.println("getNom_societe()-"+data.getNom_societe());
        Optional<SocieteDto> exist_soc = this.societeService.findSocieteByNomSociete(data.getNom_societe());
        System.out.println("jiji");
        //findByNameSociete
        System.out.println("exist_soc: " + exist_soc); // Add this line

        if (!exist_soc.isPresent()) {
            SocieteDto soc = this.societeService.createSociete(new SocieteDto(data.getNom_societe()));
            data.setId_societe(soc.getId());
        } else {
            //data.setId_societe(exist_soc.get().getId());
        }
        return ResponseEntity.ok(releveBancaireService.AddReleve(data));

    }


}
