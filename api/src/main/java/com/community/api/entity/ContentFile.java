package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "content_file")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_path")
    @Lob    // Add this annotation
    @Basic(fetch = FetchType.LAZY)
    private String filePath;

    @ManyToMany
    @JoinTable(
            name = "content_file_file_type",
            joinColumns = @JoinColumn(name = "file_id"),
            inverseJoinColumns = @JoinColumn(name = "file_type_id")
    )
    private List<FileType> fileTypes;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "content_id", referencedColumnName = "content_id", nullable = false)
    private CommunicationContent communicationContent;

    @Column(name = "size", nullable = false)
    private Long size;
}
