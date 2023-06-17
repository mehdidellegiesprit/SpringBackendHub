package com.supportportalMehdi.demo.AAAA.PARSING.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtraitBancaire {
    // nouveau  champ
    @Field("uuid")
    private UUID uuid = UUID.randomUUID();

    @Field("dateExtrait")
    private Date dateExtrait;

    @Field("dateDuSoldeCrediteurDebutMois")
    private Date dateDuSoldeCrediteurDebutMois;
    @Field("creditDuSoldeCrediteurDebutMois")
    private Double creditDuSoldeCrediteurDebutMois;

    @Field("donneeExtraits")
    private List<DonneeExtrait> donneeExtraits;

    @Field("totalMouvementsDebit")
    private Double totalMouvementsDebit;
    @Field("totalMouvementsCredit")
    private Double totalMouvementsCredit;

    @Field("dateDuSoldeCrediteurFinMois")
    private Date dateDuSoldeCrediteurFinMois;
    @Field("creditDuSoldeCrediteurFinMois")
    private Double creditDuSoldeCrediteurFinMois;
}
