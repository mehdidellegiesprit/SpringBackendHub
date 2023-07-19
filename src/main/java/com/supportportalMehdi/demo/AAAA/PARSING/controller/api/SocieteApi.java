package com.supportportalMehdi.demo.AAAA.PARSING.controller.api;


import com.supportportalMehdi.demo.AAAA.PARSING.dto.SocieteDto;
import com.supportportalMehdi.demo.AAAA.PARSING.model.Societe;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.supportportalMehdi.demo.AAAA.PARSING.utils.Constants.APP_ROOT;

public interface SocieteApi {
    @PostMapping(value=APP_ROOT+"/societe/create",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SocieteDto> createSociete(@RequestBody SocieteDto societeDto);
    @GetMapping(value=APP_ROOT+"/societe/all",produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<SocieteDto>> getAllSociete();

}
