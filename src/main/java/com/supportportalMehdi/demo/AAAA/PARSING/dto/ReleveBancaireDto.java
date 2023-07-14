package com.supportportalMehdi.demo.AAAA.PARSING.dto;

import com.supportportalMehdi.demo.AAAA.PARSING.model.ExtraitBancaire;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ReleveBancaire;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ReleveBancaireDto {
    private ObjectId id;
    private String nomBank;
    private ObjectId id_societe;
    private List<ExtraitBancaire> extraits;
    private String iban;
    private String nameFile;
    private String dataFileContent;
    private String nom_societe;

    public static ReleveBancaireDto fromEntity (ReleveBancaire releveBancaire){

        if (releveBancaire==null){
            return null ;
        }
        return ReleveBancaireDto.builder()
                .id(releveBancaire.getId())
                .nomBank(releveBancaire.getNomBank())
                .id_societe(releveBancaire.getId_societe())
                .extraits(releveBancaire.getExtraits())
                .iban(releveBancaire.getIban())
                .nameFile(releveBancaire.getIban())
                .dataFileContent(releveBancaire.getDataFileContent())
                .nom_societe(releveBancaire.getNom_societe())
                .build();
    }
    public static ReleveBancaire toEntity (ReleveBancaireDto releveBancaireDto){
        if (releveBancaireDto==null){
            return null ;
        }
        ReleveBancaire releveBancaire = new ReleveBancaire() ;
        releveBancaire.setId(releveBancaireDto.getId());
        releveBancaire.setNomBank(releveBancaireDto.getNomBank());
        releveBancaire.setId_societe(releveBancaireDto.getId_societe());
        releveBancaire.setExtraits(releveBancaireDto.getExtraits());
        releveBancaire.setIban(releveBancaireDto.getIban());
        releveBancaire.setNameFile(releveBancaireDto.getNameFile());
        releveBancaire.setDataFileContent(releveBancaireDto.getDataFileContent());
        releveBancaire.setNom_societe(releveBancaireDto.getNom_societe());
        return releveBancaire;
    }
}
