package com.supportportalMehdi.demo.AAAA.PARSING.dto;

import com.supportportalMehdi.demo.AAAA.PARSING.model.Societe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SocieteDto {

    private ObjectId id;
    private String nomSociete;

    public static SocieteDto fromEntity (Societe societe){

        if (societe==null){
            return null ;
        }
        return SocieteDto.builder()
                .id(societe.getId())
                .nomSociete(societe.getNomSociete())
                .build();
    }
    public static Societe toEntity (SocieteDto societeDto){
        if (societeDto==null){
            return null ;
        }
        Societe societe = new Societe() ;
        societe.setId(societeDto.getId());
        societe.setNomSociete(societeDto.getNomSociete());
        return societe;

    }

    public SocieteDto(String nomSociete) {
        this.nomSociete = nomSociete;
    }
}
