package com.supportportalMehdi.demo.AAAA.PARSING.repository;

import com.supportportalMehdi.demo.AAAA.PARSING.model.ReleveBancaire;
import com.supportportalMehdi.demo.AAAA.PARSING.model.Societe;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocieteRepository extends MongoRepository<Societe, ObjectId> {
    Optional<Societe> findByNameSociete(String nameSociete);
    Optional<Societe> findById(ObjectId id);

    @Query(value = "{}", fields = "{ 'nameSociete' : 1}")
    List<Societe> findAllDistinctNames();
}


