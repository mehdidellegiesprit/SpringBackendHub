package com.supportportalMehdi.demo.AAAA.PARSING.controller;



import com.supportportalMehdi.demo.AAAA.PARSING.controller.api.SocieteApi;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.SocieteDto;
import com.supportportalMehdi.demo.AAAA.PARSING.services.SocieteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SocieteController implements SocieteApi {

    private SocieteService societeService ;


    @Autowired
    public SocieteController(SocieteService societeService) {
        this.societeService = societeService;
    }

    @Override
    public ResponseEntity<SocieteDto> createSociete(SocieteDto societeDto) {
        return ResponseEntity.ok(societeService.createSociete(societeDto));
    }

    @Override
    public ResponseEntity<List<SocieteDto>> getAllSociete() {
        return ResponseEntity.ok(societeService.getAllSocietes());
    }
}
