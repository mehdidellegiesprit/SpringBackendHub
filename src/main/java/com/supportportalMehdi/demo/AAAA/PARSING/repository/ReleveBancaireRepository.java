package com.supportportalMehdi.demo.AAAA.PARSING.repository;

import com.supportportalMehdi.demo.AAAA.PARSING.model.ReleveBancaire;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleveBancaireRepository extends MongoRepository<ReleveBancaire, ObjectId> {


    //List<Binary> findAllDataFile();


    @Query(value = "{}", fields = "{ 'dataFileContent' : 1}")
    List<ReleveBancaire> findAllDataFile();


}
