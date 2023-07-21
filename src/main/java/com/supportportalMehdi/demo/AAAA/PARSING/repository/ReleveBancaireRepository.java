package com.supportportalMehdi.demo.AAAA.PARSING.repository;

import com.supportportalMehdi.demo.AAAA.PARSING.model.DonneeExtrait;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ExtraitBancaire;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ReleveBancaire;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReleveBancaireRepository extends MongoRepository<ReleveBancaire, ObjectId> {


    default ReleveBancaire findWithDonneeExtrait(UUID uuid) {
        List<ReleveBancaire> releveBancaires = findAll();

        for (ReleveBancaire releveBancaire : releveBancaires) {
            for (ExtraitBancaire extrait : releveBancaire.getExtraits()) {
                for (DonneeExtrait donneeExtrait : extrait.getDonneeExtraits()) {
                    if (donneeExtrait.getUuid().equals(uuid)) {
                        return releveBancaire;
                    }
                }
            }
        }

        return null;
    }


    @Query(value = "{}", fields = "{ 'dataFileContent' : 1}")
    List<ReleveBancaire> findAllDataFile();

    Optional<ReleveBancaire> findByIban(String iban);
}
