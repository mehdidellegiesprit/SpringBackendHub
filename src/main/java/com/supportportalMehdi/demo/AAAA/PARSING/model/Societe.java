package com.supportportalMehdi.demo.AAAA.PARSING.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Societe")
public class Societe {

    @Id
    private ObjectId id;

    @Field("nameSociete")
    private String nomSociete;
}
