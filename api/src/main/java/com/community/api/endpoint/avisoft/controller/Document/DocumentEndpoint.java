package com.community.api.endpoint.avisoft.controller.Document;

import com.community.api.annotation.Authorize;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.DocumentTypeDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Role;
import com.community.api.entity.ShortAccessToken;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.util.Date;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.jsonwebtoken.JwsHeader.ALGORITHM;


@RestController
@RequestMapping(value = "/document-type")
public class DocumentEndpoint {
    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    private CustomDocumentTypeService documentTypeService;

    @Autowired
    private FileService fileService;


    @Autowired
    private DocumentStorageService documentStorageService;


    private static String key="2025202220202512";


    @Autowired
    private RoleService roleService;
    private EntityManager entityManager;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;

    public DocumentEndpoint(EntityManager entityManager, ExceptionHandlingImplement exceptionHandling, ResponseService responseService) {
        this.entityManager = entityManager;
        this.exceptionHandling = exceptionHandling;
        this.responseService = responseService;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> createDocumentType(@RequestBody DocumentTypeDto documentType, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            DocumentType documentTypeToAdd = documentTypeService.addDocumentTypes(documentType,authHeader);
            return  ResponseService.generateSuccessResponse("Document type created successfully", documentTypeToAdd, HttpStatus.CREATED);
        }
        catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{documentTypeId}")
    public ResponseEntity<?> updateDocumentType(@PathVariable Integer documentTypeId, @RequestBody DocumentTypeDto documentType, @RequestHeader(value = "Authorization")String authHeader)
    {
        try
        {
            DocumentType updatedDocumentType= documentTypeService.updateDocumentType(documentTypeId,documentType,authHeader);
            return responseService.generateResponse(HttpStatus.OK,"DocumentType is updated successfully", updatedDocumentType);
        }
        catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

//    @Authorize(value = {Constant.roleSuperAdmin})
    @PutMapping("/manage/{id}")
    public ResponseEntity<?> manageDocumentTypeStatus(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "false") Boolean archive) {
        try {
            DocumentType university = documentTypeService.manageDocumentType(id, archive);
            String message = archive ? "DocumentType archived successfully" : "DocumentType unarchived successfully";
            return responseService.generateSuccessResponse(message, university, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating document-type status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-document")
    public ResponseEntity<?> getAllDocuments(@RequestParam(required = false,defaultValue = "false")Boolean archived) {
        try {
            List<DocumentType> documentTypes;

            TypedQuery<DocumentType> query = entityManager.createQuery("SELECT dt FROM DocumentType dt WHERE dt.archived = :archived ORDER BY dt.sort_order ASC", DocumentType.class);
            query.setParameter("archived",archived);
            documentTypes=query.getResultList();

            if (documentTypes.isEmpty()) {
                return responseService.generateErrorResponse(archived?"No any document-type is archived":"No any document-type is unarchived", HttpStatus.OK);
            }

            return responseService.generateSuccessResponse("Document Types retrieved successfully", documentTypes, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Document Types", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-by-id/{documentTypeId}")
    public ResponseEntity<?> getDocumentById(@PathVariable Integer documentTypeId) {
        try {
            DocumentType documentType = documentTypeService.getDocumentTypeById(documentTypeId);
            if (documentType == null) {
                return ResponseService.generateErrorResponse("NO DOCUMENT TYPE FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("DOCUMENT TYPE FOUND", documentType, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-document-of-customer")
    public ResponseEntity<?> getDocumentOfCustomer(
            @RequestParam Long customerId,
            @RequestParam(required = false) Integer role,@RequestHeader(value = "Authorization")String authHeader,
            HttpServletRequest request) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            Role roleCheck=roleService.getRoleByRoleId(roleId);

            //checking for super admin and admin
            if((roleCheck.getRole_name().equals(Constant.roleUser)&&!Objects.equals(tokenUserId, customerId)))
                return ResponseService.generateErrorResponse("Forbidden",HttpStatus.FORBIDDEN);

            if (role != null) {
                if (roleService.findRoleName(role).equals(Constant.SERVICE_PROVIDER)) {

                    ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, customerId);

                    if (serviceProviderEntity == null) {
                        return responseService.generateErrorResponse("Data not found", HttpStatus.NOT_FOUND);

                    }
                    StringBuilder jpql = new StringBuilder("SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity AND isArchived=false");
                    jpql.append(" AND d.filePath != null");
                    TypedQuery<ServiceProviderDocument> query1 = entityManager.createQuery(jpql.toString(), ServiceProviderDocument.class);
                    query1.setParameter("serviceProviderEntity", serviceProviderEntity);
                    List<ServiceProviderDocument> serviceProviderDocuments = query1.getResultList();
                    if (serviceProviderDocuments.isEmpty()) {
                        return responseService.generateSuccessResponse("No documents found",null ,HttpStatus.OK);
                    }
                    List<DocumentResponse> documentResponses = serviceProviderDocuments.stream()
                            .map(serviceProviderDocument -> {
                                String fileName = serviceProviderDocument.getName();
                                String filePath = null;
                                try {
                                    filePath = documentStorageService.encrypt(serviceProviderDocument.getFilePath());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                String fileUrl = null;
                                    fileUrl = fileService.getFileUrl(filePath, request);
                                String document_name = documentStorageService.findRoleName(serviceProviderDocument.getDocumentType());
                                Date created_date = serviceProviderDocument.getUploadedDate();

                                return new DocumentResponse(fileName, fileUrl, document_name, created_date);
                            })
                            .collect(Collectors.toList());
                    return responseService.generateSuccessResponse("Documents retrieved successfully", documentResponses, HttpStatus.OK);
                }

            } else {
                CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
                if (customer == null) {
                    return responseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                }

                StringBuilder jpql = new StringBuilder("SELECT d FROM Document d WHERE d.custom_customer = :customer AND isArchived=false");
                jpql.append(" AND d.filePath != null");
                TypedQuery<Document> query = entityManager.createQuery(jpql.toString(), Document.class);
                query.setParameter("customer", customer);
                List<Document> documents = query.getResultList();
                if (documents.isEmpty()) {
                    return responseService.generateSuccessResponse("No documents found",null ,HttpStatus.OK);
                }
                List<DocumentResponse> documentResponses = documents.stream()
                        .map(document -> {
                            String fileName = document.getName();
                            String filePath = null;
                            try {
                                filePath = documentStorageService.encrypt(document.getFilePath());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            String fileUrl = fileService.getFileUrl(filePath, request);

                            String document_name = documentStorageService.findRoleName(document.getDocumentType());
                            Date created_date = document.getCreatedDate();
                            return new DocumentResponse(fileName, fileUrl, document_name, created_date);
                        })
                        .collect(Collectors.toList());
                return responseService.generateSuccessResponse("Documents retrieved successfully", documentResponses, HttpStatus.OK);

            }
            return responseService.generateErrorResponse("Invalid request", HttpStatus.BAD_REQUEST);


        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Documents", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private static final String AES_ALGORITHM = "AES";
    public static String decrypt(String encryptedData) throws Exception {
        try {
            // Decode the URL-safe Base64 string
            byte[] decodedData = Base64.getUrlDecoder().decode(encryptedData);

            // Initialize the SecretKeySpec and Cipher for decryption
            SecretKeySpec secretKey = new SecretKeySpec(Constant.KEY.getBytes(), AES_ALGORITHM ); //@TODO-remove key from constants
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM );
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(decodedData);
            System.out.println("i am returning");
            // Convert the decrypted data back to a String
            return new String(decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }
    @Autowired
    JwtUtil jwtUtil;
    @Authorize(value = {Constant.roleAdmin,Constant.roleSuperAdmin,Constant.roleServiceProvider})
    @Transactional
    @PostMapping("/download")
    public ResponseEntity<?> downloadFile(
            @RequestBody Map<String, Object> loginDetails,
            HttpServletResponse response,HttpServletRequest request
    ) {
        try {
            String fileUrl1 = (String) loginDetails.get("filePath");
            String token = fileUrl1.substring(fileUrl1.indexOf(".io/") + 4);
            String filePath = decrypt(token);
// Example: avisoftdocument\CUSTOMER\6416\Disability_Certificate\imresizer-1731964770170 (1) (1).jpg

// Extract ID from filePath
            Long id = null;
            java.util.regex.Pattern pattern = Pattern.compile("CUSTOMER[\\\\/](\\d+)[\\\\/]");
            Matcher matcher = pattern.matcher(filePath);
            if (matcher.find()) {
                id = Long.parseLong(matcher.group(1));
            } else {
                throw new IllegalArgumentException("Invalid file path format, ID not found");
            }

            String ip = request.getHeader("X-Forwarded-For");
// Set roleId = 5
            int roleId = 5;
            System.out.println("id is"+id+" and role is"+roleId);

// Generate short-lived token
            String tokenToAdd = jwtUtil.generateShortLivedToken(id, roleId, ip);

// Get the download URL
            String fileUrl = fileService.getDownloadFileUrl(filePath, request);

// Append token as query parameter
            String securedFileUrl = fileUrl + "?token=" + URLEncoder.encode(tokenToAdd, StandardCharsets.UTF_8.toString());
            System.out.println("Now url is"+securedFileUrl);

            TypedQuery<ShortAccessToken> query = entityManager.createQuery(
                    "SELECT s FROM ShortAccessToken s WHERE s.userId = :uid AND s.role = :role",
                    ShortAccessToken.class
            );
            query.setParameter("uid", id);
            query.setParameter("role", roleId);
            String encryptedFileName = filePath.substring(fileUrl1.lastIndexOf('/') + 1);
            // Extract "Aadhaar_Card_Backside" (the folder name before the filename)
            String[] pathParts = filePath.split("\\\\");
            String folderName = pathParts[pathParts.length - 2]; // Gets the second-last part
            String downloadFileName = folderName + ".jpg";
            CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,id);
            downloadFileName=customCustomer.getFirstName()+" "+customCustomer.getLastName()+" "+downloadFileName;
            System.out.println("fileName"+downloadFileName);
            List<ShortAccessToken> resultList = query.getResultList();

            if (resultList.isEmpty()) {
                ShortAccessToken shortAccessToken = ShortAccessToken.builder()
                        .userId(id)
                        .token(token)
                        .role(roleId)
                        .expired(false)
                        .build();
                entityManager.persist(shortAccessToken);
            } else {
                ShortAccessToken shortAccessToken = resultList.get(0);
                shortAccessToken.setToken(token);
                shortAccessToken.setExpired(false);
                entityManager.merge(shortAccessToken);
            }
            System.out.println(filePath);

            URI uri = URI.create(securedFileUrl);
            URL url = uri.toURL();
            // Download logic
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("response-content-disposition",
                    "attachment; filename=\"" + downloadFileName + "\"");
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return responseService.generateErrorResponse("Download failed", HttpStatus.BAD_REQUEST);
            }

            // Set Content-Disposition with the custom filename
            response.setContentType(connection.getContentType());
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"" + downloadFileName + "\""
            );

            // Stream the file to response
            try (InputStream in = new URL(securedFileUrl).openStream();  // Use the original fileUrl, not securedFileUrl
                 OutputStream out = response.getOutputStream()) {
                IOUtils.copy(in, out);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    "Error downloading file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    private class DocumentResponse {

        private String fileName;
        private String fileUrl;
        private String document_name;
        private Date created_date;

        public DocumentResponse(String fileName, String fileUrl, String document_name, Date createdDate) {
            this.fileName = fileName;
            this.fileUrl = fileUrl;
            this.document_name = document_name;
            this.created_date = createdDate;
        }

        public Date getCreatedDate() {
            return created_date;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public String getDocument_name() {
            return document_name;
        }

    }
}
