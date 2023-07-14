package com.supportportalMehdi.demo.AAAA.PARSING.services.impl;



import com.sun.istack.NotNull;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.ReleveBancaireDto;
import com.supportportalMehdi.demo.AAAA.PARSING.dto.SocieteDto;
import com.supportportalMehdi.demo.AAAA.PARSING.exception.ErrorCodes;
import com.supportportalMehdi.demo.AAAA.PARSING.exception.InvalidOperationException;
import com.supportportalMehdi.demo.AAAA.PARSING.model.DonneeExtrait;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ExtraitBancaire;
import com.supportportalMehdi.demo.AAAA.PARSING.model.ReleveBancaire;
import com.supportportalMehdi.demo.AAAA.PARSING.repository.ReleveBancaireRepository;
import com.supportportalMehdi.demo.AAAA.PARSING.services.ReleveBancaireService;
import com.supportportalMehdi.demo.AAAA.PARSING.services.SocieteService;
import com.supportportalMehdi.demo.AAAA.PARSING.utils.Util;
import io.github.jonathanlink.PDFLayoutTextStripper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.Adler32;


@Service
@Slf4j
public class ReleveBancaireServiceImpl implements ReleveBancaireService {

    private ReleveBancaireRepository releveBancaireRepository ;
    private SocieteService societeService ;


    @Autowired
    public ReleveBancaireServiceImpl(ReleveBancaireRepository releveBancaireRepository,
                                     SocieteService societeService
                                     ) {
        this.releveBancaireRepository = releveBancaireRepository;
        this.societeService = societeService;
    }

    public boolean ExisteFileDocument(@NotNull MultipartFile file1) throws IOException {
        // Convert MultipartFile to binary data
        System.out.println("size==="+releveBancaireRepository.findAllDataFile().size());
        byte[] file1Binary = file1.getBytes();
        for (int i = 0 ; i < releveBancaireRepository.findAllDataFile().size();i++) {
            ReleveBancaireDto releveBancaireDto = ReleveBancaireDto.fromEntity(releveBancaireRepository.findAllDataFile().get(i));
            String base64String = releveBancaireDto.getDataFileContent() ;
            System.out.println("base64String==="+base64String);
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);

            Adler32 checksum1 = new Adler32();
            Adler32 checksum2 = new Adler32();
            checksum1.update(file1Binary);
            checksum2.update(decodedBytes);
            boolean areEqual = checksum1.getValue() == checksum2.getValue();
            if (areEqual){
                return true ;
            }
        }
        return false;
    }
    @Override
    public ReleveBancaireDto createReleveBancaire(ReleveBancaireDto releveBancaireDto) {
        ReleveBancaire entity = ReleveBancaireDto.toEntity(releveBancaireDto);
        entity = releveBancaireRepository.save(entity);
        return ReleveBancaireDto.fromEntity(entity);
    }

    @Override
    public Optional<ReleveBancaireDto> getReleveBancaireById(ObjectId id) {
        Optional<ReleveBancaire> releveBancaire = releveBancaireRepository.findById(id);
        return Optional.of(ReleveBancaireDto.fromEntity(releveBancaire.get()));
    }

    @Override
    public List<ReleveBancaireDto> getAllReleveBancaires() {
        return releveBancaireRepository.findAll()
                .stream() // boucler parcourir
                .map(ReleveBancaireDto::fromEntity)
                .collect(Collectors.toList());
    }

    // updateReleveBancaire is not good developped
    @Override
    public ReleveBancaireDto updateReleveBancaire(ReleveBancaireDto releveBancaireDto) {
        Optional<ReleveBancaire> existingReleveBancaire = Optional.ofNullable(ReleveBancaireDto.toEntity(releveBancaireDto));
        if (existingReleveBancaire.isPresent()) {
            ReleveBancaire updatedReleveBancaire = existingReleveBancaire.get();
            updatedReleveBancaire.setNomBank(releveBancaireDto.getNomBank());
            updatedReleveBancaire.setId_societe(releveBancaireDto.getId_societe());
            updatedReleveBancaire.setExtraits(ReleveBancaireDto.toEntity(releveBancaireDto).getExtraits());
            updatedReleveBancaire.setIban(releveBancaireDto.getIban());
            return ReleveBancaireDto.fromEntity(releveBancaireRepository.save(updatedReleveBancaire));
        } else {
            throw new RuntimeException("ReleveBancaire not found");
        }
    }

    @Override
    public void deleteReleveBancaire(ObjectId id) {
        releveBancaireRepository.deleteById(id);
    }

    @Override
    public ReleveBancaireDto parseAndExtract(MultipartFile file) throws IOException, ParseException {
        boolean ok = false;
        ok=this.ExisteFileDocument(file);

        if (ok) {
            System.out.println("Impossible===========");
            throw new InvalidOperationException("Impossible de parser un fichier deja Traiteé !!!!!!! ",
                    ErrorCodes.FILE_ALREADY_IN_USE);
        }
        if (!ok){
            try {
                String pathFileUploaded  = getPathFileUploaded(file);
                System.out.println("pathFileUploaded="+"*"+pathFileUploaded+"*");

                String nameBank  = findNameBank(pathFileUploaded);
                System.out.println("nameBank="+"*"+nameBank+"*");
                ReleveBancaireDto releveBancaireDto = new ReleveBancaireDto();

                Util util = new Util();
                if (nameBank.contains("CIC")){
                    extractDataFromCicBank(util,pathFileUploaded);
                    extractListeOperationFromCicBank(releveBancaireDto, util,pathFileUploaded);
                    System.out.println(releveBancaireDto);
                    releveBancaireDto.setNomBank("CIC");

                    SocieteDto societeDto = new SocieteDto(util.getNameSociete());
                    releveBancaireDto.setId_societe(societeDto.getId());
                    releveBancaireDto.setNameFile(file.getOriginalFilename());

                    FileInputStream inputStream = new FileInputStream(pathFileUploaded);
                    String base64String = Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream));
                    releveBancaireDto.setDataFileContent(base64String);

                    //releveBancaireDto = this.createReleveBancaire(releveBancaireDto);
                    releveBancaireDto.setNom_societe(util.getNameSociete());

                    return releveBancaireDto; // On retourne directement le DTO sans l'enregistrer dans la base de données
                }
                if (nameBank.contains("BP")){
                    extractDataFromPopBank(util,pathFileUploaded);
                    System.out.println("nom_societe"+util.getNameSociete());
                    System.out.println("chaine_max_espace"+util.getChaineMaxEspace());
                    System.out.println("max_espace"+util.getMaxEspaces());
                    extractListeOperationFromPopBank(releveBancaireDto, util,pathFileUploaded);
                    System.out.println(releveBancaireDto);
                    releveBancaireDto.setNomBank("BP");
                    SocieteDto societeDto = new SocieteDto(util.getNameSociete());
                    releveBancaireDto.setId_societe(societeDto.getId());
                    releveBancaireDto.setNameFile(file.getOriginalFilename());

                    FileInputStream inputStream = new FileInputStream(pathFileUploaded);
                    String base64String = Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream));
                    releveBancaireDto.setDataFileContent(base64String);

                    //releveBancaireDto = this.createReleveBancaire(releveBancaireDto);
                    releveBancaireDto.setNom_societe(util.getNameSociete());

                    return releveBancaireDto; // On retourne directement le DTO sans l'enregistrer dans la base de données
                }
                // le nom de  la societe
            } catch (IOException e) {
                System.out.println("Failed to delete temporary file: " + e.getMessage());
                throw new InvalidOperationException("Impossible de parser un fichier deja en traitement",
                        ErrorCodes.FILE_ALREADY_IN_USE);
            }
        }
        return null;
    }


    @Override
    public ReleveBancaireDto AddFactureToDonneeExtrait(DonneeExtrait data) {
        // Trouver le ReleveBancaire contenant le DonneeExtrait à mettre à jour
        ReleveBancaire releveBancaire = releveBancaireRepository.findWithDonneeExtrait(data.getUuid());

        if (releveBancaire != null) {
            // Parcourir la liste des ExtraitBancaire et leur liste de DonneeExtrait pour trouver le bon à mettre à jour
            for (ExtraitBancaire extrait : releveBancaire.getExtraits()) {
                for (DonneeExtrait donneeExtrait : extrait.getDonneeExtraits()) {
                    if (donneeExtrait.getUuid().equals(data.getUuid())) {
                        // Mise à jour de l'objet DonneeExtrait
                        donneeExtrait.setDateDonneeExtrait(data.getDateDonneeExtrait());
                        donneeExtrait.setDateValeurDonneeExtrait(data.getDateValeurDonneeExtrait());
                        donneeExtrait.setOperations(data.getOperations());
                        donneeExtrait.setDebit(data.getDebit());
                        donneeExtrait.setCredit(data.getCredit());
                        donneeExtrait.setFactures(data.getFactures());
                        donneeExtrait.setCommentairesFactures(data.getCommentairesFactures());
                        //new added
                        donneeExtrait.setAssociationTitreUrl(data.getAssociationTitreUrl());
                        donneeExtrait.setValide(data.isValide());

                        // Enregistrement de l'objet ReleveBancaire mis à jour
                        ReleveBancaire updatedReleveBancaire = releveBancaireRepository.save(releveBancaire);

                        // Conversion de l'objet ReleveBancaire en ReleveBancaireDto
                        return ReleveBancaireDto.fromEntity(updatedReleveBancaire);
                    }
                }
            }
        }

        // Si aucun DonneeExtrait correspondant n'a été trouvé, retourner null ou gérer l'erreur comme vous le souhaitez
        return null;
    }

    @Override
    public ReleveBancaireDto updateCommentaireFactureToDonneeExtrait(DonneeExtrait data) {
        // Trouver le ReleveBancaire contenant le DonneeExtrait à mettre à jour
        ReleveBancaire releveBancaire = releveBancaireRepository.findWithDonneeExtrait(data.getUuid());

        if (releveBancaire != null) {
            // Parcourir la liste des ExtraitBancaire et leur liste de DonneeExtrait pour trouver le bon à mettre à jour
            for (ExtraitBancaire extrait : releveBancaire.getExtraits()) {
                for (DonneeExtrait donneeExtrait : extrait.getDonneeExtraits()) {
                    if (donneeExtrait.getUuid().equals(data.getUuid())) {
                        // Mise à jour de l'objet DonneeExtrait
                        donneeExtrait.setDateDonneeExtrait(data.getDateDonneeExtrait());
                        donneeExtrait.setDateValeurDonneeExtrait(data.getDateValeurDonneeExtrait());
                        donneeExtrait.setOperations(data.getOperations());
                        donneeExtrait.setDebit(data.getDebit());
                        donneeExtrait.setCredit(data.getCredit());
                        donneeExtrait.setFactures(data.getFactures());
                        donneeExtrait.setCommentairesFactures(data.getCommentairesFactures());
                        donneeExtrait.setValide(data.isValide());

                        // Enregistrement de l'objet ReleveBancaire mis à jour
                        ReleveBancaire updatedReleveBancaire = releveBancaireRepository.save(releveBancaire);

                        // Conversion de l'objet ReleveBancaire en ReleveBancaireDto
                        return ReleveBancaireDto.fromEntity(updatedReleveBancaire);
                    }
                }
            }
        }

        // Si aucun DonneeExtrait correspondant n'a été trouvé, retourner null ou gérer l'erreur comme vous le souhaitez
        return null;
    }

    @Override
    public ReleveBancaireDto deleteFacture(String facture, DonneeExtrait data) {
        // Fetch the specific ReleveBancaire that contains the DonneeExtrait with the specified UUID
        ReleveBancaire releveBancaire = releveBancaireRepository.findWithDonneeExtrait(data.getUuid());

        if (releveBancaire != null) {
            for (ExtraitBancaire extrait : releveBancaire.getExtraits()) {
                for (DonneeExtrait donneeExtrait : extrait.getDonneeExtraits()) {
                    // Find the DonneeExtrait instance with the specified UUID
                    if (donneeExtrait.getUuid().equals(data.getUuid())) {
                        // If the facture is present in the list, remove it
                        if (donneeExtrait.getFactures().contains(facture)) {
                            donneeExtrait.getFactures().remove(facture);

                            // If a commentaire for this facture exists, remove it
                            if (donneeExtrait.getCommentairesFactures().containsKey(facture)) {
                                donneeExtrait.getCommentairesFactures().remove(facture);
                            }
                            if (donneeExtrait.getAssociationTitreUrl().containsKey(facture)) {
                                donneeExtrait.getAssociationTitreUrl().remove(facture);
                            }
                        }
                    }
                }
            }
            // Save the modified ReleveBancaire back to the repository
            ReleveBancaire updatedReleveBancaire = releveBancaireRepository.save(releveBancaire);
            // Conversion de l'objet ReleveBancaire en ReleveBancaireDto
            return ReleveBancaireDto.fromEntity(updatedReleveBancaire);
        }

        // In this case, return null or transform your ReleveBancaire object into ReleveBancaireDto object
        // and return it as per your requirement.
        return null;
    }

    public void  extractListeOperationFromCicBank(ReleveBancaireDto releveBancaireDto, Util util, String pathFileUploaded) throws IOException, ParseException {
        int positionMax = util.getChaineMaxEspace().length() - util.getMaxEspaces() - 1 ;
        //String path = "C:\\Users\\mehdi\\Desktop\\mehdi_test_extract_pfe\\TestExtraction\\Ext1.pdf";
        File file = new File(pathFileUploaded) ;
        FileInputStream fis = new FileInputStream(file) ;
        PDDocument pdfDocument = PDDocument.load(fis);
        Splitter splitter = new Splitter();
        List<PDDocument> splitpages = splitter.split(pdfDocument);
        // pdfTextStripper = pdfReader
        PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper() ;
        pdfTextStripper.setSortByPosition(true);
        pdfTextStripper.setAddMoreFormatting(true);




        String nameBank = "CIC" ;
        ///ok c'est l 'indicateur pour extraire nom societe / adresse / nom du PDG
        boolean ok = false ;int nombreLigneNonVide = 0 ;


        boolean ok_test_Releve_bancaire = false;
        boolean ok_test_format_releve_bancaire = false;
        boolean ok_test_head_array = false;
        boolean ok_test_debut_operation_array = false;
        String date_prelevement_extrait ="" ;
        String premier_ligne_solde_crediteur_au_date = "";
        String premier_ligne_credit_euro = "";
        String dernier_ligne_solde_crediteur_au_date = "";
        String dernier_ligne_credit_euro = "";
        boolean ok_test_date_valeur_operation = false;
        String date_valeur_operation = "";
        String debit_ou_credit = "" ;
        String liste_opertation="" ;
        String dateOperation = "";
        String date_valeur_Operation="";
        boolean ok_test_fin_page;
        boolean ok_total_mouvement = false;
        String debit_total_mouvement="";
        String credit_total_mouvement="";
        String IBAN = "" ;
        String dateOperation_precedente ="";
        String date_valeur_Operation_precedente ="";
        boolean ok_solde_crediteur = false ;



        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DonneeExtrait donneeExtrait = new DonneeExtrait() ;
        ExtraitBancaire extraitBancaire = new ExtraitBancaire();



//		ArrayList<Operation>operationsArrayList = new ArrayList<>();
        List<ExtraitBancaire> extraits = new ArrayList<>();
        List<DonneeExtrait> donneeExtraits=new ArrayList<>();




        for (PDDocument page : splitpages) {
//			System.out.println("..................................................");
//			System.out.println(page);
//			System.out.println("..................................................");
            String contentPage = pdfTextStripper.getText(page); //Le contenu de la page i (1,2,3,4,5...)
            String[] arrayOfLignesContentPage = contentPage.split("\n"); // decouppage de content page ligne par ligne  par le delimiteur l /n
            ok_test_fin_page = false;
            ok_test_head_array = false;
            for (int i = 0; i < arrayOfLignesContentPage.length; i++) {
                System.out.println(arrayOfLignesContentPage[i]) ;//BREAK POINT
                // split = diviser
                //.split("\\s+") = split par espace ou plusieurs espaces
                String[] arrayOfWordsPerLigne = arrayOfLignesContentPage[i].split("\\s+");
                System.out.println(arrayOfWordsPerLigne) ;//BREAK POINT
                String chaine = convertArrayToString(arrayOfWordsPerLigne) ;
                System.out.println(chaine); //break point ici
                /***********************DEBUT DATA DE LA SOCIETE***************************/
                //if (chaine.contains("RELEVE ET INFORMATIONS BANCAIRES")){
                if (chaine.contains("RELEVE")){
                    ok_test_Releve_bancaire = true ;
                    System.out.println("ok_test_Releve_bancaire="+ok_test_Releve_bancaire);
                }
                if (ok_test_Releve_bancaire  ){
                    System.out.println("arrayOfWordsPerLigne="+arrayOfWordsPerLigne);
                    if (chaine.contains("T-CONNECT")){
                        chaine=chaine.replace("T-CONNECT", "");
                        chaine= chaine.substring(0,chaine.length()-1);
                    }
                    System.out.println(chaine);
                    if (isValidFormatOfDateReleveBancaire(chaine)){
                        ok_test_format_releve_bancaire = true ;
                        date_prelevement_extrait = chaine;
                        System.out.println("ok_test_format_releve_bancaire="+ok_test_format_releve_bancaire);
                        System.out.println("date_prelevement_extrait="+date_prelevement_extrait);
                    }
                }
                if (chaine.contains("Date Date valeur Opération Débit EUROS Crédit EUROS")){
                    ok_test_head_array = true ;
                    System.out.println("ok_test_head_array="+ok_test_head_array);
                }
                if (chaine.contains("SOLDE CREDITEUR AU")){
                    System.out.println("arrayOfWordsPerLigne ="+arrayOfWordsPerLigne) ;
                    System.out.println("chaine ="+chaine);
                }
                ///hethi fi awel tableau
                if (ok_total_mouvement==false){
                    if (chaine.contains("SOLDE CREDITEUR AU") && isValidDateCrediteur(arrayOfWordsPerLigne[4]) &&  isValidSoldeCrediteur(arrayOfWordsPerLigne[5])){
                        ok_test_debut_operation_array = true ;
                        premier_ligne_solde_crediteur_au_date = arrayOfWordsPerLigne[4] ;
                        premier_ligne_credit_euro = arrayOfWordsPerLigne[5] ;
                        System.out.println("ok_test_debut_operation_array="+ok_test_debut_operation_array);
                        System.out.println("premier_ligne_solde_crediteur_au_date="+premier_ligne_solde_crediteur_au_date);
                        System.out.println("premier_ligne_credit_euro="+premier_ligne_credit_euro);
                        //ok_total_mouvement=!ok_total_mouvement;
                    }
                }
                ///hethi fi e5er tableau
                if (ok_total_mouvement==true){
                    if (chaine.contains("SOLDE CREDITEUR AU") && isValidDateCrediteur(arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-2]) &&  isValidSoldeCrediteur(arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-1])){
                        //hethouma tawa
                        ok_solde_crediteur = true ;
                        ok_test_fin_page=true ;
                        liste_opertation = "" ;
                        //end of
                        ok_test_debut_operation_array = false ;
                        dernier_ligne_solde_crediteur_au_date = arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-2] ;
                        dernier_ligne_credit_euro = arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-1] ;
                        System.out.println("ok_test_debut_operation_array="+ok_test_debut_operation_array);
                        System.out.println("dernier_ligne_solde_crediteur_au_date="+dernier_ligne_solde_crediteur_au_date);
                        System.out.println("dernier_ligne_credit_euro="+dernier_ligne_credit_euro);
                        ok_total_mouvement=false ;
                    }
                }

                if (arrayOfWordsPerLigne.length>=3){
                    dateOperation = arrayOfWordsPerLigne[1] ;
                    date_valeur_Operation = arrayOfWordsPerLigne[2] ;
                }else {
                    dateOperation = "" ;
                    date_valeur_Operation = "" ;
                }
                if (chaine.contains("IBAN") && isValidIBAN(getOperationPerLigne(arrayOfWordsPerLigne,3,arrayOfWordsPerLigne.length-1))!=""){
                    //hethouma tawa
                    //IBAN = getOperationPerLigne(arrayOfWordsPerLigne,3,arrayOfWordsPerLigne.length-1) ;
                    IBAN = isValidIBAN(getOperationPerLigne(arrayOfWordsPerLigne,3,arrayOfWordsPerLigne.length-1)) ;

                    System.out.println("IBAN.........="+IBAN+"=");
                    // TODO IBAN = getOperationPerLigne(arrayOfWordsPerLigne,3,arrayOfWordsPerLigne.length-1) ;
                    //end of
//                    ok_test_fin_page=true ;
//                    liste_opertation = "" ;
                }
                if (isValidDateCrediteur(dateOperation) && isValidDateCrediteur(date_valeur_Operation)  && ok_test_fin_page==false && ok_test_head_array==true){
                    if (liste_opertation.equals("") == false){
                        System.out.println("\n............................................................\n");
                        System.out.println("dateOperation_precedente="+dateOperation_precedente);
                        System.out.println("date_valeur_Operation_precedente="+date_valeur_Operation_precedente);
                        System.out.println("liste_opertation="+liste_opertation);
                        System.out.println("\n............................................................\n");

                        donneeExtrait.setOperations(liste_opertation);
                        // factures
                        donneeExtrait.setFactures(new ArrayList<>());
                        donneeExtrait.setCommentairesFactures(new HashMap<>());
                        donneeExtrait.setValide(false);




                        //donneeExtraitDto = this.donneeExtraitService.createDonneeExtrait(donneeExtraitDto); //TODO haw haw
                        System.out.println(donneeExtraits);
                        //ahne lezemna nda5ouha lel base
                        donneeExtraits.add(donneeExtrait);

                        System.out.println(donneeExtraits);
                        //!!!!


                        donneeExtrait=new DonneeExtrait();
                        //operationsArrayList=new ArrayList<Operation>();
                        liste_opertation = "" ;
                        System.out.println("liste_opertation="+liste_opertation);
                    }
                    ok_test_date_valeur_operation=true ;
                    //date_valeur_operation = dateOperation ;//02/11/2020
                    dateOperation_precedente = dateOperation ;
                    date_valeur_Operation_precedente = date_valeur_Operation;

                    debit_ou_credit = arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-1];

                    int position = arrayOfLignesContentPage[i].indexOf(debit_ou_credit);






                    System.out.println("debit_ou_credit="+debit_ou_credit);
                    System.out.println("debit_ou_credit="+debit_ou_credit);
                    //!!!!!!!!!!
                    int index_date_operation = getPositionOfDateOperation_CIC_BANK(arrayOfWordsPerLigne,date_prelevement_extrait);
                    if (index_date_operation!=-1){
                        // parametre yetbedlou fel arrayOfWordsPerLigne khater mech nafes display normalemeent
                         liste_opertation = liste_opertation+getOperationPerLigne(arrayOfWordsPerLigne,3,arrayOfWordsPerLigne.length-2)+"***";
                         System.out.println("liste_opertation="+liste_opertation);
                    }


                    //operationsArrayList.add(new Operation(getOperationPerLigne(arrayOfW0ordsPerLigne,3,arrayOfWordsPerLigne.length-2))) ;


                    System.out.println("liste_opertation="+liste_opertation);//break point

                    donneeExtrait.setDateDonneeExtrait(dateFormat.parse(dateOperation_precedente));
                    donneeExtrait.setDateValeurDonneeExtrait(dateFormat.parse(date_valeur_Operation_precedente));
                    // factures
                    donneeExtrait.setFactures(new ArrayList<>());
                    donneeExtrait.setCommentairesFactures(new HashMap<>());
                    donneeExtrait.setValide(false);

                    if (position!=-1){
                        if (position<positionMax){
                            donneeExtrait.setDebit(convertToDouble(debit_ou_credit));
                            donneeExtrait.setCredit(0.0);
                        }
                        if (position>positionMax){
                            donneeExtrait.setDebit(0.0);
                            donneeExtrait.setCredit(convertToDouble(debit_ou_credit));
                        }
                    };

                }
                if (chaine.contains("Information sur la protection des comptes :") || chaine.contains(("(GE) : protégé par la Garantie de l'Etat"))){
                    ok_test_fin_page=true;
                    System.out.println("ok_test_fin_page="+ok_test_fin_page);//break point
                }
                if (ok_test_date_valeur_operation && isValidDateCrediteur(dateOperation) == false && isValidDateCrediteur(date_valeur_Operation) == false && chaine.length()!=0 && ok_test_fin_page==false && ok_test_head_array==true){
                    if ((chaine.contains("Date Date valeur Opération Débit EUROS Crédit EUROS")==false) && (chaine.contains("SOLDE CREDITEUR AU")==false)&& (chaine.contains("Total des mouvements")==false) && (chaine.contains("IBAN")==false)){
                        liste_opertation = liste_opertation+getOperationPerLigne(arrayOfWordsPerLigne,1,arrayOfWordsPerLigne.length-1)+"***";
                        //operationsArrayList.add(new Operation(getOperationPerLigne(arrayOfWordsPerLigne,1,arrayOfWordsPerLigne.length-1)));
                        System.out.println("liste_opertation="+liste_opertation);//break point
                    }
                }
                //lezemni ntayah win toufa l page

                if (chaine.contains("Total des mouvements")){
                    ok_total_mouvement=true;
                    debit_total_mouvement=arrayOfWordsPerLigne[4];
                    credit_total_mouvement=arrayOfWordsPerLigne[5];
                    System.out.println("credit_total_mouvement="+credit_total_mouvement);
                    //					mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
//					donneeExtrait.setOperations(liste_opertation);
//					System.out.println(donneeExtrait);
//					donneeExtraits.add(donneeExtrait);

//					mmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
                    donneeExtrait.setOperations(liste_opertation);
                    System.out.println(donneeExtrait);
                    donneeExtraits.add(donneeExtrait);
//					mmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
//					System.out.println("ok_total_mouvement="+ok_total_mouvement);
//					System.out.println("debit_total_mouvement="+debit_total_mouvement);
//					System.out.println("credit_total_mouvement="+credit_total_mouvement);
                }
                //if (chaine.contains("IBAN")){ /// TODO pour indiquer que le dateExtrait est terminee et je vais l'ajouter a ma DB
                if (ok_solde_crediteur){ /// TODO pour indiquer que le dateExtrait est terminee et je vais l'ajouter a ma DB
                    //IBAN = getOperationPerLigne(arrayOfWordsPerLigne,3,arrayOfWordsPerLigne.length-1) ;
                    System.out.println("------------------------------------------------------------");
                    System.out.println("date_prelevement_extrait ="+date_prelevement_extrait);
                    System.out.println("premier_ligne_solde_crediteur_au_date ="+premier_ligne_solde_crediteur_au_date);
                    System.out.println("premier_ligne_credit_euro ="+premier_ligne_credit_euro);

                    System.out.println("debit_total_mouvement="+debit_total_mouvement);
                    System.out.println("credit_total_mouvement="+credit_total_mouvement);



                    System.out.println("dernier_ligne_solde_crediteur_au_date ="+dernier_ligne_solde_crediteur_au_date);
                    System.out.println("dernier_ligne_credit_euro ="+dernier_ligne_credit_euro);

                    System.out.println("IBAN =--------------------------"+IBAN);

                    // convert "30 november 2021" to date 30 / 09 /2021
                    DateTimeFormatter dateFormatFrench = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
                    System.out.println("date_prelevement_extrait =----------------------"+date_prelevement_extrait);
                    LocalDate localDate = LocalDate.parse(date_prelevement_extrait, dateFormatFrench);
                    Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    // insert the date object into the database here


                    extraitBancaire.setDateExtrait(date);



                    extraitBancaire.setDateDuSoldeCrediteurDebutMois(dateFormat.parse(premier_ligne_solde_crediteur_au_date));
                    extraitBancaire.setCreditDuSoldeCrediteurDebutMois(convertToDouble(premier_ligne_credit_euro));

                    //donneeExtraits.add(operation_fin_tableau) ;
                    extraitBancaire.setDonneeExtraits(donneeExtraits);
                    System.out.println("debit_total_mouvement = "+debit_total_mouvement);
                    System.out.println("credit_total_mouvement = "+credit_total_mouvement);

                    extraitBancaire.setTotalMouvementsDebit(convertToDouble(debit_total_mouvement));
                    extraitBancaire.setTotalMouvementsCredit(convertToDouble(credit_total_mouvement));






                    extraitBancaire.setDateDuSoldeCrediteurFinMois(dateFormat.parse(dernier_ligne_solde_crediteur_au_date));

                    extraitBancaire.setCreditDuSoldeCrediteurFinMois(convertToDouble(dernier_ligne_credit_euro));
                    System.out.println(extraitBancaire);
                    //extraitBancaireDto = this.extraitBancaireService.createExtraitBancaire(extraitBancaireDto) ; //TODO haw haw
                    extraits.add(extraitBancaire);
                    System.out.println(extraits);//break point
                    extraitBancaire = new ExtraitBancaire() ;
                    donneeExtraits=new ArrayList<>();
                    ok_solde_crediteur=false ;
                    //releveBancaire.setIban(IBAN);
                }
            }
        }
        releveBancaireDto.setIban(IBAN);
        releveBancaireDto.setExtraits(extraits);
        pdfDocument.close();
        fis.close();

    }

    private String isValidIBAN(String operationPerLigne) {
        int index = operationPerLigne.lastIndexOf("FR");
        if (index==-1){
            return "" ;
        }
        String str = operationPerLigne.substring(index,operationPerLigne.length());
        return str ;
    }

    private static String getOperationPerLigneSansEspace(String[] arrayOfWordsPerLigne, int debut, int fin) {
        String ch = "" ;
        if (arrayOfWordsPerLigne.length!=0){
            for (int j = debut; j <= fin; j++) {
                ch = ch + arrayOfWordsPerLigne[j]; ///BreakPoint
            }
            System.out.println("ch="+ch+"***");
            if (ch.charAt(ch.length()-1) == ' '){///BreakPoint
                ch = ch.substring(0,ch.length()-1) ;
            }
            System.out.println("rs="+ch+"***");///BreakPoint
        }
        return ch ;
    }

    private static String getOperationPerLigne(String[] arrayOfWordsPerLigne, int debut, int fin) {
        String ch = "" ;
        if (arrayOfWordsPerLigne.length!=0){
            for (int j = debut; j <= fin; j++) {
                ch = ch + arrayOfWordsPerLigne[j]+' ' ; ///BreakPoint
            }
            System.out.println("ch="+ch+"***");
            if (ch.charAt(ch.length()-1) == ' '){///BreakPoint
                ch = ch.substring(0,ch.length()-1) ;
            }
            System.out.println("rs="+ch+"***");///BreakPoint
        }
        return ch ;
    }
    private static boolean isValidSoldeCrediteur(String s) {
        boolean v = true ;
        if ((s!=null) && (s.length()!=0)){
            //348.107,92
            for(int i=0;i<s.length();i++){
                v = ( s.charAt(i)>='0' && s.charAt(i)<='9' ) || s.charAt(i) == '.' || s.charAt(i) == ',' ;
                if (v == false){
                    return false ;
                }
            }
        }
        return v ;
    }
    private static boolean isValidDateCrediteur(String s) {
        boolean v = false ;
        if ((s!=null) && (s.length()==10)){
            String day_string = s.substring(0,1);
            String month_string = s.substring(3,4);
            String year_string = s.substring(6,9) ;
            v = (isNumeric(day_string) && isNumeric(month_string)) &&isNumeric(year_string)  ;
        }
        return v ;
    }
    private static boolean isNumeric(String str){
        return str != null && str.matches("[0-9.]+");
    }

    private static boolean isValidFormatOfDateReleveBancaire(String chaine) {
        boolean v = false ;//				jj/mm/aaaaa a nee pas oublier l slach / !!!!!
        if (chaine.length()>=11){
            String day_string = chaine.substring(0,1);
            String month_string = chaine.substring(3,chaine.length()-5);
            String year_string = chaine.substring(chaine.length()-5+2,chaine.length()-1) ;
            v = (isNumeric(day_string) && isValidMonth(month_string)) &&isNumeric(year_string)  ;
        }
        return v ;
    }

    private static boolean isValidMonth(String month_string) {
        if (month_string.equals("janvier")) {
            return true ;
        }
        if (month_string.equals("février")) {
            return true ;
        }
        if (month_string.equals("mars")) {
            return true ;
        }
        if (month_string.equals("avril")) {
            return true ;
        }
        if (month_string.equals("mai")) {
            return true ;
        }
        if (month_string.equals("juin")) {
            return true ;
        }
        if (month_string.equals("juillet")) {
            return true ;
        }
        if (month_string.equals("août")) {
            return true ;
        }
        if (month_string.equals("septembre")) {
            return true ;
        }
        if (month_string.equals("octobre")) {
            return true ;
        }
        if (month_string.equals("novembre")) {
            return true ;
        }
        if (month_string.equals("décembre")) {
            return true ;
        }
        return false ;
    }

    public static void extractDataFromCicBank (Util util,String pathFileUploaded)throws IOException {
        //String path = "C:\\Users\\mehdi\\Desktop\\mehdi_test_extract_pfe\\TestExtraction\\Ext1.pdf";
        File file = new File(pathFileUploaded) ;
        FileInputStream fis = new FileInputStream(file) ;
        PDDocument pdfDocument = PDDocument.load(fis);
        Splitter splitter = new Splitter();
        List<PDDocument> splitpages = splitter.split(pdfDocument);
        // pdfTextStripper = pdfReader
        PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper() ;
        pdfTextStripper.setSortByPosition(true);
        pdfTextStripper.setAddMoreFormatting(true);




        String nameBank = "CIC" ;
        ///ok c'est l 'indicateur pour extraire nom societe / adresse / nom du PDG
        boolean ok = false ;int nombreLigneNonVide = 0 ;
        String nomSociete = "" ;
        String dateOperation = "" ;
        String date_valeur_Operation = "" ;

        // Expression régulière pour trouver les espaces à partir de la fin de la chaîne jusqu'au premier caractère non-espace
        Pattern pattern = Pattern.compile("\\s+(?!.*\\s)");
        int maxEspaces = 0; // Variable pour stocker le maximum nombre d'espaces trouvés
        String chaine_max_espace = "" ;
        for (PDDocument page : splitpages) {
//			System.out.println("..................................................");
//			System.out.println(page);
//			System.out.println("..................................................");
            String contentPage = pdfTextStripper.getText(page); //Le contenu de la page i (1,2,3,4,5...)
            String[] arrayOfLignesContentPage = contentPage.split("\n"); // decouppage de content page ligne par ligne  par le delimiteur l /n
            for (int i = 0; i < arrayOfLignesContentPage.length; i++) {

                /// hethi normalement eli bech na3diha lel matcher !!!
                System.out.println(arrayOfLignesContentPage[i]) ;//BREAK POINT
                String[] arrayOfWordsPerLigne = arrayOfLignesContentPage[i].split("\\s+");///men e5er l page jusqu"a l awel caractere trouveè
                System.out.println(arrayOfWordsPerLigne) ;//BREAK POINT
                String chaine = convertArrayToString(arrayOfWordsPerLigne) ;
                if (arrayOfWordsPerLigne.length>=3){
                    dateOperation = arrayOfWordsPerLigne[1] ;
                    date_valeur_Operation = arrayOfWordsPerLigne[2] ;
                }else {
                    dateOperation = "" ;
                    date_valeur_Operation = "" ;
                }
                // je peux faire la somme en meme temps
                if (isValidDateCrediteur(dateOperation) && isValidDateCrediteur(date_valeur_Operation) ){
                    System.out.println("---------------------------------------"+arrayOfLignesContentPage[i].length());
                    //lezemni n3adi chaine eli meme exemple que que sublime texte e!!!
                    Matcher matcher = pattern.matcher(arrayOfLignesContentPage[i]);
                    // Si des espaces ont été trouvés
                    if (matcher.find()) {
                        int count = matcher.group().length();
                        // Mettre à jour le maximum nombre d'espaces trouvés
                        if (count > maxEspaces) {
                            maxEspaces = count;
                            chaine_max_espace = arrayOfLignesContentPage[i] ;
                        }
                    }
                }

                /***********************DEBUT NOM DE LA SOCIETE***************************/
                if ((chaine.contains("CIC") || chaine.contains(("CREDIT INDUSTRIEL ET COMMERCIAL")) ) && (ok==false)){//BREAK POINT
                    ok = true ;
                }
                if (ok){
                    // il faut compter les nombres de ligne remplie par des caracteres !
                    if (arrayOfWordsPerLigne.length!=0){
                        nombreLigneNonVide = nombreLigneNonVide +1 ;
                    }
                    if ((nombreLigneNonVide == 6 ) && (nomSociete.equals(""))){
                        nomSociete = chaine ;
                        System.out.println("nomSociete="+nomSociete);
                        //return nomSociete ;
                    }
                }
                /***********************FIN NOM DE LA SOCIETE***************************/





            }
        }
        System.out.println("Le maximum nombre d'espaces trouvés de la fin de page jusqu'a premier caratere est !  est : " + maxEspaces);

        pdfDocument.close();
        fis.close();
        util.setNameSociete(nomSociete);
        util.setMaxEspaces(maxEspaces);
        util.setChaineMaxEspace(chaine_max_espace);

    }

    public static String findNameBank (String pathFileUploaded)throws IOException {
        //String path = "C:\\Users\\mehdi\\Desktop\\mehdi_test_extract_pfe\\TestExtraction\\Ext1.pdf";
        File file = new File(pathFileUploaded) ;
        FileInputStream fis = new FileInputStream(file) ;
        PDDocument pdfDocument = PDDocument.load(fis);
        Splitter splitter = new Splitter();
        List<PDDocument> splitpages = splitter.split(pdfDocument);
        // pdfTextStripper = pdfReader
        PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper() ;
        pdfTextStripper.setSortByPosition(true);
        pdfTextStripper.setAddMoreFormatting(true);
        String nameBank = "" ;
        for (PDDocument page : splitpages) {
//			System.out.println("..................................................");
//			System.out.println(page);
//			System.out.println("..................................................");
            String contentPage = pdfTextStripper.getText(page); //Le contenu de la page i (1,2,3,4,5...)
            String[] arrayOfLignesContentPage = contentPage.split("\n"); // decouppage de content page ligne par ligne  par le delimiteur l /n
            for (int i = 0; i < arrayOfLignesContentPage.length; i++) {

                System.out.println(arrayOfLignesContentPage[i]) ;//BREAK POINT
                // split = diviser
                //.split("\\s+") = split par espace ou plusieurs espaces
                String[] arrayOfWordsPerLigne = arrayOfLignesContentPage[i].split("\\s+");
                System.out.println(arrayOfWordsPerLigne) ;//BREAK POINT
                String chaine = convertArrayToString(arrayOfWordsPerLigne) ;
                if (chaine.contains("CIC")){//BREAK POINT
                    pdfDocument.close();
                    fis.close();
                    nameBank = "CIC PARIS KLEBER" ;//BREAK POINT
                    return nameBank ;// Nraj3ou l nom kemel emte3 l agence par exemple "CIC PARIS KLEBER"
                    //System.out.println("BANK ---CIC PARIS KLEBER--- BANK");
                    //System.out.println("nameBank"+nameBank);
                }
                if (chaine.contains("banquepopulaire.fr")){//BREAK POINT
                    pdfDocument.close();
                    fis.close();
                    nameBank = "BP" ;//BREAK POINT
                    return nameBank ;//BREAK POINT
                    //System.out.println("BANK ---CIC PARIS KLEBER--- BANK");
                    //System.out.println("nameBank"+nameBank);
                }
            }
        }
        pdfDocument.close();
        fis.close();
        return nameBank ;

    }

    private static String getPathFileUploaded(MultipartFile file) throws IOException {
        MultipartFile file1 = file ;// Your MultipartFile object
        String uploadDir = "C:\\Users\\mehdi\\Desktop\\mehdi_test_extract_pfe\\TestExtraction\\src\\main\\resources\\Uploaded_PDF";

        // Create a temporary file
        File tempFile = File.createTempFile("temp-", "-" + file1.getOriginalFilename());
        String filePath = tempFile.getAbsolutePath();

        // Write the contents of the MultipartFile to the temporary file
        file1.transferTo(tempFile);
        // Move the temporary file to the upload directory
        Path target = Paths.get(uploadDir, file1.getOriginalFilename());
        Files.move(tempFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

        // Get the path of the uploaded file
        String uploadedFilePath = target.toAbsolutePath().toString();

        // Print the uploaded file path
        System.out.println("Uploaded file path: " + uploadedFilePath);
        tempFile.delete();
        return uploadedFilePath ;
    }

    public static String convertArrayToString(String[] arrayOfWordsPerLigne) {
        String ch = "" ;
        if (arrayOfWordsPerLigne.length!=0){
            for (int j = 0; j < arrayOfWordsPerLigne.length; j++) {
                boolean ok = arrayOfWordsPerLigne[j].equals("") ;
                if (ok==false){
                    ch = ch + arrayOfWordsPerLigne[j]+' ' ; ///BreakPoint
                }
            }
            System.out.println("ch="+ch+"***");
            if (ch.charAt(ch.length()-1) == ' '){///BreakPoint
                ch = ch.substring(0,ch.length()-1) ;
            }
            System.out.println("rs="+ch+"***");///BreakPoint
        }
        return ch ;
    }
    public static double convertToDouble(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Input value cannot be null or empty");
        }
        // Replace comma with dot and remove any additional dots
        value = value.replace(",", ".").replaceAll("\\.(?=.*\\.)", "");

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input format: " + value, e);
        }
    }

    public static void extractDataFromPopBank (Util util,String pathFileUploaded)throws IOException {
        //String path = "C:\\Users\\mehdi\\Desktop\\mehdi_test_extract_pfe\\TestExtraction\\Ext1.pdf";
        File file = new File(pathFileUploaded) ;
        FileInputStream fis = new FileInputStream(file) ;
        PDDocument pdfDocument = PDDocument.load(fis);
        Splitter splitter = new Splitter();
        List<PDDocument> splitpages = splitter.split(pdfDocument);
        // pdfTextStripper = pdfReader
        PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper() ;
        pdfTextStripper.setSortByPosition(true);
        pdfTextStripper.setAddMoreFormatting(true);




        String nameBank = "banquepopulaire.fr" ;
        ///ok c'est l 'indicateur pour extraire nom societe / adresse / nom du PDG
        boolean ok = false ;int nombreLigneNonVide = 0 ;
        String nomSociete = "" ;
        String dateOperation = "" ;
        String date_valeur_Operation = "" ;
        String anneeOperation = "" ;
        // Expression régulière pour trouver les espaces à partir de la fin de la chaîne jusqu'au premier caractère non-espace
        Pattern pattern = Pattern.compile("\\s+(?!.*\\s)");
        int maxEspaces = 0; // Variable pour stocker le maximum nombre d'espaces trouvés
        String chaine_max_espace = "" ;
        for (PDDocument page : splitpages) {
//			System.out.println("..................................................");
//			System.out.println(page);
//			System.out.println("..................................................");
            String contentPage = pdfTextStripper.getText(page); //Le contenu de la page i (1,2,3,4,5...)
            String[] arrayOfLignesContentPage = contentPage.split("\n"); // decouppage de content page ligne par ligne  par le delimiteur l /n
            for (int i = 0; i < arrayOfLignesContentPage.length; i++) {

                /// hethi normalement eli bech na3diha lel matcher !!!
                System.out.println(arrayOfLignesContentPage[i]) ;//BREAK POINT
                String[] arrayOfWordsPerLigne = arrayOfLignesContentPage[i].split("\\s+");///men e5er l page jusqu"a l awel caractere trouveè
                System.out.println(arrayOfWordsPerLigne) ;//BREAK POINT
                String chaine = convertArrayToString(arrayOfWordsPerLigne) ;
                if (arrayOfWordsPerLigne.length>=8){
                    dateOperation = arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-3]+'/'+anneeOperation ;
                    date_valeur_Operation = arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-2]+'/'+anneeOperation  ;
                }else {
                    dateOperation = "" ;
                    date_valeur_Operation = "" ;
                }
                // je peux faire la somme en meme temps
                if (isValidDateCrediteur(dateOperation) && isValidDateCrediteur(date_valeur_Operation) ){
                    System.out.println("---------------------------------------"+arrayOfLignesContentPage[i].length());
                    //lezemni n3adi chaine eli meme exemple que que sublime texte e!!!
                    Matcher matcher = pattern.matcher(arrayOfLignesContentPage[i]);
                    // Si des espaces ont été trouvés
                    if (matcher.find()) {
                        int count = matcher.group().length();
                        // Mettre à jour le maximum nombre d'espaces trouvés
                        if (count > maxEspaces) {
                            maxEspaces = count;
                            chaine_max_espace = arrayOfLignesContentPage[i] ;
                        }
                    }
                }

                /***********************DEBUT NOM DE LA SOCIETE***************************/
                if ((chaine.contains(nameBank)) && (ok==false)){//BREAK POINT
                    ok = true ;
                }
                if (ok){
                    // il faut compter les nombres de ligne remplie par des caracteres !
                    if (arrayOfWordsPerLigne.length!=0){
                        nombreLigneNonVide = nombreLigneNonVide +1 ;
                    }
                    if ((nombreLigneNonVide == 4 ) && (nomSociete.equals(""))){
                        nomSociete = getOperationPerLigne(arrayOfWordsPerLigne,3,arrayOfWordsPerLigne.length-1); ;
                        System.out.println("nomSociete="+nomSociete);
                        //return nomSociete ;
                    }
                }
                /***********************FIN NOM DE LA SOCIETE***************************/
                if ((chaine.contains("RELEVE")) && anneeOperation.equals("")){//BREAK POINT
                    anneeOperation= arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-1].substring(6,10);
                    System.out.println("anneeOperation="+anneeOperation);
                }




            }
        }
        System.out.println("Le maximum nombre d'espaces trouvés de la fin de page jusqu'a premier caratere est !  est : " + maxEspaces);

        pdfDocument.close();
        fis.close();
        util.setNameSociete(nomSociete);
        util.setMaxEspaces(maxEspaces);
        util.setChaineMaxEspace(chaine_max_espace);

    }

    public void  extractListeOperationFromPopBank(ReleveBancaireDto releveBancaireDto, Util util, String pathFileUploaded) throws IOException, ParseException {
        int positionMax = util.getChaineMaxEspace().length() - util.getMaxEspaces() - 1 ;
        //String path = "C:\\Users\\mehdi\\Desktop\\mehdi_test_extract_pfe\\TestExtraction\\Ext1.pdf";
        File file = new File(pathFileUploaded) ;
        FileInputStream fis = new FileInputStream(file) ;
        PDDocument pdfDocument = PDDocument.load(fis);
        Splitter splitter = new Splitter();
        List<PDDocument> splitpages = splitter.split(pdfDocument);
        // pdfTextStripper = pdfReader
        PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper() ;
        pdfTextStripper.setSortByPosition(true);
        pdfTextStripper.setAddMoreFormatting(true);




        ///ok c'est l 'indicateur pour extraire nom societe / adresse / nom du PDG
        boolean ok = false ;int nombreLigneNonVide = 0 ;


        boolean ok_test_Releve_bancaire = false;
        boolean ok_test_format_releve_bancaire = false;
        boolean ok_test_head_array = false;
        boolean ok_test_debut_operation_array = false;
        String date_prelevement_extrait ="" ;
        String premier_ligne_solde_crediteur_au_date = "";
        String premier_ligne_credit_euro = "";
        String dernier_ligne_solde_crediteur_au_date = "";
        String dernier_ligne_credit_euro = "";
        boolean ok_test_date_valeur_operation = false;
        String date_valeur_operation = "";
        String debit_ou_credit = "" ;
        String liste_opertation="" ;
        String dateOperation = "";
        String date_valeur_Operation="";
        boolean ok_test_fin_page;
        boolean ok_total_mouvement = false;
        String debit_total_mouvement="";
        String credit_total_mouvement="";
        String IBAN = "" ;
        String dateOperation_precedente ="";
        String date_valeur_Operation_precedente ="";
        boolean ok_solde_crediteur = false ;



        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DonneeExtrait donneeExtrait = new DonneeExtrait() ;
        ExtraitBancaire extraitBancaire = new ExtraitBancaire();



//		ArrayList<Operation>operationsArrayList = new ArrayList<>();
        List<ExtraitBancaire> extraits = new ArrayList<>();
        List<DonneeExtrait> donneeExtraits=new ArrayList<>();

        String chaine_date_prelevement_extrait ="";


        for (PDDocument page : splitpages) {
//			System.out.println("..................................................");
//			System.out.println(page);
//			System.out.println("..................................................");
            String contentPage = pdfTextStripper.getText(page); //Le contenu de la page i (1,2,3,4,5...)
            String[] arrayOfLignesContentPage = contentPage.split("\n"); // decouppage de content page ligne par ligne  par le delimiteur l /n
            ok_test_fin_page = false;
            ok_test_head_array = false;
            for (int i = 0; i < arrayOfLignesContentPage.length; i++) {
                System.out.println(arrayOfLignesContentPage[i]) ;//BREAK POINT
                // split = diviser
                //.split("\\s+") = split par espace ou plusieurs espaces
                String[] arrayOfWordsPerLigne = arrayOfLignesContentPage[i].split("\\s+");
                System.out.println(arrayOfWordsPerLigne) ;//BREAK POINT
                String chaine = convertArrayToString(arrayOfWordsPerLigne) ;
                System.out.println(chaine); //break point ici
                /***********************DEBUT DATA DE LA SOCIETE***************************/
                //if (chaine.contains("RELEVE ET INFORMATIONS BANCAIRES")){
                if (chaine.contains("RELEVE N°")){
                    ok_test_Releve_bancaire = true ;
                    System.out.println("ok_test_Releve_bancaire="+ok_test_Releve_bancaire);
                }
                if (ok_test_Releve_bancaire){
                    System.out.println("arrayOfWordsPerLigne="+arrayOfWordsPerLigne);
                    if (arrayOfWordsPerLigne.length-1>=0){
                        chaine_date_prelevement_extrait=arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-1];
                        System.out.println(chaine);
                    }

                    if (isValidDateCrediteur(chaine_date_prelevement_extrait) && (date_prelevement_extrait.equals(""))){
                        ok_test_format_releve_bancaire = true ;
                        date_prelevement_extrait = chaine_date_prelevement_extrait;
                        System.out.println("ok_test_format_releve_bancaire="+ok_test_format_releve_bancaire);
                        System.out.println("date_prelevement_extrait="+date_prelevement_extrait);
                    }
                }
                if (chaine.contains("COMPTA LIBELLE/REFERENCE OPERATION VALEUR EUROS")){
                    ok_test_head_array = true ;
                    System.out.println("ok_test_head_array="+ok_test_head_array);
                }
                if (chaine.contains("SOLDE CREDITEUR AU")){
                    System.out.println("arrayOfWordsPerLigne ="+arrayOfWordsPerLigne) ;
                    System.out.println("chaine ="+chaine);
                }
                ///hethi fi awel tableau
                if (ok_total_mouvement==false){
                    String str_solde_crediteur_concatiner = getOperationPerLigneSansEspace(arrayOfWordsPerLigne,arrayOfWordsPerLigne.length-2,arrayOfWordsPerLigne.length-1) ;
                    if (chaine.contains("SOLDE CREDITEUR AU") && isValidDateCrediteur(arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-3]) &&  isValidSoldeCrediteur(str_solde_crediteur_concatiner)){
                        ok_test_debut_operation_array = true ;
                        premier_ligne_solde_crediteur_au_date =  arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-3] ;
                        premier_ligne_credit_euro = str_solde_crediteur_concatiner;
                        System.out.println("ok_test_debut_operation_array="+ok_test_debut_operation_array);
                        System.out.println("premier_ligne_solde_crediteur_au_date="+premier_ligne_solde_crediteur_au_date);
                        System.out.println("premier_ligne_credit_euro="+premier_ligne_credit_euro);
                        //ok_total_mouvement=!ok_total_mouvement;
                    }
                }
                ///hethi fi e5er tableau
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!ppppppppppppp
                String str_solde_crediteur_concatiner = "" ;
                String str_date_crediteur_concatiner ="" ;
                if (ok_total_mouvement==true){     ///TODO  hneeee
                     if (arrayOfWordsPerLigne.length>=6){
                         str_solde_crediteur_concatiner = getOperationPerLigneSansEspace(arrayOfWordsPerLigne,arrayOfWordsPerLigne.length-2,arrayOfWordsPerLigne.length-1) ;
                         str_date_crediteur_concatiner = arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-3] ;
                     }
                    if (str_date_crediteur_concatiner.indexOf("*")!=-1){
                        str_date_crediteur_concatiner=str_date_crediteur_concatiner.replace("*","");
                    }
                    System.out.println("str_date_crediteur_concatiner="+str_date_crediteur_concatiner);
                    System.out.println("str_date_crediteur_concatiner="+str_date_crediteur_concatiner);

                    if (chaine.contains("SOLDE CREDITEUR AU") && isValidDateCrediteur(str_date_crediteur_concatiner) &&  isValidSoldeCrediteur(str_solde_crediteur_concatiner)){
                        //hethouma tawa
                        ok_solde_crediteur = true ;
                        ok_test_fin_page=true ;
                        liste_opertation = "" ;
                        //end of
                        ok_test_debut_operation_array = false ;
                        dernier_ligne_solde_crediteur_au_date = str_date_crediteur_concatiner;
                        dernier_ligne_credit_euro = str_solde_crediteur_concatiner ;
                        System.out.println("ok_test_debut_operation_array="+ok_test_debut_operation_array);
                        System.out.println("dernier_ligne_solde_crediteur_au_date="+dernier_ligne_solde_crediteur_au_date);
                        System.out.println("dernier_ligne_credit_euro="+dernier_ligne_credit_euro);
                        ok_total_mouvement=false ;
                    }
                }

                if (arrayOfWordsPerLigne.length>=5){
                    int index_date_operation = getPositionOfDateOperation(arrayOfWordsPerLigne,date_prelevement_extrait);
                    if (index_date_operation!=-1){
                        dateOperation = arrayOfWordsPerLigne[index_date_operation]+'/'+date_prelevement_extrait.substring(6,10);
                        date_valeur_Operation = arrayOfWordsPerLigne[index_date_operation+1] + '/'+date_prelevement_extrait.substring(6,10);
                    }else{
                        dateOperation = arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-3]+'/'+date_prelevement_extrait.substring(6,10);
                        date_valeur_Operation = arrayOfWordsPerLigne[arrayOfWordsPerLigne.length-2] + '/'+date_prelevement_extrait.substring(6,10);
                    }
                    System.out.println("\ndateOperation="+dateOperation);
                    System.out.println("\ndate_valeur_Operation="+date_valeur_Operation);
                    System.out.println("");
                }else {
                    dateOperation = "" ;
                    date_valeur_Operation = "" ;
                }
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                if (chaine.contains("IBAN") && IBAN.equals("")){
                    int begin_index = chaine.indexOf(" ") ;
                    int end_index = chaine.indexOf("BIC") ;

                    IBAN = chaine.substring(begin_index+1,end_index-1);
                    //hethouma tawa
                    //IBAN = chaine;
                    //IBAN = isValidIBAN(getOperationPerLigne(arrayOfWordsPerLigne,3,arrayOfWordsPerLigne.length-1)) ;

                    System.out.println("IBAN.........="+IBAN+"=");
                    // TODO IBAN = getOperationPerLigne(arrayOfWordsPerLigne,3,arrayOfWordsPerLigne.length-1) ;
                    //end of
//                    ok_test_fin_page=true ;
//                    liste_opertation = "" ;
                }
                if (isValidDateCrediteur(dateOperation) && isValidDateCrediteur(date_valeur_Operation)  && ok_test_fin_page==false && ok_test_head_array==true){
                    if (liste_opertation.equals("") == false){
                        System.out.println("\n............................................................\n");
                        System.out.println("dateOperation_precedente="+dateOperation_precedente);
                        System.out.println("date_valeur_Operation_precedente="+date_valeur_Operation_precedente);
                        System.out.println("liste_opertation="+liste_opertation);
                        System.out.println("\n............................................................\n");

                        donneeExtrait.setOperations(liste_opertation);
                        //donneeExtraitDto = this.donneeExtraitService.createDonneeExtrait(donneeExtraitDto); //TODO haw haw
                        System.out.println(donneeExtraits);
                        //ahne lezemna nda5ouha lel base
                        donneeExtraits.add(donneeExtrait);

                        System.out.println(donneeExtraits);
                        //!!!!


                        donneeExtrait=new DonneeExtrait();
                        //operationsArrayList=new ArrayList<Operation>();
                        liste_opertation = "" ;
                        System.out.println("liste_opertation="+liste_opertation);
                    }
                    ok_test_date_valeur_operation=true ;
                    //date_valeur_operation = dateOperation ;//02/11/2020
                    dateOperation_precedente = dateOperation ;
                    date_valeur_Operation_precedente = date_valeur_Operation;



                    int position_date_operation = getPositionOfDateOperation(arrayOfWordsPerLigne,date_prelevement_extrait);
                    System.out.println(position_date_operation);

                    if (position_date_operation !=-1){
                        debit_ou_credit=getOperationPerLigneSansEspace(arrayOfWordsPerLigne,position_date_operation+2,arrayOfWordsPerLigne.length-1) ;
                    }




                    System.out.println("debit_ou_credit="+debit_ou_credit);
                    System.out.println("debit_ou_credit="+debit_ou_credit);



                    int index_date_operation = getPositionOfDateOperation(arrayOfWordsPerLigne,date_prelevement_extrait);
                    System.out.println("index_date_operation="+index_date_operation);
                    if (index_date_operation!=-1){
                        liste_opertation = liste_opertation+getOperationPerLigne(arrayOfWordsPerLigne,2,index_date_operation-1)+"***";
                    }


                    //operationsArrayList.add(new Operation(getOperationPerLigne(arrayOfW0ordsPerLigne,3,arrayOfWordsPerLigne.length-2))) ;



                    System.out.println("liste_opertation="+liste_opertation);//break point






                    System.out.println("\narrayOfWordsPerLigne="+arrayOfWordsPerLigne);
                    System.out.println("\nchaine"+convertArrayToString(arrayOfWordsPerLigne)) ;//BREAK POINT
                    // cette  fonction removeSpacesBetweenDigits permet de supprimer les espaces entre deux chiffres SI IL Y A entre [1..3] ESPACES c'est le cas de valeur debit et credit (entre 1 et 3 pour le cas de l'objet 10 : debit euro = 25 200,00) et dans la date 15/11
                    String output = removeSpacesBetweenDigits(arrayOfLignesContentPage[i]);
                    int position = output.indexOf(debit_ou_credit);
                    //int position = indexOfSubstring3(output,debit_ou_credit) ;

                    System.out.println("debit_ou_credit="+debit_ou_credit);
                    System.out.println("output="+output);
                    System.out.println("position="+position);
                    System.out.println("---------------------------************************************************");
                    donneeExtrait.setDateDonneeExtrait(dateFormat.parse(dateOperation_precedente));
                    donneeExtrait.setDateValeurDonneeExtrait(dateFormat.parse(date_valeur_Operation_precedente));
                    if (position!=-1){
                        if (position<positionMax){
                            donneeExtrait.setDebit(convertToDouble(debit_ou_credit));
                            donneeExtrait.setCredit(0.0);
                        }
                        if (position>positionMax){
                            donneeExtrait.setDebit(0.0);
                            donneeExtrait.setCredit(convertToDouble(debit_ou_credit));
                        }
                    };

                }
                if (chaine.contains("A la Banque Populaire Rives de Paris, l'assurance des personnes et des biens c'est aussi notre métier.") ){
                    ok_test_fin_page=true;
                    System.out.println("ok_test_fin_page="+ok_test_fin_page);//break point
                }
                if (ok_test_date_valeur_operation && isValidDateCrediteur(dateOperation) == false && isValidDateCrediteur(date_valeur_Operation) == false && chaine.length()!=0 && ok_test_fin_page==false && ok_test_head_array==true){
                    if ((chaine.contains("COMPTA LIBELLE/REFERENCE OPERATION VALEUR EUROS")==false) && (chaine.contains("SOLDE CREDITEUR AU")==false)&& (chaine.contains("TOTAL DES MOUVEMENTS")==false) && (chaine.contains("IBAN")==false)){
                        liste_opertation = liste_opertation+getOperationPerLigne(arrayOfWordsPerLigne,1,arrayOfWordsPerLigne.length-1)+"***";
                        //operationsArrayList.add(new Operation(getOperationPerLigne(arrayOfWordsPerLigne,1,arrayOfWordsPerLigne.length-1)));
                        System.out.println("liste_opertation="+liste_opertation);//break point
                    }
                }
                //lezemni ntayah win toufa l page

                if (chaine.contains("TOTAL DES MOUVEMENTS")){
                    ok_total_mouvement=true;

                    debit_total_mouvement = "0";
                    credit_total_mouvement= "0";
                    if (arrayOfWordsPerLigne.length==6){
                        debit_total_mouvement=arrayOfWordsPerLigne[4]; // TODO yebda l montant diviser sur 2
                        credit_total_mouvement=arrayOfWordsPerLigne[5];
                    }
                    if (arrayOfWordsPerLigne.length==8){
                        debit_total_mouvement=arrayOfWordsPerLigne[4]+arrayOfWordsPerLigne[5]; // TODO yebda l montant diviser sur 2
                        credit_total_mouvement=arrayOfWordsPerLigne[6]+arrayOfWordsPerLigne[7];
                    }


                    System.out.println("credit_total_mouvement="+credit_total_mouvement);
                    System.out.println("debit_total_mouvement="+debit_total_mouvement);
                    System.out.println("debit_total_mouvement="+debit_total_mouvement);


                    donneeExtrait.setOperations(liste_opertation);
                    System.out.println(donneeExtrait);
                    donneeExtraits.add(donneeExtrait);



                }
                //if (chaine.contains("IBAN")){ /// TODO pour indiquer que le dateExtrait est terminee et je vais l'ajouter a ma DB
                if (ok_solde_crediteur){ /// TODO pour indiquer que le dateExtrait est terminee et je vais l'ajouter a ma DB
                    //IBAN = getOperationPerLigne(arrayOfWordsPerLigne,3,arrayOfWordsPerLigne.length-1) ;
                    System.out.println("------------------------------------------------------------");
                    System.out.println("date_prelevement_extrait ="+date_prelevement_extrait);
                    System.out.println("premier_ligne_solde_crediteur_au_date ="+premier_ligne_solde_crediteur_au_date);
                    System.out.println("premier_ligne_credit_euro ="+premier_ligne_credit_euro);

                    System.out.println("debit_total_mouvement="+debit_total_mouvement);
                    System.out.println("credit_total_mouvement="+credit_total_mouvement);



                    System.out.println("dernier_ligne_solde_crediteur_au_date ="+dernier_ligne_solde_crediteur_au_date);
                    System.out.println("dernier_ligne_credit_euro ="+dernier_ligne_credit_euro);

                    System.out.println("IBAN =--------------------------"+IBAN);

                    // insert the date object into the database here


                    extraitBancaire.setDateExtrait(dateFormat.parse(date_prelevement_extrait));

                    extraitBancaire.setDateDuSoldeCrediteurDebutMois(dateFormat.parse(premier_ligne_solde_crediteur_au_date));
                    extraitBancaire.setCreditDuSoldeCrediteurDebutMois(convertToDouble(premier_ligne_credit_euro));

                    extraitBancaire.setDonneeExtraits(donneeExtraits);
                    System.out.println("debit_total_mouvement = "+debit_total_mouvement);
                    System.out.println("credit_total_mouvement = "+credit_total_mouvement);

                    extraitBancaire.setTotalMouvementsDebit(convertToDouble(debit_total_mouvement));
                    extraitBancaire.setTotalMouvementsCredit(convertToDouble(credit_total_mouvement));








                    extraitBancaire.setDateDuSoldeCrediteurFinMois(dateFormat.parse(dernier_ligne_solde_crediteur_au_date));

                    extraitBancaire.setCreditDuSoldeCrediteurFinMois(convertToDouble(dernier_ligne_credit_euro));
                    System.out.println("extrait_BancaireDto");
                    System.out.println(extraitBancaire);
                    //extraitBancaireDto = this.extraitBancaireService.createExtraitBancaire(extraitBancaireDto) ; //TODO haw haw
                    extraits.add(extraitBancaire);
                    System.out.println(extraits);//break point

                    extraitBancaire = new ExtraitBancaire() ;
                    donneeExtraits=new ArrayList<>();
                    ok_solde_crediteur=false ;
                }
            }
        }
        releveBancaireDto.setIban(IBAN);
        releveBancaireDto.setExtraits(extraits);
        pdfDocument.close();
        fis.close();

    }
    int getPositionOfDateOperation(String[] arrayOfWordsPerLigne,String date_prelevement_extrait){
        System.out.println("date_prelevement_extrait.substring(6,9)="+date_prelevement_extrait.substring(6,10));
        System.out.println("//////");
        for (int i=2;i<=arrayOfWordsPerLigne.length-2;i++){
            String str1 = arrayOfWordsPerLigne[i] ;
            String str2 = arrayOfWordsPerLigne[i+1];
            if ((date_prelevement_extrait.substring(6,10).contains(arrayOfWordsPerLigne[i]) ==false) && (date_prelevement_extrait.substring(6,10).contains(arrayOfWordsPerLigne[i+1])==false) ){
                str1 = str1 + '/' +date_prelevement_extrait.substring(6,10) ;
                str2 = str2 + '/' +date_prelevement_extrait.substring(6,10) ;
            }
            boolean ok =  isValidDateCrediteur(str1) &&   isValidDateCrediteur(str2) ;
            if (ok){
                return i;
            }
        }
        return -1 ;
    }

    int getPositionOfDateOperation_CIC_BANK(String[] arrayOfWordsPerLigne,String date_prelevement_extrait){
        System.out.println("date_prelevement_extrait.substring(6,9)="+date_prelevement_extrait.substring(6,10));
        System.out.println("//////");
        for (int i=1;i<=arrayOfWordsPerLigne.length-2;i++){
            String str1 = arrayOfWordsPerLigne[i] ;
            String str2 = arrayOfWordsPerLigne[i+1];
            boolean ok =  isValidDateCrediteur(str1) &&   isValidDateCrediteur(str2) ;
            if (ok){
                return i;
            }
        }
        return -1 ;
    }
    // we use it for position (debit_or_credit  )
    public static String removeSpacesBetweenDigits(String input) {
        // Replace single space between two digits with no space
        return input.replaceAll("(?<=\\d)\\s{1,3}(?=\\d)", "");
        //return input.replaceAll("(?<=\\d)\\s(?=\\d)", "");
    }

}
