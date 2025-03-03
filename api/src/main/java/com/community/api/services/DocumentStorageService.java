package com.community.api.services;
import com.community.api.component.Constant;
import com.community.api.component.FFmpegManager;
import com.community.api.configuration.ImageSizeConfig;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.FileType;
import com.community.api.entity.TypingText;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.*;

@Service
public class DocumentStorageService {

    @Autowired
    private  ResponseService responseService;

    @Autowired
    private EntityManager em;

    @Autowired
    private ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private DocumentStorageService documentStorageService;

    @Autowired
    private EntityManager entityManager;

    @Value("${file.server.url}")
    private String fileServerUrl;

    @Autowired
    private RestTemplate restTemplate;

    private final String ffmpegPath;
    private final ExecutorService executorService;
    @Value("${secret.key}")
    private  String key;

    private static final String ALGORITHM = "AES";
    // 16-byte secret key for AES-128

    private static final long MIN_SIZE_BYTES = 100 * 1024; // 100KB
    private static final long MAX_SIZE_BYTES = 200 * 1024; // 200KB
    private static final int MAX_ATTEMPTS = 5;

    private static final Set<String> SUPPORTED_IMAGE_FORMATS = new HashSet<>(Arrays.asList(
            "heic", "heif", "jpg", "jpeg", "png", "webp", "tiff", "tif", "bmp",
            "cr2", "cr3", "nef", "arw", "orf", "dng", "raf", "pef", "srw", "rw2",
            "3fr", "psd", "xcf", "avif", "jp2", "jpx", "ico", "pcx", "tga", "sgi",
            "dib", "jxr", "dpx", "cin", "gif"
    ));

    /**
     * Constructor with FFmpegManager - preferred approach
     */
    @Autowired
    public DocumentStorageService(FFmpegManager ffmpegManager) {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.ffmpegPath = ffmpegManager.getFFmpegExecutable();
    }

    /**
     * Constructor with explicit path (for backward compatibility or testing)
     */
    public DocumentStorageService(String ffmpegPath) {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.ffmpegPath = ffmpegPath;
    }

    /**
     * Get the FFmpeg executable path
     * This method is now simpler since FFmpegManager handles all the complexity
     */
    private String getFfmpegExecutablePath() {
        return this.ffmpegPath;
    }
    public ResponseEntity<Map<String, Object>> saveDocuments(MultipartFile file, String documentTypeStr, Long customerId, String role) {
        try {

            if (!DocumentStorageService.isValidFileType(file)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "status_code", HttpStatus.BAD_REQUEST.value(),
                        "message", "Invalid file type: " + file.getOriginalFilename()
                ));
            }

            if (file.getSize() > Constant.MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "status_code", HttpStatus.BAD_REQUEST.value(),
                        "message", "File size exceeds the maximum allowed size: " + file.getOriginalFilename()
                ));
            }

            String fileName = file.getOriginalFilename();
            try (InputStream fileInputStream = file.getInputStream()) {
                this.saveDocumentOndirctory(customerId.toString(), documentTypeStr, fileName, fileInputStream, role);
            }catch(Exception e){
                exceptionHandlingService.handleException(e);
                return ResponseEntity.badRequest().body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "status_code", HttpStatus.BAD_REQUEST.value(),
                        "message", "Invalid file : " + file
                ));
            }

            Map<String, Object> responseBody = Map.of(
                    "message", "Document uploaded successfully",
                    "status", "OK",
                    "data",documentTypeStr +" uploaded successfully",
                    "status_code", HttpStatus.OK.value()
            );

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", ApiConstants.STATUS_ERROR,
                    "message", "Error uploading document: " + e.getMessage(),
                    "status_code", HttpStatus.INTERNAL_SERVER_ERROR.value()
            ));
        }
    }


    /**
     * Saves a file to a dynamic directory structure.
     *
     * @param customerId The ID of the customer.
     * @param documentType The type of document (e.g., "aadhar", "pan", "signature").
     * @param fileName The name of the file to be saved.
     * @param fileInputStream InputStream of the file data.
     * @throws IOException If an I/O error occurs.
     */
    public void saveDocumentOndirctory(String customerId, String documentType, String fileName, InputStream fileInputStream, String role) throws IOException {

        try{
            String currentDir = System.getProperty("user.dir");

            String testDirPath = currentDir + "/../test/";

            File avisoftDir = new File(testDirPath + "avisoftdocument");
            if (!avisoftDir.exists()) {
                avisoftDir.mkdirs();
            }

            File roleDir = new File(avisoftDir, role);
            if (!roleDir.exists()) {
                roleDir.mkdirs();
            }

            File customerDir = new File(roleDir, customerId);
            if (!customerDir.exists()) {
                customerDir.mkdirs();
            }

            File documentTypeDir = new File(customerDir, documentType);
            if (!documentTypeDir.exists()) {
                documentTypeDir.mkdirs();
            }

            File file = new File(documentTypeDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        }catch(Exception e){
            exceptionHandlingService.handleException(e);
            throw new IOException("Error saving document: " + e.getMessage());
        }
    }


    public static boolean isValidFileType(MultipartFile file) {
        String[] allowedFileTypes = {"application/pdf", "image/jpeg", "image/png", "image/jpg"};
        String contentType = file.getContentType();

        boolean isContentTypeValid = Arrays.asList(allowedFileTypes).contains(contentType);

        String fileName = file.getOriginalFilename();

        boolean isExtensionValid = fileName != null && (fileName.endsWith(".pdf") || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") || fileName.endsWith(".png"));

        return isContentTypeValid && isExtensionValid;
    }


    public List<DocumentType> getAllDocumentTypes() {
        return em.createQuery("SELECT dt FROM DocumentType dt", DocumentType.class).getResultList();
    }
    public String getDocumentTypeFromMultipartFile(MultipartFile file, List<DocumentType> allDocumentTypes) {
        String fileName = file.getOriginalFilename();

        if (fileName != null) {
            for (DocumentType docType : allDocumentTypes) {
                if (fileName.toLowerCase().contains(docType.getDocument_type_name().toLowerCase())) {
                    return docType.getDocument_type_name();
                }
            }
        }
        return "Unknown Document Type";
    }

    private static final int BYTES_TO_MB = 1024 * 1024;

    public void validateDocument(MultipartFile file, DocumentType documentType) {
//        ValidationResult result = new ValidationResult();

        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() : "";

        boolean isValidFileType = documentType.getRequired_document_types().stream()
                .anyMatch(fileType -> fileType.getFile_type_name().toLowerCase().equals(fileExtension));

        if (!isValidFileType) {
            String allowedTypes = documentType.getRequired_document_types().stream()
                    .map(FileType::getFile_type_name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid file type. Allowed types for " +
                    documentType.getDocument_type_name() + " are: " + allowedTypes);
        }

        // Validate file size
//        long fileSizeInMB = file.getSize() / BYTES_TO_MB;
        if (file.getSize()> ImageSizeConfig.convertToBytes(documentType.getMax_document_size())) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + documentType.getMax_document_size());
        }
        if (file.getSize() < ImageSizeConfig.convertToBytes(documentType.getMin_document_size())) {
            throw new IllegalArgumentException("File size is below minimum requirement of " +
                    documentType.getMin_document_size());
        }
    }

    @Transactional
    public void saveDocumentType(DocumentType document) {
        entityManager.persist(document);
    }

    @Transactional
    public void saveAllDocumentTypes() {

        DocumentType[] documents = {


                /*new DocumentType(14,"MATRICULATION", "Completed secondary education or equivalent"),
              /*  new DocumentType(14,"MATRICULATION", "Completed secondary education or equivalent"),
                new DocumentType( 15,"INTERMEDIATE", "Completed higher secondary education or equivalent"),
                new DocumentType(16,"BACHELORS", "Completed undergraduate degree program education "),
                new DocumentType(17,"MASTERS", "Completed postgraduate degree program education"),
                new DocumentType( 18,"DOCTORATE", "Completed doctoral degree program education"),
                new DocumentType(19,"DOMICILE", "The permanent home or principal residence of a person."),
                new DocumentType( 20,"HANDICAPED", "An outdated term for individuals with physical or mental disabilities; \"person with a disability\" is preferred today"),
                new DocumentType(21,"C-FORM-PHOTO", "A C Form photo is a standardized ID photo for official documents."),
                new DocumentType(23,"BUSSINESS_PHOTO", "A Standard proof of Running Bussiness"),
                new DocumentType(25,"NCC CERTIFICATE A", "NCC CERTIFICATE A"),
                new DocumentType(26,"NCC CERTIFICATE B", "NCC CERTIFICATE B"),
                new DocumentType(27,"NCC CERTIFICATE C", "NCC CERTIFICATE C"),
                new DocumentType(28,"NSS CERTIFICATE", "NSS CERTIFICATE"),
                new DocumentType(29,"SPORTS CERTIFICATE - STATE", "SPORTS CERTIFICATE FOR STATE LEVEL"),
                new DocumentType(30,"SPORTS CERTIFICATE - CENTRE", "SPORTS CERTIFICATE FOR STATE LEVEL"),
                new DocumentType(23,"BUSSINESS_PHOTO", "A Standard proof of Running Bussiness"),

                new DocumentType(24,"PERSONAL_PHOTO", "A Personal Photgraph of SP"),

                new DocumentType(24,"PERSONAL_PHOTO", "A Personal Photgraph of SP"),


                new DocumentType(25, "CATEGORY", "The classification of individuals, such as gender categories: Male, Female, Other."),
                new DocumentType(26, "DISABILITY", "A term used to describe individuals with physical or mental impairments; 'person with a disability' is the preferred terminology."),
                new DocumentType(27, "EX-SERVICE-MEN", "An identification document required for veterans, typically used to access benefits or services."),
                new DocumentType(28, "NCC", "A document serving as proof of participation in the National Cadet Corps, often required for certain government applications."),
                new DocumentType(29, "SPORTS", "A personal photograph typically required for sports-related documentation, such as player registrations or team memberships."),
                new DocumentType(30, "FREEDOM FIGHTER", "A personal photograph required for identification and documentation purposes related to recognition and benefits for freedom fighters.")
                */
        };
        for (DocumentType document : documents) {
            saveDocumentType(document);
        }
    }


    @Transactional
    public void deleteDocument(Document document) {

        String filePath = document.getFilePath();
        System.out.println(filePath + " filePath");
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
        em.remove(document);
    }

    @Transactional
    public void updateOrCreateDocument(Document existingDocument, MultipartFile file, DocumentType documentTypeObj, Long customerId, String role) {
        String snakeCaseDocumentType = documentTypeObj.getDocument_type_name().trim().replaceAll(" +", "_");
        String newFilePath = "avisoftdocument"
                + File.separator + role + File.separator + customerId
                + File.separator + snakeCaseDocumentType
                + File.separator + file.getOriginalFilename();

        existingDocument.setFilePath(newFilePath);
        existingDocument.setName(file.getOriginalFilename());
        em.merge(existingDocument);
    }

    @Transactional
    public Document createDocument(MultipartFile file, DocumentType documentTypeObj, CustomCustomer customCustomer, Long customerId, String role) {
        String snakeCaseDocumentType = documentTypeObj.getDocument_type_name().trim().replaceAll(" +", "_");
        Document newDocument = new Document();
        newDocument.setName(file.getOriginalFilename());
        newDocument.setCustom_customer(customCustomer);
        newDocument.setDocumentType(documentTypeObj);
        newDocument.setIsArchived(false);

        String newFilePath = "avisoftdocument"
                + File.separator + role + File.separator + customerId
                + File.separator + snakeCaseDocumentType
                + File.separator + file.getOriginalFilename();


        newDocument.setFilePath(newFilePath);
        em.persist(newDocument);
        return newDocument;
    }




    public String encrypt(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());

        // Use URL-safe Base64 encoding
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedData);
    }

    @Transactional
    public ServiceProviderDocument createDocumentServiceProvider(MultipartFile file, DocumentType documentTypeObj, ServiceProviderEntity serviceProviderEntity, Long customerId, String role) {
        String snakeCaseDocumentType = documentTypeObj.getDocument_type_name().trim().replaceAll(" +", "_");
        ServiceProviderDocument newDocument = new ServiceProviderDocument();
        newDocument.setName(file.getOriginalFilename());
        newDocument.setServiceProviderEntity(serviceProviderEntity);
        newDocument.setDocumentType(documentTypeObj);
        newDocument.setIsArchived(false);

        String newFilePath = "avisoftdocument"
                + File.separator + role + File.separator + customerId
                + File.separator + snakeCaseDocumentType
                + File.separator + file.getOriginalFilename();


        newDocument.setFilePath(newFilePath);
        em.persist(newDocument);
        return newDocument;
    }
    @Transactional
    public void updateOrCreateServiceProvider(ServiceProviderDocument existingDocument, MultipartFile file, DocumentType documentTypeObj, Long customerId, String role) {
        String snakeCaseDocumentType = documentTypeObj.getDocument_type_name().trim().replaceAll(" +", "_");
        String newFilePath = "avisoftdocument"
                + File.separator + role + File.separator + customerId
                + File.separator + snakeCaseDocumentType
                + File.separator + file.getOriginalFilename();

        existingDocument.setFilePath(newFilePath);
        existingDocument.setName(file.getOriginalFilename());
        em.merge(existingDocument);
    }
    public String findRoleName(DocumentType documentTypeId) {
        return entityManager.createQuery("SELECT dt.document_type_name FROM DocumentType dt WHERE dt.document_type_id = :documentTypeId", String.class)
                .setParameter("documentTypeId", documentTypeId.getDocument_type_id())
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void saveAllTypingTexts() {
        TypingText[] typingTexts = {
                new TypingText(1L, "The sun sets over the horizon, painting the sky with vibrant hues of orange and pink. Birds fly home, and the world quietly transitions into the peaceful calm of evening."),
                new TypingText(2L, "A gentle breeze rustles the leaves, carrying the sweet scent of blooming flowers through the air. The world feels alive and at peace."),
                new TypingText(3L, "The mountain stood tall, its peak covered in snow, contrasting sharply with the clear blue sky above. Nature's beauty was on full display."),
                new TypingText(4L, "Waves crash against the shore, their rhythmic motion soothing to the soul. The ocean stretches endlessly, its mysteries hidden beneath the surface."),
                new TypingText(5L, "In the heart of the forest, sunlight filters through the canopy, casting dappled shadows on the ground. A sense of tranquility fills the air.")
        };

        for (TypingText text : typingTexts) {
            saveTypingText(text);
        }
    }

    @Transactional
    public void saveTypingText(TypingText typingText) {
        entityManager.persist(typingText);
    }


    public void uploadFileOnFileServer(MultipartFile file, String documentType, String customerId, String role) throws IOException {
        try {
            String url = fileServerUrl + "/files/upload";
            String snakeCaseDocumentType = documentType.trim().replaceAll(" +", "_");
            final String filename = file.getOriginalFilename();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
            ByteArrayResource contentsAsResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
            multiValueMap.add("file", contentsAsResource);
            multiValueMap.add("documentType", snakeCaseDocumentType);
            multiValueMap.add("customerId", customerId);
            multiValueMap.add("role", role.toLowerCase());
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(multiValueMap, headers);

            restTemplate.postForObject(url, request, String.class);

        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new IOException("Error saving document: " + e.getMessage());
        }
    }

    public String deleteFile(Long customerId, String documentType, String fileName, String role) throws IOException {
        try {
            // String snakeCaseDocumentType = documentType.trim().replaceAll(" +", "_");
            String url = fileServerUrl + "/files/delete?customerId=" + customerId +
                    "&documentType=" + documentType + "&fileName=" + fileName + "&role=" + role;


            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);

            String deletedFilePath = response.getBody();
            if (deletedFilePath != null && !deletedFilePath.isEmpty()) {
                System.out.println("File deleted: " + deletedFilePath);
            } else {
                throw new IOException("No file path returned from server.");
            }
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new IOException("Error deleting document: " + e.getMessage());
        }
        return  fileName;
    }

    public MultipartFile convertToJpg(MultipartFile inputFile) throws IOException {
        if (inputFile.getSize() == 0) {
            throw new IOException("Input file is empty");
        }

        String originalExtension = getFileExtension(inputFile.getOriginalFilename());
        if (!isImageFormat(originalExtension)) {
            throw new IOException("Unsupported file format: " + originalExtension);
        }

        File tempInputFile = null;
        File tempIntermediateFile = null;
        File tempOutputFile = null;

        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            tempInputFile = File.createTempFile("temp_" + timestamp, "." + originalExtension);
            tempIntermediateFile = File.createTempFile("intermediate_" + timestamp, ".jpg");
            tempOutputFile = File.createTempFile("converted_" + timestamp, ".jpg");

            inputFile.transferTo(tempInputFile);

            // STEP 1: Use FFmpeg to convert format to JPG (without resizing)
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ffmpegPath,
                    "-i", tempInputFile.getAbsolutePath(),
                    "-q:v", "1",  // Best quality for intermediate step
                    "-y",
                    tempIntermediateFile.getAbsolutePath()
            );

            Process process = processBuilder.start();

            // Read the process output for debugging
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFmpeg: " + line);
                }
            }

            boolean completed = process.waitFor(30, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new IOException("FFmpeg conversion timed out");
            }

            if (process.exitValue() != 0 || !tempIntermediateFile.exists() || tempIntermediateFile.length() == 0) {
                throw new IOException("Format conversion failed - output file is empty or doesn't exist");
            }

            // STEP 2: Now use Java's image processing to resize and adjust quality
            BufferedImage intermediateImage = ImageIO.read(tempIntermediateFile);
            if (intermediateImage == null) {
                throw new IOException("Failed to read intermediate image");
            }

            // Calculate target size based on whether we need to upscale or downscale
            int width = intermediateImage.getWidth();
            int height = intermediateImage.getHeight();
            long intermediateSize = tempIntermediateFile.length();

            if (intermediateSize < MIN_SIZE_BYTES) {
                // Upscale small images
                double scaleFactor = calculateUpscaleRatio(intermediateSize);
                width = (int) (width * scaleFactor);
                height = (int) (height * scaleFactor);
            } else if (intermediateSize > MAX_SIZE_BYTES) {
                // Downscale large images
                double scaleFactor = calculateDownscaleRatio(intermediateSize);
                width = (int) (width * scaleFactor);
                height = (int) (height * scaleFactor);
            }

            // Resize with high quality
            BufferedImage resultImage = resizeWithHighQuality(intermediateImage, width, height);

            // Write the JPEG with appropriate compression
            writeJpegWithTargetSize(resultImage, tempOutputFile, intermediateSize);

            String newFilename = generateOutputFilename(inputFile.getOriginalFilename());

            FileItem fileItem = new DiskFileItem(
                    "file",
                    "image/jpeg",
                    false,
                    newFilename,
                    (int) tempOutputFile.length(),
                    tempOutputFile.getParentFile()
            );

            try (InputStream input = new FileInputStream(tempOutputFile);
                 OutputStream output = fileItem.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }

            return new CommonsMultipartFile(fileItem);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Conversion interrupted", e);
        } finally {
            cleanupFile(tempInputFile);
            cleanupFile(tempIntermediateFile);
            cleanupFile(tempOutputFile);
        }
    }

    // Reuse these methods from the first version
    private BufferedImage resizeWithHighQuality(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return resized;
    }

    private void writeJpegWithTargetSize(BufferedImage image, File outputFile, long originalSize) throws IOException {
        // Start with reasonable quality
        float targetQuality = originalSize < MIN_SIZE_BYTES ? 0.95f : 0.85f;

        // Try writing with different quality levels until we hit target size range
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(targetQuality);

            try (ImageOutputStream output = ImageIO.createImageOutputStream(outputFile)) {
                writer.setOutput(output);
                writer.write(null, new IIOImage(image, null, null), params);
            }

            long fileSize = outputFile.length();

            // Check if size is in desired range
            if (fileSize >= MIN_SIZE_BYTES && fileSize <= MAX_SIZE_BYTES) {
                break;
            }

            // Adjust quality for next attempt
            if (fileSize < MIN_SIZE_BYTES) {
                targetQuality = Math.min(1.0f, targetQuality + 0.1f);
            } else {
                targetQuality = Math.max(0.5f, targetQuality - 0.1f);
            }
        }
    }

    private double calculateUpscaleRatio(long originalSize) {
        // Calculate desired upscale factor based on current size
        double targetSize = (MIN_SIZE_BYTES + MAX_SIZE_BYTES) / 2.0;
        double ratio = Math.sqrt(targetSize / originalSize);
        // Limit maximum upscale to 3x
        return Math.min(3.0, ratio);
    }

    private double calculateDownscaleRatio(long originalSize) {
        // Calculate desired downscale factor based on current size
        double targetSize = (MIN_SIZE_BYTES + MAX_SIZE_BYTES) / 2.0;
        double ratio = Math.sqrt(targetSize / originalSize);
        // Limit minimum downscale to 0.3x
        return Math.max(0.3, ratio);
    }

    // Reuse these utility methods from the second version
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean isImageFormat(String extension) {
        return SUPPORTED_IMAGE_FORMATS.contains(extension.toLowerCase());
    }

    private String generateOutputFilename(String originalFilename) {
        if (originalFilename == null) {
            return "converted.jpg";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex != -1) {
            return originalFilename.substring(0, dotIndex) + ".jpg";
        }
        return originalFilename + ".jpg";
    }

    private void cleanupFile(File file) {
        if (file != null && file.exists()) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                System.err.println("Failed to cleanup temporary file: " + e.getMessage());
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
