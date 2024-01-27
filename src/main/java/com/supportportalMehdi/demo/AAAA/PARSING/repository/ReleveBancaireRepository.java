package com.supportportalMehdi.demo.AAAA.PARSING.repository;

import com.supportportalMehdi.demo.AAAA.PARSING.model.DonneeExtrait;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ExtraitBancaire;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ReleveBancaire;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

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

    @Query("{'extraits.dateExtrait': {$gte: ?0, $lt: ?1}}")
    List<ReleveBancaire> findAllByYear(Date startOfYear, Date endOfYear);
    @Query(value = "{}", fields = "{ 'extraits.dateExtrait' : 1}")
    List<ReleveBancaire> findAllWithDates();
    default List<Integer> findDistinctYears() {
        List<ReleveBancaire> releves = findAllWithDates();
        return releves.stream()
                .flatMap(releve -> releve.getExtraits().stream())
                .map(ExtraitBancaire::getDateExtrait)
                .filter(Objects::nonNull)
                .map(date -> date.getYear() + 1900) // Ajout de 1900 à l'année
                .distinct()
                .sorted() // optionnel, pour trier les années
                .collect(Collectors.toList());
    }

}
