package com.supportportalMehdi.demo.AAAA.PARSING.controller;



import com.supportportalMehdi.demo.AAAA.PARSING.controller.api.ReleveBancaireApi;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReleveBancaireDto;
import com.supportportalMehdi.demo.AAAA.PARSING.model.DonneeExtrait;
import com.supportportalMehdi.demo.AAAA.PARSING.model.FactureData;
import com.supportportalMehdi.demo.AAAA.PARSING.services.ReleveBancaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@RestController
public class ReleveBancaireController implements ReleveBancaireApi {


    private ReleveBancaireService releveBancaireService ;

    @Autowired
    public ReleveBancaireController(ReleveBancaireService releveBancaireService) {
        this.releveBancaireService = releveBancaireService;
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


}
