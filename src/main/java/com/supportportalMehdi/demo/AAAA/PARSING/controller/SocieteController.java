package com.supportportalMehdi.demo.AAAA.PARSING.controller;



import com.supportportalMehdi.demo.AAAA.PARSING.controller.api.SocieteApi;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.SocieteDto;
import com.supportportalMehdi.demo.AAAA.PARSING.model.Societe;
import com.supportportalMehdi.demo.AAAA.PARSING.repository.ReleveBancaireRepository;
import com.supportportalMehdi.demo.AAAA.PARSING.repository.SocieteRepository;
import com.supportportalMehdi.demo.AAAA.PARSING.services.SocieteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SocieteController implements SocieteApi {

    private SocieteService societeService ;
    private SocieteRepository societeRepository ;


    @Autowired
    public SocieteController(SocieteService societeService,SocieteRepository societeRepository ) {
        this.societeService = societeService;
        this.societeRepository = societeRepository;
    }

    @Override
    public ResponseEntity<SocieteDto> createSociete(SocieteDto societeDto) {
        return ResponseEntity.ok(societeService.createSociete(societeDto));
    }

    @Override
    public ResponseEntity<List<SocieteDto>> getAllSociete() {
        return ResponseEntity.ok(societeService.getAllSocietes());
    }

    @Override
    public ResponseEntity<List<String>> getAvailableSocietes() {
        List<String> societes = societeRepository.findAllDistinctNames()
                .stream()
                .map(Societe::getNameSociete)
                .collect(Collectors.toList());
        return ResponseEntity.ok(societes);
    }
}
