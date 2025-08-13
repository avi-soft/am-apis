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


    @Authorize(value = {Constant.roleAdmin, Constant.roleSuperAdmin, Constant.roleServiceProvider})
    @Transactional
    @PostMapping("/download")
    public void downloadFile(
            @RequestBody Map<String, Object> loginDetails,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        try {
            System.out.println("📌 downloadFile() called");

            String fileUrl1 = (String) loginDetails.get("filePath");
            System.out.println("➡ Original filePath param from UI: " + fileUrl1);

            String token = fileUrl1.substring(fileUrl1.indexOf(".io/") + 4);
            System.out.println("➡ Extracted token from filePath: " + token);

            String filePath = decrypt(token);
            System.out.println("➡ Decrypted filePath: " + filePath);

            // Extract ID
            Long id = null;
            Matcher matcher = Pattern.compile("(CUSTOMER|SERVICE_PROVIDER|ADMIN)[\\\\/](\\d+)[\\\\/]").matcher(filePath);

            if (matcher.find()) {
                String role = matcher.group(1); // CUSTOMER, SERVICE_PROVIDER, or ADMIN
                id = Long.parseLong(matcher.group(2)); // The ID
                System.out.println("Role: " + role + ", Extracted ID: " + id);
            } else {
                System.out.println(" ID not found in filePath");
                throw new IllegalArgumentException("Invalid file path");
            }

            // Generate token for remote fetch
            String ip = request.getHeader("X-Forwarded-For");
            System.out.println("➡ Client IP: " + ip);
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, id);
            ServiceProviderEntity sp=null;
            if(customCustomer==null)
            {
                sp=entityManager.find(ServiceProviderEntity.class,id);
            }
            int roleId=4;
            if(customCustomer!=null)
                 roleId = 5;
            String tokenToAdd = jwtUtil.generateShortLivedToken(id, roleId, ip);
            System.out.println("✅ Generated short-lived token: " + tokenToAdd);

            String fileUrl = fileService.getDownloadFileUrl(filePath, request);
            System.out.println("➡ Base file service URL: " + fileUrl);

            String securedFileUrl = fileUrl + "?token=" + URLEncoder.encode(tokenToAdd, StandardCharsets.UTF_8);
            System.out.println("✅ Secured file URL for remote fetch: " + securedFileUrl);

            // Prepare actual name & extension from remote content-type
            String folderName = filePath.split("\\\\")[filePath.split("\\\\").length - 2];
            System.out.println("➡ Folder name from file path: " + folderName);

            URL url = new URL(securedFileUrl);
            URLConnection conn = url.openConnection();
            String contentType = conn.getContentType();
            System.out.println("✅ Remote content-type: " + contentType);

            response.setContentType(contentType);

            String extension = "";
            if ("image/png".equalsIgnoreCase(contentType)) extension = ".png";
            else if ("image/jpeg".equalsIgnoreCase(contentType)) extension = ".jpg";
            else if ("application/pdf".equalsIgnoreCase(contentType)) extension = ".pdf";
            else extension = ".bin";

            System.out.println("✅ Determined file extension: " + extension);
            String downloadFileName=null;
            if(customCustomer!=null) {
                downloadFileName   = customCustomer.getFirstName() + " " +
                        customCustomer.getLastName() + " " +
                        folderName + extension;
            }
            else
            {
                 downloadFileName = sp.getFirst_name() + " " +
                        sp.getLast_name() + " " +
                        folderName + extension;
            }
            System.out.println("✅ Final custom download filename: " + downloadFileName);

            String encodedFileName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            System.out.println("➡ Encoded filename for header: " + encodedFileName);

            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + downloadFileName + "\"; filename*=UTF-8''" + encodedFileName);

            System.out.println("📤 Starting file streaming to client...");
            try (InputStream in = conn.getInputStream(); OutputStream out = response.getOutputStream()) {
                IOUtils.copy(in, out);
            }
            System.out.println("✅ File streaming completed successfully");

        } catch (Exception e) {
            System.out.println("❌ Exception in downloadFile(): " + e.getMessage());
            e.printStackTrace();

            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            try {
                response.getWriter().write("Error: " + e.getMessage());
            } catch (IOException ignored) {}
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
