package com.community.api.services;

import com.community.api.component.JwtUtil;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;

@Service
public class PdfEditService {

    private final ResourceLoader resourceLoader;

    public PdfEditService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public byte[] createPdfInMemory(String ackId, Integer role, Long userId, String phoneNumber) throws Exception {
        Resource resource = resourceLoader.getResource("classpath:Acknowledgement/policy.pdf");
        try (InputStream inputStream = resource.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfReader reader = new PdfReader(inputStream);
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(reader, writer);
            Document document = new Document(pdfDoc);

            int lastPage = pdfDoc.getNumberOfPages();

            // Font and colors
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            Color userColor = ColorConstants.BLUE;
            Color providerColor = ColorConstants.GREEN;
            Color infoColor = ColorConstants.BLACK;

            // Create signature line with color and spacing
            Paragraph signatureLine = new Paragraph()
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginBottom(10);

            if (role == 5) {
                signatureLine.setFontColor(userColor)
                        .add("The user with ID: " + userId + " has agreed to the terms and conditions of this policy.");
            } else {
                signatureLine.setFontColor(providerColor)
                        .add("The Service Provider with ID: " + userId + " has agreed to the terms and conditions of this policy.");
            }

            Paragraph timestampLine = new Paragraph("Agreement Timestamp: " + new Date())
                    .setFont(font)
                    .setFontSize(12)
                    .setFontColor(infoColor);

            // Additional info lines
            Paragraph agreementIdLine = new Paragraph("Agreement ID: " + ackId)
                    .setFont(font)
                    .setFontSize(12)
                    .setFontColor(infoColor)
                    .setMarginBottom(8);

            Paragraph phoneLine = new Paragraph("User Phone Number: +91 " + phoneNumber)
                    .setFont(font)
                    .setFontSize(12)
                    .setFontColor(infoColor)
                    .setMarginBottom(8);



            // Define the area on the last page to put the text block
            PdfPage lastPageObj = pdfDoc.getPage(lastPage);
            PdfCanvas pdfCanvas = new PdfCanvas(lastPageObj);
            Rectangle rect = new Rectangle(50, 420, 500, 150);

            // Corrected Canvas constructor: only pass PdfCanvas and Rectangle
            Canvas canvas = new Canvas(pdfCanvas, rect);
            canvas.add(signatureLine);
            canvas.add(agreementIdLine);
            canvas.add(phoneLine);
            canvas.add(timestampLine);
            canvas.close();

            document.close();

            return baos.toByteArray();
        }
    }

@Autowired
JwtUtil jwtUtil;
    public void sendPdfToApi(byte[] pdfBytes, Long customerId, HttpServletRequest request) {
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
        String token=jwtUtil.generateShortLivedToken(22L,1, request.getRemoteAddr());
        headers.setBearerAuth("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJiMjYzODdmOS1hZDU4LTQ2ZjYtYjZjMy1kYmQ2Y2JlMWVkZTUiLCJpZCI6MjIsInJvbGUiOjEsInVzZXJBZ2VudCI6IlBvc3RtYW5SdW50aW1lLzcuNDUuMCIsImlwQWRkcmVzcyI6IjA6MDowOjA6MDowOjA6MSIsImlhdCI6MTc1NTE1MDQ1OSwiZXhwIjoxNzU1MTg2NDU5fQ.nR0nxFN3-XqfrFq_CqRiEvXaL6BRdCU1OIXfQx8dVGU");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("fileTypes", "42"); // send as string, backend will map to Integer
        body.add("files", contentsAsResource);
        body.add("removeFileTypes","0");
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String apiUrl = "https://szhijed7a6.ap.loclx.io/api/v1/customer/upload-documents?customerId="+customerId+"&extUpdate=true";
        System.out.println(apiUrl);
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
