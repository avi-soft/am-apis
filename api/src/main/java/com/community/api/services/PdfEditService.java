package com.community.api.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import io.swagger.models.auth.In;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

@Service
public class PdfEditService {

    private final ResourceLoader resourceLoader;

    public PdfEditService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public byte[] createPdfInMemory(String ackId, Integer role,Long userId,String phoneNumber) throws Exception {
        Resource resource = resourceLoader.getResource("classpath:Acknowledgement/policy.pdf");
        try (InputStream inputStream = resource.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfReader reader = new PdfReader(inputStream);
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(reader, writer);
            Document document = new Document(pdfDoc);
            int lastPage = pdfDoc.getNumberOfPages();
            if(role==5) {
                document.showTextAligned(new Paragraph(
                                "The user with ID: "+userId+" has agreed to the terms and conditions of this policy."),
                        60, 510, lastPage, TextAlignment.LEFT,
                        VerticalAlignment.BOTTOM, 0);
            }
            else
            {
                document.showTextAligned(new Paragraph(
                                "The Service Provider with ID: "+userId+" has agreed to the terms and conditions of this policy."),
                        60, 510, lastPage, TextAlignment.LEFT,
                        VerticalAlignment.BOTTOM, 0);
            }
            document.showTextAligned(new Paragraph(
                            "Agreement ID: "+ackId),
                    60, 495, lastPage, TextAlignment.LEFT,
                    VerticalAlignment.BOTTOM, 0);


            document.showTextAligned(new Paragraph(
                            "User Phone Number: +91 "+phoneNumber),
                    60, 480, lastPage, TextAlignment.LEFT,
                    VerticalAlignment.BOTTOM, 0);

            document.showTextAligned(new Paragraph(
                            "Agreement Timestamp: " + new Date()),
                    60, 465, lastPage, TextAlignment.LEFT,
                    VerticalAlignment.BOTTOM, 0);
            document.close();
            return baos.toByteArray();
        }
    }

    public void sendPdfToApi(byte[] pdfBytes,Long customerId) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            System.out.println("PDF bytes are null or empty, skipping upload.");
            return;
        }
        System.out.println("Preparing to send PDF...");

        ByteArrayResource contentsAsResource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return "signed-policy-v.1.pdf";
            }

            @Override
            public long contentLength() {
                return pdfBytes.length;
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJiMjYzODdmOS1hZDU4LTQ2ZjYtYjZjMy1kYmQ2Y2JlMWVkZTUiLCJpZCI6MjIsInJvbGUiOjEsInVzZXJBZ2VudCI6IlBvc3RtYW5SdW50aW1lLzcuNDUuMCIsImlwQWRkcmVzcyI6IjA6MDowOjA6MDowOjA6MSIsImlhdCI6MTc1NTE1MDQ1OSwiZXhwIjoxNzU1MTg2NDU5fQ.nR0nxFN3-XqfrFq_CqRiEvXaL6BRdCU1OIXfQx8dVGU");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("fileTypes", "42"); // send as string, backend will map to Integer
        body.add("files", contentsAsResource);
        body.add("removeFileTypes","0");
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String apiUrl = "https://szhijed7a6.ap.loclx.io/api/v1/customer/upload-documents?customerId="+customerId;

        try {
            ResponseEntity<String> response = new RestTemplate()
                    .postForEntity(apiUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("PDF successfully uploaded. Response: " + response.getBody());
            } else {
                System.err.println("Failed to upload PDF. Status: " + response.getStatusCode());
            }
        } catch (Exception ex) {
            System.err.println("Error occurred while uploading PDF: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


}
