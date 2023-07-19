package com.supportportalMehdi.demo.AAAA.PARSING.repository;

import com.supportportalMehdi.demo.AAAA.PARSING.model.Societe;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocieteRepository extends MongoRepository<Societe, ObjectId> {
    Optional<Societe> findByNameSociete(String nameSociete);
}


