package com.supportportalMehdi.demo.AAAA.PARSING.services;

import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReleveBancaireDto;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReportDataDto;
import com.supportportalMehdi.demo.AAAA.PARSING.model.DonneeExtrait;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ReleveBancaire;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ReleveBancaireService {
    ReleveBancaireDto createReleveBancaire(ReleveBancaireDto releveBancaireDto) ;
    Optional<ReleveBancaireDto> getReleveBancaireById(ObjectId id);
    List<ReleveBancaireDto> getAllReleveBancaires() ;
    ReleveBancaireDto updateReleveBancaire(ReleveBancaireDto releveBancaireDto) ;
    void deleteReleveBancaire(ObjectId id) ;
    ReleveBancaireDto parseAndExtract(MultipartFile file) throws IOException, ParseException;

    ReleveBancaireDto AddFactureToDonneeExtrait(DonneeExtrait data);

    ReleveBancaireDto updateCommentaireFactureToDonneeExtrait(DonneeExtrait data);

    ReleveBancaireDto deleteFacture(String facture,DonneeExtrait data);

    ReleveBancaireDto AddReleve(ReleveBancaire data);

    Optional<ReleveBancaireDto> findByIban(String iban);

    public List<ReleveBancaire> findByYear(int year) ;

    List<ReportDataDto> aggregateDataForAllYears();

    List<ReportDataDto> aggregateDataForAllMonths();

    List<ReportDataDto> aggregateDataForAllWeeks();
}
