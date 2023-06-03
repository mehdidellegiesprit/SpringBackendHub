package com.supportportalMehdi.demo.AAAA.PARSING.controller.api;

import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReleveBancaireDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static com.supportportalMehdi.demo.AAAA.PARSING.utils.Constants.APP_ROOT;

public interface ReleveBancaireApi {
    @PostMapping(value=APP_ROOT+"/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ReleveBancaireDto> getUploadedFile(@RequestParam("file") MultipartFile file) throws IOException, ParseException;

    @GetMapping(value=APP_ROOT+"/AllRelevesBancaires",produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ReleveBancaireDto>> getAllRelevesBancaires();


}
