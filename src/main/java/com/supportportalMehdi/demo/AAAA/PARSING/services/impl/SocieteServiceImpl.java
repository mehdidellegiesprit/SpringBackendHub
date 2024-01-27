package com.supportportalMehdi.demo.AAAA.PARSING.services.impl;


import com.supportportalMehdi.demo.AAAA.PARSING.dto.SocieteDto;
import com.supportportalMehdi.demo.AAAA.PARSING.model.Societe;
import com.supportportalMehdi.demo.AAAA.PARSING.repository.SocieteRepository;
import com.supportportalMehdi.demo.AAAA.PARSING.services.SocieteService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SocieteServiceImpl implements SocieteService {

    private SocieteRepository societeRepository ;

    @Autowired
    public SocieteServiceImpl(SocieteRepository societeRepository) {
        this.societeRepository = societeRepository;
    }

    @Override
    public SocieteDto createSociete(SocieteDto societeDto) {
        return SocieteDto.fromEntity(
                this.societeRepository.save(SocieteDto.toEntity(societeDto))
        );
    }

    @Override
    public Optional<SocieteDto> findSocieteByNomSociete(String name) {
        Optional<Societe> societe = societeRepository.findByNameSociete(name);
        return societe.map(SocieteDto::fromEntity);
    }


    @Override
    public List<SocieteDto> getAllSocietes() {
        return societeRepository.findAll()
                .stream() // boucler parcourir
                .map(SocieteDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SocieteDto> findById(ObjectId id) {
        if (id == null) {
            return Optional.empty();
        }

        return societeRepository.findById(id)
                .map(SocieteDto::fromEntity); // Convertir Societe en SocieteDto si trouvé
    }

}
