package com.supportportalMehdi.demo.AAAA.PARSING.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonneeExtrait {

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


    @Field("commentairesFactures")
    private HashMap<String, String> commentairesFactures;

    @Field("valide")
    private boolean valide;

}
