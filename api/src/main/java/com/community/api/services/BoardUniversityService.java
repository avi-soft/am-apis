package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.BoardUniversity;
import com.community.api.entity.BoardUniversity;
import com.community.api.entity.ServiceProviderTestStatus;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.community.api.component.Constant.FIND_ALL_QUALIFICATIONS_QUERY;

@Service
public class BoardUniversityService
{
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandlingService;
    @Autowired
    private BoardUniversityService boardUniversityService;
    @Autowired
    private ResponseService responseService;

    @Transactional
    public BoardUniversity addBoardUniversity(@RequestBody BoardUniversity boardUniversity) {
        BoardUniversity boardUniversityToBeSaved =new BoardUniversity();
        long id = findCount() + 1;
        if (boardUniversity.getBoard_university_name() == null || boardUniversity.getBoard_university_name().trim().isEmpty()) {
            throw new IllegalArgumentException("Board or University name cannot be empty or consist only of whitespace");
        }
        if (boardUniversity.getBoard_university_code() == null || boardUniversity.getBoard_university_code().trim().isEmpty()) {
            throw new IllegalArgumentException("Board or University code cannot be empty or consist only of whitespace");
        }
        if (boardUniversity.getBoard_university_type() == null) {
            throw new IllegalArgumentException("Board or University type cannot be empty or consist only of whitespace");
        }
        if (boardUniversity.getBoard_university_location() == null || boardUniversity.getBoard_university_location().trim().isEmpty()) {
            throw new IllegalArgumentException("Board or University type cannot be empty or consist only of whitespace");
        }
        if (!boardUniversity.getBoard_university_name().matches("^[a-zA-Z ]+$")) {
            throw new IllegalArgumentException("Board or University name cannot contain numeric values or special characters");
        }
        if (boardUniversity.getBoard_university_code().matches("^[a-zA-Z ]+$")){
            throw new IllegalArgumentException("Board or university code cannot contain numeric values or special characters");
        }
        if (boardUniversity.getBoard_university_type().matches("^[a-zA-Z ]+$")){
            throw new IllegalArgumentException("Board or university type cannot contain numeric values or special characters");
        }
        if(!boardUniversity.getBoard_university_type().equalsIgnoreCase("BOARD") || !boardUniversity.getBoard_university_type().equalsIgnoreCase("UNIVERSITY"))
        {
            throw new IllegalArgumentException("Board or university type can be either 'BOARD' or 'UNIVERSITY'");
        }

        List<BoardUniversity> boardUniversities = getAllBoardUniversities();
        for (BoardUniversity existingBoardUniversity : boardUniversities) {
            if (existingBoardUniversity.getBoard_university_name().equalsIgnoreCase(boardUniversity.getBoard_university_name())) {
                throw new IllegalArgumentException("BoardUniversity with the same name already exists");
            }
            if (existingBoardUniversity.getBoard_university_code().equalsIgnoreCase(boardUniversity.getBoard_university_code())) {
                throw new IllegalArgumentException("BoardUniversity with the same code already exists");
            }
        }
        boardUniversityToBeSaved.setId(id);
        boardUniversityToBeSaved.setBoard_university_name(boardUniversity.getBoard_university_name());
        boardUniversityToBeSaved.setBoard_university_location(boardUniversity.getBoard_university_location());
        boardUniversityToBeSaved.setBoard_university_code(boardUniversity.getBoard_university_code());
        boardUniversityToBeSaved.setBoard_university_type(boardUniversity.getBoard_university_type());
//        boardUniversityToBeSaved.setCreated_by();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        boardUniversityToBeSaved.setCreated_date(now);
        entityManager.persist(boardUniversityToBeSaved);
        return boardUniversityToBeSaved;
    }

    public List<BoardUniversity> getAllBoardUniversities() {
        TypedQuery<BoardUniversity> query = entityManager.createQuery(Constant.FIND_ALL_BOARD_UNIVERSITY_QUERY, BoardUniversity.class);
        List<BoardUniversity> boardUniversityList = query.getResultList();
        return boardUniversityList;
    }

    //need to be change here
    public long findCount() {
        String queryString = Constant.GET_BOARD_UNIVERSITY_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
}
