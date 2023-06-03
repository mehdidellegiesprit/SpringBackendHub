package com.supportportalMehdi.demo.AAAA.PARSING.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtraitBancaire {

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
