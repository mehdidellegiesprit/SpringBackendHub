package com.supportportalMehdi.demo.AAAA.PARSING.controller.api;

import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReleveBancaireDto;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReportDataDto;
import com.supportportalMehdi.demo.AAAA.PARSING.model.DonneeExtrait;
import com.supportportalMehdi.demo.AAAA.PARSING.model.FactureData;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ReleveBancaire;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static com.supportportalMehdi.demo.AAAA.PARSING.utils.Constants.APP_ROOT;

public interface ReleveBancaireApi {
    @PostMapping(value=APP_ROOT+"/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ReleveBancaireDto> getUploadedFile(@RequestParam("file") MultipartFile file) throws IOException, ParseException;

    @GetMapping(value=APP_ROOT+"/AllRelevesBancaires",produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ReleveBancaireDto>> getAllRelevesBancaires();

    @PostMapping(value=APP_ROOT+"/facture/add",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ReleveBancaireDto> AddFactureComment(@RequestBody DonneeExtrait data) ;


    @PostMapping(value=APP_ROOT+"/facture/commentaire/update",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ReleveBancaireDto> updateCommentaireFacture(@RequestBody DonneeExtrait data) ;

    @PostMapping(value = APP_ROOT + "/facture/delete")
    ResponseEntity<ReleveBancaireDto> deleteFacture(@RequestBody FactureData factureData);

    @PostMapping(value=APP_ROOT+"/releve/add",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ReleveBancaireDto> AddReleve(@RequestBody ReleveBancaire data) ;

    @GetMapping(value = APP_ROOT + "/yearlyReport/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ReportDataDto> getYearlyReport(@PathVariable("year") int year);
}
