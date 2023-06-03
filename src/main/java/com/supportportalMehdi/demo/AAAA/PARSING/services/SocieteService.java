package com.supportportalMehdi.demo.AAAA.PARSING.services;

import com.supportportalMehdi.demo.AAAA.PARSING.dto.SocieteDto;

import java.util.List;
import java.util.Optional;

public interface SocieteService {
    SocieteDto createSociete(SocieteDto societeDto) ;
    Optional<SocieteDto> findSocieteByNomSociete(String name);
    List<SocieteDto> getAllSocietes() ;
}
