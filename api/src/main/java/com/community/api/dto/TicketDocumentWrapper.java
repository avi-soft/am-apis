package com.community.api.dto;

import com.community.api.utils.ServiceProviderDocument;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDocumentWrapper extends BaseWrapper implements APIWrapper<ServiceProviderDocument> {

    @JsonProperty("document_id")
    private Long documentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("archived")
    private Boolean isArchived;

    @JsonProperty("uploaded_date")
    protected Date uploadedDate;

    @JsonProperty("modified_date")
    protected Date modifiedDate;


    @Override
    public void wrapDetails(ServiceProviderDocument serviceProviderDocument, HttpServletRequest httpServletRequest) {

    }

    public void wrapDetails(ServiceProviderDocument serviceProviderDocument, String fileUrl, HttpServletRequest httpServletRequest) {
        this.documentId = serviceProviderDocument.getDocumentId();
        this.name = serviceProviderDocument.getName();
        this.fileUrl = fileUrl;
        this.isArchived = serviceProviderDocument.getIsArchived();
        this.uploadedDate = serviceProviderDocument.getUploadedDate();
        this.modifiedDate = serviceProviderDocument.getModifiedDate();
    }

    @Override
    public void wrapSummary(ServiceProviderDocument serviceProviderDocument, HttpServletRequest httpServletRequest) {

    }
}
