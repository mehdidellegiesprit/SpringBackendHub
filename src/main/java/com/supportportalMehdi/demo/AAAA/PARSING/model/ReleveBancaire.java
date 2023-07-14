package com.supportportalMehdi.demo.AAAA.PARSING.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document (collection = "ReleveBancaire")
public class ReleveBancaire {

    @Id
    private ObjectId id;
    @Field("nomBank")
    private String nomBank;
    @Field("id_societe")
    private ObjectId id_societe;
    @Field("extraits")
    private List<ExtraitBancaire> extraits;
    @Field("iban")
    private String iban;
    @Field("nameFile")
    private String nameFile;
    @Field("dataFileContent")
    @JsonIgnore
    private String dataFileContent;

    private String nom_societe;

}
