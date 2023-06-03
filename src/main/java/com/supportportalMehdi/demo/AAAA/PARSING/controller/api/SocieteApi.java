package com.supportportalMehdi.demo.AAAA.PARSING.controller.api;


import com.supportportalMehdi.demo.AAAA.PARSING.dto.SocieteDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.supportportalMehdi.demo.AAAA.PARSING.utils.Constants.APP_ROOT;

public interface SocieteApi {
    @PostMapping(value=APP_ROOT+"/societe/create",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SocieteDto> createSociete(@RequestBody SocieteDto societeDto);
}
