package com.supportportalMehdi.demo.AAAA.PARSING.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonneeExtrait {
    // nouveau  champ
    @Field("uuid")
    private UUID uuid = UUID.randomUUID();

    @Field("dateDonneeExtrait")
    private Date dateDonneeExtrait;

    @Field("dateValeurDonneeExtrait")
    private Date dateValeurDonneeExtrait;

    @Field("operations")
    private String operations;

    @Field("debit")
    private Double debit;

    @Field("credit")
    private Double credit;

    @Field("factures")
    private List<String> factures;

    @Field("commentairesFactures")
    HashMap<String, String> commentairesFactures = new HashMap<>();

    @Field("valide")
    private boolean valide;

    @Field("associationTitreUrl")
    HashMap<String, String> associationTitreUrl = new HashMap<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DonneeExtrait)) return false;
        DonneeExtrait that = (DonneeExtrait) o;
        return Objects.equals(dateDonneeExtrait, that.dateDonneeExtrait) &&
                Objects.equals(dateValeurDonneeExtrait, that.dateValeurDonneeExtrait) &&
                Objects.equals(operations, that.operations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateDonneeExtrait, dateValeurDonneeExtrait, operations);
    }

}
