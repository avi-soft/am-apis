    package com.community.api.entity;

    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    import javax.persistence.*;

    @Entity
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Table(name = "board_university")
    public class BoardUniversity
    {
        @Id
        private Long board_university_id;

        @Column(name="board_university_name",nullable = false)
        private String board_university_name;

        @Column(name = "board_university_location", nullable = false)
        private String board_university_location;

        @Column(name = "board_university_code",nullable = false)
        private String board_university_code;

        @Column(name= "board_university_type",nullable = false)
        private String board_university_type;

        @Column(name = "created_date", updatable = false)
        private String created_date;

        @Column(name = "modified_date")
        private String modified_date;

        @Column(name = "created_by")
        private String created_by;

        @Column(name = "modified_by")
        private String modified_by;
    }
