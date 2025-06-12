package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.configuration.ImageSizeConfig;
import com.community.api.entity.Image;
import com.community.api.entity.RandomImageType;
import com.community.api.entity.TypingText;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import static com.community.api.services.DocumentStorageService.isValidFileType;
import static com.community.api.services.ServiceProviderTestService.areImagesVisuallyIdentical;

@Service
public class ImageService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DocumentStorageService documentStorageService;
    public ImageService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    public static final double DEFAULT_DPI = 300.0;

    @Transactional
    public Image saveImage(MultipartFile file, Integer randomImageTypeId) throws Exception {
        // Early validations - fail fast
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("File is missing or empty");
        }
        checkForDuplicateImageFast(file.getBytes(),file.getOriginalFilename());
        if (randomImageTypeId == null) {
            throw new IllegalArgumentException("Random image type cannot be null");
        }

        RandomImageType randomImage = entityManager.find(RandomImageType.class, randomImageTypeId);
        if (randomImage == null) {
            throw new IllegalArgumentException("No randomImage type exists with id " + randomImageTypeId);
        }

        DocumentType documentType= differentiateRandomType(randomImageTypeId);
        documentStorageService.validateDocument(file,documentType);

        long fileSize = file.getSize();

        if (needsPassportValidation(randomImageTypeId)) {
            validatePassportSizeDimensionsUltraFast(file,documentType);
        }
//        validateFileSize(fileSize, randomImageTypeId);

        byte[] fileBytes = file.getBytes();

        documentStorageService.uploadFileOnFileServer(file, "Random_Images", "Random", "SERVICE_PROVIDER");
        String dbPath = "avisoftdocument/SERVICE_PROVIDER/Random/Random_Images" + File.separator + file.getOriginalFilename();

        Image image = new Image();
        image.setFile_name(file.getOriginalFilename());
        image.setFile_type(file.getContentType());
        image.setImage_data(fileBytes);
        image.setFile_path(dbPath);
        image.setRandomImageType(randomImage);
        image.setImage_size(ImageSizeConfig.convertBytesToReadableSize(fileSize));

        entityManager.persist(image);
        return image;
    }

    private void validatePassportSizeDimensionsUltraFast(MultipartFile file,DocumentType documentType) throws Exception {
        try (ImageInputStream iis = ImageIO.createImageInputStream(file.getInputStream())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Invalid image format");
            }

            ImageReader reader = readers.next();
            reader.setInput(iis);

            // Get dimensions instantly
            int widthPx = reader.getWidth(0);
            int heightPx = reader.getHeight(0);
            reader.dispose();

            double widthMm =  ((widthPx * 25.4) / DEFAULT_DPI);
            double heightMm = ((heightPx * 25.4) / DEFAULT_DPI);

           widthMm= ((int) widthMm);
           heightMm= ((int) heightMm);
            double PASSPORT_MIN_WIDTH_MM = documentType.getMin_width_dimension_in_mm();
            double PASSPORT_MAX_WIDTH_MM = documentType.getMax_width_dimension_in_mm();
            double PASSPORT_MIN_HEIGHT_MM = documentType.getMin_height_dimension_in_mm();
            double PASSPORT_MAX_HEIGHT_MM = documentType.getMax_height_dimension_in_mm();
            // Quick validation
            if (widthMm < PASSPORT_MIN_WIDTH_MM || widthMm > PASSPORT_MAX_WIDTH_MM || heightMm < PASSPORT_MIN_HEIGHT_MM || heightMm > PASSPORT_MAX_HEIGHT_MM || heightMm <= widthMm) {
                throw new IllegalArgumentException(
                        String.format(
                                "upload passport size photo, your photo dimensions: %.1f x %.1f mm. Required: %.1f to %.1f mm width, %.1f to %.1f mm height, portrait orientation.",
                                widthMm, heightMm,
                                PASSPORT_MIN_WIDTH_MM, PASSPORT_MAX_WIDTH_MM,
                                PASSPORT_MIN_HEIGHT_MM, PASSPORT_MAX_HEIGHT_MM
                        )
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error reading image", e);
        }
    }

    private DocumentType differentiateRandomType(Integer randomImageTypeId)
    {
        DocumentType documentType=null;
        switch (randomImageTypeId){
            case 1:
                 documentType= entityManager.find(DocumentType.class,Constant.RANDOM_RESIZED_DOCUMENT_TYPE_ID);
                if(documentType==null)
                {
                    throw new IllegalArgumentException("The requirements not found in DB for Random Resize image");
                }
                break;

            case 2:
                 documentType= entityManager.find(DocumentType.class,Constant.RANDOM_PDF_DOCUMENT_TYPE_ID);
                if(documentType==null)
                {
                    throw new IllegalArgumentException("The requirements not found in DB for Random PDF image");
                }
                break;
            case 3:
                 documentType= entityManager.find(DocumentType.class,Constant.RANDOM_SIGNATURE_DOCUMENT_TYPE_ID);
                if(documentType==null)
                {
                    throw new IllegalArgumentException("The requirements not found in DB for Random Signature image");
                }
        }
        return documentType;
    }

    private boolean needsPassportValidation(Integer randomImageTypeId) {
        return randomImageTypeId.equals(1);
    }

    public void checkForDuplicateImageFast(byte[] uploadImageData, String filename) throws IllegalArgumentException {
        try {
            String uploadHash = generateImageHash(uploadImageData);
            List<Image> images = getAllRandomImages(null);
            for (Image image : images) {
                String existingHash = generateImageHash(image.getImage_data());
                if (uploadHash.equals(existingHash)) {
                    // If hashes match, do detailed comparison to confirm
                    boolean areImagesIdentical = areImagesVisuallyIdentical(uploadImageData, image.getImage_data());
                    if (areImagesIdentical) {
                        throw new IllegalArgumentException(
                                String.format("Duplicate image detected. File '%s' already exists as '%s'",
                                        filename, image.getFile_name())
                        );
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to check for duplicate images", e);
        }
    }

    private String generateImageHash(byte[] imageData) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(imageData);
        return Base64.getEncoder().encodeToString(hash);
    }

        /*@Transactional
        public List<Image> saveImages(List<MultipartFile> files) throws Exception {

            List<Image> savedImages = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    throw new IllegalStateException("File is missing or empty");
                }

                byte[] uploadImageData = file.getBytes();
                List<Image> images = getAllRandomImages();
                for(Image image : images) {
                    byte[] imageData = image.getImage_data();
                    try{
                        boolean areImagesIdentical=areImagesVisuallyIdentical(uploadImageData, imageData);
                        if(areImagesIdentical) {
                            throw new IllegalStateException("Image already exists");
                        }
                    }
                    catch (IOException e) {
                        throw new IllegalStateException("Error comparing images", e);
                    }
                }
                // Construct file path
                String db_path = "avisoftdocument/SERVICE_PROVIDER/Random/Random_Images";
                String dbPath = db_path + File.separator + file.getOriginalFilename();

                // Validate the file type
                if (!isValidFileType(file)) {
                    throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
                }

                // Validate the file size
                long maxSizeInBytes = ImageSizeConfig.convertToBytes(maxImageSize);
                if (file.getSize() < Constant.RANDOM_MIN_FILE_SIZE || file.getSize() > maxSizeInBytes) {
                    String minImageSize = ImageSizeConfig.convertBytesToReadableSize(Constant.RANDOM_MIN_FILE_SIZE);
                    throw new IllegalArgumentException("Image size should be between " + minImageSize + " and " + maxImageSize);
                }

                byte[] fileBytes = file.getBytes();

                fileUploadService.uploadFileOnFileServer(file, "Random_Images", "Random", "SERVICE_PROVIDER");

                // Create and populate the Image entity
                Image image = new Image();
                image.setFile_name(file.getOriginalFilename());
                image.setFile_type(file.getContentType());
                image.setImage_data(fileBytes);
                image.setFile_path(dbPath);

                // Persist the image entity to the database
                entityManager.persist(image);
                savedImages.add(image);
            }

            return savedImages;
        }
*/

    @Transactional
    public List<Image> getAllRandomImages(List<Integer> randomImageTypeIds) {
        String baseQuery = "SELECT i FROM Image i WHERE i.archived = false";

        if (randomImageTypeIds != null && !randomImageTypeIds.isEmpty()) {
            baseQuery += " AND i.randomImageType.randomImageTypeId IN :typeIds";
        }

        TypedQuery<Image> typedQuery = entityManager.createQuery(baseQuery, Image.class);

        if (randomImageTypeIds != null && !randomImageTypeIds.isEmpty()) {
            typedQuery.setParameter("typeIds", randomImageTypeIds);
        }
        return typedQuery.getResultList();
    }

    @Transactional
    public Image archiveOrUnArchiveImage(Long randomImageId, Boolean archive)
    {
        Image image= entityManager.find(Image.class,randomImageId);
        if(image==null)
        {
            throw new IllegalArgumentException("No Image exists in db with id "+ randomImageId);
        }
        if (archive) {
            if(image.getArchived().equals(true))
            {
                throw new IllegalArgumentException("Image is already archived");
            }
            image.setArchived(true);
        } else {
            if (!image.getArchived()) {
                throw new IllegalArgumentException("Image already unarchived");
            }
            image.setArchived(false);
        }
        entityManager.merge(image);
        return image;
    }

    @Transactional
    public Image updateImageTypeInRandomImage(Integer randomImageTypeId , Long imageId)
    {
        Image imageToUpdate= entityManager.find(Image.class,imageId);
        if(imageToUpdate==null)
        {
            throw new IllegalArgumentException("Image not found");
        }
        if(imageToUpdate.getArchived().equals(true))
        {
            throw new IllegalArgumentException("Image is archived");
        }

        if(randomImageTypeId!=null)
        {

            RandomImageType randomImageType= entityManager.find(RandomImageType.class,randomImageTypeId);
            if(randomImageType==null)
            {
                throw new IllegalArgumentException("No Any randomImage type exists with id " + randomImageTypeId);
            }
            imageToUpdate.setRandomImageType(randomImageType);
        }
        entityManager.merge(imageToUpdate);
        return imageToUpdate;
    }
}